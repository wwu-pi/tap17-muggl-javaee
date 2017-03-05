package de.wwu.muggl.solvers.solver.constraints;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * Represents a system of independent and <i>conjunctive</i> combined sets of
 * constraints. Each constraint set stored in objects of this class is
 * independent of the other constraint sets in that way that they do not share any
 * common variables. 
 * </BR></BR>
 * The independent sub systems of a ConstraintSystem are non-independent conjunctive 
 * combined systems i.e. {@link SingleConstraintSet} objects.
 * </BR></BR>
 * Finding a solution for a ConstraintSystem means to find
 * a solution for each subsystem (the ConstraintSets) and combining these
 * solutions. If for one of the subsystems no solution can be found, the whole
 * system will have no solution.
 * 
 * @author Christoph Lembeck
 */
public abstract class ConstraintSystem {

    /**
     * Class used to represent a contradictory constraint system.
     */
    public static final ConstraintSystem FALSESYSTEM = new ConstraintSystem(){

	@Override
	public void collectVariables(Set<Variable> set){
	}

	@Override
	public boolean containsVariable(Variable variable){
	    return false;
	}

	@Override
	public SingleConstraintSet getConstraintSet(int idx) {
	    return new SingleConstraintSet(BooleanConstant.FALSE);
	}

	@Override
	public int getConstraintSetCount() {
	    return 1;
	}

	@Override
	public boolean isContradictory() {
	    return true;
	}

	@Override
	public String toString() {
	    return "false";
	}

	@Override
	public ConstraintSystem transformConstraintSets(ConstraintSetTransformer transformer){
	    return this;
	}
    };

    /**
     * Constructs a new ConjunctiveConstraintSystem containing both systems cs1 and cs2.
     * @param cs1
     * @param cs2
     * @return a ConjunctiveConstraintSystem containing cs1 and cs2
     */
    public static ConstraintSystem getConstraintSystem(ConstraintSystem cs1, ConstraintSystem cs2){

	if ( cs1.isContradictory() ) return cs1;
	if ( cs2.isContradictory() ) return cs2;

	// get all constraints out of all constraint sets from both systems and
	// put them in a new ConjunctiveConstraintSystem
	
	
	Vector<SingleConstraint> constraints = new Vector<SingleConstraint>();
	for (int i = 0; i < cs1.getConstraintSetCount(); i++){
	    SingleConstraintSet set = cs1.getConstraintSet(i);
	    for (int j = 0; j < set.getConstraintCount(); j++)
		constraints.add( set.getConstraint(j) );
	}
	for (int i = 0; i < cs2.getConstraintSetCount(); i++){
	    SingleConstraintSet set = cs2.getConstraintSet(i);
	    for (int j = 0; j < set.getConstraintCount(); j++)
		constraints.add( set.getConstraint(j) );
	}

	return ConjunctiveConstraintSystem.getConstraintSystem(constraints);
    }

    /**
     * Constructs a new ConjunctiveConstraintSystem of constraint.
     * @param constraint
     * @return a conjunctive constraint system containing constraint.
     */
    public static ConstraintSystem getConstraintSystem(SingleConstraint constraint){
	if (constraint.equals(BooleanConstant.FALSE))
	    return FALSESYSTEM;
	else
	    return new ConjunctiveConstraintSystem(constraint);
    }

    public static boolean intersects(Set<Variable> a, Set<Variable> b){
	Iterator<Variable> it = a.iterator();
	while (it.hasNext()){
	    if (b.contains(it.next()))
		return true;
	}
	return false;
    }

    public abstract void collectVariables(Set<Variable> set);

    public abstract boolean containsVariable(Variable variable);

    /**
     * Constraints of a Conjunctive Constraint System are stored in ConstraintSet objects.
     * Each one of these is variable-disjunct to the others.
     * @param idx Index of the system to get.
     * @return {@link SingleConstraintSet} according to idx.
     */
    public abstract SingleConstraintSet getConstraintSet(int idx);

    public abstract int getConstraintSetCount();

    public abstract boolean isContradictory();

    @Override
    public abstract String toString();

    public abstract ConstraintSystem transformConstraintSets(ConstraintSetTransformer transformer);

