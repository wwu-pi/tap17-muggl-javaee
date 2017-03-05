package de.wwu.muggl.solvers.expressions;

import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * @author Christoph Lembeck
 */
public class TypeCast extends Term{

    /**
     * Creates a new TypeConversion object representing a typecast of the passed
     * expression from and to the specified types.
     * @param expr the expression that should be casted.
     * @param fromType the type from that the expression should be casted.
     * @param toType the type to that the expression should be casted.
     * @return the new TypeConversion object.
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    public static Term newInstance(Term expr, byte fromType, byte toType){
	if (expr instanceof NumericConstant){
	    NumericConstant constant = (NumericConstant)expr;
	    return constant.castTo(toType);
	}
	if (fromType == toType)
	    return expr;
	return new TypeCast(expr, fromType, toType);
    }

    /**
     * The expression that should be casted to another type.
     */
    protected Term expr;

    /**
     * The original type of the expression.
     */
    protected byte fromType;

    /**
     * The type to which the expression should be castet.
     */
    protected byte toType;

    /**
     * Creates a new TypeConversion object representing a typecast of the passed
     * expression from and to the specified types. This constructor should not be
     * called directly to allow more flexible optimizations on runtime. Please
     * call the static method <code>newInstance</code> instead.
     * @param expr the expression that should be casted.
     * @param fromType the type from that the expression should be casted.
     * @param toType the type to that the expression should be casted.
     * @see #newInstance(Term, byte, byte)
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    private TypeCast(Term expr, byte fromType, byte toType) {
	if ((fromType == Expression.BYTE) || (fromType == Expression.SHORT) || (fromType == Expression.CHAR))
	    fromType = Expression.INT;
	this.fromType = fromType;
	this.toType = toType;
	this.expr = expr;
    }

    @Override
    public void checkTypes() throws TypeCheckException {
	expr.checkTypes();
	byte eType = expr.getType();
	if ((eType == Expression.BYTE) || (eType == Expression.SHORT) || (eType == Expression.CHAR))
	    eType = Expression.INT;
	if (fromType != eType)
	    throw new TypeCheckException(expr.toString() + " is not of the expected type");
	if ((!((fromType == Expression.INT) && (toType == Expression.LONG))) &&
		(!((fromType == Expression.INT) && (toType == Expression.FLOAT))) &&
		(!((fromType == Expression.INT) && (toType == Expression.DOUBLE))) &&
		(!((fromType == Expression.LONG) && (toType == Expression.INT))) &&
		(!((fromType == Expression.LONG) && (toType == Expression.FLOAT))) &&
		(!((fromType == Expression.LONG) && (toType == Expression.DOUBLE))) &&
		(!((fromType == Expression.FLOAT) && (toType == Expression.INT))) &&
		(!((fromType == Expression.FLOAT) && (toType == Expression.LONG))) &&
		(!((fromType == Expression.FLOAT) && (toType == Expression.DOUBLE))) &&
		(!((fromType == Expression.DOUBLE) && (toType == Expression.INT))) &&
		(!((fromType == Expression.DOUBLE) && (toType == Expression.LONG))) &&
		(!((fromType == Expression.DOUBLE) && (toType == Expression.FLOAT))) &&
		(!((fromType == Expression.INT) && (toType == Expression.BYTE))) &&
		(!((fromType == Expression.INT) && (toType == Expression.CHAR))) &&
		(!((fromType == Expression.INT) && (toType == Expression.SHORT))))
	    throw new TypeCheckException("Invalid cast operator");
    }

    @Override
    protected boolean containsAsDenominator(Term t) {
	return expr.containsAsDenominator(t);
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof TypeCast){
	    TypeCast otherCast = (TypeCast)other;
	    return (toType == otherCast.toType) && expr.equals(otherCast.expr);
	}
	return false;
    }

    @Override
    protected Substitution findSubstitution(SubstitutionTable subTable) {
	Substitution result = subTable.lookupSubstitution(this);
	if (result == null)
	    result = expr.findSubstitution(subTable);
	return result;
    }

    @Override
    protected Set<Term> getDenominators() {
	return expr.getDenominators();
    }

    @Override
    protected Modulo getFirstModulo() {
	return expr.getFirstModulo();
    }

    @Override
    protected Quotient getFirstQuotient() {
	return expr.getFirstQuotient();
    }

    /**
     * If this type cast is a narrowing type cast it will be returned directly.
     * If this type cast is not narrowing, it will only be returned if the
     * parameter <i>onlyNarrowing</i> will be set to false. Otherwise the
     * first type cast that can be found in the expression behind this type cast
     * will be returned if one exists.
     * @param onlyNarrowing if set to <i>true</i> the method only returns
     * narrowing type casts, otherwise narrowing and widening type casts.
     * @return the first fitting type cast that can be found using the algorithm
     * mentioned above.
     */
    @Override
    protected TypeCast getFirstTypeCast(boolean onlyNarrowing) {
	if ((!onlyNarrowing) || isNarrowing())
	    return this;
	else
	    return expr.getFirstTypeCast(onlyNarrowing);
    }

