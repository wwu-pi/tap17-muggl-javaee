package de.wwu.pi.app.ejb;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.wwu.pi.app.entities.Customer;
import de.wwu.pi.app.entities.CustomerOrder;
import de.wwu.pi.app.util.CustomerNotFoundException;
import de.wwu.pi.app.util.NoMagicException;

@Stateless
public class CustomerService {

	@PersistenceContext
	private EntityManager em;
	
	private int magic;
	
	@PostConstruct
	public void init() {
		magic = 2;
	}
	
	/**
	 * Get a customer with the given <code>customerId</code>.
	 * If no customer exists with this id, throw exception.
	 */
	public Customer getCustomer(String customerId) throws CustomerNotFoundException {
		Customer c = em.find(Customer.class, customerId);
		if(c != null) {
			return c;
		}
		throw new CustomerNotFoundException(customerId);
	}
	
	/**
	 * Get a customer with the given <code>customerId</code>.
	 * If no customer exists with this id, throw exception.
	 */
	public Customer getCustomerWithDeadCode(String customerId) throws CustomerNotFoundException {
		Customer x = new Customer();
		x.setId(customerId); x.setName("Max"); x.setStatus(1);
		em.persist(x); em.flush(); // customer with the given id must exists
		Customer c = em.find(Customer.class, customerId);
		if(c != null) { // always true
			return c;
		}
		throw new CustomerNotFoundException(customerId); // dead code, customer must exists!
	}
	
	/**
	 * Get the magic customer with the given <code>customerId</code> and the magic status.
	 */
	public Customer getMagicCustomer(String customerId) throws CustomerNotFoundException, NoMagicException {
		Customer c = em.find(Customer.class, customerId);
		if(c != null) {
			if(c.getStatus() == magic * magic) {
				return c;
			}
			throw new NoMagicException();
		}
		throw new CustomerNotFoundException(customerId);
	}
	
	

	public void incrementStatus(int s, Date d, long r) {
		String ql = "SELECT oi.order.customer " 
					+ "FROM OrderItem oi " 
					+ "  JOIN oi.order o " 
					+ "  JOIN o.customer c "
					+ "WHERE o.orderDate > :d "
					+ "  AND oi.price <= 10 "
					+ "GROUP BY c.id " 
					+ "HAVING SUM(oi.price) > :r";
		List<Customer> cList = em.createQuery(ql, Customer.class)
				.setParameter("r", r)
				.setParameter("d", d)
				.getResultList();
		for (Customer c : cList) {
			if (c.getStatus() < s) {
				c.setStatus(s);
			}
		}
	}
	
	public void incrementStatus(int s, Date d) {
		incrementStatus(s, d, 15);
	}
}
