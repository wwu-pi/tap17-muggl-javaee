package de.wwu.muggl.symbolic.jpa.criteria;

import javax.persistence.criteria.Join;

public class CriteriaJoinWrapper {

	protected Join original;
	
	public CriteriaJoinWrapper(Join join) {
		this.original = join;
	}
	
	public Join getOriginal() {
		return this.original;
	}
}
