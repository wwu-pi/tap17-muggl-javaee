package javaeetutorial.dukestutoring.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Address.class)
public abstract class Address_ {

	public static volatile SingularAttribute<Address, String> country;
	public static volatile SingularAttribute<Address, String> province;
	public static volatile SingularAttribute<Address, String> city;
	public static volatile SingularAttribute<Address, Boolean> isPrimary;
	public static volatile SingularAttribute<Address, Person> person;
	public static volatile SingularAttribute<Address, String> postalCode;
	public static volatile SingularAttribute<Address, Boolean> active;
	public static volatile SingularAttribute<Address, String> street1;
	public static volatile SingularAttribute<Address, Long> id;
	public static volatile SingularAttribute<Address, String> street2;

}

