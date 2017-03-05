package de.wwu.muggl.solvers.solver.tsolver.bisection;

import java.util.SortedSet;
import java.util.TreeSet;

import de.wwu.muggl.solvers.expressions.NumericVariable;

/**
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class MultiIndexSet extends TreeSet<MultiIndex>{

    protected MultiIndexVariablesReference variablesRef;

    public MultiIndexSet(SortedSet<NumericVariable> vars){
	super();
	variablesRef = new MultiIndexVariablesReference(vars);
    }

    public MultiIndexVariablesReference getVariablesReference(){
	return variablesRef;
    }

}
