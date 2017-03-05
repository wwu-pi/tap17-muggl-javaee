package de.wwu.muggl.symbolic.testCases;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Level;

import de.wwu.muggl.common.TimeSupport;
import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.db.solution.PostExecutionDatabaseSolution;
import de.wwu.muggl.db.solution.PreExecutionDatabaseSolution;
import de.wwu.muggl.solvers.Solution;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;
import de.wwu.muggl.solvers.expressions.ObjectConstant;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.testCases.jpa.ExpectedDatabasePostExecutionGenerator;
import de.wwu.muggl.symbolic.testCases.jpa.JPAPreExecutionGenerator;
import de.wwu.muggl.symbolic.testCases.jpa.ReferenceObjectTextGenerator;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.execution.ExecutionAlgorithms;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.ModifieableArrayref;
import de.wwu.muggl.vm.initialization.ObjectNullRef;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.vm.support.CheckingArrayList;
import de.wwu.muggl.vm.var.ReferenceVariable;

/**
 * The SolutionProcessor stores information about the solutions found during the symbolic execution
 * of a program. It provides access to these information. Furthermore, if offers the method
 * generateTestCases() which can be uses to process the solutions found in order to generate test
 * cases from them. If this operation is successful, the test cases are written to a file that be
 * run with JUnit.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-15
 */
public class SolutionProcessor {
	// Internal fields.
	private boolean testCaseGenerationStarted;
	private boolean testCaseGenerationFinished;
	private boolean testCaseGenerationAborted;
	private boolean testCaseGenerationFailed;
	private Throwable causeOfFailure;
	private boolean interrupted;

	// References to solution data.
	private SymbolicVirtualMachine vm;
	private MugglClassLoader classLoader;
	private Method initialMethod;
	private boolean foundSolution;
	private int newestSolutionNumber;
	private boolean doNotSaveTheNextSolution;
	private TestCaseSolution firstSolutionFound;
	private TestCaseSolution latestSolutionFound;

	// References to earlier found solutions data.
	private TestCaseSolution firstSolutionFoundEalier;

	// Field for status and information purposes.
	private boolean finishedDeletingRedudancy;
	private boolean finishedElimination;
	private long numberOfSolutionsWithRedundancy;
	private long numberOfSolutionsBeforeElimination;
	private long numberOfTestCases;
	private String generatedClassFilePath;
	private volatile long deletingRedudancySolutionCurrentlyProcessed;
	private volatile long eliminationSolutionsKept;
	private volatile long eliminationSolutionsDropped;

	/**
	 * The constructor initializes the SolutionProcessor with the most important data
	 * and sets its private fields to initial values.
	 * @param vm The SymbolicalVirtualMachine that is generating solutions.
	 * @param classLoader The main ClassLoader to use.
	 * @param initialMethod The Method to start execution with.
	 * @throws NullPointerException If any of the arguments is null.
	 */
	public SolutionProcessor(
			SymbolicVirtualMachine vm,
			MugglClassLoader classLoader,
			Method initialMethod
			) {
		// Check for null pointers.
		if (vm == null || classLoader == null || initialMethod == null)
			throw new NullPointerException("None of the arguments must be null.");

		// Set the fields.
		this.vm = vm;
		this.classLoader = classLoader;
		this.initialMethod = initialMethod;

		// Initialize other values.
		this.testCaseGenerationStarted = false;
		this.testCaseGenerationFinished = false;
		this.testCaseGenerationAborted = false;
		this.testCaseGenerationFailed = false;
		this.interrupted = false;
		this.firstSolutionFound = null;
		this.latestSolutionFound = null;
		this.foundSolution = false;
		this.newestSolutionNumber = -1;
		this.doNotSaveTheNextSolution = false;
		this.finishedDeletingRedudancy = false;
		this.finishedElimination = false;
		this.numberOfSolutionsWithRedundancy = -1;
		this.numberOfSolutionsBeforeElimination = -1;
		this.numberOfTestCases = -1;
		this.deletingRedudancySolutionCurrentlyProcessed = -1L;
		this.eliminationSolutionsKept = -1L;
		this.eliminationSolutionsDropped = -1L;
	}

	/**
	 * Add solutions found in an earlier execution. Redundancies will be removed from them.
	 *
	 * @param firstSolutionFoundEalier The first solution found earlier. (TestCaseSolution objects
	 *        are chained, so only this reference is needed.)
	 */
	public void addFirstSolutionFoundEalier(TestCaseSolution firstSolutionFoundEalier) {
		this.firstSolutionFoundEalier = firstSolutionFoundEalier;
		if (firstSolutionFoundEalier != null) {
			try {
				// TODO: is this really a good idea?
				firstSolutionFoundEalier.deleteRedundancy(this);
			} catch (InterruptedException e) {
				/*
				 * This cannot happen at this point. Interruption is only possible if the test case
				 * processing started.
				 */
			}
		}
	}

	/**
	 * Add a new Solution to the List of solutions and add a corresponding return value Object to
	 * the list of return values.
	 *
	 * @param solution The Solution to add.
	 * @param returnValue The new return value.
	 * @param throwsAnUncaughtException Indicates whether the returnValue is an actual return value
	 *        or an NoExceptionHandlerFoundException.
	 * @param controlFlowCoverageMapping The mapping of methods and arrays of boolean values
	 *        indicating which control graph edges have been covered.
	 * @param dUCoverage An array of boolean values indicating which def-use chains have
	 *        been covered.
	 * @throws IllegalStateException If the method is invoked after test generation was started.
	 */
	public void addSolution(Solution solution, Object returnValue, 
			StringBuilder requiredDBDataStringBuilder,
			StringBuilder postDBStateCheckStringBuilder,
			PreExecutionDatabaseSolution preExecutionRequiredSolution,
			PostExecutionDatabaseSolution postExecutionRequiredSolution,
			boolean throwsAnUncaughtException, Map<Method, boolean[]> controlFlowCoverageMapping,
			boolean[] dUCoverage
		) {
		if (this.testCaseGenerationStarted) {
			throw new IllegalStateException("Test generation has started already. Cannot add any more solutions.");
		}

		// Clone the returnValue should it be a referenceValue.
		if (returnValue instanceof ReferenceValue) {
			if (returnValue instanceof Objectref) {
				// TODO
			} else if (returnValue instanceof Arrayref) {
				returnValue = ((Arrayref) returnValue).clone();
			}
		}

		// Add the solution.
		if (this.latestSolutionFound == null) {
			this.latestSolutionFound = new TestCaseSolution(
				this.initialMethod, solution, returnValue,  throwsAnUncaughtException, 
				requiredDBDataStringBuilder, postDBStateCheckStringBuilder,
				preExecutionRequiredSolution, 
				postExecutionRequiredSolution, 
				prepareVariablesForTestCaseSolution(),
				prepareDUCoverageForTestCaseSolution(dUCoverage), prepareControlFlowCoverageForTestCaseSolution(controlFlowCoverageMapping)
				);
		} else {
			this.latestSolutionFound = new TestCaseSolution(
					this.initialMethod, solution, returnValue, throwsAnUncaughtException, 
					requiredDBDataStringBuilder, postDBStateCheckStringBuilder,
					preExecutionRequiredSolution, 
					postExecutionRequiredSolution, 
					prepareVariablesForTestCaseSolution(),
					prepareDUCoverageForTestCaseSolution(dUCoverage), prepareControlFlowCoverageForTestCaseSolution(controlFlowCoverageMapping),
					this.latestSolutionFound
					);
		}

		// Is it the first solution?
		if (this.firstSolutionFound == null) {
			this.firstSolutionFound = this.latestSolutionFound;
		}

		// Mark that there was a solution found.
		this.foundSolution = true;
		this.newestSolutionNumber++;
	}

	/**
	 * Prepare the def use chain coverages for a TestCaseSolution. Basically the array of boolean values will be cloned before being inserted to
	 * protect it from later modifications to the references. If a detailed logging level is
	 * set, some logging will also take place.
	 *
	 * @param dUCoverage The mapping of methods and arrays of boolean values indicating
	 *        which def-use chains have been covered.
	 * @return The cloned def-use coverage mapping.
	 */
	private boolean[] prepareDUCoverageForTestCaseSolution(boolean[] dUCoverage) {
		// Log the covered def-use chains for debug purposes.
		if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) {
			boolean firstOne = true;
			int covered = 0;
			String toLog = "A solution was found that covers the following def-use chains: ";
			for (int a = 0; a < dUCoverage.length; a++) {
				if (dUCoverage[a]) {
					if (!firstOne) {
						toLog += ", ";
					} else {
						firstOne = false;
					}
					toLog += "a";
					covered++;
				}
			}
			toLog += " (" + covered + " out of " + dUCoverage.length + " def-use chains in total.)";
			// Finish.
			Globals.getInst().symbolicExecLogger.trace(toLog);
		}

		// Clone the dUCoverage.
		boolean[] dUCoverageCloned = new boolean[dUCoverage.length];
		System.arraycopy(dUCoverage, 0, dUCoverageCloned, 0, dUCoverage.length);

