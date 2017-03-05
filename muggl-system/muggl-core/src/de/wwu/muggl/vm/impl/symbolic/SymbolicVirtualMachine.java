package de.wwu.muggl.vm.impl.symbolic;

import java.util.Iterator;
import java.util.Stack;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.db.list.ISymbolicResultList;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.LCmp;
import de.wwu.muggl.instructions.bytecode.Newarray;
import de.wwu.muggl.instructions.general.CompareFp;
import de.wwu.muggl.instructions.general.GeneralInstructionWithOtherBytes;
import de.wwu.muggl.instructions.general.If_acmp;
import de.wwu.muggl.instructions.general.Invoke;
import de.wwu.muggl.instructions.general.Load;
import de.wwu.muggl.instructions.general.Switch;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.control.JumpConditional;
import de.wwu.muggl.instructions.jpa.JPAInvokeInterface;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.symbolic.flow.coverage.CoverageController;
import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.searchAlgorithms.SearchAlgorithm;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.database.EntityManagerFindChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.database.EntityManagerPersistChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.DepthFirstSearchAlgorithm;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.StackToTrail;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.FrameChange;
import de.wwu.muggl.symbolic.searchAlgorithms.iterativeDeepening.IterativeDeepeningSearchAlgorithm;
import de.wwu.muggl.symbolic.structures.Loop;
import de.wwu.muggl.symbolic.testCases.SolutionProcessor;
import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.Limitations;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ConversionException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ForwardingUnsuccessfulException;
import de.wwu.muggl.vm.execution.NativeWrapper;
import de.wwu.muggl.vm.execution.ResolutionAlgorithms;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.db.SymbolicQueryResultListConstraintManager;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.vm.var.sym.SymbolicQueryResultList;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.type.IObjectreference;

/**
 * This concrete class represents a virtual machine for the symbolic execution of java bytecode,
 * especially focusing on the generation of test cases. It inherits methods from the abstract
 * implementation, providing additional methods as well as overriding others.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-08
 */
public class SymbolicVirtualMachine extends VirtualMachine {
	// The Solver Manager.
	protected SolverManager			solverManager;

	// The search algorithm.
	protected final SearchAlgorithm	searchAlgorithm;
	protected boolean				doNotTryToTrackBack;

	// Coverage Controller.
	protected CoverageController		coverage;
	protected boolean					trackCoverage;

	// Solution related fields.
	protected SolutionProcessor		solutionProcessor;
	protected boolean					doNotProcessSolutions;

	// Fields for the execution time measured.
	protected boolean					measureExecutionTime;
	protected long					timeExecutionInstruction;
	protected long					timeLoopDetection;
	protected long					timeCoverageChecking;
	protected long					timeChoicePointGeneration;
	protected long					timeBacktracking;
	protected long					timeSolvingChoicePoints;
	protected long					timeSolvingBacktracking;
	protected long					timeSolutionGeneration;
	// Temporary fields for the time measuring.
	protected long					timeExecutionInstructionTemp;
	protected long					timeLoopDetectionTemp;
	protected long					timeCoverageCheckingTemp;
	protected long					timeSolutionGenerationTemp;

	// Fields for counting the instructions executed since the last solution was found.
	protected int						maximumInstructionsBeforeFindingANewSolution;
	protected boolean					onlyCountChoicePointGeneratingInstructions;
	protected int						instructionsExecutedSinceLastSolution;

	/*
	 * Field for statistical information on generation.
	 */
	protected long 					arraysGeneratorsUsed;
	protected long					arraysGenerated;
	
	/*
	 * Fields to indicate if and if yes, why the execution did not finish without matching an
	 * abortion criterion.
	 */
	protected boolean					abortionCriterionMatched;
	protected String					abortionCriterionMatchedMessage;
	protected boolean					maximumLoopsReached;

	// Constant.
	protected static final long		NANOS_MILLIS	= 1000000;
	
	
	protected SymbolicQueryResultListConstraintManager symQryResConstraintManager;

	/**
	 * Basic constructor, which initializes the additional fields.
	 * 
	 * @param application The application this virtual machine is used by.
	 * @param classLoader The main ClassLoader to use.
	 * @param classFile The classFile to start execution with.
	 * @param initialMethod The Method to start execution with. This Method has to be a method of
	 *        the supplied classFile.
	 * @throws InitializationException If initialization of auxiliary classes fails.
	 */
	public SymbolicVirtualMachine(Application application, MugglClassLoader classLoader,
			ClassFile classFile, Method initialMethod) throws InitializationException {
		this(application, classLoader, classFile, initialMethod, selectSearchAlgorithm());
		this.solutionProcessor = new SolutionProcessor(this, this.classLoader, this.initialMethod);
	}

	/**
	 * Special constructor, which sets the search algorithm and initializes the other fields. It is
	 * used to start a "fresh" symbolic virtual machine during execution and start from scratch. The
	 * virtual machine specified is succeeded by it. The solutions found in the earlier execution
	 * and old statistical records are imported. If also the measuring of execution time is enabled,
	 * the old execution times are imported.
	 * 
	 * @param searchAlgorithm The search algorithm used.
	 * @param succeededSVM The SymbolicalVirtualMachine that is succeeded by this one.
	 * @throws InitializationException If initialization of auxiliary classes fails.
	 * @throws NullPointerException If succeededSVM is null.
	 */
	public SymbolicVirtualMachine(SearchAlgorithm searchAlgorithm,
			SymbolicVirtualMachine succeededSVM) throws InitializationException {
		this(succeededSVM.getApplication(), succeededSVM.getClassLoader(), succeededSVM
				.getClassFile(), succeededSVM.getInitialMethod(), searchAlgorithm);
		this.solutionProcessor = new SolutionProcessor(this, this.classLoader, this.initialMethod);
		
		// Import outcomes of the former execution.
		this.solutionProcessor.addFirstSolutionFoundEalier(succeededSVM.getSolutionProcessor()
				.getFirstSolution());
		this.executedFrames = succeededSVM.getExecutedFrames();
		this.executedInstructions = succeededSVM.getExecutedInstructions();
		if (Options.getInst().measureSymbolicExecutionTime) {
			long[] executionTimes = succeededSVM.getNanoExecutionTimeInformation();
			this.timeExecutionInstruction = executionTimes[0];
			this.timeLoopDetection = executionTimes[1];
			this.timeCoverageChecking = executionTimes[2];
			this.timeChoicePointGeneration = executionTimes[3];
			this.timeBacktracking = executionTimes[4];
			this.timeSolvingChoicePoints = executionTimes[5];
			this.timeSolvingBacktracking = executionTimes[6];
			this.timeSolutionGeneration = executionTimes[7];
		}
	}

