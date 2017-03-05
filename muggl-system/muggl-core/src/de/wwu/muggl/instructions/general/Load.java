package de.wwu.muggl.instructions.general;

import de.wwu.muggl.instructions.InvalidInstructionInitialisationException;
import de.wwu.muggl.instructions.interfaces.LocalVariableAccess;
import de.wwu.muggl.instructions.interfaces.control.JumpNever;
import de.wwu.muggl.instructions.interfaces.data.StackPush;
import de.wwu.muggl.instructions.interfaces.data.VariableLoading;
import de.wwu.muggl.instructions.typed.TypedInstruction;
import de.wwu.muggl.symbolic.generating.Generator;
import de.wwu.muggl.symbolic.generating.GeneratorProvider;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeCode;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.execution.ConversionException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.execution.MugglToJavaConversion;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.ModifieableArrayref;
import de.wwu.muggl.solvers.expressions.Variable;

/**
 * Abstract instruction with some concrete methods for instructions that load elements
 * from the local variables. Concrete instructions can be extended from this class.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-29
 */
public abstract class Load extends GeneralInstructionWithOtherBytes implements LocalVariableAccess,
		JumpNever, VariableLoading, StackPush {
	private static final int MAX_INDEX = 255;
	
	/**
	 * The index of the variable to load.
	 */
	protected int index;
	/**
	 * The number of other bytes of this instruction.
	 */
	protected int numberOfOtherBytes;
	/**
	 * The type the inheriting class will take.
	 */
	protected TypedInstruction typedInstruction;

	/**
	 * Standard constructor. For the extraction of the other bytes, the attribute_code of the method that the
	 * instruction belongs to is supplied as an argument, as well as the index of the instruction, pointing at
	 * the local variable that it is responsible for.
	 * @param code The attribute_code of the method that the instruction belongs to.
	 * @param index The index to load from. Must be a value between 0 and 3 inclusively. Otherwise, the index is set to 255 and methods are required to calculate if from the additional bytes.
	 * @throws InvalidInstructionInitialisationException If the instruction could not be initialized successfully, most likely due to missing additional bytes. This might be caused by a corrupt classfile, or a classfile of a more recent version than what can be handled.
	 */
	public Load(AttributeCode code, int index) throws InvalidInstructionInitialisationException {
		super(code, index, 3, 1);
		if (index < 0 || index > 3) {
			this.index = MAX_INDEX;
			this.numberOfOtherBytes = 1;
		} else {
			this.index = index;
			this.numberOfOtherBytes = 0;
		}
	}

	/**
	 * Execute the inheriting instruction.
	 * @param frame The currently executed frame.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	@Override
	public void execute(Frame frame) throws ExecutionException {
		int localVariable = getLocalVariableIndex();
		execute(frame, localVariable);
	}

	/**
	 * For widened index: execute the inheriting instruction.
	 *
	 * @param frame The currently executed frame.
	 * @param localVariable The local variable index to be loaded from.
	 * @throws ExecutionException Thrown in case of fatal problems during the execution.
	 */
	public void execute(Frame frame, int localVariable) throws ExecutionException {
		try {
			Object[] localVariables = frame.getLocalVariables();

			// Unexpected exception: The local variable is undefined.
			if (localVariables[localVariable] != null && localVariables[localVariable].getClass().getName().endsWith("UndefinedValue"))
				throw new ExecutionException("Could not load local variable #" + localVariable + ": Expected " + this.typedInstruction.getDesiredType() + ", got an undefined value indicator. You should predefine this value.");

			// Unexpected exception: The type check failed.
			if (localVariables[localVariable] != null && !this.typedInstruction.checkDesiredType(localVariables[localVariable].getClass().getName()))
				throw new ExecutionException("Could not load local variable #" + localVariable + ": Expected " + this.typedInstruction.getDesiredType() + ", got " + localVariables[localVariable].getClass() + ".");

			frame.getOperandStack().push(localVariables[localVariable]);
		} catch (ExecutionException e) {
			executionFailed(e);
		}
	}

	/**
	 * Execute the inheriting instruction symbolically.
	 * @param frame The currently executed frame.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	@Override
	public void executeSymbolically(Frame frame) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		int localVariable = getLocalVariableIndex();
		executeSymbolically(frame, localVariable);
	}

	/**
	 * For widened index: execute the inheriting instruction symbolically.
	 *
	 * @param frame The currently executed frame.
	 * @param localVariable The local variable index to be loaded from.
	 * @throws NoExceptionHandlerFoundException If no handler could be found.
	 * @throws SymbolicExecutionException Thrown in case of fatal problems during the symbolic execution.
	 */
	public void executeSymbolically(Frame frame, int localVariable) throws NoExceptionHandlerFoundException, SymbolicExecutionException {
		try {
			Object[] localVariables = frame.getLocalVariables();

			// Is the local variable defined, yet?
			if (localVariables[localVariable] != null && localVariables[localVariable].getClass().getName().endsWith("UndefinedValue")) {
				// First check if we got a GeneratorProvider for it.
				Method method = frame.getMethod();

				GeneratorProvider generatorProvider = method.getGeneratorProvider(method
						.getParameterIndexForLocalVariableIndex(localVariable));
				if (generatorProvider != null) {
					try {
						// Get the Generator.
						Generator generator = generatorProvider.provideInstance(
								method.getParameterName(localVariable));

						// Has a Generator been provided?
						if (generator == null)
							throw new SymbolicExecutionException("A generator provider did not provide a Generator.");

						// Check if the generator allows a GeneratorChoicePoint to be created.
						if (generator.allowsChoicePoint()) {
							// Generate a GeneratorChoicePoint.
							((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this, generator, null);
						} else {
							// Simply provide a value.
							if (generator.hasAnotherObject())  {
								Object object = generator.provideObject();
								if (generator.objectNeedsConversion()) {
									MugglToJavaConversion conversion = new MugglToJavaConversion(frame.getVm());
									object = conversion.toMuggl(object, false);
								}
								frame.setLocalVariable(localVariable, object);
								method.setGeneratedValue(localVariable, object);
							} else {
								throw new SymbolicExecutionException(
										"A variable generator was provided that does not even supply one object.");
							}
						}
					} catch (ConversionException e) {
						throw new SymbolicExecutionException(
								"An object provided by a generator required conversion to Muggl, but conversion failed: "
								+ e.getClass().getName() + " (" + e.getMessage() + ")");
					} catch (Throwable t) { // Catching anything the generator provider may throw.
						throw new SymbolicExecutionException(
								"A generator provider did not provide a Generator. It instead threw a Throwable: "
								+ t.getClass().getName() + " (" + t.getMessage() + ")");
					}
				} else {
					// Check if it is an array type.
					String type = frame.getMethod().getParameterTypeAtIndex(localVariable);
					if (type.endsWith("[]")) {
						// Check if it is a multidimensional array.
						if (type.endsWith("[][]")) {
							throw new SymbolicExecutionException("Multidimensional arrays are not yet supported in symbolic execution mode.");
						}

						// Generate an ArrayInitializationChoicePoint.
						((SymbolicVirtualMachine) frame.getVm()).generateNewChoicePoint(this, null, null);
						
						if (localVariables[localVariable] == null) {
							frame.getMethod().setGeneratedValue(localVariable, null);
						} else {
							frame.getMethod().setGeneratedValue(localVariable, ((ModifieableArrayref)localVariables[localVariable]).clone());
						}
					} else {
						// Create and push an non array type.
						Variable variable = this.typedInstruction.getNewVariable(frame.getMethod(), localVariable, frame.getVm());
						frame.setLocalVariable(localVariable, variable);
						frame.getMethod().setVariable(localVariable, variable);
					}
				}
			}

			// Fetch the object.
			Object object = localVariables[localVariable];

			// Push it onto the stack.
			frame.getOperandStack().push(object);
		} catch (SymbolicExecutionException e) {
			executionFailedSymbolically(e);
		}
	}

	/**
	 * Get the number of other bytes for this instruction.
	 * @return The number of other bytes.
	 */
	@Override
	public int getNumberOfOtherBytes() {
		return this.numberOfOtherBytes;
	}

	/**
	 * Resolve the instructions name.
	 * @return The instructions name as a String.
	 */
	@Override
	public String getName() {
		if (this.index <= 3) {
			return "load_" + this.index;
		}
		return "load";
	}

	/**
	 * Get the index into the local variables.
	 *
	 * @return The local variable index.
	 */
	public int getLocalVariableIndex() {
		if (this.index > 3) {
			return this.otherBytes[0]; // no explicit cast needed
		}
		return this.index;
	}

	/**
	 * Get the number of elements that will be pushed onto the stack when this instruction is
	 * executed.
	 *
	 * @return The number of elements that will be pushed onto the stack.
	 */
	public int getNumberOfPushedElements() {
		return 1;
	}

}
