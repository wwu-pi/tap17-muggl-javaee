package de.wwu.muggl.vm.impl.symbolic;

import java.util.ArrayList;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.StackToTrail;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.PopFromFrame;
import de.wwu.muggl.symbolic.structures.Loop;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.execution.ExecutionException;

/**
 * The SymbolicFrame inherits the functionality of a "normal" Frame. It also offers some
 * features needed for symbolic execution.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class SymbolicFrame extends Frame {
		
	// New fields.
	private boolean loopsHaveBeenChecked;
	private ArrayList<Loop> loops;
	private boolean executionFinishedNormally;

	/**
	 * Constructor that simply invokes the super constructor, before initializing the class'
	 * own fields.
	 * @param invokedBy The frame this frame was invoked by. Might be null.
	 * @param vm The symbolic virtual machine this frame can be executed on.
	 * @param method The Method this frame represents.
	 * @param constantPool A reference to the constant pool of the methods class.
	 * @param arguments The predefined arguments for this method.
	 * @throws ExecutionException Thrown on any fatal error that happens during execution and is not coped by one of the other Exceptions.
	 */
	public SymbolicFrame(Frame invokedBy, SymbolicVirtualMachine vm, Method method,
			Constant[] constantPool, Object[] arguments) throws ExecutionException {
		super(invokedBy, vm, method, constantPool, arguments);

		// Initialize the fields of this class.
		this.loopsHaveBeenChecked = false;
		this.loops = new ArrayList<Loop>();
		this.executionFinishedNormally = false;
	}

	/**
	 * Getter for the loops ArrayList.
	 * @return The ArrayList of loops.
	 */
	public ArrayList<Loop> getLoops() {
		return this.loops;
	}

	/**
	 * Getter for loopsHaveBeenChecked.
	 * @return true, if the loops have been checked, false otherwise.
	 */
	public boolean getLoopsHaveBeenChecked() {
		return this.loopsHaveBeenChecked;
	}

	/**
	 * Set loopsHaveBeenChecked to true. Setting it back to false is not possible.
	 */
	public void setLoopsHaveBeenChecked() {
		this.loopsHaveBeenChecked = true;
	}

	/**
	 * Mark that the execution finished normally and invoke the super implementation.
	 */
	@Override
	public void returnFromMethod() {
		this.executionFinishedNormally = true;
		super.returnFromMethod();
		revertLastPCForCFCovering();
	}

	/**
	 * Mark that the execution finished normally and pushes a returned object onto the stack of the
	 * invoking frame, or if there was none onto the virtual machines stack and sets the frame
	 * inactive. Clears this frames' operand stack afterwards.
	 *
	 * @param value The value returned from the method.
	 */
	@Override
	public void returnFromMethod(Object value) {
		this.executionFinishedNormally = true;
		if (this.invokedBy != null) {
			StackToTrail<Object> operandStack = (StackToTrail<Object>) this.invokedBy.getOperandStack();
			// Enable restoring mode to the pushed item will not be added as a pop trail element to the trail.
			operandStack.setRestoringMode(true);
			// Push the return value.
			operandStack.push(value);
			// Enable restoring mode.
			operandStack.setRestoringMode(false);
			// Add this item manually to the trail.
			ChoicePoint choicePoint = ((SymbolicVirtualMachine) this.vm).getSearchAlgorithm()
					.getCurrentChoicePoint();
			if (choicePoint != null && choicePoint.hasTrail())
				choicePoint.addToTrail(new PopFromFrame(this.invokedBy));
		} else {
			this.vm.getStack().push(value);
		}
		while (!this.operandStack.isEmpty()) {
			this.operandStack.pop();
		}
		this.active = false;
		revertLastPCForCFCovering();
	}

	/**
	 * Revert the last pc value for control flow tracking. This has to be done in order to reflect
	 * the return from invoking a method.
	 */
	public void revertLastPCForCFCovering() {
		/*
		 * Under no circumstances the last pc must be reverted if this Frame is used to execute
		 * the static initializer. It is not invoked by a invoke instruction!
		 */
		if (this.invokedBy != null && Options.getInst().useCFCoverage
				&& !this.method.getName().equals("<clinit>")) {
			int pc = this.invokedBy.getPc() - 3;
			Method method = this.invokedBy.getMethod();
			// Check if that pc points to an instruction.
			try {
				// There is no instruction. The invoking instruction was invokeinterface. Reduce the pc by 2.
				if (method.getInstructionsAndOtherBytes()[pc] == null)
					pc -= 2;
			} catch (InvalidInstructionInitialisationException e) {
				// This cannot happen at this point.
			}
			((SymbolicVirtualMachine) this.vm).getCoverageController().revertPcTo(
					method, pc);
		}
	}

	/**
	 * Find out if execution in this frame was finished by the execution of a return instruction.
	 * @return true if execution in this frame was finished normally, false otherwise.
	 */
	public boolean hasExecutionFinishedNormally() {
		return this.executionFinishedNormally;
	}

	/**
	 * Reset the execution status of this symbolic frame. When tracking back, the frame must no longer
	 * be marked as a frame that has normally finished its execution (i.e. its return method has been
	 * executed.)
	 */
	public void resetExecutionFinishedNormally() {
		this.executionFinishedNormally = false;
	}

}