	/**
	 * Main constructor, which initializes the additional fields. It has to be invoked by any public
	 * constructor.
	 * 
	 * @param application The application this virtual machine is used by.
	 * @param classLoader The main ClassLoader to use.
	 * @param classFile The classFile to start execution with.
	 * @param initialMethod The Method to start execution with. This Method has to be a method of
	 *        the supplied classFile.
	 * @param searchAlgorithm The search algorithm used.
	 * @throws InitializationException If initialization of auxiliary classes fails.
	 */
	private SymbolicVirtualMachine(Application application, MugglClassLoader classLoader,
			ClassFile classFile, Method initialMethod, SearchAlgorithm searchAlgorithm)
			throws InitializationException {
		super(application, classLoader, classFile, initialMethod);
		Options options = Options.getInst();
		try {
			this.solverManager = (SolverManager) Class.forName(options.solverManager).newInstance();
		} catch (InstantiationException e) {
			throw new InitializationException("Solver manager of class " + options.solverManager + " cannot be instantiated.");
		} catch (IllegalAccessException e) {
			throw new InitializationException("Solver manager of class " + options.solverManager + " cannot be accessed.");
		} catch (ClassNotFoundException e) {
			throw new InitializationException("Solver manager of class " + options.solverManager + " does not exist.");
		}
		this.symQryResConstraintManager = new SymbolicQueryResultListConstraintManager();
		this.searchAlgorithm = searchAlgorithm;
		this.coverage = new CoverageController(this);
		this.trackCoverage = options.useCFCoverage && options.useDUCoverage;
		this.stack = new StackToTrail<Object>(true, this.searchAlgorithm);
		this.doNotTryToTrackBack = false;
		this.doNotProcessSolutions = false;
		this.measureExecutionTime = options.measureSymbolicExecutionTime;
		this.timeExecutionInstruction = 0;
		this.timeLoopDetection = 0;
		this.timeCoverageChecking = 0;
		this.timeChoicePointGeneration = 0;
		this.timeBacktracking = 0;
		this.timeSolvingChoicePoints = 0;
		this.timeSolvingBacktracking = 0;
		this.timeSolutionGeneration = 0;
		this.instructionsExecutedSinceLastSolution = 0;
		this.arraysGeneratorsUsed = 0L;
		this.arraysGenerated = 0L;
		this.maximumInstructionsBeforeFindingANewSolution = options.maxInstrBeforeFindingANewSolution;
		this.onlyCountChoicePointGeneratingInstructions = options.onlyCountChoicePointGeneratingInst;
		this.abortionCriterionMatched = false;
		this.maximumLoopsReached = false;
		this.abortionCriterionMatchedMessage = null;
	}

	/**
	 * Select the supply the search algorithm based on the current setting.
	 * 
	 * @return An instance of SearchAlgorithm.
	 */
	private static SearchAlgorithm selectSearchAlgorithm() {
		SearchAlgorithm searchAlgorithm;
		if (Options.getInst().searchAlgorithm == 2) {
			searchAlgorithm = new IterativeDeepeningSearchAlgorithm(
					Options.getInst().iterativeDeepeningStartingDepth,
					Options.getInst().iterativeDeepeningDeepnessIncrement);
		} else if (Options.getInst().searchAlgorithm == 1) {
			searchAlgorithm = new DepthFirstSearchAlgorithm();
		} else {
			searchAlgorithm = new DepthFirstSearchAlgorithm();
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
				Globals.getInst().symbolicExecLogger
						.warn("The breadth first search algorithm is currently not implemented. Using depth first instead.");
		}
		if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
			Globals.getInst().symbolicExecLogger.trace("Using search algorithm: "
					+ searchAlgorithm.getName());
		return searchAlgorithm;
	}

