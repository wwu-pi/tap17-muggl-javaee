//package de.wwu.muggl.symbolic.jpa;
//
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.sql.Date;
//import java.sql.Time;
//import java.sql.Timestamp;
//import java.util.Collection;
//import java.util.Map;
//import java.util.Set;
//
//import javax.persistence.Tuple;
//import javax.persistence.criteria.CollectionJoin;
//import javax.persistence.criteria.CompoundSelection;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaDelete;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.CriteriaUpdate;
//import javax.persistence.criteria.Expression;
//import javax.persistence.criteria.Join;
//import javax.persistence.criteria.ListJoin;
//import javax.persistence.criteria.MapJoin;
//import javax.persistence.criteria.Order;
//import javax.persistence.criteria.ParameterExpression;
//import javax.persistence.criteria.Path;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//import javax.persistence.criteria.Selection;
//import javax.persistence.criteria.SetJoin;
//import javax.persistence.criteria.Subquery;
//
//import de.wwu.muggl.jpa.criteria.MugglCriteriaQuery;
//import de.wwu.muggl.jpa.criteria.MugglPredicate;
//import de.wwu.muggl.jpa.criteria.meta.MugglJPA;
//import de.wwu.muggl.solvers.expressions.NumericVariable;
//import de.wwu.muggl.solvers.expressions.Variable;
//import de.wwu.muggl.symbolic.jpa.gen.EntityReferenceGenerator;
//import de.wwu.muggl.vm.initialization.Objectref;
//
//public class MugglCriteriaBuilder implements CriteriaBuilder, MugglJPA {
//
//	protected CriteriaBuilder original;
//	
//	public MugglCriteriaBuilder(CriteriaBuilder original) {
//		this.original = original;
//	}
//	
//	public MugglCriteriaQuery<?> createQuery(Objectref objectref) {
//		Class<?> resultClass = null;
//		try {
//			EntityReferenceGenerator gen = new EntityReferenceGenerator(null);
//			String className = gen.getClassNameOfClassObjectRef(objectref);
//			resultClass = ClassLoader.getSystemClassLoader().loadClass(className);
//		} catch(ClassNotFoundException e) {
//			throw new RuntimeException("Could not load class: "+ objectref);
//		}
//		return new MugglCriteriaQuery<>(this, resultClass);
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
//	@Override
//	public CriteriaQuery<Object> createQuery() {
//		return original.createQuery();
//	}
//
//	@Override
//	public <T> CriteriaQuery<T> createQuery(Class<T> resultClass) {
//		
//		return original.createQuery(resultClass);
//	}
//
//	@Override
//	public CriteriaQuery<Tuple> createTupleQuery() {
//		
//		return original.createTupleQuery();
//	}
//
//	@Override
//	public <T> CriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
//		
//		return original.createCriteriaUpdate(targetEntity);
//	}
//
//	@Override
//	public <T> CriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
//		
//		return original.createCriteriaDelete(targetEntity);
//	}
//
//	@Override
//	public <Y> CompoundSelection<Y> construct(Class<Y> resultClass,
//			Selection<?>... selections) {
//		
//		return original.construct(resultClass, selections);
//	}
//
//	@Override
//	public CompoundSelection<Tuple> tuple(Selection<?>... selections) {
//		
//		return original.tuple(selections);
//	}
//
//	@Override
//	public CompoundSelection<Object[]> array(Selection<?>... selections) {
//		
//		return original.array(selections);
//	}
//
//	@Override
//	public Order asc(Expression<?> x) {
//		
//		return original.asc(x);
//	}
//
//	@Override
//	public Order desc(Expression<?> x) {
//		
//		return original.desc(x);
//	}
//
//	@Override
//	public <N extends Number> Expression<Double> avg(Expression<N> x) {
//		
//		return original.avg(x);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> sum(Expression<N> x) {
//		
//		return original.sum(x);
//	}
//
//	@Override
//	public Expression<Long> sumAsLong(Expression<Integer> x) {
//		
//		return original.sumAsLong(x);
//	}
//
//	@Override
//	public Expression<Double> sumAsDouble(Expression<Float> x) {
//		
//		return original.sumAsDouble(x);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> max(Expression<N> x) {
//		return original.max(x);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> min(Expression<N> x) {
//		return original.min(x);
//	}
//
//	@Override
//	public <X extends Comparable<? super X>> Expression<X> greatest(Expression<X> x) {
//		return original.greatest(x);
//	}
//
//	@Override
//	public <X extends Comparable<? super X>> Expression<X> least(Expression<X> x) {
//		return original.least(x);
//	}
//
//	@Override
//	public Expression<Long> count(Expression<?> x) {
//		
//		return original.count(x);
//	}
//
//	@Override
//	public Expression<Long> countDistinct(Expression<?> x) {
//		
//		return original.countDistinct(x);
//	}
//
//	@Override
//	public Predicate exists(Subquery<?> subquery) {
//		
//		return original.exists(subquery);
//	}
//
//	@Override
//	public <Y> Expression<Y> all(Subquery<Y> subquery) {
//		
//		return original.all(subquery);
//	}
//
//	@Override
//	public <Y> Expression<Y> some(Subquery<Y> subquery) {
//		
//		return original.some(subquery);
//	}
//
//	@Override
//	public <Y> Expression<Y> any(Subquery<Y> subquery) {
//		
//		return original.any(subquery);
//	}
//
//	@Override
//	public Predicate and(Expression<Boolean> x, Expression<Boolean> y) {
//		
//		return original.and(x,y);
//	}
//
//	@Override
//	public Predicate and(Predicate... restrictions) {
//		
//		return original.and(restrictions);
//	}
//
//	@Override
//	public Predicate or(Expression<Boolean> x, Expression<Boolean> y) {
//		
//		return original.or(x,y);
//	}
//
//	@Override
//	public Predicate or(Predicate... restrictions) {
//		
//		return original.or(restrictions);
//	}
//
//	@Override
//	public Predicate not(Expression<Boolean> restriction) {
//		
//		return original.not(restriction);
//	}
//
//	@Override
//	public Predicate conjunction() {
//		
//		return original.conjunction();
//	}
//
//	@Override
//	public Predicate disjunction() {
//		
//		return original.disjunction();
//	}
//
//	@Override
//	public Predicate isTrue(Expression<Boolean> x) {
//		
//		return original.isTrue(x);
//	}
//
//	@Override
//	public Predicate isFalse(Expression<Boolean> x) {
//		
//		return original.isFalse(x);
//	}
//
//	@Override
//	public Predicate isNull(Expression<?> x) {
//		
//		return original.isNull(x);
//	}
//
//	@Override
//	public Predicate isNotNull(Expression<?> x) {
//		
//		return original.isNotNull(x);
//	}
//
//	@Override
//	public Predicate equal(Expression<?> x, Expression<?> y) {
//		
//		return original.equal(x,y);
//	}
//
//	@Override
//	public Predicate equal(Expression<?> x, Object y) {
//		
//		return original.equal(x,y);
//	}
//
//	@Override
//	public Predicate notEqual(Expression<?> x, Expression<?> y) {
//		
//		return original.notEqual(x,y);
//	}
//
//	@Override
//	public Predicate notEqual(Expression<?> x, Object y) {
//		
//		return original.notEqual(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate greaterThan(Expression<? extends Y> x, Expression<? extends Y> y) {
//		
//		return original.greaterThan(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate greaterThan(
//			Expression<? extends Y> x, Y y) {
//		
//		return original.greaterThan(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(
//			Expression<? extends Y> x, Expression<? extends Y> y) {
//		
//		return original.greaterThanOrEqualTo(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(
//			Expression<? extends Y> x, Y y) {
//		
//		return original.greaterThanOrEqualTo(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate lessThan(
//			Expression<? extends Y> x, Expression<? extends Y> y) {
//		
//		return original.lessThan(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate lessThan(
//			Expression<? extends Y> x, Y y) {
//		
//		return original.lessThan(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(
//			Expression<? extends Y> x, Expression<? extends Y> y) {
//		
//		return original.lessThanOrEqualTo(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(
//			Expression<? extends Y> x, Y y) {
//		
//		return original.lessThanOrEqualTo(x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate between(
//			Expression<? extends Y> v, Expression<? extends Y> x,
//			Expression<? extends Y> y) {
//		
//		return original.between(v,x,y);
//	}
//
//	@Override
//	public <Y extends Comparable<? super Y>> Predicate between(
//			Expression<? extends Y> v, Y x, Y y) {
//		
//		return original.between(v,x,y);
//	}
//
//	@Override
//	public Predicate gt(Expression<? extends Number> x,
//			Expression<? extends Number> y) {
//		
//		return original.gt(x,y);
//	}
//
//	@Override
//	public Predicate gt(Expression<? extends Number> x, Number y) {
//		
//		return original.gt(x,y);
//	}
//
//	@Override
//	public Predicate ge(Expression<? extends Number> x,
//			Expression<? extends Number> y) {
//		
//		return original.ge(x,y);
//	}
//
//	@Override
//	public Predicate ge(Expression<? extends Number> x, Number y) {
//		
//		return original.ge(x,y);
//	}
//
//	@Override
//	public Predicate lt(Expression<? extends Number> x,
//			Expression<? extends Number> y) {
//		
//		return original.lt(x,y);
//	}
//
//	@Override
//	public Predicate lt(Expression<? extends Number> x, Number y) {
//		
//		return original.lt(x,y);
//	}
//
//	@Override
//	public Predicate le(Expression<? extends Number> x,
//			Expression<? extends Number> y) {
//		
//		return original.le(x,y);
//	}
//
//	@Override
//	public Predicate le(Expression<? extends Number> x, Number y) {
//		
//		return original.le(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> neg(Expression<N> x) {
//		
//		return original.neg(x);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> abs(Expression<N> x) {
//		
//		return original.abs(x);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> sum(Expression<? extends N> x,
//			Expression<? extends N> y) {
//		
//		return original.sum(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> sum(Expression<? extends N> x, N y) {
//		
//		return original.sum(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> sum(N x, Expression<? extends N> y) {
//		
//		return original.sum(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> prod(Expression<? extends N> x,
//			Expression<? extends N> y) {
//		
//		return original.prod(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> prod(Expression<? extends N> x, N y) {
//		
//		return original.prod(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> prod(N x, Expression<? extends N> y) {
//		
//		return original.prod(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> diff(Expression<? extends N> x,
//			Expression<? extends N> y) {
//		
//		return original.diff(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> diff(Expression<? extends N> x, N y) {
//		
//		return original.diff(x,y);
//	}
//
//	@Override
//	public <N extends Number> Expression<N> diff(N x, Expression<? extends N> y) {
//		
//		return original.diff(x,y);
//	}
//
//	@Override
//	public Expression<Number> quot(Expression<? extends Number> x,
//			Expression<? extends Number> y) {
//		
//		return original.quot(x,y);
//	}
//
//	@Override
//	public Expression<Number> quot(Expression<? extends Number> x, Number y) {
//		
//		return original.quot(x,y);
//	}
//
//	@Override
//	public Expression<Number> quot(Number x, Expression<? extends Number> y) {
//		
//		return original.quot(x,y);
//	}
//
//	@Override
//	public Expression<Integer> mod(Expression<Integer> x, Expression<Integer> y) {
//		
//		return original.mod(x,y);
//	}
//
//	@Override
//	public Expression<Integer> mod(Expression<Integer> x, Integer y) {
//		
//		return original.mod(x,y);
//	}
//
//	@Override
//	public Expression<Integer> mod(Integer x, Expression<Integer> y) {
//		
//		return original.mod(x,y);
//	}
//
//	@Override
//	public Expression<Double> sqrt(Expression<? extends Number> x) {
//		
//		return original.sqrt(x);
//	}
//
//	@Override
//	public Expression<Long> toLong(Expression<? extends Number> number) {
//		
//		return original.toLong(number);
//	}
//
//	@Override
//	public Expression<Integer> toInteger(Expression<? extends Number> number) {
//		
//		return original.toInteger(number);
//	}
//
//	@Override
//	public Expression<Float> toFloat(Expression<? extends Number> number) {
//		
//		return original.toFloat(number);
//	}
//
//	@Override
//	public Expression<Double> toDouble(Expression<? extends Number> number) {
//		
//		return original.toDouble(number);
//	}
//
//	@Override
//	public Expression<BigDecimal> toBigDecimal(
//			Expression<? extends Number> number) {
//		
//		return original.toBigDecimal(number);
//	}
//
//	@Override
//	public Expression<BigInteger> toBigInteger(
//			Expression<? extends Number> number) {
//		
//		return original.toBigInteger(number);
//	}
//
//	@Override
//	public Expression<String> toString(Expression<Character> character) {
//		
//		return original.toString(character);
//	}
//
//	@Override
//	public <T> Expression<T> literal(T value) {
//		
//		return original.literal(value);
//	}
//
//	@Override
//	public <T> Expression<T> nullLiteral(Class<T> resultClass) {
//		
//		return original.nullLiteral(resultClass);
//	}
//
//	@Override
//	public <T> ParameterExpression<T> parameter(Class<T> paramClass) {
//		
//		return original.parameter(paramClass);
//	}
//
//	@Override
//	public <T> ParameterExpression<T> parameter(Class<T> paramClass, String name) {
//		
//		return original.parameter(paramClass, name);
//	}
//
//	@Override
//	public <C extends Collection<?>> Predicate isEmpty(Expression<C> collection) {
//		
//		return original.isEmpty(collection);
//	}
//
//	@Override
//	public <C extends Collection<?>> Predicate isNotEmpty(
//			Expression<C> collection) {
//		
//		return original.isNotEmpty(collection);
//	}
//
//	@Override
//	public <C extends Collection<?>> Expression<Integer> size(
//			Expression<C> collection) {
//		
//		return original.size(collection);
//	}
//
//	@Override
//	public <C extends Collection<?>> Expression<Integer> size(C collection) {
//		
//		return original.size(collection);
//	}
//
//	@Override
//	public <E, C extends Collection<E>> Predicate isMember(Expression<E> elem,
//			Expression<C> collection) {
//		
//		return original.isMember(elem, collection);
//	}
//
//	@Override
//	public <E, C extends Collection<E>> Predicate isMember(E elem,
//			Expression<C> collection) {
//		
//		return original.isMember(elem, collection);
//	}
//
//	@Override
//	public <E, C extends Collection<E>> Predicate isNotMember(
//			Expression<E> elem, Expression<C> collection) {
//		
//		return original.isNotMember(elem, collection);
//	}
//
//	@Override
//	public <E, C extends Collection<E>> Predicate isNotMember(E elem,
//			Expression<C> collection) {
//		
//		return original.isNotMember(elem, collection);
//	}
//
//	@Override
//	public <V, M extends Map<?, V>> Expression<Collection<V>> values(M map) {
//		
//		return original.values(map);
//	}
//
//	@Override
//	public <K, M extends Map<K, ?>> Expression<Set<K>> keys(M map) {
//		
//		return original.keys(map);
//	}
//
//	@Override
//	public Predicate like(Expression<String> x, Expression<String> pattern) {
//		
//		return original.like(x,pattern);
//	}
//
//	@Override
//	public Predicate like(Expression<String> x, String pattern) {
//		
//		return original.like(x,pattern);
//	}
//
//	@Override
//	public Predicate like(Expression<String> x, Expression<String> pattern,
//			Expression<Character> escapeChar) {
//		
//		return original.like(x,pattern,escapeChar);
//	}
//
//	@Override
//	public Predicate like(Expression<String> x, Expression<String> pattern,
//			char escapeChar) {
//		
//		return original.like(x,pattern,escapeChar);
//	}
//
//	@Override
//	public Predicate like(Expression<String> x, String pattern,
//			Expression<Character> escapeChar) {
//		
//		return original.like(x,pattern,escapeChar);
//	}
//
//	@Override
//	public Predicate like(Expression<String> x, String pattern, char escapeChar) {
//		
//		return original.like(x,pattern,escapeChar);
//	}
//
//	@Override
//	public Predicate notLike(Expression<String> x, Expression<String> pattern) {
//		
//		return original.notLike(x,pattern);
//	}
//
//	@Override
//	public Predicate notLike(Expression<String> x, String pattern) {
//		
//		return original.notLike(x,pattern);
//	}
//
//	@Override
//	public Predicate notLike(Expression<String> x, Expression<String> pattern,
//			Expression<Character> escapeChar) {
//		
//		return original.notLike(x,pattern,escapeChar);
//	}
//
//	@Override
//	public Predicate notLike(Expression<String> x, Expression<String> pattern,
//			char escapeChar) {
//		
//		return original.notLike(x,pattern,escapeChar);
//	}
//
//	@Override
//	public Predicate notLike(Expression<String> x, String pattern,
//			Expression<Character> escapeChar) {
//		
//		return original.notLike(x,pattern,escapeChar);
//	}
//
//	@Override
//	public Predicate notLike(Expression<String> x, String pattern,
//			char escapeChar) {
//		
//		return original.notLike(x,pattern,escapeChar);
//	}
//
//	@Override
//	public Expression<String> concat(Expression<String> x, Expression<String> y) {
//		
//		return original.concat(x, y);
//	}
//
//	@Override
//	public Expression<String> concat(Expression<String> x, String y) {
//		
//		return original.concat(x, y);
//	}
//
//	@Override
//	public Expression<String> concat(String x, Expression<String> y) {
//		
//		return original.concat(x,y);
//	}
//
//	@Override
//	public Expression<String> substring(Expression<String> x,
//			Expression<Integer> from) {
//		
//		return original.substring(x, from);
//	}
//
//	@Override
//	public Expression<String> substring(Expression<String> x, int from) {
//		
//		return original.substring(x, from);
//	}
//
//	@Override
//	public Expression<String> substring(Expression<String> x,
//			Expression<Integer> from, Expression<Integer> len) {
//		
//		return original.substring(x, from, len);
//	}
//
//	@Override
//	public Expression<String> substring(Expression<String> x, int from, int len) {
//		
//		return original.substring(x, from, len);
//	}
//
//	@Override
//	public Expression<String> trim(Expression<String> x) {
//		
//		return original.trim(x);
//	}
//
//	@Override
//	public Expression<String> trim(Trimspec ts, Expression<String> x) {
//		
//		return original.trim(ts, x);
//	}
//
//	@Override
//	public Expression<String> trim(Expression<Character> t, Expression<String> x) {
//		
//		return original.trim(t,x);
//	}
//
//	@Override
//	public Expression<String> trim(Trimspec ts, Expression<Character> t,
//			Expression<String> x) {
//		
//		return original.trim(ts,t,x);
//	}
//
//	@Override
//	public Expression<String> trim(char t, Expression<String> x) {
//		
//		return original.trim(t,x);
//	}
//
//	@Override
//	public Expression<String> trim(Trimspec ts, char t, Expression<String> x) {
//		
//		return original.trim(ts,t,x);
//	}
//
//	@Override
//	public Expression<String> lower(Expression<String> x) {
//		
//		return original.lower(x);
//	}
//
//	@Override
//	public Expression<String> upper(Expression<String> x) {
//		
//		return original.upper(x);
//	}
//
//	@Override
//	public Expression<Integer> length(Expression<String> x) {
//		
//		return original.length(x);
//	}
//
//	@Override
//	public Expression<Integer> locate(Expression<String> x,
//			Expression<String> pattern) {
//		
//		return original.locate(x,pattern);
//	}
//
//	@Override
//	public Expression<Integer> locate(Expression<String> x, String pattern) {
//		
//		return original.locate(x,pattern);
//	}
//
//	@Override
//	public Expression<Integer> locate(Expression<String> x,
//			Expression<String> pattern, Expression<Integer> from) {
//		
//		return original.locate(x,pattern,from);
//	}
//
//	@Override
//	public Expression<Integer> locate(Expression<String> x, String pattern,
//			int from) {
//		
//		return original.locate(x,pattern,from);
//	}
//
//	@Override
//	public Expression<Date> currentDate() {
//		
//		return original.currentDate();
//	}
//
//	@Override
//	public Expression<Timestamp> currentTimestamp() {
//		
//		return original.currentTimestamp();
//	}
//
//	@Override
//	public Expression<Time> currentTime() {
//		
//		return original.currentTime();
//	}
//
//	@Override
//	public <T> In<T> in(Expression<? extends T> expression) {
//		
//		return original.in(expression);
//	}
//
//	@Override
//	public <Y> Expression<Y> coalesce(Expression<? extends Y> x,
//			Expression<? extends Y> y) {
//		
//		return original.coalesce(x,y);
//	}
//
//	@Override
//	public <Y> Expression<Y> coalesce(Expression<? extends Y> x, Y y) {
//		
//		return original.coalesce(x,y);
//	}
//
//	@Override
//	public <Y> Expression<Y> nullif(Expression<Y> x, Expression<?> y) {
//		return original.nullif(x,y);
//	}
//
//	@Override
//	public <Y> Expression<Y> nullif(Expression<Y> x, Y y) {
//		return original.nullif(x,y);
//	}
//
//	@Override
//	public <T> Coalesce<T> coalesce() {
//		return original.coalesce();
//	}
//
//	@Override
//	public <C, R> SimpleCase<C, R> selectCase(Expression<? extends C> expression) {
//		return original.selectCase(expression);
//	}
//
//	@Override
//	public <R> Case<R> selectCase() {
//		return original.selectCase();
//	}
//
//	@Override
//	public <T> Expression<T> function(String name, Class<T> type, Expression<?>... args) {
//		return original.function(name, type, args);
//	}
//
//	@Override
//	public <X, T, V extends T> Join<X, V> treat(Join<X, T> join, Class<V> type) {
//		return original.treat(join, type);
//	}
//
//	@Override
//	public <X, T, E extends T> CollectionJoin<X, E> treat(CollectionJoin<X, T> join, Class<E> type) {
//		return original.treat(join, type);
//	}
//
//	@Override
//	public <X, T, E extends T> SetJoin<X, E> treat(SetJoin<X, T> join,	Class<E> type) {
//		return original.treat(join, type);
//	}
//
//	@Override
//	public <X, T, E extends T> ListJoin<X, E> treat(ListJoin<X, T> join, Class<E> type) {
//		return original.treat(join, type);
//	}
//
//	@Override
//	public <X, K, T, V extends T> MapJoin<X, K, V> treat(MapJoin<X, K, T> join,	Class<V> type) {
//		return original.treat(join, type);
//	}
//
//	@Override
//	public <X, T extends X> Path<T> treat(Path<X> path, Class<T> type) {
//		return original.treat(path, type);
//	}
//
//	@Override
//	public <X, T extends X> Root<T> treat(Root<X> root, Class<T> type) {
//		return original.treat(root, type);
//	}
//	
//}
