//package de.wwu.pi;
//
//import java.util.List;
//
//import javax.persistence.EntityManager;
//import javax.persistence.EntityManagerFactory;
//import javax.persistence.Persistence;
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
//import de.wwu.pi.entity.TeamInt;
//
//public class RequestBeanNonJTA {
//
//	public static void main(String[] args) {
//		foobar();
//	}
//	
//	public static void foobar() {
//		List<PlayerInt> players = null;
//
//		EntityManagerFactory emf = Persistence.createEntityManagerFactory("NONJTAPersistenceUnit");
//		EntityManager em = emf.createEntityManager();
//		CriteriaBuilder cb = em.getCriteriaBuilder();
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
//                            42));
//            cq.select(player);
//
//            TypedQuery<PlayerInt> q = em.createQuery(cq);
//            players = q.getResultList();
//            
//            System.out.println("players is of type: " + players.getClass().getName());
//        }
//
//	}
//}
