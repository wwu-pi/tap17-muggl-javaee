package de.wwu.muggl.vm.classfile.structures;

import java.io.IOException;
import java.util.Set;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.instructions.replaced.QuickInstruction;
import de.wwu.muggl.instructions.replaced.ReplacingInstruction;
import de.wwu.muggl.symbolic.flow.controlflow.ControlGraph;
import de.wwu.muggl.symbolic.generating.ArrayElementsGeneratorProvider;
import de.wwu.muggl.symbolic.generating.GeneratorProvider;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeAnnotationDefault;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeDeprecated;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeExceptions;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeLocalVariableTable;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeInvisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeInvisibleParameterAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleParameterAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSynthetic;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeUnknownSkipped;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.LocalVariableTable;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * Representation of a method in classes. This class is a concrete implementation of the
 * abstract FieldMethod.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-05-03
 */
public class Method extends FieldMethod {
	// General fields
	private AttributeCode codeAttribute;
	private Object[] predefinedParameters;
	private GeneratorProvider[] generatorProviders;
	private ArrayElementsGeneratorProvider[] arrayElementsGeneratorProviders;
	private boolean[] takeASecondSlot;
	private String[] parameterNames;
	private Variable[] variables;
	private Object[] generatedValues;
	private ControlGraph controlGraph;

	// Fields for the parsed access flags.
	private boolean accSynchronized;
	private boolean accBridge;
	private boolean accVarargs;
	private boolean accNative;
	private boolean accAbstract;
	private boolean accStrict;
	
	// Cached hash code
	private boolean hashComputed = false;
	private int hashCode;

	/**
	 * Basic constructor.
	 *
	 * @param classFile The ClassFile the Method belongs to.
	 * @throws ClassFileException On unexpected fatal errors when parsing the class file.
	 * @throws IOException On errors reading from the DataInputStream of the class.
	 */
	public Method(ClassFile classFile) throws ClassFileException, IOException {
		super(classFile);
		createParametersArray();
	}

	/**
	 * Get a String representation of this class file's structure name.
	 * @return A String representation of this class file's structure name.
	 */
	@Override
	public String getStructureName() {
		return "Method_info";
	}

	/**
	 * Parse the attribute number a. Only such attributes will be read that are of relevance
	 * for fields. Other attributes will be skipped.
	 * @param a The number of the attribute.
	 * @throws ClassFileException Thrown on unexpected fatal errors when parsing the class file.
	 * @throws IOException Thrown on errors reading from the DataInputStream of the class.
	 */
	@Override
	protected void readAttribute(int a) throws ClassFileException, IOException {
    	int attributeNameIndex = this.classFile.getDis().readUnsignedShort();
    	checkIndexIntoTheConstantPool(attributeNameIndex);
    	ConstantUtf8 constant = null;
    	try {
    		constant = (ConstantUtf8) this.classFile.getConstantPool()[attributeNameIndex];
    	} catch (ClassCastException e) {
    		throw new ClassFileException("Expected a ConstantUtf8 at " + attributeNameIndex + " in the constant_pool when reading the attributes, but got " + this.classFile.getConstantPool()[attributeNameIndex].getClass().getName() + ".");
    	}
    	String attributeName = constant.getStringValue();
    	// Which Attribute is it?
    	if (attributeName.equals("Code")) {
    		this.codeAttribute = new AttributeCode(this.classFile, attributeNameIndex);
    		this.attributes[a] = this.codeAttribute;
    	} else if (attributeName.equals("Exceptions")) {
    		this.attributes[a] = new AttributeExceptions(this.classFile, attributeNameIndex);
    	} else if (attributeName.equals("Synthetic")) {
    		this.attributes[a] = new AttributeSynthetic(this.classFile, attributeNameIndex);
    	} else if (attributeName.equals("Deprecated")) {
    		this.attributes[a] = new AttributeDeprecated(this.classFile, attributeNameIndex);
		} else if (attributeName.equals("RuntimeVisibleAnnotations")) {
			this.attributes[a] = new AttributeRuntimeVisibleAnnotations(this.classFile,
					attributeNameIndex);
		} else if (attributeName.equals("RuntimeInvisibleAnnotations")) {
			this.attributes[a] = new AttributeRuntimeInvisibleAnnotations(this.classFile,
					attributeNameIndex);
		} else if (attributeName.equals("RuntimeVisibleParameterAnnotations")) {
			this.attributes[a] = new AttributeRuntimeVisibleParameterAnnotations(this.classFile,
					attributeNameIndex);
		} else if (attributeName.equals("RuntimeInvisibleParameterAnnotations")) {
			this.attributes[a] = new AttributeRuntimeInvisibleParameterAnnotations(this.classFile,
					attributeNameIndex);
		} else if (attributeName.equals("AnnotationDefault")) {
			this.attributes[a] = new AttributeAnnotationDefault(this.classFile, attributeNameIndex);
		} else {
	if (Globals.getInst().logger.isDebugEnabled()) Globals.getInst().logger.debug("Parsing: Encountered an unknown attribute \"" + attributeName + "\"");
    		this.attributes[a] = new AttributeUnknownSkipped(this.classFile, attributeNameIndex);
    	}
	}

