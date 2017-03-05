//package de.wwu.muggl.symbolic.searchAlgorithms.choice.db;
//
//import java.util.Stack;
//
//import de.wwu.muggl.configuration.Globals;
//import de.wwu.muggl.configuration.MugglException;
//import de.wwu.muggl.configuration.Options;
//import de.wwu.muggl.solvers.SolverManager;
//import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
//import de.wwu.muggl.solvers.exceptions.TimeoutException;
//import de.wwu.muggl.solvers.expressions.ConstraintExpression;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.SolvingException;
//import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
//import de.wwu.muggl.vm.Frame;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
//
//public class DatabaseChoicePoint implements ChoicePoint {
//
//	/**
//	 * The parent choice point. Can be null to indicate that a choice point has no parent.
//	 */
//	protected ChoicePoint parent = null;
//	/**
//	 * The identification number of the choice point.
//	 */
//	protected long number;
//	/**
//	 * The frame of the method that offers a choice.
//	 */
//	protected Frame frame;
//	/**
//	 * The pc of the instruction that offers a choice.
//	 */
//	protected int pc;
//	/**
//	 * The pc following the instruction that offers a choice.
//	 */
//	protected int pcNext;
//	/**
//	 * The pc that is executed after the instruction that offers a choice if the conditional jump is
//	 * triggered.
//	 */
//	protected int pcWithJump;
//	/**
//	 * The ConstraintExpression describing the current choice of the conditional jump.
//	 */
//	protected ConstraintExpression[] constraintExpressions;
//	
//	private Stack<TrailElement> trail = new Stack<TrailElement>();
//	
//	public DatabaseChoicePoint(Frame frame, int pc, int pcNext, int pcWithJump, ChoicePoint parent, ConstraintExpression... constraintExpressions) 
//			throws SolvingException {
//		this.number = 0;
//		this.frame = frame;
//		this.pc = pc;
//		this.pcNext = pcNext;
//		this.pcWithJump = pcWithJump;
//		this.constraintExpressions = constraintExpressions;
//		this.parent = parent;
//		
//		if(parent != null) {
//			this.number = parent.getNumber() + 1;
//		}
//		
//		// Add the ConstraintExpression.
//		SolverManager solverManager = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager();
//		for(ConstraintExpression constraintExpression : this.constraintExpressions) {
//			solverManager.addConstraint(constraintExpression);
//		}
//	
//		try {
//			if (!solverManager.hasSolution()) {
//				System.out.println("-> hello you");
////				tryTheNegatedConstraint(solverManager, constraintExpression);
//			} else {
//				// Set the pc to the jump target.
//				this.frame.getVm().setPC(pcWithJump);
//			}
//		} catch (SolverUnableToDecideException e) {
//			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving lead to a SolverUnableToDecideException with message: " + e.getMessage());
////			tryTheNegatedConstraint(solverManager, constraintExpression);
////			this.alreadyVisitedNonJumpingBranch = true;
//		} catch (TimeoutException e) {
//			if (Globals.getInst().symbolicExecLogger.isTraceEnabled()) Globals.getInst().symbolicExecLogger.trace("Solving lead to a TimeoutException with message: " + e.getMessage());
////			tryTheNegatedConstraint(solverManager, constraintExpression);
////			this.alreadyVisitedNonJumpingBranch = true;
//		}
//	}
//	
//	
//	/**
//	 * Return this ChoicePoint's number.
//	 *
//	 * @return This ChoicePoint's number.
//	 */
//	@Override
//	public long getNumber() {
//		return this.number;
//	}
//
//	@Override
//	public boolean hasAnotherChoice() {
//		return false; // TODO: when?
//	}
//
//	@Override
//	public void changeToNextChoice() throws MugglException {
//		// TODO
//	}
//
//	@Override
//	public Frame getFrame() {
//		return this.frame;
//	}
//
//	@Override
//	public int getPc() {
//		return this.pc;
//	}
//
//	@Override
//	public int getPcNext() {
//		return this.pcNext;
//	}
//
//	@Override
//	public ChoicePoint getParent() {
//		return null;
//	}
//
//	@Override
//	public boolean changesTheConstraintSystem() {
//		return true;
//	}
//
//	@Override
//	public ConstraintExpression getConstraintExpression() {
//		return this.constraintExpression;
//	}
//
//	@Override
//	public void setConstraintExpression(ConstraintExpression constraintExpression) {
//		this.constraintExpression = constraintExpression;
//	}
//
//	@Override
//	public boolean hasTrail() {
//		return true;
//	}
//
//	@Override
//	public Stack<TrailElement> getTrail() {
//		return this.trail;
//	}
//
//	@Override
//	public void addToTrail(TrailElement element) {
//		this.trail.push(element);
//	}
//
//	@Override
//	public boolean enforcesStateChanges() {
//		return false; // TODO: or true?
//	}
//
//	@Override
//	public void applyStateChanges() {
//		// does nothing
//	}
//
//	@Override
//	public String getChoicePointType() {
//		return "database choice point";
//	}
//
//}
