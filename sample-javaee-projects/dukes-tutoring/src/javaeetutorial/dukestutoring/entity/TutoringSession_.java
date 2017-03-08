package javaeetutorial.dukestutoring.entity;

import java.util.Calendar;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TutoringSession.class)
public abstract class TutoringSession_ {

	public static volatile SingularAttribute<TutoringSession, Calendar> sessionDate;
	public static volatile ListAttribute<TutoringSession, Student> students;
	public static volatile SingularAttribute<TutoringSession, Long> id;
	public static volatile ListAttribute<TutoringSession, StatusEntry> statusEntries;

}

