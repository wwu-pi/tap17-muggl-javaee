package de.wwu.muggl.vm.exceptions;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ExceptionTable;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantClass;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.loading.MugglClassLoader;
import de.wwu.muggl.solvers.expressions.IntConstant;

/**
 * The exception handler is initialized with a thrown runtime exception. After doing so, its' only
 * public method - handleException() - can be called. It tries to resolve the exception as described
 * in chapter 2.16.2 of the JavaVirtual Machine Specification (2nd ed.)
 * (http://java.sun.com/docs/books/jvms/second_edition/html/Concepts.doc.html#22746).<br />
 * <br />
 * On success, the virtual machine will be refreshed accordingly. This means, the operand stack will
 * be cleared, the pc set to the handlers' position and the objectref pushed onto the operand stack.
 * If the throwing frame does not handle the exception, the handling frame is found and pushed onto
 * the virtual machine stack. The virtual machine is then informed that it has to continue execution
 * with this frame.<br />
 * <br />
 * Being unable to handle the exception, an virtual machine internal
 * NoExceptionHandlerFoundException will be thrown, containing the wrapped root exception.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-16
 */
public class ExceptionHandler {
	// Basic fields.
	/**
	 * The frame the exception was thrown by.
	 */
	protected Frame		frame;
	private Objectref	objectref;
	private String		classname;
	private boolean		handlingFinished;

	// Fields to store exception source data.
	private Method		method;
	private int			pc;

	/**
	 * Constructor for catching exceptions to occur during the processing of bytecode instructions.
	 * These are thrown and "transported" within an internal VmRuntimeException. This exception
	 * hints to the virtual machine, that an exception was thrown which should be handled by the
	 * application executed. The wrapped exception is therefore unpacked.
	 * 
	 * @param frame The frame the exception was thrown by.
	 * @param vmRuntimeException The wrapper for the thrown exception.
	 */
	public ExceptionHandler(Frame frame, VmRuntimeException vmRuntimeException) {
		this.frame = frame;
		this.objectref = vmRuntimeException.getWrappedException();
		this.classname = vmRuntimeException.getWrappedException().getInitializedClass().getClassFile().getName();
		this.handlingFinished = false;
		this.method = frame.getMethod();
		this.pc = frame.getVm().getPc();
	}

	/**
	 * Constructor that suits the need of the athrow instruction. Why unpack and initialize a class
	 * as a throwable prior to invoking the exception handler? This constructor takes a object
	 * reference directly and will process it gracefully.
	 * 
	 * @param frame The frame the exception was thrown by.
	 * @param objectref The thrown object reference.
	 */
	public ExceptionHandler(Frame frame, Objectref objectref) {
		this.frame = frame;
		this.objectref = objectref;
		this.classname = objectref.getInitializedClass().getClassFile().getName();
		this.handlingFinished = false;
		this.method = frame.getMethod();
		this.pc = frame.getVm().getPc();
	}

	/**
	 * Handle the exception, if it has not already been handled. Invoking this method will
	 * definitely interrupt the program flow. Either the execution will be transfered to the
	 * appropriate handler, or the virtual machine will halt.
	 * 
	 * @throws ExecutionException If no handler could be found ultimately and a Throwable has to be
	 *         instantiated but that process fails; also thrown on class loading errors during the
	 *         evaluation of suitable handlers.
	 * @throws NoExceptionHandlerFoundException If no handler could be found ultimately.
	 */
	public void handleException() throws ExecutionException, NoExceptionHandlerFoundException {
		if (this.handlingFinished) return;
		int currentLine = this.frame.getPc();//this.frame.getVm().getPc();
		ExceptionTable[] exceptionTable = this.frame.getMethod().getCodeAttribute()
				.getExceptionTable();
		// Is there an exception table at all?
		if (exceptionTable != null) {
			for (int a = 0; a < exceptionTable.length; a++) {
				/*
				 * The line at which the exception is thrown has to be between the handler start
				 * line and its end line (not included!). The handler is either zero (finally) or
				 * the exception type has to be matched.
				 */
				if (currentLine >= exceptionTable[a].getStartPc()
						&& currentLine < exceptionTable[a].getEndPc()
						&& (exceptionTable[a].getCatchType() == 0 || checkForExceptionMatch(
								this.classname, this.frame.getConstantPool()[exceptionTable[a]
										.getCatchType()].toString().replace("/", "."), this.frame
										.getVm().getClassLoader()))) {
					// Handling successful!
					this.frame.getOperandStack().clear();
					this.frame.getVm().setPC(exceptionTable[a].getHandlerPc());
					this.frame.setPc(exceptionTable[a].getHandlerPc());
					this.frame.getOperandStack().push(this.objectref);
					this.handlingFinished = true;
					return;
				}
			}
		}

		// If this point is reached, handling in the current Frame failed.
		noHandlerFound();
		this.handlingFinished = true;
	}

