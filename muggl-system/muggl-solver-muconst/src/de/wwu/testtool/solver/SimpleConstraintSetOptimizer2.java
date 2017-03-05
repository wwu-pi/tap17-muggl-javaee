package de.wwu.testtool.solver;

import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSetTransformer;
import de.wwu.muggl.solvers.solver.constraints.Equation;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.constraints.StrictInequation;
import de.wwu.muggl.solvers.solver.constraints.WeakInequation;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;

/**
 * Macht keine Weltbewegenden Sachen. Wahrscheinlich nur unnötiger Aufwand, da hohe
 * Laufzeit.
 * @author Christoph Lembeck
 */
public class SimpleConstraintSetOptimizer2 implements ConstraintSetTransformer{

    public SingleConstraintSet transform(SingleConstraintSet constraintSet){
	int count = constraintSet.getConstraintCount();
	SingleConstraint[] constraints = new SingleConstraint[count];
	for (int i = 0; i < count; i++)
	    constraints[i] = constraintSet.getConstraint(i);
	boolean[][] checked = new boolean[count][count];
	for (int y = 0; y < count; y++)
	    for (int x = 0; x < count; x ++)
		checked[y][x] = x <= y;

	boolean madeSimplification;
	do {
	    madeSimplification = false;
	    SingleConstraint[] pair = new SingleConstraint[2];
	    for (int y = 0; y < count - 1; y++){
		pair[0] = constraints[y];
		for (int x = y + 1; x < count; x++){
		    pair[1] = constraints[x];
		    if (!checked[y][x]){
			combineConstraints(pair);
			if (constraints[y] != pair[0]){
			    if (pair[0].equals(BooleanConstant.FALSE))
				return SingleConstraintSet.FALSESET;
			    constraints[y] = pair[0];
			    invalidate(checked, y);
			    madeSimplification = true;
			}
			if (constraints[x] != pair[1]){
			    if (pair[1].equals(BooleanConstant.FALSE))
				return SingleConstraintSet.FALSESET;
			    constraints[x] = pair[1];
			    invalidate(checked, x);
			}
			checked[y][x] = true;
		    }
		}
	    }
	} while (madeSimplification);
	SingleConstraintSet result = new SingleConstraintSet();
	for (SingleConstraint constraint: constraints)
	    result.add(constraint);
	return result;
    }

    protected void combineConstraints(SingleConstraint[] pair){
	SingleConstraint c0 = pair[0];
	SingleConstraint c1 = pair[1];
	if (c0.equals(BooleanConstant.TRUE) || c1.equals(BooleanConstant.TRUE))
	    return;
	if (c0.equals(BooleanConstant.FALSE)){
	    pair[1] = BooleanConstant.FALSE;
	    return;
	}
	if (c1.equals(BooleanConstant.FALSE)){
	    pair[0] = BooleanConstant.FALSE;
	    return;
	}
	if (c0 instanceof Assignment){
	    Assignment assignment = (Assignment)c0;
	    Variable var = assignment.getVariable();
	    if (c1.containsVariable(var))
		pair[1] = c1.insert(assignment);
	    return;
	}
	if (c1 instanceof Assignment){
	    Assignment assignment = (Assignment)c1;
	    Variable var = assignment.getVariable();
	    if (c0.containsVariable(var))
		pair[0] = c0.insert(assignment);
	    return;
	}

	if (c0 instanceof Equation){
	    if (c1 instanceof Equation)
		combineEqEq(pair);
	    else
		if (c1 instanceof WeakInequation)
		    combineEqLeq(pair);
		else
		    if (c1 instanceof StrictInequation)
			combineEqLt(pair);
	} else
	    if (c0 instanceof WeakInequation){
		if (c1 instanceof Equation)
		    combineLeqEq(pair);
		else
		    if (c1 instanceof WeakInequation)
			combineLeqLeq(pair);
		    else
			if (c1 instanceof StrictInequation)
			    combineLeqLt(pair);
	    } else
		if (c0 instanceof StrictInequation){
		    if (c1 instanceof Equation)
			combineLtEq(pair);
		    else
			if (c1 instanceof WeakInequation)
			    combineLtLeq(pair);
			else
			    if (c1 instanceof StrictInequation)
				combineLtLt(pair);
		}
    }

    protected void combineEqEq(SingleConstraint[] pair){
	Equation c0 = (Equation)pair[0];
	Equation c1 = (Equation)pair[1];
	Polynomial p0 = c0.getPolynomial();
	Polynomial p1 = c1.getPolynomial();
	if (p0.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (constEqual(nc0, nc1)){
		pair[1] = BooleanConstant.TRUE;
		return;
	    } else {
		pair[0] = BooleanConstant.FALSE;
		pair[1] = BooleanConstant.FALSE;
		return;
	    }
	}
	Polynomial p1neg = p1.negate();
	if (p0.equalsIgnoreConstant(p1neg)){
	    NumberWrapper nc0 = p0.getConstant();
	    NumberWrapper nc1 = p1neg.getConstant();
	    if (constEqual(nc0, nc1)){
		pair[1] = BooleanConstant.TRUE;
		return;
	    } else {
		pair[0] = BooleanConstant.FALSE;
		pair[1] = BooleanConstant.FALSE;
		return;
	    }
	}
    }

    private boolean constEqual(NumberWrapper nc1, NumberWrapper nc2){
	if (nc1 == null){
	    return nc2 == null || nc2.isZero();
	} else {
	    if (nc1.isZero()){
		return nc2 == null || nc2.isZero();
	    } else{
		return nc1.equals(nc2);
	    }
	}
    }

