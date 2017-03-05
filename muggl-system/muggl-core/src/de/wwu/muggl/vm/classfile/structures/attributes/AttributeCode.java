package de.wwu.muggl.vm.classfile.structures.attributes;

import java.io.DataOutputStream;
import java.io.IOException;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.replaced.QuickInstruction;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.Limitations;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ExceptionTable;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;
import de.wwu.muggl.vm.classfile.support.BytecodeParser;

/**
 * Representation of a attribute_code of a method of a class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-18
 */
public class AttributeCode extends Attribute {
	// Fields that represent data structures of the class.
	private int maxStack;
	private int maxLocals;
	private int codeLength;
	private short[] code;
	private int exceptionTableLength;
	private ExceptionTable[] exceptionTable;
	private int attributeCount;
	private Attribute[] attributes;

	// The only other field.
	private int codePosition;
	Instruction[] instructions = null;
	

	/**
	 * Basic constructor.
	 * @param classFile The ClassFile the attribute belongs to.
	 * @param attributeNameIndex The index in the constant_pool that hold the attribute's name as a UTF8.
	 * @throws ClassFileException Thrown when parsing of sub attributes fails or an invalid index into the constant pool is ecnountered.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	public AttributeCode(ClassFile classFile, int attributeNameIndex) throws ClassFileException, IOException {
		super(classFile, attributeNameIndex);
		if (!this.classFile.getConstantPool()[this.attributeNameIndex].getStringValue().equals("Code")) {
			throw new ClassFileException("Encountered a corrupt class file: attribute_name_index of an attribute_code must be \"Code\".");
		}
		this.maxStack = classFile.getDis().readUnsignedShort();
		if (this.maxStack < 0) {
			throw new ClassFileException("Encountered a corrupt class file: max_stack of an attribute_code must not be negative.");
		}
		if (this.maxStack > Limitations.MAX_MAX_STACK) {
			throw new ClassFileException("Encountered a corrupt class file: max_stack of an attribute_code must be less or equal  than " + Limitations.MAX_MAX_STACK + ".");
		}
		this.maxLocals = classFile.getDis().readUnsignedShort();
		if (this.maxLocals < 0) {
			throw new ClassFileException("Encountered a corrupt class file: max_locals of an attribute_codemust not be negative.");
		}
		if (this.maxLocals > Limitations.MAX_MAX_LOCALS) {
			throw new ClassFileException("Encountered a corrupt class file: max_locals of an attribute_code must be less or ewual than " + Limitations.MAX_MAX_LOCALS + ".");
		}
		this.codeLength = classFile.getDis().readInt();
		if (this.codeLength <= 0) {
			throw new ClassFileException("Encountered a corrupt class file: code_length of an attribute_code must be greater zero.");
		}
		if (this.codeLength >= Limitations.MAX_CODE_LENGTH) {
			throw new ClassFileException("Encountered a corrupt class file: code_length of an attribute_code must be lessor equal  than " + Limitations.MAX_CODE_LENGTH + ".");
		}
		// Read the code.
		this.code = new short[this.codeLength];
		for (int a = 0; a < this.codeLength; a++) {
			this.code[a] = classFile.getDis().readByte();
			if (this.code[a] < 0) {
				this.code[a] += Limitations.OPCODE_TO_OVERFLOW;
			}
		}
		this.exceptionTableLength = classFile.getDis().readUnsignedShort();
		if (this.exceptionTableLength < 0) {
			throw new ClassFileException("Encountered a corrupt class file: An exception_table_length must not be negative.");
		}

		// Build the exception table.
		this.exceptionTable = new ExceptionTable[this.exceptionTableLength];
		for (int a = 0; a < this.exceptionTableLength; a++) {
			this.exceptionTable[a] = new ExceptionTable(this.classFile, this);
		}

		this.attributeCount = classFile.getDis().readUnsignedShort();
		// This attribute might have attributes itself.
		this.attributes = new Attribute[this.attributeCount];

		if (Globals.getInst().logger.isTraceEnabled()) Globals.getInst().logger.trace("Parsing: Reading the attribute_code with name \"" + ((ConstantUtf8) this.classFile.getConstantPool()[this.attributeNameIndex]).getValue() + "\" with " + this.attributeCount + " attributes");
        for (int a = 0; a < this.attributeCount; a++) {
        	int subAttributeNameIndex = classFile.getDis().readUnsignedShort();
        	checkIndexIntoTheConstantPool(subAttributeNameIndex);
        	ConstantUtf8 constant = null;
        	try {
        		constant = (ConstantUtf8) this.classFile.getConstantPool()[subAttributeNameIndex];
        	} catch (ClassCastException e) {
        		throw new ClassFileException("Expected a ConstantUtf8 at " + subAttributeNameIndex + " in the constant_pool when reading the attributes, but got " + this.classFile.getConstantPool()[subAttributeNameIndex].getClass().getName() + ".");
        	}
        	String attributeName = constant.getStringValue();
        	// Which Attribute is it?
        	if (attributeName.equals("LineNumberTable")) {
        		this.attributes[a] = new AttributeLineNumberTable(classFile, subAttributeNameIndex, this);
        	} else if (attributeName.equals("LocalVariableTable")) {
        		this.attributes[a] = new AttributeLocalVariableTable(classFile, subAttributeNameIndex, this);
        	} else {
        		if (Globals.getInst().logger.isDebugEnabled()) Globals.getInst().logger.debug("Parsing attribute_code: Encountered an unknown attribute \"" + attributeName + "\"");
        		this.attributes[a] = new AttributeUnknownSkipped(classFile, subAttributeNameIndex);
        	}
        }
	}

	/**
	 * Write the represented structure to the output stream provided.
	 * @param dos A DataOutputStream to write the represented structure to.
	 * @throws IOException If writing to the output stream failed.
	 */
	@Override
	public void writeToClassFile(DataOutputStream dos) throws IOException {
		super.writeToClassFile(dos);
		dos.writeShort(this.maxStack);
		dos.writeShort(this.maxLocals);
		dos.writeInt(this.codeLength);

		// Write the code.
		for (int a = 0; a < this.codeLength; a++) {
			short byteValue = this.code[a];
			if (byteValue > Limitations.OPCODE_OVERFLOW) byteValue -= Limitations.OPCODE_TO_OVERFLOW;
			dos.writeByte(byteValue);
		}

		// Write the exception table.
		dos.writeShort(this.exceptionTableLength);
		for (int a = 0; a < this.exceptionTableLength; a++) {
			this.exceptionTable[a].writeToClassFile(dos);
		}

		// Write the attributes.
		dos.writeShort(this.attributeCount);
		for (int a = 0; a < this.attributeCount; a++) {
			this.attributes[a].writeToClassFile(dos);
		}
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "attribute_code";
	}

