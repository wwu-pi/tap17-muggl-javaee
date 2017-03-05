package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantDouble;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantLong;
import de.wwu.muggl.solvers.expressions.DoubleConstant;
import de.wwu.muggl.solvers.expressions.LongConstant;

/**
 * Implementation of the instruction <code>ldc2_w</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public class Ldc2_w extends de.wwu.muggl.instructions.general.PushFromConstantPool implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Ldc2_w(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "ldc2_w";
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return 2;
	}

	/**
	 * Provide information about the object that will be fetched. The returned String array
	 * has two elements. The first one represents the String representation of the fully
	 * qualified name of the object. The second holds a String representation of the objects'
	 * value.<br />
	 * <br />
	 * This method is especially meant to be used by the GUI. It is computationally more
	 * efficient than provideObject() if a ConstantString or ConstantClass is found. It does,
	 * however, not provide an object that can be used in the virtual machine.
	 *
	 * @param frame The currently executed frame.
	 * @return Information about the object that will be fetched.
	 */
	@Override
	public String[] provideObjectInformation(Frame frame) {
		String[] information = new String[2];
		Constant constant = frame.getConstantPool()[fetchIndex(this.otherBytes)];
		if (constant instanceof ConstantDouble) {
			information[0] = "java.lang.Double";
		} else if (constant instanceof ConstantLong) {
			information[0] = "java.lang.Long";
		}
		information[1] = constant.toString();
		return information;
	}

	/**
	 * Check if the supplied Object meets a expected type. If this is met, convert it from the
	 * internal class file structure into the desired type. Otherwise return null.
	 *
	 * @param frame The currently executed frame.
	 * @param constant An constant from the constant_pool.
	 * @return An object for further processing, or null.
	 */
	@Override
	protected Object checkAndGetObjectType(Frame frame, Constant constant) {
		if (constant instanceof ConstantLong) {
			ConstantLong constantLong = (ConstantLong) constant;
			return constantLong.getValue();
		} else if (constant instanceof ConstantDouble) {
			ConstantDouble constantDouble = (ConstantDouble) constant;
			return constantDouble.getValue();
		}
		return null;
	}

	/**
	 * Check if the supplied Object meets a expected type. If this is met, convert it from the
	 * internal class file structure into the desired symbolic type. Otherwise return null.
	 *
	 * @param frame The currently executed frame.
	 * @param constant An constant from the constant_pool.
	 * @return An object for further processing, or null.
	 */
	@Override
	protected Object checkAndGetSymbolicObjectType(Frame frame, Constant constant) {
		if (constant instanceof ConstantLong) {
			ConstantLong constantLong = (ConstantLong) constant;
			return LongConstant.getInstance(constantLong.getValue());
		} else if (constant instanceof ConstantDouble) {
			ConstantDouble constantDouble = (ConstantDouble) constant;
			return DoubleConstant.getInstance(constantDouble.getValue());
		}
		return null;
	}

	/**
	 * Fetch the index into the constant_pool from the additional bytes of the instruction.
	 * @param otherBytes The array of additional bytes.
	 * @return The index.
	 */
	@Override
	protected int fetchIndex(short[] otherBytes) {
		return otherBytes[0] << ONE_BYTE | otherBytes[1];
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
	@Override
	public byte getTypePushed(ClassFile methodClassFile) {
		Constant constant = methodClassFile.getConstantPool()[fetchIndex(this.otherBytes)];
		if (constant instanceof ConstantDouble) {
			return ClassFile.T_DOUBLE;
		}
		return ClassFile.T_LONG;
	}

}
