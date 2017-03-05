//package de.wwu.muggl.symbolic.searchAlgorithms.choice.db;
//
//import java.util.Stack;
//
//import de.wwu.muggl.solvers.expressions.ConstraintExpression;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
//import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
//import de.wwu.muggl.vm.Frame;
//import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
//import de.wwu.muggl.vm.impl.jpa.db.NewVirtualDatabase;
//
//public class EMCreateQueryCP implements ChoicePoint {
//	
//	protected boolean alreadyVisitedNonJumpingBranch = false;
//	protected Stack<TrailElement> trail = new Stack<TrailElement>();
//	protected Frame frame;
//	protected int pc;
//	protected int pcNext;
//	protected ChoicePoint parent;
//	protected long number;
//	
//	private NewVirtualDatabase database;
//	
//	
//	public EMCreateQueryCP(Frame frame, int pc, int pcNext, ChoicePoint parent, String entityName, Variable idVariable) {
//		this.frame = frame;
//		this.pc = pc;
//		this.pcNext = pcNext;
//		this.parent = parent;
//		this.number = 0;
//		
//		this.database = ((JPAVirtualMachine)frame.getVm()).getVirtualDatabase().getClone();
//		
//		createChoicePoint();
//	}
//	
//	protected void createChoicePoint() {
//		System.out.println("******************");
//		System.out.println(" -> Choice Point: an entity=" + entityName + " must exist with id=" + idVariable);
//		System.out.println("******************");
//		
//	}
//	
//	
//	@Override
//	public void changeToNextChoice() {
//		System.out.println("******************");
//		System.out.println(" -> Change the coice point: reset db to state before, and now:");
//		System.out.println("******************");
//		this.alreadyVisitedNonJumpingBranch = true;
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
//		return false;
//	}
//	
//	@Override
//	public ConstraintExpression getConstraintExpression() {
//		return null;
//	}
//
//	@Override
//	public void setConstraintExpression(ConstraintExpression constraintExpression) {
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
////		frame.getOperandStack().pop();
////		frame.getOperandStack().push(null);
////		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(this.database);
//		System.out.println("*************************");
//		System.out.println(" -> BEFORE changeing the choice point, reset DB");
//		System.out.println("*************************");
//	}
//
//	@Override
//	public String getChoicePointType() {
//		return "JPA EntityManager#createQuery Choice Point";
//	}
//	
//
//}