	/**
	 * Getter for the instructions and other bytes. This array can be used for execution, since
	 * jumping is much easier in this form.
	 *
	 * @return The instructions as an array of Instruction objects. Objects being null represent
	 *         other bytes.
	 * @throws InvalidInstructionInitialisationException Any fatal problems with the parsing and the
	 *         initialization will lead to this exception.
	 */
	public Instruction[] getInstructionsAndOtherBytes() throws InvalidInstructionInitialisationException {
		// If instructions are requested for the first time, they are parsed and loaded.
		if (this.instructions == null) {
			try {
				this.instructions = new Instruction[this.code.length];
				for (this.codePosition = 0; this.codePosition < this.code.length; this.codePosition++) {
					this.instructions[this.codePosition] =
						BytecodeParser.parse(this.code[this.codePosition], this, this.codePosition);
				}
			} catch (InvalidInstructionInitialisationException e) {
				// If anything goes wrong, reset the instructions array.
				this.instructions = null;
				throw new InvalidInstructionInitialisationException(e);
			}
		}
		return this.instructions;
	}

	/**
	 * Getter for the instructions.
	 * @return The instructions as an array of Instruction objects.
	 * @throws InvalidInstructionInitialisationException Any fatal problems with the parsing and the initialization will lead to this exception.
	 */
	public Instruction[] getInstructions() throws InvalidInstructionInitialisationException {
		Instruction[] instructionsAndOtherBytes = getInstructionsAndOtherBytes();
		int numberOfInstructions = 0;
		for (int a = 0; a < instructionsAndOtherBytes.length; a++) {
			if (instructionsAndOtherBytes[a] != null) numberOfInstructions++;
		}
		Instruction[] instructions = new Instruction[numberOfInstructions];
		int b = 0;
		for (int a = 0; a < instructionsAndOtherBytes.length; a++) {
			if (instructionsAndOtherBytes[a] != null) {
				instructions[b] = instructionsAndOtherBytes[a];
				b++;
			}
		}
		return instructions;
	}

