package de.wwu.muggl.symbolic.jpa.criteria;

import javax.persistence.criteria.Path;

public class CriteriaPathWrapper {

	protected Path path;
	
	public CriteriaPathWrapper(Path path) {
		this.path = path;
	}
	
	public Path getOriginal() {
		return this.path;
	}
}
