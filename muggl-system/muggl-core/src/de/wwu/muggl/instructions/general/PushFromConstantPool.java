package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.VirtualMachine;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantClass;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantFloat;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantInteger;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantString;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.ResolutionAlgorithms;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.solvers.expressions.FloatConstant;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * Abstract instruction with some concrete methods for ldc instructions. These are used to push
 * items from the runtime constant pool. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-31
 */
public abstract class PushFromConstantPool extends GeneralInstructionWithOtherBytes implements
		JumpNever, StackPush {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public PushFromConstantPool(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the inheriting instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			Object object = provideObject(frame);
			if (object == null)
				throw new ExecutionException("Wrong type fetched from constant_pool for " + getName() + " instruction.");
			frame.getOperandStack().push(object);
		} catch (VmRuntimeException e) {
			ExceptionHandler handler = new ExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailed(e2);
			}
		} catch (ExecutionException e) {
			executionFailed(e);
		}
	}

	/**
	 * Execute the inheriting instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		try {
			Object object = provideSymbolicObject(frame);
			if (object == null)
				throw new ExecutionException("Wrong type fetched from constant_pool for " + getName() + " instruction.");
			frame.getOperandStack().push(object);
		} catch (VmRuntimeException e) {
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		} catch (ExecutionException e) {
			symbolicExecutionFailedWithAnExecutionException(e);
		}
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 1;
	}

	/**
	 * Provide the desired object from the constant pool. The index is generated from the additional
	 * bytes of the instruction.
	 *
	 * @param frame The currently executed frame.
	 * @return The desired Object.
	 * @throws VmRuntimeException If resolving of a class fails.
	 */
	public Object provideObject(Frame frame) throws VmRuntimeException {
		Constant constant = frame.getConstantPool()[fetchIndex(this.otherBytes)];
		return checkAndGetObjectType(frame, constant);
	}

	/**
	 * Provide the desired object from the constant pool and convert it into a symbolic object. The
	 * index is generated from the additional bytes of the instruction.
	 *
	 * @param frame The currently executed frame.
	 * @return The desired Object.
	 * @throws VmRuntimeException If resolving of a class fails.
	 */
	public Object provideSymbolicObject(Frame frame) throws VmRuntimeException {
		Constant constant = frame.getConstantPool()[fetchIndex(this.otherBytes)];
		return checkAndGetSymbolicObjectType(frame, constant);
	}

	/**
	 * Provide information about the object that will be fetched. The returned String array
	 * has two elements. The first one represents the String representation of the fully
	 * qualified name of the object. The second holds a String representation of the objects'
	 * value.
	 *
	 * This method is especially meant to be used by the GUI. It is computationally more
	 * efficient than provideObject() if a ConstantString or ConstantClass is found. It does,
	 * however, not provide an object that can be used in the virtual machine.
	 *
	 * @param frame The currently executed frame.
	 * @return Information about the object that will be fetched.
	 */
	public String[] provideObjectInformation(Frame frame) {
		String[] information = new String[2];
		Constant constant = frame.getConstantPool()[fetchIndex(this.otherBytes)];
		if (constant instanceof ConstantString) {
			information[0] = "java.lang.String";
		} else if (constant instanceof ConstantClass) {
			information[0] = "class";
		} else if (constant instanceof ConstantFloat) {
			information[0] = "java.lang.Float";
		} else if (constant instanceof ConstantInteger) {
			information[0] = "java.lang.Integer";
		}
		information[1] = constant.toString();
		return information;
	}

	/**
	 * Check if the supplied Object meets a expected type. If this is met, convert it from the
	 * internal class file structure into the desired type. Otherwise return null.
	 *
	 * @param frame The currently executed frame.
	 * @param constant A constant from the constant_pool.
	 * @return An object for further processing, or null.
	 * @throws VmRuntimeException If resolving of a class fails.
	 */
	protected Object checkAndGetObjectType(Frame frame, Constant constant) throws VmRuntimeException {
		if (constant instanceof ConstantString) {
			String value = ((ConstantString) constant).getStringValue();
			return frame.getVm().getStringCache().getStringObjectref(value);
		} else if (constant instanceof ConstantClass) {
			return provideClassReference(
					frame.getVm(), (ConstantClass) constant, frame.getMethod().getClassFile());
		} else if (constant instanceof ConstantInteger) {
			ConstantInteger constantInteger = (ConstantInteger) constant;
			return constantInteger.getValue();
		} else if (constant instanceof ConstantFloat) {
			ConstantFloat constantFloat = (ConstantFloat) constant;
			return constantFloat.getValue();
		}
		return null;
	}

	/**
	 * Check if the supplied constant meets a expected type. If this is met, convert it from the
	 * internal class file structure into the desired symbolic type. Otherwise return null.
	 *
	 * @param frame The currently executed frame.
	 * @param constant A constant from the constant_pool.
	 * @return An constant for further processing, or null.
	 * @throws VmRuntimeException If resolving of a class fails.
	 */
	protected Object checkAndGetSymbolicObjectType(Frame frame, Constant constant)
			throws VmRuntimeException {
		if (constant instanceof ConstantString) {
			String value = ((ConstantString) constant).getStringValue();
			return frame.getVm().getStringCache().getStringObjectref(value);
		} else if (constant instanceof ConstantClass) {
			return provideSymbolicClassReference(
					frame.getVm(), (ConstantClass) constant, frame.getMethod().getClassFile());
		} else if (constant instanceof ConstantInteger) {
			ConstantInteger constantInteger = (ConstantInteger) constant;
			return IntConstant.getInstance(constantInteger.getValue());
		} else if (constant instanceof ConstantFloat) {
			ConstantFloat constantFloat = (ConstantFloat) constant;
			return FloatConstant.getInstance(constantFloat.getValue());
		}
		return null;
	}

	/**
	 * Provide a String reference for a CONSTANT_Class_info structure.
	 *
	 * @param vm The virtual machine this instruction is executed in.
	 * @param constant The ONSTANT_Class_info structure to get the String value from.
	 * @param d The symbolic reference origin.
	 * @return A class reference.
	 * @throws VmRuntimeException If resolving of the class fails.
	 */
	private Object provideClassReference(VirtualMachine vm, ConstantClass constant, ClassFile d)
			throws VmRuntimeException {
		try {
			ResolutionAlgorithms resolution = new ResolutionAlgorithms(vm.getClassLoader());
			String value = constant.getValue().replace("/", ".");
			ClassFile classFile = resolution.resolveClassAsClassFile(d, "java.lang.Class");
			Objectref objectref = vm.getAnObjectref(classFile);
			Field field = classFile.getFieldByName("name");
			Object stringReference;
			stringReference = vm.getStringCache().getStringObjectref(value);
			objectref.putField(field, stringReference);
			return objectref;
		} catch (IllegalAccessError e) {
			throw new VmRuntimeException(vm.generateExc("java.lang.IllegalAccessError", e.getMessage()));
		} catch (NoClassDefFoundError e) {
			throw new VmRuntimeException(vm.generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
		}
	}

	/**
	 * Provide a String reference for a CONSTANT_Class_info structure that can be used for symbolic
	 * execution. Its name field will be initialized using the
	 * provideSymbolicStringReference(VirtualMacvhine, ConstantString) method.
	 *
	 * @param vm The virtual machine this instruction is executed in.
	 * @param constant The ONSTANT_Class_info structure to get the String value from.
	 * @param d The symbolic reference origin.
	 * @return A class reference.
	 * @throws VmRuntimeException If resolving of the class fails.
	 */
	private Object provideSymbolicClassReference(VirtualMachine vm, ConstantClass constant,
			ClassFile d) throws VmRuntimeException {
		try {
			ResolutionAlgorithms resolution = new ResolutionAlgorithms(vm.getClassLoader());
			String value = constant.getValue().replace("/", ".");
			ClassFile classFile = resolution.resolveClassAsClassFile(d, "java.lang.Class");
			Objectref objectref = vm.getAnObjectref(classFile);
			Field field = classFile.getFieldByName("name");
			Object stringReference;
			stringReference = vm.getStringCache().getStringObjectref(value);
			objectref.putField(field, stringReference);
			return objectref;
		} catch (IllegalAccessError e) {
			throw new VmRuntimeException(vm.generateExc("java.lang.IllegalAccessError", e.getMessage()));
		} catch (NoClassDefFoundError e) {
			throw new VmRuntimeException(vm.generateExc("java.lang.NoClassDefFoundError", e.getMessage()));
		}
	}

	/**
	 * Fetch the index into the constant_pool from the additional bytes of the instruction.
	 *
	 * @param otherBytes The array of additional bytes.
	 * @return The index.
	 */
	protected abstract int fetchIndex(short[] otherBytes);

	/**
	 * Get the number of elements that will be pushed onto the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be pushed onto the stack.
	 */
	public int getNumberOfPushedElements() {
		return 1;
	}

	/**
	 * Get the type of elements this instruction will push onto the stack.
	 *
	 * @param methodClassFile The class file of the method this instruction belongs to.
	 * @return The type this instruction pushes. Types are {@link ClassFile#T_BOOLEAN},
	 *         {@link ClassFile#T_BYTE} {@link ClassFile#T_CHAR}, {@link ClassFile#T_DOUBLE},
	 *         {@link ClassFile#T_FLOAT}, {@link ClassFile#T_INT}, {@link ClassFile#T_LONG} and
	 *         {@link ClassFile#T_SHORT}, 0 to indicate a reference or return address type or -1 to
	 *         indicate the pushed type cannot be determined statically.
	 */
	public byte getTypePushed(ClassFile methodClassFile) {
		Constant constant = methodClassFile.getConstantPool()[fetchIndex(this.otherBytes)];
		if (constant instanceof ConstantFloat) {
			return ClassFile.T_FLOAT;
		} else if (constant instanceof ConstantInteger) {
			return ClassFile.T_INT;
		}

		// It is either a String or a class. Both are reference types.
		return 0;
	}

}
