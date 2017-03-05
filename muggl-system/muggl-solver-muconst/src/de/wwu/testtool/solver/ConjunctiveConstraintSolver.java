package de.wwu.testtool.solver;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;

public class ConjunctiveConstraintSolver implements ComposedConstraintSolver {

    @Override
    public void addConstraint(ComposedConstraint constraint)
	    throws IncorrectSolverException {
	// TODOME: check that constraints are of type AndConstraint and SingleConstraint only
	
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

}
