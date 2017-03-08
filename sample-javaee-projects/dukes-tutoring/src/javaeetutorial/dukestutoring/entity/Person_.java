package javaeetutorial.dukestutoring.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Person.class)
public abstract class Person_ {

	public static volatile SingularAttribute<Person, String> firstName;
	public static volatile SingularAttribute<Person, String> lastName;
	public static volatile ListAttribute<Person, Address> addresses;
	public static volatile SingularAttribute<Person, String> password;
	public static volatile SingularAttribute<Person, String> mobilePhone;
	public static volatile SingularAttribute<Person, String> homePhone;
	public static volatile SingularAttribute<Person, String> nickname;
	public static volatile SingularAttribute<Person, String> middleName;
	public static volatile SingularAttribute<Person, PersonDetails> details;
	public static volatile SingularAttribute<Person, Long> id;
	public static volatile SingularAttribute<Person, String> suffix;
	public static volatile SingularAttribute<Person, String> email;

}

