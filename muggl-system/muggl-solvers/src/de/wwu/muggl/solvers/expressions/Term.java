package de.wwu.muggl.solvers.expressions;

import java.util.Set;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * The top level class for all numeric expressions.
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public abstract class Term implements Expression {

    /**
     * Checks whether the two passed types may be combined by any operation
     * without an explicit type cast.
     * @param typeA the first type for that the check should be performed.
     * @param typeB the second type for that the check should be performed.
     * @return <i>true</i> if the two types may be combined without a type cast,
     * <i>false</i> if an explicit type cast is necessary.
     */
    public static boolean compatibleTypes(byte typeA, byte typeB) {
	return (typeA == typeB) || (isIntegerType(typeA) && isIntegerType(typeB));
    }

    /**
     * Returns the string representation of the passed type.
     * @param type the type for that the string representation should be returned.
     * @return the string representation of the passed type.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    public static String getTypeName(byte type) {
	switch (type) {
	case Expression.BYTE :
	    return "byte";
	case Expression.CHAR :
	    return "char";
	case Expression.SHORT :
	    return "short";
	case Expression.INT :
	    return "int";
	case Expression.LONG :
	    return "long";
	case Expression.FLOAT :
	    return "float";
	case Expression.DOUBLE :
	    return "double";
	case Expression.BOOLEAN :
	    return "boolean";
	default :
	    return "unknown";
	}
    }

    /**
     * Checks if the passed type constant represents an integer type or not.
     * @param type the type for that the test should be done.
     * @return <i>true</i> if the passed type represents an integer, <i>false</i>
     * otherwise.
     */
    public static boolean isIntegerType(byte type) {
	return ((type == Expression.BYTE) || (type == Expression.CHAR) || (type == Expression.SHORT) || (type == Expression.INT) || (type == Expression.LONG));
    }

    /**
     * Checks if the passed type constant represents a numeric type or not.
     * @param type the type for that the test should be done.
     * @return <i>true</i> if the passed type is numeric, <i>false</i> otherwise.
     */
    public static boolean isNumericType(byte type) {
	return ((type == Expression.BYTE) || (type == Expression.CHAR) || (type == Expression.SHORT) || (type == Expression.INT) || (type == Expression.LONG) || (type == Expression.FLOAT) || (type == Expression.DOUBLE));
    }

    /**
     * Removes multi-fractions like a/(b/c) with b or c being non-integer types by
     * extending the term with c. I.e. multiplying with the questioned denominator
     * a*c/b. This is done recursively from innermost to outermost.
     * 
     * Proper Fractions are allowed and not cleared.
     * <BR>
     * Interesting stuff is done in @{link de.wwu.testtool.expressions.Quotient.#clearMultiFractions()}.
     * 
     * @param denominators All the denominators used to multiply some terms are gathered in this set.
     * They are used in {@link de.wwu.muggl.solvers.expressions.LessThan#removeIntegerDenominator(Quotient, SubstitutionTable)}
     * for example to satisfy that they don't become zero by adding constraints accordingly.
     * @return a Term cleared of real type multi-fractions.
     */
    public abstract Term clearMultiFractions(Set<Term> denominators);

    /**
     * TODOME doc!
     * @param assignment
     * @return
     */
    public abstract Term insertAssignment(Assignment assignment);

    /**
     * 
     * Substitutes the contained variables of the term by the values defined
     * in the passed solution object. Remaining variables may be substituted by
     * zeros using the produceNumericSolution parameter.
     * @param solution the bindings that should be used for the substitutions.
     * @param produceNumericSolution <code>true</code> if the missing variables
     * should be replaced by zeros, <code>false</code> if the variables that are
     * not specified in the solution object should remain in the resulting expression.
     * @return the new Term that does not contain the variables specified in
     * the solution object any more.
     */
    @Override
    public abstract Term insert(Solution solution, boolean produceNumericSolution);
    
    /**
     * Returns <i>false</i>, because terms are always numeric and hence not
     * boolean.
     * @return <i>false</i>.
     */
    public boolean isBoolean() {
	return false;
    }

    /**
     * Searches for all appearances of the first passed argument in this term and
     * replaces them by the second argument.
     * @param a the term that should be replaced.
     * @param b the new term that should replace the other argument.
     * @return a copy of this term having the second passed term on all places the
     * first passed argument was before.
     */
    public abstract Term substitute(Term a, Term b);


    /**
     * Converts this term into an easier manageable polynomial object.
     * @return the polynomial representing the operations done in this term.
     */
    public abstract Polynomial toPolynomial();

    /**
     * Returns a string representation of this term.
     * @return a string representation of this term.
     */
    @Override
    public String toString(){
	return toString(false);
    }

    /**
     * Tests if the passed term appears somewhere in this term as a denominator.
     * @param t the term that should be searched for.
     * @return <i>true</i> if the term appears in this term as denominator,
     * <i>false</i> otherwise.
     */
    protected abstract boolean containsAsDenominator(Term t);

    /**
     * TODOME doc!
     * @param subTable
     * @return
     */
    protected abstract Substitution findSubstitution(SubstitutionTable subTable);

    /**
     * Collects all contained denominators of this term and returns them as a set.
     * @return the set of all contained denominators out of this term.
     */
    protected abstract Set<Term> getDenominators();

    /**
     * Returns the first modulo expression that can be found in this term.
     * @return the first modulo expression that can be found in this term.
     */
    protected abstract Modulo getFirstModulo();

    /**
     * TODOME doc!
     * @return
     */
    protected abstract Quotient getFirstNonintegerQuotient() ;

    /**
     * Returns the first quotient that can be found in this term.
     * @return the first quotient that can be found in this term.
     */
    protected abstract Quotient getFirstQuotient();

    /**
     * Returns the first type cast that can be found in this term.
     * @param onlyNarrowing if set to <i>true</i> the method only returns
     * narrowing type casts, otherwise narrowing and widening type casts.
     * @return the first type cast that can be found in this term.
     */
    protected abstract TypeCast getFirstTypeCast(boolean onlyNarrowing);

    /**
     * Self-explaining.
     * @return
     */
    protected abstract Modulo getInmostModulo();
    
    /**
     * Self-explaining.
     * @return
     */
    protected abstract Quotient getInmostQuotient();

    /**
     * Multiplies this term with the passed argument and returns the product.
     * @param factor the factor this term should be multiplied with.
     * @return the product of the multiplication.
     */
    protected abstract Term multiply(Term factor);
}
