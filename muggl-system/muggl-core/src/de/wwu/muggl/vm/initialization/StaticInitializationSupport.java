package de.wwu.muggl.vm.initialization;

import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.solvers.expressions.BooleanConstant;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;

/**
 * Provides static methods that are needed for initialization procedures.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class StaticInitializationSupport {

	/**
	 * Protected default constructor.
	 */
	protected StaticInitializationSupport() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Provide a primitive type wrapper that is initialized to the primitive types initial value.<br />
	 * <br />
	 * Primitive types in java in contradiction to reference types - thats' initial value is null -
	 * have an initial value. Hence, if an primitive type is encountered first and it is not
	 * explicitly initialized, it ha to be set to its initial value. As the java.lang wrapper types
	 * are used to represent primitive types in this virtual machine implementation, a new wrapper
	 * type object is constructed thats value will be the primitive type's initialization value.<br />
	 * <br />
	 * This method also provides support for the symbolic invocation. If the execution mode is set to
	 * symbolic, the returned value will be a symbolic one automatically.
	 *
	 * @param type A String representing the type of the primitive value, e.g. "int".
	 * @param isWrapperType If set to true, a java.lang wrapper class will be expected as the type,
	 *        e.g. "java.lang.String".
	 * @return An java.lang primitive wrapper object with the primitive type's initial value; or
	 *         null, should the type description be unsuitable.
	 */
	public static Object getInitializedPrimitiveTypeWrapper(String type, boolean isWrapperType) {
		return getInitializedPrimitiveTypeWrapper(type, isWrapperType,
				Options.getInst().symbolicMode);
	}

	/**
	 * Provide a primitive type wrapper that is initialized to the primitive types initial value.<br />
	 * <br />
	 * Primitive types in java in contradiction to reference types - thats' initial value is null -
	 * have an initial value. Hence, if an primitive type is encountered first and it is not
	 * explicitly initialized, it ha to be set to its initial value. As the java.lang wrapper types
	 * are used to represent primitive types in this virtual machine implementation, a new wrapper
	 * type object is constructed thats value will be the primitive type's initialization value.<br />
	 * <br />
	 * This method offers the possibility to chose a symbolic type as the returned value.
	 *
	 * @param type A String representing the type of the primitive value, e.g. "int".
	 * @param isWrapperType If set to true, a java.lang wrapper class will be expected as the type,
	 *        e.g. "java.lang.String".
	 * @param symbolicalType If set to true, a symbolic type will be provided.
	 * @return An java.lang primitive wrapper object with the primitive type's initial value; or
	 *         null, should the type description be unsuitable.
	 */
	public static Object getInitializedPrimitiveTypeWrapper(String type, boolean isWrapperType,
			boolean symbolicalType) {
		// Symbolic mode?
		if (!symbolicalType) {
			// Is the Type description a wrapper type?
			if (isWrapperType) {
				if (type.equals("java.lang.Doble"))
					return Double.valueOf(0.0);
				else if (type.equals("java.lang.Float"))
					return Float.valueOf(0.0F);
				else if (type.equals("java.lang.Integer"))
					return Integer.valueOf(0);
				else if (type.equals("java.lang.Byte"))
					return Byte.valueOf((byte) 0);
				else if (type.equals("java.lang.Character"))
					return Character.valueOf('\u0000');
				else if (type.equals("java.lang.Short"))
					return Short.valueOf((short) 0);
				else if (type.equals("java.lang.Boolean"))
					return Boolean.valueOf(false);
				else if (type.equals("java.lang.Long"))
					return Long.valueOf(0L);
				return null;
			}

			if (type.equals("byte")) {
				return Byte.valueOf((byte) 0);
			} else if (type.equals("short")) {
				return Short.valueOf((short) 0);
			} else if (type.equals("int")) {
				return Integer.valueOf(0);
			} else if (type.equals("long")) {
				return Long.valueOf(0L);
			} else if (type.equals("float")) {
				return Float.valueOf(0.0F);
			} else if (type.equals("double")) {
				return Double.valueOf(0.0);
			} else if (type.equals("char")) {
				return Character.valueOf('\u0000');
			} else if (type.equals("boolean")) {
				return Boolean.FALSE;
			} else {
				return null;
			}
		}
	
		// Is the Type description a wrapper type?
		if (isWrapperType) {
			if (type.equals("java.lang.Doble"))
				return DoubleConstant.getInstance(0.0);
			else if (type.equals("java.lang.Float"))
				return FloatConstant.getInstance(0.0F);
			else if (type.equals("java.lang.Integer"))
				return IntConstant.getInstance(0);
			else if (type.equals("java.lang.Byte"))
				return IntConstant.getInstance(0);
			else if (type.equals("java.lang.Character"))
				return IntConstant.getInstance(0);
			else if (type.equals("java.lang.Short"))
				return IntConstant.getInstance(0);
			else if (type.equals("java.lang.Boolean"))
				return BooleanConstant.getInstance(false);
			else if (type.equals("java.lang.Long"))
				return LongConstant.getInstance(0L);
			return null;
		}
	
		if (type.equals("byte")) {
			return IntConstant.getInstance(0);
		} else if (type.equals("short")) {
			return IntConstant.getInstance(0);
		} else if (type.equals("int")) {
			return IntConstant.getInstance(0);
		} else if (type.equals("long")) {
			return LongConstant.getInstance(0L);
		} else if (type.equals("float")) {
			return FloatConstant.getInstance(0.0F);
		} else if (type.equals("double")) {
			return DoubleConstant.getInstance(0.0);
		} else if (type.equals("char")) {
			return IntConstant.getInstance(0);
		} else if (type.equals("boolean")) {
			return BooleanConstant.getInstance(false);
		} else {
			return null;
		}
	}
}