		// Return.
		return dUCoverageCloned;
	}

	/**
	 * Prepare the control flow coverages for a TestCaseSolution. Basically a new HashMap will
	 * be instantiated and the arrays of boolean values will be cloned before being inserted
	 * to protect them from later modifications to the references. If a detailed logging
	 * level is set, some logging will also take place.
	 * @param controlFlowCoverageMapping The mapping of methods and arrays of boolean values indicating which control graph edges have been covered.
	 * @return The cloned control flow coverage mapping.
	 */
	private Map<Method, boolean[]> prepareControlFlowCoverageForTestCaseSolution(Map<Method, boolean[]> controlFlowCoverageMapping) {
		// Log the covered control graph edges for debug purposes.
		if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) {
			String toLog = "A solution was found that covers the following control graph edges: ";
			Set<Entry<Method, boolean[]>> controlFlowCoverageSet = controlFlowCoverageMapping.entrySet();
			// Iterate over all methods.
			for (Entry<Method, boolean[]> entry : controlFlowCoverageSet) {
				toLog += "\n\nMethod " + entry.getKey().getFullNameWithParameterTypesAndNames() + ":\n";
				boolean[] controlFlowCoverage = entry.getValue();
				boolean firstOne = true;
				int covered = 0;
				for (int a = 0; a < controlFlowCoverage.length; a++) {
					if (controlFlowCoverage[a]) {
						if (!firstOne) {
							toLog += ", ";
						} else {
							firstOne = false;
						}
						toLog += "a";
						covered++;
					}
				}
				toLog += " (" + covered + " out of " + controlFlowCoverage.length + " control graph edges in total.)";
			}
			// Finish.
			Globals.getInst().symbolicExecLogger.trace(toLog);
		}

		// Instantiate a new HashMap.
		HashMap<Method, boolean[]> newMap = new HashMap<Method, boolean[]>();
		Set<Method> controlFlowCoverageSet = controlFlowCoverageMapping.keySet();
		// Iterate over all methods.
		for (Method method : controlFlowCoverageSet) {
			boolean[] controlFlowCoverage = controlFlowCoverageMapping.get(method);

			// Clone the controlFlowCoverage.
			boolean[] controlFlowCoverageCloned = new boolean[controlFlowCoverage.length];
			for (int a = 0; a < controlFlowCoverage.length; a++) {
				controlFlowCoverageCloned[a] = controlFlowCoverage[a];
			}

			// Insert the cloned array.
			newMap.put(method, controlFlowCoverageCloned);
		}

		// Return.
		return newMap;
	}

	/**
	 * Prepare the variables for a TestCaseSolution. If it are array references, copy them, as the modification
	 * when execution further is undesired here.
	 * 
	 * @return A cloned array of objects representation the variables of the method.
	 */
	private Object[] prepareVariablesForTestCaseSolution() {
		Variable[] variables = this.initialMethod.getVariables();
		Object[] generatedValues = this.initialMethod.getGeneratedValues();
		Object[] variablesCloned = new Object[variables.length];
		for (int a = 0; a < variables.length; a++) {
			int index = this.initialMethod.getParameterIndexForLocalVariableIndex(a, true);
			if (variables[a] != null) {
				variablesCloned[a] = variables[a];
			} else if (generatedValues[index] != null) {
				if (generatedValues[index] instanceof Arrayref) {
					variablesCloned[a] = ((Arrayref) generatedValues[index]).clone();
				} else {
					variablesCloned[a] = generatedValues[index];
				}
			} else {
				variablesCloned[a] = null;
			}
		}

		// Return.
		return variablesCloned;
	}
	
	
	private String getMaxLengthString(String original, int maxLength) {
		String newString = original + " ";
		for(int i=0; i<maxLength; i++) {
			newString+=" ";
		}
		return newString.substring(0, maxLength);
	}
	
	private String getExtraLines(int length) {
		String lines = "";
		for(int i=0; i<length; i++) {
			lines+="-";
		}
		return lines;
	}

	/**
	 * Generate a java file containing a class with all test cases suggested by the symbolic execution.
	 * This method will only work once.
	 * @throws IllegalStateException If the method is invoked again after test generation was started.
	 */
	public synchronized void generateTestCases() {
		// This method must only be run a single time.
		if (this.testCaseGenerationStarted) {
			throw new IllegalStateException("Test generation has started already. Cannot start it a second time.");
		}
		this.testCaseGenerationStarted = true;

		try {
			// Are there any solutions?
			if (this.firstSolutionFoundEalier == null && this.latestSolutionFound == null)
				throw new SymbolicExecutionException("No solutions have been found.");

			// Are there both earlier found and current solutions?
			if (this.firstSolutionFoundEalier != null && this.latestSolutionFound != null) {
				// Merge solutions found earlier with the current solutions.
				TestCaseSolution currentSolution = this.firstSolutionFoundEalier;
				// Move to the last solution of the earlier found solutions.
				while (currentSolution.hasSuccessor()) {
					currentSolution = currentSolution.getSuccessor();
				}
				// Connect the chains of earlier found and current solutions.
				currentSolution.setSuccessor(this.firstSolutionFound);
				this.firstSolutionFound.setPredecessor(currentSolution);
				// Set the now completed chain as the only chain.
				this.firstSolutionFound = this.firstSolutionFoundEalier;
				this.firstSolutionFoundEalier = null;
			} else if (this.latestSolutionFound == null) {
				// Make the solutions found earlier the current solutions.
				this.firstSolutionFound = this.firstSolutionFoundEalier;
				this.firstSolutionFoundEalier = null;
			}

			// Check if test case generation has been interrupted.
			if (this.interrupted) {
				this.testCaseGenerationAborted = true;
				if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
					Globals.getInst().symbolicExecLogger.info("Test case generation has been interrupted and is now aborted.");
				return;
			}

			// Clear redundancy.
			this.numberOfSolutionsWithRedundancy = this.firstSolutionFound.getNumberOfSolutions();

			try {
				// TODO: this is really slow. There should be the possibility to disable it. Probably a more efficient algorithm would be possible, too.
				if (4.0 == Math.floor(3.0)) this.firstSolutionFound.deleteRedundancy(this);
			} catch (InterruptedException e) {
				this.testCaseGenerationAborted = true;
				if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
					Globals.getInst().symbolicExecLogger.info("Test case generation has been interrupted and is now aborted.");
				return;
			}
			this.finishedDeletingRedudancy = true;
			this.deletingRedudancySolutionCurrentlyProcessed = -1L;
			this.numberOfSolutionsBeforeElimination = this.firstSolutionFound.getNumberOfSolutions();

			// Remove solutions based on the def-use coverage?
			TestCaseEliminationAlgorithms testCaseElimination = null;
			if (Options.getInst().eliminateSolutionsByCoverage > 0) {
				try {
					this.eliminationSolutionsDropped = 0L;
					this.eliminationSolutionsKept = 0L;
					testCaseElimination = new TestCaseEliminationAlgorithms(this, this.firstSolutionFound);
					try {
						this.firstSolutionFound = testCaseElimination.eliminateSolutions();
					} catch (InterruptedException e) {
						this.testCaseGenerationAborted = true;
						if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
							Globals.getInst().symbolicExecLogger.info("Test case generation has been interrupted and is now aborted.");
						return;
					}
				} catch (TestCaseEliminationException e) {
					throw new SymbolicExecutionException(
						"Eliminating test cases failed with an TestCaseEliminationException with message " + e.getMessage()
						);
				}
			}
			this.finishedElimination = true;
			this.eliminationSolutionsDropped = -1L;
			this.eliminationSolutionsKept = -1L;
			this.numberOfTestCases = this.firstSolutionFound.getNumberOfSolutions();

			// Check if test case generation has been interrupted.
			if (this.interrupted) {
				this.testCaseGenerationAborted = true;
				if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
					Globals.getInst().symbolicExecLogger.info("Test case generation has been interrupted and is now aborted.");
				return;
			}

			// Generate the java file.
			String testCaseFile = Options.getInst().testClassesDirectory + "/" + Options.getInst().testClassesName + "0.java";
			String className = Options.getInst().testClassesName + "0";
			// Check if this file already exists.
			if (new File(testCaseFile).isFile()) {
				for (long a = 1; a < Long.MAX_VALUE; a++) {
					// Finding the appropriate file name.
					testCaseFile = Options.getInst().testClassesDirectory + "/" + Options.getInst().testClassesName + a + ".java";
					if (!new File(testCaseFile).isFile()) {
						className = Options.getInst().testClassesName + a;
						break;
					}
					if (a == Long.MAX_VALUE - 1)
						if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN)) Globals.getInst().symbolicExecLogger.warn("Test cases have been generated but could not be written to a file. It appears there are already " + Long.MAX_VALUE + " files. Please delete at least one of them or change the class name for the test classes.");
				}
			}

			// Load the parameter types.
			String[] parameterTypes = this.initialMethod.getParameterTypesAsArray();
			int startAt = 0;
			// Start at 1 if it is not a static method.
			if (!this.initialMethod.isAccStatic()) startAt = 1;

			// Sum up the parameters and return values and build the test methods' string representation.
			int referenceValueCounter = 0;
			int returnedArrayCounter = 0;
			StringBuilder referenceValueInstantiationStringBuilder = new StringBuilder();
			StringBuilder referenceValueInitializationStringBuilder = new StringBuilder();
			CheckingArrayList<Boolean> booleanParameters = new CheckingArrayList<Boolean>();
			CheckingArrayList<Byte> byteParameters = new CheckingArrayList<Byte>();
			CheckingArrayList<Character> charParameters = new CheckingArrayList<Character>();
			CheckingArrayList<Double> doubleParameters = new CheckingArrayList<Double>();
			CheckingArrayList<Float> floatParameters = new CheckingArrayList<Float>();
			CheckingArrayList<Integer> intParameters = new CheckingArrayList<Integer>();
			CheckingArrayList<Long> longParameters = new CheckingArrayList<Long>();
			CheckingArrayList<Short> shortParameters = new CheckingArrayList<Short>();
			CheckingArrayList<List<?>> listParameters = new CheckingArrayList<List<?>>();
			ArrayList<Integer> arrayParameterPositions = new ArrayList<Integer>();
			ArrayList<String> arrayParameterValues = new ArrayList<String>();

			TestCaseSolution solution = this.firstSolutionFound;
			TestCaseSolution jpaSolution = this.firstSolutionFound;
			StringBuilder testMethodStringBuilder = new StringBuilder();
			
			StringBuilder jpaTestCaseStringBuilder = new StringBuilder();
			StringBuilder jpaTestListGenerationMethodBuilder = new StringBuilder();
			
			
			
			
			
			
			ReferenceObjectTextGenerator refObjTxtGen = new ReferenceObjectTextGenerator();
			
			
			// ------- START JPA ----------------------------------------
			
			// Iterate through all solutions. JPA style.
			int testCaseCounter = 0;
			while(jpaSolution != null) {
				testCaseCounter++;

				String methodUnderTestName = this.initialMethod.getName();
				String testMethod = "test" + methodUnderTestName.substring(0, 1).toUpperCase() + methodUnderTestName.substring(1) + testCaseCounter;
//				jpaTestCaseStringBuilder.append("\t//@DBReset\r\n");
				jpaTestCaseStringBuilder.append("\t@Test public void "+testMethod+"() throws Exception { \r\n");
				
//				JPAPreExecutionGenerator dbGenerator = new JPAPreExecutionGenerator();
//				jpaTestCaseStringBuilder.append("\t\t//Generate pre-execution required data.\r\n");
//				jpaTestCaseStringBuilder.append(dbGenerator.generatePreExecutionRequiredString(jpaSolution.getPreExecutionDatabaseSolution().getSolution()));
//				jpaTestCaseStringBuilder.append("\r\n");
				
				jpaTestCaseStringBuilder.append(jpaSolution.getRequiredDBDataStringBuilder());
				
				jpaTestCaseStringBuilder.append(jpaSolution.getMethodParameterStringBuilder());
				
				jpaTestCaseStringBuilder.append("\t\t/** SOLUTION \r\n");
				jpaTestCaseStringBuilder.append("\t\t"+jpaSolution.getSolution().toString());
				jpaTestCaseStringBuilder.append("\r\n\t\t  ***********************************/ \r\n");
				
				
				/*
				 * Distinguish between parameters that will actually lead to a return value and
				 * those, that will lead to an uncaught exception.
				 */
				String addAfterTheMethod = "";
				if (jpaSolution.getThrowsAnUncaughtException()) {
					// Get information about the exception.
					String exceptionName = jpaSolution.getUncaughtExceptionData().getUncaughtThrowable().getName();
					
					jpaTestCaseStringBuilder.append("\t\tthrown.expect(new MugglExceptionMatcher(");
					jpaTestCaseStringBuilder.append(exceptionName);
					jpaTestCaseStringBuilder.append(".class));\r\n\t\t");
					
					// Build the Strings.
//					jpaTestCaseStringBuilder.append("\t\ttry {\r\n\t\t\t");
//					addAfterTheMethod = "\t\t\tfail(\"Expected a " + exceptionName + " to be thrown.\");\r\n"
//						+ "\t\t} catch (" + exceptionName + " e) {\r\n"
//						+ "\t\t\t// Do nothing - this is what we expect to happen!\r\n"
//						+ "\t\t}\r\n";
				} else {
					// add expected result
					ExpectedDatabasePostExecutionGenerator postDBGen = new ExpectedDatabasePostExecutionGenerator();
					addAfterTheMethod += postDBGen.generatePostExecutionDataString(jpaSolution.getPostExecutionDatabaseSolution().getSolution());
					
					// Add the return value.
					String returnType = this.initialMethod.getReturnType();
					if (!returnType.equals("void")) {
						Object returnValue = jpaSolution.getEvaluatedReturnValue();

						// Distinguish between the types.
						if (returnValue == null || returnValue instanceof ReferenceValue) {
							if (returnValue != null) {
								// Check for assignment compatibility first.
								ExecutionAlgorithms execution = new ExecutionAlgorithms(this.classLoader);
								try {
									if (returnValue instanceof ModifieableArrayref
											&& ((ModifieableArrayref) returnValue).getRepresentedType() != null) {
										String representedType = ((ModifieableArrayref) returnValue).getRepresentedType();
										if (representedType.startsWith("[")) {
											representedType = "[" + representedType;
										} else {
											representedType = "[L" + representedType + ";";
										}
										if (!execution.checkForAssignmentCompatibility(representedType, returnType, this.vm, true))
											throw new SymbolicExecutionException("The returned value is not assignment compatible to the expected return type. Aborting.");
									} else {
										if (!execution.checkForAssignmentCompatibility(returnValue, returnType, this.vm, false))
											throw new SymbolicExecutionException("The returned value is not assignment compatible to the expected return type. Aborting.");
									}
								} catch (ExecutionException e) {
									throw new SymbolicExecutionException(e.getMessage());
								}
							}

							// Get the canonical name of the return type.
							String returnTypeCanonical = returnType;
							try {
								returnTypeCanonical = this.classLoader.getClassAsClassFile(returnTypeCanonical).getCanonicalName();
							} catch (ClassFileException e) {
								// Simply ignore it.
							}

							// Array type?
							if (returnType.endsWith("[]")) {
								jpaTestCaseStringBuilder.append("\t\tassertArrayEquals(this.");
								referenceValueInstantiationStringBuilder.append("\tprivate " + returnTypeCanonical
								 + " returnedArray" + returnedArrayCounter + " = ");
								if (returnValue == null) {
									jpaTestCaseStringBuilder.append("returnedArray" + returnedArrayCounter);
									// Set the instantiation String.
									referenceValueInstantiationStringBuilder.append("null");
								} else {
									// Got a Arrayref?
									if (returnValue instanceof Arrayref) {
										// Extract its values and try to evaluate them.
										Arrayref arrayref = (Arrayref) returnValue;
										// TODO This does not work for multidimensional arrays.
										Constant[] constants = (Constant[]) jpaSolution.convertArrayrefIntoConstantArray(arrayref, -1);
										jpaTestCaseStringBuilder.append("returnedArray" + returnedArrayCounter);
										// Is is an empty array?
										if (constants.length == 0) {
											String emptyArrayInitialization = "new " + returnType;
											referenceValueInstantiationStringBuilder.append(
												emptyArrayInitialization.substring(0, emptyArrayInitialization.length() - 1)
												+ "0" + emptyArrayInitialization.substring(emptyArrayInitialization.length() - 1));
										} else {
											referenceValueInstantiationStringBuilder.append(constantArrayToInitializationString(constants));
										}

									} else {
										// This array cannot be processed.
										throw new SymbolicExecutionException("Cannot process the return value as no array reference was supplied. Aborting.");
									}
								}
								referenceValueInstantiationStringBuilder.append(";\r\n");
								returnedArrayCounter++;
							} else {
								jpaTestCaseStringBuilder.append("\t\tassertEquals(this."
										+ "reference" + referenceValueCounter);
								String initializationStringForReferenceValue = "";
								
								// TODO: Implement a method to get suitable initialization Strings from the reference values' fields.
								if (returnValue != null && returnValue instanceof Objectref) {
									if (((Objectref) returnValue).getInitializedClass().getClassFile().getName().equals("java.lang.String")) {
										Field field = ((Objectref) returnValue).getInitializedClass().getClassFile().getFieldByNameAndDescriptor("value", "[C");
										Arrayref characters = (Arrayref) ((Objectref) returnValue).getField(field);
										initializationStringForReferenceValue = "\"";
										for (int b = 0; b < characters.length; b++) {
											initializationStringForReferenceValue += (char)((IntConstant)characters.getElement(b)).getIntValue();
										}
										initializationStringForReferenceValue += "\"";
									} else {
										// TODO: Recursively create object structures to initialize complex objects.
									}
								/*
								 * If the return type is a primitive type, the java.lang wrapper class
								 * has to be taken. Otherwise, the field could for example not be set to
								 * null.
								 */
								} else if (returnType.equals("boolean")) returnType = "Boolean";
								else if (returnType.equals("byte")) returnType = "Byte";
								else if (returnType.equals("char")) returnType = "Character";
								else if (returnType.equals("double")) returnType = "Double";
								else if (returnType.equals("float")) returnType = "Float";
								else if (returnType.equals("int")) returnType = "Integer";
								else if (returnType.equals("long")) returnType = "Long";
								else if (returnType.equals("short")) returnType = "Short";

								// Build the code.
								referenceValueInstantiationStringBuilder.append("\tprivate " + returnTypeCanonical + " reference" + referenceValueCounter + ";\r\n");
								referenceValueInitializationStringBuilder.append("\t\tthis.reference" + referenceValueCounter + " = ");
								if (returnValue != null && !(returnValue instanceof ObjectNullRef)) {
									referenceValueInitializationStringBuilder.append("new " + ((ReferenceValue) returnValue).getInitializedClass().getClassFile().getCanonicalName() + "(" + initializationStringForReferenceValue + ");\r\n");
								} else {
									referenceValueInitializationStringBuilder.append("null;\r\n");
								}
								referenceValueCounter++;
							}
						} else {
							jpaTestCaseStringBuilder.append("\t\tassertEquals(this.");

							// Non-Constant return type?
							if (returnValue instanceof Term) {
								Term returnValueTerm = (Term) returnValue;
								if (!returnValueTerm.isConstant()) {
									// Insert the solution to resolve variables with no calculated value.
									returnValue = returnValueTerm.insert(jpaSolution.getSolution(), true);
								}
							}

							// Distinguish between the primitive types.
							if (returnType.equals("boolean") && (returnValue instanceof BooleanConstant || returnValue instanceof IntConstant)) {
								if (returnValue instanceof BooleanConstant) {
									booleanParameters.addIfNotContained(((BooleanConstant) returnValue).getValue());
									jpaTestCaseStringBuilder.append("boolean" + returnValue.toString()); // No need to escape a boolean value.
								} else {
									Boolean value = ((IntConstant) returnValue).getValue() == 1 ? true : false;
									booleanParameters.addIfNotContained(value);
									jpaTestCaseStringBuilder.append("boolean" + value.toString());
								}
							} else if (returnType.equals("byte") && returnValue instanceof IntConstant) {
								byteParameters.addIfNotContained((byte) ((IntConstant) returnValue).getIntValue());
								jpaTestCaseStringBuilder.append("byte" + escapeSignsUnsuitableForFields(returnValue.toString()));
							} else if (returnType.equals("char") && returnValue instanceof IntConstant) {
								charParameters.addIfNotContained((char) ((IntConstant) returnValue).getIntValue());
								jpaTestCaseStringBuilder.append("char" + escapeSignsUnsuitableForFields((Character) returnValue));
							} else if (returnType.equals("double") && returnValue instanceof DoubleConstant) {
								Double value = ((DoubleConstant) returnValue).getDoubleValue();
								doubleParameters.addIfNotContained(value);
								jpaTestCaseStringBuilder.append("double");
								if (value == Double.POSITIVE_INFINITY) {
									jpaTestCaseStringBuilder.append("PositiveInfinity");
								} else if (value == Double.NEGATIVE_INFINITY) {
									jpaTestCaseStringBuilder.append("NegativeInfinity");
								} else if (value.isNaN()) {
									jpaTestCaseStringBuilder.append("NaN");
								} else {
									jpaTestCaseStringBuilder.append(escapeSignsUnsuitableForFields(value.toString()));
								}
							} else if (returnType.equals("float") && returnValue instanceof FloatConstant) {
								Float value = ((FloatConstant) returnValue).getFloatValue();
								floatParameters.addIfNotContained(value);
								jpaTestCaseStringBuilder.append("float");
								if (value == Float.POSITIVE_INFINITY) {
									jpaTestCaseStringBuilder.append("PositiveInfinity");
								} else if (value == Float.NEGATIVE_INFINITY) {
									jpaTestCaseStringBuilder.append("NegativeInfinity");
								} else if (value == Float.NaN) {
									jpaTestCaseStringBuilder.append("NaN");
								} else {
									jpaTestCaseStringBuilder.append(escapeSignsUnsuitableForFields(value.toString()));
								}
							} else if (returnType.equals("int") && returnValue instanceof IntConstant) {
								intParameters.addIfNotContained(((IntConstant) returnValue).getIntValue());
								jpaTestCaseStringBuilder.append("int" + escapeSignsUnsuitableForFields(returnValue.toString()));
							} else if (returnType.equals("long") && returnValue instanceof LongConstant) {
								longParameters.addIfNotContained(((LongConstant) returnValue).getLongValue());
								jpaTestCaseStringBuilder.append("long" + escapeSignsUnsuitableForFields(returnValue.toString()));
							} else if (returnType.equals("short") && returnValue instanceof IntConstant) {
								shortParameters.addIfNotContained((short) ((IntConstant) returnValue).getIntValue());
								jpaTestCaseStringBuilder.append("short" + escapeSignsUnsuitableForFields(returnValue.toString()));
							} else if (returnType.equals("java.util.List") && returnValue instanceof List<?>) {
								listParameters.addIfNotContained((List<?>)returnValue);
								jpaTestCaseStringBuilder.append("list" + escapeSignsUnsuitableForFields(Integer.toString(returnValue.hashCode())));
							} else {
								throw new SymbolicExecutionException("The type of the returned value does not match the expected type. Aborting.");
							}
						}
						jpaTestCaseStringBuilder.append(", ");
					} else {
						// it is a void return type
						jpaTestCaseStringBuilder.append("\t\t");
					}
				}

				// Has the class been instantiated?
				if (!this.initialMethod.isAccStatic()) {
					// Add this information only if the tested class will be instantiated.
					jpaTestCaseStringBuilder.append("this.testedClass.");
				} else {
					jpaTestCaseStringBuilder.append(this.initialMethod.getClassFile().getCanonicalName() + ".");
				}

				jpaTestCaseStringBuilder.append(this.initialMethod.getName() + "(");
				Object[] parameters = jpaSolution.getParameters(this.vm.getStringCache());
		
				for(int i=0; i<parameters.length; i++) {
					if(parameters[i] != null) {
						jpaTestCaseStringBuilder.append("arg"+i);
						if( (i+1) < parameters.length ) {
							jpaTestCaseStringBuilder.append(", ");
						}
					}
				}
				
		if(false) {				
				// Iterate through all parameters.
				boolean firstPass = true;
		
				// Go!
				for (int a = startAt; a < parameters.length; a++) {
					// Add a comma separator if needed.
					if (!firstPass) {
						jpaTestCaseStringBuilder.append(", this.");
					} else {
						jpaTestCaseStringBuilder.append("this.");
					}

					// Distinguish between the types.
					if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].endsWith("[]")) {
						if (parameters[a] == null) {
							jpaTestCaseStringBuilder.append("array" + arrayParameterPositions.size());
							// Process an array.
							arrayParameterPositions.add(a);
							arrayParameterValues.add("null");
						} else {
							// Make sure the array to process contains constants only.
							String nonArrayClassName = parameters[a].getClass().getCanonicalName().replace("[]", "");
							if (nonArrayClassName.equals("de.wwu.muggl.solvers.expressions.Constant")) {
								jpaTestCaseStringBuilder.append("array" + arrayParameterPositions.size());
								// Process an array.
								arrayParameterPositions.add(a);
								Constant[] constants = (Constant[]) parameters[a];
								// Is is an empty array?
								if (constants.length == 0) {
									String emptyArrayInitialization = "new " + parameterTypes[a - startAt];
									emptyArrayInitialization =
										emptyArrayInitialization.substring(0, emptyArrayInitialization.length() - 1)
										+ "0" + emptyArrayInitialization.substring(emptyArrayInitialization.length() - 1);
									arrayParameterValues.add(emptyArrayInitialization);
								} else {
									arrayParameterValues.add(constantArrayToInitializationString(constants));
								}
							} else {
								// This array cannot be processed.
								throw new SymbolicExecutionException("Cannot process an array that does not contain constant values. Aborting.");
							}
						}
					} else if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].equals("boolean") && (parameters[a] instanceof BooleanConstant || parameters[a] instanceof IntConstant)) {
						if (parameters[a] instanceof BooleanConstant) {
							booleanParameters.addIfNotContained(((BooleanConstant) parameters[a]).getValue());
							jpaTestCaseStringBuilder.append("boolean" + parameters[a].toString()); // No need to escape a boolean value.
						} else {
							Boolean value = ((IntConstant) parameters[a]).getValue() == 1 ? true : false;
							booleanParameters.addIfNotContained(value);
							jpaTestCaseStringBuilder.append("boolean" + value.toString());
						}
					} else if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].equals("byte") && parameters[a] instanceof IntConstant) {
						byteParameters.addIfNotContained((byte) ((IntConstant) parameters[a]).getIntValue());
						jpaTestCaseStringBuilder.append("byte" + escapeSignsUnsuitableForFields(((Byte) (byte) ((IntConstant) parameters[a]).getIntValue()).toString()));
					} else if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].equals("char") && parameters[a] instanceof IntConstant) {
						charParameters.addIfNotContained((char) ((IntConstant) parameters[a]).getIntValue());
						jpaTestCaseStringBuilder.append("char" + escapeSignsUnsuitableForFields((char) ((IntConstant) parameters[a]).getIntValue()));
					} else if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].equals("double") && parameters[a] instanceof DoubleConstant) {
						Double value = ((DoubleConstant) parameters[a]).getDoubleValue();
						doubleParameters.addIfNotContained(value);
						jpaTestCaseStringBuilder.append("double");
						if (value == Double.POSITIVE_INFINITY) {
							jpaTestCaseStringBuilder.append("PositiveInfinity");
						} else if (value == Double.NEGATIVE_INFINITY) {
							jpaTestCaseStringBuilder.append("NegativeInfinity");
						} else if (value.isNaN()) {
							jpaTestCaseStringBuilder.append("NaN");
						} else {
							jpaTestCaseStringBuilder.append(escapeSignsUnsuitableForFields(value.toString()));
						}
					} else if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].equals("float") && parameters[a] instanceof FloatConstant) {
						Float value = ((FloatConstant) parameters[a]).getFloatValue();
						floatParameters.addIfNotContained(value);
						jpaTestCaseStringBuilder.append("float");
						if (value == Float.POSITIVE_INFINITY) {
							jpaTestCaseStringBuilder.append("PositiveInfinity");
						} else if (value == Float.NEGATIVE_INFINITY) {
							jpaTestCaseStringBuilder.append("NegativeInfinity");
						} else if (value == Float.NaN) {
							jpaTestCaseStringBuilder.append("NaN");
						} else {
							jpaTestCaseStringBuilder.append(escapeSignsUnsuitableForFields(value.toString()));
						}
					} else if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].equals("int") && parameters[a] instanceof IntConstant) {
						intParameters.addIfNotContained(((IntConstant) parameters[a]).getIntValue());
						jpaTestCaseStringBuilder.append("int" + escapeSignsUnsuitableForFields(parameters[a].toString()));
					} else if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].equals("long") && parameters[a] instanceof LongConstant) {
						longParameters.addIfNotContained(((LongConstant) parameters[a]).getLongValue());
						jpaTestCaseStringBuilder.append("long" + escapeSignsUnsuitableForFields(parameters[a].toString()));
					} else if (parameterTypes.length > (a - startAt) && parameterTypes[a - startAt].equals("short") && parameters[a] instanceof IntConstant) {
						shortParameters.addIfNotContained((short) ((IntConstant) parameters[a]).getIntValue());
						jpaTestCaseStringBuilder.append("short" + escapeSignsUnsuitableForFields(parameters[a].toString()));
					} else if(parameterTypes.length > (a - startAt) && parameters[a] != null && parameters[a] instanceof ReferenceVariable) {
						ReferenceVariable referenceVariable = (ReferenceVariable)parameters[a];
						String varName = referenceVariable.getName().replace("\\.", "") + (referenceValueCounter+1);
						jpaTestCaseStringBuilder.append(varName);
						
						referenceValueInstantiationStringBuilder.append("\tprivate " + referenceVariable.getObjectType() + " " + varName +";\r\n");
						
						refObjTxtGen.generateObjectInitializationText(jpaSolution, referenceValueInitializationStringBuilder, varName, referenceVariable, true);
						
						referenceValueCounter++;
					} else {
						if(parameters[a] != null && parameters[a] instanceof ObjectConstant) {
							ObjectConstant objectConstant = (ObjectConstant)parameters[a];
							
							jpaTestCaseStringBuilder.append("reference" + referenceValueCounter);
							
							referenceValueInstantiationStringBuilder.append("\tprivate " + objectConstant.getObjectReference().getClass().getName() + " reference" + referenceValueCounter + ";\r\n");
							
							referenceValueInitializationStringBuilder.append("\t\tthis.reference" + referenceValueCounter + " = new Foobar();\r\n");
							
							referenceValueCounter++;
						// It has to be a reference value, or we encountered a mismatch.
						} else if (parameters[a] == null || parameters[a] instanceof ReferenceValue) {
							if (parameters[a] != null) {
								// Check for assignment compatibility first.
								ExecutionAlgorithms execution = new ExecutionAlgorithms(this.classLoader);
								try {
									if (!execution.checkForAssignmentCompatibility(parameters[a], parameterTypes[a - startAt], this.vm, false)) {
										throw new SymbolicExecutionException("A parameter value is not assignment compatible to the expected parameter type. Aborting.");
									}
								} catch (ExecutionException e) {
									throw new SymbolicExecutionException(e.getMessage());
								}
							}

							jpaTestCaseStringBuilder.append("reference" + referenceValueCounter);
							String initializationStringForReferenceValue = "";
							// TODO: Implement a method to get suitable initialization Strings from the reference values' fields.
							if (parameters[a] != null && parameters[a] instanceof Objectref && ((Objectref) parameters[a]).getInitializedClass().getClassFile().getName().equals("java.lang.String")) {
								Field field = ((Objectref) parameters[a]).getInitializedClass().getClassFile().getFieldByNameAndDescriptor("value", "[C");
								initializationStringForReferenceValue = "";
								
								Object stringValueObject = ((Objectref) parameters[a]).getField(field);
								if(stringValueObject instanceof Arrayref) {
									Arrayref stringValueObjectRef = (Arrayref)stringValueObject;
									initializationStringForReferenceValue += "\"";
									for(int c=0; c<stringValueObjectRef.length; c++) {
										Object o = stringValueObjectRef.getElement(c);
										if(o instanceof IntConstant) {
											int asciiStringValue = ((IntConstant)o).getValue();
											String asciiString = Character.toString((char)asciiStringValue);
											initializationStringForReferenceValue += asciiString;
										}
									}
									initializationStringForReferenceValue += "\"";
								} else {
									//TODO: dont know if this branch is ever reached...
									Character[] characters = (Character[]) ((Objectref) parameters[a]).getField(field);
									initializationStringForReferenceValue = "";
									for (int b = 0; b < characters.length; b++) {
										initializationStringForReferenceValue += characters[b].toString();
									}
								}
							}
							String parameterType = parameterTypes[a - startAt];
							if (parameterType.endsWith("..."))
								parameterType = parameterType.substring(0, parameterType.length() - 3) + "[]";

							// Get the canonical name of the parameter type.
							try {
								parameterType = this.classLoader.getClassAsClassFile(parameterType).getCanonicalName();
							} catch (ClassFileException e) {
								// Simply ignore it.
							}

							// Construct the Strings.
							referenceValueInstantiationStringBuilder.append("\tprivate " + parameterType + " reference" + referenceValueCounter + ";\r\n");
							referenceValueInitializationStringBuilder.append("\t\tthis.reference" + referenceValueCounter + " = ");
							if (parameters[a] != null) {
								referenceValueInitializationStringBuilder.append("new " + ((ReferenceValue) parameters[a]).getInitializedClass().getClassFile().getCanonicalName() + "(" + initializationStringForReferenceValue + ");\r\n");
							} else {
								referenceValueInitializationStringBuilder.append("null;\r\n");
							}
							referenceValueCounter++;
						} else {
							throw new SymbolicExecutionException("A parameter type does not match the expected type. Aborting.");
						}
					}
					firstPass = false;
				}
			}
		
				
				String returnType = this.initialMethod.getReturnType();
				if (!returnType.equals("void")) {
					jpaTestCaseStringBuilder.append("));");
				} else {
					jpaTestCaseStringBuilder.append(");");
				}
				
				if (jpaSolution.getSolutionComment() != null) {
					jpaTestCaseStringBuilder.append(" // " + jpaSolution.getSolutionComment());
				}

				jpaTestCaseStringBuilder.append("\r\n" + addAfterTheMethod);
				
				
				jpaTestCaseStringBuilder.append(jpaSolution.getPostDBStateCheckStringBuilder());
				
				jpaTestCaseStringBuilder.append("\t} \r\n \r\n");
				
				// Continue with the next solution.
				jpaSolution = jpaSolution.getSuccessor();				
			}


			
			// -------------- END JPA ----------------------------------------------------
			
			
			