	/**
	 * Simply return "Method" as a String.
	 * @return "Method" as a String.
	 */
	@Override
	protected String getFieldMethod() {
		return "Method";
	}

	/**
	 * Get the full name, including prefixes, parameters etc.
	 * @return The full name.
	 */
	@Override
	public String getFullName() {
		return getPrefix() + getReturnType() + " " + getName() + "(" + getParameterTypes() + ")";
	}

	/**
	 * Parse the access flags field so that the single access flags can be easily checked.
	 *
	 * @throws ClassFileException If the access_flags have a value less than zero or if there is an
	 *         illegal combination of flags.
	 */
	@Override
	protected void parseAccessFlags() throws ClassFileException {
		int flags = this.accessFlags;
		if (flags < 0) throw new ClassFileException("Encountered a corrupt class file: access_flags of a " + getName() + " is less than zero.");

		// Parse the flags.
		if (flags >= 0x4000) {
			flags -= 0x4000;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x4000 which is unknown for a method.");
		}
		if (flags >= 0x2000) {
			flags -= 0x2000;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x2000 which is unknown for a method.");
		}
		if (flags >= ClassFile.ACC_SYNTHETIC) {
			flags -= ClassFile.ACC_SYNTHETIC;
			this.accSynthetic = true;
		}
		if (flags >= ClassFile.ACC_STRICT) {
			flags -= ClassFile.ACC_STRICT;
			this.accStrict = true;
		}
		if (flags >= ClassFile.ACC_ABSTRACT) {
			flags -= ClassFile.ACC_ABSTRACT;
			this.accAbstract = true;
		}
		if (flags >= 0x0200) {
			flags -= 0x0200;
			if (Globals.getInst().logger.isDebugEnabled())
				Globals.getInst().logger.debug(
						"Encountered and ignored a flag with value 0x0200 which is unknown for a method.");
		}
		if (flags >= ClassFile.ACC_NATIVE) {
			flags -= ClassFile.ACC_NATIVE;
			this.accNative = true;
		}
		if (flags >= ClassFile.ACC_VARARGS) {
			flags -= ClassFile.ACC_VARARGS;
			this.accVarargs = true;
		}
		if (flags >= ClassFile.ACC_BRIDGE) {
			flags -= ClassFile.ACC_BRIDGE;
			this.accBridge = true;
		}
		if (flags >= ClassFile.ACC_SYNCHRONIZED) {
			flags -= ClassFile.ACC_SYNCHRONIZED;
			this.accSynchronized = true;
		}
		if (flags >= ClassFile.ACC_FINAL) {
			flags -= ClassFile.ACC_FINAL;
			this.accFinal = true;
		}
		if (flags >= ClassFile.ACC_STATIC) {
			flags -= ClassFile.ACC_STATIC;
			this.accStatic = true;
		}
		if (flags >= ClassFile.ACC_PROTECTED) {
			flags -= ClassFile.ACC_PROTECTED;
			this.accProtected = true;
		}
		if (flags >= ClassFile.ACC_PRIVATE) {
			flags -= ClassFile.ACC_PRIVATE;
			this.accPrivate = true;
		}
		if (flags >= ClassFile.ACC_PUBLIC) {
			flags -= ClassFile.ACC_PUBLIC;
			this.accPublic = true;
		}

		// Check the flags.
		if ((this.accPublic && (this.accPrivate | this.accProtected))
				|| (this.accPrivate && this.accProtected))
				throw new ClassFileException(
						"At most one of the flags ACC_PRIVATE, ACC_PROTECTED and ACC_PUBLIC is allowed for a method");
		if (this.accAbstract) {
			if (this.accFinal)
				throw new ClassFileException("An abstract method must not have its ACC_FINAL flag set.");
			if (this.accNative)
				throw new ClassFileException("An abstract method must not have its ACC_NATIVE flag set.");
			if (this.accPrivate)
				throw new ClassFileException("An abstract method must not have its ACC_PRIVATE flag set.");
			if (this.accStatic)
				throw new ClassFileException("An abstract method must not have its ACC_STATIC flag set.");
			if (this.accStrict)
				throw new ClassFileException("An abstract method must not have its ACC_STRICT flag set.");
			if (this.accSynchronized)
				throw new ClassFileException("An abstract method must not have its ACC_SYNCHRONIZED flag set.");
		}
		if (this.classFile.isAccInterface()) {
			if (!this.accPublic)
				throw new ClassFileException("An interface method must have its ACC_PUBLIC flag set.");
//			if (!this.accAbstract)
//				throw new ClassFileException("An interface method must have its ACC_ABSTRACT flag set.");
			if (this.accNative)
				throw new ClassFileException("An interface method must not have the ACC_NATIVE flag set.");
			if (this.accBridge)
				throw new ClassFileException("An interface method must not have the ACC_BRIDGE flag set.");
			if (this.accSynchronized)
				throw new ClassFileException("An interface method must not have the ACC_SYNCHRONIZED flag set.");
			if (this.accFinal)
				throw new ClassFileException("An interface method must not have the ACC_FINAL flag set.");
			if (this.accStatic)
				throw new ClassFileException("An interface method must not have the ACC_STATIC flag set.");
			if (this.accProtected)
				throw new ClassFileException("An interface method must not have the ACC_PROTECTED flag set.");
			if (this.accPrivate)
				throw new ClassFileException("An interface method must not have the ACC_PRIVATE flag set.");
		}
		if (getName().equals("<init>")) {
			if (this.accAbstract)
				throw new ClassFileException("An instance initialization method must not have the ACC_ABSTRACT flag set.");
			if (this.accNative)
				throw new ClassFileException("An instance initialization method must not have the ACC_NATIVE flag set.");
			if (this.accBridge)
				throw new ClassFileException("An instance initialization method must not have the ACC_BRIDGE flag set.");
			if (this.accSynchronized)
				throw new ClassFileException("An instance initialization method must not have the ACC_SYNCHRONIZED flag set.");
			if (this.accFinal)
				throw new ClassFileException("An instance initialization method must not have the ACC_FINAL flag set.");
			if (this.accStatic)
				throw new ClassFileException("An instance initialization method must not have the ACC_STATIC flag set.");
		}
	}

