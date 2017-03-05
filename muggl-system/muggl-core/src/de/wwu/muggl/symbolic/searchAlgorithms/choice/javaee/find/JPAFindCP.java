package de.wwu.muggl.symbolic.searchAlgorithms.choice.javaee.find;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.db.constraint.EntityConstraintAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.GreaterThan;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.exc.ExceptionThrowingChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.javaee.AbstractJavaEEChoicePoint;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.ObjectNullRef;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;
import de.wwu.muggl.vm.var.ReferenceVariable;
import de.wwu.muggl.vm.var.SymbolicArrayref;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;

public class JPAFindCP extends AbstractJavaEEChoicePoint {
	
	protected Queue<FindOptions> options = new LinkedList<>();
	protected FindOptions nextChoice;
	
	protected String entityName;
	protected Object idReference;
	
	protected Field idField;
	
	
	protected DatabaseObject alreadyExistingEntity;
	
	public JPAFindCP(Frame frame, int pc, int pcNext, ChoicePoint parent, String entityName, Object idReference) {
		super(frame, pc, pcNext, parent);
		this.entityName = entityName;
		this.idReference = idReference;
		
		
		ClassFile entityClassFile = null;
		try {
			entityClassFile = this.vm.getClassLoader().getClassAsClassFile(this.entityName);
		} catch (ClassFileException e) {
			throw new RuntimeException("coult not generate find choice point", e);
		}
		this.idField = getIdField(entityClassFile);
		
		
		
		createOptions();
		this.nextChoice = this.options.poll();
	}

	private void createOptions() {
		this.alreadyExistingEntity = getEntityWithSameIdentifier();
		if(this.alreadyExistingEntity != null) {
			this.options.add(FindOptions.ENTITY_ALREADY_EXIST);
		} else {
			this.options.add(FindOptions.ENTITY_DOES_EXIST);
			this.options.add(FindOptions.ENTITY_DOES_NOT_EXIST);
		}
	}

	private DatabaseObject getEntityWithSameIdentifier() {
		Set<DatabaseObject> data = this.vm.getVirtualObjectDatabase().getData(this.entityName);
		if(data == null) {
			return null;
		}
		
		
		for(DatabaseObject dbObj : data) {
			Object idVal = dbObj.valueMap().get(this.idField.getName());
			if(idVal != null) {
				// currently, check on same logic variable instance for equality
				if(idVal == this.idReference) {
					return dbObj;
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasAnotherChoice() {
		return this.options.size() >= 1;
	}

	@Override
	public void changeToNextChoice() throws MugglException {
		this.nextChoice = this.options.poll();
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
	public boolean enforcesStateChanges() {
		return true;
	}

	@Override
	public void doApplyChanges() throws VmRuntimeException {
		switch(this.nextChoice) {
			case ENTITY_ALREADY_EXIST: applyEntityAlreadyExists(); return;
			case ENTITY_DOES_EXIST: applyEntityDoesExists(); return;
			case ENTITY_DOES_NOT_EXIST: applyEntityDoesNotExist(); return;
		}
	}
	
	private void applyEntityAlreadyExists() {
		this.frame.getOperandStack().push(this.alreadyExistingEntity);
	}

	private void applyEntityDoesExists() throws VmRuntimeException {
		SymoblicEntityFieldGenerator entityFieldGenerator = new SymoblicEntityFieldGenerator(this.vm);
		ClassFile entityClassFile = null;
		try {
			entityClassFile = this.vm.getClassLoader().getClassAsClassFile(this.entityName);
		} catch (ClassFileException e) {
			throw new RuntimeException("coult not generate find choice point", e);
		}

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
		
		
		Field idField = getIdField(entityClassFile);
		
		req_entityObject.putField(idField, idReference);
		data_entityObject.putField(idField, idReference);
		
		if(idReference instanceof NumericVariable) {
			this.vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance((NumericVariable)idReference, NumericConstant.getInstance(0, Expression.INT)));
		}
		if(idReference instanceof ReferenceVariable && ((ReferenceVariable)idReference).getObjectType().equals("java.lang.String")) {
			ReferenceVariable refVar = (ReferenceVariable)idReference;
			Field valueField = refVar.getInitializedClass().getClassFile().getFieldByName("value");
			SymbolicArrayref symArray = (SymbolicArrayref)refVar.getField(valueField);
			this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(symArray.getSymbolicLength(), NumericConstant.getInstance(0, Expression.INT)));
		}
		
		this.entityConstraints.addStaticConstraints(req_entityObject);
		this.entityConstraints.addStaticConstraints(data_entityObject);
		
		this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(this.vm.getSolverManager(), this.entityName, req_entityObject);
		this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), this.entityName, data_entityObject);
		
		this.frame.getOperandStack().push(data_entityObject);
	}
	
	private void applyEntityDoesNotExist() throws VmRuntimeException {
		// apply constraints that id is not null, etc..
		if(this.idReference instanceof NumericVariable) {
			this.vm.getSolverManager().addConstraint(GreaterThan.newInstance((NumericVariable)this.idReference, NumericConstant.getZero(Expression.INT)));
		} else if(this.idReference instanceof ReferenceVariable && ((ReferenceVariable)this.idReference).getObjectType().equals("java.lang.String")) {
			Field valueField = ((ReferenceVariable)this.idReference).getInitializedClass().getClassFile().getFieldByName("value");
			Object valueValue = ((ReferenceVariable)this.idReference).getField(valueField);
			if(valueValue instanceof SymbolicArrayref) {
				this.vm.getSolverManager().addConstraint(GreaterThan.newInstance(((SymbolicArrayref)valueValue).getSymbolicLength(), NumericConstant.getZero(Expression.INT)));
			}
		}

		try {
			InitializedClass ic = this.vm.getClassLoader().getClassAsClassFile(entityName).getInitializedClass();
			this.frame.getOperandStack().push(new ObjectNullRef(ic));
		} catch (ClassFileException e) {
			throw new RuntimeException("coult not generate find choice point", e);
		}
		
	}

	@Override
	public String getChoicePointType() {
		return "JPA Find Choice Point";
	}
}