//			StringBuilder databaseResultStringBuilder = new StringBuilder();
//			// Iterate through all solutions.
//			while (solution != null && false) {
//				int addDelta = 0;
//				
//				databaseResultStringBuilder.append("\t\t/* Database after test execution:\r\n");
//				for(String tbl : VirtualDatabase.getInstance().getDataTables()) {
//					databaseResultStringBuilder.append("\t\t * " + tbl +"\r\n");
//					int cnt=1;
//					for(Object data : VirtualDatabase.getInstance().getData(tbl)) {
//						databaseResultStringBuilder.append("\t\t * #"+cnt+": " + data +"\r\n");
//						Objectref objectRefData = (Objectref)data;
//						Enumeration<Field> fields = objectRefData.getFields().keys();
//						while(fields.hasMoreElements()) {
//							Field dataField = fields.nextElement();
//							databaseResultStringBuilder.append("\t\t\t" + dataField.getName() + "=" + objectRefData.getFields().get(dataField) + "\r\n");
//						}
//						
//						cnt++;
//					}
//				}
//				databaseResultStringBuilder.append("\t\t*/\r\n");
//				
//				for(String entityName : JPAConstraintVariableManager.getInstance().getEntitiesWithConstraints()) {
//					String[] entitySplitName = entityName.split("\\.");
//					String generatedEntityFieldName = entitySplitName[entitySplitName.length-1].toLowerCase();
//					testMethodStringBuilder.append("\t\t" +entityName + " " + generatedEntityFieldName + " = new " + entityName + "(); \r\n");
//					JPAEntityConstraint entityConstraint = JPAConstraintVariableManager.getInstance().getEntityConstraint(entityName);
//					
//					if(entityConstraint.getDependentEntities().size() > 0) {
//						for(Class<?> dependentEntity : entityConstraint.getDependentEntities()) {
//							String[] dpEntitySplitName = dependentEntity.getName().split("\\.");
//							String dpGeneratedEntityFieldName = dpEntitySplitName[dpEntitySplitName.length-1].toLowerCase();
//							testMethodStringBuilder.append("\t\t" +dependentEntity.getName() + " " + dpGeneratedEntityFieldName + " = new " + dependentEntity.getName() + "(); \r\n");
//							
//							JPAEntityConstraint dependent = JPAConstraintVariableManager.getInstance().getEntityConstraint(dependentEntity.getName());
//							for(String fieldName : dependent.getFields()) {
//								Variable entityVar = dependent.getVariable(fieldName);
//								Constant constant = solution.getSolution().getValue(entityVar);
//								String setterMethod = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
//								testMethodStringBuilder.append("\t\t" +dpGeneratedEntityFieldName + ".set" + setterMethod + "(" + constant + "); \r\n");
//							}
//							String setterMethod = dpGeneratedEntityFieldName.substring(0, 1).toUpperCase() + dpGeneratedEntityFieldName.substring(1);
//							testMethodStringBuilder.append("\t\t" +generatedEntityFieldName + ".set" + setterMethod + "(" + dpGeneratedEntityFieldName + "); \r\n");
//						}
//					}
//					
//					for(String fieldName : entityConstraint.getFields()) {
//						Variable entityVar = entityConstraint.getVariable(fieldName);
//						Constant constant = solution.getSolution().getValue(entityVar);
//						String setterMethod = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
//						testMethodStringBuilder.append("\t\t" +generatedEntityFieldName + ".set" + setterMethod + "(" + constant + "); \r\n");
//					}
//					testMethodStringBuilder.append("\t\tthis.entityManager.persist(" + generatedEntityFieldName + "); \r\n");
//				}
//
//				/*
//				 * Distinguish between parameters that will actually lead to a return value and
//				 * those, that will lead to an uncaught exception.
//				 */
//				String addAfterTheMethod = "";
//				if (solution.getThrowsAnUncaughtException()) {
//					// Get information about the exception.
//					String exceptionName = solution.getUncaughtExceptionData().getUncaughtThrowable().getName();
//
//					// Build the Strings.
//					testMethodStringBuilder.append("\t\ttry {\r\n\t\t\t");
//					addAfterTheMethod = "\t\t\tfail(\"Expected a " + exceptionName + " to be thrown.\");\r\n"
//						+ "\t\t} catch (" + exceptionName + " e) {\r\n"
//						+ "\t\t\t// Do nothing - this is what we expect to happen!\r\n"
//						+ "\t\t}\r\n";
//				} else {
//					// Add the return value.
//					String returnType = this.initialMethod.getReturnType();
//					if (!returnType.equals("void")) {
//						Object returnValue = solution.getEvaluatedReturnValue();
//
//						// Distinguish between the types.
//						if (returnValue == null || returnValue instanceof ReferenceValue) {
//							if (returnValue != null) {
//								// Check for assignment compatibility first.
//								ExecutionAlgorithms execution = new ExecutionAlgorithms(this.classLoader);
//								try {
//									if (returnValue instanceof ModifieableArrayref
//											&& ((ModifieableArrayref) returnValue).getRepresentedType() != null) {
//										String representedType = ((ModifieableArrayref) returnValue).getRepresentedType();
//										if (representedType.startsWith("[")) {
//											representedType = "[" + representedType;
//										} else {
//											representedType = "[L" + representedType + ";";
//										}
//										if (!execution.checkForAssignmentCompatibility(representedType, returnType, this.vm, true))
//											throw new SymbolicExecutionException("The returned value is not assignment compatible to the expected return type. Aborting.");
//									} else {
//										if (!execution.checkForAssignmentCompatibility(returnValue, returnType, this.vm, false))
//											throw new SymbolicExecutionException("The returned value is not assignment compatible to the expected return type. Aborting.");
//									}
//								} catch (ExecutionException e) {
//									throw new SymbolicExecutionException(e.getMessage());
//								}
//							}
//
//							// Get the canonical name of the return type.
//							String returnTypeCanonical = returnType;
//							try {
//								returnTypeCanonical = this.classLoader.getClassAsClassFile(returnTypeCanonical).getCanonicalName();
//							} catch (ClassFileException e) {
//								// Simply ignore it.
//							}
//
//							// Array type?
//							if (returnType.endsWith("[]")) {
//								testMethodStringBuilder.append("\t\tassertArrayEquals(this.");
//								referenceValueInstantiationStringBuilder.append("\tprivate " + returnTypeCanonical
//								 + " returnedArray" + returnedArrayCounter + " = ");
//								if (returnValue == null) {
//									testMethodStringBuilder.append("returnedArray" + returnedArrayCounter);
//									// Set the instantiation String.
//									referenceValueInstantiationStringBuilder.append("null");
//								} else {
//									// Got a Arrayref?
//									if (returnValue instanceof Arrayref) {
//										// Extract its values and try to evaluate them.
//										Arrayref arrayref = (Arrayref) returnValue;
//										// TODO This does not work for multidimensional arrays.
//										Constant[] constants = (Constant[]) solution.convertArrayrefIntoConstantArray(arrayref, -1);
//										testMethodStringBuilder.append("returnedArray" + returnedArrayCounter);
//										// Is is an empty array?
//										if (constants.length == 0) {
//											String emptyArrayInitialization = "new " + returnType;
//											referenceValueInstantiationStringBuilder.append(
//												emptyArrayInitialization.substring(0, emptyArrayInitialization.length() - 1)
//												+ "0" + emptyArrayInitialization.substring(emptyArrayInitialization.length() - 1));
//										} else {
//											referenceValueInstantiationStringBuilder.append(constantArrayToInitializationString(constants));
//										}
//
//									} else {
//										// This array cannot be processed.
//										throw new SymbolicExecutionException("Cannot process the return value as no array reference was supplied. Aborting.");
//									}
//								}
//								referenceValueInstantiationStringBuilder.append(";\r\n");
//								returnedArrayCounter++;
//							} else {
//								testMethodStringBuilder.append("\t\tassertEquals(this."
//										+ "reference" + referenceValueCounter);
//								String initializationStringForReferenceValue = "";
//								
//								// TODO: Implement a method to get suitable initialization Strings from the reference values' fields.
//								if (returnValue != null && returnValue instanceof Objectref) {
//									if (((Objectref) returnValue).getInitializedClass().getClassFile().getName().equals("java.lang.String")) {
//										Field field = ((Objectref) returnValue).getInitializedClass().getClassFile().getFieldByNameAndDescriptor("value", "[C");
//										Arrayref characters = (Arrayref) ((Objectref) returnValue).getField(field);
//										initializationStringForReferenceValue = "\"";
//										for (int b = 0; b < characters.length; b++) {
//											initializationStringForReferenceValue += (char)((IntConstant)characters.getElement(b)).getIntValue();
//										}
//										initializationStringForReferenceValue += "\"";
//									} else {
//										// TODO: Recursively create object structures to initialize complex objects.
//									}
//								/*
//								 * If the return type is a primitive type, the java.lang wrapper class
//								 * has to be taken. Otherwise, the field could for example not be set to
//								 * null.
//								 */
//								} else if (returnType.equals("boolean")) returnType = "Boolean";
//								else if (returnType.equals("byte")) returnType = "Byte";
//								else if (returnType.equals("char")) returnType = "Character";
//								else if (returnType.equals("double")) returnType = "Double";
//								else if (returnType.equals("float")) returnType = "Float";
//								else if (returnType.equals("int")) returnType = "Integer";
//								else if (returnType.equals("long")) returnType = "Long";
//								else if (returnType.equals("short")) returnType = "Short";
//
//								// Build the code.
//								referenceValueInstantiationStringBuilder.append("\tprivate " + returnTypeCanonical + " reference" + referenceValueCounter + ";\r\n");
//								referenceValueInitializationStringBuilder.append("\t\tthis.reference" + referenceValueCounter + " = ");
//								if (returnValue != null) {
//									referenceValueInitializationStringBuilder.append("new " + ((ReferenceValue) returnValue).getInitializedClass().getClassFile().getCanonicalName() + "(" + initializationStringForReferenceValue + ");\r\n");
//								} else {
//									referenceValueInitializationStringBuilder.append("null;\r\n");
//								}
//								referenceValueCounter++;
//							}
//						} else {
//							testMethodStringBuilder.append("\t\tassertEquals(this.");
//
//							// Non-Constant return type?
//							if (returnValue instanceof Term) {
//								Term returnValueTerm = (Term) returnValue;
//								if (!returnValueTerm.isConstant()) {
//									// Insert the solution to resolve variables with no calculated value.
//									returnValue = returnValueTerm.insert(solution.getSolution(), true);
//								}
//							}
//
//							// Distinguish between the primitive types.
//							if (returnType.equals("boolean") && (returnValue instanceof BooleanConstant || returnValue instanceof IntConstant)) {
//								if (returnValue instanceof BooleanConstant) {
//									booleanParameters.addIfNotContained(((BooleanConstant) returnValue).getValue());
//									testMethodStringBuilder.append("boolean" + returnValue.toString()); // No need to escape a boolean value.
//								} else {
//									Boolean value = ((IntConstant) returnValue).getValue() == 1 ? true : false;
//									booleanParameters.addIfNotContained(value);
//									testMethodStringBuilder.append("boolean" + value.toString());
//								}
//							} else if (returnType.equals("byte") && returnValue instanceof IntConstant) {
//								byteParameters.addIfNotContained((byte) ((IntConstant) returnValue).getIntValue());
//								testMethodStringBuilder.append("byte" + escapeSignsUnsuitableForFields(returnValue.toString()));
//							} else if (returnType.equals("char") && returnValue instanceof IntConstant) {
//								charParameters.addIfNotContained((char) ((IntConstant) returnValue).getIntValue());
//								testMethodStringBuilder.append("char" + escapeSignsUnsuitableForFields((Character) returnValue));
//							} else if (returnType.equals("double") && returnValue instanceof DoubleConstant) {
//								Double value = ((DoubleConstant) returnValue).getDoubleValue();
//								doubleParameters.addIfNotContained(value);
//								testMethodStringBuilder.append("double");
//								if (value == Double.POSITIVE_INFINITY) {
//									testMethodStringBuilder.append("PositiveInfinity");
//								} else if (value == Double.NEGATIVE_INFINITY) {
//									testMethodStringBuilder.append("NegativeInfinity");
//								} else if (value.isNaN()) {
//									testMethodStringBuilder.append("NaN");
//								} else {
//									testMethodStringBuilder.append(escapeSignsUnsuitableForFields(value.toString()));
//								}
//								addDelta = 1;
//							} else if (returnType.equals("float") && returnValue instanceof FloatConstant) {
//								Float value = ((FloatConstant) returnValue).getFloatValue();
//								floatParameters.addIfNotContained(value);
//								testMethodStringBuilder.append("float");
//								if (value == Float.POSITIVE_INFINITY) {
//									testMethodStringBuilder.append("PositiveInfinity");
//								} else if (value == Float.NEGATIVE_INFINITY) {
//									testMethodStringBuilder.append("NegativeInfinity");
//								} else if (value == Float.NaN) {
//									testMethodStringBuilder.append("NaN");
//								} else {
//									testMethodStringBuilder.append(escapeSignsUnsuitableForFields(value.toString()));
//								}
//								addDelta = 2;
//							} else if (returnType.equals("int") && returnValue instanceof IntConstant) {
//								intParameters.addIfNotContained(((IntConstant) returnValue).getIntValue());
//								testMethodStringBuilder.append("int" + escapeSignsUnsuitableForFields(returnValue.toString()));
//							} else if (returnType.equals("long") && returnValue instanceof LongConstant) {
//								longParameters.addIfNotContained(((LongConstant) returnValue).getLongValue());
//								testMethodStringBuilder.append("long" + escapeSignsUnsuitableForFields(returnValue.toString()));
//							} else if (returnType.equals("short") && returnValue instanceof IntConstant) {
//								shortParameters.addIfNotContained((short) ((IntConstant) returnValue).getIntValue());
//								testMethodStringBuilder.append("short" + escapeSignsUnsuitableForFields(returnValue.toString()));
//							} else if (returnType.equals("java.util.List") && returnValue instanceof List<?>) {
//								listParameters.addIfNotContained((List<?>)returnValue);
//								testMethodStringBuilder.append("list" + escapeSignsUnsuitableForFields(Integer.toString(returnValue.hashCode())));
//							} else {
//								throw new SymbolicExecutionException("The type of the returned value does not match the expected type. Aborting.");
//							}
//						}
//						testMethodStringBuilder.append(", ");
//					} else {
//						// it is a void return type
//						testMethodStringBuilder.append("\t\t");
//					}
//				}
//
//				// Has the class been instantiated?
//				if (!this.initialMethod.isAccStatic()) {
//					// Add this information only if the tested class will be instantiated.
//					testMethodStringBuilder.append("this.testedClass.");
//				} else {
//					testMethodStringBuilder.append(this.initialMethod.getClassFile().getCanonicalName() + ".");
//				}
//
//				testMethodStringBuilder.append(this.initialMethod.getName() + "(");
//				Object[] parameters = solution.getParameters(this.vm.getStringCache());
//				// Iterate through all parameters.
//				boolean firstPass = true;
//
//				// Go!
//				for (int a = startAt; a < parameters.length; a++) {
//					// Add a comma separator if needed.
//					if (!firstPass) {
//						testMethodStringBuilder.append(", this.");
//					} else {
//						testMethodStringBuilder.append("this.");
//					}
//
//					// Distinguish between the types.
//					if (parameterTypes[a - startAt].endsWith("[]")) {
//						if (parameters[a] == null) {
//							testMethodStringBuilder.append("array" + arrayParameterPositions.size());
//							// Process an array.
//							arrayParameterPositions.add(a);
//							arrayParameterValues.add("null");
//						} else {
//							// Make sure the array to process contains constants only.
//							String nonArrayClassName = parameters[a].getClass().getCanonicalName().replace("[]", "");
//							if (nonArrayClassName.equals("de.wwu.muggl.solvers.expressions.Constant")) {
//								testMethodStringBuilder.append("array" + arrayParameterPositions.size());
//								// Process an array.
//								arrayParameterPositions.add(a);
//								Constant[] constants = (Constant[]) parameters[a];
//								// Is is an empty array?
//								if (constants.length == 0) {
//									String emptyArrayInitialization = "new " + parameterTypes[a - startAt];
//									emptyArrayInitialization =
//										emptyArrayInitialization.substring(0, emptyArrayInitialization.length() - 1)
//										+ "0" + emptyArrayInitialization.substring(emptyArrayInitialization.length() - 1);
//									arrayParameterValues.add(emptyArrayInitialization);
//								} else {
//									arrayParameterValues.add(constantArrayToInitializationString(constants));
//								}
//							} else {
//								// This array cannot be processed.
//								throw new SymbolicExecutionException("Cannot process an array that does not contain constant values. Aborting.");
//							}
//						}
//					} else if (parameterTypes[a - startAt].equals("boolean") && (parameters[a] instanceof BooleanConstant || parameters[a] instanceof IntConstant)) {
//						if (parameters[a] instanceof BooleanConstant) {
//							booleanParameters.addIfNotContained(((BooleanConstant) parameters[a]).getValue());
//							testMethodStringBuilder.append("boolean" + parameters[a].toString()); // No need to escape a boolean value.
//						} else {
//							Boolean value = ((IntConstant) parameters[a]).getValue() == 1 ? true : false;
//							booleanParameters.addIfNotContained(value);
//							testMethodStringBuilder.append("boolean" + value.toString());
//						}
//					} else if (parameterTypes[a - startAt].equals("byte") && parameters[a] instanceof IntConstant) {
//						byteParameters.addIfNotContained((byte) ((IntConstant) parameters[a]).getIntValue());
//						testMethodStringBuilder.append("byte" + escapeSignsUnsuitableForFields(((Byte) (byte) ((IntConstant) parameters[a]).getIntValue()).toString()));
//					} else if (parameterTypes[a - startAt].equals("char") && parameters[a] instanceof IntConstant) {
//						charParameters.addIfNotContained((char) ((IntConstant) parameters[a]).getIntValue());
//						testMethodStringBuilder.append("char" + escapeSignsUnsuitableForFields((char) ((IntConstant) parameters[a]).getIntValue()));
//					} else if (parameterTypes[a - startAt].equals("double") && parameters[a] instanceof DoubleConstant) {
//						Double value = ((DoubleConstant) parameters[a]).getDoubleValue();
//						doubleParameters.addIfNotContained(value);
//						testMethodStringBuilder.append("double");
//						if (value == Double.POSITIVE_INFINITY) {
//							testMethodStringBuilder.append("PositiveInfinity");
//						} else if (value == Double.NEGATIVE_INFINITY) {
//							testMethodStringBuilder.append("NegativeInfinity");
//						} else if (value.isNaN()) {
//							testMethodStringBuilder.append("NaN");
//						} else {
//							testMethodStringBuilder.append(escapeSignsUnsuitableForFields(value.toString()));
//						}
//					} else if (parameterTypes[a - startAt].equals("float") && parameters[a] instanceof FloatConstant) {
//						Float value = ((FloatConstant) parameters[a]).getFloatValue();
//						floatParameters.addIfNotContained(value);
//						testMethodStringBuilder.append("float");
//						if (value == Float.POSITIVE_INFINITY) {
//							testMethodStringBuilder.append("PositiveInfinity");
//						} else if (value == Float.NEGATIVE_INFINITY) {
//							testMethodStringBuilder.append("NegativeInfinity");
//						} else if (value == Float.NaN) {
//							testMethodStringBuilder.append("NaN");
//						} else {
//							testMethodStringBuilder.append(escapeSignsUnsuitableForFields(value.toString()));
//						}
//					} else if (parameterTypes[a - startAt].equals("int") && parameters[a] instanceof IntConstant) {
//						intParameters.addIfNotContained(((IntConstant) parameters[a]).getIntValue());
//						testMethodStringBuilder.append("int" + escapeSignsUnsuitableForFields(parameters[a].toString()));
//					} else if (parameterTypes[a - startAt].equals("long") && parameters[a] instanceof LongConstant) {
//						longParameters.addIfNotContained(((LongConstant) parameters[a]).getLongValue());
//						testMethodStringBuilder.append("long" + escapeSignsUnsuitableForFields(parameters[a].toString()));
//					} else if (parameterTypes[a - startAt].equals("short") && parameters[a] instanceof IntConstant) {
//						shortParameters.addIfNotContained((short) ((IntConstant) parameters[a]).getIntValue());
//						testMethodStringBuilder.append("short" + escapeSignsUnsuitableForFields(parameters[a].toString()));
//					} else {
//						// It has to be a reference value, or we encountered a mismatch.
//						if (parameters[a] == null || parameters[a] instanceof ReferenceValue) {
//							if (parameters[a] != null) {
//								// Check for assignment compatibility first.
//								ExecutionAlgorithms execution = new ExecutionAlgorithms(this.classLoader);
//								try {
//									if (!execution.checkForAssignmentCompatibility(parameters[a], parameterTypes[a - startAt], this.vm, false)) {
//										throw new SymbolicExecutionException("A parameter value is not assignment compatible to the expected parameter type. Aborting.");
//									}
//								} catch (ExecutionException e) {
//									throw new SymbolicExecutionException(e.getMessage());
//								}
//							}
//
//							testMethodStringBuilder.append("reference" + referenceValueCounter);
//							String initializationStringForReferenceValue = "";
//							// TODO: Implement a method to get suitable initialization Strings from the reference values' fields.
//							if (parameters[a] != null && parameters[a] instanceof Objectref && ((Objectref) parameters[a]).getInitializedClass().getClassFile().getName().equals("java.lang.String")) {
//								Field field = ((Objectref) parameters[a]).getInitializedClass().getClassFile().getFieldByNameAndDescriptor("value", "[C");
//								initializationStringForReferenceValue = "";
//								
//								Object stringValueObject = ((Objectref) parameters[a]).getField(field);
//								if(stringValueObject instanceof Arrayref) {
//									Arrayref stringValueObjectRef = (Arrayref)stringValueObject;
//									initializationStringForReferenceValue += "\"";
//									for(int c=0; c<stringValueObjectRef.length; c++) {
//										Object o = stringValueObjectRef.getElement(c);
//										if(o instanceof IntConstant) {
//											int asciiStringValue = ((IntConstant)o).getValue();
//											String asciiString = Character.toString((char)asciiStringValue);
//											initializationStringForReferenceValue += asciiString;
//										}
//									}
//									initializationStringForReferenceValue += "\"";
//								} else {
//									//TODO: dont know if this branch is ever reached...
//									Character[] characters = (Character[]) ((Objectref) parameters[a]).getField(field);
//									initializationStringForReferenceValue = "";
//									for (int b = 0; b < characters.length; b++) {
//										initializationStringForReferenceValue += characters[b].toString();
//									}
//								}
//							}
//							String parameterType = parameterTypes[a - startAt];
//							if (parameterType.endsWith("..."))
//								parameterType = parameterType.substring(0, parameterType.length() - 3) + "[]";
//
//							// Get the canonical name of the parameter type.
//							try {
//								parameterType = this.classLoader.getClassAsClassFile(parameterType).getCanonicalName();
//							} catch (ClassFileException e) {
//								// Simply ignore it.
//							}
//
//							// Construct the Strings.
//							referenceValueInstantiationStringBuilder.append("\tprivate " + parameterType + " reference" + referenceValueCounter + ";\r\n");
//							referenceValueInitializationStringBuilder.append("\t\tthis.reference" + referenceValueCounter + " = ");
//							if (parameters[a] != null) {
//								referenceValueInitializationStringBuilder.append("new " + ((ReferenceValue) parameters[a]).getInitializedClass().getClassFile().getCanonicalName() + "(" + initializationStringForReferenceValue + ");\r\n");
//							} else {
//								referenceValueInitializationStringBuilder.append("null;\r\n");
//							}
//							referenceValueCounter++;
//						} else {
//							throw new SymbolicExecutionException("A parameter type does not match the expected type. Aborting.");
//						}
//					}
//					firstPass = false;
//				}
//
//				// Finish the String for the method invocation.
//				if (!solution.getThrowsAnUncaughtException())
//					testMethodStringBuilder.append(")");
//
//				if (addDelta > 0) {
//					if (addDelta == 1) {
//						testMethodStringBuilder.append(", 0.000000001");
//					} else if (addDelta == 2) {
//						testMethodStringBuilder.append("0.00001f");
//					}
//				}
//
//				testMethodStringBuilder.append(");");
//				if (solution.getSolutionComment() != null) {
//					testMethodStringBuilder.append(" // " + solution.getSolutionComment());
//				}
//
//				testMethodStringBuilder.append("\r\n" + addAfterTheMethod);
//
//				// Continue with the next solution.
//				solution = solution.getSuccessor();
//
//				// Check if test case generation has been interrupted.
//				if (this.interrupted) {
//					this.testCaseGenerationAborted = true;
//					if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
//						Globals.getInst().symbolicExecLogger.info("Test case generation has been interrupted and is now aborted.");
//					return;
//				}
//			}

			// Get date and time information.
			SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM, yyyy HH:mm", Locale.ENGLISH);

			// Generate unit tests.
			StringBuilder fileContents = new StringBuilder("package " + Options.getInst().testClassesPackageName + ";\r\n\r\n"
				+ "import static org.junit.Assert.*;\r\n\r\n"

				+ "import javax.inject.Inject;\r\n"

				+ "import org.jboss.arquillian.container.test.api.Deployment;\r\n"
				+ "import org.jboss.arquillian.junit.Arquillian;\r\n"
				+ "import org.jboss.shrinkwrap.api.Archive;\r\n"
				+ "import org.jboss.shrinkwrap.api.ShrinkWrap;\r\n"
				+ "import org.jboss.shrinkwrap.api.asset.EmptyAsset;\r\n"
				+ "import org.jboss.shrinkwrap.api.spec.WebArchive;\r\n"
				+ "import org.junit.Before;\r\n"
				+ "import org.junit.Test;\r\n"
				+ "import org.junit.runner.RunWith;\r\n\r\n"
				+ "import de.wwu.javaee.test.runner.MugglExceptionMatcher;\r\n"

				+ "/**\r\n"
				+ " * This class has been generated by " + Globals.APP_NAME + " for the automated testing of method\r\n"
				+ " * " + this.initialMethod.getFullNameWithParameterTypesAndNames() + ".\r\n"
				+ " * Test cases have been computed using the symbolic execution of " + Globals.APP_NAME + ". " + Globals.APP_NAME + "\r\n"
				+ " * is a tool for the fully automated generation of test cases by analysing a\r\n"
				+ " * program's byte code. It aims at testing any possible flow through the program's\r\n"
				+ " * code rather than \"guessing\" required test cases, as a human would do.\r\n"
				+ " * Refer to http://www.wi.uni-muenster.de/pi/personal/majchrzak.php for more\r\n"
				+ " * information or contact the author at tim.majchrzak@wi.uni-muenster.de.\r\n"
				+ " * \r\n"
				+ " * Executing the method main(null) will invoke JUnit (if it is in the class path).\r\n"
				+ " * The methods for setting up the test and for running the tests have also been\r\n"
				+ " * annotated.\r\n"
				+ " * \r\n"
				+ " * Important settings for this run:\r\n"
				+ " * Search algorithm:            " + this.vm.getSearchAlgorithm().getName() + "\r\n"
				+ " * Time Limit:                  " + TimeSupport.computeRunningTime(Options.getInst().maximumExecutionTime * 1000, false) + "\r\n"
				+ " * Maximum loop cycles to take: ");
			if (Options.getInst().maximumLoopsToTake == -1) {
				fileContents.append("infinite");
			} else {
				fileContents.append(Options.getInst().maximumLoopsToTake);
			}
			fileContents.append("\r\n"
				+ " * Maximum instructions before\r\n"
				+ " * finding a new solution:     ");
				if (Options.getInst().maxInstrBeforeFindingANewSolution == -1) {
					fileContents.append("infinite");
				} else {
					fileContents.append(Options.getInst().maxInstrBeforeFindingANewSolution);
					if (Options.getInst().onlyCountChoicePointGeneratingInst)
						fileContents.append("\r\n"
							+ " * Only instructions that generate choice points have been counted.");
				}
			fileContents.append("\r\n");
			fileContents.append(" * Solver:                     " + this.vm.getSolverManager().getClass().getCanonicalName() + "\r\n");
			if (this.vm.isFinalized() || this.vm.getAbortionCriterionMatched()) {
				fileContents.append(" *\r\n"
							 + " * Execution has been aborted before it was finished.");
				if (this.vm.getAbortionCriterionMatchedMessage() != null)
					fileContents.append(" " + this.vm.getAbortionCriterionMatchedMessage());

				fileContents.append("\r\n"
							 + " * It might be possible to get more test cases by having less restrictive abortion\r\n"
							 + " * criteria.\r\n");

			}
			if (this.vm.getMaximumLoopsReached()) {
				fileContents.append(" * \r\n"
					+ " * The maximum number of loops was reached at least one time. Setting a higher\r\n"
					+ " * number of maximum loops to reach before backtracking might lead to a higher\r\n"
					+ " * number of solutions found.\r\n");
			}
			fileContents.append(" * \r\n"
				+ " * The total number of solutions found was " + this.numberOfSolutionsWithRedundancy + ". After deleting redundancy and\r\n"
				+ " * removing unnecessary solutions, " + this.numberOfSolutionsBeforeElimination + " distinct test cases were found.\r\n");
			if (Options.getInst().eliminateSolutionsByCoverage == 0) {
				fileContents.append(" * There was no further reduction of test cases.\r\n");
			} else {
				String contributeTo = "";
				switch (Options.getInst().eliminateSolutionsByCoverage) {
					case 1:
						contributeTo = "def-use chain coverage";
						break;
					case 2:
						contributeTo = "control graph edge coverage";
						break;
					case 3:
						contributeTo = "def-use chain and control graph edge coverage";
						break;
				}

				if (this.numberOfTestCases < this.numberOfSolutionsWithRedundancy) {
					fileContents.append(" * By eliminating solutions based on their contribution to the\r\n * " + contributeTo + "\r\n"
						+ " * the total number of solutions could be reduced by "
						+ (this.numberOfSolutionsWithRedundancy - this.numberOfTestCases) + " to the final number of "
						+ this.numberOfTestCases + " test cases.\r\n");
				} else {
					fileContents.append(" * It was tryed to eliminate more solutions based on their contribution to the\r\n * "
						+ contributeTo + "\r\nbut no further reduction was possible.\r\n");
				}
			}

			// Coverage information.
			if (Options.getInst().coverageTracking != 0 && testCaseElimination != null) {
				fileContents.append(" * \r\n"
						+ " * Covered def-use chains:\t\t"
						+ testCaseElimination.getTotalNumberOfDefUseChainsCovered() + " of "
						+ testCaseElimination.getTotalNumberOfDefUseChains() + "\r\n"
						+ " * Covered control graph edges:\t"
						+ testCaseElimination.getTotalNumberOfControlGraphEdgesCovered() + " of "
						+ testCaseElimination.getTotalNumberOfControlGraphEdges() + "\r\n");
			}

			
			String runWithArquillian = Options.getInst().jpaMode ? "@RunWith(Arquillian.class)\r\n" : "";
			
			String[] s = className.split("_");
			String abstractClassName = "Abstract"+className;
			if(s != null && s.length > 0) {
				abstractClassName = "Abstract"+className.split("_")[0];
			}
			
			
			
			// Final comments.
			fileContents.append(" * \r\n"
				+ " * This file has been generated on " + df.format(new Date()) + ".\r\n"
				+ " * \r\n"
				+ " * @author " + Globals.APP_NAME + " " + Globals.VERSION_MAJOR + "."
				+ Globals.VERSION_MINOR + " " + Globals.VERSION_RELEASE + "\r\n"
				+ " */\r\n"
				+ runWithArquillian
				+ "public class " + className + " extends "+abstractClassName+" {\r\n"
//				+ "\t// Fields for test parameters and expected return values.\r\n"
				+ referenceValueInstantiationStringBuilder.toString());

			// Generate the instantiation and initialization part.
			StringBuilder initializationStringBuilder = new StringBuilder();
			// Is there a instantiation of the tested class needed?
			if (!this.initialMethod.isAccStatic()) {
				if(Options.getInst().jpaMode) {
//					fileContents.append("\r\n\t@PersistenceContext\r\n\tprivate EntityManager em;\r\n\r\n");
//					fileContents.append("\t//@Inject\r\n\t//private de.wwu.pi.muggl.tr.MugglEntityManager em;\r\n\r\n");
					
					fileContents.append("\t@Inject\r\n\tprivate " + this.initialMethod.getClassFile().getName() + " testedClass;\r\n\r\n");
					
					fileContents.append("\t@Deployment\r\n");
					fileContents.append("\tpublic static Archive<?> createDeployment() {\r\n");
					fileContents.append("\t\treturn ShrinkWrap.create(WebArchive.class, \"test.war\")\r\n");
					fileContents.append("\t\t\t.addClasses(requiredApplicationClasses)\r\n");
					fileContents.append("\t\t\t.addAsResource(\"META-INF/persistence.xml\")\r\n");
					fileContents.append("\t\t\t.addAsWebInfResource(EmptyAsset.INSTANCE, \"beans.xml\");\r\n");
					fileContents.append("\t}\r\n\r\n");
				} else {
					fileContents.append("\tprivate " + this.initialMethod.getClassFile().getName() + " testedClass;\r\n");
					initializationStringBuilder.append("\t\tthis.testedClass = new " + this.initialMethod.getClassFile().getName() + "();\r\n"); // TODO: Check if initialization without parameters is ok. Probably parameters have to be specified.
				}
			}

			// Add the initialization of parameters and return values for primitive types.
			Iterator<Boolean> booleanIterator = booleanParameters.iterator();
			while (booleanIterator.hasNext()) {
				Boolean booleanObject = booleanIterator.next();
				fileContents.append("\tprivate boolean boolean" + booleanObject.toString() + ";\r\n");
				initializationStringBuilder.append("\t\tthis.boolean" + booleanObject.toString() + " = " + booleanObject.booleanValue() + ";\r\n");
			}
			Iterator<Byte> byteIterator = byteParameters.iterator();
			while (byteIterator.hasNext()) {
				Byte value = byteIterator.next().byteValue();
				String escapedValue = escapeSignsUnsuitableForFields(value.toString());
				fileContents.append("\tprivate byte byte" + escapedValue + ";\r\n");
				initializationStringBuilder.append("\t\tthis.byte " + escapedValue + " = " + value.byteValue() + ";\r\n");
			}
			Iterator<Character> charIterator = charParameters.iterator();
			while (charIterator.hasNext()) {
				Character charObject = charIterator.next();
				String escapedChar = escapeSignsUnsuitableForFields(charObject);
				fileContents.append("\tprivate char char" + escapedChar + ";\r\n");
				initializationStringBuilder.append("\t\tthis.char" + escapedChar + " = " + charObject.charValue() + ";\r\n");
			}
			Iterator<Double> doubleIterator = doubleParameters.iterator();
			while (doubleIterator.hasNext()) {
				Double value = doubleIterator.next().doubleValue();
				initializationStringBuilder.append("\t\tthis.double");
				String escapedValue;
				String valueString;

				if (value.isInfinite() || value.isNaN()) {
					if (value == Double.POSITIVE_INFINITY) {
						escapedValue = "PositiveInfinity";
						valueString = "Double.POSITIVE_INFINITY";
					} else if (value == Double.NEGATIVE_INFINITY) {
						escapedValue = "NegativeInfinity";
						valueString = "Double.NEGATIVE_INFINITY";
					} else {
						escapedValue = "NaN";
						valueString = "Double.NaN";
					}
				} else {
					escapedValue = escapeSignsUnsuitableForFields(value.toString());
					valueString = String.valueOf(value.doubleValue());
				}

				fileContents.append("\tprivate double double" + escapedValue + ";\r\n");
				initializationStringBuilder.append(escapedValue + " = " + valueString);
				initializationStringBuilder.append(";\r\n");
			}
			Iterator<Float> floatIterator = floatParameters.iterator();
			while (floatIterator.hasNext()) {
				Float value = floatIterator.next().floatValue();
				initializationStringBuilder.append("\t\tthis.float");
				String escapedValue;
				String valueString;

				if (value.isInfinite() || value.isNaN()) {
					if (value == Float.POSITIVE_INFINITY) {
						escapedValue = "PositiveInfinity";
						valueString = "Float.POSITIVE_INFINITY";
					} else if (value == Float.NEGATIVE_INFINITY) {
						escapedValue = "NegativeInfinity";
						valueString = "Float.NEGATIVE_INFINITY";
					} else {
						escapedValue = "NaN";
						valueString = "Float.NaN";
					}
				} else {
					escapedValue = escapeSignsUnsuitableForFields(value.toString());
					valueString = String.valueOf(value.floatValue());
				}

				fileContents.append("\tprivate float float" + escapedValue + ";\r\n");
				initializationStringBuilder.append(escapedValue + " = " + valueString);
				initializationStringBuilder.append(";\r\n");
			}
			Iterator<Integer> intIterator = intParameters.iterator();
			while (intIterator.hasNext()) {
				Integer value = intIterator.next().intValue();
				String escapedValue = escapeSignsUnsuitableForFields(value.toString());
				fileContents.append("\tprivate int int" + escapedValue + ";\r\n");
				initializationStringBuilder.append("\t\tthis.int" + escapedValue + " = " + value.intValue() + ";\r\n");
			}
			Iterator<Long> longIterator = longParameters.iterator();
			while (longIterator.hasNext()) {
				Long value = longIterator.next().longValue();
				String escapedValue = escapeSignsUnsuitableForFields(value.toString());
				fileContents.append("\tprivate long long" + escapedValue + ";\r\n");
				initializationStringBuilder.append("\t\tthis.long" + escapedValue + " = " + value.longValue() + ";\r\n");
			}
			Iterator<Short> shortIterator = shortParameters.iterator();
			while (shortIterator.hasNext()) {
				Short value = shortIterator.next().shortValue();
				String escapedValue = escapeSignsUnsuitableForFields(value.toString());
				fileContents.append("\tprivate short short" + escapedValue + ";\r\n");
				initializationStringBuilder.append("\t\tthis.Short" + escapedValue + " = " + value.shortValue() + ";\r\n");
			}
			Iterator<List<?>> listIterator = listParameters.iterator();
			while (listIterator.hasNext()) {
				List<?> value = listIterator.next();
				String escapedValue = escapeSignsUnsuitableForFields(Integer.toString(value.hashCode()));
				fileContents.append("\tprivate List<?> list" + escapedValue + ";\r\n");
				initializationStringBuilder.append("\t\tgenerateList"+escapedValue+"();\r\n");
			}
			// Add the array information.
			Iterator<Integer> arrayParameterPositionsIterator = arrayParameterPositions.iterator();
			Iterator<String> arrayParameterValuesIterator = arrayParameterValues.iterator();
			int a = 0;
			while (arrayParameterPositionsIterator.hasNext()) {
				// Add everything directly. There is no possibility to add the constant initialization Strings in the constructor.
				fileContents.append("\tprivate " + parameterTypes[arrayParameterPositionsIterator.next() - startAt]
				             + " array" + a + " = " + arrayParameterValuesIterator.next() + ";\r\n");
				a++;
			}
			
			// create the list generation help methods
			Iterator<List<?>> listIterator2 = listParameters.iterator();
			while (listIterator2.hasNext()) {
				List<?> value = listIterator2.next();
				String listName = Integer.toString(value.hashCode());
				jpaTestListGenerationMethodBuilder.append("\tprivate void generateList"+listName+"() {\r\n");
				jpaTestListGenerationMethodBuilder.append("\t\t//generate data for list\r\n");
				jpaTestListGenerationMethodBuilder.append("\t\tthis.list"+listName+" = new ArrayList<?>();\r\n");
				for(Object o : value) {
					jpaTestListGenerationMethodBuilder.append("\t\tthis.list"+listName+".add("+o.toString()+");\r\n");
				}
				jpaTestListGenerationMethodBuilder.append("\t}\r\n");
			}
			

			// Generate the further parts of the file.
			fileContents.append("\r\n"
				+ "\t/**\r\n"
				+ "\t * Set up the unit test by initializing the fields to the desired values.\r\n"
				+ "\t */\r\n"
				+ "\t@Before public void setUp() {\r\n"
				+ referenceValueInitializationStringBuilder.toString()
				+ initializationStringBuilder.toString()
				+ "\t}\r\n"
				+ jpaTestListGenerationMethodBuilder.toString()
				+ "\t/**\r\n"
				+ "\t * Run the tests on " + this.initialMethod.getClassFile().getName() + "." + this.initialMethod.getName() + "(" + this.initialMethod.getParameterTypesAndNames() + ").\r\n"
				+ "\t */\r\n"
				+ jpaTestCaseStringBuilder.toString()
//				+ "\t@Test public void testIt() {\r\n"
//				+ testMethodStringBuilder.toString()
//				+ databaseResultStringBuilder.toString()
//				+ "\t}\r\n\r\n"
//				+ "\t/**\r\n"
//				+ "\t * Invoke JUnit to run the unit tests.\r\n"
//				+ "\t * @param args Command line arguments, which are ignored here. Just supply null.\r\n"
//				+ "\t */\r\n"
//				+ "\tpublic static void main(String args[]) {\r\n"
//				+ "\t\torg.junit.runner.JUnitCore.main(\"" + Options.getInst().testClassesPackageName + "." + className + "\");\r\n"
//				+ "\t}\r\n\r\n"
				+ "}");

			// Check if test case generation has been interrupted. This is the last chance.
			if (this.interrupted) {
				this.testCaseGenerationAborted = true;
				if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
					Globals.getInst().symbolicExecLogger.info("Test case generation has been interrupted and is now aborted.");
				return;
			}

			// Write to the java file.
			File file = new File(testCaseFile);
			try {
				FileWriter out = new FileWriter(file);
				out.write(fileContents.toString());
				out.close();
			} catch (IOException e) {
				if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
					Globals.getInst().symbolicExecLogger.warn("Test cases have been generated but could not be written to a file. There was a unexpected IOException after generating the file. The message is " + e.getMessage());
			}

			// Finished - log that.
			this.generatedClassFilePath = testCaseFile;
			this.testCaseGenerationFinished = true;
			if (Globals.getInst().symbolicExecLogger.isInfoEnabled())
				Globals.getInst().symbolicExecLogger.info("Successfully wrote " + this.numberOfTestCases + " test cases to " + testCaseFile + ".");
		} catch (Throwable t) {
			t.printStackTrace();
			// Something went wrong.
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
				Globals.getInst().symbolicExecLogger.warn(
						"Test case generation failed. Root reason: " + t.getClass().getName()
							+ " (" + t.getMessage() + ")");
			this.causeOfFailure = t;
			this.testCaseGenerationFailed = true;
			this.testCaseGenerationFinished = true;
		}
	}

	/**
	 * Variable names are generated with regard to their values, so that these names are
	 * "speeking" and therefore hinting to their value. However, not all values string
	 * representations are suitable for beeing used as field names within java classes.
	 * This method will escape them so they are still readble for the user, but can be
	 * used as field names.
	 *
	 * Please note: This method can be used for values of type boolean, byte, double,
	 * float, integer, long and short. It can not be used to values if type char. Use
	 * escapeSignsUnsuitableForFields(char toEscape) instead.
	 *
	 * The detailed replacement strategy:
	 * - A leading - (minues sign) will be replaced with the character m.
	 * - Any dots or commas (. or ,) will be replaced by underlines (_).
	 *
	 * @param toEscape The String that will be checked for signs that need to be escaped.
	 * @return The processed String that can be used as a field name.
	 */
	private String escapeSignsUnsuitableForFields(String toEscape) {
		// Escaping.
		if (toEscape.contains("-")) toEscape = toEscape.replace("-", "m");
		if (toEscape.contains(".")) toEscape = toEscape.replace(".", "_");
		if (toEscape.contains(",")) toEscape = toEscape.replace(",", "_");

		// Return the String.
		return toEscape;
	}

	/**
	 * Please refer to the above method (escapeSignsUnsuitableForFields(String toEscape))
	 * for an introduction.
	 *
	 * This method only escapes single values of type char. It will escape a lot
	 * of common signs that will not work as field names. It however might not be
	 * complete.
	 *
	 * @param toEscape The char that will be checked if it needs to be escaped.
	 * @return A String representation of the char that can be used as a field name.
	 */
	private String escapeSignsUnsuitableForFields(char toEscape) {
		// Create a new String.
		String escaped = Character.toString(toEscape);
		String[] escapedSplit = escaped.split("");
		escaped = "";
		// Escaping.
		for (String sign : escapedSplit) {
			if (sign.equals("-")) 		sign = "_minus_";
			else if (sign.equals(".")) 	sign = "_dot_";
			else if (sign.equals(",")) 	sign = "_comma_";
			else if (sign.equals("\\")) sign = "_backslash_";
			else if (sign.equals("=")) 	sign = "_equal_";
			else if (sign.equals("+")) 	sign = "_plus_";
			else if (sign.equals("*")) 	sign = "_asterisk_";
			else if (sign.equals("/")) 	sign = "_slash_";
			else if (sign.equals("%")) 	sign = "_modulo_";
			else if (sign.equals("!")) 	sign = "_exclamationMark_";
			else if (sign.equals("<")) 	sign = "_lessThan_";
			else if (sign.equals(">")) 	sign = "_greaterThan_";
			else if (sign.equals("&")) 	sign = "_and_";
			else if (sign.equals("|")) 	sign = "_or_";
			else if (sign.equals("^")) 	sign = "_exponential_";
			else if (sign.equals("~")) 	sign = "_tilde_";
			else if (sign.equals("(")) 	sign = "_parenthesisOpen_";
			else if (sign.equals(")")) 	sign = "_parenthesisClosed_";
			else if (sign.equals("?")) 	sign = "_questionMark_";
			else if (sign.equals("\"")) sign = "_doublequote_";
			else if (sign.equals("'")) 	sign = "_quote_";
			else if (sign.equals("{")) 	sign = "_braceOpen_";
			else if (sign.equals("}")) 	sign = "_braceClosed_";
			else if (sign.equals("[")) 	sign = "_suaqreBracketOpen_";
			else if (sign.equals("]")) 	sign = "_suaqreBracketClosed_";
			else if (sign.equals("�")) 	sign = "_paragraph_";
			else if (sign.equals("#")) 	sign = "_hash_";
			else if (sign.equals("�")) 	sign = "_degreeSymbol_";
			else if (sign.equals("�")) 	sign = "_square2_";
			else if (sign.equals("�")) 	sign = "_square3_";
			else if (sign.equals(";")) 	sign = "_semicolon_";
			else if (sign.equals(":")) 	sign = "_colon_";
			escaped += sign;
		}

		// Return the String.
		return escaped;
	}

	/**
	 * Recursively process an array of Constant and convert it into an
	 * initialization String.
	 *
	 * Example: The input array haqs two dimensions with two elements each.
	 * The elements values are
	 * [0][0] = 1
	 * [0][1] = 7
	 * [1][0] = 3
	 * [1][1] = 5
	 * The resulting initialization String will be {{1,7},{3,5}}.
	 *
	 * @param constants The array of constant values to be processed.
	 * @return The initialization String.
	 */
	private String constantArrayToInitializationString(Constant[] constants) {
		if (constants != null && constants.length > 0) {
			String initializationString = "";
			// Check if another array is contained.
			if (constants[0].getClass().isArray()) {
				//  Process it recursively.
				for (int a = 0; a < constants.length; a++) {
					Object constantsObject = constants[a];
					initializationString += constantArrayToInitializationString((Constant[]) constantsObject);
				}
			} else {
				// Process every element.
				for (int a = 0; a < constants.length; a++) {
					if (initializationString.length() > 0) initializationString += ",";
					initializationString += constants[a].toString();
				}
			}
			initializationString = "{" + initializationString + "}";

			// Return the String.
			return initializationString;
		}
		// There is nothing in it.
		return "";
	}

	/**
	 * Getter for the generatedClassFilePath. This String stores the path of the class
	 * file written on succesful symbolic execution of a method.
	 * @return The generatedClassFilesPath, or null, if execution is either not finished or failed.
	 */
	public String getGeneratedClassFilePath() {
		return this.generatedClassFilePath;
	}

	/**
	 * Getter for the number of solutions added.
	 * @return The number of solutions added.
	 */
	public long getNumberOfSolutions() {
		if (this.firstSolutionFound == null) return 0L;
		if (this.testCaseGenerationStarted)
			return this.numberOfSolutionsBeforeElimination;
		return this.firstSolutionFound.getNumberOfSolutions();
	}

	/**
	 * Getter for the information whether the Solution found next should be saved, or not.
	 * @return true, if the Solution found next should be solved, false otherwise.
	 */
	public boolean getDoNotSaveTheNextSolution() {
		return this.doNotSaveTheNextSolution;
	}

	/**
	 * Setter for the information whether the Solution found next should be saved, or not.
	 * @param doNotSaveTheNextSolution true for saving, false otherwise.
	 */
	public void setDoNotSaveTheNextSolution(boolean doNotSaveTheNextSolution) {
		this.doNotSaveTheNextSolution = doNotSaveTheNextSolution;
	}

	/**
	 * Return the newest Solution found. Before returning the Solution, mark that no new Solution was be
	 * found. So the next query for a new Solution will return false, unless actually a new Solution
	 * was found.
	 * @return The newest Solution found.
	 */
	public TestCaseSolution getNewestSolution() {
		this.foundSolution = false;
		return this.latestSolutionFound;
	}

	/**
	 * Reset the found solutions.
	 * @throws IllegalStateException If the method is invoked after test generation was started.
	 */
	public void resetFoundSolutionsAndReturnValues() {
		if (this.testCaseGenerationStarted) {
			throw new IllegalStateException("Test generation has started already. Cannot reset the found solutions.");
		}
		this.firstSolutionFound = null;
		this.latestSolutionFound = null;
		this.foundSolution = false;
		this.newestSolutionNumber = -1;
	}

	/**
	 * Getter for foundSolution.
	 * @return true, if a solution has been found, false otherwise.
	 */
	public boolean hasFoundSolution() {
		return this.foundSolution;
	}

	/**
	 * Getter for newestSolutionNumber.
	 * @return The newest solutions' number.
	 */
	public int getNewestSolutionNumber() {
		return this.newestSolutionNumber;
	}

	/**
	 * Getter for the first Solution found.
	 * @return The first Solution found.
	 */
	public TestCaseSolution getFirstSolution() {
		return this.firstSolutionFound;
	}

	/**
	 * Getter for the information whether test case generation has been started, or not.
	 * @return true, if test case generation has been started; false otherwise.
	 */
	public boolean hasTestCaseGenerationStarted() {
		return this.testCaseGenerationStarted;
	}

	/**
	 * Getter for the information whether test case generation has finished, or not.
	 * @return true, if test case generation has been finished; false otherwise.
	 */
	public boolean hasTestCaseGenerationFinished() {
		return this.testCaseGenerationFinished;
	}

	/**
	 * Getter for the information whether test case generation has been aborted, or not.
	 * @return true, if test case generation has been aborted; false otherwise.
	 */
	public boolean hasTestCaseGenerationAborted() {
		return this.testCaseGenerationAborted;
	}

	/**
	 * Getter for the information whether test case generation has failed, or not.
	 * @return true, if test case generation has failed; false otherwise.
	 */
	public boolean hasTestCaseGenerationFailed() {
		return this.testCaseGenerationFailed;
	}

	/**
	 * Get the Throwable that stopped the test case generation. Be sure to check if test case
	 * generation actually failed.
	 *
	 * @return The Throwable that stopped the test case generation
	 * @throws IllegalStateException If test case generation is not finished or is finished, but did not fail.
	 * @see #hasTestCaseGenerationFailed()
	 */
	public Throwable getCauseOfFailure() {
		if (!this.testCaseGenerationFinished)
			throw new IllegalStateException("Test generation is not finished.");
		if (!this.testCaseGenerationFailed)
			throw new IllegalStateException("Test generation has been finished successfully.");
		return this.causeOfFailure;
	}

	/**
	 * Return information about the elimination of test cases. The returned long array
	 * has three dimensions:
	 * <ul>
	 * <li>The first one is the number of solutions supplied to the solution processor.</li>
	 * <li>The second one is the number of solutions that are not redundant but that have not
	 *     undergone the elimination process.</li>
	 * <li>The third one is the number of solutions that are actually used for test cases.</li>
	 * </ul>
	 * If any number is -1 it has not been calculated, yet.
	 *
	 * @return Information about the elimination of test cases.
	 * @throws IllegalStateException If test case generation has not been started, yet.
	 *
	 * @see #hasFinishedDeletingRedudancy()
	 * @see #hasFinishedElimination()
	 */
	public long[] getTestCaseEliminationFigures() {
		if (!this.testCaseGenerationStarted)
			throw new IllegalStateException("Test generation has not yet started.");
		long[] figures = {this.numberOfSolutionsWithRedundancy,
				this.numberOfSolutionsBeforeElimination,
				this.numberOfTestCases};
		return figures;
	}

	/**
	 * Getter for the information whether clearing the solution of redundancy was finished,
	 * or not.
	 * @return true, if clearing the solution of redundancy was finished, false otherwise.
	 */
	public boolean hasFinishedDeletingRedudancy() {
		return this.finishedDeletingRedudancy;
	}

	/**
	 * Getter for the information whether solution elimination was finished, or not.
	 * @return true, if solution elimination was finished, false otherwise.
	 */
	public boolean hasFinishedElimination() {
		return this.finishedElimination;
	}

	/**
	 * Get the number of the solution currently processed while deleting redundancy from the solutions. The
	 * value only has any meaning while the test case generation has been started and deleting redundancy is
	 * not finished, yet. In any other case, -1 will be returned.
	 *
	 * @return The number of the solution currently processed while deleting redundancy, or -1 if not
	 *         currently deleting redundancy.
	 */
	public long getDeletingRedudancySolutionCurrentlyProcessed() {
		return this.deletingRedudancySolutionCurrentlyProcessed;
	}

	/**
	 * Set the number of the solution currently processed while deleting redundancy from the solutions.
	 *
	 * This method has package visibility only and is meant to be used by TestCaseSolution instances.
	 *
	 * @param deletingRedudancySolutionCurrentlyProcessed The number of the solution currently processed.
	 */
	void setDeletingRedudancySolutionCurrentlyProcessed(
			long deletingRedudancySolutionCurrentlyProcessed) {
		this.deletingRedudancySolutionCurrentlyProcessed = deletingRedudancySolutionCurrentlyProcessed;
	}

	/**
	 * Get the number of solutions already dropped while eliminating test cases based on the solutions'
	 * contribution to the coverages. The value only has any meaning while the test case generation has been
	 * started and test case elimination is not finished, yet. In any other case, -1 will be returned.
	 *
	 * @return The number of solutions already dropped while eliminating test cases, or -1 if not currently
	 *         eliminating test cases.
	 */
	public long getEliminationSolutionsDropped() {
		return this.eliminationSolutionsDropped;
	}

	/**
	 * Increase the number of solutions already dropped while eliminating test cases based on the
	 * solutions' contribution to the coverages by one.
	 *
	 * This method has package visibility only and is meant to be used by TestCaseSolution instances.
	 */
	void increaseEliminationSolutionsDropped() {
		this.eliminationSolutionsDropped++;
	}

	/**
	 * Get the number of solutions already kept while eliminating test cases based on the solutions'
	 * contribution to the coverages. The value only has any meaning while the test case generation has been
	 * started and test case elimination is not finished, yet. In any other case, -1 will be returned.
	 *
	 * @return The number of solutions already kept while eliminating test cases, or -1 if not currently
	 *         eliminating test cases.
	 */
	public long getEliminationSolutionsKept() {
		return this.eliminationSolutionsKept;
	}

	/**
	 * Increase the number of solutions already kept while eliminating test cases based on the solutions'
	 * contribution to the coverages by one.
	 *
	 * This method has package visibility only and is meant to be used by TestCaseSolution instances.
	 */
	void increaseEliminationSolutionsKept() {
		this.eliminationSolutionsKept++;
	}

	/**
	 * Interrupt processing of test cases. This method must not be called unless the actual
	 * processing has been started. Processing will not stop immediately but at the nearest
	 * point where the test case generation algorithm or any of its utilized algorithms
	 * check whether they should continue, or not.
	 *
	 * @throws IllegalStateException If test case generation has not been started, yet.
	 */
	public void interrupt() {
		if (!this.testCaseGenerationStarted)
			throw new IllegalStateException("Test generation has not yet started.");
		this.interrupted = true;
	}

	/**
	 * Check if the test case generation has been interrupted.
	 *
	 * This method has package visibility as it is only meant to be used by utilized test case
	 * generation algorithms.
	 *
	 * @return true, if test case generation has been interupted; false if it has not or has not yet started.
	 */
	boolean isInterrupted() {
		return this.interrupted;
	}

}
