package javaeetutorial.dukestutoring.entity;

import javaeetutorial.dukestutoring.util.StatusType;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Student.class)
public abstract class Student_  extends Person_ {

	public static volatile ListAttribute<Student, TutoringSession> sessions;
	public static volatile SingularAttribute<Student, String> school;
	public static volatile SingularAttribute<Student, Integer> grade;
	public static volatile SingularAttribute<Student, Boolean> active;
	public static volatile ListAttribute<Student, Guardian> guardians;
	public static volatile ListAttribute<Student, StatusEntry> statusEntries;
	public static volatile SingularAttribute<Student, StatusType> status;
}

