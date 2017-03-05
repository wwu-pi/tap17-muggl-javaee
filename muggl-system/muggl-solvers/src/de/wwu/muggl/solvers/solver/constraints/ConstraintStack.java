package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.conf.ConfigReader;
import de.wwu.muggl.solvers.conf.TesttoolConfig;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * TODOME: doc!
 * @author Christoph Lembeck
 */
@SuppressWarnings("all")
public class ConstraintStack {

    protected static ConstraintSetTransformer[] transformersForMethodGetSystem;

    protected static ConstraintSetTransformer[] transformersForStackElements;

    static{
	TesttoolConfig options = TesttoolConfig.getInstance();
	transformersForMethodGetSystem = options.getTransformersForMethodGetSystem();
	transformersForStackElements = options.getTransformersForStackElements();
    }

    /**
     * Reference to the first added element of the stack.
     */
    protected ConstraintStackElement bottom;

    /**
     * The actual number of stack elements.
     */
    protected int size;

    /**
     * Reference to the last added element of the theck.
     */
    protected ConstraintStackElement top;

    /**
     * Creates a new, emtpy Constraint stack.
     */
    public ConstraintStack(){
	size = 0;
    }

    /**
     * Inserts a new composed constraint at the end of the constraint stack.
     * @param cc the constraint that should be appended to the stack.
     */
    public void addConstraint(ConstraintExpression ce, ComposedConstraint cc){
	addConstraint(ce, cc, null);
    }

    /**
     * Inserts a new composed constraint at the end of the constraint stack and
     * transforms the newly generated system of constraints using the passed
     * ConstraintSetTransformer.
     * @param ce
     * @param cc the constraint that should be appended to the stack.
     * @param transformer the ConstraintSetTransformer that should be applied to
     * the new sets of constraints in the new element of the constraint stack.
     */
    public void addConstraint(ConstraintExpression ce, 
    		ComposedConstraint cc, 
    		ConstraintSetTransformer transformer){
    	ConstraintStackElement newElement;

    	if (bottom == null){
    		bottom = new ConstraintStackElement(ce, cc, null);
    		newElement = bottom;

    		if (transformer != null)
    			bottom.transformNodeSystems(transformer);
    		top = bottom;
    	} else {
    		if (containsExpression(ce))
    			top.next = new ConstraintStackElement(BooleanConstant.TRUE, BooleanConstant.TRUE, top);
    		else
    			top.next = new ConstraintStackElement(ce, cc, top);
    		newElement = top.next;

    		if (transformer != null)
    			top.next.transformNodeSystems(transformer);
    		top = top.next;
    	}

    	for (int i = 0; i < transformersForStackElements.length; i++)
    		newElement.transformNodeSystems(transformersForStackElements[i]);

    	size++;
    }

    /**
     * Adds all the variables contained in the constraints that are kept in the constraint
     * stack to the passed set.
     * @param set the set, the contained variables should be added to
     */
    public void collectVariables(Set<Variable> set){
	ConstraintStackElement e = bottom;
	while (e != null){
	    e.collectVariables(set);
	    e = e.next;
	}
    }

    /**
     * Checks whether a ConstraintExpression is already on the stack or not.
     * @param ce
     * @return true if ce is already on the stack
     */
    public boolean containsExpression(ConstraintExpression ce){
	ConstraintStackElement element = bottom;
	while (element != null){
	    if (element.expression.equals(ce))
		return true;
	    element = element.next;
	}
	return false;
    }

    /**
     * Checks whether the constraint stack contains a constraint using the passed
     * variable <i>var</i> or not.
     * @param var the variable that should be searched for in the constraints.
     * @return <i>true</i> if the variable is member of one of the constraints,
     * <i>false</i> otherwise.
     */
    public boolean containsVariable(Variable var){
	ConstraintStackElement cse = bottom;
	while (cse != null){
	    if (cse.containsVariable(var))
		return true;
	    cse = cse.next;
	}
	return false;
    }


    /**
     * Returns the index of the first system of constraints for which it was not
     * recognized before that no solution for it exists. Finding a solution for
     * the whole system can so be started at this index to avoid duplicate
     * calculations for the same systems of constraints.
     * @return the index of the first system of constraints it is reasonable to
     * find a solution for. 0 if stack is empty.
     */
    public int getFirstNoncontradictorySystemIndex(){
	if (top != null)
	    return top.getFirstNoncontradictorySystemIndex();
	else
	    return 0;
    }

    /**
     * Returns the actual number of elements in the constraint stack.
     * @return the actual number of elements in the constraint stack
     */
    public int getSize(){
	return size;
    }

    /**
     * Returns the already computed, registered solution of the system of
     * constraints identified by the parameter <i>idx</i>. If no solution
     * was registered for the specified system of constraints, yet, it will be
     * checked if one of the previously considered systems of constraints, the
     * actual system is based on, is marked to have no solution at all. In this
     * case the actual system will be marked as well and the result will be the
     * constant value <i>System.NOSOLUTION</i>. If no cached solution or
     * inconsistency can be found, the result will be <i>null</i>.
     * @param idx the index of the system of constraints a cached solution should
     * be searched for considering the actual state of the constraint stack
     * @return a previously calculated solution of the given system of
     * constraints, if one was registered before. <i>System.NOSOLUTION</i>, if
     * the considered system itself or one of the underlying systems was detected
     * to have no solution. <i>null</i>, if no solution was registerd before and
     * the system is not marked to have no solution
     */
    public Solution getCachedSolution(int idx){
	return top.getCachedSolution(idx);
    }

