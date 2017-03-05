package de.wwu.muggl.symbolic.searchAlgorithms.choice.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.persistence.GeneratedValue;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.hsqldb.lib.HashMappedList;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.SolvingException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.database.meta.PersistChoicePointOptions;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.jpa.db.constraints.JPAStaticEntityConstraint;
import de.wwu.muggl.vm.impl.jpa.db.constraints.JPAStaticEntityConstraintManager;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.sym.gen.ctr.SymbolicStaticEntityConstraints;

public class EntityManagerPersistChoicePoint implements ChoicePoint {

	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;
	
	protected PersistChoicePointOptions lastOption;
		
	protected Queue<PersistChoicePointOptions> options;
	
	// the virtual machine
	protected JPAVirtualMachine vm;
	
	protected Objectref entityObject;
	
	// entity objects in the required data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> requiredObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> dataObjectIds;
	
	protected SymbolicStaticEntityConstraints entityConstraints;
		
	public EntityManagerPersistChoicePoint(
			Frame frame, 
			int pc, 
			int pcNext, 
			ChoicePoint parent, 
			Objectref entityObject) {
		
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		this.entityObject = entityObject;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		this.options = new LinkedList<>();
		
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		
		this.vm = (JPAVirtualMachine)frame.getVm();
		
		this.entityConstraints = new SymbolicStaticEntityConstraints(vm);
		
		this.requiredObjectIds = new HashSet<>();
		Map<String, Set<DatabaseObject>> reqData = this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData();
		for(String e : reqData.keySet()) {
			for(DatabaseObject d : reqData.get(e)) {
				this.requiredObjectIds.add(d.getObjectId());
			}
		}
		
		this.dataObjectIds = new HashSet<>();
		Map<String, Set<DatabaseObject>> data = this.vm.getVirtualObjectDatabase().getData();
		for(String e : data.keySet()) {
			for(DatabaseObject d : data.get(e)) {
				this.dataObjectIds.add(d.getObjectId());
			}
		}
		
		
		createChoicePoint();
	}
	
	
	private void createChoicePoint() {
		// check if the entity has an ID attribute marked with @GeneratedValue and the id attribute is currently null
		// in that case, the framework (e.g., Hibernate) ensures that always a 'new' ID attribute is generated,
		// thus, no 'entity_exist' scenario can happen
		if(isGeneratedIdAttribute()) {
			options.add(PersistChoicePointOptions.ENTITY_NON_EXISTENT);
		} else {
			options.add(PersistChoicePointOptions.ENTITY_EXISTENT);
			if(!doesEntityAlreadyExist()) {
				options.add(PersistChoicePointOptions.ENTITY_NON_EXISTENT);
			}
		}
	}

	private boolean isGeneratedIdAttribute() {
		EntityType<?> entityType = null;
		for(EntityType<?> et : this.vm.getMugglEntityManager().getMetamodel().getEntities()) {
			if(et.getJavaType().getName().equals(this.entityObject.getObjectType())) {
				entityType = et;
				break;
			}
		}
		
		String idFieldName = null;
		
		if(entityType != null) {
			try {
				Set<?> set = entityType.getIdClassAttributes();
				if(set.size() > 1) {
					return false; // more than one id attribute, so no generated value possible
				}
				SingularAttribute<?, ?> sa = (SingularAttribute<?, ?>)set.iterator().next();
				idFieldName = sa.getName();
			} catch(IllegalArgumentException e) {
				// no id class, so singular id attribute
				SingularAttribute<?, ?> sa =  entityType.getId(entityType.getIdType().getJavaType());
				idFieldName = sa.getName();
			}
		} else {
			try {
				Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(this.entityObject.getObjectType());
				for(java.lang.reflect.Field f : clazz.getDeclaredFields()) {
					if(f.isAnnotationPresent(GeneratedValue.class)) {
						return true;
					}
				}
			} catch(Exception e) {
			}
		}
		
		if(idFieldName != null) {
			ClassFile classFile = this.entityObject.getInitializedClass().getClassFile();
			Field idField = getIdField(classFile, idFieldName);
			Method idMethod = null;
			String idMethodName = "get"+idFieldName;
			for(Method m : classFile.getMethods()) {
				if(m.getName().toUpperCase().equals(idMethodName.toUpperCase())) {
					idMethod = m;
					break;
				}
			}
			boolean fieldWithAutoGenAnnotation = hasFieldIdGenerationAnnotation(idField);
			if(idMethod != null && !fieldWithAutoGenAnnotation) {
				boolean methodWithAutoGenAnnotation = hasMethodIdGenerationAnnotation(idMethod);
				return methodWithAutoGenAnnotation;
			}
			return fieldWithAutoGenAnnotation;
		}
		
		return false;
	}
	