	/**
	 * Getter for the access flag "abstract".
	 *
	 * @return true, if the method is abstract, false otherwise.
	 */
	public boolean isAccAbstract() {
		return this.accAbstract;
	}

	/**
	 * Getter for the access flag "native".
	 * @return true, if the method is native, false otherwise.
	 */
	public boolean isAccNative() {
		return this.accNative;
	}

	/**
	 * Getter for the access flag "fpstrict".
	 * @return true, if the method is fpstrict, false otherwise.
	 */
	public boolean isAccStrict() {
		return this.accStrict;
	}

	/**
	 * Getter for the access flag "synchronized".
	 * @return true, if the field/method is synchronized, false otherwise.
	 */
	public boolean isAccSynchronized() {
		return this.accSynchronized;
	}

	/**
	 * Getter for the access flag "bridge".
	 * @return true, if the method is a bridge method, false otherwise.
	 */
	public boolean isAccBridge() {
		return this.accBridge;
	}

	/**
	 * Getter for the access flag "varargs".
	 * @return true, if the method may take a variable number of arguments, false otherwise.
	 */
	public boolean isAccVarargs() {
		return this.accVarargs;
	}

	/**
	 * Build and return a String representation of the access flags.<br>
	 * <br>
	 * Please note: The flags ACC_BRIDGE and ACC_VARARGS will not be included in the prefix, even if
	 * they are set.
	 *
	 * @return A String representation of the access flags.
	 */
	@Override
	public String getPrefix() {
		String prefix = super.getPrefix();
		if (this.accSynchronized) {
			prefix += "synchronized ";
		}
		if (this.accNative) {
			prefix += "native ";
		}
		if (this.accAbstract) {
			prefix += "abstract ";
		}
		if (this.accStrict) {
			prefix += "strictfp ";
		}

		return prefix;
	}

	/**
	 * Get a String representation of the return type of this method.
	 * @return A String representation of the return type.
	 */
	public String getReturnType() {
		return getLongTypeDescription(getDescriptor().substring(getDescriptor().indexOf(")") + 1));
	}

	/**
	 * Get a String representation of the full method name (including the class it is in) and its
	 * parameters. Parameters are given with names, if available.
	 *
	 * @return A String representation full method name with the parameter types and names.
	 */
	public String getFullNameWithParameterTypesAndNames() {
		return this.classFile.getName() + "." + getName() + "(" + getParameterTypesAndNames() + ")";
	}

