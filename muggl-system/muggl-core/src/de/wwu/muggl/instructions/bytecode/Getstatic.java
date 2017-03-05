package de.wwu.muggl.instructions.bytecode;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.general.Get;
import de.wwu.muggl.instructions.interfaces.Instruction;
import de.wwu.muggl.jpa.criteria.metamodel.SymbolicEntityAttribute;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.exceptions.ExceptionHandler;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.InitializedClass;

/**
 * Implementation of the instruction <code>getstatic</code>.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class Getstatic extends Get implements Instruction {

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Getstatic(AttributeCode code) throws InvalidInstructionInitialisationException {
		super(code);
	}

	/**
	 * Execute the instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		try {
			// Fetch and push the value.
			frame.getOperandStack().push(getFieldValue(frame));
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
	 * Execute the instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException,
			SymbolicExecutionException {
		try {
			ClassFile methodClassFile = frame.getMethod().getClassFile();
			Field field = getField(frame, methodClassFile, methodClassFile.getClassLoader());
			if(field.getType().equals("java.util.logging.Level")) {
				frame.getOperandStack().push(new Object());
				return;
			}
			
			// Get the fields' value.
			Object value = getFieldValue(frame);

			// Push it.
			frame.getOperandStack().push(value);
		} catch (VmRuntimeException e) {
			SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
			try {
				handler.handleException();
			} catch (ExecutionException e2) {
				executionFailedSymbolically(e2);
			}
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		} catch (ExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Get the instance field and push its value onto the stack.
	 *
	 * @param frame The currently executed frame.
	 * @return The fields' value.
	 * @throws ExecutionException In case of fatal problems during the execution.
	 * @throws VmRuntimeException If the Field could not be found, wrapping a NoSuchFieldError.r.
	 */
	private Object getFieldValue(Frame frame) throws ExecutionException, VmRuntimeException {
		// Preparations.
		ClassFile methodClassFile = frame.getMethod().getClassFile();
		Field field = getField(frame, methodClassFile, methodClassFile.getClassLoader());

		// Runtime exception: is the field not static?
		if (!field.isAccStatic())
			throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IncompatibleClassChangeError",
					"The field accessed by instruction " + getName() + " must not be static."));

		// Get the value of the field.
		ClassFile fieldClassFile = field.getClassFile();
		InitializedClass initClass = fieldClassFile.getTheInitializedClass(frame.getVm());

		if(initClass.getField(field) == null) {
			// check if the given class is of type _StaticMetamodel_
			for(Attribute attribute : fieldClassFile.getAttributes()) {
				if(attribute.getStructureName().equals("attribute_runtime_visible_annotation")) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					for(Annotation annotation : attributeAnnotation.getAnnotations()) {
						String annoName = fieldClassFile.getConstantPool()[annotation.getTypeIndex()].getStringValue();
						if(annoName.equals("Ljavax/persistence/metamodel/StaticMetamodel;")) {
							SymbolicEntityAttribute symEntityAttribute = new SymbolicEntityAttribute(fieldClassFile.getName().substring(0, fieldClassFile.getName().length()-1), field, frame.getVm());
							initClass.putField(field, symEntityAttribute);
						}
					}
				}
			}
		}
		
		// Return the value.
		return initClass.getField(field);
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		return "getstatic";
	}

	/**
	 * Get the thrown exception types as fully qualified java names.
	 * @return The thrown exception types.
	 */
	public String[] getThrownExceptionTypes() {
		String[] exceptionTypes = { "java.lang.ExceptionInInitializerError",
									"java.lang.IllegalAccessError",
									"java.lang.IncompatibleClassChangeError",
									"java.lang.NoClassDefFoundError",
									"java.lang.NoSuchFieldError",
									"java.lang.NullPointerException"};
		return exceptionTypes;
	}

}
