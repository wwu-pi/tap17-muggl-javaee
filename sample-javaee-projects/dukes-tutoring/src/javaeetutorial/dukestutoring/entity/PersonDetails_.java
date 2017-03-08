package javaeetutorial.dukestutoring.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(PersonDetails.class)
public abstract class PersonDetails_ {

	public static volatile SingularAttribute<PersonDetails, Date> birthday;
	public static volatile SingularAttribute<PersonDetails, Person> person;
	public static volatile SingularAttribute<PersonDetails, byte[]> photo;
	public static volatile SingularAttribute<PersonDetails, Long> id;

}

