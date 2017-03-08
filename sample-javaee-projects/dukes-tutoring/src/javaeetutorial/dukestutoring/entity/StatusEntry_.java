package javaeetutorial.dukestutoring.entity;

import java.util.Calendar;
import javaeetutorial.dukestutoring.util.StatusType;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(StatusEntry.class)
public abstract class StatusEntry_ {

	public static volatile SingularAttribute<StatusEntry, Calendar> statusDate;
	public static volatile SingularAttribute<StatusEntry, StatusType> currentStatus;
	public static volatile SingularAttribute<StatusEntry, Student> student;
	public static volatile SingularAttribute<StatusEntry, TutoringSession> tutoringSession;
	public static volatile SingularAttribute<StatusEntry, Long> id;

}

