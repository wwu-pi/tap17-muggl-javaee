package de.wwu.testtool.solver;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.conf.SolverManagerConfig;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.exceptions.IncorrectSolverException;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.solver.constraints.ComposedConstraint;
import de.wwu.muggl.solvers.solver.listener.SolverManagerListener;
import de.wwu.muggl.solvers.solver.listener.SolverManagerListenerList;
import de.wwu.muggl.solvers.solver.tools.SubstitutionTable;

/**
 * 
 * @author Marko Ernsting
 *
 */
public class SolverManagerNew implements SolverManager, MuconstSolverManager {

    private SolverManagerListenerList listeners = new SolverManagerListenerList();
    private ComposedConstraintSolver composedConstraintSolver;
    private SubstitutionTable substitutionTable = new SubstitutionTable();    
    
    /**
     * Constructor
     */
    public SolverManagerNew(){
	composedConstraintSolver = new OldSolverManagerSolver(this);
	
	for (SolverManagerListener listener : SolverManagerConfig.getInstance().getListeners()){
	    listeners.addListener(listener);
	    TesttoolConfig.getLogger().debug("SolverManager: added listener " + listener.getClass().getName());
	}
    }
    
    public void addConstraints(ConstraintExpression... constraints) {
    	
    }
    
    @Override
    public void addConstraint(ConstraintExpression ce) {
	ComposedConstraint cc = ce.convertToComposedConstraint(substitutionTable);
	substitutionTable.signalStackElementAdded();
	
	
	
	try {
	    composedConstraintSolver.addConstraint(cc);
	} catch (IncorrectSolverException e) {
	    e.printStackTrace();
	}
	
	listeners.fireAddConstraint(this, ce, cc);
	
	//return cc;
    }

    @Override
    public Solution getSolution() throws SolverUnableToDecideException, TimeoutException {
	listeners.fireGetSolutionStarted(this);
	long startTime = System.nanoTime();
	Solution solution = composedConstraintSolver.getSolution();
	listeners.fireGetSolutionFinished(this, solution, System.nanoTime() - startTime);	
	return solution;
    }

    public boolean hasSolution() throws SolverUnableToDecideException, TimeoutException {
	listeners.fireHasSolutionStarted(this);
	long startTime = System.nanoTime();
	Boolean hasSolution = composedConstraintSolver.getSolution() == null ? false: true;
	listeners.fireHasSolutionFinished(this, hasSolution, System.nanoTime() - startTime);	
	return hasSolution;
    }

    @Override
    public void removeConstraint() {
	composedConstraintSolver.removeConstraint();
	substitutionTable.signalStackElementRemoved();
	listeners.fireConstraintRemoved(this);
    }

    @Override
    public void reset() {
	substitutionTable = new SubstitutionTable();
	composedConstraintSolver = composedConstraintSolver.reset();
    }

    @Override
    public boolean verifySolution(Solution solution) {
	return false;
    }

    @Override @Deprecated
    public long getTotalConstraintsChecked() { return 0; }

    @Override @Deprecated
    public void resetCounter() {};
    
    @Override
    public void finalize() throws Throwable{
	
    }

	@Override
	public void addStaticConstraint(ConstraintExpression constraint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getConstraintLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetConstraintLevel(int level) {
		// TODO Auto-generated method stub
		
	}
}
