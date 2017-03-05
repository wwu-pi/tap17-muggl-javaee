package de.wwu.muggl.solvers.expressions.array;

import java.util.Map;

import de.wwu.muggl.solvers.expressions.NumericVariable;

public interface ISymbolicArrayref extends IArrayref {

	public NumericVariable getSymbolicLength();
	
	public Map<Integer,Object> getElements();
	
	public Object getElement(int index);
	
	public void setElementAt(int index, Object element);
	
	public String getName();
}
