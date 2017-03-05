package de.wwu.muggl.solvers.solver;

import de.wwu.muggl.solvers.Solution;

/**
 * TODOME: doc!
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class HasSolutionInformation{

    private final boolean hasSolution;

    private final Solution solution;

    public HasSolutionInformation(Solution solution){
	this(!solution.equals(Solution.NOSOLUTION), solution);
    }

    public HasSolutionInformation(boolean hasSolution){
	this(hasSolution, null);
    }

    public HasSolutionInformation(boolean hasSolution, Solution solution){
	this.hasSolution = hasSolution;
	this.solution = solution;
    }

    public Solution getSolution(){
	return solution;
    }

    public boolean hasSolution(){
	return hasSolution;
    }

}
