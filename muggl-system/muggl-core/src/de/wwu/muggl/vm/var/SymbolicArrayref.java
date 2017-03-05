package de.wwu.muggl.vm.var;

import java.util.HashMap;
import java.util.Map;

import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.array.ISymbolicArrayref;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.ReferenceValue;

public class SymbolicArrayref implements ReferenceValue, ISymbolicArrayref {

	protected String name;
	protected InitializedClass initializedClass;
	protected Map<Integer,Object> elements;
	protected NumericVariable length;
	
	// this field gives the maximum index number used
	private int currentMaximumIndex;
	
	public SymbolicArrayref(InitializedClass initializedClass, String name) {
		this.name = name;
		this.initializedClass = initializedClass;
		this.length = new NumericVariable(this.name+".length", Expression.INT);
		this.elements = new HashMap<>();
		this.currentMaximumIndex = -1;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public Map<Integer,Object> getElements() {
		return this.elements;
	}

	public Object getElement(int index) {
		if(index >= 0) {
			if(index > currentMaximumIndex) {
				currentMaximumIndex = index;
			}
			return this.elements.get(index);
		}
		return null;
	}
	
	public void setElementAt(int index, Object element) {
		if(index >= 0) {
			if(index > currentMaximumIndex) {
				currentMaximumIndex = index;
			}
			this.elements.put(index, element);
		}
	}
	
	public NumericVariable getSymbolicLength() {
		return this.length;
	}
	
	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public InitializedClass getInitializedClass() {
		return this.initializedClass;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public long getInstantiationNumber() {
		return this.initializedClass.getClassFile().getClassLoader().getNextInstantiationNumber();
	}

	@Override
	public int getLength() {
		throw new RuntimeException("Could not return a concrete length, since this is a SYMBOLIC array reference");
	}
}
