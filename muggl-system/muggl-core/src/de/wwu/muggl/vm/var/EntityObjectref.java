package de.wwu.muggl.vm.var;

import java.io.PrintStream;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.TypeCheckException;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.expressions.array.cstr.SymbolicCharArrayEqual;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.gen.meta.SymoblicEntityFieldGenerationException;
import de.wwu.muggl.vm.var.gen.types.EntityStringObjectref;
import de.wwu.muggl.vm.var.sym.gen.ctr.SymbolicStaticEntityConstraints;

public class EntityObjectref extends Objectref implements Variable, ReferenceValue, Comparable {

	
	protected SymoblicEntityFieldGenerator fieldGenerator;
	
	protected SymbolicStaticEntityConstraints staticConstraints;
	
	protected String name;
	protected Objectref referenceValue;
	
	protected EntityObjectref requiredEntity;
	
	protected boolean canBeNull;
	protected boolean isNull;
	
	public EntityObjectref(SymoblicEntityFieldGenerator fieldGenerator, String name, Objectref referenceValue) {
		this(fieldGenerator, name, referenceValue, null);
	}
	
	public EntityObjectref(SymoblicEntityFieldGenerator fieldGenerator, String name, Objectref referenceValue, EntityObjectref requiredEntity) {
		super(referenceValue.getInitializedClass(), false);
		this.name = name;
		this.referenceValue = referenceValue;
		this.fieldGenerator = fieldGenerator;
		this.requiredEntity = requiredEntity;
		this.canBeNull = false;
		this.staticConstraints = new SymbolicStaticEntityConstraints(fieldGenerator.getVM());
	}
	
	public void setRequiredEntity(EntityObjectref requiredEntity) {
		this.requiredEntity = requiredEntity;
	}
	
	public EntityObjectref getRequiredEntity() {
		return this.requiredEntity;
	}
	
	@Override
	public Object getField(Field field) {
		Object fieldValue = this.fields.get(field);
		if(fieldValue == null) {
			try {
				fieldValue = fieldGenerator.generateSymbolicEntityField(this.name+"."+field.getName(), field, this);
				
				if(fieldValue instanceof EntityObjectref) {
					try {
						staticConstraints.addStaticConstraints((EntityObjectref)fieldValue);
					} catch (VmRuntimeException e) {
						e.printStackTrace();
					}
					((EntityObjectref)fieldValue).setCanBeNull(true);
				}
				
				if(fieldValue instanceof ReferenceArrayListVariable) {
					ReferenceArrayListVariable refArray = (ReferenceArrayListVariable)fieldValue;
					refArray.setEntityOwner(this);
					refArray.setEntityOwnerListField(field);
				}
				
				this.fields.put(field, fieldValue);
				
				
				if(this.requiredEntity != null) {
					if(fieldValue instanceof EntityStringObjectref) {
						EntityStringObjectref reqFiedl = (EntityStringObjectref)fieldGenerator.generateSymbolicEntityField(this.requiredEntity.name+"."+field.getName(), field, this);
						SymbolicArrayref s1 = (SymbolicArrayref)reqFiedl.valueMap().get("value");
						SymbolicArrayref s2 = (SymbolicArrayref)((EntityStringObjectref) fieldValue).valueMap().get("value");
						fieldGenerator.getVM().getSolverManager().addConstraint(new SymbolicCharArrayEqual(s1, s2));
						this.requiredEntity.fields.put(field, reqFiedl);
					}
					if(fieldValue instanceof NumericVariable) {
						NumericVariable reqNV = (NumericVariable)fieldGenerator.generateSymbolicEntityField(this.requiredEntity.name+"."+field.getName(), field, this);
						fieldGenerator.getVM().getSolverManager().addConstraint(NumericEqual.newInstance(reqNV, (NumericVariable)fieldValue));
						this.requiredEntity.fields.put(field, reqNV);
					}
					if(fieldValue instanceof EntityObjectref) {
						EntityObjectref reqEO = (EntityObjectref)fieldGenerator.generateSymbolicEntityField(this.requiredEntity.name+"."+field.getName(), field, this);
						((EntityObjectref)fieldValue).setRequiredEntity(reqEO);
						this.requiredEntity.putField(field, reqEO);
						try {
							staticConstraints.addStaticConstraints(reqEO);
						} catch (VmRuntimeException e) {
							e.printStackTrace();
						}
					}
				}
				
				
			} catch (SymoblicEntityFieldGenerationException e) {
				e.printStackTrace();
			}
		}
		return fieldValue;
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
		return "EntityReference: [name="+this.name + " to object: " + this.referenceValue.getObjectId() +"]";
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
		if(o instanceof EntityObjectref) {
			EntityObjectref other = (EntityObjectref)o;
			return other.getObjectId().compareTo(this.getObjectId());
		}
		return -1;
	}

	
	public void setCanBeNull(boolean canBeNull) {
		this.canBeNull = canBeNull;
	}

	public boolean canBeNull() {
		return this.canBeNull;
	}
	
	public void setIsNull(boolean isNull) {
		this.isNull = isNull;
	}

	public boolean isNull() {
		return this.isNull;
	}
}
