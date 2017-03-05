package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.control.JumpConditional;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.VariableUsing;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.ReferenceVariable;

/**
 * Abstract instruction with some concrete methods for comparison instructions of the group
 * if_cmp&lt;cond&gt;. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class If_acmp extends GeneralInstructionWithOtherBytes implements JumpConditional,
		StackPop, VariableUsing {
	// The line number is stored for jumping purposes.
	protected int lineNumber;

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument. Also the line number is given as an argument, since
	 * it is needed in case of jumping.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param lineNumber The line number of this instruction (including other bytes!).
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public If_acmp(AttributeCode code, int lineNumber) throws InvalidInstructionInitialisationException {
		super(code);
		this.lineNumber = lineNumber;
	}

	/**
	 * Execute the inheriting instruction.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		ReferenceValue value2 = (ReferenceValue) frame.getOperandStack().pop();
		ReferenceValue value1 = (ReferenceValue) frame.getOperandStack().pop();
		if (compare(value1, value2)) {
			frame.getVm().setPC(this.lineNumber + (this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]));
		}
	}

	/**
	 * Get the number of other bytes for this instruction.
	 *
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 2;
	}

	/**
	 * Compare two reference values. Return true if the expected condition is met, false otherwise.
	 *
	 * @param value1 The first reference value.
	 * @param value2 The second reference value.
	 * @return true If the expected condition is met, false otherwise.
	 */
	protected abstract boolean compare(ReferenceValue value1, ReferenceValue value2);
	

	/**
	 * Return the target pc of the possible jump.
	 * @return The target pc of the possible jump.
	 */
	public int getJumpTarget() {
		return this.lineNumber + (this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]);
	}

	/**
	 * Get the number of elements that will be popped from the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be popped from the stack.
	 */
	public int getNumberOfPoppedElements() {
		return 2;
	}

	/**
	 * Get the types of elements this instruction will pop from the stack.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The types this instruction pops. The length of the arrays reflects the number of
	 *         elements pushed in the order they are pushed. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference or return address type or -1 to
	 *         indicate the popped type cannot be determined statically.
	 */
	public byte[] getTypesPopped(ClassFile methodClassFile) {
		byte[] types = {0, 0};
		return types;
	}

}
