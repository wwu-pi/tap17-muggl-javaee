package de.wwu.testtool.solver;

import java.util.Vector;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.ConstraintSetTransformer;
import de.wwu.muggl.solvers.solver.constraints.Equation;
import de.wwu.muggl.solvers.solver.constraints.NumericConstraint;
import de.wwu.muggl.solvers.solver.constraints.Polynomial;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;
import de.wwu.muggl.solvers.solver.numbers.NumberWrapper;

/**
 * @author Christoph
 */
public class SimpleConstraintSetOptimizer implements ConstraintSetTransformer {

    @SuppressWarnings("unused") 
    public SingleConstraintSet transform(SingleConstraintSet constraintSet) {
	// check if any Assignments are already contained or if any Assignments
	// can be read of trivially and insert the assignments into the
	// remaining constraints
	Vector<Assignment> assignments = new Vector<Assignment>();
	Vector<SingleConstraint> checkedConstraints = new Vector<SingleConstraint>();
	Vector<SingleConstraint> uncheckedConstraints = new Vector<SingleConstraint>();
	for (int i = 0; i < constraintSet.getConstraintCount(); i++)
	    uncheckedConstraints.add(constraintSet.getConstraint(i));
	while (!uncheckedConstraints.isEmpty()){
	    SingleConstraint constraint = uncheckedConstraints.remove(0);
	    if (constraint instanceof Assignment){
		assignments.add((Assignment)constraint);
		uncheckedConstraints.addAll(checkedConstraints);
		checkedConstraints.removeAllElements();
	    } else{
		for (int assignmentIdx = 0; assignmentIdx < assignments.size(); assignmentIdx++)
		    constraint = constraint.insert(assignments.get(assignmentIdx));
		Solution solution = null; //constraint.getUniqueSolution();
		if (solution != null){
		    if (solution == Solution.NOSOLUTION)
			return new SingleConstraintSet(BooleanConstant.FALSE);
		    for (Variable variable: solution.variables()){
			Constant value = solution.getValue(variable);
			Assignment assignment = new Assignment(variable, value);
			constraint = constraint.insert(assignment);
			assignments.add(assignment);
		    }
		    uncheckedConstraints.addAll(checkedConstraints);
		    checkedConstraints.removeAllElements();
		}
		combineConditions(checkedConstraints, uncheckedConstraints, assignments, constraint);
	    }
	}

	SingleConstraintSet result = new SingleConstraintSet();
	for (int i = 0; i < checkedConstraints.size(); i++)
	    result.add(checkedConstraints.get(i));
	for (int i = 0; i < assignments.size(); i++)
	    result.add(assignments.get(i));
	return result;
    }

    protected void combineConditions(Vector<SingleConstraint> checkedConstraints, Vector<SingleConstraint> uncheckedConstraints, Vector<Assignment> assignments, SingleConstraint constraint){
	if (constraint instanceof NumericConstraint){
	    NumericConstraint constraint1 = (NumericConstraint)constraint;
	    Polynomial polynomial1 = constraint1.getPolynomial();
	    for (int j = 0; j < uncheckedConstraints.size(); j++){
		SingleConstraint sConstraint2 = uncheckedConstraints.get(j);
		if (sConstraint2 instanceof NumericConstraint){
		    NumericConstraint constraint2 = (NumericConstraint)sConstraint2;
		    Polynomial polynomial2 = constraint2.getPolynomial();
		    if (polynomial1.equals(polynomial2)){
			// the left hand sides of the conditions are equal
			if ((constraint1.isEquation() && constraint2.isEquation()) ||
				(constraint1.isWeakInequation() && constraint2.isWeakInequation()) ||
				(constraint1.isStrictInequation() && constraint2.isStrictInequation())){
			    // the constraints are equal and one of them can be removed
			    uncheckedConstraints.remove(j);
			    j--;
			}
			if (constraint1.isEquation() && constraint2.isWeakInequation()){
			    uncheckedConstraints.remove(j);
			    j--;
			}
			if (constraint2.isEquation() && constraint1.isWeakInequation()){
			    return;
			}
			if ((constraint1.isEquation() && constraint2.isStrictInequation()) ||
				(constraint2.isEquation() && constraint1.isStrictInequation())){
			    checkedConstraints.removeAllElements();
			    uncheckedConstraints.removeAllElements();
			    assignments.removeAllElements();
			    checkedConstraints.add(BooleanConstant.FALSE);
			    return;
			}
			if (constraint1.isWeakInequation() && constraint2.isStrictInequation()){
			    return;
			}
			if (constraint2.isWeakInequation() && constraint1.isStrictInequation()){
			    uncheckedConstraints.remove(j);
			    j--;
			}
		    }
		    if (polynomial1.equals(polynomial2.negate())){
			if (constraint1.isStrictInequation() || constraint2.isStrictInequation()){
			    checkedConstraints.removeAllElements();
			    uncheckedConstraints.removeAllElements();
			    assignments.removeAllElements();
			    checkedConstraints.add(BooleanConstant.FALSE);
			    return;
			}
			if (constraint1.isWeakInequation() && constraint2.isWeakInequation()){
			    uncheckedConstraints.remove(j);
			    j--;
			    uncheckedConstraints.add(Equation.newInstance(polynomial1));
			    return;
			}
			if (constraint1.isWeakInequation() && constraint2.isEquation()){
			    return;
			}
			if (constraint2.isWeakInequation() && constraint1.isEquation()){
			    uncheckedConstraints.remove(j);
			    j--;
			}
			if (constraint1.isEquation() && constraint2.isEquation()){
			    uncheckedConstraints.remove(j);
			    j--;
			}
		    }
		    if (constraint1.isEquation() && constraint2.isEquation() && polynomial1.equalsIgnoreConstant(polynomial2)){
			NumberWrapper const1 = polynomial1.getConstant();
			if (const1 != null && const1.isZero())
			    const1 = null;
			NumberWrapper const2 = polynomial2.getConstant();
			if (const2 != null && const2.isZero())
			    const2 = null;
			if ((const1 == null ^ const2 == null) || (const1 != null && const2 != null && !const1.equals(const2))){
			    checkedConstraints.removeAllElements();
			    uncheckedConstraints.removeAllElements();
			    assignments.removeAllElements();
			    checkedConstraints.add(BooleanConstant.FALSE);
			    return;
			}
		    }
		}
	    }
	    checkedConstraints.add(constraint1);
	} else
	    // the constraint is not numeric
	    checkedConstraints.add(constraint);
    }
}
