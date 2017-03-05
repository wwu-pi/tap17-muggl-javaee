package de.wwu.muggl.symbolic.searchAlgorithms.choice.array;

import java.util.Stack;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.instructions.typed.TypedInstruction;
import de.wwu.muggl.symbolic.generating.ArrayElementsGenerator;
import de.wwu.muggl.symbolic.generating.ArrayElementsGeneratorProvider;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.ModifieableArrayref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.NumericVariable;

/**
 * This class represents the ChoicePoint generated to represent the symbolic array generation strategy. It
 * manages the arrays used based on a fixed strategy.<br />
 * <br />
 * It is almost impossible to find a suitable mathematical representation for arrays that are created at
 * runtime and that have an unknown length. While of course approaches exists, constraint solving does not
 * seem possible. Hence, arrays are tested in a partly non symbolic way. The systems runs with an generated
 * array until it backtracks and restarts with another one. The elements are symbolic, while the array is
 * fixed.<br />
 * <br />
 * The strategy and its parameters are set by options.
 *
 * @see de.wwu.muggl.configuration.Options#symbArrayInitNumberOfRunsTotal
 * @see de.wwu.muggl.configuration.Options#symbArrayInitStartingLength
 * @see de.wwu.muggl.configuration.Options#symbArrayInitIncrStrategy
 * @see de.wwu.muggl.configuration.Options#symbArrayInitIncrStrategyLinearStepSize
 * @see de.wwu.muggl.configuration.Options#symbArrayInitTestNull
 * @see de.wwu.muggl.configuration.Options#symbArrayInitTestZeroLengthArray
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2011-01-05
 */
public class ArrayInitializationChoicePoint implements ChoicePoint {
	// Fields regarding the choice point.
	/**
	 * The identification number of the choice point.
	 */
	protected long number;
	/**
	 * The parent choice point. Can be null to indicate that a choice point has no parent.
	 */
	protected ChoicePoint parent;
	/**
	 * The frame of the method that offers a choice.
	 */
	protected Frame frame;
	/**
	 * The index into the local variable table to store the generated array at. A value of -1
	 * indicates that values should be pushed since they are generated for <code>newarray</code>
	 * rather than for <code>aload</code>.
	 */
	protected int index;
	/**
	 * The pc of the instruction that offers a choice.
	 */
	protected int pc;
	private Stack<TrailElement> trail;
	
	private int constraintLevel;

	// Fields regarding the generation of arrays.
	private String typeString;
	private String variableName;
	private ReferenceValue referenceValue;
	private ModifieableArrayref preparedArray;
	private boolean newArrayPrepared;
	private int numberOfRunsYet;
	private int numberOfRunsTotal;
	private boolean testNull;
	private boolean testZeroLengthArray;
	private ArrayGenerator generator;
	private boolean useCustomArrayElementGenerator;
	private ArrayElementsGenerator arrayElementGenerator;

	/**
	 * Create the array initialization choice point of a method for instruction <code>aload</code>.
	 * 
	 * @param frame The currently executed Frame.
	 * @param index The index into the local variable table to store the generated array at.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for
	 *         or on fatal errors using the expression system of the solver.
	 */
	public ArrayInitializationChoicePoint(Frame frame, int index, int pc) throws SymbolicExecutionException {
		this(frame, pc);
		this.index = index;
		
		// Check the type.
		this.typeString = frame.getMethod().getParameterTypeAtIndex(index);
		while (this.typeString.endsWith("[]")) {
			this.typeString = this.typeString.substring(0, this.typeString.length() - 2);
		}
		typecheck();
		this.variableName = TypedInstruction.generateVariableNameByNumber(this.frame.getMethod(), this.index);
		
		// Check if a array element generator should be used.
		int variableShift = 1;
		if (frame.getMethod().isAccStatic()) variableShift = 0;
		ArrayElementsGeneratorProvider generatorProvider = frame.getMethod().getArrayElementsGeneratorProvider(index - variableShift);
		if (generatorProvider != null) {
			try {
				// Get the generator.
				this.arrayElementGenerator = generatorProvider.provideInstance(this.variableName);

				// Has a generator been provided?
				if (this.arrayElementGenerator == null)
					throw new SymbolicExecutionException("An array elements generator provider did not provide a Generator.");

			// Catching anything the generator provider may throw.
			} catch (Throwable t) {
				throw new SymbolicExecutionException(
						"A array elements generator provider did not provide a Generator. It instead threw a Throwable: "
						+ t.getClass().getName() + " (" + t.getMessage() + ")");
			}
			this.useCustomArrayElementGenerator = true;
		} else {
			this.useCustomArrayElementGenerator = false;
		}
		
		// Change to the first array.
		changeToNextChoice();
		
		// Report the instantiation.
		((SymbolicVirtualMachine) frame.getVm()).reportArrayGenerator();
	}
	
