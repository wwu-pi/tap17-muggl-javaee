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
//import javax.persistence.criteria.CriteriaBuilder;
//
//import de.wwu.pi.entity.LeagueInt;
//import de.wwu.pi.entity.PlayerInt;
//import de.wwu.pi.entity.SimpleStringEntity;
//import de.wwu.pi.entity.TeamInt;
//
//public class Evaluation {
//
//    @PersistenceContext
//    private EntityManager em;
//
////    private CriteriaBuilder cb;
////    
////    @PostConstruct
////    private void init() {
////        cb = em.getCriteriaBuilder();
////    }
//
//    public List<TeamInt> getTeamsOfLeague(String leagueId) {
//
//        List<TeamInt> detailsList = new ArrayList<TeamInt>();
//        Collection<TeamInt> teams = null;
//
//            LeagueInt league = em.find(LeagueInt.class, leagueId);
//            teams = league.getTeams();
//        
//
//        Iterator<TeamInt> i = teams.iterator();
//
//        while (i.hasNext()) {
//            TeamInt team = (TeamInt) i.next();
//            detailsList.add(team);
//        }
//
//        return detailsList;
//    }
//    
//}
