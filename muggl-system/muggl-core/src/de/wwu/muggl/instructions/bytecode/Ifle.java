package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.If;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.LessOrEqual;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction <code>ifle</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-02-03
 */
public class Ifle extends If implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument. Also the line number is given as an argument, since
	 * it is needed in case of jumping.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param lineNumber The line number of this instruction (including other bytes!).
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Ifle(AttributeCode code, int lineNumber) throws InvalidInstructionInitialisationException {
		super(code, lineNumber);
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "ifle";
	}

	/**
	 * Compare the value to 0. Return true if it is less than or equal to it.
	 * @param value The value to compare to 0.
	 * @return true if the expected condition is met, false otherwise.
	 */
	@Override
	protected boolean compare(int value) {
		if (value <= 0) return true;
		return false;
	}

	/**
	 * Get the ConstraintExpression for this instruction.
	 * @param term1 The left-hand term.
	 * @param term2 The right-hand term.
	 * @return A new ConstraintExpression.
	 */
	@Override
	protected ConstraintExpression getConstraintExpression(Term term1, Term term2) {
		return LessOrEqual.newInstance(term1, term2);
	}

}
