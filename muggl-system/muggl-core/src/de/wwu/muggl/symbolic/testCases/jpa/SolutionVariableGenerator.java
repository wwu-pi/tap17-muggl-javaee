package de.wwu.muggl.symbolic.testCases.jpa;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.Variable;

public class SolutionVariableGenerator {

	public StringBuilder generateSolutionVariableString(Solution solution) {
		StringBuilder solutionVariablesValuesStringBuilder = new StringBuilder();
		solutionVariablesValuesStringBuilder.append("\t/*\r\n\t* Solution Variable Values\r\n");
		for(Variable var : solution.variables()) {
			Constant value = solution.getValue(var);
			solutionVariablesValuesStringBuilder.append("\t *   ["+var.getName()+"] = [" + value + "]\r\n");
		}
		solutionVariablesValuesStringBuilder.append("\t */\r\n");
		
		return solutionVariablesValuesStringBuilder;
	}
}
