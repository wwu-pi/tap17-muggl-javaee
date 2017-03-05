package de.wwu.muggl.symbolic.searchAlgorithms.iterativeDeepening;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.bytecode.LCmp;
import de.wwu.muggl.instructions.general.CompareFp;
import de.wwu.muggl.instructions.general.GeneralInstructionWithOtherBytes;
import de.wwu.muggl.instructions.general.Switch;
import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.DepthFirstSearchAlgorithm;
import de.wwu.muggl.vm.execution.ConversionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * This class implements the iterative deepening depth first algorithm.<br />
 * <br />
 * The algorithm basically works like the depth first search algorithm.
 * However, with each branch the currently reached depth is counted. When
 * reaching a depth that equal the predefined starting depth, no more
 * deepening is done and the algorithm tracks back, as if the depth first
 * algorithm would have reached a leaf node.<br />
 * <br />
 * If every node has been visited and the algorithm has not been aborted,
 * execution will be restarted, with the maximum depth incremented by a
 * predefined value. This will be done until the algorithm has either been
 * aborted, or there is no more deepening possible.<br />
 * <br />
 * In general, depth first can take too much time to find solutions as it
 * might branch too deep and wastes time without finding solutions.
 * Iterative deepening surely has an overhead, as with an increased depth
 * execution has to be redone for the already visited tree. However, it
 * combines the strength of breadth first to not get lost in deep branches
 * without solutions with the small memory footprint of depth first.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
public class IterativeDeepeningSearchAlgorithm extends DepthFirstSearchAlgorithm {
	// Fields
	private long maximumDepth;
	private long deepnessIncrement;
	private long currentLevelOfDeepness;
	private boolean thereWereMorePossibilities;
	private long timesStarted;
	private long maximumDepthReached;
	private long totalConstraintsCheckedYet;
	private long totalConstraintsCheckedEarlierExecutions;

	/**
	 * Instantiate the iterative deepening depth first search algorithm.
	 * @param startingDepth The maximum depth to start with. Will be set to 1, if less than 1;
	 * @param  deepnessIncrement The levels of depth used for incrementation. Will be set to 1, if less than 1;
	 */
	public IterativeDeepeningSearchAlgorithm(int startingDepth, int deepnessIncrement) {
		super();
		if (startingDepth < 1) startingDepth = 1;
		this.maximumDepth = startingDepth;
		if (deepnessIncrement < 1) deepnessIncrement = 1;
		this.deepnessIncrement = deepnessIncrement;
		this.currentLevelOfDeepness = 0;
		this.thereWereMorePossibilities = false;
		this.timesStarted = 1;
		this.maximumDepthReached = 1;
		this.totalConstraintsCheckedYet = 0;
		this.totalConstraintsCheckedEarlierExecutions = 0;
	}

	/**
	 * Try to track back to the last ChoicePoint thats non jumping branch was not
	 * yet visited.
	 *
	 * The Method is overriden for statistical purposes only. If a level of deepness
	 * greater than the currently reached maximum depth has been reached, this will
	 * be saved.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @return true, if tracking back was successful and the execution can be continued, false, if there was no possibility for tracking back and then execution should hence be stopped.
	 */
	@Override
	public boolean trackBack(SymbolicVirtualMachine vm) {
		if (this.currentLevelOfDeepness > this.maximumDepthReached) this.maximumDepthReached = this.currentLevelOfDeepness;
		return super.trackBack(vm);
	}

