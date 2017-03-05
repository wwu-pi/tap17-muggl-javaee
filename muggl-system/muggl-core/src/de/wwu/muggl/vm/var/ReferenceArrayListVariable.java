package de.wwu.muggl.vm.var;

import java.util.ArrayList;
import java.util.List;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.vm.classfile.ClassFile;
import de.wwu.muggl.vm.classfile.ClassFileException;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.elements.Annotation;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.gen.SymoblicEntityFieldGenerator;
import de.wwu.muggl.vm.var.sym.ISymbolicList;
import de.wwu.muggl.vm.var.sym.SymbolicIterator;

public class ReferenceArrayListVariable extends ReferenceVariable implements ISymbolicList {

	protected List<Objectref> list;
	protected NumericVariable symbolicLength;
	protected String collectionType;
	protected ClassFile collectionClassFile;
	
	protected EntityObjectref owner;
	protected Field fieldInOwner;
	
	public EntityObjectref getEntityOwner() {
		return this.owner;
	}
	public void setEntityOwner(EntityObjectref owner) {
		this.owner = owner;
	}
	
	public Field getListField() {
		return this.fieldInOwner;
	}
	public void setEntityOwnerListField(Field field) {
		this.fieldInOwner = field;
	}
	
	public ReferenceArrayListVariable(String name, JPAVirtualMachine vm) throws SymbolicObjectGenerationException {
		super(name, getArrayListReference(vm), vm);
		this.list = new ArrayList<>();
		this.symbolicLength = new NumericVariable(name+".length", Expression.INT);
		vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(symbolicLength, NumericConstant.getZero(Expression.INT)));
		Field lengthField = this.getInitializedClass().getClassFile().getFieldByName("size");
		this.putField(lengthField, symbolicLength);	
	}

	public NumericVariable getSymbolicLength() {
		return this.symbolicLength;
	}	
	
	public SymbolicIterator iterator() {
		SymbolicIterator iterator = new SymbolicIterator(this);
		return iterator;
	}
	
	private static Objectref getArrayListReference(SymbolicVirtualMachine vm) throws SymbolicObjectGenerationException {
		ClassFile classFile = null;
		try {
			classFile = vm.getClassLoader().getClassAsClassFile("java.util.ArrayList");
		} catch (ClassFileException e) {
			throw new SymbolicObjectGenerationException("Could not get reference value for field with type: java.util.ArrayList");
		}
		return vm.getAnObjectref(classFile);
	}



	public void setCollectionType(String collectionType) {
		this.collectionType = collectionType;
		try {
			this.collectionClassFile = this.vm.getClassLoader().getClassAsClassFile(this.collectionType);
			if(this.collectionClassFile.isAccInterface()) {
				this.collectionClassFile = this.vm.getClassLoader().getClassAsClassFile(this.collectionType+"Impl");
			}
		} catch(Exception e) {}
	}
	
	public String getCollectionType() {
		return this.collectionType;
	}

	@Override
	public List<Objectref> getResultList() {
		return this.list;
	}

	@Override
	public JPAVirtualMachine getVM() {
		return this.vm;
	}

	@Override
	public void addElement(Objectref element) {
		this.list.add(element);
	}
	
	@Override
	public boolean removeElement(Objectref element) {
		return this.list.remove(element);
	}
	
	@Override
	public Objectref generateNewElement() {
		
		
		if(isEntityClass(this.collectionClassFile)) {
			SymoblicEntityFieldGenerator entityFieldGenerator = new SymoblicEntityFieldGenerator(this.vm);
			
			Objectref req_entityObjectref = this.vm.getAnObjectref(this.collectionClassFile);
			String req_entityObjectrefName = name+".element."+this.list.size()+"#REQ#"+referenceValue.hashCode();
			EntityObjectref req_entityObject = new EntityObjectref(
					entityFieldGenerator, 
					req_entityObjectrefName, 
					req_entityObjectref);
			
			Objectref data_entityObjectref = this.vm.getAnObjectref(this.collectionClassFile);
			String data_entityObjectrefName = name+".element."+this.list.size()+"#DATA#"+referenceValue.hashCode();
			EntityObjectref data_entityObject = new EntityObjectref(
					entityFieldGenerator,
					data_entityObjectrefName,
					data_entityObjectref, 
					req_entityObject);
			
			if(this.owner != null && this.owner.getRequiredEntity() != null) {
				((ReferenceArrayListVariable)this.owner.getRequiredEntity().getField(this.fieldInOwner)).addElement(req_entityObject);
			}
			
			return data_entityObject;
		} else {
			Objectref referenceValue = this.vm.getAnObjectref(this.collectionClassFile);
			ReferenceVariable refVar = new ReferenceVariable(name+".element"+referenceValue.hashCode(), referenceValue, this.vm);
			if(this.owner != null && this.owner.getRequiredEntity() != null) {
				((ReferenceArrayListVariable)this.owner.getRequiredEntity().getField(this.fieldInOwner)).addElement(refVar);
			}
			return refVar;
		}
	}
	
	protected boolean isEntityClass(ClassFile classFile) {
		for(Attribute attribute : classFile.getAttributes()) {
			if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
				AttributeRuntimeVisibleAnnotations attributeAnnotation = (AttributeRuntimeVisibleAnnotations) attribute;
				for(Annotation anno : attributeAnnotation.getAnnotations()) {
					if (anno.getClassFile().getConstantPool()[anno.getTypeIndex()].getStringValue().equals("Ljavax/persistence/Entity;")) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
