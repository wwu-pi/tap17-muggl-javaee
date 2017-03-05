package de.wwu.muggl.symbolic.searchAlgorithms.choice.list;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.list.ISymbolicResultList;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.ReferenceArrayListVariable;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.sym.ISymbolicList;
import de.wwu.muggl.vm.var.sym.SymbolicIterator;

public class SymbolicIterationChoicePoint implements ChoicePoint {

	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;
	
	protected SymbolicIterator iterator;
	
	// the virtual machine
	protected JPAVirtualMachine vm;
	
	// entity objects in the required data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> requiredObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> dataObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> resultQueryListIds;
	
	private boolean changed;
	
	public SymbolicIterationChoicePoint(Frame frame, 
			int pc, 
			int pcNext, 
			ChoicePoint parent,
			SymbolicIterator iterator) {
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		this.iterator = iterator;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();

		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		
		this.vm = this.iterator.getSymbolicList().getVM();
				
		this.requiredObjectIds = new HashSet<>();
		Map<String, Set<DatabaseObject>> reqData = this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData();
		for(String e : reqData.keySet()) {
			for(DatabaseObject d : reqData.get(e)) {
				this.requiredObjectIds.add(d.getObjectId());
			}
		}
		
		this.dataObjectIds = new HashSet<>();
		Map<String, Set<DatabaseObject>> data = this.vm.getVirtualObjectDatabase().getData();
		for(String e : data.keySet()) {
			for(DatabaseObject d : data.get(e)) {
				this.dataObjectIds.add(d.getObjectId());
			}
		}
		
		this.resultQueryListIds = new HashSet<>();
		for(ISymbolicResultList symResList : this.vm.getVirtualObjectDatabase().getSymbolicQueryResultLists()) {
			this.resultQueryListIds.add(symResList.getId());
		}
		
		
		// first choice: no more elements...
		ConstraintExpression ce = GreaterThan.newInstance(NumericConstant.getInstance(
				(this.iterator.getCounter()+1), Expression.INT), 
				this.iterator.getSymbolicList().getSymbolicLength());
		this.vm.getSolverManager().addConstraint(ce);

		try {
			boolean hasSolution = this.vm.getSolverManager().hasSolution();
			if(!hasSolution) {
				this.vm.getSolverManager().removeConstraint();
			} else {
				frame.getOperandStack().push(NumericConstant.getInstance(0, Expression.INT));
			}
		} catch (Exception e) {
			this.vm.getSolverManager().removeConstraint();
		}
	}
	
	public boolean hasAnotherChoice() {
		return !this.changed;
	}
	
	@Override
	public void applyStateChanges() throws VmRuntimeException {
		// next choice: more elements...
		this.changed = true;
		ConstraintExpression ce = GreaterOrEqual.newInstance(this.iterator.getSymbolicList().getSymbolicLength(), NumericConstant.getInstance((this.iterator.getCounter()+1), Expression.INT));
		this.vm.getSolverManager().addConstraint(ce);
		Objectref newListElement = this.iterator.getSymbolicList().generateNewElement();
		//add constraint when entity
		ISymbolicList symList = this.iterator.getSymbolicList();
		symList.addElement(newListElement);
		
//		if(symList instanceof ReferenceArrayListVariable) {
//			ReferenceArrayListVariable entityArraylist = (ReferenceArrayListVariable)symList;
//			if(entityArraylist.getEntityOwner() != null && entityArraylist.getEntityOwner().getRequiredEntity() != null) {
//				Object requiredArraylist = entityArraylist.getEntityOwner().getRequiredEntity().getField(entityArraylist.getListField());
//				if(requiredArraylist instanceof ReferenceArrayListVariable) {
//					ReferenceArrayListVariable refVarArrayList = (ReferenceArrayListVariable)requiredArraylist;
//					refVarArrayList.addElement(newListElement);
//				}
//			}
//			entityArraylist.getResultList().add(newListElement);
//		}
		
		try {
			boolean hasSolution = this.vm.getSolverManager().hasSolution();
			if(!hasSolution) {
				this.vm.getSolverManager().removeConstraint();
			} else {
				this.frame.getOperandStack().pop(); // pop old reference
				this.frame.getOperandStack().push(NumericConstant.getInstance(1, Expression.INT));
			}
		} catch (Exception e) {
			this.vm.getSolverManager().removeConstraint();
		}
	}
	
	private Objectref generateNewListElement() {
		String type = this.iterator.getSymbolicList().getCollectionType();
		try {
			ClassFile classFile = this.vm.getClassLoader().getClassAsClassFile(type);
			if(classFile.isAccInterface()) {
				classFile = this.vm.getClassLoader().getClassAsClassFile(type+"Impl");
			}
			
			return new ReferenceVariable(
					"SymbolicList-"+this.iterator.getSymbolicList().hashCode()+".element#"+this.iterator.getCounter(),
					this.vm.getAnObjectref(classFile), this.vm);
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public long getNumber() {
		return this.number;
	}
	
	@Override
	public void changeToNextChoice() throws NoExceptionHandlerFoundException {
		
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
		return true;
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
		return "Symbolic Iterator Choice Point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
	
}