	/**
	 * Get a String representation of the full method signature (excluding the class it is in) and its
	 * parameters. Parameters are given with names, if available.
	 *
	 * @return A String representation full method signature with the parameter types and names.
	 */
	public String getFullSignature() {
		return getPrefix() + getReturnType() + " " + getName() + "(" + getParameterTypesAndNames() + ")";
	}

	/**
	 * Getter for the instructions and other bytes. This array can be used for execution, since
	 * jumping is much easier in this form.
	 *
	 * @return The instructions as an array of Instruction objects. Objects being null represent
	 *         other bytes.
	 * @throws InvalidInstructionInitialisationException On any fatal problems with the parsing and
	 *         the initialization of instruction.
	 */
	public Instruction[] getInstructionsAndOtherBytes() throws InvalidInstructionInitialisationException {
		if (this.codeAttribute != null) {
			return this.codeAttribute.getInstructionsAndOtherBytes();
		}
		return null;
	}

	/**
	 * Getter for the instructions.
	 * @return The instructions as an array of Instruction objects.
	 * @throws InvalidInstructionInitialisationException Thrown when the parsing of the bytecode representation to instruction objects fails.
	 */
	public Instruction[] getInstructions() throws InvalidInstructionInitialisationException {
		if (this.codeAttribute != null)	{
			return this.codeAttribute.getInstructions();
		}
		return null;
	}

	/**
	 * Getter for the number of instructions.
	 * @return The number of instructions. Could be zero if this Method has no code at all.
	 */
	public int getInstructionsNumber() {
		if (this.codeAttribute != null) {
			return this.codeAttribute.getCodeLength();
		}
		return 0;
	}

	/**
	 * Replace the instruction at <code>index</code> with the specified QuickIsntruction. This is
	 * a low level method and will neither check whether replacing instructions is granted, nor if
	 * the specified instruction is a suitable replacement.
	 *
	 * @param quick The QuickInstruction
	 * @param index The index into the instructions.
	 * @throws IllegalArgumentException If the index into the instructions is not suitable.
	 * @throws NullPointerException If <code>quick</code> is null.
	 * @see AttributeCode#replaceInstruction(QuickInstruction, int)
	 */
	public void replaceInstruction(QuickInstruction quick, int index) {
		if (this.codeAttribute != null) {
			this.codeAttribute.replaceInstruction(quick, index);
		}
	}

	/**
	 * Reset replaced instruction to their original values (Instructions of type
	 * {@link ReplacingInstruction}.
	 *
	 * @see AttributeCode#resetReplacedInstructions()
	 */
	public void resetReplacedInstructions() {
		if (this.codeAttribute != null) {
			this.codeAttribute.resetReplacedInstructions();
		}
	}

	/**
	 * Unload priorly cached instructions if this method has any code.
	 *
	 * @see AttributeCode#unloadInstructions()
	 */
	public void unloadInstructions() {
		if (this.codeAttribute != null) {
			this.codeAttribute.unloadInstructions();
		}
	}

	/**
	 * Getter for the attribute_code.
	 * @return The AttributeCode object.
	 */
	public AttributeCode getCodeAttribute() {
		return this.codeAttribute;
	}

	/**
	 * Get the number of arguments. Overrides the super method, as the number is calculated anyway,
	 * thus saving execution time at this point.
	 *
	 * The number of arguments matches the number of parameters for static methods.
	 * It is one less for non-static methods.
	 *
	 * @return The number of arguments as a int.
	 */
	public int getNumberOfArguments() {
		return this.predefinedParameters.length;
	}

	/**
	 * Get the number of parameters. Overrides the super method, as the number is calculated anyway,
	 * thus saving execution time at this point.
	 *
	 * The number of parameters includes the first parameter (this) handed to non-static methods.
	 *
	 * @return The number of parameters as a int.
	 */
	public int getNumberOfParameters() {
		if (this.accStatic) return this.predefinedParameters.length;
		return this.predefinedParameters.length + 1;
	}

	/**
	 * Create an array of predefined parameters and initialize it with an instance of
	 * UndefinedValue. The array might be filled with objects prior to the execution of
	 * the method.
	 */
	private void createParametersArray() {
		String[] types = getParameterTypesAsArray();
		if (types.length == 0) {
			this.predefinedParameters = new Object[0];
			this.generatorProviders = new GeneratorProvider[0];
			this.arrayElementsGeneratorProviders = new ArrayElementsGeneratorProvider[0];
			return;
		}

		this.predefinedParameters = new Object[types.length];
		this.generatorProviders = new GeneratorProvider[types.length];
		this.arrayElementsGeneratorProviders = new ArrayElementsGeneratorProvider[types.length];
		this.takeASecondSlot = new boolean[types.length];
		UndefinedValue undefinedValue = new UndefinedValue();
		for (int a = 0; a < types.length; a++) {
			this.predefinedParameters[a] = undefinedValue;
			if (types[a].equals("double") || types[a].equals("long")) {
				this.takeASecondSlot[a] = true;
			} else {
				this.takeASecondSlot[a] = false;
			}
		}
	}

