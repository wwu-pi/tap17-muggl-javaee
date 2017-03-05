package de.wwu.muggl.symbolic.searchAlgorithms.choice.longComparison;

import java.util.Stack;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicExecutionException;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.LessThan;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * A LongComparisonChoicePoint is generated whenever the instruction lcmp is reached. The
 * instruction is used to compare values of type long and leaves three possibilities. This choice
 * point will make sure all three will be checked.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class LongComparisonChoicePoint implements ChoicePoint {
	// Fields regarding the choice point.
	private long number;
	private ChoicePoint parent;
	private Frame frame;
	private int pc;
	private int pcNext;
	private Term leftTerm;
	private Term rightTerm;
	private Stack<TrailElement> trail;
	private int step;
	private ConstraintExpression constraintExpression;
	private int nextValueToPush;
	private boolean applyedState;
	protected int constraintLevel;

	/**
	 * Create the long comparison choice point.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param leftTerm The term of long variables and constants of the left hand side of the comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the comparison.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public LongComparisonChoicePoint(Frame frame, int pc, int pcNext, Term leftTerm, Term rightTerm)
			throws SymbolicExecutionException {
		// Basic initialization.
		this.number = 0;
		this.parent = null;
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.leftTerm = leftTerm;
		this.rightTerm = rightTerm;
		this.trail = new Stack<TrailElement>();
		this.step = 0;
		this.applyedState = false;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();

		// Load an option.
		boolean measureExecutionTime = Options.getInst().measureSymbolicExecutionTime;

		// Get the SolverManager.
		SolverManager solverManager = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager();

		boolean success = false;
		long timeSolvingTemp = 0L;
		if (measureExecutionTime) timeSolvingTemp = System.nanoTime();
		while (!success) {
			// Remove the last value?
			if (this.step != 0)
				solverManager.removeConstraint();

			// Change to the first value.
			changeToNextChoice();

			// Add the constraint.
			solverManager.addConstraint(this.constraintExpression);

			// Check if this ConstraintExpression does not violate other equations.
			try {
				if (solverManager.hasSolution()) {
					// Set the pc to the jump target.
					this.frame.getVm().setPC(pcNext);

					// Mark that this was successful.
					success = true;
				}
			} catch (SolverUnableToDecideException e) {
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving lead to a SolverUnableToDecideException with message: " + e.getMessage());
			} catch (TimeoutException e) {
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving lead to a TimeoutException with message: " + e.getMessage());
			}

			// Check if we do not exceed the maximum possible number of steps.
			if (!success && this.step == 3)
				throw new SymbolicExecutionException("The first term is neither less than, greater than or equal to the second one. This is impossible and hints to serious problems.");
		}
		// Save the execution time.
		if (measureExecutionTime) ((SymbolicVirtualMachine) frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - timeSolvingTemp);
	}

	/**
	 * Create a ChoicePoint that has a parent ChoicePoint.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param parent The parent ChoicePoint
	 * @param leftTerm The term of long variables and constants of the left hand side of the comparison.
	 * @param rightTerm The term of long variables and constants of the right hand side of the comparison.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public LongComparisonChoicePoint(Frame frame, int pc, int pcNext, Term leftTerm,
			Term rightTerm, ChoicePoint parent) throws SymbolicExecutionException {
		this(frame, pc, pcNext, leftTerm, rightTerm);
		if (parent != null) {
			this.number = parent.getNumber() + 1;
			this.parent = parent;
		}
	}

	/**
	 * Retun this ChoicePoint's number.
	 *
	 * @return This ChoicePoint's number.
	 */
	public long getNumber() {
		return this.number;
	}

	/**
	 * Indicates whether this choice point offers another choice. This is true
	 * as long as it's applyStateChanges() method has not been called two
	 * additional times.
	 *
	 * @return true, if this choice point has another choice, false otherwise.
	 */
	public boolean hasAnotherChoice() {
		if (this.step < 3)
			return true;
		return false;
	}

	/**
	 * Change to the next choice.
	 *
	 * @throws IllegalStateException If there are no more choices.
	 */
	public void changeToNextChoice() {
		if (this.step >= 3)
			throw new IllegalStateException("There are no more choices.");
		if (this.step == 0) {
			this.constraintExpression = GreaterThan.newInstance(this.leftTerm, this.rightTerm);
			this.nextValueToPush = 1;
		} else if (this.step == 1) {
			this.constraintExpression = LessThan.newInstance(this.leftTerm, this.rightTerm);
			this.nextValueToPush = -1;
		} else if (this.step == 2) {
			this.constraintExpression = NumericEqual.newInstance(this.leftTerm, this.rightTerm);
			this.nextValueToPush = 0;
		}

		this.step++;
		this.applyedState = false;
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
	 * instruction.
	 *
	 * @return The pc of the instruction directly following as an int value.
	 */
	public int getPcNext() {
		return this.pcNext;
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
	 * system. It does.
	 *
	 * @return true;
	 */
	public boolean changesTheConstraintSystem() {
		return true;
	}

	/**
	 * Return the ConstraintExpression of this ChoicePoint.
	 *
	 * @return The ConstraintExpression of this ChoicePoint.
	 * @throws IllegalStateException If this choice point is not yet initialized or if initialization failed.
	 */
	public ConstraintExpression getConstraintExpression() {
		if (this.constraintExpression == null)
			throw new IllegalStateException(
					"There is no ConstraintExpression since this choice point is not initiallized.");
		return this.constraintExpression;
	}

	/**
	 * Setter for the ConstraintExpression of this ChoicePoint. It does not have any
	 * effect.
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
	 *
	 * @param element The TrailElement to be added to the trail.
	 */
	public void addToTrail(TrailElement element) {
		this.trail.push(element);
	}

	/**
	 * Getter for the trail.
	 *
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
	 * Apply any state changes required for the current choice. This will push the int value -1, 0 or 1 onto the stack
	 * and add a corresponding constraint for the long variable.<br />
	 * <br />
	 * The method can only be called after changeToNextChoice() has been run. Calling it another
	 * time before running changeToNextChoice() will have an exception thrown.
	 *
	 * @throws NullPointerException If no new value has been prepared yet of if there are no more values to be applied at all.
	 */
	public synchronized void applyStateChanges() {
		if (this.constraintExpression == null || this.applyedState)
			throw new NullPointerException("Cannot apply state changes.");
		this.frame.getOperandStack().push(IntConstant.getInstance(this.nextValueToPush));
		this.applyedState = true;
	}

	/**
	 * Get a string representation of the type of choice point.
	 * @return A string representation of the type of choice point
	 */
	public String getChoicePointType() {
		return "long comparison choice point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
