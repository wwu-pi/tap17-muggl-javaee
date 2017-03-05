package de.wwu.muggl.instructions.replaced.quick;

import de.wwu.muggl.instructions.bytecode.Ldc;
import de.wwu.muggl.instructions.bytecode.Ldc2_w;
import de.wwu.muggl.instructions.bytecode.Ldc_w;
import de.wwu.muggl.instructions.general.GeneralInstructionWithOtherBytes;
import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.instructions.replaced.QuickInstruction;
import de.wwu.muggl.instructions.replaced.ReplacingInstruction;
import de.wwu.muggl.instructions.replaced.replacer.Ldc2_wReplacer;
import de.wwu.muggl.instructions.replaced.replacer.LdcReplacer;
import de.wwu.muggl.instructions.replaced.replacer.Ldc_wReplacer;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantFloat;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantInteger;

/**
 * {@link QuickInstruction} for <i>ldc</i>,
 * <i>ldc_w</i> and <i>ldw2_w</i>. It just pushes the priorly stored object onto the operand
 * stack instead of fetching it from the constant pool.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-12-14
 */
public class LdcQuick extends GeneralInstructionWithOtherBytes implements JumpNever, StackPush,
		QuickInstruction {
	private ReplacingInstruction replacer;
	private Object storedObject;
	private int typeOfLdc;

	/**
	 * Construct an instance to replace {@link Ldc}.
	 *
	 * @param replacer The {@link ReplacingInstruction} that constructs this.
	 * @param objectToStore The object to push upon execution.
	 * @param index The additional byte of the ldc instruction replaced by this.
	 */
	public LdcQuick(LdcReplacer replacer, Object objectToStore, short index) {
		this.replacer = replacer;
		this.typeOfLdc = 0;
		this.storedObject = objectToStore;
		this.otherBytes = new short[1];
		this.otherBytes[0] = index;
	}

	/**
	 * Construct an instance to replace {@link Ldc_w}.
	 *
	 * @param replacer The {@link ReplacingInstruction} that constructs this.
	 * @param objectToStore The object to push upon execution.
	 * @param index1 The first additional byte of the ldc instruction replaced by this.
	 * @param index2 The second additional byte of the ldc instruction replaced by this.
	 */
	public LdcQuick(Ldc_wReplacer replacer, Object objectToStore, short index1, short index2) {
		this.replacer = replacer;
		this.typeOfLdc = 1;
		this.storedObject = objectToStore;
		this.otherBytes = new short[2];
		this.otherBytes[0] = index1;
		this.otherBytes[1] = index2;
	}

	/**
	 * Construct an instance to replace {@link Ldc2_w}.
	 *
	 * @param replacer The {@link ReplacingInstruction} that constructs this.
	 * @param objectToStore The object to push upon execution.
	 * @param index1 The first additional byte of the ldc instruction replaced by this.
	 * @param index2 The second additional byte of the ldc instruction replaced by this.
	 */
	public LdcQuick(Ldc2_wReplacer replacer, Object objectToStore, short index1, short index2) {
		this.replacer = replacer;
		this.typeOfLdc = 2;
		this.storedObject = objectToStore;
		this.otherBytes = new short[2];
		this.otherBytes[0] = index1;
		this.otherBytes[1] = index2;
	}

	/**
	 * Execute the instruction.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void execute(Frame frame) {
		frame.getOperandStack().push(this.storedObject);
	}

	/**
	 * Execute the instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 */
	@Override
	public void executeSymbolically(Frame frame) {
		frame.getOperandStack().push(this.storedObject);
	}

	/**
	 * Get the number of other bytes for this instruction.
	 *
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		if (this.typeOfLdc > 0) {
			return 2;
		}
		return 1;
	}

	/**
	 * Resolve the instructions name.
	 *
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		switch (this.typeOfLdc) {
			case 1:
				return "ldc_w";
			case 2:
				return "ldc2_w";
			default:
				return "ldc";
		}
	}

	/**
	 * Resolve the instructions name including the additional bytes (if there are any).
	 *
	 * @return The instructions name including the additional bytes as a String.
	 */
	@Override
	public String getNameWithOtherBytes() {
		switch (this.typeOfLdc) {
			case 1:
				return "ldc_w " + this.otherBytes[0] + " " + this.otherBytes[1];
			case 2:
				return "ldc2_w " + this.otherBytes[0]  + " " + this.otherBytes[1];
			default:
				return "ldc " + this.otherBytes[0];
		}
	}

	/**
	 * Get the {@link ReplacingInstruction} that constructed this QuickInstruction.
	 *
	 * @return The ReplacingInstruction that constructed this QuickInstruction.
	 */
	public ReplacingInstruction getReplacer() {
		return this.replacer;
	}
	
	/*
	 * The following methods have just been copied from PushFromConstantPool. There should be a better solution
	 * (without inheriting from PushFromConstantPool). Probably a shared super class?
	 */
	
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
	 * Fetch the index into the constant_pool from the additional bytes of the instruction.
	 * @param otherBytes The array of additional bytes.
	 * @return The index.
	 */
	protected int fetchIndex(short[] otherBytes) {
		return otherBytes[0];
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
