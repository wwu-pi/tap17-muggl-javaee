package javaeetutorial.roster.entity;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;


@StaticMetamodel(Team.class)
public class Team_ {
  public static volatile SingularAttribute<Team, String> id;
  public static volatile SingularAttribute<Team, String> name;
  public static volatile SingularAttribute<Team, League> league;
  public static volatile SingularAttribute<Team, String> city;
  public static volatile CollectionAttribute<Team, Player> players;
}
