package de.wwu.muggl.solvers;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * Represents one valid solution for a specific system of constraints. If
 * definitively no satisfying values for a given set of constraints exists
 * the constant NO_SOLUTION can be used to indicate this.
 * @author Christoph Lembeck
 * @see de.wwu.muggl.solvers.Solution#NOSOLUTION
 */
public class Solution {

    /**
     * Represents that for a given set of constraints definitively no valid
     * solution exists (e.g. the constraints are contradictory). The constant
     * Solution.NOSOLUTION should not be used to indicate that a solver
     * was not able to find a solution for a given set of constraints it is not
     * able to deal with.
     */
    public static final Solution NOSOLUTION = new Solution(){
	@Override
	public void addBinding(Variable var, Constant value){}
	@Override
	public Solution join(Solution s2){return this;}
	@Override
	public String toString(){return "{empty}";}

	/**
	 * Writes the state of the actual solution object to the passed PrintStream
	 * object.
	 * @param logStream the stream the actual state schould be written into.
	 */
	@Override
	public void writeToLog(PrintStream logStream){
	    logStream.println("<nosolution />");
	}
    };

    /**
     * The mapping from the variables (keys of the hash table) to their dedicated
     * values
     */
    protected Hashtable<Variable, Constant> bindings;

    /**
     * Creates a new, empty Soluton object.
     */
    public Solution(){
	bindings = new Hashtable<Variable, Constant>();
    }
    
    /**
     * Adds a new mapping from the specified variable to the given value. Note
     * that the Solution object may not have different bindings for the same
     * variable.
     * @param var the variable a value should be assigned to.
     * @param value the value thet should be assigned to the variable.
     */
    public void addBinding(Variable var, Constant value){
	if (bindings.containsKey(var))
	    throw new IllegalArgumentException();
	bindings.put(var, value);
    }

    /**
     * Checks whether the solution contains the passed variable <i>var</i> or
     * not.
     * @param var the variable that should be searched for in the solution.
     * @return <i>true</i> if the variable is member of the solution,
     * <i>false</i> otherwise.
     */
    public boolean containsVariable(Variable var){
	return bindings.containsKey(var);
    }

    @Override
    public boolean equals(Object s){
	return (this == s) || (s instanceof Solution && s != NOSOLUTION && bindings.equals(((Solution)s).bindings));
    }

    /**
     * Returns the value that was assigned to the given variable as a
     * BooleanConstant.
     * @param var the variable a value should be searched for.
     * @return the boolean value that was assigned to the given variable.
     * @see #getNumericValue(Variable)
     * @see #getValue(Variable)
     */
    public BooleanConstant getBooleanValue(Variable var){
	return (BooleanConstant)bindings.get(var);
    }

    /**
     * Returns the value that was assigned to the given variable as a
     * NumericConstant.
     * @param var the variable a value should be searched for.
     * @return the numeric value that was assigned to the given variable.
     * @see #getBooleanValue(Variable)
     * @see #getValue(Variable)
     */
    public NumericConstant getNumericValue(Variable var){
	return (NumericConstant)bindings.get(var);
    }

    /**
     * Returns the value that was assigned to the given variable.
     * @param var the variable a value should be searched for.
     * @return the value that was assigned to the given variable.
     * @see #getNumericValue(Variable)
     * @see #getBooleanValue(Variable)
     */
    public Constant getValue(Variable var){
	return bindings.get(var);
    }

    @Override
    public int hashCode(){
	return bindings.hashCode();
    }

    /**
     * Combines the mappings of the passed solution object to the mappings
     * contained in the actual solution and returns a new Solution object
     * that contains the union of both sets of bindings.
     * @param solution2 the solution object whose binding should be added to the
     * actual set of bindings to form a new Solution object.
     * @return the new Solution containing the union of the sets of bindings of
     * the actual object and the passed solution object.
     */
    public Solution join(Solution solution2){
	if (solution2.equals(NOSOLUTION))
	    return NOSOLUTION;
	Solution result = new Solution();
	Enumeration<Variable> variables = bindings.keys();
	while (variables.hasMoreElements()){
	    Variable var = variables.nextElement();
	    result.addBinding(var, getValue(var));
	}
	variables = solution2.bindings.keys();
	while (variables.hasMoreElements()){
	    Variable var = variables.nextElement();
	    if (result.containsVariable(var) && !result.getValue(var).equals(solution2.getValue(var)))
		return NOSOLUTION;
	    result.addBinding(var, solution2.getValue(var));
	}
	return result;
    }

    /**
     * TODOME: doc!
     * @param var
     * @param value
     */
    public void replaceBinding(Variable var, Constant value){
	bindings.put(var, value);
    }

    /**
     * Returns a String representation of the actual Solution object.
     * @return the String representation of the actual Solution object.
     */
    @Override
    public String toString(){
	return toString(false, false);
    }
    
    public String toString(boolean includeInternalVariables, boolean useInternalVariableNames){
	Set<Variable> outputVars;
	if (includeInternalVariables)
	    outputVars = bindings.keySet();
	else{
	    outputVars = new TreeSet<Variable>();
	    for (Variable var: bindings.keySet())
		if (!var.isInternalVariable())
		    outputVars.add(var);
	}
	StringBuffer result = new StringBuffer();
	result.append('{');
	int size = outputVars.size();
	for (Variable var: outputVars){
	    size--;
	    result.append(var.toString(useInternalVariableNames));
	    result.append('=');
	    result.append(bindings.get(var));
	    if (size > 0)
		result.append(", ");
	}
	result.append('}');
	return result.toString();
    }

    /**
     * Returns a representation of this solution as a latex expression.
     * @return a representation of this solution as a latex expression.
     */
    public String toTexString(boolean useInternalVariableNames){
	StringBuffer sb = new StringBuffer();
	sb.append("\\{$");
	int i = 0;
	for (Variable var: bindings.keySet()){
	    if (i > 0)
		sb.append(", ");
	    sb.append(var.toTexString(false, useInternalVariableNames));
	    sb.append('=');
	    sb.append(getValue(var).toTexString());
	    i++;
	}
	sb.append("$\\}");
	return sb.toString();
    }

    /**
     * Returns an enumeration of the variabels contained in the solution.
     * @return an enumeration of the variabels contained in the solution.
     */
    public Set<Variable> variables(){
	return bindings.keySet();
    }

    /**
     * Writes the state of the actual solution object to the passed PrintStream
     * object.
     * @param logStream the stream the actual state schould be written into.
     */
    public void writeToLog(PrintStream logStream){
	logStream.println("<solution>");
	Enumeration<Variable> variables = bindings.keys();
	while (variables.hasMoreElements()){
	    Variable var = variables.nextElement();
	    logStream.print("<binding variablename=\"");
	    logStream.print(var.getName());
	    logStream.print("\" type=\"");
	    logStream.print(Term.getTypeName(var.getType()));
	    logStream.print("\" value=\"");
	    logStream.print(getValue(var).toString());
	    logStream.println("\" />");
	}
	logStream.println("</solution>");
    }

    public Solution getSubsolution(Set<Variable> variables){
	Solution result = new Solution();
	for (Variable var: variables){
	    Constant value = getValue(var);
	    if (value == null)
		throw new IllegalArgumentException(var + " is not a member of " + toString());
	    else
		result.addBinding(var, value);
	}
	return result;
    }

}