	/**
	 * Create the array initialization choice point of a method for instruction <code>newarray</code>.
	 * 
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param type A String representation of the type.
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for
	 *         or on fatal errors using the expression system of the solver.
	 */
	public ArrayInitializationChoicePoint(Frame frame, int pc, String type) throws SymbolicExecutionException {
		this(frame, pc);
		this.index = -1;
		
		// Check the type.
		this.typeString = type;
		while (this.typeString.endsWith("[]")) {
			this.typeString = this.typeString.substring(0, this.typeString.length() - 2);
		}
		typecheck();
		this.variableName = "anewarray"; // TODO: try to derive this from the class file. it is only a cosmetic issue, though.
		
		// We always use the built-in generator for anewarray.
		this.useCustomArrayElementGenerator = false;
		
		// Disable testing of null. newarray would not generate it.
		if (this.testNull) {
			this.testNull = false;
			this.numberOfRunsTotal--;
		}
		
		// Change to the first array.
		changeToNextChoice();
		
		// Report the instantiation.
		((SymbolicVirtualMachine) frame.getVm()).reportArrayGenerator();
	}
	

	/**
	 * Create a ChoicePoint that has a parent ChoicePoint.
	 * 
	 * @param frame The currently executed Frame.
	 * @param index The index into the local variable table to store the generated array at.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param parent The parent ChoicePoint
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for
	 *         or on fatal errors using the expression system of the solver.
	 */
	public ArrayInitializationChoicePoint(Frame frame, int index, int pc, ChoicePoint parent) throws SymbolicExecutionException {
		this(frame, index, pc);
		if (parent != null) {
			this.number = parent.getNumber() + 1;
			this.parent = parent;
		}
	}

	/**
	 * Create a ChoicePoint that has a parent ChoicePoint.
	 * 
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param type A String representation of the type.
	 * @param parent The parent ChoicePoint
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for
	 *         or on fatal errors using the expression system of the solver.
	 */
	public ArrayInitializationChoicePoint(Frame frame, int pc, String type, ChoicePoint parent) throws SymbolicExecutionException {
		this(frame, pc, type);
		if (parent != null) {
			this.number = parent.getNumber() + 1;
			this.parent = parent;
		}
	}
	
	/**
	 * Private constructor for basic initialization.
	 * 
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @throws SymbolicExecutionException On fatal errors using the expression system of the solver.
	 */
	private ArrayInitializationChoicePoint(Frame frame, int pc) throws SymbolicExecutionException {
		// Basic initialization.
		this.number = 0;
		this.parent = null;
		this.frame = frame;
		this.pc = pc;
		this.trail = new Stack<TrailElement>();
		this.newArrayPrepared = false;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		// Load options...
		Options options = Options.getInst();
		this.numberOfRunsYet = 0;
		this.numberOfRunsTotal = options.symbArrayInitNumberOfRunsTotal;
		this.testNull = options.symbArrayInitTestNull;
		if (this.testNull) this.numberOfRunsTotal++;
		this.testZeroLengthArray = options.symbArrayInitTestZeroLengthArray;
		if (this.testZeroLengthArray) this.numberOfRunsTotal++;
		switch (options.symbArrayInitIncrStrategy) {
			case 0:
				this.generator = new ArrayGeneratorLinear(options.symbArrayInitStartingLength, options.symbArrayInitIncrStrategyLinearStepSize);
				break;
			case 1:
				this.generator = new ArrayGeneratorFibonacci(options.symbArrayInitStartingLength);
				break;
			case 3:
				this.generator = new ArrayGeneratorTenPowerOfX(options.symbArrayInitStartingLength);
				break;
			default:
				this.generator = new ArrayGeneratorExponential(options.symbArrayInitStartingLength);
				break;
		}
		
		// Generate the first array reference.
		try {
			this.referenceValue = frame.getVm().getAnObjectref(frame.getVm().getClassLoader().getClassAsClassFile("de.wwu.muggl.solvers.expressions.Term"));
		} catch (ClassFileException e) {
			throw new SymbolicExecutionException("An internal class of this application's implementation could not be found. Please make sure de.wwu.testtool.* is on the class path.");
		}
	}
	
