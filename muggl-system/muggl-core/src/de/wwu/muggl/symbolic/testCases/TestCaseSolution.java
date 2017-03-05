package de.wwu.muggl.symbolic.testCases;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.db.solution.PostExecutionDatabaseSolution;
import de.wwu.muggl.db.solution.PreExecutionDatabaseSolution;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
//import de.wwu.muggl.vm.impl.jpa.db.NewVirtualDatabase;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.ObjectComparator;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.strings.StringCache;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.ObjectConstant;
//import de.wwu.muggl.solvers.expressions.ReferenceVariable;
//import de.wwu.muggl.solvers.expressions.StringVariable;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.generating.obj.ObjectConstantGenerationException;
import de.wwu.muggl.symbolic.generating.obj.ObjectConstantGenerator;

/**
 * This class represents a solution found by the symbolic virtual machine. It holds
 * references to the method initially executed and to the ArrayList of equations that
 * characterize the solution. It has the capacity for the parameter set deduced from
 * the solution and the expected return value for these parameters.<br />
 * <br />
 * Solutions can be linked in a list. To achieve this, each instance has a pointer to
 * its predecessor and its successor. These information can be used to easily delete
 * redundant (i.e. identical) solutions from the set of solutions.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class TestCaseSolution {
	// Fields for the solution related data.
	private Method initialMethod;
	private Solution solution;
	private Object returnValue;
	private boolean throwsAnUncaughtException;
	private Object[] variables;
	private String solutionComment;
	private int parametersUnused;
	private boolean[] dUCoverage;
	private Map<Method, boolean[]> cFCoverageMapping;

	// Fields for chaining the solutions.
	private TestCaseSolution predecessor;
	private TestCaseSolution successor;
	
	// virtual database state
	@Deprecated
	protected PreExecutionDatabaseSolution preExecutionRequiredSolution;
	@Deprecated
	protected PostExecutionDatabaseSolution postExecutionRequiredSolution;
	
	protected StringBuilder requiredDBDataStringBuilder;
	protected StringBuilder postDBStateCheckStringBuilder;
//	protected NewVirtualDatabase vdb;
//	protected VirtualDatabase database;
	
	protected StringBuilder methodParameterStringBuilder;
	
	protected ObjectConstantGenerator objectGenerator;

	/**
	 * Initialize the TestCaseSolution.
	 * @param initialMethod The method execution started with.
	 * @param solution The Solution this TestCaseSolution represents.
	 * @param returnValue The expected return value for the set of parameters.
	 * @param throwsAnUncaughtException Indicates whether the returnValue is an actual return value or an NoExceptionHandlerFoundException.
	 * @param variables The variables generated for the method when generating this solution.
	 * @param dUCoverage The mapping of methods and arrays of boolean values indicating which def-use chains have been covered.
	 * @param cFCoverageMapping The mapping of methods and arrays of boolean values indicating which control graph edges have been covered.
	 */
	public TestCaseSolution(
			Method initialMethod, Solution solution,
			Object returnValue, boolean throwsAnUncaughtException,
			StringBuilder requiredDBDataStringBuilder,
			StringBuilder postDBStateCheckStringBuilder,
			PreExecutionDatabaseSolution preExecutionRequiredSolution,
			PostExecutionDatabaseSolution postExecutionRequiredSolution,
			Object[] variables, boolean[] dUCoverage,
			Map<Method, boolean[]> cFCoverageMapping
			) {
		this.initialMethod = initialMethod;
		this.solution = solution;
		this.returnValue = returnValue;
		this.throwsAnUncaughtException = throwsAnUncaughtException;
		this.variables = variables;
		this.dUCoverage = dUCoverage;
		this.cFCoverageMapping = cFCoverageMapping;
		this.solutionComment = null;
		this.predecessor = null;
		this.successor = null;
		this.requiredDBDataStringBuilder = requiredDBDataStringBuilder;
		this.postDBStateCheckStringBuilder = postDBStateCheckStringBuilder;
		
		this.preExecutionRequiredSolution = preExecutionRequiredSolution;
		this.postExecutionRequiredSolution = postExecutionRequiredSolution;
//		this.vdb = virtualDatabase;
//		this.database = new VirtualDatabase();
		
		this.objectGenerator = new ObjectConstantGenerator();
		
		checkSolution();
	}

	/**
	 * Initialize the Solution and set its predecessor.
	 * @param initialMethod The method execution started with.
	 * @param solution The Solution this TestCaseSolution represents.
	 * @param returnValue The expected return value for the set of parameters.
	 * @param throwsAnUncaughtException Indicates whether the returnValue is an actual return value or an NoExceptionHandlerFoundException.
	 * @param variables The variables generated for the method when generating this solution.
	 * @param dUCoverage The mapping of methods and arrays of boolean values indicating which def-use chains have been covered.
	 * @param cFCoverageMapping The mapping of methods and arrays of boolean values indicating which control graph edges have been covered.
	 * @param predecessor The preceding solution in the list of solutions.
	 * @throws NullPointerException If predecessor is null.
	 */
	public TestCaseSolution(
			Method initialMethod, Solution solution,
			Object returnValue, boolean throwsAnUncaughtException,
			StringBuilder requiredDBDataStringBuilder,
			StringBuilder postDBStateCheckStringBuilder,
			PreExecutionDatabaseSolution preExecutionRequiredSolution,
			PostExecutionDatabaseSolution postExecutionRequiredSolution,
			Object[] variables,  boolean[] dUCoverage,
			Map<Method, boolean[]> cFCoverageMapping,
			TestCaseSolution predecessor
			) {
		this(initialMethod, solution, returnValue, throwsAnUncaughtException, 
				requiredDBDataStringBuilder, postDBStateCheckStringBuilder, preExecutionRequiredSolution, postExecutionRequiredSolution, 
				variables,dUCoverage, cFCoverageMapping);
		if (predecessor == null)
			throw new NullPointerException();
		this.predecessor = predecessor;
		predecessor.setSuccessor(this);
	}

	/**
	 * Getter for the method execution started with.
	 * @return The method execution started with.
	 */
	public Method getInitialMethod() {
		return this.initialMethod;
	}

	/**
	 * Getter for the ArrayList of equations.
	 * @return The ArrayList of equations.
	 */
	public Solution getSolution() {
		return this.solution;
	}

	/**
	 * Setter for the ArrayList of equations.
	 * @param solution The Solution this TestCaseSolution represents.
	 */
	public void setSolution(Solution solution) {
		this.solution = solution;
	}

	/**
	 * Getter for the expected return value for the set of parameters.
	 * @return The return value.
	 */
	public Object getReturnValue() {
		return this.returnValue;
	}

	/**
	 * Getter for the information whether the return value can be treated
	 * as the actual return value of the method's execution (false), or if
	 * execution the method with the given parameters will result in an
	 * uncaught Exception beeing thrown (true).
	 * @return true, if the method throws an uncaught exception, false otherwise.
	 */
	public boolean getThrowsAnUncaughtException() {
		return this.throwsAnUncaughtException;
	}

	/**
	 * Get the NoExceptionHandlerFoundException object containing data
	 * about the uncaught exception.
	 * @return The NoExceptionHandlerFoundException object, or null, should the method end correctly with the given set of parameters.
	 */
	public NoExceptionHandlerFoundException getUncaughtExceptionData() {
		if (this.throwsAnUncaughtException)
			return (NoExceptionHandlerFoundException) this.returnValue;
		return null;
	}

	/**
	 * Getter for the expected return value for the set of parameters.
	 *
	 * @return The evaluated return value.
	 */
	public Object getEvaluatedReturnValue() {
		if (this.returnValue instanceof Term) {
			// If it is a term, evaluate it.
			this.returnValue = ((Term) this.returnValue).insert(this.solution, false);
		}
		return this.returnValue;
	}

	/**
	 * Getter for the predecessing solution.
	 * @return The predecessing solution.
	 */
	public TestCaseSolution getPredecessor() {
		return this.predecessor;
	}

	/**
	 * Setter for the predecessing solution.
	 * @param predecessor The predecessing solution.
	 */
	public void setPredecessor(TestCaseSolution predecessor) {
		this.predecessor = predecessor;
	}

	/**
	 * Getter for the successing solution.
	 * @return The successing solution.
	 */
	public TestCaseSolution getSuccessor() {
		return this.successor;
	}

	/**
	 * Setter for the successing solution.
	 * @param successor The successing solution.
	 */
	public void setSuccessor(TestCaseSolution successor) {
		this.successor = successor;
	}

	/**
	 * Find out if this solution has a successor.
	 * @return true if this solution has a successor, false otherwise.
	 */
	public boolean hasSuccessor() {
		return this.successor != null;
	}
	
	public StringBuilder getRequiredDBDataStringBuilder() {
		return this.requiredDBDataStringBuilder;
	}
	
	public StringBuilder getPostDBStateCheckStringBuilder() {
		return this.postDBStateCheckStringBuilder;
	}
	
	public PreExecutionDatabaseSolution getPreExecutionDatabaseSolution() {
		return this.preExecutionRequiredSolution;
	}
	
	public PostExecutionDatabaseSolution getPostExecutionDatabaseSolution() {
		return this.postExecutionRequiredSolution;
	}
	
