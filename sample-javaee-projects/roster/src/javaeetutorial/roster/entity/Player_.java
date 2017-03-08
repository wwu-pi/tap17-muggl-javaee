package javaeetutorial.roster.entity;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Player.class)
public class Player_ {
  public static volatile SingularAttribute<Player, String> id;
  public static volatile SingularAttribute<Player, String> name;
  public static volatile SingularAttribute<Player, String> position;
  public static volatile SingularAttribute<Player, Double> salary;
  public static volatile CollectionAttribute<Player, Team> teams;
}
