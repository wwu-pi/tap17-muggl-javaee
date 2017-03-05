package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.LocalVariableAccess;
import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.VariableDefining;
import de.wwu.muggl.instructions.interfaces.data.VariableUsing;
import de.wwu.muggl.instructions.typed.TypedInstruction;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.Restore;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;

/**
 * Abstract instruction with some concrete methods for instructions that store elements
 * to the local variables. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class Store extends GeneralInstructionWithOtherBytes implements
		LocalVariableAccess, JumpNever, StackPop, VariableDefining, VariableUsing {
	private static final int MAX_INDEX = 255;
	
	/**
	 * The index of the variable to load.
	 */
	protected int index;
	/**
	 * The number of other bytes of this instruction.
	 */
	protected int numberOfOtherBytes;
	/**
	 * The type the inheriting class will take.
	 */
	protected TypedInstruction typedInstruction;

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument, as well as the index of the instruction, pointing at
	 * the local variable that it is responsible for.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param index The index to load from. Must be a value between 0 and 3 inclusively. Otherwise, the index is set to 255 and methods are required to calculate if from the additional bytes.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Store(AttributeCode code, int index) throws InvalidInstructionInitialisationException {
		super(code, index, 3, 1);
		if (index < 0 || index > 3) {
			this.index = MAX_INDEX;
			this.numberOfOtherBytes = 1;
		} else {
			this.index = index;
			this.numberOfOtherBytes = 0;
		}
	}

	/**
	 * Execute the inheriting instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		int localVariable = getLocalVariableIndex();
		execute(frame, localVariable);
	}

	/**
	 * For widened index: execute the inheriting instruction.
	 * @param frame The currently executed frame.
	 * @param localVariable The local variable index to store to.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	public void execute(Frame frame, int localVariable) throws ExecutionException {
		try {
			Object object = frame.getOperandStack().pop();

			// Unexpected exception: type check failed.
			if (object != null && !this.typedInstruction.checkDesiredType(object.getClass().getName()))
			{
				throw new ExecutionException("Could not store local variable #" + localVariable + ": Expected " + this.typedInstruction.getDesiredType() + ", got " + object.getClass().getName() + ".");
			}

			frame.setLocalVariable(localVariable, object);
		} catch (ExecutionException e) {
			executionFailed(e);
		}
	}

	/**
	 * Execute the inheriting instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		int localVariable = getLocalVariableIndex();
		executeSymbolically(frame, localVariable);
	}

	/**
	 * For widened index: execute the inheriting instruction symbolically.
	 * @param frame The currently executed frame.
	 * @param localVariable The local variable index to store to.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	public void executeSymbolically(Frame frame, int localVariable) throws SymbolicExecutionException {
		// Save the current value, if necessary.
		if (((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().savingLocalVariableValues()) {
			Restore localVariableValue = new Restore(localVariable, frame.getLocalVariables()[localVariable]);
			((SymbolicVirtualMachine) frame.getVm()).getSearchAlgorithm().saveLocalVariableValue(localVariableValue);
		}

		// Store the value.
		Object object = frame.getOperandStack().pop();
		frame.setLocalVariable(localVariable, object);
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return this.numberOfOtherBytes;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		if (this.index <= 3) {
			return "store_" + this.index;
		}
		return "store";
	}

	/**
	 * Get the index into the local variables.
	 * @return The local variable index.
	 */
	public int getLocalVariableIndex() {
		if (this.index > 3) {
			return this.otherBytes[0]; // no explicit cast needed
		}
		return this.index;
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be popped from the stack.
	 */
	public int getNumberOfPoppedElements() {
		return 1;
	}

}
