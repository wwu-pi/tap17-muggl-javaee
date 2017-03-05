package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.interfaces.LocalVariableAccess;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * Implementation of the instruction <code>ret</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-01-29
 */
public class Ret extends de.wwu.muggl.instructions.general.RetAbstract implements Instruction, LocalVariableAccess {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Ret(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	@SuppressWarnings("unused")
	public void execute(Frame frame) throws ExecutionException {
		frame.getVm().setPC((Integer) frame.getLocalVariables()[getLocalVariableIndex()]);
	}

	/**
	 * For widened index: execute the instruction.
	 * @param frame The currently executed frame.
	 * @param index The index to set the pc to.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@SuppressWarnings("unused")
	public void execute(Frame frame, int index) throws ExecutionException {
		frame.getVm().setPC(index);
	}

	/**
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	@SuppressWarnings("unused")
	public void executeSymbolically(Frame frame) throws SymbolicExecutionException {
		frame.getVm().setPC(this.otherBytes[0]);

	}

	/**
	 * For widended index: execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @param index The index to set the pc to.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@SuppressWarnings("unused")
	public void executeSymbolically(Frame frame, int index) throws SymbolicExecutionException {
		frame.getVm().setPC(index);
	}

	/**
	 * Get the index into the local variables.
	 * @return The local variable index.
	 */
	public int getLocalVariableIndex() {
		return this.otherBytes[0];
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "ret";
	}

}
