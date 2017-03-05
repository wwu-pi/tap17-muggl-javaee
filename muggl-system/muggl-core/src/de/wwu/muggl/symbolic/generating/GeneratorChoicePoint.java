package de.wwu.muggl.symbolic.generating;

import java.util.Stack;

import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.execution.ConversionException;
import de.wwu.muggl.vm.execution.MugglToJavaConversion;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;

/**
 * The ChoicePoint is used aside with variable generators. Generators can be used to
 * generate a sequence of variables which should be used one after the other. A
 * generator choice point can be created so backtracking and restarting the execution
 * with the next value provided is possible.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class GeneratorChoicePoint implements ChoicePoint {
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
	 * The corresponding generator.
	 */
	protected Generator generator;
	/**
	 * The frame of the method that offers a choice.
	 */
	protected Frame frame;
	/**
	 * The index into the local variable table to store the generated array at.
	 */
	protected int index;
	/**
	 * The pc of the instruction that offers a choice.
	 */
	protected int pc;
	private Stack<TrailElement> trail;

	// Fields regarding the generation of values.
	private Object generatedValue;
	private boolean valuePrepared;
	
	protected int constraintLevel;

	/**
	 * Constructs a GeneratorChoicePoint.
	 *
	 * @param generator The Generator used by this ChoicePoint.
	 * @param frame The currently executed Frame.
	 * @param index The index into the local variable table to store the generated array at.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @throws ConversionException If converting the first provided object failed.
	 * @throws NullPointerException If the supplied generator or frame is null.
	 */
	public GeneratorChoicePoint(Generator generator, Frame frame, int index, int pc)
			throws ConversionException {
		// Checking for null.
		if (generator == null)
			throw new NullPointerException("The supplied Generator must not be null.");
		if (frame == null)
			throw new NullPointerException("The supplied Frame must not be null.");

		// Basic initialization.
		this.number = 0;
		this.parent = null;
		this.generator = generator;
		this.frame = frame;
		this.index = index;
		this.pc = pc;
		this.trail = new Stack<TrailElement>();
		this.generatedValue = null;
		this.valuePrepared = false;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();

		// Change to the first value.
		changeToNextChoice();
	}

	/**
	 * Constructs a GeneratorChoicePoint that has a parent ChoicePoint.
	 *
	 * @param generator The Generator used by this ChoicePoint.
	 * @param frame The currently executed Frame.
	 * @param index The index into the local variable table to store the generated array at.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param parent The parent ChoicePoint.
	 * @throws ConversionException If converting the first provided object failed.
	 * @throws NullPointerException If the supplied generator or frame is null.
	 */
	public GeneratorChoicePoint(Generator generator, Frame frame, int index, int pc, ChoicePoint parent)
			throws ConversionException {
		this(generator, frame, index, pc);
		if (parent != null) {
			this.number = parent.getNumber() + 1;
			this.parent = parent;
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
	 * Check if the Generator used can provide another object.
	 *
	 * @return true, if this choice point has another choice, false otherwise.
	 */
	public boolean hasAnotherChoice() {
		return this.generator.hasAnotherObject();
	}

	/**
	 * Change to the next choice. This will prepare the generated value.
	 *
	 * @throws ConversionException If converting a provided object failed failed.
	 * @throws IllegalStateException If there are no more choices.
	 */
	public void changeToNextChoice() throws ConversionException {
		this.generatedValue = this.generator.provideObject();
		if (this.generator.objectNeedsConversion()) {
			MugglToJavaConversion conversion = new MugglToJavaConversion(this.frame.getVm());
			this.generatedValue = conversion.toMuggl(this.generatedValue, false);
		}
		this.valuePrepared = true;
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
	 * generating instruction, generator choice points are needed by load
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
	 * effect.
	 *
	 * @param expression The new ConstraintExpression for this ChoicePoint.
	 */
	public void setConstraintExpression(ConstraintExpression expression) { }

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
	 * state. Such changes include modifications of the local variables, method
	 * parameters and fields.
	 *
	 * @return true.
	 */
	public boolean enforcesStateChanges() {
		return true;
	}

	/**
	 * Apply any state changes required for the current choice. This will set the
	 * generated value as a local variable of the frame. Be sure to call this method
	 * after preparing a new value via changeToNextChoice(); and of course after using
	 * hasAnotherChoice() to find out if there is another choice at all.
	 * @throws NullPointerException If no new value has been prepared yet of if there are no more values to be applied at all.
	 */
	public void applyStateChanges() {
		if (!this.valuePrepared) throw new NullPointerException("There is no new generated value.");
		this.frame.setLocalVariable(this.index, this.generatedValue);
		this.frame.getMethod().setGeneratedValue(this.index, this.generatedValue);
		this.valuePrepared = false;

	}

	/**
	 * Get a string representation of the type of choice point.
	 * @return A string representation of the type of choice point
	 */
	public String getChoicePointType() {
		return "generator choice point";
	}

	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}

}
