package de.wwu.muggl.instructions.typed;

import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * This class provides static methods to be accessed by instructions typed as a Byte.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class ByteInstruction extends TypedInstruction {

	/**
	 * Get a String representation of the desired type of this instruction. This method is used to
	 * check whather the fetched Object is an instance of the desired type. This currently is not
	 * used by any instruction typed as Byte.
	 * @return null.
	 */
	@Override
	public String getDesiredType() {
		return null;
	}

	/**
	 * Get the int representation of the type that will be wrapped by this Variable.
	 *
	 * @return The value of SymbolicVirtualMachineElement.BYTE.
	 * @see de.wwu.testtool.expressions.Expression#BYTE
	 */
	@Override
	public int[] getDesiredSymbolicalTypes() {
		int[] types = {Expression.BOOLEAN, Expression.BYTE};
		return types;
	}

	/**
	 * Get a new byte variable for the symbolic execution.
	 * @param method The Method the variable is a parameter of.
	 * @param localVariable The index into the local variables (required for the correct naming of the variable generated).
	 * @return An instance of NumericVariable.
	 */
	@Override
	public Variable getNewVariable(Method method, int localVariable, VirtualMachine vm) {
		return new NumericVariable(generateVariableNameByNumber(method, localVariable), Expression.BYTE, false);
	}

	/**
	 * Get a String array representation of the desired types of this instruction. This method is used to
	 * check whather the fetched Object is an instance of one of the desired types.
	 * @return A String representation of the desired type.
	 */
	@Override
	public String[] getDesiredTypes() {
		String[] desiredTypes = {"java.lang.Boolean", "java.lang.Byte", "java.lang.Integer"};
		return desiredTypes;
	}

	/**
	 * Extend the value if this is required by its' type. For java.lang.Boolean use zero-extension,
	 * for java.lang.Byte use sign-extension.
	 * @param value The object that needs to be extended.
	 * @param type The type of the object, which determines the extension strategy.
	 * @return The extended value.
	 */
	@Override
	protected Object extendValue(Object value, String type) {
		if (type.equals("java.lang.Boolean")) {
			value = ((Boolean) value).booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0);
		} else {
			value = ((Byte) value).intValue();
		}
		return value;
	}

	/**
	 * Truncate the value if this is required by its' type. In this case the integer value is
	 * truncated to a byte.
	 * @param value The object that needs to be truncated.
	 * @param type The type of the object, which determines the truncation strategy.
	 * @return The truncated value.
	 */
	@Override
	protected Object truncateValue(Object value, String type) {
		if (type.equals("java.lang.Boolean")) {
			value = ((Integer) value).intValue() == 1 ? new Boolean(true) : Boolean.FALSE;
		} else {
			value = ((Integer) value).byteValue();
		}
		return value;
	}

	/**
	 * Truncate the symbolic value if this is required by its' type. In this case the integer value
	 * is truncated to a byte.
	 *
	 * @param term The Term that needs to be truncated.
	 * @param type The type of the Term, which determines the truncation strategy.
	 * @return The truncated term.
	 */
	@Override
	protected Term truncateSymbolicValue(Term term, int type) {
		// TODO
		return term;
	}

}