	/**
	 * If no handle is found in the currently process Frame, the next Frame is popped from the
	 * virtual machine stack. If the stack is empty, handling failes. Otherwise the popped Frame is
	 * taken as the virtual machines current Frame and the exception handler is searched in its'
	 * exception_table. If there is no handler found, this methods is called recursively until
	 * either a handler is found, or the stack is emptied and execution fails.
	 * 
	 * @throws ExecutionException If no handler could be found ultimatively and a Throwable has to
	 *         be instantiated but that process fails.
	 * @throws NoExceptionHandlerFoundException If no handler could be found ultimatively.
	 */
	protected void noHandlerFound() throws ExecutionException, NoExceptionHandlerFoundException {
		// Is there a invoking frame?
		if (this.frame.getVm().getStack().isEmpty()) {
			// Fatal abortion of the current execution.
			abortExecution();
		} else {
			Frame frame = null;
			Object object = this.frame.getVm().getStack().pop();
			// Is the object a frame? It has to be!
			if (!(object instanceof Frame)) {
				abortExecution();
			} else {
				frame = (Frame) object;
			}

			// Set this frame as the current frame.
			this.frame.getVm().changeCurrentFrame(frame);
			this.frame.getVm().setPC(this.frame.getVm().getCurrentFrame().getPc());

			// The execution of the old frame must not be continued!
			this.frame.getVm().setReturnFromCurrentExecution(true);
			this.frame.getVm().enableNextFrameIsAlreadyLoaded();

			// Refresh the frame at this point, too.
			this.frame = frame;

			// Report that a frame has been restored.
			frameHasBeenRestored();

			// Recursion: Handle the exception in this Frame!
			handleException();
		}
	}

	/**
	 * Abort the execution by throwing an NoExceptionHandlerFoundException.
	 * 
	 * @throws ExecutionException If a Throwable has to be instantiated but that process fails.
	 * @throws NoExceptionHandlerFoundException On every invocation of this method.
	 * @throws ClassFileException If java.lang.String cannot be resolved.
	 */
	private void abortExecution() throws ExecutionException, NoExceptionHandlerFoundException {
		// Generate a message for logging.
		if (Globals.getInst().execLogger.isDebugEnabled()) {
			try {
				ClassFile throwableClassFile = this.objectref.getInitializedClass().getClassFile();
				Field detailMessageField = throwableClassFile.getFieldByName("detailMessage", true);
				ClassFile stringClassFile = this.frame.getVm().getClassLoader()
						.getClassAsClassFile("java.lang.String");
				Field stringValueField = stringClassFile.getFieldByNameAndDescriptor("value", "[C");
				Objectref stringObjectref = (Objectref) this.objectref.getField(detailMessageField);
				String message;
				if (stringObjectref == null) {
					message = "null";
				} else {
					Arrayref arrayref = (Arrayref) stringObjectref.getField(stringValueField);
	
					// Convert it.
					boolean symbolicalMode = Options.getInst().symbolicMode;
					char[] characters = new char[arrayref.length];
					for (int a = 0; a < arrayref.length; a++) {
						if (symbolicalMode) {
							characters[a] = (char) ((IntConstant) arrayref.getElement(a)).getIntValue();
						} else {
							characters[a] = (Character) arrayref.getElement(a);
						}
					}
					message = new String(characters);
				}

				String logMessage = "The executed application threw an exception but no suitable "
						+ "exception handler was found. Uncaught exception: "
						+ this.classname.replace("/", ".")
						+ " (" + message + ")";
				Globals.getInst().execLogger.debug(logMessage);

			} catch (ClassFileException e) {
				throw new ExecutionException(
						"Could not fetch class java.lang.String. This hints to serious problems.");
			}
		}

		// Throw the NoExceptionHandlerFoundException;
		throw new NoExceptionHandlerFoundException(this.objectref, this.method, this.pc);
	}