	/**
	 * The main loop on the SymbolicalVirtualMachine executes the VirtualMachines main loop; it then
	 * saves the found solutions, checks weather a tracking back is possible and continues from that
	 * point on, again using the normal VirtualMachines main loop. It keeps doing so, until tracking
	 * back is no longer possible.
	 * 
	 * @param visualStartingFrame If step by step execution is enabled, there will be no stepping
	 *        until this frame has been reached.
	 * @throws ExecutionException An ExecutionExeption is thrown on any fatal errors during
	 *         execution.
	 * @throws InterruptedException Thrown to signal the manual end of the step by step execution.
	 * @throws InvalidInstructionInitialisationException Any fatal problems with the parsing and the
	 *         initialization will lead to this exception.
	 * @throws OutOfMemoryError If the virtual machine runs out for memory, this error is thrown.
	 * @throws StackOverflowError If the operand stack of a frame or the virtual machine stack
	 *         exceeds its element limit, this Error is thrown.
	 */
	@Override
	protected void runMainLoop(Frame visualStartingFrame) throws ExecutionException,
			InterruptedException, InvalidInstructionInitialisationException {
		boolean firstRun = true;
		// Symbolic loop - the end of the program flow is not necessarily the end of the execution.
		while (true) {
			try {                
				if(!firstRun && this.searchAlgorithm.getTrackBackExpectedException() != null) {
					this.threwAnUncaughtException = true;
					this.returnedObject = this.searchAlgorithm.getTrackBackExpectedException();
				} else {
					// Run the program.
					if (firstRun) {
						super.runMainLoop(visualStartingFrame);
						firstRun = false;
					} else {
						super.runMainLoop(this.currentFrame);
					}
				}
			} catch (NoExceptionHandlerFoundException e) {
				/*
				 * The only exception reaching this point and not halting the virtual machine is the
				 * NoExceptionHandlerFoundException. It indicates that an Exception was thrown by
				 * the executed program which would stop the execution of it. At this point is means
				 * that a set of parameters was found that will result in the application throwing
				 * an uncaught Exception. This forms a solution!
				 */
				this.threwAnUncaughtException = true;
				// The return object is used to store the NoExceptionHandlerFoundException
				// containing the uncaught throwable as there is no returned value anyway.
				this.returnedObject = e;
				firstRun = false;
			} catch (InterruptedException e) {
				// Mark that the actual execution has finished.
				this.application.executionHasFinished();

				// Logging.
				if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.INFO))
					Globals.getInst().symbolicExecLogger
							.info("The virtual machine was halted with an InterruptionException. "
									+ "Trying to generate test cases.");

				/*
				 * Execution has been interrupted. This happens if it is aborted externally, for
				 * example if a limit has been reached. Before rethrowing the exception, try to
				 * generate test cases.
				 */
				if (!this.doNotProcessSolutions
						&& this.solutionProcessor.getNumberOfSolutions() != 0L) {
					this.solutionProcessor.generateTestCases();
				} else {
					if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.INFO))
						Globals.getInst().symbolicExecLogger
								.info("No test cases will be generated.");
				}

				// Rethrow.
				throw e;
			}

			/*
			 * If this point is reached, a new solution was found. Save it if it is supposed to be
			 * saved.
			 */
			if (this.solutionProcessor.getDoNotSaveTheNextSolution()) {
				this.solutionProcessor.setDoNotSaveTheNextSolution(false);
			} else {
				if (this.measureExecutionTime) this.timeSolutionGenerationTemp = System.nanoTime();

//				if(this instanceof JPAVirtualMachine) {
//					// add query result list constraints, e.g., by its WHERE restrictions
//					for(ISymbolicResultList symbolicResult : ((JPAVirtualMachine)this).getVirtualObjectDatabase().getSymbolicQueryResultLists()) {
//						SymbolicQueryResultList res = (SymbolicQueryResultList)symbolicResult;
//						this.symQryResConstraintManager.addQueryConstraints((JPAVirtualMachine)this, res);
//					}
//				}
				
				saveSolution();
				if (this.measureExecutionTime)
					this.timeSolutionGeneration += System.nanoTime()
							- this.timeSolutionGenerationTemp;
			}
			// Track back if desired and possible.
			if (this.doNotTryToTrackBack || !this.searchAlgorithm.trackBack(this)) break;
		}

		/*
		 * After finishing, process the solutions and generate test cases from them. However, only
		 * do this if this virtual machine has not set not to process them. There might be
		 * circumstances by that it becomes finalized before execution really is finished. In this
		 * cases, not processing the solutions might be chosen. An example is the iterative
		 * deepening depth first algorithm: If it increases the maximum search depth, a new symbolic
		 * virtual machine is initialized and execution started from scratch. It will manually call
		 * the finalize() method of this symbolic virtual machine. In this case, execution just has
		 * to be finished here, and no generation of test cases is needed.
		 */
		if (!this.doNotProcessSolutions) {
			/*
			 * Mark that the actual execution has finished. Generating the test cases might take a
			 * while. However, this is not part of the execution.
			 */
			this.application.executionHasFinished();
			// Generate the test cases.
			String className = this.currentFrame.getMethod().getClassFile().getClassName();
			String methodName = this.currentFrame.getMethod().getName();
			Options.getInst().testClassesName = className + "Test_" + methodName;
			this.solutionProcessor.generateTestCases();
		}
	}

	/**
	 * Execute the current frame. It is first checked for loops, then the super implementation is
	 * invoked.
	 * 
	 * @param allowStepping If set to false there will not be no stepping even if step by step mode
	 *        is enabled. This is used to skip static initializers.
	 * @throws ExecutionException An ExecutionExeption is thrown on any fatal errors during
	 *         execution.
	 * @throws InterruptedException Thrown to signal the manual end of the step by step execution.
	 * @throws InvalidInstructionInitialisationException Any fatal problems with the parsing and the
	 *         initialization will lead to this exception.
	 */
	@Override
	protected void executeFrame(boolean allowStepping) throws ExecutionException,
			InterruptedException, InvalidInstructionInitialisationException {
		// Check this frame for loops.
		if (this.measureExecutionTime) this.timeLoopDetectionTemp = System.nanoTime();
		detectLoops();
		if (this.measureExecutionTime)
			this.timeLoopDetection += System.nanoTime() - this.timeLoopDetectionTemp;

		// Invoke the super implementation.
		super.executeFrame(allowStepping);
	}

	/**
	 * This concrete method executes the given instruction symbolically.
	 * 
	 * @param instruction The instruction that is to be executed.
	 * @throws ExecutionException Thrown on any fatal errors during symbolic execution.
	 */
	@Override
	protected void executeInstruction(Instruction instruction) throws ExecutionException {
		// If the number of instructions before finding a new solution is limited, check them now.
		// No need to do that if no more tracking back is desired.
		if (!this.doNotTryToTrackBack && this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.measureExecutionTime) this.timeExecutionInstructionTemp = System.nanoTime();
			if (!this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
			if (this.instructionsExecutedSinceLastSolution > this.maximumInstructionsBeforeFindingANewSolution) {
				// Abort the current execution.
				if (Globals.getInst().symbolicExecLogger.isDebugEnabled())
					Globals.getInst().symbolicExecLogger
							.debug("Aborted execution as no new solution was found after executing "
									+ this.maximumInstructionsBeforeFindingANewSolution
									+ " instructions.");
				this.abortionCriterionMatched = true;
				this.abortionCriterionMatchedMessage = "No new solution was found after executing "
						+ this.maximumInstructionsBeforeFindingANewSolution + " instructions.";
				this.solutionProcessor.setDoNotSaveTheNextSolution(true);
				this.returnFromCurrentExecution = true;
				this.stack.clear();
				this.doNotTryToTrackBack = true;
				if (this.measureExecutionTime)
					this.timeExecutionInstruction += System.nanoTime()
							- this.timeExecutionInstructionTemp;
				return;
			}
		}

		// Update the coverage before actually executing the instruction. This is needed since
		// exception handling might further update it.
		Options options = Options.getInst();
		Method method = null;

		// Update the control-flow and def-use coverage.
		if (this.trackCoverage) {
			if (this.measureExecutionTime) this.timeCoverageCheckingTemp = System.nanoTime();
			method = this.currentFrame.getMethod();
			try {
				this.coverage.updateCoverage(method, this.pc);
			} catch (InitializationException e) {
				throw new ExecutionException(
						"Tracking coverage failed with an InitializationException: "
								+ e.getMessage());
			}

			// Finished updating.
			if (this.measureExecutionTime)
				this.timeCoverageChecking += System.nanoTime() - this.timeCoverageCheckingTemp;
		}

		// Execute the instruction.
		if (this.measureExecutionTime) this.timeExecutionInstructionTemp = System.nanoTime();
		int oldpc = this.pc;
		Frame oldFrame = this.currentFrame;
		super.executedInstructions++;
		
		String methodName = method.getName();
		String classFile = method.getClassFile().getName();
		final String rosterBean = "javaeetutorial.roster.request.RequestBean";
		final String simpleBean = "de.wwu.pi.muggl.services.SimpleService";
		final String demoBean = "de.wwu.pi.muggl.demo.DemoService";
		final String requestBeanEvaluation = "de.wwu.pi.RequestBeanEvaluation";
		final String requestBean = "de.wwu.pi.RequestBean";
		final String evaBean = "de.wwu.pi.Evaluation";
		final String stringBean = "de.wwu.pi.StringBean";
		final String bean2Use = "de.wwu.pi.RequestBean2Evaluate";
		
		
		if(classFile.equals(bean2Use) && false) {
			System.out.println("execute instruction #" + pc + ": " + instruction.getName() );	
			Stack<Object> operandStack = this.currentFrame.getOperandStack();
			System.out.println("Stack before execution:   (element size=" + operandStack.size() + ")");
			for(Object ele : operandStack) {
				System.out.println("    -> " + ele);
			}
		}
		
		
		String myBreakPoint = "execute instruction #8: invokevirtual";
		if(myBreakPoint.equals("execute instruction #" + pc + ": " + instruction.getName()) && false) {
			System.out.println("-> jetzt wirds spannend");
		}
		
//		if(classFile.equals(bean2Use) && methodName.equals("copyOf")) {
//			System.out.println("-> here we go..");
//		}
		
//		boolean b1 = classFile.equals("java.util.Arrays") 
//				&& this.pc == 3
//				&& methodName.equals("copyOf");
//		boolean b2 = classFile.equals("java.util.Arrays") 
//				&& this.pc == 18
//				&& methodName.equals("copyOf");
//		if(b1 || b2) {
//			System.out.println(classFile+"."+methodName+" -> " + instruction);
//		}
			
		if(
				classFile.equals("java.lang.String") 
				&& methodName.equals("equals") 
				&& false
//				&& pc == 20
//				&& instruction.getName().equals("getfield")
				) {
			System.out.println("waiting here");
		}
		
//		System.out.println("classfile=["+classFile + "], method=["+methodName + "], execute instruction #" + pc + ": " + instruction.getName() );
		
//		if(!methodName.equals("<clinit>") && !methodName.equals("<init>")) {
		if(!methodName.equals("<clinit>") && true) {
//			System.out.println();
			System.out.println(classFile + "#" + methodName
				 + " -> execute instruction #" + pc + ": " + instruction.getName());
			String x = "javaeetutorial.dukesbookstore.ejb.BookRequestBean#getBook";
			if(classFile.equals("javaeetutorial.roster.request.RequestBeanNoCriteria")) {
				String y ="";
			}
		}
		
		if(methodName.equals("joinTest") && pc == 144) {
			System.out.println("wait here....");
		}
		
		instruction.executeSymbolically(this.currentFrame);

		
		
//		if(!methodName.equals("<clinit>") && !methodName.equals("<init>") && true) {
		if(!methodName.equals("<clinit>") && true) {
			Stack<Object> operandStack = this.currentFrame.getOperandStack();
//			System.out.println("Stack after execution:   (element size=" + operandStack.size() + ")");
			for(Object ele : operandStack) {
				String eleString = "null";
				if(ele != null) {
					eleString = ele.toString();
				}
				eleString += "                                                                                                                                                                                                                                                                                                 ";
				eleString = eleString.substring(0,150);
				System.out.println("    | " + eleString + " |");
			}
			if(operandStack.size() == 0) {
				System.out.println("    |                                                                                                      |");
			}
			System.out.println("    --------------------------------------------------------------------------------------------------------");
		}
		
		if (this.measureExecutionTime)
			this.timeExecutionInstruction += System.nanoTime() - this.timeExecutionInstructionTemp;

		// Further coverage checks.
		if (this.trackCoverage) {
			if (this.measureExecutionTime) this.timeCoverageCheckingTemp = System.nanoTime();

			// Update the coverage if the frame changed.
			if (options.useCFCoverage) {
				if (oldFrame != this.currentFrame
						&& ((SymbolicFrame) oldFrame).hasExecutionFinishedNormally())
					this.coverage.markCFInvocationFinished(method);

				/*
				 * TODO: what was this code supposed to do? // Check if the invocation was a
				 * self-invocation. if (((SymbolicFrame) this.currentFrame).getControlGraph() ==
				 * controlGraph) { // Mark that the next edge is not checked. }
				 */

			}

			/*
			 * Abort as a desired level of coverage has been reached. Check if coverage of either
			 * the def-use chains or the control flow has been reached. This requires that the
			 * abortion has not already been triggered and that abortion by coverage is desired at
			 * all.
			 * 
			 * Execution is aborted when the next solution is found. This solution will contain at
			 * least one control-flow edge or def-use chain which is not covered by any solution
			 * found so far. Hence, in contradiction to the absolute abortion, the following
			 * commands are not executed here:
			 * this.solutionProcessor.setDoNotSaveTheNextSolution(true);
			 * this.returnFromCurrentExecution = true; this.stack.clear(); return; Still, setting
			 * this.doNotTryToTrackBack to true will make sure that no other criteria will lead to
			 * an abortion and after finding the next solution there will be no tracking back.
			 */
			if (!this.doNotTryToTrackBack && options.coverageAbortionCriteria != 0
					&& this.coverage.shallStopExecution(method)) {
				if (Globals.getInst().symbolicExecLogger.isDebugEnabled())
					Globals.getInst().symbolicExecLogger
							.debug("Aborting execution when the next solution is found as all "
									+ this.coverage.getWhatIsCovered()
									+ " are covered. This decision might be canceled if further "
									+ "methods that need tracking are discovered.");
				this.abortionCriterionMatched = true;
				this.abortionCriterionMatchedMessage = "All def-use chains are covered.";
				this.doNotTryToTrackBack = true;
			}

			// Update the time needed for coverage checks.
			if (this.measureExecutionTime)
				this.timeCoverageChecking += System.nanoTime() - this.timeCoverageCheckingTemp;
		}

		// Check for loops.
		if (this.measureExecutionTime) this.timeLoopDetectionTemp = System.nanoTime();
		if (options.maximumLoopsToTake != -1 && !checkForLoops(instruction, oldpc)) {
			// TODO: partially disabled if this.doNotTryToTrackBack is set to true? There must not
			// be an endless looping after it is that, however, there must be no hard abortion
			// either.

			// Abort the current execution.
			if (Globals.getInst().symbolicExecLogger.isDebugEnabled())
				Globals.getInst().symbolicExecLogger
						.debug("Aborted execution as the maximum loop limit was reached. Trying to track back...");
			this.maximumLoopsReached = true;
			this.solutionProcessor.setDoNotSaveTheNextSolution(true);
			this.returnFromCurrentExecution = true;
			this.stack.clear();
		}
		if (this.measureExecutionTime)
			this.timeLoopDetection += System.nanoTime() - this.timeLoopDetectionTemp;
	}
	
	private boolean symbolicExecuteStaticMethodByWrapper(Frame frame, String className, String methodName) {
		boolean wrap = false;
		if(className != null && methodName != null && methodName.equals("valueOf")) {
			if(className.equals("java.lang.Integer")) {
				wrap = true;
			}
		}
		return wrap;
	}

	/**
	 * Since the execution came to an end, save the found solution.
	 */
	protected void saveSolution() {
		// Reset the instruction counter.
		this.instructionsExecutedSinceLastSolution = 0;

		// Generate an expression from the solutions.
		try {
			// Check if there would be a return value.
			Object returnValue;
			if (this.hasAReturnValue || this.threwAnUncaughtException) {
				returnValue = this.returnedObject;
			} else {
				returnValue = new UndefinedValue();
			}

			// Add the solutions.
			this.solutionProcessor.addSolution(this.solverManager.getSolution(), returnValue,
					null, null, null, null,
					this.threwAnUncaughtException,
					this.coverage.getCFCoverageMap(), this.coverage.getDUCoverageAsBoolean());

			// Commit the coverage of control flow edges and def-use chains.
			this.coverage.commitAllchanges();
		} catch (SolverUnableToDecideException e) {
			// This should not happen as the Constraints already were solvable.
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
				Globals.getInst().symbolicExecLogger
						.warn("Could not generate a Solution as a SolverUnableToDecideException was thrown with the message: "
								+ e.getMessage()
								+ ".\n\nThis should not happen, please check the corresponding source code.");
		} catch (TimeoutException e) {
			// Log that.
			if (Globals.getInst().symbolicExecLogger.isDebugEnabled())
				Globals.getInst().symbolicExecLogger
						.debug("Could not generate a Solution as a TimeoutException was thrown with the message: "
								+ e.getMessage() + ".");
		}
	}

	/**
	 * Create a new symbolic frame and set its stack to be a StackToTrail.
	 * 
	 * @param invokedBy The frame this frame is invoked by. Might by null.
	 * @param method The Method that this frame holds.
	 * @param arguments The arguments that will be stored in the local variables prior to execution.
	 * @return The new frame.
	 * @throws ExecutionException Thrown on any fatal error that happens during execution and is not
	 *         coped by one of the other Exceptions.
	 */
	@Override
	protected Frame createFrame(Frame invokedBy, Method method, Object[] arguments)
			throws ExecutionException {
		SymbolicFrame frame = new SymbolicFrame(invokedBy, this, method, method.getClassFile()
				.getConstantPool(), arguments);
		frame.setOperandStack(new StackToTrail<Object>(false, this.searchAlgorithm));
		return frame;
	}

	/**
	 * Detect the loops in the currently executed frame. A loop is always constructed by a
	 * conditional jump (a unconditional jump would lead to an infinite loop). Due to the nature of
	 * the graphs, loops are always jumps into the backward direction. (The only other possibility
	 * would be to jump unconditionally backwards, and then have a conditional forward jump to leave
	 * the loop. This is not used.)
	 * 
	 * When a conditional jump has been detected, its target is checked. If it is a backward jump,
	 * its destination and target instructions are saved for the current SymbolicFrame.
	 * 
	 * @throws InvalidInstructionInitialisationException Any fatal problems with the parsing and the
	 *         initialization will lead to this exception.
	 */
	private void detectLoops() throws InvalidInstructionInitialisationException {
		// Only check for loops if this has not been done already and if there should be an abortion
		// after a number of loops at all.
		if (Options.getInst().maximumLoopsToTake != -1
				&& !((SymbolicFrame) this.currentFrame).getLoopsHaveBeenChecked()) {
			// Get the instructions.
			Instruction[] instructions = this.currentFrame.getMethod()
					.getInstructionsAndOtherBytes();
			// Work through the exceptions.
			for (int a = 0; a < instructions.length; a++) {
				// Check if it is a conditional jump.
				if (instructions[a] instanceof JumpConditional) {
					// Loops are characterized by the fact, that the jump is done in backward
					// direction.
					int jumpTarget = ((JumpConditional) instructions[a]).getJumpTarget();
					// Jump target beyond bounds?
					if (jumpTarget >= Limitations.MAX_CODE_LENGTH) {
						jumpTarget -= Limitations.MAX_CODE_LENGTH;
					}
					// Was it a backward jump?
					if (a > jumpTarget) {
						Loop loop = new Loop(a, jumpTarget);
						((SymbolicFrame) this.currentFrame).getLoops().add(loop);
					}
				}

				// Increase a by the number of other bytes to get to the next instruction.
				a += instructions[a].getNumberOfOtherBytes();
			}

			// Mark that loops have been checked.
			((SymbolicFrame) this.currentFrame).setLoopsHaveBeenChecked();
		}
	}

	/**
	 * Check if the current instruction is the constituting element of a loop. In that case,
	 * increase the counter for the passes of the loop. If the counter has reached the limit, return
	 * false. This will signalize, that execution should be aborted and backtracking started.
	 * Otherwise, true is returned, which signalizes that execution should continue.
	 * 
	 * @param instruction The currently executed instruction.
	 * @param oldPc The pc before this instruction was executed.
	 * @return true, if execution should continue, false, if execution should be aborted.
	 */
	private boolean checkForLoops(Instruction instruction, int oldPc) {
		// Only do further operations if the instruction is actually a conditional jump.
		if (instruction instanceof JumpConditional) {
			Iterator<Loop> iterator = ((SymbolicFrame) this.currentFrame).getLoops().iterator();
			while (iterator.hasNext()) {
				Loop loop = iterator.next();
				// Is there a matching entry?
				if (loop.getFrom() == oldPc) {
					int newPC = this.pc;
					// Jumped too far?
					if (newPC >= Limitations.MAX_CODE_LENGTH) {
						newPC -= Limitations.MAX_CODE_LENGTH;
					}
					// Only increase if really jumping.
					if (newPC != oldPc + 1 + instruction.getNumberOfOtherBytes()) loop.incCount();
					// End the check here. If the count is now greater or equal than the maximum
					// loops to take, return false. Return true otherwise.
					return !loop.isCountGreaterEqual(Options.getInst().maximumLoopsToTake);
				}
			}
		}
		// Everything went all right. Return true, execution can be continued.
		return true;
	}

	/**
	 * Generate a new choice point.
	 * 
	 * @param instruction The instruction that wants to generate the choice points.
	 * @param constraintExpression The ConstraintExpression describing the choice a if it is
	 *        conditional jump Instruction. May be null.

	 * @throws SymbolicExecutionException If the instruction supplied is no conditional jump, no
	 *         load instruction or if an Exception is thrown during the choice point generation.
	 */
	public void generateNewChoicePoint(GeneralInstructionWithOtherBytes instruction,
			ConstraintExpression constraintExpression)
			throws SymbolicExecutionException {
		// Counting the instructions before a new solution is found?
		if (this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
		}

		// Check if it is a suitable instruction.
		if (instruction instanceof JumpConditional) { // Conditional jump found.
			this.searchAlgorithm.generateNewChoicePoint(this, instruction, constraintExpression);
		} else {
			throw new SymbolicExecutionException(
					"Only conditional jump instructions might attempt to generate a choice point using this method.");
		}
	}
		