	/**
	 * Getter for the predefined parameters.
	 * @return The predefined parameters as an array of objects.
	 */
	public Object[] getPredefinedParameters() {
		return this.predefinedParameters;
	}

	/**
	 * Getter for the boolean that tells which predefined parameters take a second slot.
	 * @return The boolean that tells which predefined parameters take a second slot.
	 */
	public boolean[] getTakeASecondSlot() {
		return this.takeASecondSlot;
	}

	/**
	 * Setter for the predefined parameters.
	 * @param predefinedParameters The predefined parameters as an array of objects.
	 */
	public void setPredefinedParameters(Object[] predefinedParameters) {
		this.predefinedParameters = predefinedParameters;
	}

	/**
	 * Calculates the predefined Parameters that are not undefined (instance of UndefinedValue).
	 * @return The number of defined parameters as an int.
	 */
	public int getNumberOfDefinedPredefinedParameters() {
		int number = 0;
		for (int a = 0; a < this.predefinedParameters.length; a++) {
			if (!(this.predefinedParameters[a] instanceof UndefinedValue)) number++;
		}
		return number;
	}

	/**
	 * Return the String representation of the parameter type at the specified index.
	 * 
	 * @param index The index of the parameter. Please note that non-static methods have one
	 *        additional parameter (<i>this</i> at index 0).
	 * @return The String representation of the parameter type.
	 * @throws IndexOutOfBoundsException If index is negative or greater than the parameter count.
	 */
	public String getParameterTypeAtIndex(int index) {
		// If the method is not static, at local variable index 0 the reference to the class is saved.
		if (!isAccStatic()) index--;
		if (index < 0) throw new IndexOutOfBoundsException("Index is less than 0 for a static or less than 1 for a non-static method.");

		// Get the parameter type.
		String[] parameters = getParameterTypesAsArray();
		for (int a  = 0; a < parameters.length; a++) {
			if (a == index) return parameters[index];
			if (parameters[a].equals("double") || parameters[a].equals("long")) index--;
		}
		return "";
	}

	/**
	 * If the local variable at the specified index is expected to be Double,
	 * Float, Integer or Long, return such an object, initialized to 0. Otherwise
	 * a null reference will be returned.
	 * @param index The index of the local variable to fetch the type for. Please note that non-static methods have one additional parameter (<i>this</i> at index 0).
	 * @return An Number object with a value of 0, or a null reference.
	 */
	public Object getZeroOrNullParameter(int index) {
		// Get the type;
		String parameterType = getParameterTypeAtIndex(index);
		if (parameterType.equals("double") || parameterType.equals("java.lang.Doble")) return Double.valueOf(0D);
		if (parameterType.equals("float") || parameterType.equals("java.lang.Float")) return Float.valueOf(0F);
		if (parameterType.equals("int") || parameterType.equals("java.lang.Integer")
				|| parameterType.equals("byte") || parameterType.equals("java.lang.Byte")
				|| parameterType.equals("char") || parameterType.equals("java.lang.Character")
				|| parameterType.equals("short") || parameterType.equals("java.lang.Short")
				|| parameterType.equals("boolean") || parameterType.equals("java.lang.Boolean")) return Integer.valueOf(0);
		if (parameterType.equals("long") || parameterType.equals("java.lang.Long")) return Long.valueOf(0L);
		return null;
	}

	/**
	 * Get a String representation of the parameters of this method.
	 * @return A String representation of the parameters.
	 */
	public String getParameterTypes() {
		String typeDescription = getLongTypeDescription(getDescriptor().substring(1, getDescriptor().indexOf(")")));
		if (this.accVarargs) {
			// The last array has a variable number of arguments.
			int position = typeDescription.lastIndexOf("[]");
			typeDescription = typeDescription.substring(0, position) + "...";
		}
		return typeDescription;
	}

	/**
	 * Get a String representation of the parameters of this field/method.
	 * @return A String array representation of the parameters.
	 */
	public String[] getParameterTypesAsArray() {
		String longTypeDescription = getParameterTypes();
		if (longTypeDescription.length() == 0) return new String[0];
		return longTypeDescription.split(", ");
	}

