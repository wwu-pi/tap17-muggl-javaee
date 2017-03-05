package de.wwu.muggl.vm.execution;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.solvers.expressions.StringVariable;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * This class provided basic algorithms specified by the java virtual machine specification. They
 * are needed for the proper execution of a couple of instructions. For more clearness, they are
 * relocated in this class. The  methods represent the algorithm to check for assignment compatibility.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class ExecutionAlgorithms {
	private MugglClassLoader classLoader;

	/**
	 * Basic constructor to set the class loader.
	 * @param classLoader The class loader.
	 */
	public ExecutionAlgorithms(MugglClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Check for assignment compatibility. The sourceObject is an object. It has to be one of the
	 * primitive type wrapper classes provided in java.lang, which are used to represent primitive
	 * values in this application. The target is just a String representation of what type the
	 * source is expected to be assignment compatible to.
	 *
	 * The sourceObject will be wrapped into a ReferenceValue. However, this will only be done for
	 * the check for assignment compatibility. No fields are read and put into the ReferenceValue.
	 * If sourceObject is an array, the length of its dimensions is simply ignored.
	 *
	 * The target will be looked up and a reference value of it will be generated so the assignment
	 * compatibility algorithm can be used.
	 *
	 * Should the sourcrObject happen to be a ReferenceValue already, the wrapping will be skipped
	 * and just the target will be prepared.
	 *
	 * The check for assignment compatibility is done by invoking
	 * checkForAssignmentCompatibility(ReferenceValue source, ReferenceValue target).
	 *
	 * @param sourceObject The source object (probably a ReferenceValue already).
	 * @param targetString The target type in a String representation.
	 * @param vm The currently running virtual machine.
	 * @param sourceObjectIsStringRepresentation Indicates if the sourceObject is a String with the
	 *        fully qualified name of the source class.
	 * @return true, if the source is assignment compatible to the target, false otherwise.
	 * @throws ExecutionException Thrown on fatal errors on resolving or initializing the target
	 *         class.
	 */
	public boolean checkForAssignmentCompatibility(Object sourceObject, String targetString,
			VirtualMachine vm, boolean sourceObjectIsStringRepresentation)
			throws ExecutionException {
		// Variables.
		ReferenceValue source;
		ReferenceValue target;

		if (sourceObject == null) {
			// If the source is null, keep this information.
			source = null;
		} else {
			// Prepare the source - if it is not a referenceValue already.
			if (sourceObject instanceof ReferenceValue) {
				source = (ReferenceValue) sourceObject;
			} else {
				try {
					String name;
					if (sourceObjectIsStringRepresentation) {
						name = sourceObject.toString();
					} else {
						name = sourceObject.getClass().getName();
					}
					int arrayDimensions = 0;

					// Check if the target type is an array.
					while (name.startsWith("[")) {
						arrayDimensions++;
						name = name.substring(1);
					}
					// If there were any "[", drop the "L" as well.
					if (arrayDimensions > 0) {
						name = name.substring(1);
						// Probably there is a ";" at the end. Drop it.
						if (name.endsWith(";")) name = name.substring(0, name.length() - 1);
					}

					// Can the type be wrapped up?
					if (!(name.equals("java.lang.Boolean") || name.equals("java.lang.Byte")
							|| name.equals("java.lang.Character") || name.equals("java.lang.Short")
							|| name.equals("java.lang.Integer") || name.equals("java.lang.Double")
							|| name.equals("java.lang.Float") || name.equals("java.lang.Long")
							|| name.equals("java.lang.String")))
						throw new ExecutionException("The check for assigment compatibility failed unexpected as only the wrapper classes of java.lang might be wrapped into ReferenceValues for the assignment compatibility check.");

					// Get the Objectref
					source = this.classLoader.getClassAsClassFile(name).getAPrimitiveWrapperObjectref(vm);

					// Source is an arrayref?
					if (arrayDimensions > 0) {
						// Generate the dimensions. The length is always 0, since it is unimportant here.
						for (int a = 0; a < arrayDimensions; a++) {
							source = new Arrayref(source, 0);
						}
					}
				} catch (ClassFileException e) {
					throw new ExecutionException("The check for assigment compatibility failed unexpected since a class could not be loaded due to a ClasNotFoundException with the message " + e.getMessage() + ".");
				} catch (ExceptionInInitializerError e) {
					e.printStackTrace();
					throw new ExecutionException("The check for assigment compatibility failed unexpected with an ExceptionInInitializerError with the message " + e.getMessage() + ".");
				}
			}
		}

		// Prepare the target.
		try {
			int arrayDimensions = 0;
			boolean targetIsPrimitiveType = false;

			// Check if the target type is an array.
			while (targetString.contains("[]")) {
				arrayDimensions++;
				targetString = targetString.substring(0, targetString.length() - 2);
			}

			// Check if target is a primitive type.
			if (targetString.equals("boolean"))  {
				if (arrayDimensions == 0) {
					// Boolean values are stored as Integers.
					targetString = "java.lang.Integer";
				} else {
					targetString = "java.lang.Boolean";
				}
				targetIsPrimitiveType = true;
			} else if (targetString.equals("byte")) {
				if (arrayDimensions == 0) {
					// Byte values are stored as Integers.
					targetString = "java.lang.Integer";
				} else {
					targetString = "java.lang.Byte";
				}
				targetIsPrimitiveType = true;
			} else if (targetString.equals("char")) {
				if (arrayDimensions == 0) {
					targetString = "java.lang.Integer";
				} else {
					targetString = "java.lang.Character";
				}
				targetIsPrimitiveType = true;
			} else if (targetString.equals("short")) {
				if (arrayDimensions == 0) {
					// Short values are stored as Integers.
					targetString = "java.lang.Integer";
				} else {
					targetString = "java.lang.Short";
				}
				targetIsPrimitiveType = true;
			} else if (targetString.equals("int")) {
				targetString = "java.lang.Integer";
				targetIsPrimitiveType = true;
			} else if (targetString.equals("double")) {
				targetString = "java.lang.Double";
				targetIsPrimitiveType = true;
			} else if (targetString.equals("float")) {
				targetString = "java.lang.Float";
				targetIsPrimitiveType = true;
			} else if (targetString.equals("long")) {
				targetString = "java.lang.Long";
				targetIsPrimitiveType = true;
			}

			// Get the objectref for the target.
			if (targetIsPrimitiveType) {
				target =  this.classLoader.getClassAsClassFile(targetString).getAPrimitiveWrapperObjectref(vm);
			} else {
				/*
				 * If source is null, it is only then not assignment compatible, if the target type
				 * is an primitive type. Check it as this point, as there is no need to the the
				 * target's class file in that case.
				 */
				if (source == null) return true;
				target = vm.getAnObjectref(this.classLoader.getClassAsClassFile(targetString));
			}

			// Target is an arrayref?
			if (arrayDimensions > 0) {
				// Generate the dimensions. The length is always 0, since it is unknown.
				for (int a = 0; a < arrayDimensions; a++) {
					target = new Arrayref(target, 0);
				}
			}
		} catch (ClassFileException e) {
			throw new ExecutionException("The check for assigment compatibility failed unexpected since a class could not be loaded due to a ClasNotFoundException with the message " + e.getMessage() + ".");
		}

		// Invoke checkForAssignmentCompatibilityReferenceValue source, ReferenceValue target).
		return checkForAssignmentCompatibility(source, target);
	}

	/**
	 * Check for assignment compatibility. See
	 * http://java.sun.com/docs/books/jvms/second_edition/html/Concepts.doc.html#19674
	 * for details.
	 * @param source The source reference value.
	 * @param target The target reference value.
	 * @return true, if the source is assignment compatible to the target, false otherwise.
	 */
	public boolean checkForAssignmentCompatibility(ReferenceValue source, ReferenceValue target) {
		try {
			// If source is null, it is only then not assignment compatible, if the target type is an primitive type.
			if (source == null) {
				if (target.isPrimitive()) return false;
				return true;
			}

			// Needed variables.
			ClassFile sourceClassFile = source.getInitializedClass().getClassFile();
			ClassFile targetClassFile = target.getInitializedClass().getClassFile();

			// The algorithm.
			if (source.isArray()) { // If S is an array type SC[], that is, a array of components of type SC:
				if (target.isArray()) { // If T is an array type TC[], that is, an array of components of type TC, then either
					if (source.isPrimitive() && target.isPrimitive() && sourceClassFile.getName().equals(targetClassFile.getName())) { // TC and SC must be the same primitive type, or
						return true;
					}
					// TC and SC are both reference types and type SC is assignable to TC.
					return checkForAssignmentCompatibility(((Arrayref) source).getReferenceValue(), ((Arrayref) target).getReferenceValue());
				} else if (targetClassFile.isAccInterface()) { // If T is an interface type, then T must be either Cloneable or java.io.Serializable.
					if (targetClassFile.getName().equals("java.lang.Cloneable") || targetClassFile.getName().equals("java.io.Serializable")) return true;
					return false;
				}
				// If T is a class type, then T must be Object.
				if (targetClassFile.getName().equals("java.lang.Object")) return true;
				return false;
			} else if (sourceClassFile.isAccInterface()) { // If S is an interface type:
				if (!targetClassFile.isAccInterface()) { // If T is a class type, then T must be Object.
					if (targetClassFile.getName().equals("java.lang.Object")) return true;
					return false;
				}
				return isClassOrSubclassOf(sourceClassFile, targetClassFile); // If T is an interface type, then T must be the same interface as S, or T must be a superinterface of S.
			} else { // If S is a class type:
				if (targetClassFile.isAccInterface()) { // If T is an interface type, then S must implement interface T.
					return isInterfaceOf(sourceClassFile, targetClassFile);
				}
				//If T is a class type, then S must be the same class as T, or S must be a subclass of T.
				return isClassOrSubclassOf(sourceClassFile, targetClassFile);
			}
		} catch (ClassFileException e) {
			if (Globals.getInst().execLogger.isDebugEnabled()) Globals.getInst().execLogger.debug("The check for assigment compatibility failed unexpected since a class could not be loaded due to a ClasNotFoundException with the message " + e.getMessage() + ".");
			return false;
		}
	}

	/**
	 * Check if source is equal to target or a subclass of it.
	 * @param source The source objects' ClassFile.
	 * @param target The target objects' ClassFile.
	 * @return true, if source is equal to or a subclass of target, false otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 */
	public boolean isClassOrSubclassOf(ClassFile source, ClassFile target) throws ClassFileException {
		if (source.getName().equals(target.getName())) return true;
		// Walk up the class hierarchy until java.lang.Object has been reached.
		while (source.getSuperClass() != 0) {
			source = this.classLoader.getClassAsClassFile(source.getConstantPool()[source.getSuperClass()].toString());
			if (source.getName().equals(target.getName())) return true;
		}
		// Source is not equal to or a subclass of target.
		return false;
	}

	/**
	 * Check if source implements interface target or a subclass of it. Therefore, the method
	 * isClassOrSubclassOf(ClassFile source, ClassFile target) it utilized, with source being
	 * each interface source implements.
	 *
	 * @param source The source objects' ClassFile.
	 * @param target The target objects' ClassFile.
	 * @return true, if source implements interface target or a subclass of it, false otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 */
	private boolean isInterfaceOf(ClassFile source, ClassFile target) throws ClassFileException {
		int[] interfaces = source.getInterfaces();
		for (int a = 0; a < interfaces.length; a++) {
			if (isClassOrSubclassOf(this.classLoader.getClassAsClassFile(source.getConstantPool()[interfaces[a]].getStringValue()), target)) return true;
		}

		// Recursively check if any of the super classes of source implement the interface.
		ClassFile sourceSuperClassFile = source.getSuperClassFile();
		while (sourceSuperClassFile != null) {
			// Check if the super class implements the interface.
			interfaces = sourceSuperClassFile.getInterfaces();
			for (int a = 0; a < interfaces.length; a++) {
				if (isClassOrSubclassOf(this.classLoader.getClassAsClassFile(sourceSuperClassFile.getConstantPool()[interfaces[a]].getStringValue()), target)) return true;
			}

			// Get the super class' super class.
			sourceSuperClassFile = sourceSuperClassFile.getSuperClassFile();
		}

		// Source does not implement interface T.
		return false;
	}

	/**
	 * Check if source or any of its super classes implements interface target or a subclass of it.
	 * Therefore, the method isInterfaceOf(ClassFile source, ClassFile target) it utilized, with source
	 * being each interface source implements.
	 * @param source The source objects' ClassFile.
	 * @param target The target objects' ClassFile.
	 * @return true, if source or any of its super classes implements interface target or a subclass of it, false otherwise.
	 * @throws ClassFileException Thrown on fatal errors loading or parsing a class file.
	 */
	public boolean implementsInterface(ClassFile source, ClassFile target) throws ClassFileException {
		// First check if the interface or a subclass of it is directly implemented.
		if (isInterfaceOf(source, target)) return true;

		// Walk up the class hierachy until java.lang.Object has been reached.
		while (source.getSuperClass() != 0) {
			source = this.classLoader.getClassAsClassFile(source.getConstantPool()[source.getSuperClass()].toString());
			if (isInterfaceOf(source, target)) return true;
		}

		// Neither source nor any of its super classes implements the interface.
		return false;
	}
}