	/**
	 * Replace the instruction at <code>index</code> with the specified QuickIsntruction. This is
	 * a low level method and will neither check whether replacing instructions is granted, nor if
	 * the specified instruction is a suitable replacement.<br />
	 * <br />
	 * Do <b>not</b> use this method before the instructions have been retrieved. It will not check
	 * for that and throw a {@link NullPointerException}!
	 *
	 * @param quick The QuickInstruction
	 * @param index The index into the instructions.
	 * @throws IllegalArgumentException If the index into the instructions is not suitable.
	 * @throws NullPointerException If <code>quick</code> is null.
	 */
	public void replaceInstruction(QuickInstruction quick, int index) {
		Instruction[] instructions = this.instructions;

		// Checks.
		if (quick == null)
			throw new NullPointerException("The specified QuickInstruction must not be null");

		if (index < 0 || index > instructions.length - 1) {
			throw new IllegalArgumentException("No valid index into the instructions.");
		}

		if (instructions[index] == null) {
			throw new IllegalArgumentException("Cannot replace an instruction at the specified index.");
		}

		int additionalBytes = 1;
		while (instructions[index + additionalBytes] == null) {
			additionalBytes++;
		}
		additionalBytes--;

		if (quick.getNumberOfOtherBytes() != additionalBytes) {
			throw new IllegalArgumentException(
					"Cannot replace instructions with unequal number of additional bytes.");
		}

		// Replace it.
		this.instructions[index] = quick;
	}

	/**
	 * Reset replaced instruction to their original values (Instructions of type
	 * {@link de.wwu.muggl.instructions.replaced.ReplacingInstruction}. There is no reason to run this method unless an application
	 * has been executed, which might have lead to executions being replaced.
	 */
	public void resetReplacedInstructions() {
		if (this.instructions != null) {
			for (int a = 0; a < this.instructions.length; a++) {
				Instruction instruction = this.instructions[a];
				if (instruction != null && instruction instanceof QuickInstruction) {
					this.instructions[a] = ((QuickInstruction) instruction).getReplacer();
				}
			}
		}
	}

	/**
	 * Unload priorly cached instructions. The next times a instruction of the array of instructions
	 * is requested, the according byte codes are read the the instructions generated.
	 */
	public void unloadInstructions() {
		this.instructions = null;
	}

	/**
	 * Get the next byte of the code. Warning: This method is only intended to by used by classes parsing and
	 * generating the current instructions after getInstructions() has been invoked.
	 * @return The next unsigned byte of the code as a short.
	 * @throws NoMoreCodeBytesException If the end of the code stream has been reached.
	 */
	public short getNextCodeByte() throws NoMoreCodeBytesException {
		this.codePosition++;
		if (this.codePosition >= this.code.length) {
			throw new NoMoreCodeBytesException("Bytecode error: There are no more additional bytes.");
		}
		return this.code[this.codePosition];
	}

	/**
	 * Get the next bytes of the code. Warning: This method is only intended to by used by classes parsing and
	 * generating the current instructions after getInstructions() has been invoked.
	 * @param number The number of bytes to fetch.
	 * @return An array with unsigned bytes of the code as short-values.
	 * @throws NoMoreCodeBytesException If the end of the code stream has been reached, or 0 or less bytes have been requested.
	 */
	public short[] getNextCodeBytes(int number) throws NoMoreCodeBytesException {
		if (number < 0) {
			throw new NoMoreCodeBytesException("Parameter number must be greater than 0");
		}
		short[] bytes = new short[number];
		for (int a = 1; a <= number; a++) {
			if (this.codePosition + a >= this.code.length) {
				throw new NoMoreCodeBytesException("Bytecode error: There are no more additional bytes.");
			}
			bytes[a - 1] = this.code[this.codePosition + a];
		}
		this.codePosition += number;

		return bytes;
	}

	/**
	 * Getter for the attributes.
	 * @return The attributes as an array of Attribute objects.
	 */
	public Attribute[] getAttributes() {
		return this.attributes;
	}

	/**
	 * Getter for the attribute_count.
	 * @return The attribute_count as an int.
	 */
	public int getAttributeCount() {
		return this.attributeCount;
	}

	/**
	 * Getter for the code.
	 * @return The code bytes as an array of short.
	 */
	public short[] getCode() {
		return this.code;
	}

	/**
	 * Getter for the code_length.
	 * @return The code_length as an int.
	 */
	public int getCodeLength() {
		return this.codeLength;
	}

	/**
	 * Getter for the exception_table.
	 * @return The exception_table as an array of ExceptionTable objects.
	 */
	public ExceptionTable[] getExceptionTable() {
		return this.exceptionTable;
	}

	/**
	 * Getter for the exception_table_length.
	 * @return The exception_table_length as an int.
	 */
	public int getExceptionTableLength() {
		return this.exceptionTableLength;
	}

	/**
	 * Getter for the max_locals.
	 * @return The max_locals as an int.
	 */
	public int getMaxLocals() {
		return this.maxLocals;
	}

	/**
	 * Getter for the max_stack.
	 * @return The max_stack as an int.
	 */
	public int getMaxStack() {
		return this.maxStack;
	}

}
