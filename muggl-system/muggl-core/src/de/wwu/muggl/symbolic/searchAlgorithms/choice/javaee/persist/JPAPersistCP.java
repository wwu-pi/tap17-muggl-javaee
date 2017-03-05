package de.wwu.muggl.symbolic.searchAlgorithms.choice.javaee.persist;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.wwu.muggl.configuration.MugglException;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.javaee.AbstractJavaEEChoicePoint;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Constant;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.ElementValueClass;
import de.wwu.muggl.vm.exceptions.VmRuntimeException;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.var.EntityObjectref;

public class JPAPersistCP extends AbstractJavaEEChoicePoint {
	
	protected Queue<PersistOptions> options = new LinkedList<>();
	protected PersistOptions nextChoice;
	protected Objectref objectToPersist;
	
	public JPAPersistCP(Frame frame, int pc, int pcNext, ChoicePoint parent, Objectref objectToPersist) throws VmRuntimeException {
		super(frame, pc, pcNext, parent);
		
		this.objectToPersist = objectToPersist;
		
		if(entityAlreadyExists()) {
			// throw new persistence exception: entity already exists
			throw new VmRuntimeException(frame.getVm().generateExc("javax.persistence.PersistenceException"));
		}
		
		createOptions();
		this.nextChoice = this.options.poll();
	}

	private void createOptions() {
		
		if(!isGeneratedIdAttribute(this.objectToPersist.getInitializedClass().getClassFile())) {
			this.options.add(PersistOptions.ENTITY_ALREADY_EXISTS);
		}
		this.options.add(PersistOptions.ENTITY_DOES_NOT_EXIST);
	}

	private boolean entityAlreadyExists() {
		Set<DatabaseObject> reqSet = this.vm.getVirtualObjectDatabase().getPreExecutionRequiredData().get(this.objectToPersist.getObjectType());
		Set<DatabaseObject> dataSet = this.vm.getVirtualObjectDatabase().getData().get(this.objectToPersist.getObjectType());
		
		Field idField = getIdField(this.objectToPersist.getInitializedClass().getClassFile());
		String idFieldName = idField.getName();
		
		if(reqSet != null) {
			for(DatabaseObject dbObj : reqSet) {
				Object idValue = dbObj.valueMap().get(idFieldName);
				if(idValue != null && idValue.equals(this.objectToPersist.getField(idField))) {
					return true;
				}
			}
		}
		
		if(dataSet != null) {
			for(DatabaseObject dbObj : dataSet) {
				Object idValue = dbObj.valueMap().get(idFieldName);
				if(idValue != null && idValue.equals(this.objectToPersist.getField(idField))) {
					return true;
				}
			}
		}
		
		return false;
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
			case ENTITY_ALREADY_EXISTS: applyEntityAlreadyExists(); return;
			case ENTITY_DOES_NOT_EXIST: applyEntityDoesNotExist(); return;
		}
	}

	private void applyEntityAlreadyExists() throws VmRuntimeException {
		
		String entityName = this.objectToPersist.getObjectType();
		
		// generate new required entity object
		ClassFile classFile = this.objectToPersist.getInitializedClass().getClassFile();
		Objectref requiredEntityObjectref = this.vm.getAnObjectref(classFile);
		EntityObjectref requiredEntityObject = new EntityObjectref(entityFieldGenerator, entityName, requiredEntityObjectref);
		
		// add the entity to the symbolic object store
		this.vm.getVirtualObjectDatabase().addPreExecutionRequiredData(this.vm.getSolverManager(), entityName, requiredEntityObject);

		this.entityConstraints.addStaticConstraints(requiredEntityObject);		
		
		// link the id fields of the required entity object and the object-to-persist
		Field idField = getIdField(classFile);
		Object idFieldValueObject2Persist = this.objectToPersist.getField(idField);
		Object idFieldValueRequiredEntity = requiredEntityObject.getField(idField);
		addEqualConstraint(idFieldValueObject2Persist, idFieldValueRequiredEntity);
		
		// throw new persistence exception: entity already exists
		throw new VmRuntimeException(frame.getVm().generateExc("javax.persistence.PersistenceException"));
	}
	
	private void applyEntityDoesNotExist() throws VmRuntimeException {
		
		String entityName = this.objectToPersist.getObjectType();
		
		ClassFile entityClassFile = null;
		try {
			entityClassFile = this.vm.getClassLoader().getClassAsClassFile(entityName);
		} catch (ClassFileException e) {
			throw new RuntimeException("coult not generate persist choice point", e);
		}
		
		// generate a new entity object reference for operating data
		Objectref data_entityObjectref = this.vm.getAnObjectref(entityClassFile);
		long data_number = data_entityObjectref.getInstantiationNumber();
		String data_entityObjectrefName = "DATA#entity-object#"+entityClassFile.getClassName()+data_number;
		EntityObjectref data_entityObject = new EntityObjectref(entityFieldGenerator, data_entityObjectrefName, data_entityObjectref);
		
		for(String attributeName : this.objectToPersist.valueMap().keySet()) {
			Field f = entityClassFile.getFieldByName(attributeName, true);
			Object o = this.objectToPersist.getField(f);
			data_entityObject.putField(f, o);
		}
		
		this.entityConstraints.addStaticConstraints(data_entityObject);
		
		this.vm.getVirtualObjectDatabase().addEntityData(this.vm.getSolverManager(), entityName, data_entityObject);
	}

	@Override
	public String getChoicePointType() {
		return "JPA Persist Choice Point";
	}

}
