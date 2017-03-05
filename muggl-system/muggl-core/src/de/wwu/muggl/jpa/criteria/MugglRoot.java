package de.wwu.muggl.jpa.criteria;

import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import de.wwu.muggl.jpa.criteria.meta.MugglJPA;
import de.wwu.muggl.jpa.criteria.metamodel.SymbolicEntityAttribute;
import de.wwu.muggl.solvers.expressions.IntConstant;
import de.wwu.muggl.vm.initialization.Arrayref;
import de.wwu.muggl.vm.initialization.Objectref;

public class MugglRoot<X> extends MugglFrom<X,X> implements Root<X>, MugglJPA {

	public MugglRoot(String entityClassName) {
		super(entityClassName, entityClassName);
	}

	public EntityType<X> getModel() {
		return null;
	}
	
	public MugglJoin join(SymbolicEntityAttribute joinAttribute) {
		return super.join(joinAttribute);
	}
}