//	public void generateNewJPAEntityManagerFindChoicePointOLD(JPAInvokeInterface instruction, ConstraintExpression expression) throws SymbolicExecutionException {
//		this.searchAlgorithm.generateNewEntityManagerFindChoicePoint(this, instruction, expression);
//	}
	
	
	
//	public void generateNewJPAEntityManagerFindChoicePoint(JPAInvokeInterface instruction, String entityClassName, Object keyValue) throws SymbolicExecutionException {
//		ConstraintExpression expression = NumericEqual.newInstance(new NumericVariable("UserId0", "int"), IntConstant.THREE);; 
//		this.searchAlgorithm.generateNewEntityManagerFindChoicePoint(this, instruction, expression);
//	}
	
	public void generateNewDBChoicePoint(GeneralInstructionWithOtherBytes instruction,
			ConstraintExpression constraintExpression)
			throws SymbolicExecutionException {
		// Counting the instructions before a new solution is found?
		if (this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
		}

		// Check if it is a suitable instruction.
		if (instruction instanceof JumpConditional) { // Conditional jump found.
			this.searchAlgorithm.generateNewDBChoicePoint(this, instruction, constraintExpression);
		} else {
			throw new SymbolicExecutionException(
					"Only conditional jump instructions might attempt to generate a choice point using this method.");
		}
	}
	
	/**
	 * Generate a new choice point.
	 * 
	 * @param instruction The instruction that wants to generate the choice points.
	 * @param generator A variable Generator. May be null to indicate no custom variable generator
	 *        is used.
	 * @param type A String representation of the type.
	 * @throws SymbolicExecutionException If the instruction supplied is no conditional jump, no
	 *         load instruction or if an Exception is thrown during the choice point generation.
	 */
	public void generateNewChoicePoint(GeneralInstructionWithOtherBytes instruction,
			Generator generator, String type)
			throws SymbolicExecutionException {
		// Counting the instructions before a new solution is found?
		if (this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
		}

		// Check if it is a suitable instruction.
		if (instruction instanceof Load) {
			try {
				this.searchAlgorithm.generateNewChoicePoint(this, ((Load) instruction)
						.getLocalVariableIndex(), generator);
			} catch (ConversionException e) {
				throw new SymbolicExecutionException(
						"An object provided by a generator required conversion to Muggl, but conversion failed: "
								+ e.getClass().getName() + " (" + e.getMessage() + ")");
			}
		} else if (instruction instanceof Newarray) {
			this.searchAlgorithm.generateNewChoicePoint(this, type);
		} else {
			throw new SymbolicExecutionException(
					"Only loading instructions or newarray might attempt to generate a choice point using this method.");
		}
	}

	
	public void generateNewChoicePoint(GeneralInstructionWithOtherBytes instruction, Object reference1, Object reference2, boolean compareEquality)
			throws SymbolicExecutionException {
		// Counting the instructions before a new solution is found?
		if (this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
		}

		// Create the choice point.
		this.searchAlgorithm.generateNewChoicePoint(this, instruction, reference1, reference2, compareEquality);
	}
	

	public void generateNewStringComparisonChoicePoint(Invoke instruction, Objectref stringReference1, Objectref stringReference2) {
		// Counting the instructions before a new solution is found?
		if (this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
		}

		// Create the choice point.
		this.searchAlgorithm.generateNewStringComparisonChoicePoint(this, instruction, stringReference1, stringReference2);
	}
	
	
	/**
	 * Generate a new choice point.
	 * 
	 * @param instruction A lcmp instruction.
	 * @param leftTerm The term of long variables and constants of the left hand side of the
	 *        comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the
	 *        comparison.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point
	 *         generation.
	 */
	public void generateNewChoicePoint(LCmp instruction, Term leftTerm, Term rightTerm)
			throws SymbolicExecutionException {
		// Counting the instructions before a new solution is found?
		if (this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
		}

		// Create the choice point.
		this.searchAlgorithm.generateNewChoicePoint(this, instruction, leftTerm, rightTerm);
	}

	/**
	 * Generate a new choice point.
	 * 
	 * @param instruction A CompareFp instruction.
	 * @param less If set to true, the choice point will have the behaviour of dcmpl / fcmpl;
	 *        otherwise, it will behave like dcmpg / fcmpg.
	 * @param leftTerm The term of long variables and constants of the left hand side of the
	 *        comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the
	 *        comparison.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point
	 *         generation.
	 */
	public void generateNewChoicePoint(CompareFp instruction, boolean less, Term leftTerm,
			Term rightTerm) throws SymbolicExecutionException {
		// Counting the instructions before a new solution is found?
		if (this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
		}

		// Create the choice point.
		this.searchAlgorithm.generateNewChoicePoint(this, instruction, less, leftTerm, rightTerm);
	}

	/**
	 * Generate a new choice point.
	 * 
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param termFromStack The term term that was on top of the stack. Using the non symbolic
	 *        execution, this would be the key for the switch.
	 * @param keys The possible keys.
	 * @param pcs The possible jump targets.
	 * @param low The "low" boundary of the tableswitch instruction; or null, if the choice point is
	 *        generated for a lookupswitch instruction.
	 * @param high The "high" boundary of the tableswitch instruction; or null, if the choice point
	 *        is generated for a lookupswitch instruction.
	 * @throws IllegalArgumentException If the number of keys is not equal to the number of jump
	 *         targets or if there are no choices at all.
	 * @throws NullPointerException If either of the specified arrays is null, or if the instruction
	 *         is tableswitch and at least one of the boundaries is null.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point
	 *         generation.
	 */
	public void generateNewChoicePoint(Switch instruction, Term termFromStack, IntConstant[] keys,
			int[] pcs, IntConstant low, IntConstant high) throws SymbolicExecutionException {
		// Counting the instructions before a new solution is found?
		if (this.maximumInstructionsBeforeFindingANewSolution != -1) {
			if (this.onlyCountChoicePointGeneratingInstructions)
				this.instructionsExecutedSinceLastSolution++;
		}

		// Create the choice point.
		this.searchAlgorithm.generateNewChoicePoint(this, instruction, termFromStack, keys, pcs,
				low, high);
	}

	/**
	 * Sets nextFrameIsAlreadyLoaded to true.
	 */
	public void setNextFrameIsAlreadyLoaded() {
		this.nextFrameIsAlreadyLoaded = true;
	}

	/**
	 * Getter for the search algorithm implemented in this symbolic virtual machine.
	 * 
	 * @return The SearchAlgorithm.
	 */
	public SearchAlgorithm getSearchAlgorithm() {
		return this.searchAlgorithm;
	}

	/**
	 * Setter for the stack of this symbolic virtual machine.
	 * 
	 * @param stack The new stack.
	 */
	public void setStack(Stack<Object> stack) {
		this.stack = stack;
	}

	/**
	 * Make sure the virtual machine will not continue execution.
	 */
	public void abortExecution() {
		this.returnFromCurrentExecution = true;
		this.nextFrameIsAlreadyLoaded = false;
		this.stack.clear();
		this.solutionProcessor.setDoNotSaveTheNextSolution(true);
	}

	/**
	 * Interrupt the execution.
	 * 
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		if (!isInterrupted()) {
			// Interrupt the test case generation if it has been already started.
			if (this.solutionProcessor.hasTestCaseGenerationStarted()) {
				/*
				 * TODO: need an extra option to decide whether test cases are generated after
				 * abortion, or not. At the moment, it will usually be desired to do so. Abortion is
				 * usually done when reaching the maixmum execution time and we definitely want to
				 * get test cases after reaching it.
				 */
				//this.solutionProcessor.interrupt();
			}

			// Invoke the interruption implementation of java.lang.Thread.
			super.interrupt();
		}
	}

	/**
	 * Getter for the SolverManager.
	 * 
	 * @return The SolverManager of this SymbolicalVirtualMachine.
	 */
	public SolverManager getSolverManager() {
		return this.solverManager;
	}

	/**
	 * Getter for the SolutionProcessor.
	 * 
	 * @return The SolutionProcessor of this SymbolicalVirtualMachine.
	 */
	public SolutionProcessor getSolutionProcessor() {
		return this.solutionProcessor;
	}

	/**
	 * Finalize the SymbolicalVirtualMachine.
	 */
	@Override
	public void finalize() {
		try {
			this.finalized = true;
			this.solverManager.finalize();
		} catch (Throwable t) {
			// Log it, but do nothing.
			if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
				Globals.getInst().symbolicExecLogger
						.warn("Shutting down the SolverManager failed.");
		} finally {
			super.finalize();
		}
	}

	/**
	 * Return an array of long elements with the times of the execution measurement. If it is
	 * disabled, an array of zero length will be returned.
	 * 
	 * The array has six entries: 0 - Time spent on the execution of instructions. 1 - Time spent on
	 * the detection of loops. 2 - Time spent on the coverage checking. 3 - Time spent on the
	 * generation of choice points. 4 - Time spent on the solving of constraints. 5 - Time spent on
	 * backtracking. 6 - Time spent on the generation of solutions.
	 * 
	 * The time spent on the execution of instructions has already been reduced by the time spent on
	 * the generation of choice points (including solving), which is a sub task.
	 * 
	 * The time is in milliseconds and can directly be used for displaying issues.
	 * 
	 * @return An array of long elements with the times of the execution measurement.
	 */
	public long[] getExecutionTimeInformation() {
		if (!this.measureExecutionTime) return new long[0];
		long[] executionTime = {
				(this.timeExecutionInstruction - this.timeChoicePointGeneration) / NANOS_MILLIS,
				this.timeLoopDetection / NANOS_MILLIS, this.timeCoverageChecking / NANOS_MILLIS,
				(this.timeChoicePointGeneration - this.timeSolvingChoicePoints) / NANOS_MILLIS,
				(this.timeSolvingChoicePoints + this.timeSolvingBacktracking) / NANOS_MILLIS,
				(this.timeBacktracking - this.timeSolvingBacktracking) / NANOS_MILLIS,
				this.timeSolutionGeneration / NANOS_MILLIS };
		return executionTime;
	}

	/**
	 * Return an array of long elements with the times of the execution measurement. There will be
	 * no preprocessing of the elements, hence it is only meant for internal (private) use.
	 * 
	 * @return An array of long elements with the times of the execution measurement.
	 */
	private long[] getNanoExecutionTimeInformation() {
		long[] executionTime = { this.timeExecutionInstruction, this.timeLoopDetection,
				this.timeCoverageChecking, this.timeChoicePointGeneration,
				this.timeSolvingChoicePoints, this.timeSolvingBacktracking, this.timeBacktracking,
				this.timeSolutionGeneration };
		return executionTime;
	}

	/**
	 * Increase the time spent on coverage checking by the supplied increment. Lower the instruction
	 * execution time by that value.
	 * 
	 * @param increment The time needed for coverage checking.
	 */
	public void increaseTimeCoverageChecking(long increment) {
		this.timeCoverageChecking += increment;
	}

	/**
	 * Increase the time spent on choice point generation by the supplied increment. Lower the
	 * instruction execution time by that value.
	 * 
	 * @param increment The time needed for a choice point generation.
	 */
	public void increaseTimeChoicePointGeneration(long increment) {
		this.timeChoicePointGeneration += increment;
	}

	/**
	 * Increase the time spent on backtracking by the supplied increment.
	 * 
	 * @param increment The time needed for a backtracking action.
	 */
	public void increaseTimeBacktracking(long increment) {
		this.timeBacktracking += increment;
	}

	/**
	 * Increase the time spent on solving for the generation of choice points by the supplied
	 * increment.
	 * 
	 * @param increment The time needed for a solving action.
	 */
	public void increaseTimeSolvingForChoicePointGeneration(long increment) {
		this.timeSolvingChoicePoints += increment;
	}

	/**
	 * Increase the time spent on solving for backtracking by the supplied increment.
	 * 
	 * @param increment The time needed for a solving action.
	 */
	public void increaseTimeSolvingForBacktracking(long increment) {
		this.timeSolvingBacktracking += increment;
	}

	/**
	 * Getter for the finalized field.
	 * 
	 * @return true, if this symbolic virtual machine has been finalized, false otherwise.
	 */
	public boolean isFinalized() {
		return this.finalized;
	}

	/**
	 * Getter for the information, whether execution finished as an abortion criterion was matched.
	 * 
	 * @return true, if an abortion criterion was matched, false otherwise.
	 */
	public boolean getAbortionCriterionMatched() {
		return this.abortionCriterionMatched;
	}

	/**
	 * Setter for the information, whether execution finished as an abortion criterion was matched.
	 * 
	 * @param abortionCriterionMatched Information, whether execution finished as an abortion
	 *        criterion was matched.
	 */
	public void setAbortionCriterionMatched(boolean abortionCriterionMatched) {
		this.abortionCriterionMatched = abortionCriterionMatched;
	}

	/**
	 * Getter for the message stored if the execution finished as an abortion criterion was matched.
	 * 
	 * @return The abortion criterion message, or null, if there is no such message stored.
	 */
	public String getAbortionCriterionMatchedMessage() {
		return this.abortionCriterionMatchedMessage;
	}

	/**
	 * Setter for the message stored if the execution finished as an abortion criterion was matched.
	 * 
	 * @param abortionCriterionMatchedMessage The abortion criterion message.
	 */
	public void setAbortionCriterionMatchedMessage(String abortionCriterionMatchedMessage) {
		this.abortionCriterionMatchedMessage = abortionCriterionMatchedMessage;
	}

	/**
	 * Getter for the information, whether the maximum number of loops were reached and no further
	 * deepening was done at least one time.
	 * 
	 * @return true, if the maximum number of loops were reached, false otherwise.
	 */
	public boolean getMaximumLoopsReached() {
		return this.maximumLoopsReached;
	}

	/**
	 * Get the {@link CoverageController} used by the symbolic virtual machine.
	 * 
	 * @return The coverage controller used.
	 */
	public CoverageController getCoverageController() {
		return this.coverage;
	}

	/**
	 * Change the current frame and put that information onto the trail, if there is any.
	 * 
	 * @param frame The frame to become the current frame.
	 */
	@Override
	public void changeCurrentFrame(Frame frame) {
		// Create a FrameChange trail element?
		ChoicePoint choicePoint = this.searchAlgorithm.getCurrentChoicePoint();
		if (choicePoint != null && choicePoint.hasTrail()) {
			// Add a FrameChange trail element.
			choicePoint.addToTrail(new FrameChange(this.currentFrame));
		}

		// Report frame change to the coverage controller.
		this.coverage.reportFrameChange(frame);

		// Invoke the super implementation to change the frame.
		super.changeCurrentFrame(frame);
	}

	/**
	 * Mark that the solutions found by this symbolic virtual machine will no be processed.
	 */
	public void doNotProcessSolutions() {
		this.doNotProcessSolutions = true;
	}
	
	/**
	 * Report that another array generator is used.
	 */
	public void reportArrayGenerator() {
		this.arraysGeneratorsUsed++;
	}
	
	/**
	 * Report that another array was generated.
	 */
	public void reportGeneratedArray() {
		this.arraysGenerated++;
	}

	/**
	 * Get information on array generation.
	 * 
	 * @return An array of type long containing data on the number of array generators used and the
	 *         number of arrays generated.
	 */
	public long[] getArrayGenerationInformation() {
		long[] info = {this.arraysGeneratorsUsed, this.arraysGenerated};
		return info;
	}

	@Deprecated
	public void setDoNotTryToTrackBack(boolean b) {
		this.doNotTryToTrackBack = b;
	}


}
