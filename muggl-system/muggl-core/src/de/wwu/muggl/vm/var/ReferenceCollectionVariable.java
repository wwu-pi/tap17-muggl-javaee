package de.wwu.muggl.vm.var;

import java.util.ArrayList;
import java.util.List;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;
import de.wwu.muggl.vm.var.sym.ISymbolicList;
import de.wwu.muggl.vm.var.sym.SymbolicIterator;

public class ReferenceCollectionVariable extends ReferenceVariable implements ISymbolicList {

	private int counter;
	private Field elementDataField;
	private String collectionType;
	
	protected List<Objectref> resultList;
	
	public ReferenceCollectionVariable(String name,	ReferenceValue referenceValue, JPAVirtualMachine vm) {
		super(name, referenceValue, vm);
		
		this.elementDataField = referenceValue.getInitializedClass().getClassFile().getFieldByName("elementData");
		this.counter = 0;
		this.resultList = new ArrayList<>();
	}
	
	public SymbolicIterator iterator() {
		SymbolicIterator iterator = new SymbolicIterator(this);
		return iterator;
	}
	
	public void add(Objectref variableToAdd) {
		SymbolicArrayref symArray = (SymbolicArrayref)this.getField(elementDataField);
		symArray.setElementAt(counter, variableToAdd);
		counter++;
	}

	public boolean remove(Objectref elementToRemove) {
		SymbolicArrayref symArray = (SymbolicArrayref)this.getField(elementDataField);
		for(int i=0; i<symArray.elements.size(); i++) {
			Object element = symArray.elements.get(i);
			if(element == elementToRemove) {
				throw new RuntimeException("Remove entry and put all following entries one position to top...");
//				return true;
			}
		}
		return false;
	}
	
	public SymbolicArrayref getSymbolicArray() {
		return (SymbolicArrayref)this.getField(elementDataField);
	}
	
	public NumericVariable getSymbolicLength() {
		SymbolicArrayref symArray = (SymbolicArrayref)this.getField(elementDataField);
		return symArray.getSymbolicLength();
	}

	public void setCollectionType(String colType) {
		this.collectionType = colType;
	}
	
	public String getCollectionType() {
		return this.collectionType;
	}
	
	@Override
	public String toString() {
		return "ReferenceCollection: [name="+name+", type="+collectionType+", objectId="+getObjectId()+"]";
	}

	@Override
	public List<?> getResultList() {
		return this.resultList;
	}
	
	@Override
	public void addElement(Objectref element) {
		this.resultList.add(element);
	}
	
	@Override
	public JPAVirtualMachine getVM() {
		return this.vm;
	}
	
	@Override
	public Objectref generateNewElement() {		
		return null;
	}

	@Override
	public boolean removeElement(Objectref element) {
		return this.resultList.remove(element);
	}
}
