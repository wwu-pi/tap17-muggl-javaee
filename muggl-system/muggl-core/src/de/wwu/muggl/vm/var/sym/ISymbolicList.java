package de.wwu.muggl.vm.var.sym;

import java.util.List;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;

public interface ISymbolicList {

	List<?> getResultList();

	NumericVariable getSymbolicLength();
	
	JPAVirtualMachine getVM();

	void addElement(Objectref element);
	
	boolean removeElement(Objectref element);
	
	String getCollectionType();

	Objectref generateNewElement();
}
