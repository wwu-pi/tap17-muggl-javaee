package de.wwu.muggl.jpa.criteria;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import de.wwu.muggl.jpa.criteria.meta.MugglJPA;
import de.wwu.muggl.jpa.criteria.metamodel.SymbolicEntityAttribute;
import de.wwu.muggl.vm.classfile.structures.Attribute;
import de.wwu.muggl.vm.classfile.structures.Field;
import de.wwu.muggl.vm.classfile.structures.Method;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeRuntimeVisibleAnnotations;
import de.wwu.muggl.vm.classfile.structures.attributes.AttributeSignature;
import de.wwu.muggl.vm.classfile.structures.constants.ConstantUtf8;

public class MugglFrom<Z, X> extends MugglPath<X> implements From<Z, X>, FetchParent<Z,X>, MugglJPA {

	protected Set<MugglJoin<?,?>> joinSet;
	protected String sourceEntityClassName;
	protected String targetEntityClassName;
	
	public MugglFrom(String sourceEntityClassName, String targetEntityClassName) {
		super(targetEntityClassName);
		this.sourceEntityClassName = sourceEntityClassName;
		this.targetEntityClassName = targetEntityClassName;
		this.joinSet = new HashSet<>();
	}
	
	public String getSourceEntityName() {
		return sourceEntityClassName;
	}
	
	public String getTargetEntityName() {
		return targetEntityClassName;
	}
	
	public void addJoin(MugglJoin<?,?> join) {
		this.joinSet.add(join);
	}
	
	public Set<MugglJoin<?,?>> getJoinSet() {
		return this.joinSet;
	}
	
	public MugglJoin join(SymbolicEntityAttribute joinAttribute) {
		String type = joinAttribute.getField().getType();
		if(type.equals("javax.persistence.metamodel.CollectionAttribute") || type.equals("javax.persistence.metamodel.SingularAttribute")) {
			byte index = ((AttributeSignature)joinAttribute.getField().getAttributes()[0]).getBytes()[1];
			ConstantUtf8 signature = (ConstantUtf8)joinAttribute.getField().getClassFile().getConstantPool()[index];
			signature.getStringValue();
			
			String[] types = signature.getStringValue().substring(type.length()+2, signature.getStringValue().length()-2).split(";");
			
			String fromType = types[0].substring(1).replace("/", ".");
			String targetType = types[1].substring(1).replace("/", ".");
			
			de.wwu.muggl.jpa.criteria.metamodel.JoinType joinType = null;

			// first, check the field for annotations like @OneToMany, etc...
			Field entityAttributeField = joinAttribute.getEntityClassFile().getFieldByName(joinAttribute.getField().getName());
			
			// second, check the method, if annotation at field is not found..
			String getterMethodName = "get"+joinAttribute.getField().getName().substring(0, 1).toUpperCase()+joinAttribute.getField().getName().substring(1);
			for(Method method : joinAttribute.getEntityClassFile().getMethods()) {
				if(method.getName().equals(getterMethodName)) {
					for(Attribute attribute : method.getAttributes()) {
						if(attribute instanceof AttributeRuntimeVisibleAnnotations) {
							int idx = ((AttributeRuntimeVisibleAnnotations)attribute).getAnnotations()[0].getTypeIndex();
							String annotationName = joinAttribute.getEntityClassFile().getConstantPool()[idx].getStringValue();
							if(annotationName.equals("Ljavax/persistence/OneToMany;")) {
								joinType = de.wwu.muggl.jpa.criteria.metamodel.JoinType.ONE_TO_MANY;
							} else if(annotationName.equals("Ljavax/persistence/OneToOne;")) {
								joinType = de.wwu.muggl.jpa.criteria.metamodel.JoinType.ONE_TO_ONE;
							} else if(annotationName.equals("Ljavax/persistence/ManyToOne;")) {
								joinType = de.wwu.muggl.jpa.criteria.metamodel.JoinType.MANY_TO_ONE;
							} else if(annotationName.equals("Ljavax/persistence/ManyToMany;")) {
								joinType = de.wwu.muggl.jpa.criteria.metamodel.JoinType.MANY_TO_MANY;
							}
						}
					}
				}
			}
			
			
			MugglJoin join = new MugglJoin<>(fromType, targetType, joinAttribute.getFieldName(), joinType);

			this.joinSet.add(join);
			
			return join;
		}
		
		
		
		return null;
	}
	
	

	
	
	
	
	
	
	
	
	
	
	@Override
	public Set<Fetch<X, ?>> getFetches() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute,
			JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute,
			JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> Fetch<X, Y> fetch(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> Fetch<X, Y> fetch(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Join<X, ?>> getJoins() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCorrelated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public From<Z, X> getCorrelationParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> Join<X, Y> join(SingularAttribute<? super X, Y> attribute,
			JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> CollectionJoin<X, Y> join(
			CollectionAttribute<? super X, Y> collection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> CollectionJoin<X, Y> join(
			CollectionAttribute<? super X, Y> collection, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> SetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <Y> ListJoin<X, Y> join(ListAttribute<? super X, Y> list,
			JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapJoin<X, K, V> join(MapAttribute<? super X, K, V> map,
			JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> Join<X, Y> join(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> SetJoin<X, Y> joinSet(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> ListJoin<X, Y> joinList(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> Join<X, Y> join(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> CollectionJoin<X, Y> joinCollection(String attributeName,
			JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> SetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, Y> ListJoin<X, Y> joinList(String attributeName, JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X, K, V> MapJoin<X, K, V> joinMap(String attributeName,
			JoinType jt) {
		// TODO Auto-generated method stub
		return null;
	}


}
