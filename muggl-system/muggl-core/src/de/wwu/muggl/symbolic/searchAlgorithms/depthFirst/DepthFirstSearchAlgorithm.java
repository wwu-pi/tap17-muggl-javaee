package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst;

import java.util.Stack;

import javax.persistence.criteria.CriteriaQuery;

import org.apache.log4j.Level;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.db.sym.list.SymbolicHasNext;
import de.wwu.muggl.instructions.bytecode.Getfield;
import de.wwu.muggl.instructions.bytecode.LCmp;
import de.wwu.muggl.instructions.bytecode.Lookupswitch;
import de.wwu.muggl.instructions.general.CompareDouble;
import de.wwu.muggl.instructions.general.CompareFp;
import de.wwu.muggl.instructions.general.GeneralInstructionWithOtherBytes;
import de.wwu.muggl.instructions.general.If_acmp;
import de.wwu.muggl.instructions.general.Invoke;
import de.wwu.muggl.instructions.general.Switch;
import de.wwu.muggl.instructions.interfaces.control.JumpConditional;
import de.wwu.muggl.instructions.jpa.JPAInvokeInterface;
import de.wwu.muggl.jpa.FindResult;
import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.symbolic.flow.coverage.CGCoverageTrailElement;
import de.wwu.muggl.symbolic.flow.coverage.DUCoverageTrailElement;
import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.generating.GeneratorChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.SearchAlgorithm;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.EquationViolationException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.SolvingException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.array.ArrayInitializationChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.database.EntityManagerFindChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.database.EntityManagerPersistChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.database.FoobarChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.db.ConditionalJumpDBChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.db.EMCollectionHasNextCP;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.db.EMFindCP;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.db.EMResultListCP;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.db.EMFindCP;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.fpComparison.DoubleComparisonChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.fpComparison.FloatComparisonChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.iterator.SymbolicIteratorHasNextChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.javaee.find.JPAFindCP;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.javaee.persist.JPAPersistCP;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.jpa.GetResultListChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.jpa.GetSingleResultChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.list.SymbolicIterationChoicePoint;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.jpa.JPAFindResultNullJumpCP;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.longComparison.LongComparisonChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.obj.GetFieldOfNullableObjectChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.obj.ObjectIsNullChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ref.ReferenceVariableComparisonChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ref.StringEqualityComparisonChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.switching.LookupswitchChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.switching.SwitchingChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.switching.TableswitchChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.ArrayRestore;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.FieldPut;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.FrameChange;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.InstanceFieldPut;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.Pop;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.PopFromFrame;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.Push;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.Restore;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.StaticFieldPut;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.VmPop;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.VmPush;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ConversionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicFrame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.sym.SymbolicIterator;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.solvers.type.IObjectreference;

