package de.wwu.muggl.vm.impl.symbolic.support;

import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;

/**
 * This class provides static methods for the conversion of objects from java to symbolic types
 * and the other way around. It offers both methods that directly take distinct types as well as
 * general methods that will process various types.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class JavaToSymbolicConversion {

	/**
	 * Protected default constructor.
	 */
	protected JavaToSymbolicConversion() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Provide a symbolic constant value for the object specified. If the object cannot be
	 * converted, an exception will be thrown. Any instance of a primitive type java.lang wrapper
	 * class will be processed by this method.
	 *
	 * @param object The java object to convert.
	 * @return The symbolic constant.
	 * @throws IllegalArgumentException If the specified object has a type that cannot be converted
	 *         to a symbolic type or is an array.
	 */
	public static Constant tryToSymbolic(Object object) {
		if (object.getClass().isArray())
			throw new IllegalArgumentException(
					"Array cannot be process. Please convert array elements one by one.");

		// Check the type of the specified object.
		if (object instanceof Boolean) {
			return toSymbolic(((Boolean) object).booleanValue() ? 1 : 0);
		} else if (object instanceof Byte) {
			return toSymbolic((Byte) object);
		} else if (object instanceof Character) {
			return toSymbolic((Character) object);
		} else if (object instanceof Double) {
			return toSymbolic((Double) object);
		} else if (object instanceof Float) {
			return toSymbolic((Float) object);
		} else if (object instanceof Integer) {
			return toSymbolic((Integer) object);
		} else if (object instanceof Long) {
			return toSymbolic((Long) object);
		} else if (object instanceof Short) {
			return toSymbolic((Short) object);
		}

		// Object cannot be converted to a symbolic equivalent.
		throw new IllegalArgumentException(
				"The specified object has a type that cannot be converted to a symbolic type.");
	}

	/**
	 * Convert a Boolean object to a symbolic boolean constant.
	 *
	 * @param booleanObject The Boolean object to convert.
	 * @return A symbolic boolean constant.
	 */
	public static BooleanConstant toSymbolic(Boolean booleanObject) {
		return BooleanConstant.getInstance(booleanObject.booleanValue());
	}

	/**
	 * Convert a Byte object to a symbolic int constant. (There is no symbolic representation of
	 * a byte.)
	 *
	 * @param byteObject The Byte object to convert.
	 * @return A symbolic int constant.
	 */
	public static IntConstant toSymbolic(Byte byteObject) {
		return IntConstant.getInstance(byteObject.intValue());
	}

	/**
	 * Convert a Character object to a symbolic int constant. (There is no symbolic
	 * representation of a character.)
	 *
	 * @param characterObject The Character object to convert.
	 * @return A symbolic int constant.
	 */
	public static IntConstant toSymbolic(Character characterObject) {
		return IntConstant.getInstance(characterObject);
	}

	/**
	 * Convert a Double object to a symbolic double constant.
	 *
	 * @param doubleObject The Double object to convert.
	 * @return A symbolic double constant.
	 */
	public static DoubleConstant toSymbolic(Double doubleObject) {
		return DoubleConstant.getInstance(doubleObject.doubleValue());
	}

	/**
	 * Convert a Float object to a symbolic float constant.
	 *
	 * @param floatObject The Float object to convert.
	 * @return A symbolic float constant.
	 */
	public static FloatConstant toSymbolic(Float floatObject) {
		return FloatConstant.getInstance(floatObject.floatValue());
	}

	/**
	 * Convert an Integer object to a symbolic int constant.
	 *
	 * @param integerObject The Integer object to convert.
	 * @return A symbolic int constant.
	 */
	public static IntConstant toSymbolic(Integer integerObject) {
		return IntConstant.getInstance(integerObject.intValue());
	}

	/**
	 * Convert a Long object to a symbolic long constant.
	 *
	 * @param longObject The Long object to convert.
	 * @return A symbolic long constant.
	 */
	public static LongConstant toSymbolic(Long longObject) {
		return LongConstant.getInstance(longObject.longValue());
	}

	/**
	 * Convert a Short object to a symbolic int constant. (There is no symbolic representation
	 * of a short.)
	 *
	 * @param shortObject The Short object to convert.
	 * @return A symbolic int constant.
	 */
	public static IntConstant toSymbolic(Short shortObject) {
		return IntConstant.getInstance(shortObject.intValue());
	}

	/**
	 * Provide a primitive type java.lang wrapper object for the symbolic constant specified. The
	 * only java objects resulting from this operation may be {@link java.lang.Boolean},
	 * {@link java.lang.Double}, {@link java.lang.Float}, {@link java.lang.Integer} or
	 * {@link java.lang.Long}.
	 *
	 * @param constant The symbolic constant to convert.
	 * @return A primitive type java.lang wrapper object for the symbolic constant specified.
	 */
	public static Object tryToJava(Constant constant) {
		return tryToJava(constant, null);
	}

	/**
	 * Provide a primitive type java.lang wrapper object for the symbolic constant specified. The
	 * only java objects resulting from this operation may be {@link java.lang.Boolean},
	 * {@link java.lang.Byte}, {@link java.lang.Character} {@link java.lang.Double},
	 * {@link java.lang.Float}, {@link java.lang.Integer}, {@link java.lang.Long} or
	 * {@link java.lang.Short}. However, to have a Byte, Character or Short returned the <i>type</i>
	 * parameter has to be set explicitly <b>and</b> the specified constant has to be of type
	 * {@link de.wwu.testtool.expressions.IntConstant}.
	 *
	 * @param constant The symbolic constant to convert.
	 * @param type The type of the desired object. May be {@link java.lang.Byte#TYPE},
	 *        {@link java.lang.Character#TYPE}, {@link java.lang.Short#TYPE} or null.
	 * @return A primitive type java.lang wrapper object for the symbolic constant specified.
	 */
	public static Object tryToJava(Constant constant, Class<?> type) {
		// Check the type of the specified object.
		if (constant instanceof BooleanConstant) {
			return toJava((BooleanConstant) constant);
		} else if (constant instanceof DoubleConstant) {
			return toJava((DoubleConstant) constant);
		} else if (constant instanceof FloatConstant) {
			return toJava((FloatConstant) constant);
		} else if (constant instanceof LongConstant) {
			return toJava((LongConstant) constant);
		} else {
			// It has to be an IntConstant.
			IntConstant intConstant = (IntConstant) constant;
			if (type == null) {
				// If no type is specified, always return an Integer.
				return toJava(intConstant);
			}

			// Check the type.
			if (type == Byte.TYPE) {
				return toJavaByte(intConstant);
			} else if (type == Character.TYPE) {
				return toJavaCharacter(intConstant);
			} else if (type == Short.TYPE) {
				return toJavaShort(intConstant);
			} else {
				// No matching type found. Return an Integer.
				return toJava(intConstant);
			}
		}
	}

	/**
	 * Convert a symbolic boolean constant to a Boolean object.
	 *
	 * @param booleanConstant The symbolic boolean constant to convert.
	 * @return A Boolean object.
	 */
	public static Boolean toJava(BooleanConstant booleanConstant) {
		return Boolean.valueOf(booleanConstant.getValue());
	}

	/**
	 * Convert a symbolic int constant to a Byte object. (There is no symbolic representation
	 * of a byte.)
	 *
	 * @param intConstant The symbolic int constant to convert.
	 * @return A Byte object.
	 */
	public static Byte toJavaByte(IntConstant intConstant) {
		return Byte.valueOf((byte) intConstant.getValue());
	}

	/**
	 * Convert a symbolic int constant to a Character object. (There is no symbolic representation
	 * of a character.)
	 *
	 * @param intConstant The symbolic int constant to convert.
	 * @return A Character object.
	 */
	public static Character toJavaCharacter(IntConstant intConstant) {
		return Character.valueOf((char) intConstant.getValue());
	}

	/**
	 * Convert a symbolic double constant to a Double object.
	 *
	 * @param doubleConstant The symbolic double constant to convert.
	 * @return A Double object.
	 */
	public static Double toJava(DoubleConstant doubleConstant) {
		return Double.valueOf(doubleConstant.getValue());
	}

	/**
	 * Convert a symbolic float constant to a object.
	 *
	 * @param floatConstant The symbolic float constant to convert.
	 * @return A Float object.
	 */
	public static Float toJava(FloatConstant floatConstant) {
		return Float.valueOf(floatConstant.getValue());
	}

	/**
	 * Convert a symbolic int constant to a Integer object.
	 *
	 * @param intConstant The symbolic int constant to convert.
	 * @return An Integer object.
	 */
	public static Integer toJava(IntConstant intConstant) {
		return Integer.valueOf(intConstant.getValue());
	}

	/**
	 * Convert a long symbolic constant to a Long object.
	 *
	 * @param longConstant The symbolic long constant to convert.
	 * @return A Long object.
	 */
	public static Long toJava(LongConstant longConstant) {
		return Long.valueOf(longConstant.getValue());
	}

	/**
	 * Convert a symbolic int constant to a Short object. (There is no symbolic representation
	 * of a short.)
	 *
	 * @param intConstant The symbolic int constant to convert.
	 * @return A Short object.
	 */
	public static Short toJavaShort(IntConstant intConstant) {
		return Short.valueOf((short) intConstant.getValue());
	}

}
