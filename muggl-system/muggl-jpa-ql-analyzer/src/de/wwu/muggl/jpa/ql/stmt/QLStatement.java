package de.wwu.muggl.jpa.ql.stmt;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.hibernate.sqm.query.SqmSelectStatement;

public class QLStatement<X> implements TypedQuery<X> {

	protected String resultClassName;
	protected SqmSelectStatement sqmStatement;
	protected Map<String, Object> parameters;
	
	public QLStatement(SqmSelectStatement sqmStatement, String resultClassName) {
		this.sqmStatement = sqmStatement;
		this.resultClassName = resultClassName;
		this.parameters = new HashMap<>();
	}

	public SqmSelectStatement getSqmStatement() {
		return this.sqmStatement;
	}
	
	public TypedQuery<X> setParameter(String name, Object value) {
		parameters.put(name, value);
		return this;
	}
	
	public Map<String, Object> getParameterMap() {
		return this.parameters;
	}
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public List<X> getResultList() {
		throw new RuntimeException("Method not implemented yet");
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public int executeUpdate() {
		throw new RuntimeException("Method not implemented yet");
	}

	public int getMaxResults() {
		throw new RuntimeException("Method not implemented yet");
	}

	public int getFirstResult() {
		throw new RuntimeException("Method not implemented yet");
	}

	public Map<String, Object> getHints() {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public Set<Parameter<?>> getParameters() {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public Parameter<?> getParameter(String name) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public Parameter<?> getParameter(int position) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public boolean isBound(Parameter<?> param) {
		throw new RuntimeException("Method not implemented yet");
	}

	public <T> T getParameterValue(Parameter<T> param) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public Object getParameterValue(String name) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public Object getParameterValue(int position) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public FlushModeType getFlushMode() {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public LockModeType getLockMode() {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public <T> T unwrap(Class<T> cls) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public X getSingleResult() {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setMaxResults(int maxResult) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setFirstResult(int startPosition) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setHint(String hintName, Object value) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
		throw new RuntimeException("Method not implemented yet");
		
	}
	
	public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setParameter(int position, Object value) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
		throw new RuntimeException("Method not implemented yet");
		
	}

	public TypedQuery<X> setLockMode(LockModeType lockMode) {
		throw new RuntimeException("Method not implemented yet");
	}
}
