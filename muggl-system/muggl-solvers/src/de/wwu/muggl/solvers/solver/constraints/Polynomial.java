package de.wwu.muggl.solvers.solver.constraints;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncompleteSolutionException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.numbers.DoubleWrapper;
import de.wwu.muggl.solvers.solver.numbers.Fraction;
import de.wwu.muggl.solvers.solver.numbers.NumberFactory;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;
import de.wwu.muggl.solvers.solver.tsolver.bisection.Hypercube;
import de.wwu.muggl.solvers.solver.tsolver.bisection.MultiIndex;
import de.wwu.muggl.solvers.solver.tsolver.bisection.MultiIndexMap;
import de.wwu.muggl.solvers.solver.tsolver.bisection.MultiIndexVariablesReference;

/**
 * Represents a polynomial that may be contained in an equation, or in a weak or
 * strong inequation. Polynomials are built by a sum of monomials multiplied by
 * their coefficients and a single constant (if its not equal to 0).
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class Polynomial {

    /**
     * Stores the single constant if one exists or null otherwise.
     */
    protected NumberWrapper constant;

    /**
     * Stores the mapping from the monomials (keys) to their coefficients
     * (values).
     */
    protected TreeMap<Monomial, NumberWrapper> monomials;

    protected NumberFactory numberFactory = new Fraction();

    /**
     * Creates a new Polynomial object containing the passed Monomials with their
     * assigned Coefficients and the passed constant as constant addend.
     * @param monomials the mapping from the monomials to their coefficients.
     * @param constant the constant addend of the new polynomial if exists.
     */
    public Polynomial(Map<Monomial, NumberWrapper> monomials, NumberWrapper constant) {
	if (monomials == null)
	    this.monomials = new TreeMap<Monomial, NumberWrapper>();
	else
	    this.monomials = new TreeMap<Monomial, NumberWrapper>(monomials);
	this.constant = constant;
    }

    /**
     * Creates a new Polynomial object containing the passed Monomials with their
     * assigned Coefficients and the passed constant as constant addend.
     * @param monomials the mapping from the monomials to their coefficients.
     * @param constant the constant addend of the new polynomial if exists.
     */
    public Polynomial(Map<Monomial, NumberWrapper> monomials, NumericConstant constant){
	if (monomials == null)
	    this.monomials = new TreeMap<Monomial, NumberWrapper>();
	else
	    this.monomials = new TreeMap<Monomial, NumberWrapper>(monomials);
	this.constant = numberFactory.getInstance(constant);
    }

    /**
     * Creates an empty polynomial. (For internal use only!)
     */
    public Polynomial(){
	monomials = new TreeMap<Monomial, NumberWrapper>();
    }

    /**
     * Returns the sum of the actual polynomial and the passed polynomial.
     * @param poly the polynomial that the actual polynomial should be added to.
     * @return the sum of the actual polynomial and the passed argument.
     */
    public Polynomial addPolynomial(Polynomial poly){
	Polynomial result = new Polynomial(monomials, constant);
	Iterator<Monomial> monomialIt = poly.monomials.keySet().iterator();
	while (monomialIt.hasNext()){
	    Monomial monomial = monomialIt.next();
	    result.addMonomial(poly.getCoefficient(monomial), monomial);
	}
	if (poly.constant != null)
	    result.addConstant(poly.constant);
	result.checkConsistency();
	return result;
    }

    public void collectNumericVariables(Set<NumericVariable> set){
	for (Monomial monomial: monomials.keySet())
	    monomial.collectNumericVariables(set);
    }

    /**
     * Adds all contained variables to the passed set.
     * @param set the set the contaiend variables should be added to.
     */
    public void collectVariables(Set<Variable> set){
	for (Monomial monomial: monomials.keySet())
	    monomial.collectVariables(set);
    }

    /**
     * Computes the value of this polynomial by substituting the contained
     * variables by their assiciated values in the passed solution object.
     * @param solution the solution that should be inserted into the polynomial.
     * @return the value of the polynomial after substituting all variables by
     * their values.
     * @throws IncompleteSolutionException if the polynomial contains a variable
     * for that no binding is available in the passed solution.
     */
    public NumberWrapper computeValue(Solution solution) throws IncompleteSolutionException{
	NumberWrapper result = constant;
	for (Monomial monomial: getMonomials()){
	    if (result == null)
		result = getCoefficient(monomial).mult(monomial.compute(solution));
	    else
		result = result.add(getCoefficient(monomial).mult(monomial.compute(solution)));
	}
	return result;
    }

    public NumberWrapper computeValue(Map<NumericVariable, NumberWrapper> map) throws IncompleteSolutionException{
	NumberWrapper result = constant;
	for (Monomial monomial: getMonomials()){
	    if (result == null)
		result = getCoefficient(monomial).mult(monomial.compute(map));
	    else
		result = result.add(getCoefficient(monomial).mult(monomial.compute(map)));
	}
	return result;
    }

    /**
     * Checks whether the polynomial contains the passed variable <i>var</i> or
     * not.
     * @param var the variable that should be searched for in the polynomial.
     * @return <i>true</i> if the variable is member of the polynomial,
     * <i>false</i> otherwise.
     */
    public boolean containsVariable(Variable var){
	Iterator<Monomial> monomialIt = monomials.keySet().iterator();
	while (monomialIt.hasNext()){
	    Monomial monomial = monomialIt.next();
	    if (monomial.containsVariable(var))
		return true;
	}
	return false;
    }

    /**
     * Checks whether this polynomial is equal to the passed object.
     * @param obj the object this polynomial should be compared to.
     * @return <i>true</i> if the passed object is also an Polynomial and
     * its monomials, their coefficients and the constant value are equal.
     */
    @Override
    public boolean equals(Object obj){
	if (obj == this)
	    return true;
	if (obj instanceof Polynomial){
	    Polynomial p = (Polynomial)obj;
	    if (constant == null){
		if (p.constant != null && !p.constant.isZero())
		    return false;
	    } else {
		if (p.constant == null){
		    if (!constant.isZero())
			return false;
		} else
		    if (!constant.equals(p.constant))
			return false;
	    }
	    return monomials.equals(p.monomials);
	} else
	    return false;
    }
    
    /**
     * 
     * @param poly2
     * @return wheter the polynomial without constants is the same as poly2 or not.
     */
    public boolean equalsIgnoreConstant(Polynomial poly2){
	return (poly2 == this) || monomials.equals(poly2.monomials);
    }

    public Hypercube generateHypercube(){
	TreeSet<NumericVariable> vars = new TreeSet<NumericVariable>();
	collectNumericVariables(vars);
	return new Hypercube(new MultiIndexVariablesReference(vars));
    }

    /**
     * Returns the coefficient of the given monomial in the actual polynomial.
     * @param monomial the monomial the coefficient should be searched for.
     * @return the coefficient of the monomial in the actual polynomial or
     * <i>null</i> if the monomial is not contained in the polynomial.
     */
    public NumberWrapper getCoefficient(Monomial monomial){
	return monomials.get(monomial);
    }

    /**
     * Returns the constant addend of the polynomial if exists.
     * @return the constant addend of the polynomial or <i>null</i> of none
     * exists.
     */
    public NumberWrapper getConstant(){
	if (constant != null && constant.isZero())
	    constant = null;
	return constant;
    }

    public TreeMap<NumericVariable, Integer> getDegree(){
	TreeMap<NumericVariable, Integer> result = new TreeMap<NumericVariable, Integer>();
	for (Monomial monomial: monomials.keySet()){
	    TreeMap<NumericVariable, Integer> monomialDegree = monomial.getDegree();
	    for (NumericVariable var: monomialDegree.keySet()){
		Integer currentVal = result.get(var);
		Integer monomialVal = monomialDegree.get(var);
		if (currentVal == null || currentVal.intValue() < monomialVal.intValue())
		    result.put(var, monomialVal);
	    }
	}
	return result;
    }

    public Polynomial getDerivate(NumericVariable var){
	Polynomial derivate = new Polynomial();
	for (Monomial m: monomials.keySet()){
	    int exp = m.getExponent(var);
	    if (exp > 0){
		Monomial md = m.getDerivate(var);
		if (md == null){
		    derivate.addConstant(monomials.get(m).mult(numberFactory.getInstance(exp)));
		} else
		    derivate.addMonomial(monomials.get(m).mult(numberFactory.getInstance(exp)), md);
	    }
	}
	return derivate;
    }

    public int getMonomialCount(){
	return monomials.size();
    }

    /**
     * Returns a set of all monomials contained in the polynomial
     * @return the set of the contained monomials.
     */
    public Set<Monomial> getMonomials(){
	return monomials.keySet();
    }

    public MultiIndexMap getMultiIndexCoefficients(NumberFactory factory){
	TreeSet<NumericVariable> vars = new TreeSet<NumericVariable>();
	collectNumericVariables(vars);
	MultiIndexVariablesReference varRef = new MultiIndexVariablesReference(vars);
	TreeMap<MultiIndex, NumberWrapper> map = new TreeMap<MultiIndex, NumberWrapper>();
	collectMultiIndexCoefficients(factory, map, varRef);
	MultiIndexMap result = new MultiIndexMap(factory, varRef, map);
	return result;
    }

    /**
     * Calculates a hash code out of the coefficients, the monomials, and the
     * constant.
     *
     * @return the has code for this polynomial.
     */
    @Override
    public int hashCode(){
	int constantHash = (constant != null) ? constant.hashCode() : 0;
	return constantHash + monomials.hashCode();
    }

    public Polynomial insert(Assignment assignment){
	Iterator<Monomial> monomialIt = monomials.keySet().iterator();
	Polynomial newPolynomial = new Polynomial();
	if (constant != null)
	    newPolynomial.addConstant(constant);
	while (monomialIt.hasNext()){
	    Monomial monomial = monomialIt.next();
	    Object[] result = monomial.insert(assignment);
	    NumberWrapper monomialVariance = getCoefficient(monomial).mult((NumberWrapper)result[0]);
	    Monomial newMonomial = (Monomial)result[1];
	    if (newMonomial != null)
		newPolynomial.addMonomial(monomialVariance, newMonomial);
	    else
		newPolynomial.addConstant(monomialVariance);
	}
	return newPolynomial;
    }

    public boolean isCompletelyInteger(boolean onlyVariables){
	if (!onlyVariables){
	    if ((constant != null) && (!constant.isInteger()))
		return false;
	    for (NumberWrapper coeff: monomials.values())
		if (!coeff.isInteger())
		    return false;
	}
	for (Monomial monomial: monomials.keySet()){
	    if (!monomial.isCompletelyInteger())
		return false;
	}
	return true;
    }

    /**
     * Checks whether the polynomial contains any monomials.
     * @return <i>true</i> if no monomial is contained, <i>false</i> otherwise.
     */
    public boolean isConstant(){
	return monomials.isEmpty();
    }

    /**
     * Checks whether the polynomial only contains monomials representing single
     * variables having the exponent one or not.
     * @return <i>true</i> if the polynomial represents a linear cimbination of
     * its contained variables, <i>false</i> otherwise.
     */
    public boolean isLinear(){
	Iterator<Monomial> monomialIt = monomials.keySet().iterator();
	while (monomialIt.hasNext()){
	    Monomial monomial = monomialIt.next();
	    if (!monomial.isLinear())
		return false;
	}
	return true;
    }

    public boolean isUnivariate(){
	TreeSet<NumericVariable> vars = new TreeSet<NumericVariable>();
	collectNumericVariables(vars);
	return vars.size() == 1;
    }

    /**
     * Returns the product of the actual polynomial and the passed polynomial.
     * @param poly the polynomial that the actual polynomial should be multiplied
     * with.
     * @return the product of the actual polynomial and the passed argument.
     */
    public Polynomial multiplyPolynomial(Polynomial poly){
	Polynomial result = new Polynomial();
	
	Iterator<Monomial> f1It = monomials.keySet().iterator();
	while (f1It.hasNext()){
	    Monomial monomial1 = f1It.next();
	    Iterator<Monomial> f2It = poly.monomials.keySet().iterator();
	    while (f2It.hasNext()){
		Monomial monomial2 = f2It.next();
		result.addMonomial(getCoefficient(monomial1).mult(poly.getCoefficient(monomial2)), monomial1.multiply(monomial2));
	    }
	}
	if (constant != null){
	    Iterator<Monomial> f2It = poly.monomials.keySet().iterator();
	    while (f2It.hasNext()){
		Monomial monomial2 = f2It.next();
		result.addMonomial(constant.mult(poly.getCoefficient(monomial2)), monomial2);
	    }
	}
	if (poly.constant != null){
	    f1It = monomials.keySet().iterator();
	    while (f1It.hasNext()){
		Monomial monomial1 = f1It.next();
		result.addMonomial(poly.constant.mult(getCoefficient(monomial1)), monomial1);
	    }
	}
	if ((constant != null) && (poly.constant != null))
	    result.addConstant(constant.mult(poly.constant));
	result.checkConsistency();
	return result;
    }

    /**
     * Returns the negated polynomial of this polynomial.
     * @return the negated polynomial.
     */
    public Polynomial negate(){
	Polynomial result = new Polynomial();
	Iterator<Monomial> it = monomials.keySet().iterator();
	while (it.hasNext()){
	    Monomial m = it.next();
	    result.addMonomial(getCoefficient(m).negate(), m);
	}
	if (constant != null){
	    result.addConstant(constant.negate());
	}

	return result;
    }

    public MultiIndexMap scale(NumberFactory factory, Hypercube cube){
	MultiIndexMap old = getMultiIndexCoefficients(factory);
	return old.scale(cube);
    }

    /**
     * Returns the difference of the actual polynomial and the passed polynomial.
     * @param poly the polynomial that should be subtracted from the actual
     * polynomial.
     * @return the differencet of the actual polynomial and the passed argument.
     */
    public Polynomial subtractPolynomial(Polynomial poly){
	Polynomial result = new Polynomial(monomials, constant);
	Iterator<Monomial> monomialIt = poly.monomials.keySet().iterator();
	while (monomialIt.hasNext()){
	    Monomial monomial = monomialIt.next();
	    result.subtractMonomial(poly.getCoefficient(monomial), monomial);
	}
	if (poly.constant != null)
	    result.subtractConstant(poly.constant);
	result.checkConsistency();
	return result;
    }

    /**
     * Returns the String representation of the polynomial.
     * @return the String representation of the polynomial.
     */
    @Override
    public String toString(){
	StringBuffer sb = new StringBuffer();
	Iterator<Monomial> monomialIt = monomials.keySet().iterator();
	while (monomialIt.hasNext()){
	    Monomial monomial = monomialIt.next();
	    NumberWrapper coeff = getCoefficient(monomial);
	    if (!coeff.isLessThanZero())
		sb.append("+");
	    if (coeff.isMinusOne())
		sb.append("-");
	    else
		if (!coeff.isOne())
		    sb.append(coeff.toString());
	    sb.append(monomial.toString());
	}
	if (constant != null && !constant.isZero()){
	    if (!constant.isLessThanZero())
		sb.append("+");
	    sb.append(constant.toString());
	}
	return sb.toString();
    }

    /**
     * Returns a representation of this polynomial as a latex expression.
     * @return a representation of this polynomial as a latex expression.
     */
    public String toTexString(boolean useInternalVariableNames){
	StringBuffer sb = new StringBuffer();
	
	TreeMap<Integer, Monomial> sortedMap = new TreeMap<Integer, Monomial>();
	
	for (Monomial monomial: monomials.keySet()){
	    for (NumericVariable var: monomial.getVariables()){
		sortedMap.put(var.getInternalID(), monomial);
	    }
	}
	
	
	
	//for (Monomial monomial: monomials.keySet()){
	for (Monomial monomial: sortedMap.values()){
	    NumberWrapper coeff = getCoefficient(monomial);
	    if (!coeff.isLessThanZero())
		sb.append("+");
	    if (coeff.isMinusOne())
		sb.append("-");
	    else
		if (!coeff.isOne())
		    sb.append(coeff.toTexString());
	    sb.append(monomial.toTexString(useInternalVariableNames));
	}
	if (constant != null && !constant.isZero()){
	    if (!constant.isLessThanZero())
		sb.append("+");
	    sb.append(constant.toTexString());
	}
	return sb.toString();
    }

    private void collectMultiIndexCoefficients(NumberFactory factory, TreeMap<MultiIndex, NumberWrapper> map, MultiIndexVariablesReference varRef){
	if (this.constant != null && !constant.isZero()){
	    MultiIndex index = new MultiIndex(new int[varRef.getDimension()]);
	    map.put(index, constant);
	}
	for (Monomial monomial: monomials.keySet()){
	    NumberWrapper coefficient = monomials.get(monomial);
	    MultiIndex index = monomial.getMultiIndex(varRef);
	    map.put(index, coefficient);
	}
    }

    /**
     * Adds the passed constant value to this polynomial.
     * @param value the constant that should be added.
     */
    protected void addConstant(NumberWrapper value){
	if (constant == null)
	    constant = value;
	else
	    constant = constant.add(value);
    }

    /**
     * Adds the passed monomial multiplied by its coefficient to this polynomial.
     * @param coefficient the coefficient of the Monomial that should be added.
     * @param monomial the monomial that should be added.
     */
    protected void addMonomial(NumberWrapper coefficient, Monomial monomial){
	if (monomials.containsKey(monomial))
	    coefficient = coefficient.add(getCoefficient(monomial));
	monomials.put(monomial, coefficient);
	checkConsistency();
    }

    /**
     * Removes monomials from the polynomial if their coefficients are equal to
     * zero. The same operation will be done on the constant addend.
     */
    protected void checkConsistency(){
	Iterator<Monomial> monomialIt = monomials.keySet().iterator();
	while (monomialIt.hasNext()){
	    Monomial monomial = monomialIt.next();
	    if (getCoefficient(monomial).isZero())
		monomialIt.remove();
	}
	if ((constant != null) && constant.isZero())
	    constant = null;
    }

    /**
     * Subtracts the passed constant from this polynomial.
     * @param value the constant that should be subtracted.
     */
    protected void subtractConstant(NumberWrapper value){
	if (constant == null)
	    constant = value.negate();
	else
	    constant = constant.sub(value);
    }

    /**
     * Subtracts the passed Monomial multiplied by the passed coefficient from
     * this polynomial.
     * @param coefficient the coefficient of the monomial that should be
     * subtracted.
     * @param monomial the monomial that should be subtracted.
     */
    protected void subtractMonomial(NumberWrapper coefficient, Monomial monomial){
	NumberWrapper newCoefficient = getCoefficient(monomial);
	if (newCoefficient == null)
	    newCoefficient = coefficient.negate();
	else
	    newCoefficient = newCoefficient.sub(coefficient);
	monomials.put(monomial, newCoefficient);
	checkConsistency();
    }
    
    public byte getType(){
	return Expression.DOUBLE;
    }
}


