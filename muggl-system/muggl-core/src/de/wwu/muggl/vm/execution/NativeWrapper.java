package de.wwu.muggl.vm.execution;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.UndefinedValue;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * This class provides the functionality to wrap native methods calls. It therefore offers several
 * means of processing native methods.<br />
 * <br />
 * Methods from the java-package can be forwarded to the java virtual machine that Muggl runs on.
 * For some native methods special wrapper classes can be snapped-in.<br />
 * <br />
 * The wrapping process does currently not check accessibility beside the normal accessibility check
 * done by the invocation instructions. Whenever possible, access is forced to be granted. Not
 * forcing access will not work in common, since many native methods have package or even private
 * visibility only; still the reflection objects are invoked by this wrapper and not by the original
 * class. This bridge from the "world" of this application and its virtual machine would lead to
 * problems, if accessed was not forced.<br />
 * <br />
 * In case of invalid class files or recompilation of just a couple of files of a projects, where
 * the visibility of classes or methods has been changed, might lead to a behavior that does not
 * reflect the behavior the virtual machine of sun would show. However, this is unlikely.<br />
 * <br />
 * In future releases, a dedicated access right checking could be added to this wrapper.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-01-15
 */
public class NativeWrapper {

	/**
	 * Protected default constructor.
	 */
	protected NativeWrapper() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Forward the method to its native implementation in the java-package or to a special wrapper
	 * class.
	 *
	 * @param frame The currently executed frame.
	 * @param method The method to forward.
	 * @param methodClassFile The methods' ClassFile.
	 * @param invokingObjectref The object reference invoking the method. Should be null for static
	 *        method calls.
	 * @param parameters The parameters for the invocation of the method.
	 * @throws ForwardingUnsuccessfulException If forwarding fails.
	 * @throws VmRuntimeException If the invoked method throws an exception (which is a valid result
	 *         of the invocation).
	 */
	public static void forwardNativeInvocation(
			Frame frame,
			Method method,
			ClassFile methodClassFile,
			Object invokingObjectref,
			Object[] parameters
			) throws ForwardingUnsuccessfulException, VmRuntimeException {
		Stack<Object> stack = frame.getOperandStack();
		MugglToJavaConversion conversion = new MugglToJavaConversion(frame.getVm());

		if( invokingObjectref != null && !(invokingObjectref instanceof Objectref) && !(invokingObjectref instanceof Arrayref)) {
			throw new RuntimeException("Cannot fowared native invocation with no objectref or arrayref");
		}
		
		// Forward to a native implementation in the java-package?
		if (methodClassFile.getPackageName().startsWith("java.")) {
			/*
			 * Check if there is a special handling desired for this method. If it is, it will be
			 * applied directly and native handling is finished upon returning from it.
			 */
			if (invokingObjectref instanceof Objectref && javaPackageSpecialHandling(frame, method, methodClassFile, (Objectref)invokingObjectref, parameters))
				return;

			// Proceed with the general forwarding procedure.
			try {
				// Save the original parameters.
				Object[] originalParameters = new Object[parameters.length];
				System.arraycopy(parameters, 0, originalParameters, 0, parameters.length);

				// Get the types of the parameters as Class objects.
				String[] parameterTypesAsString = method.getParameterTypesAsArray();
				Class<?>[] parameterTypes;

				// Are there any parameters at all?
				if (parameterTypesAsString.length > 0) {
					parameterTypes = new Class<?>[parameterTypesAsString.length];
					// Check the String representations of the parameter types.
					for (int a  = 0; a < parameterTypesAsString.length; a++) {
						// The class to initialize.
						Class<?> classTemp = null;

						// Do we have a primitive type or even an array of it?
						int arrayDimensions = 0;
						while (parameterTypesAsString[a].contains("[]")) {
							arrayDimensions++;
							parameterTypesAsString[a] = parameterTypesAsString[a].substring(0, parameterTypesAsString[a].length() - 2);
						}

						// Get the information to instantiate the matching Class wrapper.
						if (parameterTypesAsString[a].equals("boolean"))  {
							classTemp = java.lang.Boolean.TYPE;
						} else if (parameterTypesAsString[a].equals("byte")) {
							classTemp = java.lang.Byte.TYPE;
						} else if (parameterTypesAsString[a].equals("char")) {
							classTemp = java.lang.Character.TYPE;
						} else if (parameterTypesAsString[a].equals("short")) {
							classTemp = java.lang.Short.TYPE;
						} else if (parameterTypesAsString[a].equals("int")) {
							classTemp = java.lang.Integer.TYPE;
						} else if (parameterTypesAsString[a].equals("double")) {
							classTemp = java.lang.Double.TYPE;
						} else if (parameterTypesAsString[a].equals("float")) {
							classTemp = java.lang.Float.TYPE;
						} else if (parameterTypesAsString[a].equals("long")) {
							classTemp = java.lang.Long.TYPE;
						} else {
							classTemp = methodClassFile.getClassLoader().getClassAsClassFile(parameterTypesAsString[a]).getInstanceOfClass();
						}

						// Initialize an array?
						if (arrayDimensions > 0) {
							parameterTypes[a] = Array.newInstance(classTemp, arrayDimensions).getClass();
						} else {
							parameterTypes[a] = classTemp;
						}
					}
				} else {
					// No parameters are symbolized by an Class<?> array of zero size.
					parameterTypes = new Class<?>[0];
				}

				// Convert parameters if necessary.
				for (int a = 0; a < parameters.length; a++) {
					parameters[a] = conversion.toJava(parameters[a]);
				}

				// Use reflection to get a java.lang.reflect.Method.
				String methodName = method.getName();
				Class<?> methodClass = methodClassFile.getInstanceOfClass();
				Object object = null;

				// Now distinguish between the kind of method.
				if (methodName.equals("<clinit>")) {
					/*
					 * The static initializer cannot be fetched with getDeclaredMethod(). Just get an instance of the
					 * class instead, of course using reflection. Encountering an invocation of the static initializer
					 * within a java application is not to be expected. For better compatibility, it is coped with
					 * though.
					 */
					object = methodClass.newInstance();
				} else if (methodName.equals("<init>")) {
					/*
					 * The instance initializer cannot be fetched with getDeclaredMethod(). Find the suiting constructor
					 * by using reflection and use it for initialization.
					 */
					java.lang.reflect.Constructor<?> constructor = methodClass.getDeclaredConstructor(parameterTypes);
					// Force the accessibility - ignoring private access etc.
					constructor.setAccessible(true);
					// Invoke the constructor
					object = constructor.newInstance(parameters);
				} else {
					// Invoke the method directly, it is a "normal" static or instance method.
					java.lang.reflect.Method reflectedMethod = methodClass.getDeclaredMethod(methodName, parameterTypes);
					// Force the accessibility - ignoring private access etc.
					reflectedMethod.setAccessible(true);
					// Invoke the method.
					if(invokingObjectref instanceof Arrayref) {
						Arrayref arrayRef = (Arrayref)invokingObjectref;
						Object javaInvObj = conversion.arrayRefToJavaObject(null, null);
						object = reflectedMethod.invoke(javaInvObj, parameters);
						System.out.println("-> " + object.getClass().isArray());
					} else {
						object = reflectedMethod.invoke(conversion.toJava(invokingObjectref), parameters);
					}
				}

				// Push the return value to the operand stack, if the method is not void.
				if (!method.getReturnType().equals("void")) {
					// Check the return type.
					String returnType = method.getReturnType();
					while (returnType.contains("[]")) {
						returnType = returnType.substring(0, returnType.length() - 2);
					}

					boolean isPrimitive = false;
					if (returnType.equals("boolean") || returnType.equals("byte")
							|| returnType.equals("char") || returnType.equals("short")
							|| returnType.equals("int") || returnType.equals("double")
							|| returnType.equals("float") || returnType.equals("long"))
						isPrimitive = true;

					// Convert if necessary.
					object = conversion.toMuggl(object, isPrimitive);

					// Finally push it.
					stack.push(object);
				}

				/*
				 * If reference values were used as parameters, they might have experiences changes.
				 * Write back the changes. Do necessary conversions before doing so.
				 */
				saveChangesInReferenceParameters(originalParameters, parameters, conversion);
			} catch (ExceptionInInitializerError e) {
				/*
				 * An exception was thrown while invoking the native method. It signalizes that
				 * initialization failed. It has to be caught separately so it is not caught by the
				 * general purpose handler. It then is unwrapped and wrapped into a
				 * VmRuntimeException. The wrapped exception is a valid result of the method
				 * invocation, and so it should be given to the exception handler as a runtime
				 * exception.
				 */
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.ExceptionInInitializerError", e.getMessage()));
			} catch (IllegalAccessException e) {
				/*
				 * An exception was thrown while invoking the native method. It signalizes that
				 * access to the method is not possible. It has to be caught separately so it is not
				 * caught by the general purpose handler. It then is unwrapped and wrapped into a
				 * VmRuntimeException. The wrapped exception is a valid result of the method
				 * invocation, and so it should be given to the exception handler as a runtime
				 * exception.
				 */
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IllegalAccessException", e.getMessage()));
			} catch (InstantiationException e) {
				/*
				 * An exception was thrown by the newInstance()-Method when trying to instantiate a
				 * class (since the &lt;init&gt; method cannot be executed. It is a runtime
				 * exception and handled as one. So it is wrapped into a VmRuntimeException and
				 * thrown for further handling by the exception handler.
				 */
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.InstantiationException", e.getMessage()));
			} catch (InvocationTargetException e) {
				/*
				 * An exception was thrown by the invoked method. It has to be caught separately so
				 * it is not caught by the general purpose handler. It then is unwrapped and wrapped
				 * into a VmRuntimeException. The wrapped exception is a valid result of the method
				 * invocation, and so it should be given to the exception handler as a runtime
				 * exception.
				 */
				throw new VmRuntimeException(frame.getVm().generateExc(
						e.getTargetException().getClass().getCanonicalName(), e.getMessage()));
			} catch (Throwable t) {
				/*
				 * General exception handling. In this try block, a lot of different exceptions
				 * might be thrown. After all they have the same effect: Handling the invocation by
				 * forwarding it to a native method of the java package failed, but the failure is
				 * not supposed to happen during the normal execution of native methods and hence
				 * has not to be thrown as a result of the native invocation. So a
				 * ForwardingUnsuccessfulException is thrown, just giving the root cause.
				 */
				throw new ForwardingUnsuccessfulException("Forwarding a native method failed with "
						+ t.getClass().getName() + " with message: " + t.getMessage() + ".");
			}
		} else {
			// A method was forwarded that is not suitable for forwarding.
			throw new ForwardingUnsuccessfulException("In general, only methods from the java package can be forwarded.");
		}
	}