//	public NewVirtualDatabase getVirtualDatabase() {
//		return this.vdb;
//	}

	/**
	 * Get the total number of solutions.  This method can be called from any solution as it
	 * will always move to the first solution in the list and start counting from that point on.
	 * @return The number of solutions
	 */
	public long getNumberOfSolutions() {
		long numberOfSolutions = 1;
		TestCaseSolution currentSolution = this;
		// First move to the first solution in the solutions' list.
		while (currentSolution.getPredecessor() != null) {
			currentSolution = currentSolution.getPredecessor();
		}

		// Move through the whole list.
		while (currentSolution.getSuccessor() != null) {
			// Count up.
			numberOfSolutions++;
			// Get the successor.
			currentSolution = currentSolution.getSuccessor();
		}

		// Finish.
		return numberOfSolutions;
	}

	/**
	 * Delete redundancy by removing identical solutions from the list of solutions. This method
	 * can be called from any solution as it will always move to the first solution in the list
	 * and start comparison from that point on, moving towards the end of the list and removing
	 * any identical solutions on its way.
	 *
	 * This method will occasionally check the SolutionProcessor for its interruption status.
	 * Should it have been interrupted, processing will stop and a InterruptedException will be
	 * thrown.
	 *
	 * @param solutionProcessor The SolutionProcessor requesting the deletion of redundancy.
	 * @throws InterruptedException If the solution processor has received the command to interrupt.
	 * @throws NullPointerException If the parameter is null.
	 */
	public void deleteRedundancy(SolutionProcessor solutionProcessor) throws InterruptedException {
		if (solutionProcessor == null)
			throw new NullPointerException("The parameter must not be null.");

		TestCaseSolution currentSolution = this;
		// First move to the first solution in the solutions' list.
		while (currentSolution.getPredecessor() != null) {
			currentSolution = currentSolution.getPredecessor();
		}

		// Move through the whole list.
		long counter = 0L;
		while (currentSolution.getSuccessor() != null) {
			// Set the number for statistical reasons.
			solutionProcessor.setDeletingRedudancySolutionCurrentlyProcessed(counter);

			// Compare to any solution further in the list of solutions.
			TestCaseSolution aSuccessingSolution = currentSolution.getSuccessor();
			while (aSuccessingSolution != null) {
				// Check for equality.
				if (currentSolution.isEqualTo(aSuccessingSolution)) {
					// Remove aSuccessingSolution from the list by removing the linking of it.
					TestCaseSolution aSuccessingSolutionSuccessor = aSuccessingSolution.getSuccessor();
					TestCaseSolution aSuccessingSolutionPredecessor = aSuccessingSolution.getPredecessor();
					aSuccessingSolutionSuccessor.setPredecessor(aSuccessingSolutionPredecessor);
					aSuccessingSolutionPredecessor.setSuccessor(aSuccessingSolutionSuccessor);
					// For faster garbage collection remove further linking information from aSuccessingSolution.
					aSuccessingSolution.setSuccessor(null);
					aSuccessingSolution.setPredecessor(null);
					// Set the successor as aSuccessingSolution.
					aSuccessingSolution = aSuccessingSolutionSuccessor;
				} else {
					// Get the next solution in the list of solutions.
					aSuccessingSolution = aSuccessingSolution.getSuccessor();
				}
			}

			// Move one solution further.
			currentSolution = currentSolution.getSuccessor();
			counter++;

			// Check if test case generation has been interrupted.
			if (solutionProcessor.isInterrupted())
				throw new InterruptedException("Interrupted");
		}
	}

	/**
	 * Compare this Solution to the one supplied.
	 * @param solution The Solution to compare this one to.
	 * @return true, if the solutions are equal, false otherwise.
	 */
	private boolean isEqualTo(TestCaseSolution solution) {
		// Solutions must have the same initial method.
		if (this.initialMethod != solution.initialMethod) {
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN)) Globals.getInst().symbolicExecLogger.warn("Unexpected problem when comparing two solutions to delete redundancy: The solutions have different initial methods. This problem will be ignored, generated test cases might however be incompleted or even incorrect.");
			return false;
		}

		// Compare the solutions.
		if (!this.solution.equals(solution))
			return false;

		/* TODO: Can this be dropped?
		// Compare the parameters.
		Set<Variable> variables1 = this.solution.variables();
		Set<Variable> variables2 = solution.solution.variables();
		if (variables1.size() != variables2.size()) return false;
		for (Variable variable : variables1)
		{
			if (!variables2.contains(variable)) return false;
			if (variable.isBoolean()) {
				if (this.solution.getBooleanValue(variable).getValue() != solution.solution.getBooleanValue(variable).getValue()) return false;
			} else if (variable.isConstant()) {
				NumericConstant numericValue = this.solution.getNumericValue(variable);
				if (numericValue.getType() == SymbolicVirtualMachineElement.DOUBLE) {
					if (numericValue.getDoubleValue() != solution.solution.getNumericValue(variable).getDoubleValue()) return false;
				} else if (numericValue.getType() == SymbolicVirtualMachineElement.FLOAT) {
					if (numericValue.getFloatValue() != solution.solution.getNumericValue(variable).getFloatValue()) return false;
				} else if (numericValue.getType() == SymbolicVirtualMachineElement.LONG) {
					if (numericValue.getLongValue() != solution.solution.getNumericValue(variable).getLongValue()) return false;
				} else {
					if (numericValue.getIntValue() != solution.solution.getNumericValue(variable).getIntValue()) return false;
				}
			} else {
				return false;
			}
		} */

		// Compare the return value. If this point has been reached, it should be equal as well.
		if (!ObjectComparator.compareObjects(this.returnValue, solution.returnValue)) {
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN)) Globals.getInst().symbolicExecLogger.warn("Unexpected problem when comparing two solutions to delete redundancy: The solutions are equal but for their return value. Solutions with equal parameters should have the same return value. This problem will be ignored, generated test cases might however be incompleted or even incorrect.");
			return false;
		}
		// Everything is equal!
		return true;
	}

	public Object[] getParameters() {
		return getParameters(null);
	}
	
	/**
	 * Return an array of Constant object reflecting the actual values to be inserted as the method's parameters for
	 * this solution. Parameters not set will be initialized to the default value of the type. This is the appropriate
	 * representation of zero for simple types, and null for reference types.
	 * @return A Array of Constant or Constant arrays object with the parameters for the Method.
	 */
	public Object[] getParameters(StringCache stringCache) {
		Object[] parameters = new Object[this.variables.length];
		this.parametersUnused = 0;

		// Skip the first value at instance methods.
		int startAt = 1;
		if (this.initialMethod.isAccStatic()) {
			startAt = 0;
		}

		
		// Process the variables.
		for (int a = startAt; a < this.variables.length; a++) {
			if (this.variables[a] instanceof Arrayref) {
				// Process the array.
				parameters[a] = convertArrayrefIntoConstantArray((Arrayref) this.variables[a], a);
			} else if(this.variables[a] instanceof ReferenceVariable) {
				System.out.println("\n\n**** ADD A REFERENCE VARIABLE AS PARAMETER: " + this.variables[a] + " ***\n");
				parameters[a] = (ReferenceVariable)this.variables[a];//generateReferenceObject((ReferenceVariable)this.variables[a]);
			} else { // TODO: Object references?
				int[] index = {a};
				parameters[a] = provideConstantForVariable((Term) this.variables[a], index, false);
			}
		}

		// Close the comment?
		if (this.solutionComment != null) {
			if (this.parametersUnused == 1)  {
				this.solutionComment += " is unused";
			} else {
				int lastComma = this.solutionComment.lastIndexOf(",");
				this.solutionComment = this.solutionComment.substring(0, lastComma) + " and" + this.solutionComment.substring(lastComma + 1);
				this.solutionComment += " are unused";
			}
			this.solutionComment += ").";
		}

		// Return the value.
		return parameters;
	}

	
	private ObjectConstant generateReferenceObject(ReferenceVariable referenceVariable) {
		try {
			return objectGenerator.generateNewObjectConstant(referenceVariable);
		} catch (ObjectConstantGenerationException e) {
			throw new RuntimeException("Could not generate object constant for reference variable: " + referenceVariable, e);
		}
	}

	/**
	 * Provide a constant value for a term, or null, should this not be possible.
	 *
	 * @param term The term to provide a constant value for.
	 * @param index The index of the parameter in the method. If the array has more than one
	 *        dimension, the dimensions describe the terms position within an array.
	 * @param isReturnValue Indicates that the variable is not a parameter but a return value
	 * @return The Constant, or null.
	 */
	private Constant provideConstantForVariable(Term term, int[] index, boolean isReturnValue) {
		boolean parameterNotNeeded = false;
		if (term != null) {
			if (term instanceof Variable) {
				// Get the value.
				Constant constant = this.solution.getValue((Variable) term);
				if (constant == null)  {
					parameterNotNeeded = true;
				} else {
					return constant;
				}
			} else {
				term = term.insert(this.solution, true); // TODO does this work as expected or does it leave out possible cases?
				if (term.isConstant()) {
					return (Constant) term;
				}
				parameterNotNeeded = true;
			}
		}

		// Is the parameter not needed? Add this as a comment then.
		if (parameterNotNeeded && !isReturnValue) {
			this.parametersUnused++;
			if (this.solutionComment == null) {
				this.solutionComment = "In this solution, not all parameters are used (";
			} else {
				this.solutionComment += ", ";
			}
			if (index.length == 1) {
				this.solutionComment += this.initialMethod.getParameterTypeAtIndex(index[0]) + " " + this.initialMethod.getParameterName(index[0]);
			} else {
				this.solutionComment += this.initialMethod.getParameterTypeAtIndex(index[0]).replace("[]", "") + " " + this.initialMethod.getParameterName(index[0]);
				for (int a = 1; a < index.length; a++) {
					this.solutionComment += "[" + index[a] + "]";
				}
			}
		}

		// If the value is null, get a default value for it if the corresponding type is primitive.
		String type;
		if (isReturnValue) {
			type = this.initialMethod.getReturnType();
		} else {
			type = this.initialMethod.getParameterTypeAtIndex(index[0]);
		}
		if (index.length > 1 || isReturnValue) {
			while (type.endsWith("[]")) {
				type = type.substring(0, type.length() - 2);
			}
		}

		if (type.equals("boolean")) {
			return BooleanConstant.getInstance(false);
		} else if (type.equals("byte") || type.equals("char") || type.equals("int") || type.equals("short")) {
			return  IntConstant.getInstance(0);
		} else if (type.equals("double")) {
			return  DoubleConstant.getInstance(0D);
		} else if (type.equals("float")) {
			return  FloatConstant.getInstance(0F);
		} else if (type.equals("long")) {
			return LongConstant.getInstance(0L);
		} else {
			return null;
		}
	}

	/**
	 * Convert an array reference into an array of Constant which can be used to generate the test cases file.
	 *
	 * @param arrayref The array reference to be converted.
	 * @param index The index of the array in the methods parameters, or -1 to to indicate the variable is not a parameter
	 * @return An array of type Constant. it can be multidimensional, hence the return type is just Object.
	 */
	public Object convertArrayrefIntoConstantArray(Arrayref arrayref, int index) {
		// First gather information about the dimensions.
		int[] dimensions = new int[1];
		dimensions[0] = arrayref.length;

		// Work trough the dimensions.
		Arrayref currentDimension = arrayref;
		if (dimensions[dimensions.length - 1] > 0 && currentDimension.getReferenceValue().isArray()) {
			int[] dimensionsNew = new int[dimensions.length + 1];
			for (int a = 0; a < dimensions.length; a++) {
				dimensionsNew[a] = dimensions[a];
			}
			currentDimension = (Arrayref) currentDimension.getElement(0);
			dimensionsNew[dimensions.length] = currentDimension.length;
			dimensions = dimensionsNew;
		}

		// Generate the new array.
		Object constants = Array.newInstance(Constant.class, dimensions);

		// Generate a new dimensions array that takes one more entry.
		int[] dimensionsNew = new int[dimensions.length + 1];
		for (int a = 1; a < dimensionsNew.length; a++) {
			// Set all values to -1, signaling that this dimension has not been processed, yet.
			dimensionsNew[a] = -1;
		}
		// The first position takes the array's index in the method's parameters.
		dimensionsNew[0] = index;

		// Now process the arrayref and the array simultaneously and insert the values.
		insertValuesFromArrayref((Constant[]) constants, arrayref, 1, dimensionsNew);

		// Return the constants.
		return constants;
	}

	/**
	 * Recursively process an array reference and convert the variables found there into constants.
	 * @param constants The destination array for constant values.
	 * @param arrayref The source array reference.
	 * @param dimension The currently processed dimension.
	 * @param dimensions An array describing the way trough the array's dimensions.
	 */
	private void insertValuesFromArrayref(Constant[] constants, Arrayref arrayref, int dimension, int[] dimensions) {
		// Is there a deeper dimension?
		if (dimension < dimensions.length - 1) {
			// Process any entry recursively.
			for (int a = 0; a < constants.length; a++) {
				dimensions[dimension] = a;
				Object constantObject = constants[a];
				insertValuesFromArrayref((Constant[]) constantObject, (Arrayref) arrayref.getElement(a), dimension + 1, dimensions);
			}
		} else {
			for (int a = 0; a < constants.length; a++) {
				dimensions[dimension] = a;
				boolean isReturnValue = false;
				if (dimensions[0] == -1)
					isReturnValue = true;
				constants[a] = provideConstantForVariable((Term) arrayref.getElement(a), dimensions, isReturnValue);
			}
		}
 	}


	/**
	 * Getter for the comment on the solution.
	 *
	 * The comment states additional information that can be included as a comment to the generated TestCase file.
	 * If there is no comment, null will be returned.
	 *
	 * @return A comment on this solution, if there is any, null otherwise.
	 */
	public String getSolutionComment() {
		return this.solutionComment;
	}

	/**
	 * Check if the solution does not contain any bindings. If it
	 * does not, there most likely have been no constraints that
	 * would characterize it. So zero values are added.
	 */
	private void checkSolution() {
		// Check if the solution does not contain any bindings.
		if (this.solution.toString().equals("{}")) {
			if (Globals.getInst().symbolicExecLogger.isDebugEnabled()) Globals.getInst().symbolicExecLogger.debug("A solution was found that is valid but empty. Zero values will by assigned.");
			for (Object variable : this.variables) {
				if (variable != null && variable instanceof Variable) {
					int type = ((Variable) variable).getType();
					switch (type) {
						case Expression.BOOLEAN:
							this.solution.addBinding((Variable) variable, BooleanConstant.getInstance(false));
							break;
						case Expression.BYTE:
							this.solution.addBinding((Variable) variable, IntConstant.getInstance(0));
							break;
						case Expression.CHAR:
							this.solution.addBinding((Variable) variable, IntConstant.getInstance(0));
							break;
						case Expression.DOUBLE:
							this.solution.addBinding((Variable) variable, DoubleConstant.getInstance(0.0));
							break;
						case Expression.FLOAT:
							this.solution.addBinding((Variable) variable, FloatConstant.getInstance(0.0F));
							break;
						case Expression.INT:
							this.solution.addBinding((Variable) variable, IntConstant.getInstance(1));
							break;
						case Expression.LONG:
							this.solution.addBinding((Variable) variable, LongConstant.getInstance(0L));
							break;
						case Expression.SHORT:
							this.solution.addBinding((Variable) variable, IntConstant.getInstance(0));
							break;
						default:
							if (Globals.getInst().symbolicExecLogger.isDebugEnabled()) Globals.getInst().symbolicExecLogger.debug("Encountered an unknown type when filling up a solution with zero values.");
							break;
					}
				}
				// TODO: Process Arrayrefs and bind variables contained?
			}
		}
	}

	/**
	 * Getter for the def-use coverage.
	 *
	 * @return The def-use coverage as an array of boolean values indicating
	 *         which def-use chains have been covered.
	 */
	public boolean[] getDUCoverage() {
		return this.dUCoverage;
	}

	/**
	 * Getter for the def-use coverage mapping.
	 *
	 * @return The mapping of methods and control flow coverage as an array of boolean values
	 *         indicating which control graph edges have been covered.
	 */
	public Map<Method, boolean[]> getCFCoverageMap() {
		return this.cFCoverageMapping;
	}

	/**
	 * Get a String representation of this TestCaseSolution.
	 * @return A String representation of this object.
	 */
	@Override
	public String toString() {
		String toString = "Solution for " + this.initialMethod.getFullNameWithParameterTypesAndNames() + "\n\n"
			+ "Parameters:\n";
		Object[] parameters = getParameters();
		for (int a = 0; a < parameters.length; a++) {
			if (a > 0) toString += ", ";
			if (parameters[a] != null) {
				toString += parameters[a].toString();
			} else {
				toString += "null";
			}
		}
		toString += "\n";
		if (this.throwsAnUncaughtException) {
			Throwable t = (Throwable) this.returnValue;
			toString += "Execution throws an uncaught exception: " + t.getClass().getName() + " (" + t.getMessage() + ")";
		} else {
			toString += "Return value: ";
			if (this.returnValue != null) {
				toString += this.returnValue.getClass().getName() + " with toString(): " + this.returnValue.toString();
			} else {
				toString += " a null reference";
			}
		}
		boolean[] defUseCoverage = this.dUCoverage;
		toString += "\n\nDef-use chain coverages: (" + defUseCoverage.length + " chains):\n";
		for (int a = 0; a < defUseCoverage.length; a++) {
			toString += a + ":";
			if (defUseCoverage[a]) {
				toString += "covered";
			} else {
				toString += "uncovered";
			}
			toString += "\n";
		}
		toString += "\nControl graph edge coverages:\n";
		Set<Method> controlFlowCoverageSet = this.cFCoverageMapping.keySet();
		for (Method method : controlFlowCoverageSet) {
			boolean[] controlFlowCoverage = this.cFCoverageMapping.get(method);
			toString += "Method " + method.getName() + " (" + controlFlowCoverage.length + " edges):\n";
			for (int a = 0; a < controlFlowCoverage.length; a++) {
				if (controlFlowCoverage[a]) {
					toString += "covered";
				} else {
					toString += "uncovered";
				}
				toString += "\n";
			}
		}

		return toString;
	}

	public void addMethodArgumentStringBuilder(StringBuilder methodParameterStringBuilder) {
		this.methodParameterStringBuilder = methodParameterStringBuilder;
	}
	
	public StringBuilder getMethodParameterStringBuilder() {
		return this.methodParameterStringBuilder;
	}

}
