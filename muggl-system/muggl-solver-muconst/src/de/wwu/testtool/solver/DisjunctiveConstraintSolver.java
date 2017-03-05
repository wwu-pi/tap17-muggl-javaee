package de.wwu.testtool.solver;

import java.util.ArrayList;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;

@SuppressWarnings("all")
public class DisjunctiveConstraintSolver implements ComposedConstraintSolver {

    @Override
    public void addConstraint(ComposedConstraint constraint)
	    throws IncorrectSolverException {

    }

    @Override
    public Solution getSolution() throws SolverUnableToDecideException,
	    TimeoutException {
	return null;
    }

    @Override
    public void removeConstraint() {
    }

    @Override
    public ComposedConstraintSolver reset() {
	return null;
    }
    
    private class DisjunctiveNode{
	ComposedConstraint disjunctiveConstraint;
	ConjunctiveNode firstConjunctiveNode;
    }
    
    private class ConjunctiveNode{
	/**
	 * Of type AndConstraint or SingleConstraint.
	 */
	ComposedConstraint conjunctiveConstraint;
	ConjunctiveNode right;
	ConjunctiveNode left;
	ArrayList<ConjunctiveNode> unsolvableSuccessors;
	ConjunctiveNode solvablePredecessor;
    }
}
