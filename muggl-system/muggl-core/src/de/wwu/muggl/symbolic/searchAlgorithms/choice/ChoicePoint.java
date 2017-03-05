package de.wwu.muggl.symbolic.searchAlgorithms.choice;

import java.util.Stack;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;

/**
 * Interface for choice points. It defines methods to be implemented by any choice point used in
 * this application. It hence has to be implemented by any concrete implementation, regardless of
 * the number of possible jump targets or choices.<br />
 * <br />
 * Choice points are not necessarily conditional jumps. Instructions that have choice points
 * generated may not lead to jumps in the control flow at all or may even enforce jumping on any
 * execution. Choice points are hence used to make sure execution will track back to them and
 * something in the state space will change in comparison to the first pass before the execution is
 * continued.<br />
 * <br />
 * The typical life cycle of a choice point can be considered to be like this:
 * <ul>
 * <ol>
 * 1. Generation of the choice point with some initial values.
 * </ol>
 * <ol>
 * 2. Addition of further values.
 * </ol>
 * <ol>
 * 3. Continued execution with the first choice's state.
 * </ol>
 * <ol>
 * 4. Backtracking due to the end of the execution or another trigger.
 * </ol>
 * <ol>
 * 5. Checking if this choice point has another solution. => End of life if no solution is found.
 * Control is given to the parent choice point.
 * </ol>
 * <ol>
 * 6. Restoring of the state that the machine was in when generating the choice point, before
 * actually finishing execution of the choice point's instruction.
 * </ol>
 * <ol>
 * 7. Calling changeToNextChoice().
 * </ol>
 * <ol>
 * 8. Usage of the choice point to adapt the state to the situation after the choice point's
 * instruction has been executed.
 * </ol>
 * <ol>
 * 9. Continued execution until 4. occurs.
 * </ol>
 * </ul>
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public interface ChoicePoint {

	/**
	 * Return this ChoicePoint's number.
	 *
	 * @return This ChoicePoint's number.
	 */
	long getNumber();

	/**
	 * Indicates whether this choice point offers another choice. Initially, any
	 * choice point should return true as a choice point with only one choice is
	 * obsolete. Use changeToNextChoice() to change to this choice.
	 *
	 * @return true, if this choice point has another choice, false otherwise.
	 */
	boolean hasAnotherChoice();

	/**
	 * Change to the next choice. This will change the internal state of this choice point only. For
	 * example, the ConstraintExpression returned by getConstraintExpression() might change or the
	 * action taken when applyStateChanges() is called could be adjusted.
	 *
	 * @throws MugglException If the change to the next choice failed. Implementing classes may
	 *         not throw this exception. If they throw it, they should throw sub classes of it.
	 * @throws IllegalStateException If there are no more choices.
	 */
	void changeToNextChoice() throws MugglException;

	/**
	 * Return the Frame of this ChoicePoint.
	 *
	 * @return The Frame of this ChoicePoint.
	 */
	Frame getFrame();

	/**
	 * Return the pc of this ChoicePoint.
	 *
	 * @return The pc of this ChoicePoint.
	 */
	int getPc();

	/**
	 * Return the pc of the instruction directly following the choice point's
	 * instruction. It even is the directly following instruction of the
	 * execution of the choice point's instruction might lead to a jump. It
	 * however has to be the jump target if the choice point's instruction is an
	 * unconditional jump.
	 *
	 * @return The pc of the instruction directly following.
	 */
	int getPcNext();

	/**
	 * Return the parent of this ChoicePoint. The parent might be null,
	 * indicating that this is the root choice point.
	 *
	 * @return The parent of this ChoicePoint.
	 */
	ChoicePoint getParent();

	/**
	 * Indicates whether this choice point enforces changes to the constraint
	 * system. Choice points that represent conditional states will introduce
	 * constraints to the constraint sysytem and will need to have them removed
	 * on backtracking. Other choice point will possible work without changes to
	 * the constraint system.
	 *
	 * @return true, if this choice point enforces changes to the constraint
	 *         system, false otherwise.
	 */
	boolean changesTheConstraintSystem();

	/**
	 * Return the ConstraintExpression of this ChoicePoint. Be sure to use
	 * changesTheConstraintSystem() and receive true before calling this method.
	 *
	 * @return The ConstraintExpression of this ChoicePoint.
	 */
	ConstraintExpression getConstraintExpression();

	/**
	 * Setter for the ConstraintExpression of this ChoicePoint.
	 *
	 * @param constraintExpression The new ConstraintExpression for this ChoicePoint.
	 */
	void setConstraintExpression(
			ConstraintExpression constraintExpression);

	/**
	 * Find out if this ChoicePoint has a trail.
	 *
	 * @return true, if this ChoicePoint has a trail, false otherwise.
	 */
	boolean hasTrail();

	/**
	 * Getter for the trail.
	 *
	 * @return The trail.
	 */
	Stack<TrailElement> getTrail();

	/**
	 * If it has one, add an object to the trail of this ChoicePoint.
	 *
	 * @param element
	 *            The TrailElement to be added to the trail.
	 */
	void addToTrail(TrailElement element);

	/**
	 * Indicates whether this choice point enforces changes to the execution
	 * state. Suche changes include modifications of the local variables, method
	 * parameters and fields.
	 *
	 * @return true, if this choice point enforces changes to the execution
	 *         state, false otherwise.
	 */
	boolean enforcesStateChanges();

	/**
	 * Apply any state changes required for the current choice.
	 */
	void applyStateChanges() throws VmRuntimeException;

	/**
	 * Get a string representation of the type of choice point.
	 * @return A string representation of the type of choice point
	 */
	String getChoicePointType();

	
	
	/**
	 * Get the level of the constraint solver at time of choice point creation.
	 * Needed to reset the choice point.
	 */
	int getConstraintLevel();
}
