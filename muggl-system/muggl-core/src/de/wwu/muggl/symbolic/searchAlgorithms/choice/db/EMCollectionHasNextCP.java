package de.wwu.muggl.symbolic.searchAlgorithms.choice.db;

import java.util.Stack;

import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.db.ana.EntityAnalyzer;
import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.entry.EntityEntry;
import de.wwu.muggl.db.sym.list.CollectionVariable;
import de.wwu.muggl.db.sym.list.SymbolicHasNext;
import de.wwu.muggl.solvers.SolverManager;
import de.wwu.muggl.solvers.exceptions.SolverUnableToDecideException;
import de.wwu.muggl.solvers.exceptions.TimeoutException;
import de.wwu.muggl.solvers.expressions.ConstraintExpression;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.TrailElement;
import de.wwu.muggl.vm.Frame;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.ObjectNullRef;
import de.wwu.muggl.vm.initialization.Objectref;

public class EMCollectionHasNextCP implements ChoicePoint {

	protected boolean alreadyVisitedNonJumpingBranch = false;
	protected Stack<TrailElement> trail = new Stack<TrailElement>();
	protected Frame frame;
	protected int pc;
	protected int pcNext;
	protected int pcWithJump;
	protected ChoicePoint parent;
	protected long number;
	protected int constraintLevel;
	
	protected CollectionVariable<DatabaseObject> collection;
	
	protected JPAVirtualMachine vm;
	
	private VirtualDatabase database;
	
	public EMCollectionHasNextCP(Frame frame, int pc, int pcNext, int pcWithJump, ChoicePoint parent, CollectionVariable<DatabaseObject> collection) {
		this.frame = frame;
		this.pc = pc;
		this.pcNext = pcNext;
		this.number = 0;
		this.pcWithJump = pcWithJump;
		this.collection = collection;
		this.parent = parent;
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
		// first: create a new element in the list
		// such that hasNext = true
		
		String collectionType = this.collection.getCollectionType();
		
		try {
			EntityEntry entityObject = EntityAnalyzer.getInitialEntityEntry(collectionType);
		
			ClassFile entityClassFile = this.vm.getClassLoader().getClassAsClassFile(collectionType);
			Objectref objectRef = this.vm.getAnObjectref(entityClassFile);
			
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
			
			this.collection.add(objectRef);		
			
			VirtualDatabase newVDB = this.database.getClone();
			
			newVDB.addRequired(objectRef.getObjectId(), entityObject);
			newVDB.addData(objectRef);
			
			((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(newVDB);	
		
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		
		
		
		
//		Objectref objectRef = null;
//		try {
//			ClassFile entityClassFile = this.vm.getClassLoader().getClassAsClassFile(collectionType);
//			objectRef = this.vm.getAnObjectref(entityClassFile);
//		} catch (ClassFileException e) {
//			e.printStackTrace();
//		}
//		
//		
//		this.collection.add(objectRef);
//		
//		// we must add this element to the required data
//		DatabaseObject collectionParent = collection.getParent();
//		if(collectionParent != null) {
//			String parentCode = collectionParent.getObjectId();
//			EntityEntry entityEntry = database.getRequiredData().get(parentCode);
//			String parentFieldName = this.collection.getParentFieldName();
//			CollectionVariable<Objectref> parentCollection = (CollectionVariable<Objectref>)entityEntry.valueMap().get(parentFieldName);
////			parentCollection.add(objectRef);
//		}
		
		try {
			if(!this.vm.getSolverManager().hasSolution()) {
				// try the hasNext=false branch...
				//TODO: daten aus der colection wieder entfernen
				System.out.println("ljjlk");
			} else {
				this.vm.setPC(pcWithJump);
			}
		} catch (SolverUnableToDecideException e) {
			this.alreadyVisitedNonJumpingBranch = true;
		} catch (TimeoutException e) {
			this.alreadyVisitedNonJumpingBranch = true;
		}
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
		System.out.println("*** reset the database in order to have NO NEXT ELEMENT IN COLLECITON");
		
		for(DatabaseObject dbObj : collection.getRealCollection()) {
			this.database.getRequiredData().remove(dbObj.getObjectId());
		}
		collection.getRealCollection().clear();
		((JPAVirtualMachine)frame.getVm()).setVirtualDatabase(this.database);
	}

	@Override
	public String getChoicePointType() {
		return "Collection Element Choice Point";
	}
	
	@Override
	public int getConstraintLevel() {
		return this.constraintLevel;
	}
}