    /**
     * Returns the conjunctive associated system of constraints with the index
     * <i>idx</i> of the disjunctive normal form of the overall constraint build
     * by all the constraints contained in the ConstraintStack (Beginning with
     * 0). The disjunctive normalform of the constraint has not to be calculated
     * for this purpose, thus the calculation of the system can be performed very
     * fast.
     * @param idx the index of the conjunctive associated system of conatraints in
     * the disjuntive normal form of the constraint build by the stack.
     * @return the conjunctive associated system of constraints with the index
     * <i>idx</i>.
     */
    public ConstraintSystem getSystem(int idx){
	ConstraintSystem result = top.getSystem(idx);
	for (int i = 0; i < transformersForMethodGetSystem.length; i++){
	    result = result.transformConstraintSets(transformersForMethodGetSystem[i]);
	}
	return result;
    }

    /**
     * Returns the number of disjunctive associated systems of constraints in the
     * disjunctive normal form of the constraints contained in this stach.
     * @return the number of disjunctive associated systems of constraints
     * build by the whole stack.
     */
    public int getSystemCount(){
	if (top == null)
	    return 0;
	else
	    return top.systemCount;
    }

    public void printStack() {
	printStack( TesttoolConfig.getLogger() );
    }
    
    
    public void printStack(Logger solverLogger){
	if (solverLogger.isDebugEnabled()) {
	    ConstraintStackElement current = bottom;
	    if (current == null)
		solverLogger.debug("<stack is empty>"); // Changed 2008.02.05
	    else {
		int idx = 0;
		while (current != null){
		    solverLogger.debug("stack element nr " + idx++ + " : " + current.expression); // Changed 2008.02.05
		    solverLogger.debug(" " + current.solutionCache.toString()); // Changed 2008.02.05
		    solverLogger.debug(" first noncontradictory system: " + current.getFirstNoncontradictorySystemIndex()); // Changed 2008.02.05
		    for (int systemIdx = 0; systemIdx < current.getNodeSystemCount(); systemIdx++){
			if (current.getNodeSystemCount() > 1)
			    solverLogger.debug("  system " + systemIdx); // Changed 2008.02.05
			ConstraintSystem system = current.getNodeSystem(systemIdx);
			for (int setIdx = 0; setIdx < system.getConstraintSetCount(); setIdx++){
			    if (system.getConstraintSetCount() > 1)
				solverLogger.debug("    set " + setIdx); // Changed 2008.02.05
			    SingleConstraintSet set = system.getConstraintSet(setIdx);
			    for (int constraintIdx = 0; constraintIdx < set.getConstraintCount(); constraintIdx++)
				solverLogger.debug("      " + set.getConstraint(constraintIdx)); // Changed 2008.02.05
			}
		    }
		    current = current.next;
		}
	    }
	}
    }

    /**
     * Removes the lastly added constraint from the stack.
     */
    public void removeConstraint(){
	if (top.pred == null){
	    bottom = null;
	    top = null;
	} else{
	    top = top.pred;
	    top.next = null;
	}
	size--;
    }

    /**
     * Removes the lastly <i>count</i> added constraints from the stack.
     * @param count the number of constraints to remove.
     */
    public void removeConstraints(int count){
	for (int i = 0; i < count; i++)
	    removeConstraint();
    }

    /**
     * Associates the solution <i>sol</i> to the system of constraints with the
     * index <i>idx</i>. Following calls to the method getSolution asking for
     * solutions to the system of constraints having the index <i>idx</i> then
     * will return this solution and no new solution has to be calculated for
     * that system of constraints in future.
     * @param idx the index of the system of constraints the solution should be
     * associated to.
     * @param sol the solution of the system of constraints specified by the index
     * <i>idx</i>.
     */
    public void setSolution(int idx, Solution sol){
	top.setSolution(idx, sol);
    }

    /**
     * Returns an Enumeration object to enumerate the single elements of the
     * ConstraintStack from the latest added element to the first added element.
     * @return the Enumeration of the current elements of the stack.
     */
    public Enumeration<ConstraintStackElement> stackElements(){
	return new Enumeration<ConstraintStackElement>(){

	    protected ConstraintStackElement next = top;

	    public boolean hasMoreElements() {
		return next != null;
	    }

	    public ConstraintStackElement nextElement() {
		ConstraintStackElement result = next;
		next = next.pred;
		return result;
	    }
	};
    }

    public boolean verifySolution(Solution solution){
	return bottom.verifySolution(solution);
    }

    /**
     * Writes the actual state of the constraint stack into the passed stream.
     * @param logStream the stream the logging informations should be written
     * into.
     */
    public void writeToLog(PrintStream logStream){
	logStream.println("<stackstate stackelements=\"" + size + "\">");
	Enumeration<ConstraintStackElement> elements = stackElements();
	int idx = size - 1;
	while (elements.hasMoreElements()){
	    ConstraintStackElement element = elements.nextElement();
	    element.writeToLog(logStream, idx--);
	}
	logStream.println("</stackstate>");
    }

}