    /**
     * Returns the type from that the expression should be casted to another type.
     * @return the type from that the expression should be casted to another type.
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    public byte getFromType(){
	return fromType;
    }

    @Override
    protected Modulo getInmostModulo(){
	return expr.getInmostModulo();
    }

    @Override
    protected Quotient getInmostQuotient() {
	return expr.getInmostQuotient();
    }

    /**
     * Returns the Term expression that is stored inside the type cast.
     * @return the Term expression that is stored inside the type cast.
     */
    public Term getInternalTerm(){
	return expr;
    }

    /**
     * Returns the type to which the contained expression is casted to.
     * @return the type to which the contained expression is casted to.
     * @see de.wwu.muggl.solvers.expressions.Expression
     */
    @Override
    public byte getType() {
	return toType;
    }

    @Override
    public int hashCode(){
	return -expr.hashCode() * (toType + 1);
    }

    @Override
    public Term insertAssignment(Assignment assignment){
	Term exprNew = expr.insertAssignment(assignment);
	if (exprNew instanceof NumericConstant)
	    return ((NumericConstant)exprNew).castTo(toType);
	else
	    return newInstance(exprNew, fromType, toType);
    }

    @Override
    public Term insert(Solution solution, boolean produceNumericSolution){
	Term exprNew = expr.insert(solution, produceNumericSolution);
	if (exprNew instanceof NumericConstant)
	    return ((NumericConstant)exprNew).castTo(toType);
	else
	    return newInstance(exprNew, fromType, toType);
    }

    @Override
    public boolean isConstant(){
	return expr.isConstant();
    }

    /**
     * Checks whether this typecast is a narrowing typecast or not.
     * @return <i>true</i> if this typecast is narrowing, <i>false</i> if it is
     * a widening typecast.
     */
    public boolean isNarrowing(){
	return ((fromType == Expression.BYTE)   && (toType == Expression.CHAR))
	|| ((fromType == Expression.SHORT)  && ((toType == Expression.BYTE) || (toType == Expression.CHAR)))
	|| ((fromType == Expression.CHAR)   && ((toType == Expression.BYTE) || (toType == Expression.SHORT)))
	|| ((fromType == Expression.INT)    && ((toType == Expression.BYTE) || (toType == Expression.SHORT) || (toType == Expression.CHAR)))
	|| ((fromType == Expression.LONG)   && ((toType == Expression.BYTE) || (toType == Expression.SHORT) || (toType == Expression.CHAR) || (toType == Expression.INT)))
	|| ((fromType == Expression.FLOAT)  && ((toType == Expression.BYTE) || (toType == Expression.SHORT) || (toType == Expression.CHAR) || (toType == Expression.INT) || (toType == Expression.LONG)))
	|| ((fromType == Expression.DOUBLE) && ((toType == Expression.BYTE) || (toType == Expression.SHORT) || (toType == Expression.CHAR) || (toType == Expression.INT) || (toType == Expression.LONG) || (toType == Expression.FLOAT)));
    }

    @Override
    protected Term multiply(Term factor) {
	return new TypeCast(expr.multiply(factor), fromType, toType);
    }

    @Override
    public Term substitute(Term a, Term b) {
	if (equals(a))
	    return b;
	else
	    return new TypeCast(expr.substitute(a, b), fromType, toType);
    }

    @Override
    public String toHaskellString(){
	String haskellType = null;
	switch (toType){
	case Expression.BOOLEAN: haskellType = "TypeBoolean"; break;
	case Expression.BYTE: haskellType = "TypeByte"; break;
	case Expression.SHORT: haskellType = "TypeShort"; break;
	case Expression.CHAR: haskellType = "TypeChar"; break;
	case Expression.INT: haskellType = "TypeInt"; break;
	case Expression.FLOAT: haskellType = "TypeFloat"; break;
	case Expression.LONG: haskellType = "TypeLong"; break;
	case Expression.DOUBLE: haskellType = "TypeDouble"; break;
	}
	return "(TypeCast " + haskellType + " " + expr.toHaskellString() + ")";
    }

    @Override
    public Polynomial toPolynomial() {
	if (isNarrowing())
	    throw new InternalError();
	else
	    return expr.toPolynomial();
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + getTypeName(toType) + ")(" + expr.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + getTypeName(toType) + ")(" + expr.toTexString(useInternalVariableNames) + ")";
    }

    @Override
    public Term clearMultiFractions(Set<Term> denominators) {
	Term newExpr = expr.clearMultiFractions(denominators);
	if (expr == newExpr)
	    return this;
	else
	    return TypeCast.newInstance(newExpr, fromType, toType);
    }

    @Override
    protected Quotient getFirstNonintegerQuotient() {
	return expr.getFirstNonintegerQuotient();
    }
}
