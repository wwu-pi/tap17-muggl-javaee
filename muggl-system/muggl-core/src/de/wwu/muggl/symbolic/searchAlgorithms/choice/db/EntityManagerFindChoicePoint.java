//package de.wwu.muggl.symbolic.searchAlgorithms.choice.db;
//
//import java.util.Stack;
//
//import de.wwu.muggl.configuration.MugglException;
//import de.wwu.muggl.solvers.SolverManager;
//import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
//import de.wwu.muggl.solvers.exceptions.TimeoutException;
//import de.wwu.muggl.solvers.expressions.ConstraintExpression;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.EquationViolationException;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.SolvingException;
//import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
//import de.wwu.muggl.vm.Frame;
//import de.wwu.muggl.vm.impl.jpa.db.NewVirtualDatabase;
//import de.wwu.muggl.vm.impl.jpa.db.VirtualDatabase;
//import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
//
//public class EntityManagerFindChoicePoint implements ChoicePoint {
//	
//	protected boolean alreadyVisitedNonJumpingBranch = false;
//	protected Stack<TrailElement> trail = new Stack<TrailElement>();
//	protected Frame frame;
//	protected long number;
//	protected ChoicePoint parent = null;
//	protected int pc;
//	protected int pcNext;
//	protected NewVirtualDatabase database;
//	protected ConstraintExpression constraintExpression;
//
//	public EntityManagerFindChoicePoint(Frame frame, int pc, int pcNext, ConstraintExpression constraintExpression, NewVirtualDatabase database, ChoicePoint parent)
//		throws EquationViolationException, SolvingException {
//		this.frame = frame;
//		this.pc = pc;
//		this.pcNext = pcNext;
//		this.parent = parent;
//		this.constraintExpression = constraintExpression;
//		this.number = 0;
//		this.database = database;
//		
//		createChoicePoint();
//		
//	}
//	
//	protected void createChoicePoint() throws EquationViolationException, SolvingException {
//		if(this.parent != null) {
//			this.number = parent.getNumber() + 1;
//		}
//		
//		SolverManager solverManager = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager();		
//		for(ConstraintExpression dbConstraint : this.database.generateDBConstraints()) {
//			solverManager.addConstraint(dbConstraint);
//		}
//		
//		solverManager.addConstraint(constraintExpression);
//		
//		try {
//			if (!solverManager.hasSolution()) {
//				tryTheNegatedConstraint(solverManager);
//			} else {
//				// TODO: was dann?
//				System.out.println("was jetzt?");
//			}
//		} catch (SolverUnableToDecideException e) {
//			tryTheNegatedConstraint(solverManager);
//			this.alreadyVisitedNonJumpingBranch = true;
//		} catch (TimeoutException e) {
//			tryTheNegatedConstraint(solverManager);
//			this.alreadyVisitedNonJumpingBranch = true;
//		}
//	}
//	
//	protected void tryTheNegatedConstraint(SolverManager solverManager) throws EquationViolationException, SolvingException {
//		solverManager.removeConstraint();
//		for(ConstraintExpression dbConstraint : this.database.generateDBConstraints()) {
//			solverManager.addConstraint(dbConstraint);
//		}
//		solverManager.addConstraint(constraintExpression.negate());
//		try {
//			if (solverManager.hasSolution()) {
//				this.frame.getVm().setPC(this.pcNext);
//				this.alreadyVisitedNonJumpingBranch = true;
//			} else {
//				throw new EquationViolationException("Cannot continue with this choice point, since equations are violated.");
//			}
//		} catch (SolverUnableToDecideException e) {
//			throw new SolvingException("Cannot continue with this choice point, since solving failed.");
//		} catch (TimeoutException e) {
//			throw new SolvingException("Cannot continue with this choice point, since solving failed.");
//		}
//	}
//	
//	@Override
//	public long getNumber() {
//		return this.number;
//	}
//
//	@Override
//	public boolean hasAnotherChoice() {
//		return !this.alreadyVisitedNonJumpingBranch;
//	}
//
//	@Override
//	public void changeToNextChoice() throws MugglException {
//		if (!this.alreadyVisitedNonJumpingBranch) {
//			this.alreadyVisitedNonJumpingBranch = true;
//			// Negate the constraint expression.
//			
//			// TODO: irgendwie andenen constaint schafen hier
//			this.constraintExpression = this.constraintExpression.negate();
//		}		
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
//		return this.parent;
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
//		return true;
//	}
//
//	@Override
//	public void applyStateChanges() {
//		this.frame.getOperandStack().pop();
//		this.frame.getOperandStack().push(null);
//	}
//
//	@Override
//	public String getChoicePointType() {
//		return "JPA EntityManager#find Choice Point";
//	}
//
//}
