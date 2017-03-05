package de.wwu.muggl.symbolic.searchAlgorithms.choice.javaee;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.AndList;
import de.wwu.muggl.solvers.expressions.CharVariable;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.array.cstr.SymbolicCharArrayEqual;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.sym.gen.ctr.SymbolicStaticEntityConstraints;

public abstract class AbstractJavaEEChoicePoint implements ChoicePoint {

	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;
	
	// the virtual machine
	protected JPAVirtualMachine vm;
	
	protected SymbolicStaticEntityConstraints entityConstraints;
	
	protected SymoblicEntityFieldGenerator entityFieldGenerator;
	
	// entity objects in the required data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> requiredObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> dataObjectIds;
	
	
	public AbstractJavaEEChoicePoint(
			Frame frame, 
			int pc, 
			int pcNext, 
			ChoicePoint parent) {
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		
		if(!(frame.getVm() instanceof JPAVirtualMachine)) {
			throw new RuntimeException("Cannot create choice point to find JPA entity if not JPA Virtual Machine is started");
		}
		this.vm = (JPAVirtualMachine)frame.getVm();
		
		this.entityFieldGenerator = new SymoblicEntityFieldGenerator(vm);
		
		this.entityConstraints = new SymbolicStaticEntityConstraints(vm);
		
		
		// remember the database state
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
	}
	
	@Override
	public void applyStateChanges() throws VmRuntimeException {
		if(this.vm.getSolverManager().getConstraintLevel() > this.constraintLevel) {
			this.vm.getSolverManager().resetConstraintLevel(this.constraintLevel);
		}
		if(this.vm.getSolverManager().getConstraintLevel() >= this.constraintLevel) {
			this.vm.getVirtualObjectDatabase().resetDatabase(this.constraintLevel);
		}
		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData(), this.requiredObjectIds);
		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getData(), this.dataObjectIds);
		doApplyChanges();
		try {
			if(!this.vm.getSolverManager().hasSolution()) {
				
			}
		} catch (TimeoutException | SolverUnableToDecideException e) {
			e.printStackTrace();
		}
	}
	
	protected abstract void doApplyChanges() throws VmRuntimeException;

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
	
	private ConstraintExpression getStringEqualityConstraint(Objectref reference1, Objectref reference2) {
		Field valueField = ((Objectref)reference1).getInitializedClass().getClassFile().getFieldByName("value");
		Object valueArray1 = reference1.getField(valueField);
		Object valueArray2 = reference2.getField(valueField);
		
		if(valueArray1 instanceof SymbolicArrayref && valueArray2 instanceof SymbolicArrayref) {
			return new SymbolicCharArrayEqual((SymbolicArrayref)valueArray1, (SymbolicArrayref)valueArray2);
		}
		
		if(valueArray1 instanceof Arrayref && valueArray2 instanceof SymbolicArrayref) {
			return getSymbolicAndNonSymoblicArrayConstraint((SymbolicArrayref)valueArray2, (Arrayref)valueArray1);
		}
		
		if(valueArray1 instanceof SymbolicArrayref && valueArray2 instanceof Arrayref) {
			return getSymbolicAndNonSymoblicArrayConstraint((SymbolicArrayref)valueArray1, (Arrayref)valueArray2);
		}
		
		return null;
	}
	
	protected void addEqualConstraint(Object value1, Object value2) {
		if(value1 instanceof NumericVariable && value2 instanceof NumericVariable) {
			this.vm.getSolverManager().addConstraint(NumericEqual.newInstance((NumericVariable)value1, (NumericVariable)value2));
		}
		else if(value1 instanceof Objectref && value2 instanceof Objectref
			&& ((Objectref)value1).getObjectType().equals("java.lang.String")
			&& ((Objectref)value2).getObjectType().equals("java.lang.String")) {
			this.vm.getSolverManager().addConstraint(getStringEqualityConstraint((Objectref)value1, (Objectref)value2));
		}
		else if(value1 instanceof NumericConstant && value2 instanceof NumericVariable) {
			this.vm.getSolverManager().addConstraint(NumericEqual.newInstance((NumericConstant)value1, (NumericVariable)value2));	
		}
		else if(value1 instanceof NumericVariable && value2 instanceof NumericConstant) {
			this.vm.getSolverManager().addConstraint(NumericEqual.newInstance((NumericVariable)value1, (NumericConstant)value2));	
		}
		else {
			throw new RuntimeException("Can not set value equal constraints for value1="+value1 + " and value2="+value2);
		}
	}
	
	protected ConstraintExpression getSymbolicAndNonSymoblicArrayConstraint(SymbolicArrayref symbolicValueArray, Arrayref objectArray) {
		List<ConstraintExpression> list = new ArrayList<>();
		list.add(NumericEqual.newInstance(symbolicValueArray.getSymbolicLength(), NumericConstant.getInstance(objectArray.length, Expression.INT)));
		for(int i=0; i<objectArray.length; i++) {
			CharVariable element = (CharVariable)symbolicValueArray.getElement(i);
			if(element == null) {
				element = new CharVariable(symbolicValueArray.getName()+".element."+i, Expression.INT);
				symbolicValueArray.setElementAt(i, element);
			}
			list.add(NumericEqual.newInstance(element, (IntConstant)objectArray.getElement(i)));
		}
		return new AndList(list);
	}
	
	protected Field getIdField(ClassFile entityClassFile) {
		String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityClassFile.getName());
		return this.getField(entityClassFile, idFieldName);
	}
	
	protected Field getField(ClassFile entityClassFile, String fieldName) {
		try {
			Field field = entityClassFile.getFieldByName(fieldName);
			return field;
		} catch(FieldResolutionError e) {
			try {
				if(entityClassFile.getSuperClassFile() != null) {
					return getField(entityClassFile.getSuperClassFile(), fieldName);
				}
			} catch (ClassFileException e1) {
			}
		}
		throw new RuntimeException("ID field for entity class: " + entityClassFile + " not found");
	}
	
	@Override
	public long getNumber() {
		return this.number;
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
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
	
	
	
	protected boolean isGeneratedIdAttribute(ClassFile classFile) {		
		// check field annotations
		Field idField = getIdField(classFile);
		for(Attribute attribute : idField.getAttributes()) {
			AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
			for(Annotation anno : attributeAnnotation.getAnnotations()) {
				if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/GeneratedValue;")) {
					return true;
				}
			}
		}
		
		// check method annotations
		Method idMethod = null;
		String idMethodName = "get"+idField.getName();
		for(Method m : classFile.getMethods()) {
			if(m.getName().toUpperCase().equals(idMethodName.toUpperCase())) {
				idMethod = m;
				break;
			}
		}
		if(idMethod != null) {
			for(Attribute attribute : idMethod.getAttributes()) {
				if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					for(Annotation anno : attributeAnnotation.getAnnotations()) {
						if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/GeneratedValue;")) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
}
