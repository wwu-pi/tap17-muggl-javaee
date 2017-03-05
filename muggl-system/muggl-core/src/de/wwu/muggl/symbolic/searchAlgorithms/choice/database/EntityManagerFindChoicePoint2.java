package de.wwu.muggl.symbolic.searchAlgorithms.choice.database;

import java.util.Set;
import java.util.Stack;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.SolvingException;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.execution.ExecutionException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.exceptions.SymbolicExceptionHandler;
import de.wwu.muggl.vm.initialization.ObjectNullRef;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;

public class EntityManagerFindChoicePoint2 implements ChoicePoint {

	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;
	
//	private EntityManagerFindChoices choices;
	
	private EntityReferenceGenerator entityRefGenerator;
	
	// this is the entity reference that is to be found / not found 
	private ReferenceVariable entityReference;
	
	// this is the id field of the entity reference;
	private String idFieldName;
	
	protected boolean alreadyVisitedNonJumpingBranch = false;
	
	// the virtual machine
	protected JPAVirtualMachine vm;
	
	// data of the entity to be found
	protected String entityName; // the name of the entity that is to be found
	protected ReferenceVariable idReference; // the reference to the id field of the entity to be found
	
	public EntityManagerFindChoicePoint2(Frame frame, int pc, int pcNext, ChoicePoint parent, String entityName, ReferenceVariable idReference) throws VmRuntimeException, SolvingException {
		System.out.println(">>>>>> CHOICE POINT: EntityManagerFindChoicePoint  <<<<<<<<");
		
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		this.entityName = entityName;
		this.idReference = idReference;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		if(!(frame.getVm() instanceof JPAVirtualMachine)) {
			throw new RuntimeException("Cannot create choice point to find JPA entity if not JPA Virtual Machine is started");
		}
		this.vm = (JPAVirtualMachine)frame.getVm();
		
		// add special constraints on id reference variable
		if(idReference.getInitializedClassName().equals("java.lang.String")) {
			// for a string: the id must have at least one character in value array
			Field valueString = idReference.getInitializedClass().getClassFile().getFieldByName("value");
			SymbolicArrayref symArray = (SymbolicArrayref)idReference.getField(valueString);
			ConstraintExpression staticConstraint = GreaterOrEqual.newInstance(symArray.getSymbolicLength(), NumericConstant.getOne(Expression.INT));
//			this.vm.getSolverManager().addStaticConstraint(staticConstraint);
			this.vm.getSolverManager().addConstraint(staticConstraint);
		}
				
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		

		
		this.vm = (JPAVirtualMachine)frame.getVm();
		
		this.entityRefGenerator = new EntityReferenceGenerator(vm);
		
		entityMustExistInDatabase();
		
//		constructChoicePoint();
	}
	
//	protected void constructChoicePoint() {
//		switch(choices) {
//			case ENTITY_EXISTS : entityMustExistInDatabase(); return;
//			case ENTITY_NOT_EXISTENT : entityMustNotExistInDatabase(); return;
//			case ID_NULL_REFERENCE : entityIdReferenceMustBeNullReference(); return;
//		}
//	}
	
	private void entityMustExistInDatabase() {
		this.entityReference = entityRefGenerator.generateNewEntityReference(this.entityName);
		this.idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityReference.getInitializedClassName());
		Field idField = entityReference.getInitializedClass().getClassFile().getFieldByName(idFieldName);
		entityReference.putField(idField, this.idReference);
		this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(this.vm.getSolverManager(), entityReference.getInitializedClassName(), entityReference);
		this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), entityReference.getInitializedClassName(), entityReference);
		this.frame.getOperandStack().push(entityReference);
	}
	
	private void entityMustNotExistInDatabase() {
		Object idFieldValue = this.entityReference.valueMap().get(this.idFieldName);
		// TODO: der wert ovn idFieldValue darf in den pre-execution required data nicht vorkommen
		Set<DatabaseObject> data = this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData().get(this.entityName);
		data.remove(this.entityReference);
		ReferenceVariable oldRefVar = (ReferenceVariable) this.frame.getOperandStack().pop();
		this.frame.getOperandStack().push(new ObjectNullRef(oldRefVar.getInitializedClass()));
	}
	
	private void entityIdReferenceMustBeNullReference() throws VmRuntimeException {
		System.out.println("c");
//		((SymbolicVirtualMachine)frame.getVm()).setDoNotTryToTrackBack(true);
		throw new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalArgumentException"));
	}
	
	@Override
	public long getNumber() {
		return this.number;
	}

	@Override
	public boolean hasAnotherChoice() {
		return !this.alreadyVisitedNonJumpingBranch;
//		return !this.visitedEntityExistBranch || !this.visitedEntityNotExistBranch || !this.visitedIdNullReferenceBranch;
	}
	
	@Override
	public void applyStateChanges() {
		System.out.println("do something");
		entityMustNotExistInDatabase();
		
//		if(!this.visitedEntityExistBranch) {
//			entityMustExistInDatabase();
//		} else if(!this.visitedEntityNotExistBranch) {
//			entityMustNotExistInDatabase();
//		} else if(!this.visitedIdNullReferenceBranch) {
//			entityIdReferenceMustBeNullReference();
//		}
	}
	
	@Override
	public void changeToNextChoice() throws NoExceptionHandlerFoundException {
		this.alreadyVisitedNonJumpingBranch = true;
		
//		throw new NoExceptionHandlerFoundException(null, null, 1);
//		
//		VmRuntimeException e = new VmRuntimeException(frame.getVm().generateExc("java.lang.IllegalArgumentException"));
//		SymbolicExceptionHandler handler = new SymbolicExceptionHandler(frame, e);
//		try {
//			handler.handleException();
//		} catch (ExecutionException e2) {
////			executionFailedSymbolically(e2);
//		}
	}
	
	@Override
	public Frame getFrame() {
		return this.frame;
	}

	@Override
	public int getPc() {
		return this.pc;
	}

	@Override
	public int getPcNext() {
		return this.pcNext;
	}

	@Override
	public ChoicePoint getParent() {
		return this.parent;
	}
	
	@Override
	public boolean changesTheConstraintSystem() {
		return false;
	}

	@Override
	public ConstraintExpression getConstraintExpression() {
		return null;
	}

	@Override
	public void setConstraintExpression(ConstraintExpression constraintExpression) {
	}

	@Override
	public boolean hasTrail() {
		return true;
	}

	@Override
	public Stack<TrailElement> getTrail() {
		return this.trail;
	}

	@Override
	public void addToTrail(TrailElement element) {
		this.trail.push(element);
	}

	@Override
	public boolean enforcesStateChanges() {
		return true;
	}
	
	@Override
	public String getChoicePointType() {
		return "JPA EntityManager#find Choice Point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
	
}
