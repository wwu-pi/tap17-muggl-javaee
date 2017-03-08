/**
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package javaeetutorial.roster.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javaeetutorial.roster.entity.League;
import javaeetutorial.roster.entity.Player;
import javaeetutorial.roster.entity.SummerLeague;
import javaeetutorial.roster.entity.Team;
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
public class RequestBeanNoCriteria {

    private static final Logger logger = 
            Logger.getLogger("roster.request.RequestBean");
    @PersistenceContext
    private EntityManager em;

    
    public void createPlayer(String id,
            String name,
            String position,
            double salary) {
        logger.info("createPlayer");
        try {
            Player player = new Player(id, name, position, salary);
            em.persist(player); em.flush();
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }
    
    public void addPlayer(String playerId, String teamId) {
        logger.info("addPlayer");
        try {
            Player player = em.find(Player.class, playerId);
            Team team = em.find(Team.class, teamId);

            team.addPlayer(player);
            player.addTeam(team);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }
    
    public void removePlayer(String playerId) {
        logger.info("removePlayer");
        try {
            Player player = em.find(Player.class, playerId);

            Collection<Team> teams = player.getTeams();
            Iterator<Team> i = teams.iterator();
            while (i.hasNext()) {
                Team team = i.next();
                team.dropPlayer(player);
            }

            em.remove(player);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    
    public void dropPlayer(String playerId, String teamId) {
        logger.info("dropPlayer");
        try {
            Player player = em.find(Player.class, playerId);
            Team team = em.find(Team.class, teamId);

            team.dropPlayer(player);
            player.dropTeam(team);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    
    public PlayerDetails getPlayer(String playerId) {
        logger.info("getPlayerDetails");
        try {
            Player player = em.find(Player.class, playerId);
            PlayerDetails playerDetails = new PlayerDetails(player.getId(),
                    player.getName(),
                    player.getPosition(),
                    player.getSalary());
            return playerDetails;
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    
    public List<PlayerDetails> getPlayersOfTeam(String teamId) {
        logger.info("getPlayersOfTeam");
        List<PlayerDetails> playerList = null;
        try {
            Team team = em.find(Team.class, teamId);
            playerList = this.copyPlayersToDetails((List<Player>) team.getPlayers());
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return playerList;
    }

    
    public List<TeamDetails> getTeamsOfLeague(String leagueId) {
        logger.info("getTeamsOfLeague");
        List<TeamDetails> detailsList = new ArrayList<>();
        Collection<Team> teams = null;

        try {
            League league = em.find(League.class, leagueId);
            teams = league.getTeams();
        } catch (Exception ex) {
            throw new EJBException(ex);
        }

        Iterator<Team> i = teams.iterator();
        while (i.hasNext()) {
            Team team = (Team) i.next();
            TeamDetails teamDetails = new TeamDetails(team.getId(),
                    team.getName(),
                    team.getCity());
            detailsList.add(teamDetails);
        }
        return detailsList;
    }

    
    public void createTeamInLeague(TeamDetails teamDetails, String leagueId) {
        logger.info("createTeamInLeague");
        try {
            League league = em.find(League.class, leagueId);
            Team team = new Team(teamDetails.getId(),
                    teamDetails.getName(),
                    teamDetails.getCity());
            em.persist(team);
            team.setLeague(league);
            league.addTeam(team);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    
    public void removeTeam(String teamId) {
        logger.info("removeTeam");
        try {
            Team team = em.find(Team.class, teamId);

            Collection<Player> players = team.getPlayers();
            Iterator<Player> i = players.iterator();
            while (i.hasNext()) {
                Player player = (Player) i.next();
                player.dropTeam(team);
            }

            em.remove(team);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    
    public TeamDetails getTeam(String teamId) {
        logger.info("getTeam");
        TeamDetails teamDetails = null;

        try {
            Team team = em.find(Team.class, teamId);
            teamDetails = new TeamDetails(team.getId(), team.getName(), team.getCity());
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return teamDetails;
    }
    
    public void removeLeague(String leagueId) {
        logger.info("removeLeague");
        try {
            League league = em.find(League.class, leagueId);
            em.remove(league);
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
    }

    
    public LeagueDetails getLeague(String leagueId) {
        logger.info("getLeague");
        LeagueDetails leagueDetails = null;

        try {
            League league = em.find(League.class, leagueId);
            leagueDetails = new LeagueDetails(league.getId(),
                    league.getName(),
                    league.getSport());
        } catch (Exception ex) {
            throw new EJBException(ex);
        }
        return leagueDetails;
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
