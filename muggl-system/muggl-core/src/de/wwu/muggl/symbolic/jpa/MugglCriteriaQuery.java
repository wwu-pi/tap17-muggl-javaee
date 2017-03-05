//package de.wwu.muggl.symbolic.jpa;
//
//import java.util.List;
//import java.util.Set;
//
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Expression;
//import javax.persistence.criteria.Order;
//import javax.persistence.criteria.ParameterExpression;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//import javax.persistence.criteria.Selection;
//import javax.persistence.criteria.Subquery;
//import javax.persistence.metamodel.EntityType;
//
//import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class MugglCriteriaQuery<T> implements CriteriaQuery<T> {
//	
//	protected CriteriaQuery<T> original;
//	
//	public MugglCriteriaQuery(CriteriaQuery<T> original) {
//		this.original = original;
//	}
//	
//	public <X> Root<X> from(Objectref objectref) {
//		Class<?> entityClass = null;
//		try {
//			EntityReferenceGenerator gen = new EntityReferenceGenerator(null);
//			String className = gen.getClassNameOfClassObjectRef(objectref);
//			entityClass = ClassLoader.getSystemClassLoader().loadClass(className);
//		} catch(ClassNotFoundException e) {
//			throw new RuntimeException("Could not load class: "+ objectref);
//		}
//		return new MugglRoot(original.from(entityClass));
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//
//	@Override
//	public <X> Root<X> from(Class<X> entityClass) {
//		
//		return original.from(entityClass);
//	}
//
//	@Override
//	public <X> Root<X> from(EntityType<X> entity) {
//		
//		return original.from(entity);
//	}
//
//	@Override
//	public Set<Root<?>> getRoots() {
//		
//		return original.getRoots();
//	}
//
//	@Override
//	public Selection<T> getSelection() {
//		
//		return original.getSelection();
//	}
//
//	@Override
//	public List<Expression<?>> getGroupList() {
//		
//		return original.getGroupList();
//	}
//
//	@Override
//	public Predicate getGroupRestriction() {
//		
//		return original.getGroupRestriction();
//	}
//
//	@Override
//	public boolean isDistinct() {
//		
//		return original.isDistinct();
//	}
//
//	@Override
//	public Class<T> getResultType() {
//		
//		return original.getResultType();
//	}
//
//	@Override
//	public <U> Subquery<U> subquery(Class<U> type) {
//		
//		return original.subquery(type);
//	}
//
//	@Override
//	public Predicate getRestriction() {
//		
//		return original.getRestriction();
//	}
//
//	@Override
//	public CriteriaQuery<T> select(Selection<? extends T> selection) {
//		
//		return original.select(selection);
//	}
//
//	@Override
//	public CriteriaQuery<T> multiselect(Selection<?>... selections) {
//		
//		return original.multiselect(selections);
//	}
//
//	@Override
//	public CriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
//		
//		return original.multiselect(selectionList);
//	}
//
//	@Override
//	public CriteriaQuery<T> where(Expression<Boolean> restriction) {
//		
//		return original.where(restriction);
//	}
//
//	@Override
//	public CriteriaQuery<T> where(Predicate... restrictions) {
//		
//		return original.where(restrictions);
//	}
//
//	@Override
//	public CriteriaQuery<T> groupBy(Expression<?>... grouping) {
//		
//		return original.groupBy(grouping);
//	}
//
//	@Override
//	public CriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
//		
//		return original.groupBy(grouping);
//	}
//
//	@Override
//	public CriteriaQuery<T> having(Expression<Boolean> restriction) {
//		
//		return original.having(restriction);
//	}
//
//	@Override
//	public CriteriaQuery<T> having(Predicate... restrictions) {
//		
//		return original.having(restrictions);
//	}
//
//	@Override
//	public CriteriaQuery<T> orderBy(Order... o) {
//		
//		return original.orderBy(o);
//	}
//
//	@Override
//	public CriteriaQuery<T> orderBy(List<Order> o) {
//		
//		return original.orderBy(o);
//	}
//
//	@Override
//	public CriteriaQuery<T> distinct(boolean distinct) {
//		
//		return original.distinct(distinct);
//	}
//
//	@Override
//	public List<Order> getOrderList() {
//		
//		return original.getOrderList();
//	}
//
//	@Override
//	public Set<ParameterExpression<?>> getParameters() {
//		
//		return original.getParameters();
//	}
//
//}
