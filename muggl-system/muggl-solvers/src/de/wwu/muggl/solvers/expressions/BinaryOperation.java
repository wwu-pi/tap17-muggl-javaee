package de.wwu.muggl.solvers.expressions;


/**
 * @author Christoph Lembeck
 */
public abstract class BinaryOperation extends Term{

    /**
     * Returns the first modulo expression that can be found in this term.
     * @return the first modulo expression that can be found in this term.
     */
    @Override
    protected Modulo getFirstModulo() {
	Modulo m = getLeft().getFirstModulo();
	if (m != null)
	    return m;
	return getRight().getFirstModulo();
    }

    /**
     * Returns the first quotient that can be found in this term.
     * @return the first quotient that can be found in this term.
     */
    @Override
    protected Quotient getFirstQuotient() {
	Quotient q = getLeft().getFirstQuotient();
	if (q != null)
	    return q;
	return getRight().getFirstQuotient();
    }

    @Override
    protected Quotient getFirstNonintegerQuotient() {
	Quotient q = getLeft().getFirstNonintegerQuotient();
	if (q != null)
	    return q;
	return getRight().getFirstNonintegerQuotient();
    }

    /**
     * Returns the first typecast that can be found in one of the arguments of
     * this expression.
     * @param onlyNarrowing if set to <i>true</i> the method only returns
     * narrowig typecasts, otherwise narrowing and widening typecasts.
     * @return the first typecast that can be found in this expression.
     */
    @Override
    protected TypeCast getFirstTypeCast(boolean onlyNarrowing) {
	TypeCast tc = getLeft().getFirstTypeCast(onlyNarrowing);
	if (tc != null)
	    return tc;
	else
	    return getRight().getFirstTypeCast(onlyNarrowing);
    }

    @Override
    protected Modulo getInmostModulo(){
	Modulo result = getLeft().getInmostModulo();
	if (result != null)
	    return result;
	return getRight().getInmostModulo();
    }

    @Override
    protected Quotient getInmostQuotient() {
	Quotient q = getLeft().getInmostQuotient();
	if (q != null)
	    return q;
	return getRight().getInmostQuotient();
    }

    public abstract Term getLeft();

    public abstract Term getRight();

    /**
     * Checks whether the expression is a constant or a combined term containing
     * unbound variables.
     * @return <code>true</code> if the expression is a constant expression,
     * <code>false</code> if the expression contains variables.
     */
    public boolean isConstant(){
	return getLeft().isConstant() && getRight().isConstant();
    }
}
