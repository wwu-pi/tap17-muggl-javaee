package de.wwu.pi.app.ejb;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.wwu.pi.app.entities.CustomerOrder;

@Stateless
public class OrderService {

	@PersistenceContext
	private EntityManager em;
	
	
	// CustomerOrder has an auto-generated ID
	// method parameter must be the id of the entity that is _pre_execution_required_
	public boolean doesOrderExists(int orderId) {
		return em.find(CustomerOrder.class, orderId) != null;
	}
	
	
}
