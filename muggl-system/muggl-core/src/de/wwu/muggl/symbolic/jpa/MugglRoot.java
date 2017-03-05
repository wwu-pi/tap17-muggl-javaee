//package de.wwu.muggl.symbolic.jpa;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.persistence.criteria.CollectionJoin;
//import javax.persistence.criteria.Expression;
//import javax.persistence.criteria.Fetch;
//import javax.persistence.criteria.From;
//import javax.persistence.criteria.Join;
//import javax.persistence.criteria.JoinType;
//import javax.persistence.criteria.ListJoin;
//import javax.persistence.criteria.MapJoin;
//import javax.persistence.criteria.Path;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//import javax.persistence.criteria.Selection;
//import javax.persistence.criteria.SetJoin;
//import javax.persistence.metamodel.CollectionAttribute;
//import javax.persistence.metamodel.EntityType;
//import javax.persistence.metamodel.ListAttribute;
//import javax.persistence.metamodel.MapAttribute;
//import javax.persistence.metamodel.PluralAttribute;
//import javax.persistence.metamodel.SetAttribute;
//import javax.persistence.metamodel.SingularAttribute;
//
//import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class MugglRoot<X> implements Root<X> {
//	
//	protected EntityReferenceGenerator generator;
//	protected Root<X> original;
//
//	public MugglRoot(Root<X> original) {
//		this.original = original;
//		this.generator = new EntityReferenceGenerator(null);
//	}
//
//	
//	public Path get(Objectref objectref) {
//		if(objectref.getInitializedClassName().equals("java.lang.String")) {
//			String path = generator.getStringValueOfObjectRef(objectref);
//			original.get(path);
//		}
//		
//		return null;
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
//	@Override
//	public Set<Join<X, ?>> getJoins() {
//		return original.getJoins();
//	}
//
//	@Override
//	public boolean isCorrelated() {
//		return original.isCorrelated();
//	}
//
//	@Override
//	public From<X, X> getCorrelationParent() {
//		return original.getCorrelationParent();
//	}
//
//	@Override
//	public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute) {
//		return original.join(attribute);
//	}
//
//	@Override
//	public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute,JoinType jt) {
//		return original.join(attribute, jt);
//	}
//
//	@Override
//	public <Y> CollectionJoin<X, Y> join(
//			CollectionAttribute<? super X, Y> collection) {
//		return original.join(collection);
//	}
//
//	@Override
//	public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set) {
//		
//		return original.join(set);
//	}
//
//	@Override
//	public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
//		
//		return original.join(list);
//	}
//
//	@Override
//	public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {
//		
//		return original.join(map);
//	}
//
//	@Override
//	public <Y> CollectionJoin<X, Y> join(
//			CollectionAttribute<? super X, Y> collection, JoinType jt) {
//		
//		return original.join(collection, jt);
//	}
//
//	@Override
//	public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
//		
//		return original.join(set, jt);
//	}
//
//	@Override
//	public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list,
//			JoinType jt) {
//		
//		return original.join(list, jt);
//	}
//
//	@Override
//	public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map,
//			JoinType jt) {
//		
//		return original.join(map, jt);
//	}
//
//	@Override
//	public <X, Y> Join<X, Y> join(String attributeName) {
//		
//		return original.join(attributeName);
//	}
//
//	@Override
//	public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName) {
//		
//		return original.joinCollection(attributeName);
//	}
//
//	@Override
//	public <X, Y> SetJoin<X, Y> joinSet(String attributeName) {
//		
//		return original.joinSet(attributeName);
//	}
//
//	@Override
//	public <X, Y> ListJoin<X, Y> joinList(String attributeName) {
//		
//		return original.joinList(attributeName);
//	}
//
//	@Override
//	public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName) {
//		
//		return original.joinMap(attributeName);
//	}
//
//	@Override
//	public <X, Y> Join<X, Y> join(String attributeName, JoinType jt) {
//		
//		return original.join(attributeName, jt);
//	}
//
//	@Override
//	public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName,
//			JoinType jt) {
//		
//		return original.joinCollection(attributeName, jt);
//	}
//
//	@Override
//	public <X, Y> SetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
//		
//		return original.joinSet(attributeName, jt);
//	}
//
//	@Override
//	public <X, Y> ListJoin<X, Y> joinList(String attributeName, JoinType jt) {
//		
//		return original.joinList(attributeName, jt);
//	}
//
//	@Override
//	public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName,
//			JoinType jt) {
//		
//		return original.joinMap(attributeName, jt);
//	}
//
//	@Override
//	public Path<?> getParentPath() {
//		
//		return original.getParentPath();
//	}
//
//	@Override
//	public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
//		
//		return original.get(attribute);
//	}
//
//	@Override
//	public <E, C extends Collection<E>> Expression<C> get(
//			PluralAttribute<X, C, E> collection) {
//		
//		return original.get(collection);
//	}
//
//	@Override
//	public <K, V, M extends Map<K, V>> Expression<M> get(
//			MapAttribute<X, K, V> map) {
//		
//		return original.get(map);
//	}
//
//	@Override
//	public Expression<Class<? extends X>> type() {
//		
//		return original.type();
//	}
//
//	@Override
//	public <Y> Path<Y> get(String attributeName) {
//		
//		return original.get(attributeName);
//	}
//
//	@Override
//	public Predicate isNull() {
//		
//		return original.isNull();
//	}
//
//	@Override
//	public Predicate isNotNull() {
//		
//		return original.isNotNull();
//	}
//
//	@Override
//	public Predicate in(Object... values) {
//		
//		return original.in(values);
//	}
//
//	@Override
//	public Predicate in(Expression<?>... values) {
//		
//		return original.in(values);
//	}
//
//	@Override
//	public Predicate in(Collection<?> values) {
//		
//		return original.in(values);
//	}
//
//	@Override
//	public Predicate in(Expression<Collection<?>> values) {
//		
//		return original.in(values);
//	}
//
//	@Override
//	public <X> Expression<X> as(Class<X> type) {
//		
//		return original.as(type);
//	}
//
//	@Override
//	public Selection<X> alias(String name) {
//		
//		return original.alias(name);
//	}
//
//	@Override
//	public boolean isCompoundSelection() {
//		return original.isCompoundSelection();
//	}
//
//	@Override
//	public List<Selection<?>> getCompoundSelectionItems() {
//		
//		return original.getCompoundSelectionItems();
//	}
//
//	@Override
//	public Class<? extends X> getJavaType() {
//		
//		return original.getJavaType();
//	}
//
//	@Override
//	public String getAlias() {
//		
//		return original.getAlias();
//	}
//
//	@Override
//	public Set<Fetch<X, ?>> getFetches() {
//		
//		return original.getFetches();
//	}
//
//	@Override
//	public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
//		
//		return original.fetch(attribute);
//	}
//
//	@Override
//	public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute,
//			JoinType jt) {
//		
//		return original.fetch(attribute, jt);
//	}
//
//	@Override
//	public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) {
//		
//		return original.fetch(attribute);
//	}
//
//	@Override
//	public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute,
//			JoinType jt) {
//		
//		return original.fetch(attribute, jt);
//	}
//
//	@Override
//	public <X, Y> Fetch<X, Y> fetch(String attributeName) {
//		
//		return original.fetch(attributeName);
//	}
//
//	@Override
//	public <X, Y> Fetch<X, Y> fetch(String attributeName, JoinType jt) {
//		
//		return original.fetch(attributeName, jt);
//	}
//
//	@Override
//	public EntityType<X> getModel() {
//		
//		return original.getModel();
//	}
//
//}
