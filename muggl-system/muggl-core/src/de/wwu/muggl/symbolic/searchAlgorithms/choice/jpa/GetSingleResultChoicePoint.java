package de.wwu.muggl.symbolic.searchAlgorithms.choice.jpa;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.hibernate.sqm.query.expression.domain.SqmEntityBinding;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.select.SqmSelectClause;
import org.hibernate.sqm.query.select.SqmSelection;

import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.list.ISymbolicResultList;
import de.wwu.muggl.jpa.ql.stmt.QLStatement;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.jpa.meta.GetSingleResultOptions;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.exceptions.NoExceptionHandlerFoundException;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.sym.SymbolicQueryResultList;
import de.wwu.muggl.vm.var.sym.gen.SymbolicQueryResultElementGenerator;

public class GetSingleResultChoicePoint implements ChoicePoint {

	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;

	protected Queue<GetSingleResultOptions> options;
	
	protected QLStatement<?> qlStatement;
	
	// the virtual machine
	protected JPAVirtualMachine vm;
	
	// entity objects in the required data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> requiredObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> dataObjectIds;
	
	// entity objects in the application data that the symbolic object db has BEFORE this choice point makes any changes to it
	protected Set<String> resultQueryListIds;	

	public GetSingleResultChoicePoint(
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
	}


	private void createChoicePointOptions() {
		this.options.add(GetSingleResultOptions.RESULT);
	}
	
	public boolean hasAnotherChoice() {
		return options.size() > 0;
	}
	
	@Override
	public void applyStateChanges() throws VmRuntimeException {
		GetSingleResultOptions state = options.poll();

		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData(), this.requiredObjectIds);
		resetSymbolicDatabase(this.vm.getVirtualObjectDatabase().getData(), this.dataObjectIds);
		resetSymbolicQueryListDatabase();
		
		switch(state) {
			case RESULT: applyResultExists(); return;
			default: throw new RuntimeException("Not implemented yet");
		}
	}
	
	private void applyResultExists() throws VmRuntimeException {
		if(qlStatement.getSqmStatement().getQuerySpec().getSelectClause() != null) {
			List<SqmSelection> selectionList = qlStatement.getSqmStatement().getQuerySpec().getSelectClause().getSelections();
			if(selectionList.size() == 1) {
				if(selectionList.get(0).getExpression() instanceof CountStarFunctionSqmExpression) {					
					try {
						ClassFile longCF = vm.getClassLoader().getClassAsClassFile("java.lang.Long");
						Objectref countRef = vm.getAnObjectref(longCF);
						Field valueField = longCF.getFieldByName("value");
						NumericVariable symoblicCountSize = new NumericVariable("SYM_QRY_RES_COUNT.value", Expression.INT);
						this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(symoblicCountSize, NumericConstant.getZero(Expression.INT)));
						countRef.putField(valueField, symoblicCountSize);
						this.frame.getOperandStack().push(countRef);
						this.vm.addCountSingleResult(qlStatement, symoblicCountSize);
						return;
					} catch(Exception e) {
						throw new RuntimeException("error while generating count object reference result");
					}
				}
			}
			
			
		}
		throw new RuntimeException("single query result exist for given expression type not handled yet");

//		
//		SqmSelectClause selectClause = this.qlStatement.getSqmStatement().getQuerySpec().getSelectClause();
//		
//		try {
//			if(selectClause != null && selectClause.getSelections().size() == 1) {
//				SqmSelection selection = selectClause.getSelections().get(0);
//				if(selection.getExpression() instanceof SqmEntityBinding) {
//					SqmEntityBinding e = (SqmEntityBinding)selection.getExpression();
//					String entityName = e.getBoundNavigable().getEntityName();
//					applyEntityExists(entityName);					
//					return;
//				}
//			}
//		} catch(Exception e) {
//		}
//		this.frame.getOperandStack().push(null);	
	}
	
	
	private void applyEntityExists(String entityName) throws Exception {
		try {
			SymoblicEntityFieldGenerator entityFieldGenerator = new SymoblicEntityFieldGenerator(this.vm);
			ClassFile entityClassFile = this.vm.getClassLoader().getClassAsClassFile(entityName);

			// generate a new entity object reference for required data
			Objectref req_entityObjectref = this.vm.getAnObjectref(entityClassFile);
			long req_number = req_entityObjectref.getInstantiationNumber();
			String req_entityObjectrefName = "REQ#entity-object#"+entityClassFile.getClassName()+req_number;
			EntityObjectref req_entityObject = new EntityObjectref(entityFieldGenerator, req_entityObjectrefName, req_entityObjectref);
			
			// generate a new entity object reference for operating data
			Objectref data_entityObjectref = this.vm.getAnObjectref(entityClassFile);
			long data_number = data_entityObjectref.getInstantiationNumber();
			String data_entityObjectrefName = "DATA#entity-object#"+entityClassFile.getClassName()+data_number;
			EntityObjectref data_entityObject = new EntityObjectref(entityFieldGenerator, data_entityObjectrefName, data_entityObjectref, req_entityObject);
			
			String idFieldName = EntityConstraintAnalyzer.getIdFieldName(entityName);
			Field idField = entityClassFile.getFieldByName(idFieldName);
			Object idValue1 = req_entityObject.getField(idField);
			if(idValue1 != null && idValue1 instanceof NumericVariable) {
				this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance((NumericVariable)idValue1, NumericConstant.getInstance(0, Expression.INT)));
			}
			Object idValue2 = data_entityObject.getField(idField);
			if(idValue2 != null && idValue2 instanceof NumericVariable) {
				this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance((NumericVariable)idValue2, NumericConstant.getInstance(0, Expression.INT)));
			}
		
			this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(this.vm.getSolverManager(), entityName, req_entityObject);
			this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), entityName, data_entityObject);
			
			this.frame.getOperandStack().push(data_entityObject);
		} catch(Exception e) {
			throw new RuntimeException("Error while generating entity objects to find in database");
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
		return "Symbolic Single Result Choice Point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
	
}
