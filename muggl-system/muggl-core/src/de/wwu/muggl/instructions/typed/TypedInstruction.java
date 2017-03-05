package de.wwu.muggl.instructions.typed;

import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.Term;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * This abstract class is to be extended by all classes that offer static methods for typed instructions.
 * A lot of the bytecode instructions have the same function, but address diferent types. I common, those
 * instructions are available for References (prefix a), Doubles (d), Floats (f), Integers (i) and
 * Longs (l). Some are also available for Bytes (b), Characters (c) and Shorts (s).
 *
 * Beside the definition of abstract methods, it offers both static and instance methods for type checking.
 *
 * Last modified: 2008-09-16
 *
 * @author Tim Majchrzak
 * @version 1.0.0
 */
public abstract class TypedInstruction {

	/**
	 * Get a String representation of the desired type of this instruction. This method is used to
	 * check whether the fetched Object is an instance of the desired type.
	 * @return A String representation of the desired type.
	 */
	public abstract String getDesiredType();

	/**
	 * Check if the objectref is of the desired type.
	 * @param objectref The objectref to check.
	 * @return true, if the objectref is the desired type, false otherwise.
	 */
	public boolean checkDesiredType(String objectref) {
		if (objectref.equals(getDesiredType())) return true;
		return false;
	}

	/**
	 * Get a int representation of the desired symbolic type of this instruction as defined in
	 * de.wwu.testtool.structures.environment.object.SymbolicVirtualMachineElement. This method is
	 * used to check whether the fetched Object is an instance of the desired symbolic type.
	 *
	 * @return The int representation of the wrapping type, or -1 if there is no such type.
	 * @see de.wwu.muggl.solvers.expressions.Expression#BYTE
	 * @see de.wwu.muggl.solvers.expressions.Expression#CHAR
	 * @see de.wwu.muggl.solvers.expressions.Expression#DOUBLE
	 * @see de.wwu.muggl.solvers.expressions.Expression#FLOAT
	 * @see de.wwu.muggl.solvers.expressions.Expression#INT
	 * @see de.wwu.muggl.solvers.expressions.Expression#LONG
	 * @see de.wwu.muggl.solvers.expressions.Expression#SHORT
	 */
	public abstract int[] getDesiredSymbolicalTypes();

	/**
	 * Get a new variable for the symbolic execution.
	 * @param method The Method the variable is a parameter of.
	 * @param localVariable The index into the local variables (required for the correct naming of the variable generated).
	 * @return An instance of Variable without a super term set.
	 */
	public abstract Variable getNewVariable(Method method, int localVariable, VirtualMachine vm);

	/**
	 * Get a String array representation of the desired types of this instruction. This method is used to
	 * check whether the fetched Object is an instance of one of the desired types.
	 * @return A String representation of the desired type.
	 */
	public abstract String[] getDesiredTypes();

	/**
	 * Validate the value and extend if it necessary.
	 *
	 * @param value The (probably extended) value to check.
	 * @return true, if value is of one of the desired types, false otherwise.
	 * @throws ExecutionException If the supplied value does not match the expected type, an
	 *         ExecutionException is thrown.
	 */
	public Object validateAndExtendValue(Object value) throws ExecutionException {
		String[] desiredTypes = getDesiredTypes();
		boolean matchedOneType = false;
		for (int a = 0; a < desiredTypes.length; a++) {
			if (value.getClass().getName().equals(desiredTypes[a])) {
				matchedOneType = true;
				value = extendValue(value, desiredTypes[a]);
				break;
			}
		}

		if (!matchedOneType)
			throw new ExecutionException("Expected an array of " + desiredTypes[0] + ", got an array of " + value.getClass().getName() + ".");

		return value;
	}

	/**
	 * Extend the value if this is required by its' type. For most inheriting instructions this is not
	 * needed, so the default behavior is to just return the suplied value.
	 * @param value The object that might need to be extended.
	 * @param type The type of the object, which determines the extension strategy.
	 * @return The (probably extended) value.
	 */
	protected Object extendValue(Object value, String type) {
		return value;
	}

	/**
	 * Validate and truncate the value. In general, it only is checked if the value has the correct
	 * type. For some inheriting instructions it is truncated then. This methods can be overridden.
	 * It however is only overridden by class AAstore, since the aastore instruction requires a more
	 * complicated way of checking the value.
	 *
	 * @param value The value that is to be stored into an array.
	 * @param arrayref The array reference.
	 * @param frame The currently executed frame.
	 * @return The (probably truncated) value.
	 * @throws ExecutionException If the supplied value does not match the expected type, an
	 *         ExecutionException is thrown.
	 * @throws VmRuntimeException If a runtime exception happened and should be handled by
	 *         the exception handler.
	 */
	@SuppressWarnings("unused")
	public Object validateAndTruncateValue(Object value, Object arrayref, Frame frame) throws ExecutionException, VmRuntimeException {
		String[] desiredTypes = getDesiredTypes();
		boolean matchedOneType = false;
		for (int a = 0; a < desiredTypes.length; a++) {
			if (value.getClass().getName().equals(desiredTypes[a])) {
				matchedOneType = true;
				value = truncateValue(value, desiredTypes[a]);
				break;
			}
		}

		if (!matchedOneType)
			throw new ExecutionException("Expected an array of " + desiredTypes[0] + ", got an array of " + value.getClass().getName() + ".");

		return value;
	}

