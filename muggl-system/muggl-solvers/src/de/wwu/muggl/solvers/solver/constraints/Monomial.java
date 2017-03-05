package de.wwu.muggl.solvers.solver.constraints;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.numbers.Fraction;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;
import de.wwu.muggl.solvers.solver.tsolver.bisection.MultiIndex;
import de.wwu.muggl.solvers.solver.tsolver.bisection.MultiIndexVariablesReference;

/**
 * Represents a single monomial without any constant coefficients.
 * </BR></BR>
 * Monomials are products of variables having integer exponents.
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class Monomial implements Comparable{

    /**
     * Stores pairs of variables and their associated exponents as objects of
     * the type Integer. The keys of the TreeMap will be the variables to which
     * the exponents will be mapped by the TreeMap.
     */
    protected TreeMap<NumericVariable, Integer> variables;

    /**
     * Creates a new Monomial containing the variables contained in the passed
     * map. The keys of the map have to be the variables and the values of the
     * map represent the exponents of each variable wrapped by Integer objects.
     * @param variables the mapping of the variables to their exponents.
     */
    public Monomial(Map<NumericVariable, Integer> variables) {
	this.variables = new TreeMap<NumericVariable, Integer>(variables);
    }

    protected NumberFactory numberFactory = new Fraction();

    /**
     * Creates a new Monomial containing only the passed variable with the passed
     * exponent.
     * @param variable the variable that should become the first member of the
     * monomial.
     * @param exponent the exponent of the passed variable.
     */
    public Monomial(NumericVariable variable, int exponent){
	variables = new TreeMap<NumericVariable, Integer>();
	variables.put(variable, new Integer(exponent));
    }

    public void collectNumericVariables(Set<NumericVariable> set){
	set.addAll(variables.keySet());
    }

    /**
     * Adds all contained variables to the passed set.
     * @param set the set the contaiend variables should be added to.
     */
    public void collectVariables(Set<Variable> set){
	set.addAll(variables.keySet());
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is
     * lexicographically less than, equal to, or greater than the specified
     * object.
     * @param other the other monomial this object should be compared with.
     * @return -1 if this object is lexicographically smaller than the other
     * object, 0 if the objects are equal, and 1 if this object if greater than
     * the other object.
     */
    public int compareTo(Object other) {
	if (other instanceof Monomial){
	    Iterator<NumericVariable> left = variables.keySet().iterator();
	    Iterator<NumericVariable> right = ((Monomial)other).variables.keySet().iterator();
	    while (left.hasNext()){
		if (right.hasNext()){
		    NumericVariable leftVar = left.next();
		    NumericVariable rightVar = right.next();
		    int compareResult = leftVar.compareTo(rightVar);
		    if (compareResult < 0)
			return +1;
		    if (compareResult > 0)
			return -1;
		    if (compareResult == 0){
			int leftExponent = getExponent(leftVar);
			int rightExponent = ((Monomial)other).getExponent(rightVar);
			if (leftExponent < rightExponent)
			    return -1;
			if (leftExponent > rightExponent)
			    return +1;
		    }
		} else return -1;
	    }
	    if (right.hasNext())
		return +1;
	    else
		return 0;
	} else
	    throw new ClassCastException();
    }

    /**
     * Computes the value of this monomial by substituting the contained
     * variables by their assiciated values in the passed solution object.
     * @param solution the solution that should be inserted into the monomial.
     * @return the value of the monomial after substituting all variables by
     * their values.
     * @throws IncompleteSolutionException if the monomial contains a variable
     * for that no binding is available in the passed solution.
     */
    public NumberWrapper compute(Solution solution) throws IncompleteSolutionException{
	NumberWrapper result = null;
	for (NumericVariable variable: getVariables()){
	    NumericConstant numericConstant = solution.getNumericValue(variable);
	    if (numericConstant == null)
		throw new IncompleteSolutionException("no binding for variable " + variable);

	    NumberWrapper value = numberFactory.getInstance(numericConstant);
	    if (result == null)
		result = value.pow(getExponent(variable));
	    else
		result = result.mult(value.pow(getExponent(variable)));
	}
	return result;
    }

    public NumberWrapper compute(Map<NumericVariable, NumberWrapper> map) throws IncompleteSolutionException{
	NumberWrapper result = null;
	for (NumericVariable variable: getVariables()){
	    NumberWrapper value = map.get(variable);
	    if (value == null)
		throw new IncompleteSolutionException("no binding for variable " + variable);
	    if (result == null)
		result = value.pow(getExponent(variable));
	    else
		result = result.mult(value.pow(getExponent(variable)));
	}
	return result;
    }

    /**
     * Checks whether the Monomial contains the passed variable <i>var</i> or not.
     * @param var the variable that should be searched for in the monomial.
     * @return <i>true</i> if the variable is member of the monomial, <i>false</i>
     * otherwise.
     */
    public boolean containsVariable(Variable var){
	return variables.containsKey(var);
    }

    /**
     * Indicates whether some other monomial is "equal to" this one.
     * @param obj the other object this one should be compared with.
     * @return <i>true</i> if the objects are equal, <i>false</i> otherwise.
     */
    @Override
    public boolean equals(Object obj){
	if (obj == this)
	    return true;
	if (obj instanceof Monomial){
	    return this.variables.equals(((Monomial)obj).variables);
	} else
	    return false;
    }
    
    public TreeMap<NumericVariable, Integer> getDegree(){
	return variables;
    }

    public Monomial getDerivate(NumericVariable var){
	if (!containsVariable(var))
	    return null;
	TreeMap<NumericVariable, Integer> vars = new TreeMap<NumericVariable, Integer>();
	for (NumericVariable variable: variables.keySet()){
	    int exp = variables.get(variable);
	    if (variable.equals(var))
		exp--;
	    if (exp > 0)
		vars.put(variable, exp);
	}
	if (vars.size() == 0)
	    return null;
	return new Monomial(vars);
    }

    /**
     * Returns the exponent of the passed variable.
     * @param var the variable the exponent should be returned for.
     * @return the exponent of the passed variable.
     */
    public int getExponent(NumericVariable var){
	Integer result = variables.get(var);
	if (result == null)
	    return 0;
	else
	    return result.intValue();
    }

    public int[] getExponents(){
	int[] result = new int[variables.size()];
	int idx = 0;
	for (Integer i: variables.values()){
	    result[idx++] = i.intValue();
	}
	return result;
    }

    public MultiIndex getMultiIndex(MultiIndexVariablesReference varRef){
	int[] index = new int[varRef.getDimension()];
	for (NumericVariable var: variables.keySet()){
	    index[varRef.getIndex(var)] = variables.get(var).intValue();
	}
	return new MultiIndex(index);
    }

    /**
     * Returns a set of the variables contained in this monomial.
     * @return the set of the variables contained in this monomial.
     */
    public Set<NumericVariable> getVariables(){
	return variables.keySet();
    }

    /**
     * Returns a hash code value for the object.
     * @return the hash code value for the object.
     */
    @Override
    public int hashCode(){
	return variables.hashCode();
    }

    public Object[] insert(Assignment assignment){
	Object[] result = new Object[2];
	if (variables.containsKey(assignment.variable)){
	    if (variables.size() == 1){
		result[0] = numberFactory.getInstance((NumericConstant)assignment.getValue()).pow(variables.get(assignment.variable).intValue());
	    } else {
		TreeMap<NumericVariable, Integer> newMap = new TreeMap<NumericVariable, Integer>();
		Iterator<NumericVariable> variablesIt = variables.keySet().iterator();
		while (variablesIt.hasNext()){
		    NumericVariable variable = variablesIt.next();
		    Integer exponent = variables.get(variable);
		    if (variable.equals(assignment.getVariable()))
			result[0] = numberFactory.getInstance((NumericConstant)assignment.getValue()).pow(exponent.intValue());
		    else
			newMap.put(variable, exponent);
		}
		result[1] = new Monomial(newMap);
	    }
	} else {
	    result[0] = numberFactory.getOne();
	    result[1] = this;
	}
	return result;
    }

    public boolean isCompletelyInteger(){
	for (NumericVariable var: variables.keySet())
	    if (!var.isInteger())
		return false;
	return true;
    }

    /**
     * Checks whether the monomial only consists of a single variable having the
     * exponent 1 or not.
     * @return <i>true></i> if the only member of the monomial is a variable with
     * the index 1, <i>false</i> otherwise.
     */
    public boolean isLinear(){
	if (variables.size() != 1)
	    return false;
	Iterator<Integer> exponentsIt = variables.values().iterator();
	while (exponentsIt.hasNext()){
	    if ((exponentsIt.next()).intValue() != 1)
		return false;
	}
	return true;
    }

    /**
     * Returns the product of the actual monomial and the passed argument.
     * @param factor the monomial that this monomial should be multiplied with.
     * @return the product of the actual monomial and the passed argument.
     */
    public Monomial multiply(Monomial factor){
	Monomial result = new Monomial(variables);
	Iterator<NumericVariable> varIt = factor.variables.keySet().iterator();
	while (varIt.hasNext()){
	    NumericVariable variable = varIt.next();
	    result.variables.put(variable, new Integer(getExponent(variable)+ factor.getExponent(variable)));
	}
	return result;
    }

    /**
     * Returns a String representation of the monomial.
     * @return the String representation of the monomial.
     */
    @Override
    public String toString(){
	StringBuffer sb = new StringBuffer();
	for (NumericVariable var: variables.keySet()){
	    sb.append(var.toString());
	    int exponent = getExponent(var);
	    if (exponent != 1){
		sb.append("^");
		sb.append(exponent);
	    }
	}
	return sb.toString();
    }

    /**
     * Returns a representation of this constraint as a latex expression.
     * @return a representation of this constraint as a latex expression.
     */
    public String toTexString(boolean useInternalVariableNames){
	StringBuffer sb = new StringBuffer();
	for (NumericVariable var: variables.keySet()){
	    sb.append(var.toTexString(false, useInternalVariableNames));
	    int exponent = getExponent(var);
	    if (exponent != 1){
		sb.append("^{");
		sb.append(exponent);
		sb.append("}");
	    }
	}
	return sb.toString();
    }
    
    public byte getType(){
	return Expression.DOUBLE;
    }

}
