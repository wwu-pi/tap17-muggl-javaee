package de.wwu.muggl.symbolic.searchAlgorithms.choice.jpa;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.list.ISymbolicResultList;
import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericEqual;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.jpa.meta.GetResultListOptions;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.var.sym.SymbolicQueryResultList;

public class GetResultListChoicePoint implements ChoicePoint {

	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;
	
	protected Queue<GetResultListOptions> options;
	
	protected QLStatement<?> qlStatement;
	
	// the virtual machine
	protected JPAVirtualMachine vm;
	
	// entity objects in the required data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> requiredObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> dataObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> resultQueryListIds;
	
	protected SymbolicQueryResultList queryResultList;
	
	public GetResultListChoicePoint(
			Frame frame, 
			int pc, 
			int pcNext, 
			ChoicePoint parent,
			QLStatement<?> qlStatement) {
		
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		this.qlStatement = qlStatement;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		this.options = new LinkedList<>();
		
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		
		if(!(frame.getVm() instanceof JPAVirtualMachine)) {
			throw new RuntimeException("Cannot create choice point to find JPA entity if not JPA Virtual Machine is started");
		}
		this.vm = (JPAVirtualMachine)frame.getVm();
		
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
		
		createChoicePointOptions();
		
		
		try {
			String queryResultName = "symbolic.result.list#"+qlStatement.hashCode();
			this.queryResultList = new SymbolicQueryResultList(queryResultName, (JPAVirtualMachine)frame.getVm(), qlStatement);
		} catch (SymbolicObjectGenerationException e1) {
			throw new RuntimeException("Error while geneating symbolic result list: " + e1.getMessage(), e1);
		}
		
		
	}

	private void createChoicePointOptions() {
		options.add(GetResultListOptions.EMPTY_RESULT_LIST);
		options.add(GetResultListOptions.FILLED_RESULT_LIST);
	}
	
	public boolean hasAnotherChoice() {
		return options.size() > 0;
	}
	
	@Override
	public void applyStateChanges() throws VmRuntimeException {
		GetResultListOptions state = options.poll();

		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData(), this.requiredObjectIds);
		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getData(), this.dataObjectIds);
		resetSymbolicQueryListDatabase();
		
		switch(state) {
			case EMPTY_RESULT_LIST: applyEmptyResultList(); return;
			case FILLED_RESULT_LIST: applyFilledResultList(); return;
		}

	}
	
	private void applyFilledResultList() throws VmRuntimeException {
		try {
			this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(queryResultList.getSymbolicLength(), NumericConstant.getZero(Expression.INT)));
			this.vm.getVirtualObjectDatabase().addQueryResultList(queryResultList);
			this.queryResultList.generateAndAddNewElement(this.vm);
			this.frame.getOperandStack().push(queryResultList);
		} catch(Exception e) {
			throw new VmRuntimeException(null);
		}
	}

	private void applyEmptyResultList() throws VmRuntimeException {
		try {
			this.vm.getSolverManager().addConstraint(NumericEqual.newInstance(queryResultList.getSymbolicLength(), NumericConstant.getZero(Expression.INT)));
			this.vm.getVirtualObjectDatabase().addQueryResultList(queryResultList);
			frame.getOperandStack().push(queryResultList);
		} catch(Exception e) {
			throw new VmRuntimeException(null);
		}
	}

	private void resetSymbolicDatabase(Map<String, Set<DatabaseObject>> data, Set<String> allowedObjectIds) {
		for(String e : data.keySet()) {
			Set<DatabaseObject> e_data = data.get(e);
			Set<DatabaseObject> remove_e_data = new HashSet<>(); 
			for(DatabaseObject d : e_data) {
				if(!allowedObjectIds.contains(d.getObjectId())) {
					remove_e_data.add(d);
				}
			}
			for(DatabaseObject d : remove_e_data) {
				e_data.remove(d);
			}
		}
	}
	
	private void resetSymbolicQueryListDatabase() {
		Set<ISymbolicResultList> set = this.vm.getVirtualObjectDatabase().getSymbolicQueryResultLists();

		Set<ISymbolicResultList> removeLists = new HashSet<>(); 
		for(ISymbolicResultList l : set) {
			if(!this.resultQueryListIds.contains(l.getId())) {
				removeLists.add(l);
			}
		}
		
		for(ISymbolicResultList l : removeLists) {
			set.remove(l);
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
		return "Symbolic Query Result List Choice Point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
