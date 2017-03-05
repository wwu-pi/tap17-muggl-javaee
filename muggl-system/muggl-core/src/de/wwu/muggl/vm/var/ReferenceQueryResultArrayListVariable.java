package de.wwu.muggl.vm.var;

import java.util.List;
import java.util.Set;

import de.wwu.muggl.db.entry.DatabaseObject;
import de.wwu.muggl.db.list.SymbolicQueryResultList;
import de.wwu.muggl.jpa.criteria.MugglCriteriaQuery;
import de.wwu.muggl.jpa.criteria.MugglJPAQueryRestriction;
import de.wwu.muggl.solvers.expressions.Expression;
import de.wwu.muggl.solvers.expressions.GreaterOrEqual;
import de.wwu.muggl.solvers.expressions.NumericConstant;
import de.wwu.muggl.symbolic.objgen.SymbolicObjectGenerationException;
import de.wwu.muggl.vm.impl.jpa.JPAVirtualMachine;
import de.wwu.muggl.vm.impl.symbolic.SymbolicVirtualMachine;
import de.wwu.muggl.vm.initialization.Objectref;

public class ReferenceQueryResultArrayListVariable extends ReferenceArrayListVariable  {

	protected MugglCriteriaQuery<?> criteriaQuery;
	
	public ReferenceQueryResultArrayListVariable(String name, JPAVirtualMachine vm, MugglCriteriaQuery<?> criteriaQuery) throws SymbolicObjectGenerationException {
		super(name, vm);
		this.criteriaQuery = criteriaQuery;
		vm.getSolverManager().addConstraint(GreaterOrEqual.newInstance(this.symbolicLength, NumericConstant.getZero(Expression.INT)));
	}	

	public MugglCriteriaQuery<?> getCriteriaQuery() {
		return this.criteriaQuery;
	}
	
	public Set<MugglJPAQueryRestriction> getQueryRestrictions() {
		return this.criteriaQuery.getWhereSet();
	}
	
	public void addElement(Objectref dbObjectToAdd) {
		this.list.add(dbObjectToAdd);
	}
	
	public String getResultEntityName() {
		return this.criteriaQuery.getResultClassName();
	}
	
	public List<Objectref> getSymbolicElements() {
		return this.list;
	}
}
