package de.wwu.muggl.instructions.general;

import de.wwu.muggl.db.sym.list.SymbolicHasNext;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.control.JumpConditional;
import de.wwu.muggl.instructions.interfaces.data.StackPop;
import de.wwu.muggl.instructions.interfaces.data.VariableUsing;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Abstract instruction with some concrete methods for comparison instructions of the group
 * if&lt;cond&gt;. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-29
 */
public abstract class If extends GeneralInstructionWithOtherBytes implements JumpConditional,
		StackPop, VariableUsing {
	private int	lineNumber;

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method
	 * that the instruction belongs to is supplied as an argument. Also the line number is given as
	 * an argument, since it is needed in case of jumping.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param lineNumber The line number of this instruction (including other bytes!).
	 * @throws InvalidInstructionInitialisationException If the instruction could not be
	 *         initialized successfully, most likely due to missing additional bytes. This might be
	 *         caused by a corrupt class file, or a class file of a more recent version than what can
	 *         be handled.
	 */
	public If(AttributeCode code, int lineNumber) throws InvalidInstructionInitialisationException {
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
		int value = (Integer) frame.getOperandStack().pop();
		if (compare(value)) {
			frame.getVm().setPC(this.lineNumber + (this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]));
		}
	}

	/**
	 * Execute the inheriting instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		try {
			Object term = frame.getOperandStack().pop();
			if(term instanceof SymbolicHasNext) {
				SymbolicHasNext symHasNext = (SymbolicHasNext)term;
				
				if(symHasNext.getIterator().getCounter() > 2) {
					return;
				}
				
				if(symHasNext.getIterator().hasNext()) {
					// there are elements in the list
					// thus, cannot generate a choice point in which hasNext=false
					// thus, no choice point is created and it is always true...
					frame.getVm().setPC(this.lineNumber + (this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]));
				} else {
					// generate two choice points in which 
					// (1) hasNext=false and (2) hasNext=true
					JPAVirtualMachine jpaVM = (JPAVirtualMachine)frame.getVm();
					jpaVM.generateNewCollectionChoicePoint(this, symHasNext.getIterator().getCollectionVariable());
				}
			} else {
				// normal execution 
				Term term1 = (Term) term;

				// Check if the term is constant.
				if (term1.isConstant()) {
					// Execute without generating a choice point.
					int value = ((IntConstant) term1).getIntValue();
					if (compare(value)) {
						frame.getVm().setPC(this.lineNumber + (this.otherBytes[0] << ONE_BYTE | this.otherBytes[1]));
					}
				} else {
					// Create the ConstraintExpression and generate a new ChoicePoint. It will set the pc.
					Term term2 = IntConstant.getInstance(0);
					ConstraintExpression expression = getConstraintExpression(term1, term2);
					System.out.println("*** add constraint expresion via #if: " + expression + " <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
					((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this, expression);
				}
			}
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		}
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
	 * Compare the value to 0. Return true if the expected condition is met, false otherwise.
	 *
	 * @param value The value to compare to 0.
	 * @return true if the expected condition is met, false otherwise.
	 */
	protected abstract boolean compare(int value);

	/**
	 * Get the ConstraintExpression for this instruction.
	 * @param term1 The left-hand term.
	 * @param term2 The right-hand term.
	 * @return A new ConstraintExpression.
	 */
	protected abstract ConstraintExpression getConstraintExpression(Term term1, Term term2);

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
		byte[] types = {ClassFile.T_INT, ClassFile.T_INT};
		return types;
	}

}
