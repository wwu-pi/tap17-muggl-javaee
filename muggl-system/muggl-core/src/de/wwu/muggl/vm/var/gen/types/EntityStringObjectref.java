package de.wwu.muggl.vm.var.gen.types;

import java.io.PrintStream;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;

public class EntityStringObjectref extends Objectref implements Variable, ReferenceValue, Comparable {

	protected String name;
	protected Objectref referenceValue;
	
	public EntityStringObjectref(String name, Objectref referenceValue) {
		super(referenceValue.getInitializedClass(), false);
		this.name = name;
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
		return "EntityStringObjectref: [name="+this.name + " to object: " + this.referenceValue.getObjectId() +"]";
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
	public Object getField(Field field) {
		return super.getField(field);
	}
	
	@Override
	public void putField(Field field, Object value) {
		super.putField(field, value);
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
		if(o instanceof EntityStringObjectref) {
			EntityStringObjectref other = (EntityStringObjectref)o;
			return other.getObjectId().compareTo(this.getObjectId());
		}
		return -1;
	}

}
