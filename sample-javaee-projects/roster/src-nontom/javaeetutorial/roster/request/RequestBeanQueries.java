/**
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.roster.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import javaeetutorial.roster.entity.League;
import javaeetutorial.roster.entity.League_;
import javaeetutorial.roster.entity.Player;
import javaeetutorial.roster.entity.Player_;
import javaeetutorial.roster.entity.SummerLeague;
import javaeetutorial.roster.entity.Team;
import javaeetutorial.roster.entity.Team_;
import javaeetutorial.roster.entity.WinterLeague;
import javaeetutorial.roster.util.IncorrectSportException;
import javaeetutorial.roster.util.LeagueDetails;
import javaeetutorial.roster.util.PlayerDetails;
import javaeetutorial.roster.util.TeamDetails;

/**
 * This is the bean class for the RequestBean enterprise bean.
 *
 * @author ian
 */
@Stateful
public class RequestBeanQueries {

    private static final Logger logger = 
            Logger.getLogger("roster.request.RequestBean");
    @PersistenceContext
    private EntityManager em;
    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = em.getCriteriaBuilder();
    }

    public List<PlayerDetails> getPlayersByPosition(String position) {
        logger.info("getPlayersByPosition");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player.getModel();

                // set the where clause
                cq.where(cb.equal(player.get(Player_.position), position));
                cq.select(player);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }


    public List<PlayerDetails> getPlayersByHigherSalary(String name) {
        logger.info("getPlayersByHigherSalary");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player1 = cq.from(Player.class);
                Root<Player> player2 = cq.from(Player.class);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player1.getModel();

                // create a Predicate object that finds players with a salary
                // greater than player1
                Predicate gtPredicate = cb.greaterThan(
                        player1.get(Player_.salary),
                        player2.get(Player_.salary));
                // create a Predicate object that finds the player based on
                // the name parameter
                Predicate equalPredicate = cb.equal(
                        player2.get(Player_.name),
                        name);
                // set the where clause with the predicates
                cq.where(gtPredicate, equalPredicate);
                // set the select clause, and return only unique entries
                cq.select(player1).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }


    public List<PlayerDetails> getPlayersBySalaryRange(int low, double high) {
        logger.info("getPlayersBySalaryRange");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player.getModel();

                // set the where clause
                cq.where(cb.between(player.get(
                        Player_.salary),
                        (double)low,
                        high));
                // set the select clause
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }


    public List<PlayerDetails> getPlayersByLeagueId(String leagueId) {
        logger.info("getPlayersByLeagueId");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);
                Join<Player, Team> team = player.join(Player_.team);
                Join<Team, League> league = team.join(Team_.league);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player.getModel();

                // set the where clause
                cq.where(cb.equal(league.get(League_.id), leagueId));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }


    public List<PlayerDetails> getPlayersBySport(String sport) {
        logger.info("getPlayersByLeagueId");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);
                Join<Player, Team> team = player.join(Player_.team);
                Join<Team, League> league = team.join(Team_.league);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player.getModel();

                // set the where clause
                cq.where(cb.equal(league.get(League_.sport), sport));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }


    public List<PlayerDetails> getPlayersByCity(String city) {
        logger.info("getPlayersByCity");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);
                Join<Player, Team> team = player.join(Player_.team);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player.getModel();

                // set the where clause
                cq.where(cb.equal(team.get(Team_.city), city));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }


    public List<PlayerDetails> getAllPlayers() {
        logger.info("getAllPlayers");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                cq.select(player);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    public List<PlayerDetails> getPlayersNotOnTeam() {
        logger.info("getPlayersNotOnTeam");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player.getModel();

                // set the where clause
                cq.where(cb.isNull(player.get(Player_.team)));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }


    public List<PlayerDetails> getPlayersByPositionAndName(String position, String name) {
        logger.info("getPlayersByPositionAndName");
        List<Player> players = null;

        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player.getModel();

                // set the where clause
                cq.where(cb.equal(player.get(Player_.position), position),
                        cb.equal(player.get(Player_.name), name));
                cq.select(player).distinct(true);
                TypedQuery<Player> q = em.createQuery(cq);
                players = q.getResultList();
            }
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }


    public List<LeagueDetails> getLeaguesOfPlayer(String playerId) {
        logger.info("getLeaguesOfPlayer");
        List<LeagueDetails> detailsList = new ArrayList<>();
        List<League> leagues = null;

        try {
            CriteriaQuery<League> cq = cb.createQuery(League.class);
            if (cq != null) {
            	Root<Player> player = cq.from(Player.class);
            	Join<Player, Team> team = player.join(Player_.team);
            	Join<Team, League> league = team.join(Team_.league);

                cq.where(cb.equal(player.get(Player_.id), playerId));
                cq.select(player.get(Player_.team).get(Team_.league)).distinct(true);
                TypedQuery<League> q = em.createQuery(cq);
                leagues = q.getResultList();
            }
        } catch (Exception ex) {
            throw new EJBException(ex);
        }

        if (leagues == null) {
            logger.log(Level.WARNING, "No leagues found for player with ID {0}.", playerId);
            return null;
        } else {
            Iterator<League> i = leagues.iterator();
            while (i.hasNext()) {
                League league = (League) i.next();
                LeagueDetails leagueDetails = new LeagueDetails(league.getId(),
                        league.getName(),
                        league.getSport());
                detailsList.add(leagueDetails);
            }

        }
        return detailsList;
    }


    public List<String> getSportsOfPlayer(String playerId) {
        logger.info("getSportsOfPlayer");
        List<String> sports = new ArrayList<>();

        try {
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            if (cq != null) {
                Root<Player> player = cq.from(Player.class);
                Join<Player, Team> team = player.join(Player_.team);
                Join<Team, League> league = team.join(Team_.league);

                // Get MetaModel from Root
                //EntityType<Player> Player_ = player.getModel();

                // set the where clause
                cq.where(cb.equal(player.get(Player_.id), playerId));
                cq.select(league.get(League_.sport)).distinct(true);
                TypedQuery<String> q = em.createQuery(cq);
                sports = q.getResultList();
            }

	        Player player = em.find(Player.class, playerId);
	        Team team = player.getTeam();
            League league = team.getLeague();
            sports.add(league.getSport());        
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return sports;
    }

    private List<PlayerDetails> copyPlayersToDetails(List<Player> players) {
        List<PlayerDetails> detailsList = new ArrayList<>();
        Iterator<Player> i = players.iterator();
        while (i.hasNext()) {
            Player player = (Player) i.next();
            PlayerDetails playerDetails = new PlayerDetails(player.getId(),
                    player.getName(),
                    player.getPosition(),
                    player.getSalary());
            detailsList.add(playerDetails);
        }
        return detailsList;
    }
}