	/**
	 * Check the type for the arrays to be generated.
	 *
	 * @throws SymbolicExecutionException If a type is encountered that no array can be created for.
	 */
	private void typecheck() throws SymbolicExecutionException {
		if (!(this.typeString.equals("char") || this.typeString.equals("java.lang.Character")
				|| this.typeString.equals("boolean") || this.typeString.equals("java.lang.Boolean")
				|| this.typeString.equals("byte") || this.typeString.equals("java.lang.Byte")
				|| this.typeString.equals("double") || this.typeString.equals("java.lang.Double")
				|| this.typeString.equals("int") || this.typeString.equals("java.lang.Integer")
				|| this.typeString.equals("float") || this.typeString.equals("java.lang.Float")
				|| this.typeString.equals("long") || this.typeString.equals("java.lang.Long")
				|| this.typeString.equals("short") || this.typeString.equals("java.lang.Short"))) {
			throw new SymbolicExecutionException(
					"There is no symbolic representation for the desired array type.");
		}
	}
	
	/**
	 * Return this ChoicePoint's number.
	 *
	 * @return This ChoicePoint's number.
	 */
	public long getNumber() {
		return this.number;
	}

	/**
	 * Indicates whether this choice point offers another choice. This is true
	 * as long as more arrays can be generated following the generation strategy.
	 *
	 * @return true, if this choice point has another choice, false otherwise.
	 */
	public boolean hasAnotherChoice() {
		if (this.numberOfRunsYet < this.numberOfRunsTotal)
			return true;
		return false;
	}

	/**
	 * Change to the next choice. This will prepare the next generated array for the
	 * local variable taking an array parameter of the method.
	 * @throws IllegalStateException If there are no more choices.
	 */
	public void changeToNextChoice() {
		if (this.numberOfRunsYet < this.numberOfRunsTotal) {
			// First check if a null reference or a zero length array should be provided.
			if ((this.numberOfRunsYet == 0 && (this.testNull || this.testZeroLengthArray))
					|| (this.numberOfRunsYet == 1 && this.testNull && this.testZeroLengthArray)) {
				if (this.numberOfRunsYet == 0) {
					// Check if a null reference is needed or an array of zero length.
					if (this.numberOfRunsYet == 0 && this.testNull) {
						this.preparedArray = null;
					} else {
						this.preparedArray = new ModifieableArrayref(this.referenceValue, 0);
					}
				} else {
					// In this case is definitely is an array of zero length.
					this.preparedArray = new ModifieableArrayref(this.referenceValue, 0);
				}
			} else {
				// Use the ArrayGenerator for this.
				int length = this.generator.provideNextArraysLength();
				if (this.preparedArray == null) {
					this.preparedArray = new ModifieableArrayref(this.referenceValue, length);
				} else {
					this.preparedArray.reset(length);
				}
			}

			// Fill up the array with variables.
			if (this.preparedArray != null) {
				this.preparedArray.disableTypeChecking();
				this.preparedArray.setRepresentedTypeAsAPrimitiveWrapper("java.lang.Integer");

				for (int a = 0; a < this.preparedArray.length; a++) {
					Object element;
					// Use an array element generator?
					if (this.useCustomArrayElementGenerator) {
						// Can the generator provide an element?
						if (this.arrayElementGenerator.hasAnotherElement()) {
							element = this.arrayElementGenerator.provideElement(a);
						} else {
							// Log that but proceed gracefully.
							if (Globals.getInst().symbolicExecLogger.isDebugEnabled())
								Globals.getInst().symbolicExecLogger.debug(
										"The array element generation algorithm could not provide an further element. "
										+ "Using an unbound variable instead of it.");
							element = getVariable(a);
						}
					} else {
						element = getVariable(a);
					}
					this.preparedArray.putElement(a, element);
				}
			}

			// Count up!
			this.numberOfRunsYet++;
			this.newArrayPrepared = true;
			((SymbolicVirtualMachine) this.frame.getVm()).reportGeneratedArray();
		} else {
			throw new IllegalStateException("There are no more choices.");
		}
	}

	/**
	 * Provide a new variable object that can be used by the array generators.
	 *
	 * @param position The position the variable will take in the array.
	 * @return A new variable object that can be used by the array generators.
	 */
	public NumericVariable getVariable(int position) {
		byte type;
		if (this.typeString.equals("char") || this.typeString.equals("java.lang.Character")) {
			type = Expression.CHAR;
		} else if (this.typeString.equals("boolean") || this.typeString.equals("java.lang.Boolean")) {
			type = Expression.BOOLEAN;
		} else if (this.typeString.equals("byte") || this.typeString.equals("java.lang.Byte")) {
			type = Expression.BYTE;
		} else if (this.typeString.equals("double") || this.typeString.equals("java.lang.Double")) {
			type = Expression.DOUBLE;
		} else if (this.typeString.equals("int") || this.typeString.equals("java.lang.Integer")) {
			type = Expression.INT;
		} else if (this.typeString.equals("float") || this.typeString.equals("java.lang.Float")) {
			type = Expression.FLOAT;
		} else if (this.typeString.equals("long") || this.typeString.equals("java.lang.Long")) {
			type = Expression.LONG;
		} else { // Only type "short" is left.
			type = Expression.SHORT;
		}
		return new NumericVariable(this.variableName  + "[" + position + "]", type, false);
	}

