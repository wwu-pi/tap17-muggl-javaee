//package de.wwu.pi;
//
//import java.util.List;
//
//import javax.persistence.EntityManager;
//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.AbstractQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Path;
//import javax.persistence.criteria.Predicate;
//
//import de.wwu.pi.entity.SimpleOne;
//
//public class DemoService {
//	
//	private EntityManager entityManager;
//	
//	public DemoService(EntityManager entityManager)  {
//		this.entityManager = entityManager;
//	}
//
//	public boolean criteria() {
//		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
//		CriteriaQuery<SimpleOne> criteria = builder.createQuery(SimpleOne.class );
//		AbstractQuery<SimpleOne> aq = (AbstractQuery<SimpleOne>)criteria;
//		Path<SimpleOne> path = (Path<SimpleOne>)aq.from( SimpleOne.class );
//		criteria.select(path);
//		
//		Path<SimpleOne> p = path.get("simpleId");
//		Predicate pr = builder.equal(p, 30);
//		
//		CriteriaQuery<SimpleOne> cA = criteria.where(pr);
//		
////		criteria.where( builder.equal( path.get( "simpleId" ), 30 ) );
//		TypedQuery<SimpleOne> thequery = entityManager.createQuery( criteria );
//		List<SimpleOne> people = thequery.getResultList();
//		if(people.size() > 0) {
//			return true;
//		}
//		return false;
//	}
//}
