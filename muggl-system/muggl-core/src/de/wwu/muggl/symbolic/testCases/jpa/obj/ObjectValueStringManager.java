package de.wwu.muggl.symbolic.testCases.jpa.obj;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;

public class ObjectValueStringManager {
	
	protected Solution solution;
	
	public ObjectValueStringManager(Solution solution) {
		this.solution = solution;
	}
	
	public String getFieldValueString(StringBuilder sb, Object object) {
		
		return null;
	}

	public String getNumericFieldValueString(Object object) {
		if(object instanceof NumericConstant) {
			return getNumericConstantValue((NumericConstant)object);
		}
		
		if(object instanceof NumericVariable) {
			getNumericVariableValue((NumericVariable)object);
		}
		
		return "<IMPLME>";
	}
	
	protected String getNumericConstantValue(NumericConstant nc) {
		byte type = nc.getType();
		if(type == Expression.INT) {
			return nc.getIntValue()+"";
		}
		if(type == Expression.FLOAT) {
			return nc.getFloatValue()+"";
		}
		if(type == Expression.DOUBLE) {
			return nc.getDoubleValue()+"";
		}
		if(type == Expression.LONG) {
			return nc.getLongValue()+"L";
		}
		return "<IMPLME>";
	}
	
	protected String getNumericVariableValue(NumericVariable nv) {
		Constant c = solution.getValue(nv);
		return getNumericConstantValue((NumericConstant)c);
	}

}
