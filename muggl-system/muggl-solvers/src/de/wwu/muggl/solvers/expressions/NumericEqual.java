package de.wwu.muggl.solvers.expressions;

import java.util.HashSet;
import java.util.Iterator;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.Equation;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>==</code> operation on numeric expressions (terms).
 * @author Christoph Lembeck
 */
public class NumericEqual extends ConstraintExpression implements HasLeftAndRightTerms{

    /**
     * Creates a new NumericEqual object representing the operation
     * <code>==</code> on numeric expressions (terms).
     * @param left the first argument of the <code>==</code> operation.
     * @param right the second argument of the <code>==</code> operation.
     * @return the new NumericEqual expression.
     */
    public static ConstraintExpression newInstance(Term left, Term right){
	if (left instanceof NumericConstant && right instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)left).isEqualTo((NumericConstant)right));
	else
	    return new NumericEqual(left, right);
    }

    /**
     * The first argument of the <code>==</code> operation.
     */
    protected Term left;

    /**
     * The second argument of the <code>==</code> operation.
     */
    protected Term right;

    /**
     * Creates a new NumericEqual object representing the operation
     * <code>==</code> on numeric expressions (terms).
     * @param left the first argument of the <code>==</code> operation.
     * @param right the second argument of the <code>==</code> operation.
     * @see #newInstance(Term, Term)
     */
    protected NumericEqual(Term left, Term right) {
	this.left = left;
	this.right = right;
    }

    @Override
    public void checkTypes() throws TypeCheckException{
	left.checkTypes();
	right.checkTypes();
	if (!Term.isNumericType(left.getType()))
	    throw new TypeCheckException(left.toString() + " is not of a numeric type");
	if (!Term.isNumericType(right.getType()))
	    throw new TypeCheckException(right.toString() + " is not of a numeric type");
	if (!Term.compatibleTypes(left.getType(), right.getType()))
	    throw new TypeCheckException(left.toString() + " and " + right.toString() + " have different types (" + Term.getTypeName(left.getType())+", " + Term.getTypeName(right.getType())+")");
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable) {
	try{
	    checkTypes();
	    // CLTODO typecheck entfernen
	} catch (Exception e){
	    e.printStackTrace();
	}

	ConstraintExpression substitutedExpression = substituteWithSideConditions(subTable);
	if (substitutedExpression != this)
	    return substitutedExpression.convertToComposedConstraint(subTable);

	// remove type casts
	TypeCast cast = getFirstNarrowingTypeCast();
	if (cast != null){
	    NumericVariable var = NumericVariable.createInternalVariable(cast.getType());
	    NumericConstant one = NumericConstant.getOne(cast.getFromType());
	    NumericConstant zero = NumericConstant.getZero(cast.getFromType());
	    Term x = cast.getInternalTerm();
	    Term a = TypeCast.newInstance(var, cast.getType(), cast.getFromType());
	    ConstraintExpression nb1 = And.newInstance(GreaterOrEqual.newInstance(x, zero), And.newInstance(LessThan.newInstance(Difference.newInstance(x, one), a), LessOrEqual.newInstance(a, x)));
	    ConstraintExpression nb2 = And.newInstance(LessThan.newInstance(x, zero), And.newInstance(GreaterThan.newInstance(Sum.newInstance(x, one), a), GreaterOrEqual.newInstance(a, x)));
	    ConstraintExpression nb = Or.newInstance(nb1, nb2);
	    subTable.addSubstitution(cast, var, nb);
	    return And.newInstance(NumericEqual.newInstance(left.substitute(cast, var), right.substitute(cast, var)), nb).convertToComposedConstraint(subTable);
	}

	// remove modulo
	Modulo modulo = getInmostModulo();
	if (modulo != null){
	    if (Term.isIntegerType(modulo.getType())){
		ComposedConstraint result = removeIntegerModulo(modulo, subTable).convertToComposedConstraint(subTable);
		return result;
	    }else
		return removeModulo(modulo, subTable).convertToComposedConstraint(subTable);
	}

	// remove quotients
	HashSet<Term> denominators = new HashSet<Term>();
	Term newLeft = left.clearMultiFractions(denominators);
	Term newRight = right.clearMultiFractions(denominators);
	if (left != newLeft || right != newRight){
	    Iterator<Term> denomIt = denominators.iterator();
	    Term term = denomIt.next();
	    ConstraintExpression nb = NumericNotEqual.newInstance(term, NumericConstant.getZero(term.getType()));
	    while (denomIt.hasNext()){
		term = denomIt.next();
		nb = And.newInstance(nb, NumericNotEqual.newInstance(term, NumericConstant.getZero(term.getType())));
	    }
	    return And.newInstance(NumericEqual.newInstance(newLeft, newRight), nb).convertToComposedConstraint(subTable);
	}

	Quotient quotient = getInmostQuotient();
	if (quotient != null){
	    if (Term.isIntegerType(quotient.getType()))
		return removeIntegerDenominator(quotient, subTable).convertToComposedConstraint(subTable);
	    else
		return removeDenominator(quotient).convertToComposedConstraint(subTable);
	}

	return Equation.newInstance(left.toPolynomial().subtractPolynomial(right.toPolynomial()));
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof NumericEqual){
	    NumericEqual otherEqual = (NumericEqual)other;
	    return (left.equals(otherEqual.left) && right.equals(otherEqual.right)) || (left.equals(otherEqual.right) && right.equals(otherEqual.left));
	}
	return false;
    }

    public Modulo getFirstModulo(){
	Modulo m = left.getFirstModulo();
	if (m != null)
	    return m;
	return right.getFirstModulo();
    }

    /**
     * Searches for a narrowing typecast in both arguments of the inequation.
     * @return the first narrowing typecast that was found in one of the
     * equations arguments.
     */
    public TypeCast getFirstNarrowingTypeCast(){
	TypeCast tc = left.getFirstTypeCast(true);
	if (tc != null)
	    return tc;
	return right.getFirstTypeCast(true);
    }

    /**
     * Searches for the first occurence of any quotient and returns it.
     * @return the first quotient that can be found in one of the arguments of the
     * equation.
     */
    public Quotient getFirstQuotient(){
	Quotient q = left.getFirstQuotient();
	if (q != null)
	    return q;
	return right.getFirstQuotient();
    }

    public Modulo getInmostModulo(){
	Modulo m = left.getInmostModulo();
	if (m != null)
	    return m;
	return right.getInmostModulo();
    }

    public Quotient getInmostQuotient(){
	Quotient q = left.getInmostQuotient();
	if (q != null)
	    return q;
	return right.getInmostQuotient();
    }

    /**
     * Returns <i>Expression.Boolean</i> as type of this equation.
     * @return <i>Expression.Boolean</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    @Override
    public byte getType(){
	return Expression.BOOLEAN;
    }

    @Override
    public int hashCode(){
	return left.hashCode() - right.hashCode();
    }

    @Override
    public ConstraintExpression insertAssignment(Assignment assignment){
	Term leftNew = left.insertAssignment(assignment);
	Term rightNew = right.insertAssignment(assignment);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)leftNew).isEqualTo((NumericConstant)rightNew));
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	Term leftNew = left.insert(solution, produceNumericSolution);
	Term rightNew = right.insert(solution, produceNumericSolution);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)leftNew).isEqualTo((NumericConstant)rightNew));
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    public boolean isConstant(){
	return left.isConstant() && right.isConstant();
    }

    /**
     * Returns an arithmetic equivalent constraint to this equation in which the
     * passed quotient will be substituted.
     * @param quotient the quotient that should be substituted by an equaivalent
     * operation.
     * @return the arithmetic equivalent constraint without the passed quotient.
     */
    protected ConstraintExpression removeDenominator(Quotient quotient){
	Term t = quotient.getDenominator();
	NumericConstant zero = NumericConstant.getZero(t.getType());
	return And.newInstance(new NumericEqual(left.multiply(t), right.multiply(t)), Or.newInstance(LessThan.newInstance(t, zero), LessThan.newInstance(zero, t)));
    }

    /**
     * Returns an arithmetic equivalent constraint to this equation in which the
     * passed quotient will be substituted considering the fact that the quotient
     * represents an integer division.
     * @param quotient the quotient that should be substituted by an equaivalent
     * operation.
     * @return the arithmetic equivalent constraint without the passed quotient.
     */
    protected ConstraintExpression removeIntegerDenominator(Quotient quotient, SubstitutionTable subTable){
	// ersetze Quotienten q = a/b durch die k�nstliche Variable c.
	// Danach gilt:
	//   falls a >= 0, b > 0: bc <= a < bc+b
	//   falls a >= 0, b < 0: bc <= a < bc-b
	//   falls a <  0, b > 0: bc >= a > bc-b
	//   falls a <  0, b < 0: bc >= a > bc+b
	// wobei die Bedingungen b>0 bzw. b<0 reduntante Informationen darstellen.
	Term a = quotient.getNumerator();
	Term b = quotient.getDenominator();
	NumericVariable c = NumericVariable.createInternalVariable(quotient.getType());
	Term bc = b.multiply(c);
	Term bcPb = Sum.newInstance(bc, b);
	Term bcMb = Difference.newInstance(bc, b);
	NumericConstant zero = NumericConstant.getZero(a.getType());
	ConstraintExpression nb1 = And.newInstance(GreaterOrEqual.newInstance(a, zero), And.newInstance(LessOrEqual.newInstance(bc, a),    Or.newInstance(LessThan.newInstance(a, bcPb),    LessThan.newInstance(a,bcMb))));
	ConstraintExpression nb2 = And.newInstance(LessThan.newInstance(a, zero),       And.newInstance(GreaterOrEqual.newInstance(bc, a), Or.newInstance(GreaterThan.newInstance(a, bcMb), GreaterThan.newInstance(a, bcPb))));
	ConstraintExpression nb = Or.newInstance(nb1, nb2);
	subTable.addSubstitution(quotient, c, nb);
	return And.newInstance(new NumericEqual(left.substitute(quotient, c), right.substitute(quotient,c)), nb);
    }

    /**
     * Returns an arithmetic equivalent constraint to this equation in which the
     * passed modulo expression will be substituted  considering the fact that the
     * modulo expression represents the modulo operation on integers.
     * @param mod the modulo expression that should be substituted by an
     * equaivalent operation.
     * @return the arithmetic equivalent constraint without the passed modulo
     * expression.
     */
    protected ConstraintExpression removeIntegerModulo(Modulo mod, SubstitutionTable subTable){
	// ersetze a%b durch a-(a/b)*b
	Term a = mod.getLeft();
	Term b = mod.getRight();
	Term t = Difference.newInstance(a, Product.newInstance(Quotient.newInstance(a,b), b));
	subTable.addSubstitution(mod, t, null);
	return NumericEqual.newInstance(left.substitute(mod, t), right.substitute(mod, t));
    }

    /**
     * Returns an arithmetic equivalent constraint to this equation in which the
     * passed modulo expression will be substituted.
     * @param mod the modulo expression that should be substituted by an
     * equaivalent operation.
     * @return the arithmetic equivalent constraint without the passed modulo
     * expression.
     */
    protected ConstraintExpression removeModulo(Modulo mod, SubstitutionTable subTable){
	// ersetze Modulo a%b durch den Term a-b*l2d(q) mit der neuen variablen q
	// wobei folgende Nebenbedingungen erf�llt sein m�ssen:
	//   falls a >= 0, b > 0: a-b < b*l2d(q) <= a
	//   falls a >= 0, b < 0: a+b < b*l2d(q) <= a
	//   falls a <  0, b > 0: a+b > b*l2d(q) >= a
	//   falls a <  0, b < 0: a-b > b*l2d(q) >= a
	// wobei die Bedingungen b>0 bzw. b<0 reduntante Informationen darstellen.
	Term a = mod.getLeft();
	Term b = mod.getRight();
	Term q = TypeCast.newInstance(NumericVariable.createInternalVariable(Expression.LONG), Expression.LONG, b.getType());
	Term t = Difference.newInstance(a, Product.newInstance(b, q));

	Term bq = b.multiply(q);
	Term aMb = Difference.newInstance(a,b);
	Term aPb = Sum.newInstance(a,b);
	NumericConstant zero = NumericConstant.getZero(a.getType());
	ConstraintExpression nb1 = And.newInstance(GreaterOrEqual.newInstance(a, zero), And.newInstance(LessOrEqual.newInstance(bq, a),    Or.newInstance(LessThan.newInstance(aMb, bq),    LessThan.newInstance(aPb, bq))));
	ConstraintExpression nb2 = And.newInstance(LessThan.newInstance(a, zero),       And.newInstance(GreaterOrEqual.newInstance(bq, a), Or.newInstance(GreaterThan.newInstance(aPb, bq), GreaterThan.newInstance(aMb, bq))));
	ConstraintExpression nb = Or.newInstance(nb1, nb2);
	subTable.addSubstitution(mod, t, nb);
	return And.newInstance(NumericEqual.newInstance(left.substitute(mod, t), right.substitute(mod, t)), nb);
    }

    private ConstraintExpression substituteWithSideConditions(SubstitutionTable subTable){
	Substitution substitution = left.findSubstitution(subTable);
	if (substitution != null){
	    ConstraintExpression sideCondition = substitution.getSideCondition();
	    if (sideCondition == null || sideCondition.equals(BooleanConstant.TRUE)){
		return NumericEqual.newInstance(left.substitute((Term)substitution.getSource(), (Term)substitution.getDestination()), right);
	    } else {
		return And.newInstance(NumericEqual.newInstance(left.substitute((Term)substitution.getSource(), (Term)substitution.getDestination()), right), substitution.getSideCondition());
	    }
	}
	substitution = right.findSubstitution(subTable);
	if (substitution != null){
	    ConstraintExpression sideCondition = substitution.getSideCondition();
	    if (sideCondition == null || sideCondition.equals(BooleanConstant.TRUE)){
		return NumericEqual.newInstance(left, right.substitute((Term)substitution.getSource(), (Term)substitution.getDestination()));
	    } else {
		return And.newInstance(NumericEqual.newInstance(left, right.substitute((Term)substitution.getSource(), (Term)substitution.getDestination())), substitution.getSideCondition());
	    }
	}
	return this;
    }

    @Override
    public String toHaskellString(){
	return "(NumericEqual " + left.toHaskellString() + " " + right.toHaskellString() + ")";
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + left.toString(useInternalVariableNames) + "==" + right.toString(useInternalVariableNames) + ")";
    }

    @Override
    public ConstraintExpression negate(){
	return NumericNotEqual.newInstance(left, right);
    }
    
    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + left.toTexString(useInternalVariableNames) + " = " + right.toTexString(useInternalVariableNames) + ")";
    }

    @Override
	public Term getLeft() {
		return left;
	}

    @Override
	public Term getRight() {
		return right;
	}
}
