//package de.wwu.pi;
//
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//
//import javax.annotation.PostConstruct;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.AbstractQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.From;
//import javax.persistence.criteria.Join;
//import javax.persistence.criteria.Path;
//import javax.persistence.criteria.Root;
//
//import de.wwu.pi.entity.LeagueInt;
//import de.wwu.pi.entity.PlayerInt;
//import de.wwu.pi.entity.PlayerDetails;
//import de.wwu.pi.entity.Player_;
//import de.wwu.pi.entity.TeamInt;
//import de.wwu.pi.entity.Team_;
//
//public class RequestBean {
//
//    @PersistenceContext
//    private EntityManager em;
//
//    private CriteriaBuilder cb;
//    
//    @PostConstruct
//    private void init() {
//        cb = em.getCriteriaBuilder();
//    }
//    
//    public void addSalary(int playerId, int extraSalary) {
//    	createPlayer(0,0,0,20);
//    	createPlayer(1,0,0,20);
//    	createPlayer(2,0,0,20);
//    	
//    	PlayerInt player = em.find(PlayerInt.class, playerId);
//    	player.setSalary(player.getSalary()+extraSalary+10);
//    }
//    
//    public void createPlayer(int id, int name, int position, int salary) {
//        PlayerInt player = new PlayerInt(id, name, position, salary);
//        em.persist(player);
//    }
//    
//    public void addPlayer(int playerId, int teamId) {
//        PlayerInt player = em.find(PlayerInt.class, playerId);
//        TeamInt team = em.find(TeamInt.class, teamId);
//        team.addPlayer(player);
//        player.addTeam(team);
//    }
//    
//    public void removePlayer(int playerId) {
//        PlayerInt player = em.find(PlayerInt.class, playerId);
//
//        Collection<TeamInt> teams = player.getTeams();
//        Iterator<TeamInt> i = teams.iterator();
//
//        while (i.hasNext()) {
//            TeamInt team = i.next();
//            team.dropPlayer(player);
//        }
//
//        em.remove(player);
//    }
//    
//    public void removePlayer2(int playerId) {
//        PlayerInt player = em.find(PlayerInt.class, playerId);
//
//        Collection<TeamInt> teams = player.getTeams();
//        Iterator<TeamInt> i = teams.iterator();
//
//        if(teams.size() > 3) {
//            TeamInt team = i.next();
//            team.dropPlayer(player);
//        }
//
//        em.remove(player);
//    }
//    
//    public void dropPlayer(int playerId, int teamId) {
//        PlayerInt player = em.find(PlayerInt.class, playerId);
//        TeamInt team = em.find(TeamInt.class, teamId);
//
//        team.dropPlayer(player);
//        player.dropTeam(team);
//    }
//    
//    
//    public PlayerDetails getPlayer(String playerId) {
//        PlayerInt player = em.find(PlayerInt.class, playerId);
//        PlayerDetails playerDetails = new PlayerDetails(
//                    player.getId(),
//                    player.getName(),
//                    player.getPosition(),
//                    player.getSalary());
//
//        return playerDetails;
//    }
//    
//    public List<PlayerInt> getPlayersByPosition(int position) {
//        List<PlayerInt> players = null;
//
//        CriteriaQuery<PlayerInt> cq = cb.createQuery(PlayerInt.class);
//
//        if (cq != null) {
//        	AbstractQuery<PlayerInt> aq = (AbstractQuery<PlayerInt>)cq;
//        	Path<PlayerInt> player = (Path<PlayerInt>)aq.from(PlayerInt.class );
//
//            cq.where(
//                    cb.equal(
//                        (player.<Integer>get("position")),
//                        position));
//            cq.select(player);
//
//            TypedQuery<PlayerInt> q = em.createQuery(cq);
//            players = q.getResultList();
//        }
//
//        return players;
//    }
//    
//    public List<PlayerInt> getPlayersBySalaryRange(int low, int high) {
//    	List<PlayerInt> players = null;
//
//        CriteriaQuery<PlayerInt> cq = cb.createQuery(PlayerInt.class);
//
//        if (cq != null) {
//        	AbstractQuery<PlayerInt> aq = (AbstractQuery<PlayerInt>)cq;
//        	Path<PlayerInt> player = (Path<PlayerInt>)aq.from(PlayerInt.class );
//
//            cq.where(
//            		cb.between(
//                            player.<Integer>get("salary"),
//                            low,
//                            high));
//            cq.select(player);
//
//            TypedQuery<PlayerInt> q = em.createQuery(cq);
//            players = q.getResultList();
//        }
//
//        return players;
//    }
//    
//    public List<PlayerInt> getPlayersByLeagueId(int leagueId) {
//    	List<PlayerInt> players = null;
//
//        CriteriaQuery<PlayerInt> cq = cb.createQuery(PlayerInt.class);
//
//        if (cq != null) {
//        	AbstractQuery<PlayerInt> aq = (AbstractQuery<PlayerInt>)cq;
//        	Root<PlayerInt> root = aq.from(PlayerInt.class);
//        	Path<PlayerInt> player = (Path<PlayerInt>)root;
//        	From<PlayerInt, PlayerInt> playerFrom = (From<PlayerInt, PlayerInt>)root;
//        	Join<PlayerInt, TeamInt> team = playerFrom.join("teams");
//        	From<PlayerInt, TeamInt> teamFrom = (From<PlayerInt, TeamInt>)team;
//            Join<TeamInt, LeagueInt> league = teamFrom.join("league");
//            Path<LeagueInt> leaguePath = (Path<LeagueInt>)league;
//            
//            cq.where(
//            		cb.equal(
//            				leaguePath.<Integer>get("id"),
//                            leagueId));
//            cq.select(player);
//
//            TypedQuery<PlayerInt> q = em.createQuery(cq);
//            players = q.getResultList();
//        }
//        boolean foo = false;
//        for(PlayerInt player : players) {
//        	if(player.getTeams().size() > 7) {
//        		foo = true;
//        	}
//        }
//
//        return players;
//    }
//    
//    public void addSalary2(int playerId, int extraSalary) {
//    	PlayerInt player = em.find(PlayerInt.class, playerId);
//    	player.setSalary(player.getSalary()+extraSalary+10);
//    }
//
//}
