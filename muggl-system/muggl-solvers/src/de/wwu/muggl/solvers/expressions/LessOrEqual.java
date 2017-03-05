package de.wwu.muggl.solvers.expressions;

import java.util.HashSet;
import java.util.Iterator;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.constraints.WeakInequation;
import de.wwu.muggl.solvers.solver.tools.Substitution;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Represents the <code>&lt;=</code> operation on numeric expressions (terms).
 * @author Christoph Lembeck
 */
public class LessOrEqual extends ConstraintExpression implements HasLeftAndRightTerms{

    /**
     * Creates a new LessOrEqual object representing the <code>&lt;=</code>
     * operation on numeric expressions.
     * @param left the left hand side of the inequality.
     * @param right the right hand side of the inequality.
     * @return the new less or equal expression.
     */
    public static ConstraintExpression newInstance(Term left, Term right){
	if (left instanceof NumericConstant && right instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)left).isLesserOrEqual((NumericConstant)right));
	else
	    return new LessOrEqual(left, right);
    }

    /**
     * The left hand side of the inequation.
     */
    protected Term left;

    /**
     * The right hand side of the inequation.
     */
    protected Term right;

    /**
     * Creates a new LessOrEqual object representing the <code>&lt;=</code>
     * operation on numeric expressions.
     * @param left the left hand side of the inequality.
     * @param right the right hand side of the inequality.
     * @see #newInstance(Term, Term)
     */
    private LessOrEqual(Term left, Term right) {
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
	    throw new TypeCheckException(left.toString() + " and " + right.toString() + " have different types");
    }

    @Override
    public ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable) {
	// see LessThan.convertToComposedConstraint() for detailed comments

	ConstraintExpression substitutedExpression = substituteWithSideConditions(subTable);
	if (substitutedExpression != this){
	    return substitutedExpression.convertToComposedConstraint(subTable);
	}

	// remove type casts
	TypeCast cast = getFirstNarrowingTypeCast();
	if (cast != null){
	    NumericVariable var = NumericVariable.createInternalVariable(cast.getType());
	    NumericConstant one = NumericConstant.getOne(cast.getFromType());
	    NumericConstant zero = NumericConstant.getZero(cast.getFromType());
	    Term x = cast.getInternalTerm();
	    
	    Term a = TypeCast.newInstance(var, cast.getType(), cast.getFromType());
	    
	    ConstraintExpression nb1 = 
		And.newInstance(
			GreaterOrEqual.newInstance(x, zero),
			And.newInstance(
				LessThan.newInstance( Difference.newInstance(x, one), a ), 
				LessOrEqual.newInstance(a, x)
			)
		);
	    ConstraintExpression nb2 = 
		And.newInstance(
			LessThan.newInstance(x, zero), 
			And.newInstance(
				GreaterThan.newInstance(Sum.newInstance(x, one), a), 
				GreaterOrEqual.newInstance(a, x)
			)
		);
	    ConstraintExpression nb = Or.newInstance(nb1, nb2);
	    subTable.addSubstitution(cast, var, nb);
	    return And.newInstance(nb, LessOrEqual.newInstance(left.substitute(cast, var), right.substitute(cast, var))).convertToComposedConstraint(subTable);
	}

	// remove modulos
	Modulo modulo = getInmostModulo();
	if (modulo != null){
	    if (Term.isIntegerType(modulo.getType()))
		return removeIntegerModulo(modulo, subTable).convertToComposedConstraint(subTable);
	    else
		return removeModulo(modulo, subTable).convertToComposedConstraint(subTable);
	}

	// remove fractions
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
	    return And.newInstance(LessOrEqual.newInstance(newLeft, newRight), nb).convertToComposedConstraint(subTable);
	}

	Quotient quotient = getInmostQuotient();
	if (quotient != null){
	    if (Term.isIntegerType(quotient.getType()))
		return removeIntegerDenominator(quotient, subTable).convertToComposedConstraint(subTable);
	    else
		return removeDenominator(quotient).convertToComposedConstraint(subTable);
	}

	// l <= r  <=> l - r <= 0
	return WeakInequation.newInstance(left.toPolynomial().
		subtractPolynomial(
		right.toPolynomial()));
    }

    @Override
    public boolean equals(Object other){
	if (other == this)
	    return true;
	if (other instanceof LessOrEqual){
	    LessOrEqual otherLeq = (LessOrEqual)other;
	    return left.equals(otherLeq.left) && right.equals(otherLeq.right);
	}
	return false;
    }

    /**
     * Returns the first Modulo expression that can be found in the inequation.
     * @return the first Modulo expression that can be found in one of the
     * inequations arguments.
     * @see Term#getFirstModulo()
     */
    @SuppressWarnings("unused")
    private Modulo getFirstModulo(){
	Modulo m = left.getFirstModulo();
	if (m != null)
	    return m;
	return right.getFirstModulo();
    }

    /**
     * Searches for a narrowing typecast in both arguments of the inequation.
     * @return the first narrowing typecast that was found in one of the
     * inequations arguments.
     * @see Term#getFirstNarrowingTypeCast()
     */
    private TypeCast getFirstNarrowingTypeCast(){
	TypeCast tc = left.getFirstTypeCast(true);
	if (tc != null)
	    return tc;
	return right.getFirstTypeCast(true);
    }

    /**
     * Searches for the first occurence of any quotient and returns it.
     * @return the first quotient that can be found in one of the arguments of the
     * inequation.
     */
    @SuppressWarnings("unused")
    private Quotient getFirstQuotient(){
	Quotient q = left.getFirstQuotient();
	if (q != null)
	    return q;
	return right.getFirstQuotient();
    }

    /**
     * Returns the deepest modulo expression.
     * @return null if there is no such modulo expression.
     */
    private Modulo getInmostModulo(){
	Modulo m = left.getInmostModulo();
	if (m != null)
	    return m;
	return right.getInmostModulo();
    }

    /**
     * Returns the deepest quotient expression.
     * @return null if there is no such quotient expression.
     */
    private Quotient getInmostQuotient(){
	Quotient q = left.getInmostQuotient();
	if (q != null)
	    return q;
	return right.getInmostQuotient();
    }

    /**
     * Returns <i>Expression.Boolean</i> as type of this inequation.
     * @return <i>Expression.Boolean</i>.
     * @see de.wwu.muggl.solvers.expressions.Expression#BOOLEAN
     */
    @Override
    public byte getType(){
	return Expression.BOOLEAN;
    }

    @Override
    public int hashCode(){
	return left.hashCode() - right.hashCode() + 9;
    }

    @Override
    public ConstraintExpression insertAssignment(Assignment assignment){
	Term leftNew = left.insertAssignment(assignment);
	Term rightNew = right.insertAssignment(assignment);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)leftNew).isLesserOrEqual((NumericConstant)rightNew));
	else
	    return newInstance(leftNew, rightNew);
    }

    @Override
    public ConstraintExpression insert(Solution solution, boolean produceNumericSolution){
	Term leftNew = left.insert(solution, produceNumericSolution);
	Term rightNew = right.insert(solution, produceNumericSolution);
	if (leftNew instanceof NumericConstant && rightNew instanceof NumericConstant)
	    return BooleanConstant.getInstance(((NumericConstant)leftNew).isLesserOrEqual((NumericConstant)rightNew));
	else
	    return newInstance(leftNew, rightNew);
    }
    
    @Override
    public boolean isConstant(){
	return left.isConstant() && right.isConstant();
    }

    @Override
    public ConstraintExpression negate(){
	return LessThan.newInstance(right, left);
    }

    /**
     * Returns an arithmetic equivalent constraint to this inequation in which the
     * passed quotient will be substituted.
     * @param quotient the quotient that should be substituted by an equaivalent
     * operation.
     * @return the arithmetic equivalent constraint without the passed quotient.
     */
    protected ConstraintExpression removeDenominator(Quotient quotient){
	Term t = quotient.getDenominator();
	NumericConstant zero = NumericConstant.getZero(t.getType());
	return Or.newInstance(And.newInstance(LessOrEqual.newInstance(left.multiply(t), right.multiply(t)), LessThan.newInstance(zero, t)),
		And.newInstance(LessOrEqual.newInstance(right.multiply(t), left.multiply(t)), LessThan.newInstance(t, zero)));
    }

    /**
     * Returns an arithmetic equivalent constraint to this inequation in which the
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
	return And.newInstance(LessOrEqual.newInstance(left.substitute(quotient, c), right.substitute(quotient,c)), nb);
    }

    /**
     * Returns an arithmetic equivalent constraint to this inequation in which the
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
	return new LessOrEqual(left.substitute(mod, t), right.substitute(mod, t));
    }

    /**
     * Returns an arithmetic equivalent constraint to this inequation in which the
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
	return And.newInstance(LessOrEqual.newInstance(left.substitute(mod, t), right.substitute(mod, t)), nb);
    }

    private ConstraintExpression substituteWithSideConditions(SubstitutionTable subTable){
	Substitution substitution = left.findSubstitution(subTable);
	if (substitution != null){
	    ConstraintExpression sideCondition = substitution.getSideCondition();
	    if (sideCondition == null || sideCondition.equals(BooleanConstant.TRUE)){
		return LessOrEqual.newInstance(left.substitute((Term)substitution.getSource(), (Term)substitution.getDestination()), right);
	    } else {
		return And.newInstance(LessOrEqual.newInstance(left.substitute((Term)substitution.getSource(), (Term)substitution.getDestination()), right), substitution.getSideCondition());
	    }
	}
	substitution = right.findSubstitution(subTable);
	if (substitution != null){
	    ConstraintExpression sideCondition = substitution.getSideCondition();
	    if (sideCondition == null || sideCondition.equals(BooleanConstant.TRUE)){
		return LessOrEqual.newInstance(left, right.substitute((Term)substitution.getSource(), (Term)substitution.getDestination()));
	    } else {
		return And.newInstance(LessOrEqual.newInstance(left, right.substitute((Term)substitution.getSource(), (Term)substitution.getDestination())), substitution.getSideCondition());
	    }
	}
	return this;
    }

    @Override
    public String toHaskellString(){
	return "(LessOrEqual " + left.toHaskellString() + " " + right.toHaskellString() + ")";
    }

    @Override
    public String toString(boolean useInternalVariableNames){
	return "(" + left.toString(useInternalVariableNames) + "<=" + right.toString(useInternalVariableNames) + ")";
    }

    @Override
    public String toTexString(boolean useInternalVariableNames) {
	return "(" + left.toTexString(useInternalVariableNames) + " \\leq " + right.toTexString(useInternalVariableNames) + ")";
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