/**
 * This class implements the depth first search algorithm.<br />
 * <br />
 * The depth first algorithm always takes one path through the search tree until
 * a point with no further branching is reached; this most likely is the end of
 * the current program. In this application, the first path to take is always
 * the jumping path. This means, when a conditional jump is encountered, a
 * ChoicePoint is generated and the execution continues at the jump offset.<br />
 * <br />
 * When and leaf of the search tree has been reached and the possibly found
 * solution has been saved, tracking back starts. The algorithm will go back
 * to the last ChoicePoint and check whether its non jumping branch has been
 * visited already already. If it has not yet been visited, execution can be
 * continued at that branch. From this point on, on any further conditional
 * jumps the jumping branch will again be executed first.<br />
 * <br />
 * If tracking back and a ChoicePoint is reached thats non-jumping branch also
 * has been visited, the application tracks back to the parent ChoicePoint,
 * checking if the non-jumping branch has been visited for that ChoicePoint.
 * The algorithm recursively continues in that way. It stops when the topmost
 * ChoicePoint (the ChoicePoint that has no parent) has been reached and it
 * indicates that its non-jumping branch has already been visited.<br />
 * <br />
 * The depth first search algorithm does not need any additional information
 * stored to perform. However, finding solutions might take a lot of time.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class DepthFirstSearchAlgorithm implements SearchAlgorithm {
	// Fields
	/**
	 * The choice point the search algorithm last branched at.
	 */
	protected ChoicePoint currentChoicePoint;
	/**
	 * The total number o9f branches visited so far.
	 */
	protected long numberOfVisitedBranches;
	/**
	 * Flag to determine if the execution time should be measured.
	 */
	protected boolean measureExecutionTime;
	/**
	 * Temporary field to measure choice point generation time.
	 */
	protected long timeChoicePointGenerationTemp;
	/**
	 * Temporary field to measure backtracking time.
	 */
	protected long timeBacktrackingTemp;
	/**
	 * Temporary field to measure solving time.
	 */
	protected long timeSolvingTemp;

	/**
	 * Instantiate the depth first search algorithm.
	 */
	public DepthFirstSearchAlgorithm() {
		this.numberOfVisitedBranches = 0;
		this.measureExecutionTime = Options.getInst().measureSymbolicExecutionTime;
	}

	/**
	 * Get the current ChoicePoint of this search algorithm. It reflects the
	 * last node in the searching tree visited.
	 * @return The current choice point.
	 */
	public ChoicePoint getCurrentChoicePoint() {
		return this.currentChoicePoint;
	}

	private NoExceptionHandlerFoundException trackBackExpectedException;
	
	public NoExceptionHandlerFoundException getTrackBackExpectedException() {
		return this.trackBackExpectedException;
	}
	
	/**
	 * Try to track back to the last ChoicePoint thats non jumping branch was not yet visited.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @return true, if tracking back was successful and the execution can be continued, false, if
	 *         there was no possibility for tracking back and then execution should hence be
	 *         stopped.
	 */
	public boolean trackBack(SymbolicVirtualMachine vm) {
		this.trackBackExpectedException = null;
		
		if (this.measureExecutionTime) this.timeBacktrackingTemp = System.nanoTime();
		// Only track back if there ever was a ChoicePoint generated at all. Otherwise, no tracking back is possible.
		if (this.currentChoicePoint == null) return false;

		// Get the SolverManager.
		SolverManager solverManager = vm.getSolverManager();

//		// Since the jump is executed first, find the newest ChoicePoint thats non jumping branch was not visited yet. Restore states while doing so.
//		boolean hasAnotherChoice = this.currentChoicePoint.hasAnotherChoice();
//		
//		if(this.currentChoicePoint.getNumber() > 10) {
//			System.out.println("************ STOP FURTHER CHOICE POINTS ****************");
//			vm.setDoNotTryToTrackBack(true);
//		}
		
		while (!this.currentChoicePoint.hasAnotherChoice()) {
			// First step: Use the trail of the last choice point to get back to the old state.
			recoverState(vm);

			// Second step: If one has been set, remove the ConstraintExpression from the ConstraintStack of the SolverManager.
			if (this.currentChoicePoint.changesTheConstraintSystem()) {
				int cpConstraintLevel = this.currentChoicePoint.getConstraintLevel();
				System.out.println("TRACKING BACK TO CONSTRAINT LEVEL: " + cpConstraintLevel);
				solverManager.resetConstraintLevel(cpConstraintLevel);
				if(vm instanceof JPAVirtualMachine) {
					((JPAVirtualMachine)vm).getVirtualObjectDatabase().resetDatabase(cpConstraintLevel);
				}
			}
			
			// Third step: Load its' parent. This will also free the memory of the current choice point.
			this.currentChoicePoint = this.currentChoicePoint.getParent();
			if (this.currentChoicePoint == null) {
				// Went trough all branches.
				if (this.measureExecutionTime) vm.increaseTimeBacktracking(System.nanoTime() - this.timeBacktrackingTemp);
				return trackBackFailed(vm);
			}
		}

		// Change to the next choice.
		try {
			this.currentChoicePoint.changeToNextChoice();
		} catch (MugglException e) {
			if (this.measureExecutionTime) vm.increaseTimeBacktracking(System.nanoTime() - this.timeBacktrackingTemp);
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
				Globals.getInst().symbolicExecLogger
						.trace("Tracking back was successfull, but encountered an Exception when switching "
								+ " to the next choice. Trying to track back further. The root cause it "
								+ e.getClass().getName() + " (" + e.getMessage() + ")");

			return trackBack(vm);
		}

		// Count up for the non-jumping branch.
		this.numberOfVisitedBranches++;

		// Perform operations specific to the constraint system.
		if (this.currentChoicePoint.changesTheConstraintSystem()) {
			// Remove the Constraint and get the new one.
			int cpConstraintLevel = this.currentChoicePoint.getConstraintLevel();
			System.out.println("TRACKING BACK TO CONSTRAINT LEVEL: " + cpConstraintLevel);
			solverManager.resetConstraintLevel(cpConstraintLevel);
			
			ConstraintExpression cpCE = this.currentChoicePoint.getConstraintExpression();
			if(cpCE != null) {
				solverManager.addConstraint(cpCE);
			}

			// Check if the new branch can be visited at all, or if it causes an equation violation.
			try {
				// Try to solve the expression.
				if (this.measureExecutionTime) this.timeSolvingTemp = System.nanoTime();
				boolean hasSolution = solverManager.hasSolution();
				if (!hasSolution) {
					if (this.measureExecutionTime) vm.increaseTimeSolvingForBacktracking(System.nanoTime() - this.timeSolvingTemp);
					if (this.measureExecutionTime) vm.increaseTimeBacktracking(System.nanoTime() - this.timeBacktrackingTemp);
					return trackBack(vm);
				}
				if (this.measureExecutionTime) vm.increaseTimeSolvingForBacktracking(System.nanoTime() - this.timeSolvingTemp);
			} catch (SolverUnableToDecideException e) {
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
					Globals.getInst().symbolicExecLogger.trace("Solving lead to a SolverUnableToDecideException with message: " + e.getMessage());
				if (this.measureExecutionTime) vm.increaseTimeBacktracking(System.nanoTime() - this.timeBacktrackingTemp);
				return trackBack(vm);
			} catch (TimeoutException e) {
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
					Globals.getInst().symbolicExecLogger.trace("Solving lead to a TimeoutException with message: " + e.getMessage());
				if (this.measureExecutionTime) vm.increaseTimeBacktracking(System.nanoTime() - this.timeBacktrackingTemp);
				return trackBack(vm);
			}
		}

		// Found the choice point to continue, recover the state of it.
		recoverState(vm);

		// Does the choice point require any state specific changes beside those already done?
		if (this.currentChoicePoint.enforcesStateChanges()) {
			try {
				this.currentChoicePoint.applyStateChanges();
			} catch (VmRuntimeException e) {
				this.trackBackExpectedException = new NoExceptionHandlerFoundException(e.getWrappedException(), null, -1);
			}
		}

		// Tracking back was successful.
		if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
			Globals.getInst().symbolicExecLogger.trace("Tracking back was successful. Already visited " + (this.numberOfVisitedBranches - 1) + " branches.");
		if (this.measureExecutionTime) vm.increaseTimeBacktracking(System.nanoTime() - this.timeBacktrackingTemp);
		return true;
	}

	/**
	 * This method is called when tracking back failed. It will not change a thing, and just log
	 * log that the execution ends here, then return false. This method is intended to be overridden
	 * by inheriting algorithms.
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @return false in any case.
	 */
	@SuppressWarnings("unused")
	protected boolean trackBackFailed(SymbolicVirtualMachine vm) {
		if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
			Globals.getInst().symbolicExecLogger.trace("No more tracking back is possible. Visited " + this.numberOfVisitedBranches + " branches in total.");
		return false;
	}

	/**
	 * Recover that state at the ChoicePoint currentChoicePoint.
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 */
	public void recoverState(SymbolicVirtualMachine vm) {
		// Get the current stacks.
		StackToTrail<Object> operandStack = (StackToTrail<Object>) vm.getCurrentFrame().getOperandStack();
		StackToTrail<Object> vmStack = (StackToTrail<Object>) vm.getStack();

		// Set the StackToTrail instances to restoring mode. Otherwise the recovery will be added to the trail, which will lead to weird behavior.
		operandStack.setRestoringMode(true);
		vmStack.setRestoringMode(true);

		// If the choice point has a trail, use it to recover the state.
		if (this.currentChoicePoint.hasTrail()) {
			Stack<TrailElement> trail = this.currentChoicePoint.getTrail();
			// Empty the trail.
			while (!trail.empty()) {
				Object object = trail.pop();
				// Decide about the action by checking the trail element's type.
				if (object instanceof VmPush) {
					VmPush vmPush = (VmPush) object;
					object = vmPush.getObject();
					vmStack.push(object);
					if (vmPush.restoreStates()) {
						// Restore states of the frame.
						SymbolicFrame frame = (SymbolicFrame) object;
						frame.setPc(vmPush.getPc());
						frame.setMonitor(vmPush.getMonitor());
					}
				} else if (object instanceof VmPop) {
					if (vm.getStack().isEmpty()) {
						if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
							Globals.getInst().symbolicExecLogger.warn("Processing the trail lead to a request to "
									+ "pop an element from the empty VM stack. It will be ignored and skipped. "
									+ "However, this hints to a serious problem and should be checked.");
					} else {
						vm.getStack().pop();
					}
				} else if (object instanceof FrameChange) {
					FrameChange frameChange = (FrameChange) object;
					// There was a change in the frame. Put it as the (temporary) current Frame.
					vm.setCurrentFrame(frameChange.getFrame());
					// Disable the restoring mode for the last Frame's operand stack.
					operandStack.setRestoringMode(false);
					// Set the current operand stack accordingly.
					operandStack = (StackToTrail<Object>) frameChange.getFrame().getOperandStack();
					// Enable restoring mode for it.
					operandStack.setRestoringMode(true);
				} else if (object instanceof Push) {
					vm.getCurrentFrame().getOperandStack().push(((Push) object).getObject());
				} else if (object instanceof Pop) {
					if (vm.getCurrentFrame().getOperandStack().isEmpty()) {
						if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
							Globals.getInst().symbolicExecLogger.warn("Processing the trail lead to a request to "
									+ "pop an element from an empty operand stack. It will be ignored and skipped. "
									+ "However, this hints to a serious problem and should be checked.");
					} else {
						vm.getCurrentFrame().getOperandStack().pop();
					}
				} else if (object instanceof PopFromFrame) {
					if (((PopFromFrame) object).getFrame().getOperandStack().isEmpty()) {
						if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
							Globals.getInst().symbolicExecLogger.warn("Processing the trail lead to a request to "
									+ "pop an element from an empty operand stack. It will be ignored and skipped. "
									+ "However, this hints to a serious problem and should be checked.");
					} else {
						((PopFromFrame) object).getFrame().getOperandStack().pop();
					}
				} else if (object instanceof ArrayRestore) {
					((ArrayRestore) object).restore();
				} else if (object instanceof Restore) {
					((Restore) object).restore(vm.getCurrentFrame());
				} else if (object instanceof InstanceFieldPut) {
					((InstanceFieldPut) object).restoreField();
				} else if (object instanceof StaticFieldPut) {
					((StaticFieldPut) object).restoreField();
				} else if (object instanceof DUCoverageTrailElement) {
					((DUCoverageTrailElement) object).restore();
				} else if (object instanceof CGCoverageTrailElement) {
					((CGCoverageTrailElement) object).restore();
				} else {
					if (Globals.getInst().symbolicExecLogger.isEnabledFor(Level.WARN))
						Globals.getInst().symbolicExecLogger.warn(
								"Found an unrecognized object on the trail when trying to restore"
									+ "an old state. It will be ignored and skipped.");
				}
			}
		}

		// Set the correct Frame to be the current Frame.
		vm.setCurrentFrame(this.currentChoicePoint.getFrame());

		// If the frame was set to have finished the execution normally, reset that.
		((SymbolicFrame) vm.getCurrentFrame()).resetExecutionFinishedNormally();

		// Set the pc!
		vm.getCurrentFrame().setPc(this.currentChoicePoint.getPcNext());

		// Set the last pc value of the control graph, should control flow coverage be tracked.
		vm.getCoverageController().revertPcTo(this.currentChoicePoint.getFrame().getMethod(),
				this.currentChoicePoint.getPc());

		// Signalize to the virtual machine that no Frame has to be popped but execution can be resumed with the current Frame.
		vm.setNextFrameIsAlreadyLoaded();
		// If this tracking back is done while executing a Frame, also signalize to the vm to not continue executing it.
		vm.setReturnFromCurrentExecution(true);

		// Disable the restoring mode.
		operandStack.setRestoringMode(false);
		vmStack.setRestoringMode(false);
	}

	/**
	 * Generate a new GeneratorChoicePoint or ArrayInitializationChoicePoint for a local variable.
	 * Set it as the current choice point.
	 * 
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param localVariableIndex The index into the local variable table to store the generated
	 *        array at.
	 * @param generator A variable Generator. May be null to indicate no custom variable generator
	 *        is used.
	 * @throws ConversionException If converting the first provided object failed.
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for.
	 */
	public void generateNewChoicePoint(
			SymbolicVirtualMachine vm, int localVariableIndex, Generator generator
			) throws ConversionException, SymbolicExecutionException {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		// If a custom generator is present, generate a GeneratorChoicePoint.
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		if (generator != null) {
			this.currentChoicePoint = new GeneratorChoicePoint(
					generator,
					vm.getCurrentFrame(),
					localVariableIndex,
					vm.getPc(),
					this.currentChoicePoint
					);
		} else {
			/*
			 * The only reason to invoke this method without a custom variable generator provided is
			 * the request for a ArrayInitializationChoicePoint that uses the built-in array generator.
			 */
			this.currentChoicePoint = new ArrayInitializationChoicePoint(vm.getCurrentFrame(),
					localVariableIndex, vm.getPc(), this.currentChoicePoint);
		}

		// Apply the first value.
		try {
			this.currentChoicePoint.applyStateChanges();
		} catch (VmRuntimeException e) {
			throw new SymbolicExecutionException("Could not apply state changes");
		}

		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	
	/**
	 * Generate a new  ArrayInitializationChoicePoint for instruction
	 * <code>anewarray</code>. Set it as the current choice point and mark that the jumping branch
	 * has been visited already (since execution just continues there.)
	 * 
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param type A String representation of the type.
	 * 
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for.
	 */
	public void generateNewChoicePoint(SymbolicVirtualMachine vm, String type)
			throws SymbolicExecutionException {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();	
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new ArrayInitializationChoicePoint(vm.getCurrentFrame(), vm
				.getPc(), type, this.currentChoicePoint);


		// Apply the first value.
		try {
			this.currentChoicePoint.applyStateChanges();
		} catch (VmRuntimeException e) {
			throw new SymbolicExecutionException("Could not apply state changes");
		}

		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	

	/**
	 * Generate a new ConditionalJumpChoicePoint. Set it as the current choice point and mark that
	 * the jumping branch has been visited already (since execution just continues there.)
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param constraintExpression The ConstraintExpression describing the choice at this
	 *        conditional jump Instruction.
	 */
	public void generateNewChoicePoint(SymbolicVirtualMachine vm,
			GeneralInstructionWithOtherBytes instruction, ConstraintExpression constraintExpression) {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		try {
			this.currentChoicePoint = new ConditionalJumpChoicePointDepthFirst(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					((JumpConditional) instruction).getJumpTarget(),
					constraintExpression,
					this.currentChoicePoint);

			// Count up for the jumping branch.
			this.numberOfVisitedBranches++;
			if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
		} catch (EquationViolationException e) {
			if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
			// Track back to the last choice point and try its non-jumping branch.
			trackBack(vm);
		} catch (SolvingException e) {
			// Track back to the last choice point and try its non-jumping branch.
			trackBack(vm);
		}
	}
	
	public void generateNewObjectrefIsNullChoicePoint(JPAVirtualMachine vm,
			GeneralInstructionWithOtherBytes instruction,
			Objectref objectReference,
			boolean jumpToIsNull) {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new ObjectIsNullChoicePoint(
				vm.getCurrentFrame(),
				vm.getPc(),
				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
				((JumpConditional) instruction).getJumpTarget(),
				objectReference,
				jumpToIsNull,
				this.currentChoicePoint);

		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	
	
	public void generateNewGetFieldOfNullableObjectChoicePoint(
			JPAVirtualMachine vm,
			Getfield instruction,
			Objectref objectReference) throws VmRuntimeException {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new GetFieldOfNullableObjectChoicePoint(
				instruction,
				vm.getCurrentFrame(),
				vm.getPc(),
				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
				objectReference,
				this.currentChoicePoint);

		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	
	
	public void generateIteratorHasNextChoicePoint(SymbolicVirtualMachine vm, SymbolicIterator symbolicIterator, GeneralInstructionWithOtherBytes instruction) {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		if(this.currentChoicePoint instanceof SymbolicIteratorHasNextChoicePoint && this.currentChoicePoint.getNumber() > 4) {
			vm.getCurrentFrame().getOperandStack().push(NumericConstant.getZero(Expression.INT));
			return;
		}
		
		try {
			this.currentChoicePoint = new SymbolicIteratorHasNextChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					this.currentChoicePoint,
					symbolicIterator);

			// Count up for the jumping branch.
			this.numberOfVisitedBranches++;
			if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
		} catch (EquationViolationException e) {
			if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
			// Track back to the last choice point and try its non-jumping branch.
			trackBack(vm);
		} catch (SolvingException e) {
			// Track back to the last choice point and try its non-jumping branch.
			trackBack(vm);
		}
	}
	
//	public void generateNewEntityManagerFindChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, ConstraintExpression constraintExpression) {
//		try {
//			this.currentChoicePoint = new EntityManagerFindChoicePoint(
//				vm.getCurrentFrame(),
//				vm.getPc(),
//				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
//				constraintExpression,
//				vm.getVirtualDatabase(),
//				this.currentChoicePoint);
//			
//			this.numberOfVisitedBranches++;
//		} catch (EquationViolationException e) {
//			trackBack(vm);
//		} catch (SolvingException e) {
//			trackBack(vm);
//		}
//	}
	
	public void generateFindCP(JPAVirtualMachine vm, JPAInvokeInterface instruction, String entityName, Variable idVariable) {
//		throw new RuntimeException("NOT SUPPORTED ANY MORE");
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new EMFindCP(
				vm.getCurrentFrame(),
				vm.getPc(),
				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
				this.currentChoicePoint,
				entityName,
				idVariable);
		this.numberOfVisitedBranches++;
	}
	
	public void generateResultListCP(JPAVirtualMachine vm, JPAInvokeInterface instruction, CriteriaQuery<?> criteriaQuery) {
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new EMResultListCP(
				vm.getCurrentFrame(),
				vm.getPc(),
				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
				this.currentChoicePoint,
				criteriaQuery);
		
		this.numberOfVisitedBranches++;
	}
	
	/**
	 * Create a new choice point for a symbolic reference comparison.
	 * At least one of the parameters <tt>reference1</tt> or <tt>reference2</tt> must be of type <tt>ReferenceVariable</tt>.
	 * @param vm
	 * @param instruction
	 * @param reference1
	 * @param reference2
	 * @param compareEquality indicates if the comparison instruction is checking for equality (<tt>If_acmpeq</tt>) or inequality (<tt>If_acmpne</tt>).
	 */
	public void generateNewChoicePoint(SymbolicVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, Object reference1, Object reference2, boolean compareEquality) {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		try {
			this.currentChoicePoint = new ReferenceVariableComparisonChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					((JumpConditional) instruction).getJumpTarget(),
					this.currentChoicePoint,
					reference1,
					reference2,
					compareEquality);
			this.numberOfVisitedBranches++;
		} catch (EquationViolationException e) {
			if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
			// Track back to the last choice point and try its non-jumping branch.
			trackBack(vm);
		} catch (SolvingException e) {
			// Track back to the last choice point and try its non-jumping branch.
			trackBack(vm);
		}
	}
	
	
	
	public void generateNewStringComparisonChoicePoint(SymbolicVirtualMachine vm, GeneralInstructionWithOtherBytes instruction,  Object stringReference1, Object stringReference2) {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new StringEqualityComparisonChoicePoint(
				vm.getCurrentFrame(),
				vm.getPc(),
				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
				this.currentChoicePoint,
				stringReference1,
				stringReference2);
		this.numberOfVisitedBranches++;
	}
	
	
	
	
	
	
	
	public void generateNewCollectionChoicePoint(JPAVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, CollectionVariable<DatabaseObject> collection) {
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new EMCollectionHasNextCP(
				vm.getCurrentFrame(),
				vm.getPc(),
				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
				((JumpConditional) instruction).getJumpTarget(),
				this.currentChoicePoint,
				collection);
		this.numberOfVisitedBranches++;
	}
//	public void generateResultListCP(JPAVirtualMachine vm, JPAInvokeInterface instruction, CriteriaQuery<?> criteriaQuery) {
//		this.currentChoicePoint = new EMResultListCP(
//				vm.getCurrentFrame(),
//				vm.getPc(),
//				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
//				this.currentChoicePoint,
//				criteriaQuery);
//		this.numberOfVisitedBranches++;
//	}
	
	public void generateNewFindResultChoicePoint(JPAVirtualMachine vm, GeneralInstructionWithOtherBytes instruction, FindResult findResultObject, int jumpTarget) {
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		throw new RuntimeException("This choice point does not exist any more");
//		this.currentChoicePoint = new JPAFindResultNullJumpCP(
//				vm.getCurrentFrame(),
//				vm.getPc(),
////				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
//				jumpTarget,
//				this.currentChoicePoint,
//				findResultObject);
//		this.numberOfVisitedBranches++;
	}
	
	public void generateNewDBChoicePoint(SymbolicVirtualMachine vm,
			GeneralInstructionWithOtherBytes instruction, ConstraintExpression constraintExpression) {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		try {
			this.currentChoicePoint = new ConditionalJumpDBChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					((JumpConditional) instruction).getJumpTarget(),
					constraintExpression,
					this.currentChoicePoint);

			// Count up for the jumping branch.
			this.numberOfVisitedBranches++;
			if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
		} catch (EquationViolationException e) {
			if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
			// Track back to the last choice point and try its non-jumping branch.
			trackBack(vm);
		} catch (SolvingException e) {
			// Track back to the last choice point and try its non-jumping branch.
			trackBack(vm);
		}
		
//		this.currentChoicePoint = new DatabaseChoicePoint(
//				vm.getCurrentFrame(),
//				vm.getPc(),
//				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
//				((JumpConditional) instruction).getJumpTarget(),
//				constraintExpression,
//				this.currentChoicePoint);
//
//		// Apply the first value.
//		this.currentChoicePoint.applyStateChanges();
//		
//		// Count up for the jumping branch.
//		this.numberOfVisitedBranches++;
//		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	
	

	/**
	 * Generate a new LongComparisonChoicePoint. Set it as the current choice point.
	 *
	 * @param vm The currently executing SymbolicalVirtualMachine.
	 * @param instruction The Instruction generating the ChoicePoint.
	 * @param leftTerm The term of long variables and constants of the left hand side of the comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the comparison.
	 * @throws SymbolicExecutionException If an Exception is thrown during the choice point generation.
	 */
	public void generateNewChoicePoint(SymbolicVirtualMachine vm, LCmp instruction,
			Term leftTerm, Term rightTerm) throws SymbolicExecutionException {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new LongComparisonChoicePoint(
				vm.getCurrentFrame(),
				vm.getPc(),
				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
				leftTerm,
				rightTerm,
				this.currentChoicePoint);

		// Apply the first value.
		try {
			this.currentChoicePoint.applyStateChanges();
		} catch (VmRuntimeException e) {
			throw new SymbolicExecutionException("Could not apply state changes");
		}

		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}

	/**
	 * Generate a new FpComparisonChoicePoint. Set it as the current choice point.
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
	 */
	public void generateNewChoicePoint(SymbolicVirtualMachine vm, CompareFp instruction,
			boolean less, Term leftTerm, Term rightTerm) throws SymbolicExecutionException {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();

		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		if (instruction instanceof CompareDouble) {
			this.currentChoicePoint = new DoubleComparisonChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					less,
					leftTerm,
					rightTerm,
					this.currentChoicePoint);
		} else {
			this.currentChoicePoint = new FloatComparisonChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					less,
					leftTerm,
					rightTerm,
					this.currentChoicePoint);
		}

		// Apply the first value.
		try {
			this.currentChoicePoint.applyStateChanges();
		} catch (VmRuntimeException e) {
			throw new SymbolicExecutionException("Could not apply state changes");
		}

		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}

	/**
	 * Generate a new SwitchingComparisonChoicePoint. Set it as the current choice point.
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
	 */
	public void generateNewChoicePoint(SymbolicVirtualMachine vm, Switch instruction, Term termFromStack,
			IntConstant[] keys, int[] pcs, IntConstant low, IntConstant high)
			throws SymbolicExecutionException {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		
		if (instruction instanceof Lookupswitch) {
			this.currentChoicePoint = new LookupswitchChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					termFromStack,
					keys,
					pcs,
					this.currentChoicePoint);
		} else {
			this.currentChoicePoint = new TableswitchChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					termFromStack,
					keys,
					pcs,
					low,
					high,
					this.currentChoicePoint);
		}
		// execute side effects that formerly resided in the constructor  
		((SwitchingChoicePoint)this.currentChoicePoint).init(); 

		// Apply the first value.
		try {
			this.currentChoicePoint.applyStateChanges();
		} catch (VmRuntimeException e) {
			throw new SymbolicExecutionException("Could not apply state changes");
		}

		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}

	/**
	 * Return a String representation of this search algorithms name.
	 * @return A String representation of this search algorithms name.
	 */
	public String getName() {
		return "depth first";
	}

	/**
	 * Get the information whether this search algorithm requires a field
	 * value to be stored (at this exakt moment of execution).
	 * @return true, if a choice point has been already generated, false otherwise.
	 */
	public boolean savingFieldValues() {
		if (this.currentChoicePoint == null) return false;
		return true;
	}

	/**
	 * Store a field value for use by the seach algorithm's tracking back
	 * functionality.
	 * @param valueRepresentation Either a InstanceFieldPut or a StaticfieldPut object.
	 */
	public void saveFieldValue(FieldPut valueRepresentation) {
		if (this.currentChoicePoint != null) this.currentChoicePoint.addToTrail(valueRepresentation);
	}

	/**
	 * Get the information whether this search algorithm requires a local
	 * variable value to be stored (at this exakt moment of execution).
	 * @return true, if a choice point has been already generated, false otherwise.
	 */
	public boolean savingLocalVariableValues() {
		if (this.currentChoicePoint == null) return false;
		return true;
	}

	/**
	 * Store a local varable value for use by the seach algorithm's tracking back
	 * functionality.
	 * @param valueRepresentation A Restore object.
	 */
	public void saveLocalVariableValue(Restore valueRepresentation) {
		if (this.currentChoicePoint != null) this.currentChoicePoint.addToTrail(valueRepresentation);
	}

	/**
	 * Get the information whether this search algorithm requires an array
	 * value to be stored (at this exakt moment of execution).
	 * @return true, if a choice point has been already generated, false otherwise.
	 */
	public boolean savingArrayValues() {
		if (this.currentChoicePoint == null) return false;
		return true;
	}

	/**
	 * Store a array value for use by the seach algorithm's tracking back
	 * functionality.
	 * @param valueRepresentation An ArrayRestore object.
	 */
	public void saveArrayValue(ArrayRestore valueRepresentation) {
		if (this.currentChoicePoint != null) this.currentChoicePoint.addToTrail(valueRepresentation);
	}

	/**
	 * Return statistical information about the execution. The information is
	 * returned as an two-dimensional String array. Its first dimension
	 * represents the distinct statistical informations that can be supplied.
	 * The second dimension has two elements. Element 0 is the name of the
	 * statistical criterion, element 1 is the string representation of its
	 * value.
	 *
	 * For the depth first search algorithm the only statistical information
	 * is the number of branches visited.
	 *
	 * @return Statistical information about the execution.
	 */
	public String[][] getStatisticalInformation() {
		String[][] statistics = new String[2][2];
		statistics[0][0] = "Number of visited branches:\t\t\t\t";
		statistics[0][1] = Long.valueOf(this.numberOfVisitedBranches).toString();
		statistics[1][0] = "Number of constraints checked:\t\t\t";
		if (this.currentChoicePoint != null) {
			statistics[1][1] = Long.valueOf(((SymbolicVirtualMachine) this.currentChoicePoint
					.getFrame().getVm()).getSolverManager().getTotalConstraintsChecked())
					.toString();
		} else {
			statistics[1][1] = "0";
		}
		return statistics;
	}
	
	
	

	@Override
	public void generateEntityManagerFindChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, String entityName, Object idReference) throws VmRuntimeException {		
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
				
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new JPAFindCP(
				vm.getCurrentFrame(),
				vm.getPc(),
				vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
				this.currentChoicePoint,
				entityName,
				idReference);

		this.currentChoicePoint.applyStateChanges();
		
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	
	
	@Override
	public void generateEntityManagerPersistChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, Objectref entityObject) throws VmRuntimeException {		
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}		
	
		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
				
		this.currentChoicePoint = new JPAPersistCP(
									vm.getCurrentFrame(),
									vm.getPc(),
									vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
									this.currentChoicePoint,
									entityObject);
