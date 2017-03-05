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

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.database.meta.FindChoicePointOptions;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.ObjectNullRef;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.sym.gen.ctr.SymbolicStaticEntityConstraints;

public class EntityManagerFindChoicePoint implements ChoicePoint {

	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;
	
	protected Queue<FindChoicePointOptions> options;
	
	private EntityReferenceGenerator entityRefGenerator;
	
	// this is the entity reference that is to be found / not found 
	private ReferenceVariable entityReference;
	
	// this is the id field of the entity reference;
	private String idFieldName;
		
	// the virtual machine
	protected JPAVirtualMachine vm;
	
	// data of the entity to be found
	protected String entityName; // the name of the entity that is to be found
	protected Object idReference; // the reference to the id field of the entity to be found
	
	// entity objects in the required data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> requiredObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> dataObjectIds;
	
	protected SymbolicStaticEntityConstraints entityConstraints;
	
	public EntityManagerFindChoicePoint(
			Frame frame, 
			int pc, 
			int pcNext, 
			ChoicePoint parent, 
			String entityName, 
			Object idReference)  {		
		
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		this.entityName = entityName;
		this.idReference = idReference;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		this.options = new LinkedList<>();
				
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		
		if(!(frame.getVm() instanceof JPAVirtualMachine)) {
			throw new RuntimeException("Cannot create choice point to find JPA entity if not JPA Virtual Machine is started");
		}
		this.vm = (JPAVirtualMachine)frame.getVm();
		
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
		
		this.entityRefGenerator = new EntityReferenceGenerator(vm);
		
		this.entityConstraints = new SymbolicStaticEntityConstraints(vm);
		
		createChoicePoint();
	}
	
	private void createChoicePoint() {
		options.add(FindChoicePointOptions.ENTITY_EXISTS);
		if(!doesEntityWithIdExist(this.entityName, this.idReference)) {
			options.add(FindChoicePointOptions.ENTITY_NOT_EXISTS);
		}
		
		if(canIdReferenceBeNull() && false) {
			options.add(FindChoicePointOptions.ID_NULL_REFERENCE);
		}
	}
	
	protected boolean doesEntityWithIdExist(String entityName, Object idValue) {
		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityName);
		
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
	
