package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.PushAbstract;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * Implementation of the instruction  <code>sipush</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class SiPush extends PushAbstract implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public SiPush(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		frame.getOperandStack().push(Integer.valueOf((short) (this.otherBytes[0] << ONE_BYTE | this.otherBytes[1])));
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
		frame.getOperandStack().push(IntConstant.getInstance((short) (this.otherBytes[0] << ONE_BYTE | this.otherBytes[1])));
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "sipush";
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 2;
	}


	/**
	 * Get a int representation of the value this instruction will push.
	 *
	 * @return The value this instruction will push
	 */
	@Override
	public int getPushedValue() {
		return (short) (this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]);
	}

}
