package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst;

import java.util.Stack;

import de.wwu.muggl.configuration.Globals;
import de.wwu.muggl.configuration.Options;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.EquationViolationException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.SolvingException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.conditionalJump.ConditionalJumpChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;

/**
 * A ConditionalJumpChoicePointDepthFirst is a ChoicePoint for the depth first
 * search algorithm. Its keeps information about its parent ChoicePoint, and
 * a trail of changes made in the runtime environment since the last ChoicePoint
 * was generated. It is therefore possible to restore the state of the execution at
 * the time of the generated of each ChoicePoint.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-16
 */
public class ConditionalJumpChoicePointDepthFirst extends ConditionalJumpChoicePoint {
	// Fields.
	private boolean alreadyVisitedNonJumpingBranch = false;
	private Stack<TrailElement> trail = new Stack<TrailElement>();
	/**
	 * Flag to determine if the execution time should be measured.
	 */
	protected boolean measureExecutionTime;
	/**
	 * Temporary field for time measuring.
	 */
	protected long timeSolvingTemp;

	/**
	 * Create the initial ChoicePoint of an application.
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the conditional jump instruction.
	 * @param pcWithJump The pc the conditional jump would jump to.
	 * @param constraintExpression The ConstraintExpression describing the choice of the conditional jump.
	 */
	public ConditionalJumpChoicePointDepthFirst(Frame frame, int pc, int pcNext, int pcWithJump, ConstraintExpression constraintExpression) {
		super(frame, pc, pcNext, pcWithJump, constraintExpression);
		this.measureExecutionTime = Options.getInst().measureSymbolicExecutionTime;
	}

	/**
	 * Create an ChoicePoint that has a parent ChoicePoint.
	 * @param frame The currently executed Frame.
	 * @param pc The pc of the instruction that generates the ChoicePoint.
	 * @param pcNext The pc of the instruction after the conditional jump instruction.
	 * @param pcWithJump The pc the conditional jump would jump to.
	 * @param constraintExpression The constraintExpression describing the choice of the conditional jump.
	 * @param parent The parent ChoicePoint
	 * @throws EquationViolationException If the new equation violates the choice point parents' equations.
	 * @throws SolvingException If the solving of a newly added ConstraintExpression failed.
	 */
	public ConditionalJumpChoicePointDepthFirst(Frame frame, int pc, int pcNext, int pcWithJump, ConstraintExpression constraintExpression, ChoicePoint parent)
		throws EquationViolationException, SolvingException {
		super(frame, pc, pcNext, pcWithJump, constraintExpression);

		// Just continue if the parent is set.
		if (parent != null) {
			// Set the fields.
			this.number = parent.getNumber() + 1;
			this.parent = parent;
		}

		this.measureExecutionTime = Options.getInst().measureSymbolicExecutionTime;

		// Add the ConstraintExpression.
		SolverManager solverManager = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager();
		solverManager.addConstraint(constraintExpression);

		System.out.println("+++ create new choice point with: "+ constraintExpression + " <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		
		// Check if this ConstraintExpression does not violate other equations.
		try {
			if (this.measureExecutionTime) this.timeSolvingTemp = System.nanoTime();
			if (!solverManager.hasSolution()) {
				if (this.measureExecutionTime) ((SymbolicVirtualMachine) frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - this.timeSolvingTemp);
				tryTheNegatedConstraint(solverManager, constraintExpression);
			} else {
				if (this.measureExecutionTime) ((SymbolicVirtualMachine) frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - this.timeSolvingTemp);
				// Set the pc to the jump target.
				this.frame.getVm().setPC(pcWithJump);
			}
		} catch (SolverUnableToDecideException e) {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving lead to a SolverUnableToDecideException with message: " + e.getMessage());
			tryTheNegatedConstraint(solverManager, constraintExpression);
			this.alreadyVisitedNonJumpingBranch = true;
		} catch (TimeoutException e) {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving lead to a TimeoutException with message: " + e.getMessage());
			tryTheNegatedConstraint(solverManager, constraintExpression);
			this.alreadyVisitedNonJumpingBranch = true;
		}
	}

	/**
	 * If the newly added ConstraintExpression cannot be solved, alternatively try the negated ConstraintExpression.
	 * @param solverManager The SolverManager.
	 * @param constraintExpression The ConstraintExpression to negate.
	 * @throws EquationViolationException If the new equation violates the choice point parents' equations.
	 * @throws SolvingException If the solving of a newly added ConstraintExpression failed.
	 */
	private void tryTheNegatedConstraint(SolverManager solverManager, ConstraintExpression constraintExpression) throws EquationViolationException, SolvingException {
		try {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Cannot proceed with the jumping branch since this would violate the current constraint system. Trying the non-jumping branch...");
			// The expression is unsolvable. Is it at least possible to use the non-jumping branch?
			solverManager.removeConstraint();
			solverManager.addConstraint(constraintExpression.negate());
			if (this.measureExecutionTime) this.timeSolvingTemp = System.nanoTime();
			if (solverManager.hasSolution()) {
				if (this.measureExecutionTime) ((SymbolicVirtualMachine) this.frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - this.timeSolvingTemp);
				// Use the non-jumping branch.
				this.frame.getVm().setPC(this.pcNext);
				this.alreadyVisitedNonJumpingBranch = true;
			} else {
				if (this.measureExecutionTime) ((SymbolicVirtualMachine) this.frame.getVm()).increaseTimeSolvingForChoicePointGeneration(System.nanoTime() - this.timeSolvingTemp);
				if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Cannot proceed with the non-jumping branch either since this would violate the current constraint system. Tracking back...");
				// Throw the appropriate Exception.
				throw new EquationViolationException("Cannot continue with this choice point, since equations are violated.");
			}
		} catch (SolverUnableToDecideException e) {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving again lead to a SolverUnableToDecideException with message: " + e.getMessage());
			throw new SolvingException("Cannot continue with this choice point, since solving failed.");
		} catch (TimeoutException e) {
			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving again lead to a TimeoutException with message: " + e.getMessage());
			throw new SolvingException("Cannot continue with this choice point, since solving failed.");
		}
	}

	/**
	 * Find out if this ChoicePoint has a trail.
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
	 * Find out if the non-jumping branch of this ChoicePoint has been visited
	 * already.
	 * @return true, if the non-jumping branch has been visited already, false otherwise.
	 */
	public boolean hasAnotherChoice() {
		return !this.alreadyVisitedNonJumpingBranch;
	}

	/**
	 * Mark that the non-jumping branch of this ChoicePoint has been visited
	 * already. If the non-jumping branch has been visited already, nothing
	 * will be done.
	 */
	public void changeToNextChoice() {
		if (!this.alreadyVisitedNonJumpingBranch) {
			this.alreadyVisitedNonJumpingBranch = true;
			// Negate the constraint expression.
			this.constraintExpression = this.constraintExpression.negate();				
			System.out.println("+++ change to next choice, now: " + this.constraintExpression + " <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		}
	}

}
