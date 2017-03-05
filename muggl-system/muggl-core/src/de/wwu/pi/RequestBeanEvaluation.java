//package de.wwu.pi;
//
//import java.util.ArrayList;
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
//import javax.persistence.criteria.Path;
//import javax.persistence.criteria.Root;
//import javax.persistence.metamodel.EntityType;
//import javax.persistence.metamodel.SingularAttribute;
//
//import de.wwu.pi.entity.PlayerInt;
//import de.wwu.pi.entity.Player_;
//import de.wwu.pi.entity.SimpleOne;
//import de.wwu.pi.entity.SimpleStringEntity;
//import de.wwu.pi.entity.TeamInt;
//
//public class RequestBeanEvaluation {
//
//	@PersistenceContext
//    private EntityManager em;
//	
////	private int zahl;
////	
////	@PostConstruct
////	public void initThisBean() {
////		zahl = 42;
////	}
//	
//    public SingularAttribute<PlayerInt, Integer> salary;
//    
//    public void doStringSelect(String name) {
//    	em.find(SimpleStringEntity.class, name);
//    }
//
//    
//    public void doSimpleSelect(int pid) {
//    	PlayerInt player = em.find(PlayerInt.class, pid);
//    	player.addTeam(new TeamInt());
//    }
//
//    
//	
//	public void createPlayer(int id, int name, int position, int salary) {
//		PlayerInt player = new PlayerInt(id, name, position, salary);
//        em.persist(player);
//        // TODO: fuer diesen Testfall noch die db constraints einbauen
//	}
//	
//	public boolean addPlayerOK(int playerId) {
//		PlayerInt player = em.find(PlayerInt.class, playerId);
//		if(player == null) {
//			return false;
//		}
//		return true;
//	}
//	
//	public void addPlayer(int playerId, int teamId) {
//		PlayerInt player = em.find(PlayerInt.class, playerId);
//		TeamInt team = em.find(TeamInt.class, teamId);
//		team.addPlayer(player);
//		player.addTeam(team);
//	}
//	
//    public boolean simpleCollection(int playerId) {
//        PlayerInt player = em.find(PlayerInt.class, playerId);
//
//        Collection<TeamInt> teams = player.getTeams();
//
//        if(teams.size() > 3) {
//        	return true;
//        }
//
//        return false;
//    }
//    
//    
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
//    public void removePlayerTest(int playerId) {
//    	Collection<TeamInt> teamsNew = new ArrayList<TeamInt>();
//    	teamsNew.add(new TeamInt(1,1,2));
//        PlayerInt player = new PlayerInt(playerId, 2,2,2);
//        player.setTeams(teamsNew);
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
//    public List<PlayerInt> getPlayersBySalaryRange(int low, int high) {
//
//            List<PlayerInt> players = null;
//            
//            CriteriaBuilder cb = em.getCriteriaBuilder();
//
//            CriteriaQuery<PlayerInt> cq = cb.createQuery(PlayerInt.class);
//
//            if (cq != null) {
//            	
//            	// eigentlich sollte es so aufgerufen werden
//            	// da aber muggl nicht interface hierachie aufloesen kann
//            	// mahcen wir das jetzt hier selbst/manuell... mit casting usw.
//            	// Root<Player> player = cq.from(Player.class);
//
//            	AbstractQuery<PlayerInt> aq = (AbstractQuery<PlayerInt>)cq;
//            	Path<PlayerInt> player = (Path<PlayerInt>)aq.from(PlayerInt.class );
//            	
//        		// Get MetaModel from Root
////                EntityType<Player> Player_ = player.getModel();
//        		
//                // set the where clause
//                cq.where(
//                        cb.between(player.<Integer>get("salary"),
//                            low,
//                            high));
//                // set the select clause
//                cq.select(player);
//
//                TypedQuery<PlayerInt> q = em.createQuery(cq);
//                players = q.getResultList();
//            }
//
//            return players;
//        }
//
//	
//}
