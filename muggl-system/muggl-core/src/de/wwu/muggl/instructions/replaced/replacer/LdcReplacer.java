package de.wwu.muggl.instructions.replaced.replacer;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.bytecode.Ldc;
import de.wwu.muggl.instructions.replaced.ReplacingInstruction;
import de.wwu.muggl.instructions.replaced.quick.LdcQuick;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;

/**
 * {@link ReplacingInstruction} for <i>ldc</i>. Executed once, it will store the executions results
 * and replace itself with {@link LdcQuick}.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-11-24
 */
public class LdcReplacer extends Ldc implements ReplacingInstruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method
	 * that the instruction belongs to is supplied as an argument.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized
	 *         successfully, most likely due to missing additional bytes. This might be caused by a
	 *         corrupt class file, or a class file of a more recent version than can be handled.
	 */
	public LdcReplacer(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 *
	 * @param frame The currently executed frame.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		super.execute(frame);

		// Create the quick instruction and set it.
		Object objectToStore = frame.getOperandStack().peek();
		LdcQuick quick = new LdcQuick(this, objectToStore, getOtherByte(0));
		frame.getMethod().replaceInstruction(quick, frame.getVm().getPc());
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException In case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException,
			SymbolicExecutionException {
		super.executeSymbolically(frame);

		// Create the quick instruction and set it.
		Object objectToStore = frame.getOperandStack().peek();
		LdcQuick quick = new LdcQuick(this, objectToStore, getOtherByte(0));
		frame.getMethod().replaceInstruction(quick, frame.getVm().getPc());
	}

}