	/**
	 * Take care of a frame restored while handling an exception. This method is empty but may be
	 * overridden by inheriting implementations. It is called after the field for the frame has been
	 * set to the restored frame.
	 */
	protected void frameHasBeenRestored() { }

	/**
	 * Check if the thrown exception matched the exception handled by the current entry of the
	 * exception_table. This process is not as easy as it sounds, since the handler might be a
	 * superclass, thus handling the exception. So if there is no direct match, the handled
	 * exceptions' class is loaded. If it has a superclass, this is loaded. Otherwise checking
	 * fails. The superclass is then compared to the entry in the exception_table. If there still is
	 * no match, checking continued with the superclass of the superclass of the handled exception.
	 * This continued recursively, until java.lang.Throwable is reached. If it still does not match,
	 * checking eventually fails.
	 * 
	 * @param handledExceptionName The full name of the exception that was thrown and is to be
	 *        handled.
	 * @param catchingExceptionName The full name of the handler to check against, reflecting an
	 *        entry in the exception_table.
	 * @param classLoader The currently active class loader.
	 * @return true, if the handler is suitable, false otherwise.
	 * @throws ExecutionException Thrown on fatal class loading problems, e.g. a missing superclass.
	 */
	public static boolean checkForExceptionMatch(String handledExceptionName,
			String catchingExceptionName, MugglClassLoader classLoader) throws ExecutionException {
		catchingExceptionName = catchingExceptionName.replace("/", "."); // TODO this needs fixing at some points. This conversion should not be necessary.
		handledExceptionName = handledExceptionName.replace("/", "."); // TODO this needs fixing at some points. This conversion should not be necessary.
		
		if((catchingExceptionName.equals("java.lang.Exception") && handledExceptionName.equals("javax.persistence.TransactionRequiredException"))
			|| (catchingExceptionName.equals("javax.persistence.TransactionRequiredException") && handledExceptionName.equals("java.lang.Exception"))) {
			System.out.println("Check this...");
		}
		
		// First possibility: Direct match.
		if (handledExceptionName.equals(catchingExceptionName)) return true;
		
		try {
			ClassFile handledExceptionClassFile = classLoader
					.getClassAsClassFile(handledExceptionName);

			// Second possibility: The catching exception is a super class of the handler exception.
			if (handledExceptionClassFile.getSuperClass() != 0) {
				String superClass = ((ConstantClass) handledExceptionClassFile.getConstantPool()[handledExceptionClassFile
						.getSuperClass()]).getStringValue();
				// Search recursively.
				return checkForExceptionMatch(superClass, catchingExceptionName, classLoader);
			}
		} catch (ClassFileException e) {
			String message = "Error while checking an exception for matching classes. A required class file could not be loaded. The root exception is a ClassFileException with the message: "
					+ e.getMessage();
			if (Globals.getInst().execLogger.isDebugEnabled())
				Globals.getInst().execLogger.debug(message);
			throw new ExecutionException(message);
		} catch (ExecutionException e) {
			String message = "Error while checking an exception for matching classes. The root exception is a ExecutionException with the message: "
					+ e.getMessage();
			if (Globals.getInst().execLogger.isDebugEnabled())
				Globals.getInst().execLogger.debug(message);
			throw new ExecutionException(message);
		}

		// No match found!
		return false;
	}

}