	private boolean hasFieldIdGenerationAnnotation(Field field) {
		return checkAttributesForGeneratedValue(field.getAttributes(), field.getClassFile().getConstantPool());
	}
	
	private boolean hasMethodIdGenerationAnnotation(Method method) {
		return checkAttributesForGeneratedValue(method.getAttributes(), method.getClassFile().getConstantPool());
	}
	
	private boolean checkAttributesForGeneratedValue(Attribute[] attributes, Constant[] constantPool) {
		for(Attribute a : attributes) {
			if(a instanceof AttributeRuntimeVisibleAnnotations) {
				AttributeRuntimeVisibleAnnotations aa = (AttributeRuntimeVisibleAnnotations) a;
				for(Annotation anno : aa.getAnnotations()) {
					String annotationName = constantPool[anno.getTypeIndex()].getStringValue();
					if(annotationName.equals("Ljavax/persistence/GeneratedValue;")) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private boolean doesEntityAlreadyExist() {
		String entityName = this.entityObject.getObjectType();
		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityName);
		Object idValue = this.entityObject.valueMap().get(idFieldName);
		
		Set<DatabaseObject> requiredData = this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData().get(entityName);
		Set<DatabaseObject> applicationData = this.vm.getVirtualObjectDatabase().getData(entityName);
		
		// check if the entity object is already 'persisted' in the required data
		if(requiredData != null) {
			for(DatabaseObject dbObj : requiredData) {
				Object dbObjIdValue = dbObj.valueMap().get(idFieldName);
				if(dbObjIdValue != null && dbObjIdValue.equals(idValue) || dbObjIdValue == idValue) {
					return true;
				}
			}
		}
		
		// if it is not existent in the required data
		// check if the entity object is already 'persisted' in the application data
		if(applicationData != null) {
			for(DatabaseObject dbObj : applicationData) {
				Object dbObjIdValue = dbObj.valueMap().get(entityName);
				if(dbObjIdValue != null && dbObjIdValue.equals(idValue)) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public long getNumber() {
		return this.number;
	}

	@Override
	public boolean hasAnotherChoice() {
		return options.size() > 0;
	}
	
	
	@Override
	public void applyStateChanges() throws VmRuntimeException {
		if(this.vm.getSolverManager().getConstraintLevel() >= this.constraintLevel) {
			this.vm.getSolverManager().resetConstraintLevel(this.constraintLevel);
			this.vm.getVirtualObjectDatabase().resetDatabase(this.constraintLevel);
		}
		
		PersistChoicePointOptions state = options.poll();
		this.lastOption = state;
		
		try {
			String text = "APPLY: "+this.getClass().getSimpleName()+ " -> state=["+state+"] class.hashCode="+this.hashCode() + "\r\n";
		    Files.write(Paths.get("C:/WORK/log/cp.txt"), text.getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
		
		
		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData(), this.requiredObjectIds);
		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getData(), this.dataObjectIds);
		
		switch(state) {
			case ENTITY_EXISTENT: applyEntityExistent(); return;
			case ENTITY_NON_EXISTENT: applyNonEntityExistent(); return;
		}
	}
	
    public boolean hasLastOptionSymbolicException() {
        return this.lastOption == PersistChoicePointOptions.ENTITY_EXISTENT;
    }
    
    public SymbolicExceptionHandler getSymbolicException() {
        if(this.lastOption == PersistChoicePointOptions.ENTITY_EXISTENT) {
            VmRuntimeException e = new VmRuntimeException(frame.getVm().generateExc("javax.persistence.EntityExistsException"));
            return new SymbolicExceptionHandler(frame, e); 
        }
        return null;
    }
	
	private void resetSymbolicDatabase(Map<String, Set<DatabaseObject>> data, Set<String> allowedObjectIds) {
		for(String e : data.keySet()) {
			Set<DatabaseObject> e_data = data.get(e);
			Set<DatabaseObject> remove_e_data = new HashSet<>(); 
			for(DatabaseObject d : e_data) {
				if(!allowedObjectIds.contains(d.getObjectId())) {
					remove_e_data.add(d);
				}
			}
			for(DatabaseObject d : remove_e_data) {
				e_data.remove(d);
			}
		}
	}


	protected void applyEntityExistent() throws VmRuntimeException {
		// add entity to required data
		SymoblicEntityFieldGenerator entityFieldGenerator = new SymoblicEntityFieldGenerator(this.vm);

		ClassFile classFile = entityObject.getInitializedClass().getClassFile();
		
		Objectref entityObjRef = this.vm.getAnObjectref(classFile);
		EntityObjectref entityObjectref = new EntityObjectref(entityFieldGenerator, this.entityObject.getName(), entityObjRef);
		
		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(this.entityObject.getObjectType());
		Field idField = getIdField(classFile, idFieldName);
		entityObjectref.putField(idField, entityObject.getField(idField));
		
		
		
		this.entityConstraints.addStaticConstraints(entityObjectref);
		
		this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(
				this.vm.getSolverManager(), 
				this.entityObject.getObjectType(), 
				entityObjectref);
		
		frame.setPc(this.pc);
		
		// create exception
		VmRuntimeException e = new VmRuntimeException(frame.getVm().generateExc("javax.persistence.EntityExistsException"));
		SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
		try {
			handler.handleException();
		} catch (ExecutionException e2) {

		}
	}
	
	private Field getIdField(ClassFile entityClassFile, String idFieldName) {
		try {
			Field field = entityClassFile.getFieldByName(idFieldName);
			return field;
		} catch(FieldResolutionError e) {
			try {
				if(entityClassFile.getSuperClassFile() != null) {
					return getIdField(entityClassFile.getSuperClassFile(), idFieldName);
				}
			} catch (ClassFileException e1) {
			}
		}
		throw new RuntimeException("ID field for entity class: " + entityClassFile + " not found");
	}

	
	protected void applyNonEntityExistent() throws VmRuntimeException {
		
		try {
			SymoblicEntityFieldGenerator entityFieldGenerator = new SymoblicEntityFieldGenerator(this.vm);
			ClassFile entityClassFile = this.vm.getClassLoader().getClassAsClassFile(this.entityObject.getObjectType());
						
			// generate a new entity object reference for operating data
			Objectref data_entityObjectref = this.vm.getAnObjectref(entityClassFile);
			long data_number = data_entityObjectref.getInstantiationNumber();
			String data_entityObjectrefName = "DATA#entity-object#"+entityClassFile.getClassName()+data_number;
			EntityObjectref data_entityObject = new EntityObjectref(entityFieldGenerator, data_entityObjectrefName, data_entityObjectref);
			
			Map<String, Object> valueMap = this.entityObject.valueMap();
			for(String fieldName : valueMap.keySet()) {
				Field f = entityClassFile.getFieldByName(fieldName, true);
				Object v = valueMap.get(fieldName);
				if(v != null) {
					data_entityObject.putField(f, v);
				}
			}
			
			this.entityConstraints.addStaticConstraints(data_entityObject);
			
			this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), this.entityObject.getObjectType(), data_entityObject);

		} catch(Exception e) {
			throw new RuntimeException("Error while generating entity objects to find in database", e);
		}
	}
	
	@Override
	public void changeToNextChoice() throws NoExceptionHandlerFoundException {
		System.err.println("chagne to another choice");
	}
	
	@Override
	public Frame getFrame() {
		return this.frame;
	}

	@Override
	public int getPc() {
		return this.pc;
	}

	@Override
	public int getPcNext() {
		return this.pcNext;
	}

	@Override
	public ChoicePoint getParent() {
		return this.parent;
	}
	
	@Override
	public boolean changesTheConstraintSystem() {
		return true;
	}

	@Override
	public ConstraintExpression getConstraintExpression() {
		return null;
	}

	@Override
	public void setConstraintExpression(ConstraintExpression constraintExpression) {
	}

	@Override
	public boolean hasTrail() {
		return true;
	}

	@Override
	public Stack<TrailElement> getTrail() {
		return this.trail;
	}

	@Override
	public void addToTrail(TrailElement element) {
		this.trail.push(element);
	}

	@Override
	public boolean enforcesStateChanges() {
		return true;
	}
	
	@Override
	public String getChoicePointType() {
		return "JPA EntityManager#persist Choice Point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