	/**
	 * Return the Frame of this ChoicePoint.
	 *
	 * @return The Frame of this ChoicePoint.
	 */
	public Frame getFrame() {
		return this.frame;
	}

	/**
	 * Return the pc of this ChoicePoint.
	 *
	 * @return The pc of this ChoicePoint as an int value.
	 */
	public int getPc() {
		return this.pc;
	}

	/**
	 * Return the pc of the instruction directly following the choice point's
	 * instruction. pc and pcNext are equal, as the instruction generating this
	 * choice point has to be executed again in any case. While instructions for
	 * conditions usually require the execution to be continued after the
	 * generating instruction, array initialization choice points are needed by load
	 * instructions which need the right input in the local variables when
	 * executed.
	 *
	 * @return zero, as an int value.
	 */
	public int getPcNext() {
		return this.pc;
	}

	/**
	 * Return the parent of this ChoicePoint. The parent might be null,
	 * indicating that this is the root choice point.
	 *
	 * @return The parent of this ChoicePoint.
	 */
	public ChoicePoint getParent() {
		return this.parent;
	}

	/**
	 * Indicates whether this choice point enforces changes to the constraint
	 * system. As this kind of ChoicPoint only accesses the local variables,
	 * no changes to the constraint system will ever be made.
	 *
	 * @return false;
	 */
	public boolean changesTheConstraintSystem() {
		return false;
	}

	/**
	 * Return the ConstraintExpression of this ChoicePoint. It is null, since
	 * there is no ConstraintExpression stored for it.
	 *
	 * @return null.
	 */
	public ConstraintExpression getConstraintExpression() {
		return null;
	}

	/**
	 * Setter for the ConstraintExpression of this ChoicePoint. It does not have any
	 * effect. Empty block.
	 *
	 * @param constraintExpression The new ConstraintExpression for this ChoicePoint.
	 */
	public void setConstraintExpression(ConstraintExpression constraintExpression) { }

	/**
	 * Find out if this ChoicePoint has a trail.
	 *
	 * @return true.
	 */
	public boolean hasTrail() {
		return true;
	}

	/**
	 * Add an object to the trail of this ChoicePoint.
	 * @param element The TrailElement to be added to the trail.
	 */
	public void addToTrail(TrailElement element) {
		this.trail.push(element);
	}

	/**
	 * Getter for the trail.
	 * @return The trail.
	 */
	public Stack<TrailElement> getTrail() {
		return this.trail;
	}

	/**
	 * Indicates whether this choice point enforces changes to the execution
	 * state. Suche changes include modifications of the local variables, method
	 * parameters and fields.
	 *
	 * @return true.
	 */
	public boolean enforcesStateChanges() {
		return true;
	}

	/**
	 * Apply any state changes required for the current choice. This will set the
	 * prepared array as a local variable of the frame. Be sure to call this method
	 * after preparing a new value via changeToNextChoice(); and of course after using
	 * hasAnotherChoice() to find out if there is another choice at all.
	 * @throws NullPointerException If no new value has been prepared yet of if there are no more values to be applied at all.
	 */
	public synchronized void applyStateChanges() {
		if (!this.newArrayPrepared) throw new NullPointerException("There is no new array prepared.");
		// Set or push?
		if (this.index != -1) {
			this.frame.setLocalVariable(this.index, this.preparedArray);
			if (this.preparedArray == null) {
				this.frame.getMethod().setGeneratedValue(this.index, null);
			} else {
				this.frame.getMethod().setGeneratedValue(this.index, this.preparedArray.clone());
			}
		} else {
			// Push.
			this.frame.getOperandStack().push(this.preparedArray);
			// Do not array execute newarray but move onwards.
			this.frame.setPc(this.frame.getPc() + 2);
		}
		this.newArrayPrepared = false;

	}

	/**
	 * Get a string representation of the type of choice point.
	 * @return A string representation of the type of choice point
	 */
	public String getChoicePointType() {
		return "array initialization choice point";
	}

	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}

}
