package de.wwu.muggl.jpa.criteria;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;

import de.wwu.muggl.jpa.criteria.meta.MugglJPA;
import de.wwu.muggl.jpa.criteria.metamodel.SymbolicEntityAttribute;
import de.wwu.muggl.vm.initialization.Objectref;

public class MugglJoin<Z, X> extends MugglFrom<Z, X> implements Join<Z, X>, MugglJPA {

	protected de.wwu.muggl.jpa.criteria.metamodel.JoinType joinType;
	protected String joinAttribute;
	
	public MugglJoin(String fromEntityClassName, String targetEntityClassName, String joinAttribute, de.wwu.muggl.jpa.criteria.metamodel.JoinType joinType) {
		super(fromEntityClassName, targetEntityClassName);
		this.joinType = joinType;
		this.joinAttribute = joinAttribute;
	}
	
	public MugglJoin join(SymbolicEntityAttribute joinAttribute) {
		return super.join(joinAttribute);
	}
	
	public String getJoinAttributeName() {
		return this.joinAttribute;
	}
	
	public de.wwu.muggl.jpa.criteria.metamodel.JoinType getMugglJoinType() {
		return this.joinType;
	}

	@Override
	public String toString() {
		return "join: source="+sourceEntityClassName+", target="+targetEntityClassName+", attribute="+joinAttribute+", type="+joinType;
	}
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public Join<Z, X> on(Expression<Boolean> restriction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Join<Z, X> on(Predicate... restrictions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Predicate getOn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Attribute<? super Z, ?> getAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public From<?, Z> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JoinType getJoinType() {
		// TODO Auto-generated method stub
		return null;
	}




}