    /**
     * Writes this constraint system into the passed log stream.
     * @param logStream the stream the logging informations should be written
     * into.
     */
    public void writeToLog(PrintStream logStream){
	logStream.print("<constraintsystem contradictory=\"" + isContradictory() + "\" ");
	logStream.println("constraintsetcount=\"" + getConstraintSetCount() + "\">");
	for (int i = 0; i < getConstraintSetCount(); i++){
	    getConstraintSet(i).writeToLog(logStream);
	}
	logStream.println("</constraintsystem>");
    }



    /**
     * Nested Class for a conjunctive constraint system in which each component of the system
     * i.e. each constraint set has no intersecting variables with the system's other constraint sets.
     * </BR></BR>
     * This is basically a {@link ConstraintSystem}. 
     * </BR></BR>
     * Should never be contradictory. For that case use
     * {@link ConstraintSystem#FALSESYSTEM ConstraintSystem.FALSESYSTEM}.
     * @author Christoph
     * @see SingleConstraintSet
     */
    private static class ConjunctiveConstraintSystem extends ConstraintSystem{

	/**
	 * Mapping between variables and constraints.
	 */
	protected Vector<VariablesConstraintsMap> constraintMap;


	/**
	 * Build a (conjunctive) constraint system from several single constraints.
	 * Simplifications are done while inserting, like insertion of assignments into
	 * constraints and returning {@link ConstraintSystem#FALSESYSTEM ConstraintSystem.FALSESYSTEM}
	 * in case of contradictory assignments or constraints that are actually
	 * {@link BooleanConstant#FALSE BooleanConstant.False}.
	 * </br></br>
	 * Optimizations:
	 *  - contradictory assignments lead to FALSESYSTEM
	 *  - assignment insertion
	 *  - false constants lead to FALSESYSTEM
	 *  - multiple true constants are ignored
	 *  - separation of disjunct constraints in different VariablesConstraintsMaps
	 *  
	 * @param constraintVec Vector of single constraints to be added to a new ConstraintSystem
	 * @return ConstraintSystem Is either {@link ConstraintSystem.FALSESYSTEM} or 
	 * 	{@link ConjunctiveConstraintSystem}.
	 */
	protected static ConstraintSystem getConstraintSystem(Vector<SingleConstraint> constraintVec){

	    Vector<SingleConstraint> constraints = new Vector<SingleConstraint>();
	    Vector<Assignment> assignments = new Vector<Assignment>();

	    // separate into assignments, false and true constants and other constraints (And, Or, NumericConstraint)
	    for (int i = 0; i < constraintVec.size(); i++){
		SingleConstraint sc = constraintVec.get(i);

		if (sc instanceof Assignment){
		    // see if assignment is contradictory to one of the other assignments already processed
		    for (int j = 0; j < assignments.size(); j++){
			Assignment ass2 = assignments.get(j);
			if (((Assignment) sc).insert(ass2).equals(BooleanConstant.FALSE))
			    return FALSESYSTEM;
		    }
		    // otherwise keep it
		    assignments.add((Assignment) sc);
		} else {
		    
		    // false constant
		    if (sc.equals(BooleanConstant.FALSE)) {
			return FALSESYSTEM;
		    } else {
			// true constant
			if (sc.equals(BooleanConstant.TRUE)){
			    // only add it if its the only constraint
			    if ((constraints.size() == 0) && 
				(assignments.size() == 0) && 
				(i == constraintVec.size() - 1)) 
			    {
				constraints.add(sc);
			    }
			} else {
			    // remaining constraints
			    constraints.add(sc);
			}
		    }
		}
	    }

	    // insert assignments into the constraints containing their variables
	    for (int i = 0; i < assignments.size(); i++){
		Assignment assignment = assignments.get(i);

		for (int j = 0; j < constraints.size(); j++){
		    SingleConstraint tmp = constraints.remove(j);

		    if (tmp.containsVariable( assignment.getVariable() ))
			tmp = tmp.insert(assignment);

		    constraints.add(j, tmp);
		}
	    }


	    // collect variable <-> constraint mapping in variables constraint map
	    // first one map for each constraint/assignment
	    Vector<VariablesConstraintsMap> newMapps = new Vector<VariablesConstraintsMap>();
	    VariablesConstraintsMap map;
	    
	    // for constraints
	    for (int j = 0; j < constraints.size(); j++){
		// maybe we have false systems after insertion of assignments
		SingleConstraint sc = constraints.get(j);
		if ( sc.equals(BooleanConstant.FALSE) )
		    return FALSESYSTEM;

		map = new VariablesConstraintsMap();
		map.constraints = new SingleConstraintSet(sc);
		map.variables = new HashSet<Variable>();
		sc.collectVariables(map.variables);

		newMapps.add(map);
	    }

	    // for assignments
	    for (int i = 0; i < assignments.size(); i++){
		Assignment assignment = assignments.get(i);

		map = new VariablesConstraintsMap();
		map.constraints = new SingleConstraintSet(assignment);
		map.variables = new HashSet<Variable>();
		map.variables.add( assignment.getVariable() );

		newMapps.add(map);
	    }

	    // now unite intersecting variables and their constraints
	    // i.e. separate disjunct constraints into different VariablesConstraintsMaps
	    for (int left = 0; left < newMapps.size() - 1; left++){
		VariablesConstraintsMap leftMap = newMapps.get(left);

		for (int right = left + 1; right < newMapps.size(); right++){
		    VariablesConstraintsMap rightMap = newMapps.get(right);

		    if ( intersects(leftMap.variables, rightMap.variables) ){
			leftMap.variables.addAll(rightMap.variables);
			leftMap.constraints.addAll(rightMap.constraints);
			newMapps.remove(right);
			right = left;
		    }
		}
	    }

	    ConjunctiveConstraintSystem result = new ConjunctiveConstraintSystem();
	    result.constraintMap = newMapps;
	    return result;
	}