	/**
	 * Get a String representation of the parameter types and names, if
	 * available.
	 * @return AString representation of the parameter types and names.
	 */
	public String getParameterTypesAndNames() {
		// Preparations
		String parametersString = getParameterTypes();

		String[] parameterNames = getParameterNames();
		if (parameterNames != null) {
			// Finally augment the type description with the parameter names.
			String[] parameterTypes = parametersString.split(", ");
			// Does the number match? Otherwise, do not proceed!
			int oneMore = 0;
			if (!this.accStatic) oneMore = 1;
			if (parameterTypes.length <= parameterNames.length - oneMore) {
				parametersString = "";
				int shift = 0;
				for (int a = 0; a < parameterTypes.length; a++) {
					if (a > 0) parametersString += ", ";
					if (!parameterTypes[a].equals("")) parametersString += parameterTypes[a] + " " + parameterNames[a + oneMore + shift];
					if (parameterTypes[a].equals("double") || parameterTypes[a].equals("long")) {
						shift++;
					}
				}
			}
		}
		// Return the finished String.
		return parametersString;
	}

	/**
	 * Get a String array of the parameter Names. If parameter names are not available, null will be returned.
	 * @return A String array of the parameter Names, or null.
	 */
	public String[] getParameterNames() {
		if (this.parameterNames != null) return this.parameterNames;

		// Only proceed if there is a code attribute!
		if (this.codeAttribute != null) {
			// Fetch the LocalVariableTable_attribute (if its exists).
			AttributeLocalVariableTable attributeLocalVariableTable = null;
			for (Attribute attribute : this.codeAttribute.getAttributes()) {
				if (attribute instanceof AttributeLocalVariableTable) {
					attributeLocalVariableTable = (AttributeLocalVariableTable) attribute;
					break;
				}
			}

			// Found it? Do not proceed otherwise.
			if (attributeLocalVariableTable != null) {
				this.parameterNames = new String[this.codeAttribute.getMaxLocals()];
				// Use it to generate the array of parameter names.
				int foundParameterNames = 0;
				for (LocalVariableTable localVariableTable : attributeLocalVariableTable.getLocalVariableTable())
				{
					int index = localVariableTable.getIndex();
					this.parameterNames[index] = ((ConstantUtf8) this.classFile.getConstantPool()[localVariableTable.getNameIndex()]).getStringValue();
					foundParameterNames++;
				}
			}
		}
		return this.parameterNames;
	}

	/**
	 * Check if parameter names are available.
	 * @return True, if parameter names are available, false otherwise.
	 */
	public boolean parameterNamesAvailable() {
		String[] parameterNames = getParameterNames();
		if (parameterNames == null) return false;
		return true;
	}

	/**
	 * Get the parameter name at the specified index. If parameter names are not available, a String
	 * representation of the index will be returned.
	 *
	 * If getting a String representation of the index is not desired, use parameterNamesAvailable()
	 * first. If it returns true, his method will return the appropriate parameter names.
	 *
	 * @param index The index of the parameter. Please note that non-static methods have one
	 *        additional parameter (<i>this</i> at index 0).
	 * @return The parameter name.
	 * @throws IndexOutOfBoundsException If index is negative or greater than the parameter count.
	 */
	public String getParameterName(int index) {
		if (index < 0) throw new IndexOutOfBoundsException("Index is less than 0.");
		String[] parameterNames = getParameterNames();
		if (parameterNames == null) return String.valueOf(index);
		if (parameterNames.length < index) throw new IndexOutOfBoundsException("The requested index is greater than the parameter count.");
		return parameterNames[index];
	}

	/**
	 * Get an array of the variables of this Method. Variables not yet used are null.
	 * @return An array of variables. Their type is either Variable for single variables, or Arrayref for arrays of then.
	 */
	public Variable[] getVariables() {
		if (this.variables == null)
			this.variables = new Variable[getNumberOfParameters()];
		return this.variables;
	}

	/**
	 * Get the Variable at the specified index. If it has not been defined yet, define it.
	 * @param index The index of the parameter. Please note that non-static methods have one additional parameter (<i>this</i> at index 0).
	 * @return The Variable at the specified index. Their type is either Variable for single variables, or Arrayref for arrays of then.
	 */
	public Variable getVariable(int index) {
		Variable[] variables = getVariables();
		if (variables == null) {
			return null;
		}
		if (index < 0) throw new IndexOutOfBoundsException("Index is less than 0.");
		if (this.variables.length < index) throw new IndexOutOfBoundsException("The requested index is greater than the parameter count.");
		return variables[index];
	}

