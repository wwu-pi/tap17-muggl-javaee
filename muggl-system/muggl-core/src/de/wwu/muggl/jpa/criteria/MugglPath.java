package de.wwu.muggl.jpa.criteria;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import de.wwu.muggl.jpa.criteria.meta.MugglJPA;
import de.wwu.muggl.jpa.criteria.metamodel.SymbolicEntityAttribute;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;

public class MugglPath<X> extends MugglExpression<X> implements Path<X>, MugglJPAEntityPath, MugglJPA {

	protected String entityClassName;
	protected String attributeName;
	
	public MugglPath(String entityClassName) {
		this.entityClassName = entityClassName;
	}
	
	public String getEntityClassName() {
		return this.entityClassName;
	}

	public String getAttributeName() {
		return this.attributeName;
	}
	
	public MugglPath<X> get(SymbolicEntityAttribute symbolicEntityAttribute) {
		this.attributeName = symbolicEntityAttribute.getFieldName();
		return this;
	}
	
	public MugglPath<X> get(Objectref objectref) {
		if(objectref.getInitializedClassName().equals("java.lang.String")) {
			Arrayref classNameArrayCharRef = ((Arrayref)objectref.valueMap().get("value"));
			String name = "";
			for(int i=0; i<classNameArrayCharRef.length; i++) {
				name += (char)((IntConstant)classNameArrayCharRef.getElement(i)).getIntValue();
			}
			this.attributeName = name;
			return this;
		}
		throw new RuntimeException("Not handled yet");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public Bindable<X> getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path<?> getParentPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Path<Y> get(SingularAttribute<? super X, Y> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E, C extends Collection<E>> Expression<C> get(
			PluralAttribute<X, C, E> collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V, M extends Map<K, V>> Expression<M> get(
			MapAttribute<X, K, V> map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression<Class<? extends X>> type() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Path<Y> get(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

}
