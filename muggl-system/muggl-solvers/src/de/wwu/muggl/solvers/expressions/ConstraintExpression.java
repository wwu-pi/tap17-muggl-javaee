package de.wwu.muggl.solvers.expressions;

import java.io.PrintStream;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.solver.constraints.Assignment;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * Superclass for all expressions that may be passed as constraints to the
 * SolverManager.
 * @author Christoph Lembeck
 */
public abstract class ConstraintExpression implements Expression{

    /**
     * Transforms the constraint that is represented by this expression into
     * an easier manageable ComposedConstraint object that will later be processed
     * by the SolverManager.
     * @param subTable TODOME: doc!
     * @return the ComposedConstraint object representing the same constraint as
     * this expression.
     * @see de.wwu.muggl.solvers.solver.constraints.ComposedConstraint
     * @see de.wwu.muggl.solvers.SolverManager#addConstraint(ConstraintExpression)
     */
    public abstract ComposedConstraint convertToComposedConstraint(SubstitutionTable subTable);

    @Override
    public abstract ConstraintExpression insertAssignment(Assignment assignment);

    @Override
    public abstract ConstraintExpression insert(Solution solution, boolean produceNumericSolution);

    /**
     * Returns true, because all constraints are either true of false.
     * @return <i>true</i>.
     */
    @Override
    public boolean isBoolean(){
	return true;
    }

    public abstract ConstraintExpression negate();

    /**
     * Returns a string representation of this CosntraintExpression.
     * @return a string representation of this ConstraintExpression.
     */
    @Override
    public String toString(){
	return toString(false);
    }
    
    /**
     * TODOME: doc!
     * @return
     */
    public String toTexString(){
	return toTexString(false);
    }

    /**
     * Checks whether the given solution satisfies this expression or not.
     * @param solution
     * @return true if expression is satisfied by given solution.
     */
    public boolean verifySolution(Solution solution){
	return insert(solution, false).equals(BooleanConstant.TRUE);
    }

    /**
     * Writes this expression into the log stream.
     * @param logStream the stream this expression should be written into.
     */
    @Deprecated
    public void writeCEToLog(PrintStream logStream){
	logStream.print("<constraintexpression>");
	logStream.print(toString());
	logStream.println("</constraintexpression>");
    }
    
    
}