	/**
	 * Set the Variable at the specified index to the value supplied.
	 * 
	 * @param index The index of the parameter. Please note that non-static methods have one additional parameter (<i>this</i> at index 0).
	 * @param variable The Variable for the specified index.
	 * @throws ArrayIndexOutOfBoundsException If index is out of the parameters bounds.
	 * @throws IllegalStateException If the method has no code. This should not happen under normal circumstances.
	 */
	public void setVariable(int index, Variable variable) {
		// Initialize the array, if needed.
		if (this.variables == null) {
			if (this.codeAttribute == null) {
				throw new IllegalStateException("The method has no code!");
			}
			this.variables = new Variable[getNumberOfParameters()];
		}
			
		// Set the new one.
		this.variables[index] = variable;
	}

	/**
	 * Getter for the generated values used as parameters for this method.
	 * @return The generated values.
	 */
	public Object[] getGeneratedValues() {
		if (this.generatedValues == null)
			this.generatedValues = new Object[getNumberOfParameters()];
		return this.generatedValues;
	}

	/**
	 * Set the generated value at the specified index to the value supplied.
	 * @param index The index of the parameter. Please note that non-static methods have one additional parameter (<i>this</i> at index 0).
	 * @param generatedObject The generated value for the specified index.
	 * @throws ArrayIndexOutOfBoundsException If index is out of the parameters bounds.
	 */
	public void setGeneratedValue(int index, Object generatedObject) {
			// Initialize the array, if needed.
			if (this.generatedValues == null)
				this.generatedValues = new Object[getNumberOfParameters()];

			// Set the new one.
			this.generatedValues[index] = generatedObject;
	}

	/**
	 * Return the control flow graph for this method.
	 *
	 * @return The control graph for this method.
	 * @throws InvalidInstructionInitialisationException On any fatal problems with the parsing and
	 *         the initialization of instructions.
	 */
	public ControlGraph getControlGraph()
		throws InvalidInstructionInitialisationException  {
		if (this.controlGraph == null) {
			this.controlGraph = new ControlGraph(this);
		}
//		
//		if(this.getName().equals("foo")) {
//			int index = 14;
//			Set<Integer> edges = controlGraph.getControlGraph().get(14);
//			edges.add(17);
//			controlGraph.getControlGraph().put(14, edges);
//		}
		
		return this.controlGraph;
	}

	/**
	 * Get the declared exceptions this method may throw. This will no include
	 * unchecked exceptions the method or any method it invokes may throw.
	 * @return The declared exception types as an array of String values.
	 */
	public String[] getDeclaredExceptions() {
		// Check if there is an Exceptions attribute.
		AttributeExceptions attributeExceptions = null;
		for (int a = 0; a < this.attributes.length; a++) {
			if (this.attributes[a] instanceof AttributeExceptions) {
				attributeExceptions = (AttributeExceptions) this.attributes[a];
				break;
			}
		}

		// Found the attribute?
		if (attributeExceptions == null) {
			// Return an empty String array.
			return new String[0];
		}

		// Read the exception types.
		int[] exceptionIndexTable = attributeExceptions.getExceptionIndexTable();
		String[] exceptionTypes = new String[exceptionIndexTable.length];
		Constant[] constantPool =  this.getClassFile().getConstantPool();
		for (int a = 0; a < exceptionIndexTable.length; a++) {
			exceptionTypes[a] = constantPool[exceptionIndexTable[a]].toString();
		}

		// Return.
		return exceptionTypes;
	}

	/**
	 * Get both the declared exceptions this method may throw and the super
	 * classes for any undeclared exceptions it may throw. These are
	 * java.lang.RuntimeException and java.lang.Error. You can be sure that
	 * any exceptions thrown by this method or by a method it invokes are
	 * either of one of the types returned by this method, or are subclasses
	 * of them.
	 * @return All exception types as an array of String values.
	 */
	public String[] getAllExceptions() {
		String[] exceptions = getDeclaredExceptions();
		String[] exceptionsNew = new String[exceptions.length + 2];
		System.arraycopy(exceptions, 0, exceptionsNew, 0, exceptions.length);
		exceptionsNew[exceptionsNew.length - 2] = "java.lang.RuntimeException";
		exceptionsNew[exceptionsNew.length - 1] = "java.lang.Error";
		return exceptionsNew;
	}

	/**
	 * Get the generator provider at the specified parameter index.
	 *
	 * @param index
	 *            The index of the parameter to get a generator provider for. Please note that the index
	 *            starts at 0, the extra index non-static methods have is not counted.
	 * @return The Generator, or null, should no generator provider have been set.
	 * @throws ArrayIndexOutOfBoundsException If index is not within the parameter count.
	 */
	public GeneratorProvider getGeneratorProvider(int index) {
		return this.generatorProviders[index];
	}