	/**
	 * This method is called when tracking back failed. It will check, if there
	 * was any point where the algorithm did not branch deeper, but tracked back.
	 * If there was no such point, execution finishes and false is returned.
	 *
	 * However, if there was such an point, increase the maximum depth by the
	 * deepness increment, increase the number of times started, reset the
	 * number of visited branches in this run, and reset the execution so
	 * it will begin from the very first Instruction again.
	 *
	 * Make sure, everything is logged for the user information.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @return true, if execution can be continued, false otherwise.
	 */
	@Override
	protected boolean trackBackFailed(SymbolicVirtualMachine vm) {
		// Statistics.
		this.totalConstraintsCheckedEarlierExecutions = this.totalConstraintsCheckedYet;

		// Have there been more possibilities?
		if (this.thereWereMorePossibilities) {
			// Set to false.
			this.thereWereMorePossibilities = false;

			// Increase the maximum depth.
			this.maximumDepth += this.deepnessIncrement;

			// Log
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("No more tracking back is possible. However, execution will be restarted with a greater maximum depth of " + this.maximumDepth + ". Already visited a maximum of " + this.numberOfVisitedBranches + " branches in total, and it took " + this.timesStarted + " starts to visit the deepest ones.");

			// Alter the counters.
			this.numberOfVisitedBranches = 0;
			this.timesStarted++;

			// Reset the elements that need reseting.
			this.currentChoicePoint = null;
			this.currentLevelOfDeepness = 0;

		    /*
		     *  Set up a new symbolic virtual machine. This will make sure memory is released
		     *  and the runtime stack of this application is not increasing with each start.
		     */
			try {
				SymbolicVirtualMachine vmNew = new SymbolicVirtualMachine(this, vm);
				if(Options.getInst().jpaMode) {
					vmNew = new JPAVirtualMachine(vmNew.getApplication(), vmNew.getClassLoader(), vmNew.getClassFile(), vmNew.getInitialMethod());
				}
				
				vmNew.setStepByStepMode(vm.getStepByStepMode());

				// Tell the vm not to process the solutions found.
				vm.doNotProcessSolutions();

				// Set the new vm.
				vm.getApplication().setVirtualMachine(vmNew);
			} catch (InitializationException e) {
				// This is unlikely to happen, but may happen. Execution will not be continued.
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
					Globals.getInst().symbolicExecLogger.trace("Trying initialize another virtual machine"
							+ " for iterative deepening failed with an InitializationException with message \""
							+ e.getMessage() + "\". Aborting.");

			}

			// Make sure there is no more execution in the current vm.
			vm.abortExecution();

			// Return false either!
			return false;
		}
		// Finishing the execution.
		if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
			Globals.getInst().symbolicExecLogger.trace("No more tracking back is possible. There are no deeper branches to visit, either. Visited a maximum of " + this.numberOfVisitedBranches + " branches in total, and it took " + this.timesStarted + " starts to visit the deepest ones.");
		return false;
	}

	/**
	 * Recover that state at the ChoicePoint currentChoicePoint. Just invoke the super
	 * implementation for doing so, but decrease the current level of deepness before
	 * doing so.
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 */
	@Override
	public void recoverState(SymbolicVirtualMachine vm) {
		this.currentLevelOfDeepness--;
		super.recoverState(vm);
	}

	/**
	 * Increase the current level of deepness by one. If it gets equal to the
	 * maximum depth, execution does not continue but the trackback algorithm
	 * is invoked. Otherwise the depth first implementation is used to generate
	 * a new choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param localVariableIndex The index into the local variable table to store the generated
	 *        array at.
	 * @param generator A variable Generator. May be null to indicate no custom variable generator
	 *        is used.
	 * @throws ConversionException If converting the first provided object failed.
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for.
	 */
	@Override
	public void generateNewChoicePoint(
			SymbolicVirtualMachine vm, int localVariableIndex, Generator generator
			) throws ConversionException, SymbolicExecutionException {
		this.currentLevelOfDeepness++;
		if (this.currentLevelOfDeepness >= this.maximumDepth) {
			this.thereWereMorePossibilities = true;
			trackBack(vm);
		} else {
			super.generateNewChoicePoint(vm, localVariableIndex, generator);
		}
	}

	/**
	 * Increase the current level of deepness by one. If it gets equal to the maximum depth,
	 * execution does not continue but the trackback algorithm is invoked. Otherwise the depth first
	 * implementation is used to generate a new choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param constraintExpression The ConstraintExpression describing the choice at this
	 *        conditional jump Instruction.
	 * @see de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.DepthFirstSearchAlgorithm#generateNewChoicePoint(SymbolicVirtualMachine,
	 *      GeneralInstructionWithOtherBytes, ConstraintExpression)
	 */
	@Override
	public void generateNewChoicePoint(SymbolicVirtualMachine vm,
			GeneralInstructionWithOtherBytes instruction, ConstraintExpression constraintExpression) {
		this.currentLevelOfDeepness++;
		if (this.currentLevelOfDeepness >= this.maximumDepth) {
			this.thereWereMorePossibilities = true;
			trackBack(vm);
		} else {
			super.generateNewChoicePoint(vm, instruction, constraintExpression);
		}
	}

	/**
	 * Increase the current level of deepness by one. If it gets equal to the maximum depth,
	 * execution does not continue but the trackback algorithm is invoked. Otherwise the depth first
	 * implementation is used to generate a new choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param leftTerm The term of long variables and constants of the left hand side of the
	 *        comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the
	 *        comparison.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point
	 *         generation.
	 * @see de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.DepthFirstSearchAlgorithm#generateNewChoicePoint(SymbolicVirtualMachine,
	 *      LCmp, Term, Term)
	 */
	@Override
	public void generateNewChoicePoint(SymbolicVirtualMachine vm, LCmp instruction,
			Term leftTerm, Term rightTerm) throws SymbolicExecutionException {
		this.currentLevelOfDeepness++;
		if (this.currentLevelOfDeepness >= this.maximumDepth) {
			this.thereWereMorePossibilities = true;
			trackBack(vm);
		} else {
			super.generateNewChoicePoint(vm, instruction, leftTerm, rightTerm);
		}
	}

