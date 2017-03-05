package de.wwu.muggl.jpa.criteria;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;

import de.wwu.muggl.jpa.criteria.meta.MugglJPA;
import de.wwu.muggl.jpa.criteria.predicate.MugglPredicateComparison;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.solvers.expressions.NumericVariable;
import de.wwu.muggl.solvers.expressions.Variable;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;

public class MugglCriteriaQuery<T> implements CriteriaQuery<T>, MugglJPA {
	
	protected String resultClassName;
	
	protected Set<MugglJPAQueryRestriction> whereSet;
	protected Set<MugglSelection<?>> selectSet;
	protected Set<MugglRoot<?>> rootSet;
	
	public MugglCriteriaQuery(String resultClassName) {
		this.resultClassName = resultClassName;
		this.whereSet = new HashSet<>();
		this.selectSet = new HashSet<>();
		this.rootSet = new HashSet<>();
	}
	
	public String getResultClassName() {
		return this.resultClassName;
	}
	
	public Set<MugglJPAQueryRestriction> getWhereSet() {
		return this.whereSet;
	}
	
	public Set<MugglRoot<?>> getRootSet() {
		return this.rootSet;
	}
	
	public Set<MugglSelection<?>> getSelectSet() {
		return this.selectSet;
	}
	
	public MugglRoot<?> from(Objectref entityClassReference) {
		Arrayref classNameArrayCharRef = ((Arrayref)((Objectref)entityClassReference.valueMap().get("name")).valueMap().get("value"));
		String entityClassName = "";
		for(int i=0; i<classNameArrayCharRef.length; i++) {
			entityClassName += (char)((IntConstant)classNameArrayCharRef.getElement(i)).getIntValue();
		}
		MugglRoot<?> root = new MugglRoot<>(entityClassName);
		this.rootSet.add(root);
		return root;
	}
	
	public MugglCriteriaQuery<T> where(MugglPredicateComparison expression) {
		this.whereSet.add(expression);
		return this;
	}
	
	public MugglCriteriaQuery<T> select(MugglRoot<?> selection) {
		this.selectSet.add(selection);
		return this;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	protected MugglCriteriaBuilder cb;
//	protected Class<?> resultClass;
//	
//	public MugglCriteriaQuery(MugglCriteriaBuilder cb, Class<?> resultClass) {
//		this.cb = cb;
//		this.resultClass = resultClass;
//	}
//	
//	public MugglRoot from(Objectref entityClass) {
//		return new MugglRoot();
//	}
//	
//	public MugglPredicate equal(MugglCriteriaBuilder cb, NumericVariable variable) {
//		return new MugglPredicate();
//	}
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public <X> Root<X> from(Class<X> entityClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> Root<X> from(EntityType<X> entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Root<?>> getRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selection<T> getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Expression<?>> getGroupList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate getGroupRestriction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDistinct() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<T> getResultType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Subquery<U> subquery(Class<U> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate getRestriction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> select(Selection<? extends T> selection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> multiselect(Selection<?>... selections) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> where(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> where(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> having(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> having(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> orderBy(Order... o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> orderBy(List<Order> o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CriteriaQuery<T> distinct(boolean distinct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Order> getOrderList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ParameterExpression<?>> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