	/**
	 * Set the generator provider at the specified parameter index.
	 *
	 * @param index
	 *            The index of the parameter to get a generator provider for. Please note that the index
	 *            starts at 0, the extra index non-static methods have is not counted.
	 * @param generator The GeneratorProvider, or null if the generator provider is to be removed.
	 * @throws ArrayIndexOutOfBoundsException If index is not within the parameter count.
	 */
	public void setGeneratorProvider(int index, GeneratorProvider generator) {
		this.generatorProviders[index] = generator;
	}

	/**
	 * Get the number of generators set for this method.
	 * @return The number of generators.
	 */
	public int getGeneratorCount() {
		int generatorCount = 0;
		for (int a = 0; a < this.generatorProviders.length; a++) {
			if (this.generatorProviders[a] != null)
				generatorCount++;
		}
		return generatorCount;
	}

	/**
	 * Get the array element generator provider at the specified parameter index.
	 * @param index
	 * 			  The index of the parameter to get a array element generator provider for. Please note that the index
	 *            starts at 0, the extra index non-static methods have is not counted.
	 * @return The ArrayElementsGeneratorProvider, or null, should no array element generator have been set.
	 * @throws ArrayIndexOutOfBoundsException If index is not within the parameter count.
	 */
	public ArrayElementsGeneratorProvider getArrayElementsGeneratorProvider(int index) {
		return this.arrayElementsGeneratorProviders[index];
	}

	/**
	 * Set the array element generator provider at the specified parameter index.
	 *
	 * @param index
	 * 			  The index of the parameter to get a array element generator provider for. Please note that the index
	 *            starts at 0, the extra index non-static methods have is not counted.
	 * @param generatorProvider The ArrayElementsGeneratorProvider, or null if the generator is to be removed.
	 * @throws ArrayIndexOutOfBoundsException If index is not within the parameter count.
	 */
	public void setArrayElementsGeneratorProvider(int index, ArrayElementsGeneratorProvider generatorProvider) {
		this.arrayElementsGeneratorProviders[index] = generatorProvider;
	}

	/**
	 * Get the number of array element generators set for this method.
	 * @return The number of array element generators.
	 */
	public int getArrayElementGeneratorCount() {
		int generatorCount = 0;
		for (int a = 0; a < this.arrayElementsGeneratorProviders.length; a++) {
			if (this.arrayElementsGeneratorProviders[a] != null)
				generatorCount++;
		}
		return generatorCount;
	}

	/**
	 * Checks whether an object and this Method are equal. This is only the case if the object is of
	 * type Method, both belong to the same ClassFile (one class may be loaded into more than one
	 * ClassFile is class loaders are used incorrectly), and both name index and descriptor index
	 * are equal.
	 * 
	 * @param obj The Object to compare this Method one to.
	 * @return true if the specified object is a Method which is equal to this one; false otherwise.
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Method) {
			Method method = (Method) obj;
			if (this.classFile.equals(method.classFile) && this.nameIndex == method.nameIndex
					&& this.descriptorIndex == method.descriptorIndex) return true;
		}
		return false;
	}

	/**
	 * Returns a hash code value for the Method. It is computed from the hash code of the class file
	 * and the hash code of the String representation of the method's name.
	 * 
	 * @return a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (!this.hashComputed) {
			this.hashCode = this.classFile.hashCode() + getName().hashCode();
			this.hashComputed = true;
		}
		return this.hashCode;
	}

	/**
	 * Get the parameter index corresponding to a variable index. It might be lower if there are
	 * parameters of type double or long.
	 * 
	 * @param index The local variable index.
	 * @return The parameter index.
	 */
	public int getParameterIndexForLocalVariableIndex(int index) {
		return getParameterIndexForLocalVariableIndex(index, false);
	}
	
	/**
	 * Get the parameter index corresponding to a variable index. It might be lower if there are
	 * parameters of type double or long.
	 * 
	 * @param index The local variable index.
	 * @param skipThis If set to true, it will be ignored whether this method is static or not.
	 * @return The parameter index.
	 */
	public int getParameterIndexForLocalVariableIndex(int index, boolean skipThis) {
		if (!this.accStatic) index--; // TODO: is this correct? Probably not
		String[] types = getParameterTypesAsArray();
		for (int a = 0; a < index; a++) {
			if (types[a].equals("double") || types[a].equals("long")) {
				index--;
			}
		}
		if (!this.accStatic) index++;
		if (!skipThis && !this.accStatic) index--;
		return index;
	}
	
	/**
	 * Return aString representation of this method.
	 *
	 * @return A String representation of this method.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getFullName();
	}


}
