package de.wwu.muggl.jpa.criteria.predicate;

import de.wwu.muggl.jpa.criteria.ComparisonOperator;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryComparisonRestriction;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryRestriction;
import de.wwu.muggl.jpa.criteria.MugglPredicate;

public class MugglPredicateComparison extends MugglPredicate implements MugglJPAQueryComparisonRestriction {

	protected Object leftExpression;
	protected Object rightExpression;
	protected ComparisonOperator operator;
	
	public MugglPredicateComparison(Object leftExpression, Object rightExpression, ComparisonOperator operator) {
		this.leftExpression = leftExpression;
		this.rightExpression = rightExpression;
		this.operator = operator;
	}
	
	public Object getRight() {
		return this.rightExpression;
	}
	
	public Object getLeft() {
		return this.leftExpression;
	}
	
	public ComparisonOperator getComparisonOperator() {
		return this.operator;
	}
}
