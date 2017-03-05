//package de.wwu.pi;
//
//import javax.annotation.PostConstruct;
//import javax.ejb.Stateless;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.criteria.CriteriaBuilder;
//
//import de.wwu.pi.entity.PlayerInt;
//import de.wwu.pi.entity.PlayerDetails;
//
//@Stateless
//public class A_RequestBean {
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
//}