//		this.currentChoicePoint = new EntityManagerPersistChoicePoint(
//					vm.getCurrentFrame(),
//					vm.getPc(),
//					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
//					this.currentChoicePoint,
//					entityObject);
			
		this.currentChoicePoint.applyStateChanges();
		
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	
	
	@Override
	public void generateGetQueryResultListChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, QLStatement<?> qlStatement) throws VmRuntimeException {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new GetResultListChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					this.currentChoicePoint,
					qlStatement);
			
		this.currentChoicePoint.applyStateChanges();
		
		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	
	@Override
	public void generateGetSingleResultChoicePoint(JPAVirtualMachine vm, JPAInvokeInterface instruction, QLStatement<?> qlStatement) throws VmRuntimeException {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		// snapshot the database...
		if(vm instanceof JPAVirtualMachine) {
			((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
		}
		
		this.currentChoicePoint = new GetSingleResultChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					this.currentChoicePoint,
					qlStatement);
			
		this.currentChoicePoint.applyStateChanges();
		
		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
	
	public void generateSymbolicIterationChoicePoint(Invoke instruction, SymbolicIterator symbolicIterator) {
		if (this.measureExecutionTime) this.timeChoicePointGenerationTemp = System.nanoTime();
		
		JPAVirtualMachine vm = symbolicIterator.getSymbolicList().getVM();
		// snapshot the database...
		((JPAVirtualMachine)vm).getVirtualObjectDatabase().snapshotCurrentDB(vm.getSolverManager());
	
		
		this.currentChoicePoint = new SymbolicIterationChoicePoint(
					vm.getCurrentFrame(),
					vm.getPc(),
					vm.getPc() + 1 + instruction.getNumberOfOtherBytes(),
					this.currentChoicePoint,
					symbolicIterator);
		
		// Count up for the jumping branch.
		this.numberOfVisitedBranches++;
		if (this.measureExecutionTime) vm.increaseTimeChoicePointGeneration(System.nanoTime() - this.timeChoicePointGenerationTemp);
	}
}
