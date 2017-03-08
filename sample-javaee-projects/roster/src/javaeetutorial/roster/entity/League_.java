package javaeetutorial.roster.entity;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;


@StaticMetamodel(League.class)
public class League_ {

	  public static volatile SingularAttribute<League, String> id;
	  public static volatile SingularAttribute<League, String> name;
	  public static volatile SingularAttribute<League, String> sport;
	  public static volatile CollectionAttribute<League, Team> teams;
}
