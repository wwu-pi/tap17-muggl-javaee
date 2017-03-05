package de.wwu.muggl.jpa.criteria;

public interface MugglJPAQueryComparisonRestriction extends MugglJPAQueryRestriction {

	public Object getRight();
	public Object getLeft();
	public ComparisonOperator getComparisonOperator();
	
}
