//package de.wwu.muggl.symbolic.searchAlgorithms.choice.jpa;
//
//import java.util.Stack;
//
//import de.wwu.muggl.jpa.FindResult;
//import de.wwu.muggl.solvers.expressions.ConstraintExpression;
//import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
//import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
//import de.wwu.muggl.vm.Frame;
//import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
//import de.wwu.muggl.vm.impl.jpa.db.NewVirtualDatabase;
//
//public class JPAFindResultNullJumpCP implements ChoicePoint {
//
//	protected boolean alreadyVisitedNonJumpingBranch = false;
//	protected Stack<TrailElement> trail = new Stack<TrailElement>();
//	protected Frame frame;
//	protected int pc;
//	protected int pcNext;
//	protected ChoicePoint parent;
//	protected long number;
//	protected FindResult findResult;
//	
//	private NewVirtualDatabase database;
//	
//	public JPAFindResultNullJumpCP(Frame frame, int pc, int pcNext, ChoicePoint parent, FindResult findResult) {
//		this.frame = frame;
//		this.pc = pc;
//		this.pcNext = pcNext;
//		this.parent = parent;
//		this.number = 0;
//		this.findResult = findResult;
//		
//		this.database = ((JPAVirtualMachine)frame.getVm()).getVirtualDatabase().getClone();
//		
//		createChoicePoint();
//	}
//
//	private void createChoicePoint() {
//		System.out.println("***************************************************");
//		System.out.println("*** findResult: " + findResult + " must EXIST in DB");
//		System.out.println("***************************************************");
//		
//		NewVirtualDatabase newVDB = this.database.getClone();
//		newVDB.addEntityObject(this.findResult.getEntityName(), this.findResult);
//		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(newVDB);
//	}
//	
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
//	public void changeToNextChoice() {
//		System.out.println("***************************************************");
//		System.out.println("*** change to next choice");
//		System.out.println("***************************************************");
//		
//		this.alreadyVisitedNonJumpingBranch = true;
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
//		System.out.println("***************************************************");
//		System.out.println("*** apply state changes");
//		System.out.println("***************************************************");
//		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(this.database);
//	}
//
//	@Override
//	public String getChoicePointType() {
//		return "JPA Find Result Choice Point";
//	}
//	
//	
//}