	/**
	 * Check if there is a special handling procedure for a method from the java.lang package or one
	 * of its sub packages; if there is one, use it.<br />
	 * <br />
	 * This method is used to provide means to execute some native methods in special ways outside
	 * the scope of the general forwarding. This can include not actually forwarding them, but using
	 * some internal handling without actually using the wrapper functionality also provided by this
	 * class.
	 *
	 * @param frame The currently executed frame.
	 * @param method The method to forward.
	 * @param methodClassFile The methods' ClassFile.
	 * @param invokingObjectref The object reference invoking the method. Should be null for static
	 *        method calls.
	 * @param parameters The parameters for the invocation of the method.
	 * @return true, if special handling was successful; false otherwise.
	 * @throws VmRuntimeException If the invoked method throws an exception (which is a valid result
	 *         of the invocation).
	 */
	private static boolean javaPackageSpecialHandling(Frame frame, Method method,
			ClassFile methodClassFile, Objectref invokingObjectref, Object[] parameters) throws VmRuntimeException {
		if (methodClassFile.getName().equals("java.lang.String") && method.getName().equals("intern")) {
			// Provide an unique String object reference.
			Objectref stringObjectref = frame.getVm().getStringCache().getStringObjectref(invokingObjectref);
			frame.getOperandStack().push(stringObjectref);

			// Special handling was successful!
			return true;
		} else if (methodClassFile.getName().equals("java.lang.System") && method.getName().equals("arraycopy")) {			
			// Possible exceptions for null parameters
			if (parameters[0] == null || parameters[2] == null) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.NullPointerException", "null"));
			}
			
			// Possible exceptions with regard to types.
			if (!(parameters[0] instanceof Arrayref)) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.ArrayStoreException", "null"));
			}
			if (!(parameters[2] instanceof Arrayref)) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.ArrayStoreException", "null"));
			}
			
			// Get the five parameters.
			Integer srcPos;
			Integer destPos;
			Integer length;
			Arrayref src = (Arrayref) parameters[0];
			Object srcPosObject =  parameters[1];
			Arrayref dest = (Arrayref) parameters[2];
			Object destPosObject = parameters[3];
			Object lengthObject = parameters[4];
			
			if (srcPosObject instanceof IntConstant) {
				srcPos = ((IntConstant) srcPosObject).getIntValue();
			} else {
				srcPos = (Integer) srcPosObject;
			}
			if (destPosObject instanceof IntConstant) {
				destPos = ((IntConstant) destPosObject).getIntValue();
			} else {
				destPos = (Integer) destPosObject;
			}
			if (lengthObject instanceof IntConstant) {
				length = ((IntConstant) lengthObject).getIntValue();
			} else {
				length = (Integer) lengthObject;
			}

			// Further possible exceptions with regard to types.
			if (src.isPrimitive()) {
				if (dest.isPrimitive()) {
					if (src.getInitializedClass() != dest.getInitializedClass()) {
						throw new VmRuntimeException(frame.getVm().generateExc(
								"java.lang.ArrayStoreException", "null"));
					}
				} else {
					throw new VmRuntimeException(frame.getVm().generateExc(
							"java.lang.ArrayStoreException", "null"));
				}
			} else {
				if (dest.isPrimitive()) {
					throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ArrayStoreException",
							"null"));
				}
			}

			// Exceptions that regard the arrays' bounds.
			if (srcPos < 0) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			if (destPos < 0) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			if (length < 0) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			if (srcPos + length > src.length) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			if (destPos + length > dest.length) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			
			// Are both arrays the equal object?
			if (parameters[0] == parameters[2]) {
				src = new Arrayref(dest.getReferenceValue(), length);
				for (int a = srcPos; a < srcPos + length; a++) {
					src.putElement(a - srcPos, dest.getElement(a));
				}
				for (int a = 0; a < length; a++) {
					dest.putElement(destPos + a, src.getElement(a));
				}
			} else {
				for (int a = srcPos; a < srcPos + length; a++) {
					try {
						dest.putElement(destPos + a, src.getElement(a));
					} catch (ArrayStoreException e) {
						// Wrap the exception.
						throw new VmRuntimeException(frame.getVm().generateExc(
								"java.lang.ArrayStoreException", e.getMessage()));
					}
				}
			}
			
			// Special handling was successful!
			return true;
		}

		// Arriving here means no special handling was possible.
		return false;
	}
	
	/* TODO old code
	private static boolean javaPackageSpecialHandling(Frame frame, Method method,
			ClassFile methodClassFile, Objectref invokingObjectref, Object[] parameters) throws VmRuntimeException {
		if (methodClassFile.getName().equals("java.lang.String") && method.getName().equals("intern")) {
			// Provide an unique String object reference.
			Objectref stringObjectref = frame.getVm().getStringCache().getStringObjectref(invokingObjectref);
			frame.getOperandStack().push(stringObjectref);

			// Special handling was successful!
			return true;
		} else if (methodClassFile.getName().equals("java.lang.System") && method.getName().equals("arraycopy")) {			
			// Possible exceptions for null parameters
			if (parameters[0] == null || parameters[2] == null) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.NullPointerException", "null"));
			}
			
			int srcLength = 0;
			int destLength = 0;
			// Possible exceptions with regard to types.
			if (!(parameters[0] instanceof Object[]) && !(parameters[0] instanceof Arrayref)) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.ArrayStoreException", "null"));
			}
			if (!(parameters[2] instanceof Object[]) && !(parameters[2] instanceof Arrayref)) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.ArrayStoreException", "null"));
			}
			if (parameters[0] instanceof Object[]) {
				srcLength = ((Object[]) parameters[0]).length;
				if (parameters[2] instanceof Object[]) {
					if (!parameters[0].getClass().equals(parameters[2].getClass())) {
						throw new VmRuntimeException(frame.getVm().generateExc(
								"java.lang.ArrayStoreException", "null"));
					}
				} else {
					throw new VmRuntimeException(frame.getVm().generateExc(
							"java.lang.ArrayStoreException", "null"));
				}
			} else {
				srcLength = ((Arrayref) parameters[0]).length;
				if (parameters[2] instanceof Object[]) {
					throw new VmRuntimeException(frame.getVm().generateExc("java.lang.ArrayStoreException",
							"null"));
				}
			}
			if (parameters[2] instanceof Object[]) {
				destLength = ((Object[]) parameters[2]).length;
			} else {
				destLength = ((Arrayref) parameters[2]).length;
			}
			
			// Get the five parameters.
			Integer srcPos = (Integer) parameters[1];
			Integer destPos = (Integer) parameters[3];
			Integer length = (Integer) parameters[4];
			
			// Exceptions that regard the arrays' bounds.
			if (srcPos < 0) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			if (destPos < 0) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			if (length < 0) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			if (srcPos + length > srcLength) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			if (destPos + length > destLength) {
				throw new VmRuntimeException(frame.getVm().generateExc(
						"java.lang.IndexOutOfBoundsException", "null"));
			}
			
			// Are both arrays the equal object?
			if (parameters[0] == parameters[2]) {
				// Copy primitive or reference types?
				if (parameters[0] instanceof Object[]) {
					Object[] src = new Object[length];
					Object[] dest = (Object[]) parameters[2];
					for (int a = srcPos; a < srcPos + length; a++) {
						src[a - srcPos] = dest[a];
					}
					for (int a = 0; a < length; a++) {
						dest[destPos + a] = src[a];
					}	
				} else {
					Arrayref dest = (Arrayref) parameters[2];
					Arrayref src = new Arrayref(dest.getReferenceValue(), length);
					for (int a = srcPos; a < srcPos + length; a++) {
						dest.putElement(a - srcPos, dest.getElement(a));
					}
					for (int a = 0; a < length; a++) {
						dest.putElement(destPos + a, src.getElement(a));
					}
				}
			} else {
				// Copy primitive or reference types?
				if (parameters[0] instanceof Object[]) {
					Object[] src = (Object[]) parameters[0];
					Object[] dest = (Object[]) parameters[2];
					for (int a = srcPos; a < srcPos + length; a++) {
						dest[destPos + a] = src[a];
					}	
				} else {
					Arrayref src = (Arrayref) parameters[0];
					Arrayref dest = (Arrayref) parameters[2];
					for (int a = srcPos; a < srcPos + length; a++) {
						try {
							dest.putElement(destPos + a, src.getElement(a));
						} catch (ArrayStoreException e) {
							// Wrap the exception.
							throw new VmRuntimeException(frame.getVm().generateExc(
									"java.lang.ArrayStoreException", e.getMessage()));
						}
					}
				}
			}
			
			// Special handling was successful!
			return true;
		}

		// Arriving here means no special handling was possible.
		return false;
	}
	*/

	/**
	 * Forward the invocation of a native method to a customer wrapper provided by this virtual
	 * machine implementation.
	 *
	 * @param method The method to forward.
	 * @param methodClassFile The methods' ClassFile.
	 * @param parameters The parameters for the invocation of the method.
	 * @param objectref The object reference of the invoking method. Be sure not to confuse it with
	 *        the object reference passed to any invocation instruction but invokestatic.
	 * @return The Object returned by the custom wrapper.
	 * @throws ForwardingUnsuccessfulException If forwarding fails.
	 */
	public static Object forwardToACustomWrapper(Method method, ClassFile methodClassFile,
			Object[] parameters, Objectref objectref) throws ForwardingUnsuccessfulException {
		if (methodClassFile.getPackageName().equals("de.wwu.muggl.vm.execution.nativeWrapping")) {
			// A reference value has been replaced by an instance of a wrapper class provided by this application.
			if (methodClassFile.getName().equals("de.wwu.muggl.vm.execution.nativeWrapping.PrintStreamWrapper")) {
				// There is a wrapper for the PrintStream out of the java.lang.System class with only one method wrapped.
				if (method.getName().equals("writeToLogfile")) {
					// Fetch the field representing the String value.
					Objectref stringObjectref = (Objectref) parameters[0];
					String stringValue = null;
					// Get the value to print if it is not null.
					if (stringObjectref != null) {
						Field stringValueField = stringObjectref.getInitializedClass()
								.getClassFile().getFieldByNameAndDescriptor("value", "[C");
						Arrayref characters = (Arrayref) stringObjectref.getField(stringValueField);

						// Build the String.
						stringValue = "";
						for (int a = 0; a < characters.length; a++) {
							stringValue += characters.getElement(a).toString();
						}
					}

					// Get the information what kind of PrintStream is wrapped here.
					String wrapperFor = "";
					if (objectref != null) {
						try {
							Field field = objectref.getInitializedClass().getClassFile()
									.getFieldByNameAndDescriptor("wrapperFor", "Ljava/lang/String;");
							wrapperFor = (String) objectref.getField(field);
						} catch (FieldResolutionError e) {
							// Ignore it.
						}
					}

					// Simulate the behavior of the the System.err.print(), System.err.println(), System.out.print() and System.out.println() methods.
					de.wwu.muggl.vm.execution.nativeWrapping.PrintStreamWrapper.writeToLogfileImplementation(stringValue, wrapperFor);

					// There is no return value.
					return new UndefinedValue();
				}
			} else {
				// There was no implementation found.
				throw new ForwardingUnsuccessfulException(
						"No wrapping handler for the native method was found, even though matching a wrapper was expexted. "
								+ "This hints to an implementation problenm.");
			}
		}

		// There was no implementation found.
		throw new ForwardingUnsuccessfulException("No wrapping handler for the native method was found.");
	}

	/**
	 * Save changes in reference parameters.
	 *
	 * If reference values were used as parameters, they might have experiences changes to their
	 * fields in case of reference values and to their elements in case of array references. Check
	 * if there are any references. If references are met, copy their fields or elements.
	 *
	 * @param originalParameters The original parameters.
	 * @param parameters The parameters after the native method invocation.
	 * @param conversion A MugglToJavaConversion instance to convert objects from SUN's java to
	 *        Muggl.
	 * @throws ArrayIndexOutOfBoundsException If the dimensions of the array and the array reference
	 *         are not equal.
	 * @throws ConversionException If conversion failed.
	 *
	 */
	private static void saveChangesInReferenceParameters(Object[] originalParameters, Object[] parameters, MugglToJavaConversion conversion) throws ConversionException {
		for (int a = 0; a < originalParameters.length; a++) {
			// Distinguish between object references and array references. Ignore plain objects, as they are wrappers for primitive types.
			if (originalParameters[a] instanceof Objectref) {
				// The fields of an object might have changed, so copy them.
				try {
					conversion.copyFieldFromObject(parameters[a], (Objectref) originalParameters[a], true);
				} catch (SecurityException e) {
					// Log it but beside that ignore it.
					if (Globals.getInst().execLogger.isTraceEnabled())
						Globals.getInst().execLogger.trace("Copying fields from an object return by a native "
								+ "method call failed with a SecurityException (" + e.getMessage() + ")");
				}
			} else if (originalParameters[a] instanceof Arrayref) {
				// The elements of an array might have changed, so copy them.
				conversion.insertArrayIntoArrayref((Arrayref) originalParameters[a], (Object[]) parameters[a]);
			}
		}
	}

}