	/**
	 * Validate a return value. The validateValue()-Method is used to archive this.
	 * @param value The value that is to be returned.
	 * @param frame The currently executed frame.
	 * @return The value.
	 * @throws ExecutionException If the supplied value does not match the expected type, an ExecutionException is thrown.
	 * @throws VmRuntimeException If a runtime exception happened and should be handled by the exception handler.
	 */
	public Object validateReturnValue(Object value, Frame frame) throws ExecutionException, VmRuntimeException {
		return validateAndTruncateValue(value, null, frame);
	}

	/**
	 * Truncate the value if this is required by its' type. For most inheriting instructions this is not
	 * needed, so the default behaviour is to just return the suplied value.
	 * @param value The object that might need to be truncated.
	 * @param type The type of the object, which determines the truncation strategy.
	 * @return The (probably truncated) value.
	 */
	@SuppressWarnings("unused")
	protected Object truncateValue(Object value, String type) {
		return value;
	}

	/**
	 * Validate and truncate the value. In general, it only is checked if the value has the correct
	 * type. For some inheriting instructions it is truncated then. This methods can be overridden.
	 * It however is only overridden by class AAstore, since the aastore instruction requires a more
	 * complicated way of checking the value.
	 *
	 * @param term The term that is to be stored into an array.
	 * @param arrayref The array reference.
	 * @param frame The currently executed frame.
	 * @return The (probably truncated) term.
	 * @throws SymbolicExecutionException If the supplied value does not match the expected type,
	 *         an ExecutionException is thrown.
	 * @throws VmRuntimeException If a runtime exception happened and should be handled by
	 *         the exception handler.
	 */
	@SuppressWarnings("unused")
	public Term validateAndTruncateSymbolicValue(Term term, Object arrayref, Frame frame) throws SymbolicExecutionException, VmRuntimeException {
		int[] desiredTypes = getDesiredSymbolicalTypes();
		boolean matchedOneType = false;
		for (int a = 0; a < desiredTypes.length; a++) {
			if (term.getType() == desiredTypes[a]) {
				matchedOneType = true;
				term = truncateSymbolicValue(term, desiredTypes[a]);
				break;
			}
		}

		if (!matchedOneType)
			throw new SymbolicExecutionException("Expected an array of " + Term.getTypeName((byte) desiredTypes[0]) + ", got an array of " + Term.getTypeName(term.getType()) + ".");

		return term;
	}

	/**
	 * Truncate the symbolic value if this is required by its' type. For most inheriting
	 * instructions this is not needed, so the default behaviour is to just return the suplied
	 * value.
	 *
	 * @param term The Term that might need to be truncated.
	 * @param type The type of the Term, which determines the truncation strategy.
	 * @return The (probably truncated) term.
	 */
	protected Term truncateSymbolicValue(Term term, int type) {
		return term;
	}

	/**
	 * Get the String value corresponding for a int value of a wrappedType of the Term class.
	 * @param wrappedType The int value representing the wrapped type.
	 * @return The String representation for the wrapped type, or null, if the wrappedType is not recognized.
	 */
	public static String getStringRepresentationForWrappedType(int wrappedType) {
		if (wrappedType == Expression.BYTE) {
			return "java.lang.Byte";
		} else if (wrappedType == Expression.CHAR) {
			return "java.lang.Character";
		} else if (wrappedType == Expression.DOUBLE) {
			return "java.lang.Double";
		} else if (wrappedType == Expression.FLOAT) {
			return "java.lang.Float";
		} else if (wrappedType == Expression.INT) {
			return "java.lang.Integer";
		} else if (wrappedType == Expression.LONG) {
			return "java.lang.Long";
		} else if (wrappedType == Expression.SHORT) {
			return "java.lang.Short";
		} else if (wrappedType == Expression.STRING) {
			return "java.lang.String";
		}
		return null;
	}

	/**
	 * Returns a suitable String representation of this variable. If parameter names are
	 * available, the corresponding parameter name will be used. Otherwise, a name will be
	 * generated depending on the number supplied. In this case the output will be:
	 * - first a-z
	 * - then aa to az
	 * - then ba to zz
	 * - then aaa to zzz
	 * - etc.
	 * @param method The Method the variable belong to.
	 * @param number The number of this variable. Same numbers mean same variables.
	 * @return The variable name of the variable at position <i>number</i> in the supplied method, or a suitable variable name for the number supplied if there are no names specified.
	 */
	public static String generateVariableNameByNumber(Method method, int number) {
		if (method.parameterNamesAvailable()) {
			return method.getParameterName(number);
		}
		final int NUMBER_OF_SIGNS = 26;

		int[] dimensions = new int[1];
		while (number > 0) {
			// ending the algorithm if we can fill the first dimension
			if (number < NUMBER_OF_SIGNS) {
				dimensions[0] = number;
				number = -1;

			} else {
				// counting up the later dimensions
				boolean notIncreased = true;
				int dimensionCounter = 1;
				while (notIncreased) {
					if (dimensionCounter == dimensions.length) {
						// create a new dimensions
						dimensions = new int[dimensions.length + 1];
						for (int a = 1; a < dimensions.length; a++) {
							dimensions[a] = 0;
						}
						number -= NUMBER_OF_SIGNS;
						notIncreased = false;
						// count up another dimension
					} else if (dimensions[dimensionCounter] == NUMBER_OF_SIGNS - 1) {
						// getting further to the right
						dimensionCounter++;
					} else {
						// just increase this dimension
						dimensions[dimensionCounter]++;
						number -= NUMBER_OF_SIGNS;
						notIncreased = false;
					}
				}
			}
		}

		String returnedValue = "";
		for (int a = 0; a < dimensions.length; a++) {
			returnedValue += String.valueOf((char) (dimensions[a] + 97));
		}
		return returnedValue;
	}
}
