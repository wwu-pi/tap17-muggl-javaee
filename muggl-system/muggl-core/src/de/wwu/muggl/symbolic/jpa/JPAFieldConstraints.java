package de.wwu.muggl.symbolic.jpa;

public class JPAFieldConstraints {

	private boolean isId;
	private boolean isUnique;
	private boolean isNotNull;
	private Integer minSize;
	private Integer maxSize;
	
	public JPAFieldConstraints() {
	}

	public boolean isId() {
		return isId;
	}

	public void setIsId(boolean isId) {
		this.isId = isId;
	}
	
	public boolean isUnique() {
		return isUnique;
	}

	public void setIsUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public boolean isNotNull() {
		return isNotNull;
	}

	public void setIsNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
	}

	public Integer getMinSize() {
		return minSize;
	}

	public void setMinSize(Integer minSize) {
		this.minSize = minSize;
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}
	
}
