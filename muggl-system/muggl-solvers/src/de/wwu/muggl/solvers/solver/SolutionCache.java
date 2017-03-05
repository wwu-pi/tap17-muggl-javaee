package de.wwu.muggl.solvers.solver;

import java.util.TreeSet;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.constraints.ConstraintStackElement;

@SuppressWarnings("all")
public class SolutionCache{

    static class CacheElement implements Comparable<CacheElement>{

	protected int index;

	protected Solution solution;

	public CacheElement(int index, Solution solution){
	    this.index = index;
	    this.solution = solution;
	}

	public int compareTo(CacheElement other){
	    return this.index - other.index;
	}

	public boolean equals(CacheElement other){
	    return other.index == this.index;
	}

	@Override
	public int hashCode(){
	    return index;
	}

	@Override
	public String toString(){
	    return "CacheElement[" + index + ", " + solution + "]";
	}
    }

    protected TreeSet<CacheElement> elements;

    protected ConstraintStackElement stackElement;

    public SolutionCache(ConstraintStackElement stackElement){
	this.stackElement = stackElement;
	elements = new TreeSet<CacheElement>();
    }

    public void addSolution(int index, Solution solution){
	TreeSet<Variable> variables = new TreeSet<Variable>();
	stackElement.getSystem(index).collectVariables(variables);
	elements.add(new CacheElement(index, solution.getSubsolution(variables)));
    }

    @Override
    public String toString(){
	return "SolutionCache" + elements.toString();
    }

    public Solution getSolution(int idx){
	return null;
    }
}