    private boolean constLt(NumberWrapper nc1, NumberWrapper nc2){
	if (nc1 == null)
	    if (nc2 == null)
		return false;
	    else
		return nc2.isGreaterThanZero();
	else
	    if (nc2 == null)
		return nc1.isLessThanZero();
	    else
		return nc1.lessThan(nc2);
    }

    protected void combineEqLeq(SingleConstraint[] pair){
	Equation c0 = (Equation)pair[0];
	WeakInequation c1 = (WeakInequation)pair[1];
	Polynomial p0 = c0.getPolynomial();
	Polynomial p1 = c1.getPolynomial();
	if (p0.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (constEqual(nc0, nc1)){
		pair[1] = BooleanConstant.TRUE;
		return;
	    } else {
		if (constLt(nc0, nc1)){
		    pair[0] = BooleanConstant.FALSE;
		    pair[1] = BooleanConstant.FALSE;
		    return;
		} else {
		    pair[1] = BooleanConstant.TRUE;
		    return;
		}
	    }
	}
	Polynomial p0neg = p0.negate();
	if (p0neg.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0neg.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (constEqual(nc0, nc1)){
		pair[1] = BooleanConstant.TRUE;
		return;
	    } else {
		if (constLt(nc0, nc1)){
		    pair[0] = BooleanConstant.FALSE;
		    pair[1] = BooleanConstant.FALSE;
		    return;
		} else {
		    pair[1] = BooleanConstant.TRUE;
		    return;
		}
	    }
	}

    }

    protected void combineEqLt(SingleConstraint[] pair){
	Equation c0 = (Equation)pair[0];
	StrictInequation c1 = (StrictInequation)pair[1];
	Polynomial p0 = c0.getPolynomial();
	Polynomial p1 = c1.getPolynomial();
	if (p0.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (!constLt(nc1, nc0)){
		pair[0] = BooleanConstant.FALSE;
		pair[1] = BooleanConstant.FALSE;
	    } else
		pair[1] = BooleanConstant.TRUE;
	    return;
	}
	Polynomial p0neg = p0.negate();
	if (p0neg.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0neg.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (!constLt(nc1, nc0)){
		pair[0] = BooleanConstant.FALSE;
		pair[1] = BooleanConstant.FALSE;
	    } else
		pair[1] = BooleanConstant.TRUE;
	    return;
	}
    }

    protected void combineLeqEq(SingleConstraint[] pair){
	swap(pair);
	combineEqLeq(pair);
	swap(pair);
    }

    protected void combineLeqLeq(SingleConstraint[] pair){
	WeakInequation c0 = (WeakInequation)pair[0];
	WeakInequation c1 = (WeakInequation)pair[1];
	Polynomial p0 = c0.getPolynomial();
	Polynomial p1 = c1.getPolynomial();
	if (p0.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (constLt(nc0, nc1))
		pair[0] = BooleanConstant.TRUE;
	    else
		pair[1] = BooleanConstant.TRUE;
	    return;
	}
	Polynomial p1neg = p1.negate();
	if (p0.equalsIgnoreConstant(p1neg)){
	    NumberWrapper nc0 = p0.getConstant();
	    NumberWrapper nc1 = p1neg.getConstant();
	    if (constEqual(nc0, nc1)){
		pair[0] = BooleanConstant.TRUE;
		pair[1] = Equation.newInstance(p0);
		return;
	    }
	    if (constLt(nc1, nc0)){
		pair[0] = BooleanConstant.FALSE;
		pair[1] = BooleanConstant.FALSE;
		return;
	    }
	}
    }

    protected void combineLeqLt(SingleConstraint[] pair){
	WeakInequation c0 = (WeakInequation)pair[0];
	StrictInequation c1 = (StrictInequation)pair[1];
	Polynomial p0 = c0.getPolynomial();
	Polynomial p1 = c1.getPolynomial();
	if (p0.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (!constLt(nc1, nc0))
		pair[0] = BooleanConstant.TRUE;
	    else
		pair[1] = BooleanConstant.TRUE;
	    return;
	}
	Polynomial p0neg = p0.negate();
	if (p0neg.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0neg.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (!constLt(nc1, nc0)){
		pair[0] = BooleanConstant.FALSE;
		pair[1] = BooleanConstant.FALSE;
		return;
	    }
	}
    }

    protected void combineLtEq(SingleConstraint[] pair){
	swap(pair);
	combineEqLt(pair);
	swap(pair);
    }

    protected void combineLtLeq(SingleConstraint[] pair){
	swap(pair);
	combineLeqLt(pair);
	swap(pair);
    }

    protected void combineLtLt(SingleConstraint[] pair){
	StrictInequation c0 = (StrictInequation)pair[0];
	StrictInequation c1 = (StrictInequation)pair[1];
	Polynomial p0 = c0.getPolynomial();
	Polynomial p1 = c1.getPolynomial();
	if (p0.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (constLt(nc0, nc1))
		pair[0] = BooleanConstant.TRUE;
	    else
		pair[1] = BooleanConstant.TRUE;
	    return;
	}
	Polynomial p0neg = p0.negate();
	if (p0neg.equalsIgnoreConstant(p1)){
	    NumberWrapper nc0 = p0neg.getConstant();
	    NumberWrapper nc1 = p1.getConstant();
	    if (!constLt(nc1, nc0)){
		pair[0] = BooleanConstant.FALSE;
		pair[1] = BooleanConstant.FALSE;
		return;
	    }
	}
    }

    protected void invalidate(boolean[][] checked, int id){
	for (int i = 0; i < id; i++)
	    checked[i][id] = false;
	for (int i = id+1; i < checked.length; i++)
	    checked[id][i] = false;
    }

    protected void swap(SingleConstraint[] pair){
	SingleConstraint tmp = pair[0];
	pair[0] = pair[1];
	pair[1] = tmp;
    }
}
