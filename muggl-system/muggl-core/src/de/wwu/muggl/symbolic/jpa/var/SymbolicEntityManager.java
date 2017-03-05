package de.wwu.muggl.symbolic.jpa.var;

import de.wwu.muggl.db.VirtualDatabase;
import de.wwu.muggl.symbolic.jpa.var.meta.JPASpecialType;

public class SymbolicEntityManager implements JPASpecialType {
	
	protected VirtualDatabase database;
	
	public SymbolicEntityManager() {
		this.database = new VirtualDatabase();
	}
	
	public SymbolicQueryResult find(Class<?> entityClass, Object key, boolean isNullResult) {
		SymbolicQueryResult result = new SymbolicFindQueryResult(entityClass, key, isNullResult);
		return result;
	}	
}
