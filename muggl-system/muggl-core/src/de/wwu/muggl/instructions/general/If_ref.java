package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.control.JumpConditional;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.VariableUsing;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;

/**
 * Abstract instruction with some concrete methods for comparison instructions for checking
 * references (ifnonnull and ifnull). The concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public abstract class If_ref extends GeneralInstructionWithOtherBytes implements JumpConditional,
		StackPop, VariableUsing {
	private int	lineNumber;

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument. Also the line number is given as an argument, since
	 * it is needed in case of jumping.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param lineNumber The line number of this instruction (including other bytes!).
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public If_ref(AttributeCode code, int lineNumber) throws InvalidInstructionInitialisationException {
		super(code);
		this.lineNumber = lineNumber;
	}

	/**
	 * Execute the inheriting instruction.
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		Object value = frame.getOperandStack().pop();
		if (compare(value))
			frame.getVm().setPC(getJumpTarget());
	}

	/**
	 * Execute the inheriting instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
//		Object value = frame.getOperandStack().pop();
//		
//		ConstraintExpression expression = getConstraintExpression(value);
//		((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this, expression);

		Object value = frame.getOperandStack().pop();
		
		if(value instanceof Objectref && ((Objectref)value).canBeNull()) {
			// check if this instruction is (1) IS_NULL or (2) IS_NOT_NULL
			((JPAVirtualMachine)frame.getVm())
				.generateNewObjectrefIsNullChoicePoint(this, (Objectref)value, isJumpToIsNull());
			return;
		}
		
		if (compare(value))
			frame.getVm().setPC(getJumpTarget());
	}
	
	protected abstract boolean isJumpToIsNull();

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 2;
	}

	/**
	 * Compare if the value mets the desired condition. Return true if the expected
	 * condition is met, false otherwise.
	 * @param value The value to check.
	 * @return true if the expected condition is met, false otherwise.
	 */
	protected abstract boolean compare(Object value);

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
		return 1;
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
		byte[] types = {0};
		return types;
	}

}