	/**
	 * Increase the current level of deepness by one. If it gets equal to the maximum depth,
	 * execution does not continue but the trackback algorithm is invoked. Otherwise the depth first
	 * implementation is used to generate a new choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param less If set to true, the choice point will have the behaviour of dcmpl / fcmpl;
	 *        otherwise, it will behave like dcmpg / fcmpg.
	 * @param leftTerm The term of long variables and constants of the left hand side of the
	 *        comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the
	 *        comparison.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point
	 *         generation.
	 * @see de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.DepthFirstSearchAlgorithm#generateNewChoicePoint(SymbolicVirtualMachine,
	 *      CompareFp, boolean, Term, Term)
	 */
	@Override
	public void generateNewChoicePoint(SymbolicVirtualMachine vm, CompareFp instruction,
			boolean less, Term leftTerm, Term rightTerm) throws SymbolicExecutionException {
		this.currentLevelOfDeepness++;
		if (this.currentLevelOfDeepness >= this.maximumDepth) {
			this.thereWereMorePossibilities = true;
			trackBack(vm);
		} else {
			super.generateNewChoicePoint(vm, instruction, less, leftTerm, rightTerm);
		}
	}

	/**
	 * Increase the current level of deepness by one. If it gets equal to the maximum depth,
	 * execution does not continue but the trackback algorithm is invoked. Otherwise the depth first
	 * implementation is used to generate a new choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
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
	 * @see de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.DepthFirstSearchAlgorithm#generateNewChoicePoint(SymbolicVirtualMachine,
	 *      Switch, Term, IntConstant[], int[], IntConstant, IntConstant)
	 */
	@Override
	public void generateNewChoicePoint(SymbolicVirtualMachine vm, Switch instruction, Term termFromStack,
			IntConstant[] keys, int[] pcs, IntConstant low, IntConstant high)
			throws SymbolicExecutionException {
		this.currentLevelOfDeepness++;
		if (this.currentLevelOfDeepness >= this.maximumDepth) {
			this.thereWereMorePossibilities = true;
			trackBack(vm);
		} else {
			super.generateNewChoicePoint(vm, instruction, termFromStack, keys, pcs, low, high);
		}
	}

	/**
	 * Return a String representation of this search algorithms name.
	 * @return A String representation of this search algorithms name.
	 */
	@Override
	public String getName() {
		return "iterative deepening depth first";
	}

	/**
	 * Return statistical information about the execution. The information is
	 * returned as an two-dimensional String array. Its first dimension
	 * represents the distinct statistical informations that can be supplied.
	 * The second dimension has two elements. Element 0 is the name of the
	 * statistical criterion, element 1 is the string representation of its
	 * value.
	 *
	 * For the iterative deepening search algorithm the statistical information
	 * are the number of branches visited, the number of times the algorithm
	 * restarted and the maximum search depth reached.
	 *
	 * @return Statistical information about the execution.
	 */
	@Override
	public String[][] getStatisticalInformation() {
		String[][] statistics = new String[5][2];
		statistics[0][0] = "Number of visited branches (this run):\t";
		statistics[0][1] = Long.valueOf(this.numberOfVisitedBranches).toString();
		statistics[1][0] = "Number of times started:\t\t\t\t\t";
		statistics[1][1] = Long.valueOf(this.timesStarted).toString();
		statistics[2][0] = "Maximum search depth reached:\t\t\t";
		statistics[2][1] = Long.valueOf(this.maximumDepthReached).toString();
		statistics[3][0] = "# of constraints checked this run:\t\t";
		if (this.currentChoicePoint != null) {
			this.totalConstraintsCheckedYet = ((SymbolicVirtualMachine) this.currentChoicePoint.getFrame().getVm()).getSolverManager().getTotalConstraintsChecked();
			statistics[3][1] = Long.valueOf(this.totalConstraintsCheckedYet - this.totalConstraintsCheckedEarlierExecutions).toString();
		} else {
			statistics[3][1] = "0";
		}
		statistics[4][0] = "# of constraints checked total:\t\t\t";
		statistics[4][1] = Long.valueOf(this.totalConstraintsCheckedYet).toString();
		return statistics;
	}
}
