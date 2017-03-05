package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.If_icmp;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * Implementation of the instruction <code>if_icmpge</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2008-11-04
 */
public class If_icmpge extends If_icmp implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument. Also the line number is given as an argument, since
	 * it is needed in case of jumping.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param lineNumber The line number of this instruction (including other bytes!).
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public If_icmpge(AttributeCode code, int lineNumber) throws InvalidInstructionInitialisationException {
		super(code, lineNumber);
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "if_icmpge";
	}

	/**
	 * Compare two int values. Return true if the expected condition is met, false otherwise.
	 *
	 * @param value1 The first int value.
	 * @param value2 The second int value.
	 * @return true if the expected condition is met, false otherwise.
	 */
	@Override
	protected boolean compare(int value1, int value2) {
		if (value1 >= value2) return true;
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
		return GreaterOrEqual.newInstance(term1, term2);
	}

}
