package de.wwu.muggl.vm.execution;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.impl.symbolic.support.JavaToSymbolicConversion;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.ModifieableArrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.PrimitiveWrappingImpossibleException;
import de.wwu.muggl.vm.initialization.StaticInitializationSupport;
import de.wwu.muggl.solvers.expressions.Constant;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * This class provides for the conversion of objects from the java virtual machine implementation of
 * SUN to the jvm of Muggl and the other way around. The two methods provided will automatically
 * process any types and can convert arrays as well.<br />
 * <br />
 * If the jvm is running in symbolic mode, there will be an automatic conversion of symbolical
 * types to java types on the fly. For this,
 * {@link de.wwu.muggl.vm.impl.symbolic.support.JavaToSymbolicConversion} is utilized.<br />
 * <br />
 * This class uses a caching mechanism for converted objects and object references. Whenever an
 * object or a reference object is created and it is neither an array, array reference, or a
 * primitive type wrapper object, it is cached. This is explicitly done before inserting any field
 * values into it. While this may be beneficial for the processing speed in case of a high field
 * count, it is mainly done as a pure necessity. If not caching result, infinite loops within this
 * class would be the result.<br />
 * <br />
 * To give an example: Object A has a field that references object B. Object B has a field that
 * references object A. Without caching, trying to convert object A would lead to a try to convert
 * object B while inserting the affected field of A. This would would lead to a try to convert
 * object A and it's field that references B, even tough A is converted half-way at that time. By
 * caching the converted object reference for A before the fields of A are inserted, at the time the
 * reference to A is inserted for B the cached result is used (which will be augmented with the
 * appropriate fields once the executing returns to the conversion of A). This of course works for
 * both converting from Muggl to java as from java to Muggl.<br />
 * <br />
 * <b>Please note:</b> It is a problem to convert data structures in such a low level and still be
 * able to offer an acceptable performance. Probably the native wrapping should be either avoided
 * completely by offering custom wrappers for any native call or there should be an own native
 * mechanism for that. The limiting factor is the accessibility of java objects, even with the means
 * of reflection. This boundary could be overcome if directly working with lowest level data
 * structures, basically giving the same access to java objects as Muggl has to its object
 * references. In the meantime, this class should offer great services, even though there could be a
 * slight loose of data while converting. In general, this should only affect instances of
 * java.lang.Class. There might be unforeseen circumstances, though, that this class does not
 * provide means of getting around, yet.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public class MugglToJavaConversion {
	// A reference of the virtual machine.
	private VirtualMachine	vm;

	// Fields to cache temporary results.
	private HashMap<Objectref, Object> mugglJavaMapping;
	private HashMap<Object, Objectref> javaMugglMapping;

	/**
	 * Construct with a reference of the virtual machine.
	 *
	 * @param vm A reference of the virtual machine.
	 */
	public MugglToJavaConversion(VirtualMachine vm) {
		this.vm = vm;
		this.mugglJavaMapping = new HashMap<Objectref, Object>();
		this.javaMugglMapping = new HashMap<Object, Objectref>();
	}

	/**
	 * Convert Muggl reference values to java objects. Muggl primitive type wrapper objects will
	 * remain untouched.
	 *
	 * @param object The object to convert.
	 * @return A java object.
	 * @throws ConversionException If conversion failed.
	 */
	public Object toJava(Object object) throws ConversionException {
		return toJava(object, null);
	}

	/**
	 * Convert Muggl reference values to java objects. Muggl primitive type wrapper objects will
	 * remain untouched.
	 *
	 * @param object The object to convert.
	 * @param type The type of the desired object. May be {@link java.lang.Byte#TYPE},
	 *        {@link java.lang.Character#TYPE}, {@link java.lang.Short#TYPE} or null. This
	 *        parameter only applies for the conversion from symbolic values by
	 *        {@link de.wwu.muggl.vm.impl.symbolic.support.JavaToSymbolicConversion#tryToJava(Constant, Class)}
	 * @return A java object.
	 * @throws ConversionException If conversion failed.
	 */
	public Object toJava(Object object, Class<?> type) throws ConversionException {
		// Null will stay null.
		if (object == null)
			return null;

		// Convert object and array references.
		if (object instanceof Objectref) {
			Objectref objectref = (Objectref) object;

			// Got a cached result?
			if (this.mugglJavaMapping.containsKey(objectref))
				return this.mugglJavaMapping.get(objectref);

			// Special handling of Strings.
			ClassFile classFile = objectref.getInitializedClass().getClassFile();
			if (classFile.getName().equals("java.lang.String")) {
				// Just copying the fields would screw the String initialization mechanism of java.
				Field field = classFile.getFieldByName("value");
				Arrayref chars = (Arrayref) objectref.getField(field);
				String newString = "";
				for (int a = 0; a < chars.length; a++) {
					if (Options.getInst().symbolicMode) {
						/*
						 * TODO: Probably need to add handling for variables. It could be tried to use the current
						 * solution.
						 */
						newString += (char) ((IntConstant) chars.getElement(a)).getIntValue();
					} else {
						newString += chars.getElement(a);
					}
				}

				// Cache the result.
				this.mugglJavaMapping.put(objectref, newString);

				// Return the String.
				return newString;
			}

			// Create a new object and insert the object's field values.
			return newJavaObject(objectref);
		} else if (object instanceof Arrayref) {
			Arrayref arrayref = (Arrayref) object;
			ClassFile classFile = null;

			/*
			 * Distinguish between "normal" array references and the "special", modifiable ones used
			 * for the symbolic execution.
			 */
			if (object instanceof ModifieableArrayref) {
				ModifieableArrayref mArrayref = (ModifieableArrayref) object;
				String representedType = mArrayref.getRepresentedType();
				if (representedType != null) {
					try {
						classFile = this.vm.getClassLoader().getClassAsClassFile(representedType);
					} catch (ClassFileException e) {
						// Simply ignore it.
					}
				}
			}

			// If no class file is set at this point, use the reference value of the array reference.
			if (classFile == null)
				classFile = arrayref.getReferenceValue().getInitializedClass().getClassFile();

			// Create a new array.
			int[] dimensions = arrayref.getDimensions();
			Object[] array = (Object[]) Array.newInstance(classFile.getInstanceOfClass(), dimensions);

			// Insert the elements.
			insertArrayrefIntoArray(array, arrayref);

			// Return the array.
			return array;
		} else {
			/*
			 * Anything that is neither an object nor an array reference is a primitive type
			 * wrapper. In general, they do not need to be converted. However, if running
			 * the symbolic mode, they are symbolic types and therefore need a conversion.
			 */
			if (Options.getInst().symbolicMode) {
				/*
				 * TODO: Add handling for variables. It could be tried to use the current solution.
				 */
				if (object instanceof Constant) {
					// Got an interesting type?
					if (type == Byte.class) {
						type = Byte.TYPE;
					} else if (type == Character.class) {
						type = Character.TYPE;
					} else if (type == Short.class) {
						type = Short.TYPE;
					}

					// Convert it.
					object = JavaToSymbolicConversion.tryToJava((Constant) object, type);
				} else {
					throw new ConversionException("Cannot convert symbolic types that are no constants.");
				}
			}
		}

		return object;
	}

	public Object arrayRefToJavaObject(Object object, Class<?> type) throws ConversionException {
		return new Object[0];
	}
	
	
	/**
	 * Convert java objects to Muggl objects. This method will process any kind of java objects
	 * and convert them to Muggl reference values or primitive type wrappers.<br />
	 * <br />
	 * @param object The object to convert.
	 * @param isPrimitive Information whether the object is used as a primitive type wrapper, or
	 *        not.
	 * @return A Muggl reference value or primitive type wrapper
	 * @throws ConversionException If conversion failed.
	 */
	public Object toMuggl(Object object, boolean isPrimitive) throws ConversionException {
		// Null will stay null.
		if (object == null)
			return null;

		try {
			// Process array types.
			if (object.getClass().isArray()) {
				// Get the Reference value.
				ClassFile classFile = this.vm.getClassLoader().getClassAsClassFile(
						object.getClass());
				Objectref objectref;

				// Distinguish between primitive and reference arrays.
				if (isPrimitive) {
					objectref = classFile.getAPrimitiveWrapperObjectref(this.vm);
				} else {
					objectref = this.vm.getAnObjectref(classFile);
				}

				// Get the dimension count.
				int[] dimensionCount = getDimensionCount(object);

				// Initialize the array reference.
				Arrayref arrayref = new Arrayref(objectref, dimensionCount);

				// Insert the elements.
				insertArrayIntoArrayref(arrayref, object);

				// Make the array reference the object returned.
				object = arrayref;
			} else {
				// Distinguish between primitive and reference types.
				if (isPrimitive) {
					// If the expected Muggl type is symbolic., conversion is needed.
					if (Options.getInst().symbolicMode) {
						object = JavaToSymbolicConversion.tryToSymbolic(object);
					} else {
						/*
						 * Convert Boolean, Byte and Short to Integer. Other primitive types can be
						 * used as they are.
						 */
						if (object instanceof Boolean) {
							object = Integer.valueOf(((Boolean) object).booleanValue() ? 1 : 0);
						} else if (object instanceof Byte) {
							object = Integer.valueOf((Byte) object);
						} else if (object instanceof Short) {
							object = Integer.valueOf((Short) object);
						}
					}
				} else {
					// Got a cached result?
					if (this.javaMugglMapping.containsKey(object))
						return this.javaMugglMapping.get(object);

					// Process a reference object.
//					if(object.getClass().getComponentType() != null) {
//						return object;
//					}
					object = toObjectref(object);
				}
			}

			return object;
		} catch (ClassFileException e) {
			throw new ConversionException(
					"Conversion failed with a ClassFileException. Root cause: " + e.getMessage());
		} catch (ExceptionInInitializerError e) {
			throw new ConversionException(
					"Conversion failed with a ExceptionInInitializerError. Root cause: "
							+ e.getMessage());
		} catch (PrimitiveWrappingImpossibleException e) {
			throw new ConversionException(
					"Conversion failed with a PrimitiveWrappingImpossibleException. Root cause: "
							+ e.getMessage());
		}
	}

	/**
	 * Insert an array into an Arrayref of the same dimensional structure. Elements will be
	 * converted from java to Muggl on insertion.<br />
	 * <br />
	 * This method does NOT check the structure first, both arrays should hence have the same number
	 * of dimensions and length of each dimension.
	 *
	 * @param arrayref The Arrayref to insert the values into.
	 * @param array The array to insert the values from.
	 * @throws ArrayIndexOutOfBoundsException If the dimensions of the array and the array reference
	 *         are not equal.
	 * @throws ConversionException If conversion failed.
	 */
	public void insertArrayIntoArrayref(Arrayref arrayref, Object array) throws ConversionException {
		boolean treatAsPrimitive = false;

		// Copy the elements.
		for (int a = 0; a < Array.getLength(array); a++) {
			// Check if the elements are arrays, too.
			if (Array.getLength(array) > 0 && Array.get(array, 0) != null
					&& Array.get(array, 0).getClass().isArray()) {
				// Recursively invoke this method.
				insertArrayIntoArrayref((Arrayref) arrayref.getElement(a), Array.get(array, a));
			} else {
				/*
				 * On the first run, find out if the elements have to be treated as if they were
				 * primitive.
				 */
				if (a == 0) {
					treatAsPrimitive = arrayref.isPrimitive();
					if (!treatAsPrimitive && arrayref instanceof ModifieableArrayref)
						treatAsPrimitive = ((ModifieableArrayref) arrayref)
								.isRepresentedTypeIsAPrimitiveWrapper();

				}

				// Insert the value after converting it to Muggl.
				arrayref.putElement(a, toMuggl(Array.get(array, a), treatAsPrimitive));
			}
		}
	}

	/**
	 * Insert an array reference into an array of the same dimensional structure. Elements will be
	 * converted from Muggl to java on insertion.<br />
	 * <br />
	 * This method does NOT check the structure first, both arrays should hence have the same number
	 * of dimensions and length of each dimension.
	 *
	 * @param array The array to insert the values to.
	 * @param arrayref The Arrayref to insert the values from.
	 * @throws ArrayIndexOutOfBoundsException If the dimensions of the array and the array reference
	 *         are not equal.
	 * @throws ConversionException If conversion failed.
	 */
	private void insertArrayrefIntoArray(Object array, Arrayref arrayref)
			throws ConversionException {
		for (int a = 0; a < arrayref.length; a++) {
			// Check if the elements are arrays, too.
			if (arrayref.length > 0 && arrayref.getElement(0) != null
					&& arrayref.getReferenceValue().isArray()) {
				// Recursively invoke this method.
				insertArrayrefIntoArray(Array.get(array, a), (Arrayref) arrayref.getElement(a));
			} else {
				// Insert the value after converting it to java.
				if (Options.getInst().symbolicMode) {
					Array.set(array, a, toJava(arrayref.getElement(a), array.getClass().getComponentType()));
				} else {
					Array.set(array, a, toJava(arrayref.getElement(a)));
				}
			}
		}
	}

	/**
	 * Get the dimension count for an java array object.
	 *
	 * @param array The array object.
	 * @return The dimension count as an array of int.
	 */
	private int[] getDimensionCount(Object array) {
		// Browse through the array to determine its dimensions.
		int[] dimensionCount = new int[0];
		while (true) {
			int[] dimensionCountNew = new int[dimensionCount.length + 1];
			for (int a = 0; a < dimensionCount.length; a++) {
				dimensionCountNew[a] = dimensionCount[a];
			}
			dimensionCountNew[dimensionCount.length] = Array.getLength(array);
			dimensionCount = dimensionCountNew;
			if (Array.getLength(array) > 0 && Array.get(array, 0) != null
					&& Array.get(array, 0).getClass().isArray()) {
				array = Array.get(array, 0);
			} else {
				break;
			}
		}
		return dimensionCount;
	}

	/**
	 * This methods takes reference values from the java "world", reads their fields, creates an
	 * object reference and writes the fields into it. By doing so, java objects like the ones
	 * supplied by native method invocation can be used in this application.
	 * 
	 * @param object The object to be converted to a reference value.
	 * @return An GlasTT object reference.
	 * @throws ClassFileException On fatal errors loading or parsing a class file.
	 * @throws ConversionException If conversion failed.
	 * @throws ExceptionInInitializerError
	 *         ExceptionInInitializerError - If class initialization fails.
	 * @throws SecurityException If access is not possible. This exception is originally thrown by
	 *         the used java reflection API.
	 */
	private Objectref toObjectref(Object object) throws ConversionException, ClassFileException {
		// Create the new object reference.
		Objectref objectref = this.vm.getAnObjectref(this.vm.getClassLoader().getClassAsClassFile(
						object.getClass().getName()));

		// Cache the result. This has to be done before inserting the fields to avoid infinite loops.
		this.javaMugglMapping.put(object, objectref);

		// Insert the fields.
		copyFieldFromObject(object, objectref, false);

		// Return the new object reference.
		return objectref;
	}

	/**
	 * Create a new java object and insert the values of the fields of the specified object
	 * referenced into it.
	 *
	 * @param objectref The object reference to copy the fields from.
	 * @return The object.
	 * @throws ConversionException If conversion failed.
	 */
	public Object newJavaObject(Objectref objectref) throws ConversionException {
		Object object = null;
		Throwable lastThrowableCaught = null;
		String className = objectref.getName();

		/*
		 * Check if the object reference is a reference of java.lang.class. It will need special
		 * care, since reflective invocation of java.lang.Class is forbidden. However, we can
		 * extract the name of the class from the object reference and then use the java construct
		 * java.lang.Class#forName(String) to get a Class instance.
		 */
		if (className.equals("java.lang.Class")) {
			// Determine the class this instance belongs to. Get the name field...
			Field nameField = objectref.getInitializedClass().getClassFile().getFieldByName("name");
			Objectref nameObjectref = (Objectref) objectref.getField(nameField);
			// Get the value from the String object reference...
			Field valueField = nameObjectref.getInitializedClass().getClassFile().getFieldByName("value");
			Arrayref chars = (Arrayref) nameObjectref.getField(valueField);
			// Build a java String from it.
			String name = "";
			for (int a = 0; a < chars.length; a++) {
				if (Options.getInst().symbolicMode) {
					/*
					 * TODO: Probably need to add handling for variables. It could be tried to use the current
					 * solution.
					 */
					name += (char) ((IntConstant) chars.getElement(a)).getIntValue();
				} else {
					name += chars.getElement(a);
				}
			}

			// Get the appropriate Class instance for that String.
			try {
				object = Class.forName(name);
			} catch (ClassNotFoundException e) {
				throw new ConversionException(
						"Getting an instance of java.lang.Class for " + name + " failed.");
			}

			// Cache the result.
			this.mugglJavaMapping.put(objectref, object);

			// Return the instance of Class.
			return object;
		}

		// First try: Use the nullary constructor.
		try {
			object = Class.forName(className).newInstance();
		} catch (ClassNotFoundException e) {
			// Just ignore it.
			lastThrowableCaught = e;
		} catch (ExceptionInInitializerError e) {
			// Just ignore it.
			lastThrowableCaught = e;
		} catch (IllegalAccessException e) {
			// Just ignore it.
			lastThrowableCaught = e;
		} catch (InstantiationException e) {
			// Just ignore it.
			lastThrowableCaught = e;
		} catch (SecurityException e) {
			// Just ignore it.
			lastThrowableCaught = e;
		}

		// No success, yet?
		if (object == null) {
			Class<?> objectClass = objectref.getInitializedClass().getClassFile()
					.getInstanceOfClass();

			// Second try: Use a constructor.
			java.lang.reflect.Constructor<?>[] constructors = objectClass.getDeclaredConstructors();

			// Is there a constructor declared?
			if (constructors.length > 0) {

				/**
				 * Inner class to queue constructors.
				 */
				final class QueuedConstructor implements Comparable<QueuedConstructor> {
					private java.lang.reflect.Constructor<?> constructor;

					/**
					 * Construct the queued constructor.
					 *
					 * @param constructor The constructor that is queued.
					 */
					public QueuedConstructor(java.lang.reflect.Constructor<?> constructor) {
						this.constructor = constructor;
					}

					/**
					 * Getter for the constructor.
					 *
					 * @return The constructor.
					 */
					public java.lang.reflect.Constructor<?> getConstructor() {
						return this.constructor;
					}

					/**
					 * Compare an instance of QueuedConstructor to this QueuedConstructor.
					 *
					 * @param c The instance of QueuedConstructor to compare.
					 * @return A negative integer, zero, or a positive integer as this constructor
					 *         has less, an equal number or more parameters than the specified
					 *         constructor.
					 */
					public int compareTo(QueuedConstructor c) {
						int numberOfParameters1 = this.constructor.getParameterTypes().length;
						int numberOfParameters2 = c.constructor.getParameterTypes().length;

						// Check if the parameter count of one of the constructors is greater.
						if (numberOfParameters1 < numberOfParameters2) return -1;
						if (numberOfParameters1 > numberOfParameters2) return 1;

						/*
						 * Equal parameter count. Does one constructor have more primitive type
						 * parameters?
						 */
						numberOfParameters1 = 0;
						numberOfParameters2 = 0;
						Class<?>[] constructorParameterTypes1 = this.constructor.getParameterTypes();
						Class<?>[] constructorParameterTypes2 = c.constructor.getParameterTypes();
						// Count the primitive types parameters...
						for (int a = 0; a < constructorParameterTypes1.length; a++) {
							if (constructorParameterTypes1[a].isPrimitive()) numberOfParameters1++;
							if (constructorParameterTypes2[a].isPrimitive()) numberOfParameters2++;
						}

						/*
						 * Check if the primitive parameter count of one of the constructors is
						 * greater. There is an asymmetry of this comparison to the above one: This
						 * time, the greater number is the attribute that determines the ordering to
						 * the top.
						 *
						 * The queue should return those constructors first that have the least
						 * parameters, but prefer those with primitive parameters over those with
						 * reference parameters if the number of parameters is equal. This explains
						 * the behavior implemented in the following two lines.
						 */
						if (numberOfParameters2 < numberOfParameters1) return -1;
						if (numberOfParameters2 > numberOfParameters1) return 1;

						// It is undecidable.
						return 0;
					}

					/**
					 * Indicates whether some other object is equal to this one.
					 *
					 * @param obj The object to compare the wrapped constructor to.
					 * @return true, if the supplied object is of type QueuedConstructor and the
					 *         wrapped Constructor is equal; false otherwise.
					 * @see java.lang.Object#equals(java.lang.Object)
					 */
					@Override
					public boolean equals(Object obj) {
						if (obj instanceof QueuedConstructor) {
							QueuedConstructor queuedConstructor = (QueuedConstructor) obj;
							if (queuedConstructor.constructor.equals(this.constructor))
								return true;
						}
						return false;
					}
					
					/**
					 * Returns a hash code value for the object.
					 * 
				     * @return  a hash code value for this object.
					 * @see java.lang.Object#hashCode()
					 */
					@Override
					public int hashCode() {
						return this.constructor.hashCode();
					}
					
				}

				// Insert all constructors into a priority queue.
				PriorityQueue<QueuedConstructor> queue = new PriorityQueue<QueuedConstructor>();
				for (int a = 0; a < constructors.length; a++) {
					queue.add(new QueuedConstructor(constructors[a]));
				}

				/*
				 * Now try each constructor until either an instance of the desired object could be
				 * created or there are no more constructors left.
				 */
				while (object == null && queue.size() > 0) {
					java.lang.reflect.Constructor<?> constructor = queue.poll().getConstructor();

					// Set up the parameters.
					Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
					Object[] constructorParameters = new Object[constructorParameterTypes.length];
					for (int b = 0; b < constructorParameterTypes.length; b++) {
						// Is it a primitive type parameter?
						if (constructorParameterTypes[b].isPrimitive()) {
							constructorParameters[b] = StaticInitializationSupport
									.getInitializedPrimitiveTypeWrapper(
											constructorParameterTypes[b].getName(), false, false);
						} else {
							// Initialize with the empty constructor, if possible.
							try {
								constructorParameters[b] = constructorParameterTypes[b].newInstance();
							} catch (IllegalAccessException e) {
								/*
								 * Most likely, the empty constructor is not accessible. The parameter
								 * will be take the value of null.
								 */
								constructorParameters[b] = null;
							} catch (InstantiationException e) {
								/*
								 * Something failed with the instantiation. The parameter will be take
								 * the value of null.
								 */
								constructorParameters[b] = null;
							} catch (ExceptionInInitializerError e) {
								// Initialization failed. The parameter will be take the value of null.
								constructorParameters[b] = null;
							} catch (SecurityException e) {
								// Access was denied. The parameter will be take the value of null.
								constructorParameters[b] = null;
							}
						}
					}

					// Force the accessibility - ignoring private access etc.
					constructor.setAccessible(true);

					// Invoke the constructor.
					try {
						object = constructor.newInstance(constructorParameters);
					} catch (IllegalArgumentException e) {
						// Just ignore it.
						lastThrowableCaught = e;
					} catch (InvocationTargetException e) {
						// Just ignore it.
						lastThrowableCaught = e;
					} catch (IllegalAccessException e) {
						// Just ignore it.
						lastThrowableCaught = e;
					} catch (InstantiationException e) {
						// Just ignore it.
						lastThrowableCaught = e;
					}
				}

			}

			// Still no success?
			if (object == null) {
				// Third try: Use a static method that supplies an instance of the object.
				java.lang.reflect.Method[] methods = objectClass.getDeclaredMethods();
				for (int a = 0; a < methods.length; a++) {
					// If the class is static, check the return type.
					if (java.lang.reflect.Modifier.isStatic(methods[a].getModifiers())
							&& objectClass == methods[a].getReturnType()) {
						// Invoke it reflectively.
						try {
							object = methods[a].invoke(null);
						} catch (IllegalAccessException e) {
							// Just ignore it.
							lastThrowableCaught = e;
						} catch (ExceptionInInitializerError e) {
							// Just ignore it.
							lastThrowableCaught = e;
						} catch (IllegalArgumentException e) {
							// Just ignore it.
							lastThrowableCaught = e;
						} catch (InvocationTargetException e) {
							// Just ignore it.
							lastThrowableCaught = e;
						} catch (NullPointerException e) {
							// Just ignore it.
							lastThrowableCaught = e;
						}

						// Success?
						if (object != null) break;
					}
				}
			}

		}

		// Finally no success?
		if (object == null) {
			if (lastThrowableCaught == null) {
				throw new ConversionException(
						"Creating a new java class failed. No suitable possibiliy was found to instantiate it.");
			}
			throw new ConversionException(
					"Creating a new java class failed. The last exception caught while try to was: "
							+ lastThrowableCaught.getClass().getName() + " ("
							+ lastThrowableCaught.getMessage() + ")");
		}

		// Cache the result. This has to be done before inserting the fields to avoid infinite loops.
		this.mugglJavaMapping.put(objectref, object);

		try {
			// Insert the fields.
			insertFieldValues(objectref, object);

			// Return the object.
			return object;
		} catch (Exception e) {
			// Inserting the fields failed.
			throw new ConversionException(
					"Creating a new java class failed sine the fields could not be inserted. The root cause is: "
							+ e.getClass().getName() + " (" + e.getMessage() + ")");
		}
	}

	/**
	 * Insert the values of the fields of the specified reference value into the supplied object.
	 *
	 * @param objectref The object reference to copy the fields from.
	 * @param object The object to insert the values to.
	 * @throws ConversionException If conversion failed.
	 * @throws NoSuchFieldException If a field cannot be found. This will happen if the object is no
	 *         instance of the class represented by this InitializedClass.
	 * @throws SecurityException If access is not possible. This exception is originally thrown by
	 *         the used java reflection API.
	 */
	private void insertFieldValues(Objectref objectref, Object object) throws ConversionException,
			NoSuchFieldException {
		// Static fields.
		Enumeration<Field> fieldsEnumeration1 = objectref.getInitializedClass().getStaticFields()
				.keys();
		// Instance fields.
		Enumeration<Field> fieldsEnumeration2 = objectref.getFields().keys();

		// Process both static and instance fields.
		for (int a = 0; a < 2; a++) {
			Enumeration<Field> fieldsEnumeration;
			if (a == 0) {
				fieldsEnumeration = fieldsEnumeration1;
			} else {
				fieldsEnumeration = fieldsEnumeration2;
			}

			// Insert the fields iteratively.
			while (fieldsEnumeration.hasMoreElements()) {
				Field field = fieldsEnumeration.nextElement();
				Class<?> objectClass = object.getClass();
				boolean insertedSuccessfully = false;
				while (!insertedSuccessfully && objectClass != null) {
					try {
						java.lang.reflect.Field objectField = objectClass.getDeclaredField(field
								.getName());
						try {
							// Get the object to be inserted.
							Object toInsert = objectref.getFields().get(field);

							// Convert it.
							if (Options.getInst().symbolicMode) {
								toInsert = toJava(toInsert, objectField.getType());
							} else {
								toInsert = toJava(toInsert);
							}

							// Ensure accessibility.
							objectField.setAccessible(true);
							// Use reflection to insert the value.
							objectField.set(object, toInsert);
						} catch (IllegalAccessException e) {
							// Log it but beside that ignore it.
							if (Globals.getInst().execLogger.isTraceEnabled())
								Globals.getInst().execLogger
										.trace("Copying fields to an object "
												+ "failed with a IllegalAccessException ("
												+ e.getMessage() + ")");
						} catch (IllegalArgumentException e) {
							// Log it but beside that ignore it.
							if (Globals.getInst().execLogger.isTraceEnabled())
								Globals.getInst().execLogger
										.trace("Copying fields from an object "
												+ "failed with a IllegalArgumentException ("
												+ e.getMessage() + ")");
						}
						insertedSuccessfully = true;
					} catch (NoSuchFieldException e) {
						// Try the declaring class.
						objectClass = objectClass.getSuperclass();
					}
				}

				// Not found the field?
				if (!insertedSuccessfully) throw new NoSuchFieldException(field.getName());
			}
		}
	}

	/**
	 * Copy the values of the fields of the supplied object and insert them into the reference
	 * value.<br />
	 * <br />
	 * While an Objectref holds any fields of an object and they can be accessed directly, it is
	 * required to get the class of an java object in order to reflectively get its fields. A class
	 * of course only declares its own fields but not inherited ones. Hence, fields have to be
	 * searched in the class of the supplied object and in any super class up to java.lang.Object.
	 *
	 * @param object The object to get the field values from.
	 * @param objectref The object reference to copy the fields from.
	 * @param ignoreFinalFields If set to true, the values of finals fields will not be copied.
	 * @throws ArrayIndexOutOfBoundsException If the dimensions of the array and the array reference
	 *         are not equal.
	 * @throws ConversionException If conversion failed.
	 * @throws SecurityException If access is not possible. This exception is originally thrown by
	 *         the used java reflection API.
	 */
	public void copyFieldFromObject(Object object, Objectref objectref, boolean ignoreFinalFields)
			throws ConversionException {
		/*
		 * Check if the object is a reference of java.lang.class. Class instances are nasty. Ignore
		 * possible changed - they will most likely not be needed anyway.
		 */
		if (object.getClass().getName().equals("java.lang.Class"))
			return;

		// Work through all declared fields and non-private fields of super-classes.
		for (java.lang.reflect.Field javaField : getFieldsForObject(object)) {
			// Ensure accessibility.
			javaField.setAccessible(true);

			// Ignore final fields?
			int modifiers = javaField.getModifiers();
			if (!ignoreFinalFields || !java.lang.reflect.Modifier.isFinal(modifiers)) {
				// Distinguish between static and instance fields.
				if (java.lang.reflect.Modifier.isStatic(modifiers)) {
					Field field = objectref.getInitializedClass().getClassFile()
							.getFieldByName(javaField.getName(), true);
					try {
						// Convert and insert.
						Object objectToInsert = javaField.get(object);
						objectToInsert = toMuggl(objectToInsert, field.isPrimitiveType());
						objectref.getInitializedClass().putField(field, objectToInsert);
					} catch (IllegalAccessException e) {
						// Log it but beside that ignore it.
						if (Globals.getInst().execLogger.isTraceEnabled())
							Globals.getInst().execLogger.trace("Copying fields from an object "
									+ "failed with a IllegalAccessException (" + e.getMessage() + ")");
					} catch (IllegalArgumentException e) {
						// Log it but beside that ignore it.
						if (Globals.getInst().execLogger.isTraceEnabled())
							Globals.getInst().execLogger.trace("Copying fields from an object "
									+ "failed with a IllegalArgumentException (" + e.getMessage() + ")");
					}
				} else {
					Field field = objectref.getInitializedClass().getClassFile()
							.getFieldByName(javaField.getName(), true);
					try {
						// Convert and insert.
						Object objectToInsert = javaField.get(object);
						objectToInsert = toMuggl(objectToInsert, field.isPrimitiveType());
						objectref.putField(field, objectToInsert);
					} catch (IllegalAccessException e) {
						// Log it but beside that ignore it.
						if (Globals.getInst().execLogger.isTraceEnabled())
							Globals.getInst().execLogger.trace("Copying fields from an object "
									+ "failed with a IllegalAccessException (" + e.getMessage() + ")");
					} catch (IllegalArgumentException e) {
						// Log it but beside that ignore it.
						if (Globals.getInst().execLogger.isTraceEnabled())
							Globals.getInst().execLogger.trace("Copying fields from an object "
									+ "failed with a IllegalArgumentException (" + e.getMessage() + ")");
					}
				}
			}
		}
	}

	/**
	 * Get the fields a java object may access. The object can access any fields it declares by
	 * itself or that it inherits.
	 *
	 * @param object The java object to get the fields for.
	 * @return A Set containing any fields the object may access.
	 */
	private Set<java.lang.reflect.Field> getFieldsForObject(Object object) {
		Set<java.lang.reflect.Field> allFields = new HashSet<java.lang.reflect.Field>();

		// Process the object's class and any of its super classes.
		boolean firstPass = true;
		Class<?> objectClass = object.getClass();
		while (objectClass != null) {
			// Get the fields.
			java.lang.reflect.Field[] fields = objectClass.getDeclaredFields();

			// Add the fields.
			for (java.lang.reflect.Field field : fields) {
				/*
				 * Add the fields to the list of found fields if this is either the first pass or if
				 * the field found is not private. By using a HashSet, duplicate entries (an
				 * overridden field will be found both in the class as in one of its super classes)
				 * will simply not be added.
				 */
				if (firstPass || !java.lang.reflect.Modifier.isPrivate(field.getModifiers()))
					allFields.add(field);
			}

			// Get the super class.
			objectClass = objectClass.getSuperclass();
			firstPass = false;
		}

		// Finished.
		return allFields;
	}

}
