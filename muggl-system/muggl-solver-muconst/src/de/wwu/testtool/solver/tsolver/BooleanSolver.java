package de.wwu.testtool.solver.tsolver;

import java.util.ArrayList;
import java.util.List;

import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.BooleanVariable;
import de.wwu.muggl.solvers.solver.HasSolutionInformation;
import de.wwu.muggl.solvers.solver.Solver;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraintSet;
import de.wwu.muggl.solvers.solver.constraints.NotConstraint;
import de.wwu.muggl.solvers.solver.constraints.SingleConstraint;

/**
 * Simple boolean sover for handling the purely boolean problems that
 * occure during the symbolic execution of java bytecodes.
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class BooleanSolver implements Solver{

    public static BooleanSolver newInstance(SolverManager solverManager){
	return new BooleanSolver(solverManager);
    }

    /**
     * The SolverManager instance this solver belongs to.
     */
    protected SolverManager solverManager;

    /**
     * Stores the current constraint system of the solver
     */
    protected SingleConstraintSet system;

    /**
     * Creates a new Boolean Solver and initializes it with the given set of
     * constraints.
     * @param solverManager the instance of SolverManager this Solver belongs to.
     */
    private BooleanSolver(SolverManager solverManager){
	this.solverManager = solverManager;
	system = new SingleConstraintSet();
    }

    /**
     * Adds a new constraint to the system of constraints a solution should be
     * calculated for by this solver.
     * @param constraint the additional constraint.
     */
    public void addConstraint(SingleConstraint constraint){
	system.add(constraint);
    }

    public void addConstraintSet(SingleConstraintSet set){
	for (SingleConstraint constraint: set)
	    system.add(constraint);
    }

    /**
     * Returns the name of the solver for displaying it in some user interfaces or
     * logfiles.
     * @return the name of the solver.
     */
    public String getName(){
	return "BooleanSolver";
    }

    public List<Class<? extends Solver>> getRequiredSubsolvers(){
	List<Class<? extends Solver>> result = null;
//	List<Class<? extends Solver>> result = new ArrayList<Class<? extends Solver>>(); 
//	result.add(SimplexSolverCL.class);
	return result;
    }

    /**
     * Calculates a single solution that satisfies the prior added constraints.
     * @return the solution that satisfies the constraints passed to this solver
     * or Solution.NOSOLUTION if definitively no such solution exists.
     * @throws IncorrectSolverException if the solver is not able to handle the
     * actual set of constraints.
     * @see de.wwu.muggl.solvers.Solution#NOSOLUTION
     */
    public Solution getSolution() throws IncorrectSolverException{
	Solution result = new Solution();
	for (int i = 0; i < system.getConstraintCount(); i++){
	    SingleConstraint constraint = system.getConstraint(i);
	    if (constraint.equals(BooleanConstant.TRUE))
		return new Solution();
	    if (constraint instanceof BooleanVariable){
		BooleanVariable var = (BooleanVariable) constraint;
		if (result.containsVariable(var)){
		    if (result.getValue(var).equals(BooleanConstant.FALSE))
			return Solution.NOSOLUTION;
		} else
		    result.addBinding(var, BooleanConstant.TRUE);
	    } else
		if (constraint instanceof NotConstraint){
		    NotConstraint not = (NotConstraint)constraint;
		    BooleanVariable var = not.getVariable();
		    if (result.containsVariable(var)){
			if (result.getValue(var).equals(BooleanConstant.TRUE))
			    return Solution.NOSOLUTION;
		    } else
			result.addBinding(var, BooleanConstant.FALSE);
		} else{
		    throw new IncorrectSolverException("Not a boolean system: " + constraint);
		}
	}
	return result;
    }

    public boolean handlesEquations(){
	return false;
    }

    public boolean handlesIntegerEquations(){
	return false;
    }

    public boolean handlesNonlinearProblems(){
	return false;
    }

    public boolean handlesNumericProblems(){
	return false;
    }

    public boolean handlesStrictInequalities(){
	return false;
    }

    public boolean handlesStrictIntegerInequalities(){
	return false;
    }

    public boolean handlesWeakInequalities(){
	return false;
    }

    public boolean handlesWeakIntegerInequalities(){
	return false;
    }

    /**
     * Checks whether a solution exists for the system of constraints stored in
     * the actual constraint stack by using the hasSolution methods of the
     * constraint solvers. This method may be a little bit faster when only
     * an assertion about the solvability of a system of constraints should be
     * calculated.
     * @return <i>true</i> if any solution for the given problem exists,
     * <i>false</i> if definetively no solution satisfies the constraints.
     * @throws IncorrectSolverException if the solver is not able to handle the
     * actual set of constraints.
     */
    public HasSolutionInformation hasSolution() throws IncorrectSolverException{
	return new HasSolutionInformation(getSolution());
    }

    /**
     * Removes the lastly added constraint from the set of constraints.
     */
    public void removeConstraint() {
	system.removeLastConstraint();
    }

    public Solver reset(){
	return newInstance(solverManager);
    }

}
