package javaeetutorial.dukestutoring.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Guardian.class)
public abstract class Guardian_ extends javaeetutorial.dukestutoring.entity.Person_ {

	public static volatile ListAttribute<Guardian, Student> students;
	public static volatile SingularAttribute<Guardian, Boolean> active;

}

