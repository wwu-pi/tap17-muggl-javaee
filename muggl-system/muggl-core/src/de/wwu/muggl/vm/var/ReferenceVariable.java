package de.wwu.muggl.vm.var;

import java.io.PrintStream;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.gen.meta.SymoblicEntityFieldGenerationException;

public class ReferenceVariable extends Objectref implements Variable, ReferenceValue, Comparable {

	protected JPAVirtualMachine vm;
	
	protected String name;
	protected ReferenceValue referenceValue;
	
	protected SymoblicEntityFieldGenerator fieldGenerator;
		
	public ReferenceVariable(String name, ReferenceValue referenceValue, JPAVirtualMachine vm) {
		super(referenceValue.getInitializedClass(), false);
		this.name = name;
		this.referenceValue = referenceValue;
		this.fieldGenerator = new SymoblicEntityFieldGenerator(vm);
		this.vm = vm;
		addSpecialConstraints();
	}
	
	private void addSpecialConstraints() {
		ClassFile cl = this.referenceValue.getInitializedClass().getClassFile();
		if(cl.getName().equals("java.util.Date")) {
			Field fastTimeField = cl.getFieldByName("fastTime");
			NumericVariable nv = (NumericVariable)this.getField(fastTimeField);
			this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(nv, NumericConstant.getZero(Expression.INT)));
		}
	}
	
	@Override
	public Object getField(Field field) {
		Object fieldValue = this.fields.get(field);
		if(fieldValue == null) {
			try {
				fieldValue = fieldGenerator.generateSymbolicEntityField(this.name+"."+field.getName(), field, this, true);
				this.fields.put(field, fieldValue);
			} catch (SymoblicEntityFieldGenerationException e) {
				e.printStackTrace();
			}
		}
		return fieldValue;
	}
	
	public void resetReferenceValue(ReferenceValue referenceValue) {
		this.referenceValue = referenceValue;
	}

	@Override
	public void checkTypes() throws TypeCheckException {	
	}

	@Override
	public Expression insert(Solution solution,	boolean produceNumericSolution) {
		return null;
	}

	@Override
	public Expression insertAssignment(Assignment assignment) {
		return null;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	@Override
	public boolean isConstant() {
		return false;
	}

	@Override
	public String toString(boolean useInternalVariableNames) {
		return toString();
	}
	
	public String toString() {
		return this.name + " -> ref to: [" + this.referenceValue.getInitializedClass().getClassFile().getName() + "] with id=" + getObjectId();
	}

	@Override
	public byte getType() {
		return 0;
	}

	@Override
	public String toTexString(boolean useInternalVariableNames) {
		return null;
	}

	@Override
	public String toHaskellString() {
		return null;
	}

	@Override
	public String getInternalName() {
		return this.name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String toTexString(boolean inArrayEnvironment, boolean useInternalVariableNames) {
		return null;
	}

	@Override
	public void writeToLog(PrintStream logStream) {
	}

	@Override
	public boolean isInternalVariable() {
		return false;
	}

	@Override
	public boolean isArray() {
		return this.referenceValue.isArray();
	}

	@Override
	public InitializedClass getInitializedClass() {
		return this.referenceValue.getInitializedClass();
	}

	@Override
	public boolean isPrimitive() {
		return this.referenceValue.isPrimitive();
	}

	@Override
	public long getInstantiationNumber() {
		return this.referenceValue.getInstantiationNumber();
	}

	@Override
	public int compareTo(Object o) {
		if(o instanceof ReferenceVariable) {
			ReferenceVariable other = (ReferenceVariable)o;
			return other.getObjectId().compareTo(this.getObjectId());
		}
		return -1;
	}
}
