package de.wwu.muggl.vm.impl.logic;

import de.wwu.muggl.vm.Application;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.InitializationException;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericVariable;

/**
 * This concrete class represents a virtual machine for the logic execution of java bytecode. It
 * inherits functionality from the symbolic virtual machine but overrides some of it in order to
 * prevent undesired effects such as the generation of test cases.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-15
 */
public class LogicVirtualMachine extends SymbolicVirtualMachine {

	/**
	 * Basic constructor, which initializes the additional fields.
	 *
	 * @param application The application this virtual machine is used by.
	 * @param classLoader The main ClassLoader to use.
	 * @param classFile The classFile to start execution with.
	 * @param initialMethod The Method to start execution with. This Method has to be a method of
	 *        the supplied classFile.
	 * @throws InitializationException If initialization of auxiliary classes fails.
	 */
	public LogicVirtualMachine(Application application, MugglClassLoader classLoader,
			ClassFile classFile, Method initialMethod) throws InitializationException {
		super(application, classLoader, classFile, initialMethod);
	}

	/**
	 * Create a new frame and check whether its local variables should be initialized to logic
	 * variables.
	 * 
	 * @param invokedBy The frame this frame is invoked by. Might by null.
	 * @param method The Method that this frame holds.
	 * @param arguments The arguments that will be stored in the local variables prior to execution.
	 * @return The new frame.
	 * @throws ExecutionException On any fatal error that happens during execution and is not coped
	 *         by one of the other Exceptions.
	 */
	@Override
	protected Frame createFrame(Frame invokedBy, Method method, Object[] arguments) throws ExecutionException {
		// Get a fresh frame.
		Frame frame = super.createFrame(invokedBy, method, arguments);

		/*
		 * Check which local variables are annotated and replace undefined local variables by logic
		 * variables.
		 */
		// TODO: Impossible in Java 6 since annotations of local variables are not visible.
		
		// Return it.
		return frame;
	}

	/**
	 * Generate an instance of Objectref for the specified ClassFile and check whether its fields
	 * should be initialized to logic variables.
	 * 
	 * @param classFile The class file to get an object reference for.
	 * @return A new instance of objectref for this ClassFile.
	 * @throws ExceptionInInitializerError If class initialization fails.
	 */
	@Override
	public Objectref getAnObjectref(ClassFile classFile) {
		// Get and check the initialized class.
		InitializedClass initializedClass = classFile.getInitializedClass();
		if (initializedClass == null) {
			initializedClass = new InitializedClass(classFile, this);
		}
		
		// Get the object reference.
		Objectref objectref = initializedClass.getANewInstance();

		/*
		 * Check which fields are annotated and replace undefined fields by logic variables.
		 */
		Constant[] constantPool = classFile.getConstantPool();
		for (Field field : classFile.getFields()) {
			for (Attribute attribute : field.getAttributes()) {
				if (attribute.getStructureName().equals("attribute_runtime_visible_annotation")) {
					AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
					if (constantPool[attributeAnnotation.getAnnotations()[0].getTypeIndex()].getStringValue()
							.equals("Lde/wwu/logic/annotation/LogicVariable;")) {
						if (!objectref.hasValueFor(field)) {
							String typeString = field.getType();
							byte type;
							if (typeString.equals("char") || typeString.equals("java.lang.Character")) {
								type = Expression.CHAR;
							} else if (typeString.equals("boolean") || typeString.equals("java.lang.Boolean")) {
								type = Expression.BOOLEAN;
							} else if (typeString.equals("byte") || typeString.equals("java.lang.Byte")) {
								type = Expression.BYTE;
							} else if (typeString.equals("double") || typeString.equals("java.lang.Double")) {
								type = Expression.DOUBLE;
							} else if (typeString.equals("int") || typeString.equals("java.lang.Integer")) {
								type = Expression.INT;
							} else if (typeString.equals("float") || typeString.equals("java.lang.Float")) {
								type = Expression.FLOAT;
							} else if (typeString.equals("long") || typeString.equals("java.lang.Long")) {
								type = Expression.LONG;
							} else { // Only type "short" is left.
								type = Expression.SHORT;
							}
							objectref.putField(field, new NumericVariable(field.getName(), type, false));
						 }
					 }
				}
			}
		}
		
		// Return the object reference.
		return objectref;
	}
	
}
