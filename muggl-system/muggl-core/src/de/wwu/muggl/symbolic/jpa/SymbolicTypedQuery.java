package de.wwu.muggl.symbolic.jpa;

import javax.persistence.criteria.CriteriaQuery;

public class SymbolicTypedQuery  {
	
	private CriteriaQuery<?> criteriaQuery;
	
	public SymbolicTypedQuery(CriteriaQuery<?> criteriaQuery) {
		this.criteriaQuery = criteriaQuery;
	}

	public CriteriaQuery<?> getCriteriaQuery() {
		return criteriaQuery;
	}

}
