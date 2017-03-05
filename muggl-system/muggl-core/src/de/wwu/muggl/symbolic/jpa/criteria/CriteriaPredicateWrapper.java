package de.wwu.muggl.symbolic.jpa.criteria;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import de.wwu.muggl.symbolic.jpa.criteria.meta.CriteriaWrapper;
import de.wwu.muggl.vm.initialization.InitializedClass;
import de.wwu.muggl.vm.initialization.Objectref;
import de.wwu.muggl.vm.initialization.ReferenceValue;

public class CriteriaPredicateWrapper implements ReferenceValue, CriteriaWrapper {
	
	protected Predicate predicate;
	
	private List<Objectref> symbolicParameterList;

	public CriteriaPredicateWrapper(Predicate predicate) {
		this.predicate = predicate;
		this.symbolicParameterList = new ArrayList<>();
	}
	
	public Predicate getOriginal() {
		return this.predicate;
	}

	public void addSymbolicParameter(Objectref objectValue) {
		this.symbolicParameterList.add(objectValue);
	}
	
	public List<Objectref> getSymbolicParameter() {
		return this.symbolicParameterList;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public InitializedClass getInitializedClass() {
		return null;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public long getInstantiationNumber() {
		// TODO Auto-generated method stub
		return 0;
	}
}
