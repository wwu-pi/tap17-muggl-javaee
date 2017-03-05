package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.FieldResolutionError;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.jpa.criteria.metamodel.SymbolicEntityAttribute;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantFieldref;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ResolutionAlgorithms;
import de.wwu.muggl.vm.loading.MugglClassLoader;

/**
 * Abstract super class for instructions that access fields.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-26
 */
public abstract class FieldAccess extends GeneralInstructionWithOtherBytes {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method
	 * that the instruction belongs to is supplied as an argument.
	 *
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized
	 *         successfully, most likely due to missing additional bytes. This might be caused by a
	 *         corrupt class file, or a class file of a more recent version than what can be handled.
	 */
	public FieldAccess(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Get the field accessed by the inheriting instruction.
	 *
	 * @param frame The currently executed frame.
	 * @param methodClassFile The class file of the currently executed method.
	 * @param classLoader The active class loader.
	 * @return The field.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 * @throws NoSuchFieldError If the Field could not be found.
	 * @throws VmRuntimeException On runtime exceptions.
	 */
	protected Field getField(Frame frame, ClassFile methodClassFile, MugglClassLoader classLoader)
			throws ExecutionException, VmRuntimeException {
		int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
		Constant constant = methodClassFile.getConstantPool()[index];
		if (!(constant instanceof ConstantFieldref))
			throw new ExecutionException("Error while executing instruction " + getName()
					+ ": Runtime constant pool item at index " + index
					+ " must be a symbolic reference to a field.");

		// Resolve the class of the field.
		ResolutionAlgorithms resolution = new ResolutionAlgorithms(classLoader);
		String className = ((ConstantFieldref) constant).getClassName();
		ClassFile classFile;
		try {
			classFile = resolution.resolveClassAsClassFile(methodClassFile, className);
		} catch (IllegalAccessError e) {
			if (frame == null) {
				throw new ExecutionException(e);
			}
			throw new VmRuntimeException(frame.getVm().generateExc(
					"java.lang.IllegalAccessError", e.getMessage()));
		} catch (NoClassDefFoundError e) {
			if (frame == null) {
				throw new ExecutionException(e);
			}
			throw new VmRuntimeException(frame.getVm().generateExc(
					"java.lang.NoClassDefFoundError", e.getMessage()));
		}
		
		// Resolve the Field.
		String[] nameAndType = ((ConstantFieldref) constant).getNameAndTypeInfo();
		Field field;
		try {
			field = resolution.resolveField(classFile, nameAndType);
		} catch (ClassFileException e) {
			throw new ExecutionException(e);
		} catch (NoSuchFieldError e) {
			if (frame == null) {
				throw new ExecutionException(e);
			}
			throw new VmRuntimeException(frame.getVm().generateExc(
					"java.lang.NoSuchFieldError", e.getMessage()));
		}
		
		return field;
	}

	/**
	 * Get the field accessed by the inheriting instruction without actual execution.<br />
	 * <br />
	 * Use this method for static purposes only. It is not meant to be utilized when executing an
	 * instruction.
	 * 
	 * @param methodClassFile The class file of the currently executed method.
	 * @param classLoader The active class loader.
	 * @return The field.
	 * @throws IllegalAccessError - If access to the class resolved is illegal.
	 * @throws NoClassDefFoundError - If no class could be found for the name n.
	 * @throws NoSuchFieldError If the Field could not be found.
	 * @throws ClassFileException On problems resolving the field.
	 */
	public Field getFieldNoExecution(ClassFile methodClassFile, MugglClassLoader classLoader) throws ClassFileException {
		int index = this.otherBytes[0] << ONE_BYTE | this.otherBytes[1];
		Constant constant = methodClassFile.getConstantPool()[index];
		if (!(constant instanceof ConstantFieldref))
			throw new FieldResolutionError("Runtime constant pool item at index " + index
					+ " must be a symbolic reference to a field.");

		// Resolve the class of the field.
		ResolutionAlgorithms resolution = new ResolutionAlgorithms(classLoader);
		String className = ((ConstantFieldref) constant).getClassName();
		ClassFile classFile = resolution.resolveClassAsClassFile(methodClassFile, className);

		// Resolve the Field.
		String[] nameAndType = ((ConstantFieldref) constant).getNameAndTypeInfo();
		return resolution.resolveField(classFile, nameAndType);
	}

	/**
	 * Get the type of the field.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The type of the field. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference value or -1 to
	 *         indicate the type cannot be determined statically.
	 */
	public byte getFieldType(ClassFile methodClassFile) {
		// First get the field.
		Field field;
		try {
			field = getField(null, methodClassFile, methodClassFile.getClassLoader());
		} catch (ExecutionException e) {
			return -1;
		} catch (VmRuntimeException e) {
			return -1;
		}

		// Then determine its type.
		String typeString = field.getType();
		if (typeString.equals("boolean")) {
			return ClassFile.T_BOOLEAN;
		} else if (typeString.equals("byte")) {
			return ClassFile.T_BYTE;
		} else if (typeString.equals("char")) {
			return ClassFile.T_CHAR;
		} else if (typeString.equals("double")) {
			return ClassFile.T_DOUBLE;
		} else if (typeString.equals("float")) {
			return ClassFile.T_FLOAT;
		} else if (typeString.equals("int")) {
			return ClassFile.T_INT;
		} else if (typeString.equals("long")) {
			return ClassFile.T_LONG;
		} else if (typeString.equals("short")) {
			return ClassFile.T_SHORT;
		}

		// It has to be a reference type.
		return 0;
	}

}
