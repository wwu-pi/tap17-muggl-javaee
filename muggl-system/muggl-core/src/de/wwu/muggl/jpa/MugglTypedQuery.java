package de.wwu.muggl.jpa;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import de.wwu.muggl.jpa.criteria.MugglCriteriaQuery;
import de.wwu.muggl.jpa.criteria.MugglPredicate;
import de.wwu.muggl.jpa.criteria.MugglSelection;
import de.wwu.muggl.jpa.criteria.meta.MugglJPA;

public class MugglTypedQuery<X> implements TypedQuery<X>, MugglJPA {
	
	protected MugglCriteriaQuery<?> criteriaQuery;
	
	public MugglTypedQuery(MugglCriteriaQuery<?> criteriaQuery) {
		this.criteriaQuery = criteriaQuery;
	}
	
	public MugglCriteriaQuery<?> getCriteriaQuery() {
		return this.criteriaQuery;
	}	
	
	
	
	
	
	@Override
	public List<X> getResultList() {
		// TODO Auto-generated method stub
		return null;
	}	
	
	@Override	
	public int executeUpdate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxResults() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFirstResult() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, Object> getHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parameter<?> getParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Parameter<?> getParameter(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBound(Parameter<?> param) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T getParameterValue(Parameter<T> param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParameterValue(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParameterValue(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FlushModeType getFlushMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockModeType getLockMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public X getSingleResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setMaxResults(int maxResult) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setFirstResult(int startPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setHint(String hintName, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value,
			TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(Parameter<Date> param, Date value,
			TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Calendar value,
			TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(String name, Date value,
			TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Calendar value,
			TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setParameter(int position, Date value,
			TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypedQuery<X> setLockMode(LockModeType lockMode) {
		// TODO Auto-generated method stub
		return null;
	}


}
