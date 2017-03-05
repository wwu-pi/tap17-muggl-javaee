package de.wwu.muggl.symbolic.searchAlgorithms.choice.switching;

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
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.solvers.expressions.Term;

/**
 * A SwitchingChoicePoint is generated whenever one the instructions lookupswitch or tableswitch are
 * reached and the element on top of the operand stack of the executed frame is not constant. The
 * choice point will make sure that all possible jumping targets are evaluated. Due to its abstract
 * nature, concrete choice point can be derived from it.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public abstract class SwitchingChoicePoint implements ChoicePoint {
	// Fields regarding the choice point.
	private long number;
	private ChoicePoint parent;
	private Frame frame;
	private int pc;
	private int pcNext;
	/**
	 * The term term that was on top of the stack. Using the non symbolic
	 *        execution, this would be the key for the switch.
	 */
	protected Term termFromStack;
	/**
	 * The possible keys.
	 */
	protected IntConstant[] keys;
	/**
	 * The possible jump targets.
	 */
	protected int[] pcs;
	private Stack<TrailElement> trail;
	/**
	 * The execution step reached i.e. the number of switching targets yet reached. The value is is
	 * a number between 0 and {@link #maximumSteps}.
	 */
	protected int step;
	/**
	 * The number of switching targets.
	 */
	protected final int maximumSteps;
	/**
	 * The ConstraintExpression describing the current choice of the switch.
	 */
	protected ConstraintExpression constraintExpression;
	/**
	 * The current jump target pc.
	 */
	protected int jumpTo;
	/**
	 * true, if state changes were applied; false otherwise.
	 */
	protected boolean appliedState;
	
	protected int constraintLevel;

	/**
	 * Create the switching choice point.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param termFromStack The term term that was on top of the stack. Using the non symbolic
	 *        execution, this would be the key for the switch.
	 * @param keys The possible keys.
	 * @param pcs The possible jump targets.
	 * @throws IllegalArgumentException If the number of keys is not equal to the number of jump
	 *         targets or if there are no choices at all.
	 * @throws NullPointerException If either of the specified arrays is null.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public SwitchingChoicePoint(Frame frame, int pc, int pcNext, Term termFromStack,
			IntConstant[] keys, int[] pcs) throws SymbolicExecutionException {
		// Checking the parameters.
		if (keys == null)
			throw new NullPointerException("Keys must not be null");
		if (pcs == null)
			throw new NullPointerException("Jumping targets must not be null");
		if (keys.length != pcs.length)
			throw new IllegalArgumentException("The number of keys must be equal to the number of jump targets.");
		if (keys.length < 1)
			throw new IllegalArgumentException("There has to be at least once choice for a switching instruction");

		// Basic initialization.
		this.number = 0;
		this.parent = null;
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.termFromStack = termFromStack;
		this.keys = keys;
		this.pcs = pcs;
		this.trail = new Stack<TrailElement>();
		this.step = 0;
		this.maximumSteps = pcs.length;
		this.appliedState = false;
		
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();		
	}
	
	public void init() throws SymbolicExecutionException {
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
					this.frame.getVm().setPC(this.pcs[this.step]);

					// Mark that this was successful.
					success = true;
				}
			} catch (SolverUnableToDecideException e) {
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
					Globals.getInst().symbolicExecLogger.trace("Solving lead to a SolverUnableToDecideException with message: " + e.getMessage());
			} catch (TimeoutException e) {
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled())
					Globals.getInst().symbolicExecLogger.trace("Solving lead to a TimeoutException with message: " + e.getMessage());
			}

			// Check if we do not exceed the maximum possible number of steps.
			if (!success && this.step == this.maximumSteps)
				throw new SymbolicExecutionException("Equations are violated. The term from stack cannot be fulfilled by any switch condition, including the default. This is impossible and hints to serious problems.");
		}
		// Save the execution time.
		if (measureExecutionTime) ((SymbolicVirtualMachine) this.frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - timeSolvingTemp);
	}

	/**
	 * Create a ChoicePoint that has a parent ChoicePoint.
	 *
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the lcmp instruction.
	 * @param termFromStack The term term that was on top of the stack. Using the non symbolic
	 *        execution, this would be the key for the switch.
	 * @param keys The possible keys.
	 * @param pcs The possible jump targets.
	 * @param parent The parent ChoicePoint.
	 * @throws IllegalArgumentException If the number of keys is not equal to the number of jump
	 *         targets.
	 * @throws NullPointerException If either of the specified arrays is null.
	 * @throws SymbolicExecutionException On fatal problems applying the constraint.
	 */
	public SwitchingChoicePoint(Frame frame, int pc, int pcNext, Term termFromStack,
			IntConstant[] keys, int[] pcs, ChoicePoint parent) throws SymbolicExecutionException {
		this(frame, pc, pcNext, termFromStack, keys, pcs);
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
	 * Indicates whether this choice point offers another choice. This is true as long as it's
	 * applyStateChanges() method has not been called as many times as there are posible jump
	 * targets. Please note that it is already called once while constructing the instance of this
	 * choice point.
	 *
	 * @return true, if this choice point has another choice, false otherwise.
	 */
	public boolean hasAnotherChoice() {
		if (this.step < this.maximumSteps)
			return true;
		return false;
	}

	/**
	 * Change to the next choice.
	 *
	 * @throws IllegalStateException If there are no more choices.
	 */
	public void changeToNextChoice() {
		if (this.step >= this.maximumSteps)
			throw new IllegalStateException("There are no more choices.");

		// This is any "normal" case.
		this.constraintExpression = NumericEqual.newInstance(this.termFromStack, this.keys[this.step]);
		this.jumpTo = this.pcs[this.step];

		this.step++;
		this.appliedState = false;
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
	 * state. Such changes include modifications of the local variables, method
	 * parameters and fields.
	 *
	 * @return true.
	 */
	public boolean enforcesStateChanges() {
		return true;
	}

	/**
	 * Apply any state changes required for the current choice. This will set the pc of the frame
	 * associated with this choice point to the next jump target.<br />
	 * <br />
	 * The method can only be called after changeToNextChoice() has been run. Calling it another
	 * time before running changeToNextChoice() will have an exception thrown.
	 *
	 * @throws NullPointerException If no new value has been prepared yet of if there are no more
	 *         values to be applied at all.
	 */
	public synchronized void applyStateChanges() {
		if (this.constraintExpression == null || this.appliedState)
			throw new NullPointerException("Cannot apply state changes.");

		this.frame.setPc(this.jumpTo);
		this.appliedState = true;
	}

	/**
	 * Get a string representation of the type of choice point.
	 *
	 * @return A string representation of the type of choice point
	 */
	public abstract String getChoicePointType();
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
