package de.wwu.muggl.symbolic.searchAlgorithms.choice.db;

import java.lang.reflect.Field;
import java.util.Stack;

import javax.persistence.Id;

import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.db.ana.EntityAnalyzer;
import de.wwu.muggl.db.entry.EntityEntry;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.jpa.var.entity.EntityObjectReference;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.ObjectNullRef;
import de.wwu.muggl.vm.initialization.Objectref;

public class EMFindCP implements ChoicePoint {

	protected boolean alreadyVisitedNonJumpingBranch = false;
	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected ChoicePoint parent;
	protected long number;
	protected String entityName;
	protected Variable idVariable;
	protected int constraintLevel;

	protected JPAVirtualMachine vm;
	
	protected VirtualDatabase database;
	
	public EMFindCP(Frame frame, int pc, int pcNext, ChoicePoint parent, String entityName, Variable idVariable) {
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.parent = parent;
		this.number = 0;
		this.entityName = entityName;
		this.idVariable = idVariable;
		this.constraintLevel = ((SymbolicVirtualMachine) frame.getVm()).getSolverManager().getConstraintLevel();
		
		if (parent != null) {
			this.number = parent.getNumber() + 1;
		}
		
		if(!(frame.getVm() instanceof JPAVirtualMachine)) {
			throw new RuntimeException("Cannot create choice point to find JPA entity if not JPA Virtual Machine is started");
		}
		this.vm = (JPAVirtualMachine)frame.getVm();
		this.database = this.vm.getVirtualDatabase().getClone();
		
		createChoicePoint();
	}
	
	protected void createChoicePoint() {
		String idFieldName = getIdField(entityName);

		try {
			EntityEntry entityObject = EntityAnalyzer.getInitialEntityEntry(this.entityName);
			entityObject.addValue(idFieldName, this.idVariable);
			
			ClassFile entityClassFile = this.vm.getClassLoader().getClassAsClassFile(this.entityName);
			Objectref objectRef = this.vm.getAnObjectref(entityClassFile);//this.vm.getAFindResultObjectref(entityClassFile);
			
			for(Object value : entityObject.valueMap().values()) {
				if(value instanceof CollectionVariable) {
					CollectionVariable cv = (CollectionVariable)value;
					cv.setParent(objectRef);
				}
			}
			
			for(String fieldName : entityObject.valueMap().keySet()) {
				Object value = entityObject.valueMap().get(fieldName);
				if(value instanceof Variable) {
					value = (Variable) value;//TODO ((Variable) value).getClone();
				}
				de.wwu.muggl.vm.classfile.structures.Field field = entityClassFile.getFieldByName(fieldName);
				objectRef.putField(field, value);
			}
			
			VirtualDatabase newVDB = this.database.getClone();
			
			newVDB.addRequired(objectRef.getObjectId(), entityObject);
			newVDB.addData(objectRef);
			
			((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(newVDB);

			frame.getOperandStack().push(objectRef);
		} catch (Exception e) {
			throw new RuntimeException("Could not create choice point for #find", e);
		}
	}
	
	private String getIdField(String entityName) {
		try {
			Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass(entityName);
			for(Field field : clazz.getDeclaredFields()) {
				if(field.isAnnotationPresent(Id.class)) {
					return field.getName();
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Could not get id field of entity: " + entityName);
	}

	@Override
	public long getNumber() {
		return this.number;
	}

	@Override
	public boolean hasAnotherChoice() {
		return !this.alreadyVisitedNonJumpingBranch;
	}
	
	@Override
	public void changeToNextChoice() {
		this.alreadyVisitedNonJumpingBranch = true;
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
	public void applyStateChanges() {
		Objectref oldRef = (Objectref)frame.getOperandStack().pop();
		ObjectNullRef objectNullRef = new ObjectNullRef(oldRef.getInitializedClass());
		frame.getOperandStack().push(objectNullRef);
		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(this.database);
		// TODO: constraint: this.idVariable NOT IN SET OF ALL ID's in DATABASE CURRENTLY..
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
