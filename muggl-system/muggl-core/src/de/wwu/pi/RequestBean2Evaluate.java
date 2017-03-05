//package de.wwu.pi;
//
//import de.wwu.pi.entity.Spieler;
//import de.wwu.pi.entity.User;
//
//public class RequestBean2Evaluate {
//
////    @PersistenceContext
////    private EntityManager em;
////
////    private CriteriaBuilder cb;
////    
////    @PostConstruct
////    private void init() {
////        cb = em.getCriteriaBuilder();
////    }
//    
//	public boolean simpleStringTest(String x) {
//		if(x.equals("bar")) {
//			return true;
//		}
//		return false;
//	}
//	
//	public boolean doArrayUserTest(User user) {
//		if(user.getPlayerString().size() == 2) {
//			return true;
//		}
//		return false;
//	}
//	
//	public boolean doArrayListLoopTest(User user) {
//		for(String player : user.getPlayerString()) {
//			if(player.equals("foo")) {
//				return true;
//			}
//		}		
//		return false;
//	}
//	
//	public boolean doArrayListLoopTest2(User user) {
//		for(Spieler spieler : user.getSpieler()) {
//			if(spieler.getName().equals("foo")) {
//				return true;
//			}
//		}		
//		return false;
//	}
//	
//	public boolean doUserTest(User user) {
//		if(user.getName().equals("foo")) {
//			return true;
//		}
//		return false;
//	}
//	
//	public boolean doStringCompare(int n) {
//		while(n-- != 0) {
//			if(5 < n) {
//				return false;
//			}
//		}
//		return true;
//	}
//    
////    public void createPlayer(
////        String id,
////        String name,
////        String position,
////        double salary) {
////        try {
////            NewPlayer player = new NewPlayer(id, name, position, salary);
////            em.persist(player);
////        } catch (Exception ex) {
////            throw new EJBException(ex);
////        }
////    }
//}
