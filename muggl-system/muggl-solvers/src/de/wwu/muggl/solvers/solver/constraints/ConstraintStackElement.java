package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.Set;
import java.util.Vector;

import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.solver.SolutionCache;

/**
 * Objects of this class represent single elements of the ConstraintStack. Each
 * ConstraintStackElement object stores the reference to one
 * {@link ComposedConstraint} object whose conjunctions of the DNF are separated and stored 
 * as an array of {@link ConjunctiveConstraintSystem} objects.
 * 
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class ConstraintStackElement{

    protected int firstNoncontradictorySystemIndex;

    protected ConstraintExpression expression;

    /**
     * Stores the index of the first constraint system, for which it was not
     * indicated to have no solution.
     */
    protected int firstPotentiallySolvableSystemIndex;

    /**
     * The reference to the following stack element if exists or null if this
     * ConstraintStackElement object represents the lastly added constraint.
     */
    protected ConstraintStackElement next;

    /**
     * The constraints that are bound to this element of the constraint stack.
     */
    protected ConstraintSystem[] nodeSystems;

    /**
     * Stores the reference to the previously added element of the constraint
     * stack of null if this element is the firstly added element of the stack.
     */
    protected ConstraintStackElement pred;

    protected SolutionCache solutionCache;


    /**
     * The number of clauses in the disjunctive normal form of the constraints
     * contained in the constraint stack from the bottom of the stack to this
     * stack element inclusive. To provide a fast access to this information
     * the number of clauses will only be calculated once at the creation of the
     * object.
     */
    protected int systemCount;

    /**
     * Creates a new ConstraintStackElement instance out of the passed ComposedConstraint.
     * All disjunctions (of type ConjunctiveConstrainSystem) are recursively retrieved
     * out of the {@code constraint}'s DNF.
     * </br>
     * </br>
     * Is only called by the ConstraintStack. Hence the constructor is protected.
     * 
     * @param expression the ConstraintExpression used to create the ComposedConstraint
     * @param constraint the constraint that should be added to the constraint
     * stack.
     * @param pred the lastly added ConstraintStackElement. The new
     * ConstraintStackElement instance will directly be linked behind this
     * element.
     * @see ConjunctiveConstraintSystem
     */
    protected ConstraintStackElement(ConstraintExpression expression, 
	    			     ComposedConstraint constraint, 
	    			     ConstraintStackElement pred){

    	this.pred = pred;
    	this.solutionCache = new SolutionCache(this);

    	// the expression is held without changing it
    	this.expression = expression;

    	// get systems out of composed constraint and collect them in a Vector
    	Vector<ConstraintSystem> systems = new Vector<ConstraintSystem>();
    	for (int i = 0; i < constraint.getSystemCount(); i++) {
    		ConstraintSystem sys = constraint.getSystem(i);
    		if (!sys.isContradictory())
    			systems.add(sys);
    	}

    	// create ConstraintSystem[] nodeSystems
    	if ( systems.isEmpty() ){
    		this.nodeSystems = new ConstraintSystem[1];
    		this.nodeSystems[0] = ConstraintSystem.FALSESYSTEM;
    	} else {
    		this.nodeSystems = new ConstraintSystem[systems.size()];
    		for (int i = 0; i < systems.size(); i++)
    			nodeSystems[i] = systems.get(i);
    	}

    	// update systemCount and some indexes
    	if (pred == null){
    		this.systemCount = nodeSystems.length;
    	} else{
    		int syscount = nodeSystems.length;
    		this.systemCount = pred.systemCount * syscount;
    		this.firstPotentiallySolvableSystemIndex = pred.firstPotentiallySolvableSystemIndex * syscount;
    		this.firstNoncontradictorySystemIndex = pred.firstNoncontradictorySystemIndex * syscount;
    	}
    }

    public void collectVariables(Set<Variable> set){
	for (int i = 0; i < nodeSystems.length; i++)
	    nodeSystems[i].collectVariables(set);
    }

    public boolean containsVariable(Variable variable){
	for (int i = 0; i < nodeSystems.length; i++)
	    if (nodeSystems[i].containsVariable(variable))
		return true;
	return false;
    }

    /**
     * Returns the index of the first system of constraints contained in the stack
     * from this element to the bottom of the stack for which it was not detected
     * to have no solution.
     * @return the index of the first system of constraints contained in the stack
     * from this element to the bottom of the stack for which it was not detected
     * to have no solution.
     */
    public int getFirstNoncontradictorySystemIndex(){
	return firstNoncontradictorySystemIndex;
    }

    public ConstraintSystem getNodeSystem(int idx){
	return nodeSystems[idx];
    }

    public int getNodeSystemCount(){
	return nodeSystems.length;
    }

    /**
     * Returns the solution registered to be a solution of the system of
     * constraints specified by the index <i>idx</i> if one was registered or
     * Solution.NOSOLUTION if it was dectected, that this system of constraints
     * or one of its underlying systems has no solution at all. If none of the
     * alternatives applies the result will be <i>null</i>.
     * @param idx the index of the system of constraints a registered solution
     * should be searched for.
     * @return the registered solution if one exists, Solution.NOSOLUTION if it
     * was recognized that the system of constraints is contradictory or
     * otherwise <i>null</i>.
     * @see de.wwu.muggl.solvers.Solution#NOSOLUTION
     */
    public Solution getCachedSolution(int idx){
	if (idx < firstNoncontradictorySystemIndex)
	    return Solution.NOSOLUTION;
	else
	    return solutionCache.getSolution(idx);
    }

    /**
     * Returns the system of constraints specified by the passed index.
     * @param idx the index of the system on constraints that should be returned.
     * @return  the system of constraints specified by the passed index.
     */
    public ConstraintSystem getSystem(int idx){
	if (pred == null)
	    return nodeSystems[idx];
	else
	    return ConstraintSystem.getConstraintSystem(
		    pred.getSystem(idx / nodeSystems.length),
		    nodeSystems[idx % nodeSystems.length]);
    }

    /**
     * Returns the number of disjunctive associated systems of constraints
     * contained in the stack elements from this element to the bottom of the
     * stack.
     * @return the number of disjunctive associated systems of constraints
     * contained in the stack elements from this element to the bottom of the
     * stack.
     */
    public int getSystemCount(){
	return systemCount;
    }

    /**
     * Associates the solution <i>sol</i> to the system of constraints with the
     * index <i>idx</i>. Following calls to the method getSolution asking for
     * solutions to the system of constraints having the index <i>idx</i> then
     * will return this solution and no new solution has to be calculated for
     * that system of constraints in future.
     * @param idx the index of the system of constraints the solution should be
     * associated to.
     * @param sol the solution of the system of constraints specified by the
     * index <i>idx</i>.
     */
    public void setSolution(final int idx, Solution sol){
	if (idx == firstPotentiallySolvableSystemIndex)
	    while ((firstPotentiallySolvableSystemIndex < getSystemCount()) && (Solution.NOSOLUTION.equals(getCachedSolution(firstPotentiallySolvableSystemIndex))))
		firstPotentiallySolvableSystemIndex++;
	if (!sol.equals(Solution.NOSOLUTION)){
	    ConstraintStackElement current = this;
	    int index = idx;
	    while (current != null){
		// propagate the solution to the older nodes in a loop
		// and not via recursion to avoid that the
		// firstPotentiellySolvableSystemIndex will  be affected
		// by this operation
		current.solutionCache.addSolution(index, sol);
		index/= current.nodeSystems.length;
		current = current.pred;
	    }
	} else
	    if (firstNoncontradictorySystemIndex == idx)
		firstNoncontradictorySystemIndex++;
    }

    public void transformNodeSystems(ConstraintSetTransformer transformer){
	for (int i = 0; i < nodeSystems.length; i++)
	    nodeSystems[i] = nodeSystems[i].transformConstraintSets(transformer);
    }

    public boolean verifySolution(Solution solution){
	if (next == null)
	    return expression.verifySolution(solution);
	else
	    return expression.verifySolution(solution) && next.verifySolution(solution);
    }


    /**
     * Writes the actual state of the constraint stack element into the passed
     * stream.
     * @param logStream the stream the logging informations should be written
     * into.
     * @param idx the index of the constraint stack element that should appear in
     * the log.
     */
    public void writeToLog(PrintStream logStream, int idx){
	logStream.println("<stackelement pos=\"" + idx + "\">");
	for (ConstraintSystem system: nodeSystems)
	    system.writeToLog(logStream);
	logStream.println("<systemcount>" + systemCount + "</systemcount>");
	logStream.println("<firstpotentiallysolvablesystemindex>" + firstPotentiallySolvableSystemIndex + "</firstpotentiallysolvablesystemindex>");
	logStream.println("</stackelement>");
    }
}
