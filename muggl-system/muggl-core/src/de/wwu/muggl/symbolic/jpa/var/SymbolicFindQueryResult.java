package de.wwu.muggl.symbolic.jpa.var;

public class SymbolicFindQueryResult extends SymbolicQueryResult {

	protected Object key;
	protected boolean isNullResult;
	
	public SymbolicFindQueryResult(Class<?> entityClass, Object key, boolean isNullResult) {
		super(entityClass);
		this.key = key;
		this.isNullResult = isNullResult;
	}
	
	public boolean isNullResult() {
		return isNullResult;
	}
	
	public void setIsNullResult(boolean isNullResult) {
		this.isNullResult = isNullResult;
	}
	
	public Object getKey() {
		return key;
	}

}