	/**
	 * Constructor for an empty system.
	 */
	protected ConjunctiveConstraintSystem() {
	    constraintMap = new Vector<VariablesConstraintsMap>();
	}

	/**
	 * Constructor for one constraint.
	 * Remember: {@link ConstraintSystem#FALSESYSTEM ConstraintSystem.FALSESYSTEM} has to be used for a
	 * contradictory system.
	 */
	protected ConjunctiveConstraintSystem(SingleConstraint constraint) {
	    if (constraint.equals(BooleanConstant.FALSE))
		throw new InternalError("a ConjunctiveConstraintSystem object should never become contradictory. Please use FalseConstraintSystem instead!");

	    constraintMap = new Vector<VariablesConstraintsMap>();
	    VariablesConstraintsMap map = new VariablesConstraintsMap();
	    map.variables = new HashSet<Variable>();
	    constraint.collectVariables(map.variables);
	    map.constraints = new SingleConstraintSet(constraint);
	    constraintMap.add(map);
	}

	@Override
	public void collectVariables(Set<Variable> set){
	    for (int i = 0; i < constraintMap.size(); i++)
		set.addAll(constraintMap.get(i).variables);
	}

	@Override
	public boolean containsVariable(Variable variable){
	    for (int i = 0; i < constraintMap.size(); i++)
		if (constraintMap.get(i).variables.contains(variable))
		    return true;
	    return false;
	}

	@Override
	public SingleConstraintSet getConstraintSet(int idx){
	    return constraintMap.get(idx).constraints;
	}

	@Override
	public int getConstraintSetCount(){
	    return constraintMap.size();
	}

	@Override
	public boolean isContradictory(){
	    return false;
	}

	@Override
	public String toString(){
	    StringBuffer out = new StringBuffer();
	    if (constraintMap.size() > 1)
		out.append("(");
	    for (int i = 0; i < constraintMap.size(); i++){
		out.append(this.getConstraintSet(i).toString());
		if (i < constraintMap.size()-1)
		    out.append(" & ");
	    }
	    if (constraintMap.size() > 1)
		out.append(")");
	    return out.toString();
	}

	@Override
	public ConstraintSystem transformConstraintSets(ConstraintSetTransformer transformer){
	    Vector<SingleConstraint> newConstraints = new Vector<SingleConstraint>();
	    for (int i = 0; i < constraintMap.size(); i++){
		VariablesConstraintsMap map = constraintMap.get(i);
		SingleConstraintSet set = transformer.transform(map.constraints);
		for (int j = 0; j < set.getConstraintCount(); j++)
		    newConstraints.add(set.getConstraint(j));
	    }
	    return getConstraintSystem(newConstraints);
	}

	static class VariablesConstraintsMap{
	    public SingleConstraintSet constraints;
	    public Set<Variable> variables;
	}

    }

}
