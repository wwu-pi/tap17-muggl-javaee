package de.wwu.muggl.symbolic.jpa.var;

import de.wwu.muggl.symbolic.jpa.var.meta.JPASpecialType;

public class SymbolicQueryResult implements JPASpecialType {

	protected Class<?> resultClass;
	
	public SymbolicQueryResult(Class<?> entityClass) {
		this.resultClass = entityClass;
	}
	
	public Class<?> getResultClass() {
		return resultClass;
	}
}
