package de.wwu.muggl.db.sym.qry;

import javax.persistence.criteria.Expression;

import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.predicate.ComparisonPredicate;

import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;

public class SymbolicComparisonPredicate extends ComparisonPredicate {

	private static final long serialVersionUID = 1L;

	private Variable mugglVariable;
	
	public SymbolicComparisonPredicate(
			CriteriaBuilderImpl criteriaBuilder, 
			ComparisonOperator comparisonOperator,
			Expression<?> leftHandSide, 
			Variable mugglVariable) {
		super(criteriaBuilder, comparisonOperator, leftHandSide, getDummyValue(mugglVariable));
		this.mugglVariable = mugglVariable;
	}
	
	public Variable getMugglVariable() {
		return this.mugglVariable;
	}
	
	private static Object getDummyValue(Variable variable) {
		if(variable instanceof NumericVariable) {
			return new Integer(0);
		}
		System.out.println("*** ops could not get dummy value for variable: " + variable);
		return null;
	}

	
}