	// check if the given reference can be null...
	protected boolean canIdReferenceBeNull() {
		if(this.idReference instanceof ReferenceVariable) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean hasAnotherChoice() {
		return options.size() > 0;
	}
	
	
	@Override
	public void applyStateChanges() throws VmRuntimeException {
		if(this.vm.getSolverManager().getConstraintLevel() > this.constraintLevel) {
			this.vm.getSolverManager().resetConstraintLevel(this.constraintLevel);
			this.vm.getVirtualObjectDatabase().resetDatabase(this.constraintLevel);
		}
		
		FindChoicePointOptions state = options.poll();
		
		try {
			String text = "APPLY: "+this.getClass().getSimpleName()+ " -> state=["+state+"] class.hashCode="+this.hashCode() + "\r\n";
		    Files.write(Paths.get("C:/WORK/log/cp.txt"), text.getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
		
		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData(), this.requiredObjectIds);
		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getData(), this.dataObjectIds);
		
		switch(state) {
			case ENTITY_EXISTS: applyEntityExistent(); return;
			case ENTITY_NOT_EXISTS: applyNonEntityExistent(); return;
			case ID_NULL_REFERENCE: applyIdNullReference(); return;
		}
	}
	
	@Deprecated
	private void resetRequirdData() {
		Map<String, Set<DatabaseObject>> reqData = this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData();
		for(String e : reqData.keySet()) {
			Set<DatabaseObject> e_data = reqData.get(e);
			Set<DatabaseObject> remove_e_data = new HashSet<>(); 
			for(DatabaseObject d : e_data) {
				if(!this.requiredObjectIds.contains(d.getObjectId())) {
					remove_e_data.add(d);
				}
			}
			for(DatabaseObject d : remove_e_data) {
				e_data.remove(d);
			}
		}
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
	
	private void applyEntityExistent() {
//		this.entityReference = entityRefGenerator.generateNewEntityReference(this.entityName);
//		this.idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityReference.getInitializedClassName());
//		Field idField = entityReference.getInitializedClass().getClassFile().getFieldByName(idFieldName);
//		entityReference.putField(idField, this.idReference);
		
		try {
//			Set<String> idFieldNames = EntityConstraintAnalyzer.getIdFieldNames(this.vm.getMugglEntityManager().getMetamodel(), this.entityName);
			String idFieldName = EntityConstraintAnalyzer.getIdFieldName(this.entityName);
			
			SymoblicEntityFieldGenerator entityFieldGenerator = new SymoblicEntityFieldGenerator(this.vm);
			ClassFile entityClassFile = this.vm.getClassLoader().getClassAsClassFile(this.entityName);

			// generate a new entity object reference for required data
			Objectref req_entityObjectref = this.vm.getAnObjectref(entityClassFile);
			long req_number = req_entityObjectref.getInstantiationNumber();
			String req_entityObjectrefName = "REQ#entity-object#"+entityClassFile.getClassName()+req_number;
			EntityObjectref req_entityObject = new EntityObjectref(entityFieldGenerator, req_entityObjectrefName, req_entityObjectref);
			
			// generate a new entity object reference for operating data
			Objectref data_entityObjectref = this.vm.getAnObjectref(entityClassFile);
			long data_number = data_entityObjectref.getInstantiationNumber();
			String data_entityObjectrefName = "DATA#entity-object#"+entityClassFile.getClassName()+data_number;
			EntityObjectref data_entityObject = new EntityObjectref(entityFieldGenerator, data_entityObjectrefName, data_entityObjectref, req_entityObject);
			
			Field idField = getIdField(entityClassFile, idFieldName);
			
			req_entityObject.putField(idField, idReference);
			data_entityObject.putField(idField, idReference);
			
			if(idReference instanceof NumericVariable) {
				this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance((NumericVariable)idReference, NumericConstant.getInstance(0, Expression.INT)));
			}
			if(idReference instanceof ReferenceVariable && ((ReferenceVariable)idReference).getObjectType().equals("java.lang.String")) {
				ReferenceVariable refVar = (ReferenceVariable)idReference;
				Field valueField = refVar.getInitializedClass().getClassFile().getFieldByName("value");
				SymbolicArrayref symArray = (SymbolicArrayref)refVar.getField(valueField);
				this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(symArray.getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
			}
			
			this.entityConstraints.addStaticConstraints(req_entityObject);
			this.entityConstraints.addStaticConstraints(data_entityObject);
			
//			// check if composite primary key or not...
//			if(idFieldNames.size() > 1) {
//				for(String idFieldName : idFieldNames) {
//					Field idField = entityClassFile.getFieldByName(idFieldName);
//					// idReference must be an objectref, i.e. a composite key class
//					Object idValue = ((Objectref)idReference).valueMap().get(idFieldName);
//					if(idValue != null) {
//						req_entityObject.putField(idField, idValue);
//						data_entityObject.putField(idField, idValue);
//					}
//				}
//			} else {
//				String idFieldName = (String)idFieldNames.toArray()[0];
//				Field idField = entityClassFile.getFieldByName(idFieldName);
//				req_entityObject.putField(idField, idReference);
//				data_entityObject.putField(idField, idReference);
//			}
		
			this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(this.vm.getSolverManager(), this.entityName, req_entityObject);
			this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), this.entityName, data_entityObject);
			
			this.frame.getOperandStack().push(data_entityObject);
		} catch(Exception e) {
			throw new RuntimeException("Error while generating entity objects to find in database");
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
	
	private void applyNonEntityExistent() {
		try {
			// apply constraints that id is not null, etc..
			if(this.idReference instanceof NumericVariable) {
				this.vm.getSolverManager().addConstraint(GreaterThan.newInstance((NumericVariable)this.idReference, NumericConstant.getZero(Expression.INT)));
			} else if(this.idReference instanceof ReferenceVariable && ((ReferenceVariable)this.idReference).getObjectType().equals("java.lang.String")) {
				Field valueField = ((ReferenceVariable)this.idReference).getInitializedClass().getClassFile().getFieldByName("value");
				Object valueValue = ((ReferenceVariable)this.idReference).getField(valueField);
				if(valueValue instanceof SymbolicArrayref) {
					this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(((SymbolicArrayref)valueValue).getSymbolicLength(), NumericConstant.getZero(Expression.INT)));
				}
			}
			
			InitializedClass ic = this.vm.getClassLoader().getClassAsClassFile(entityName).getInitializedClass();
			this.frame.getOperandStack().push(new ObjectNullRef(ic));
		} catch (ClassFileException e) {
			throw new RuntimeException("Could not get class file: " + entityName);
		}
	}

	private void applyIdNullReference() {
		frame.setPc(this.pc);
		VmRuntimeException e = new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalArgumentException"));
		SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
		try {
			handler.handleException();
		} catch (ExecutionException e2) {

		}
	}
	
	
	
	
	
	
	

	private void entityMustExistInDatabase() {
		this.entityReference = entityRefGenerator.generateNewEntityReference(this.entityName);
		this.idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityReference.getInitializedClassName());
		Field idField = entityReference.getInitializedClass().getClassFile().getFieldByName(idFieldName);
		entityReference.putField(idField, this.idReference);
		this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(this.vm.getSolverManager(), entityReference.getInitializedClassName(), entityReference);
		this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), entityReference.getInitializedClassName(), entityReference);
		this.frame.getOperandStack().push(entityReference);
	}
	
	private void entityMustNotExistInDatabase() {
		Object idFieldValue = this.entityReference.valueMap().get(this.idFieldName);
		// TODO: der wert ovn idFieldValue darf in den pre-execution required data nicht vorkommen
		Set<DatabaseObject> data = this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData().get(this.entityName);
		data.remove(this.entityReference);
		ReferenceVariable oldRefVar = (ReferenceVariable) this.frame.getOperandStack().pop();
		this.frame.getOperandStack().push(new ObjectNullRef(oldRefVar.getInitializedClass()));
	}
	
	private void entityIdReferenceMustBeNullReference() throws VmRuntimeException {
		System.out.println("c");
//		((SymbolicVirtualMachine)frame.getVm()).setDoNotTryToTrackBack(true);
		throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalArgumentException"));
	}
	
	@Override
	public long getNumber() {
		return this.number;
	}
	
	@Override
	public void changeToNextChoice() throws NoExceptionHandlerFoundException {
		
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
		return false;
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
		return "JPA EntityManager#find Choice Point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
