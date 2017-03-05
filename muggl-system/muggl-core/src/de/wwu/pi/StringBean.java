//package de.wwu.pi;
//
//import javax.annotation.PostConstruct;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.criteria.CriteriaBuilder;
//
//import de.wwu.pi.entity.User;
//
//public class StringBean {
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
//    public boolean setNewName(int userId, String newName) {
//    	User user = em.find(User.class, userId);
//    	Integer x = 10;
//    	if(userId > x) {
//    		return true;
//    	}
//    	return false;
////    	
////    	User user = em.find(User.class, userId);
////    	if(!user.getName().equals("Boss")) {
////    		user.setName(newName);
////    	}
//    }
//}
