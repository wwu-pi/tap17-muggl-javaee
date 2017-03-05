package de.unimuenster.pi.library.ejb;

import java.util.Collection;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.unimuenster.pi.library.jpa.User;

/**
 * Session Bean implementation class UserServiceBean.
 * 
 * @author Henning Heitkoetter
 */
@Stateless
public class UserServiceBean { 
	@PersistenceContext
	private EntityManager em;
	
	public User createUser(String name, String address) {
		User newUser = new User();
		newUser.setName(name);
		newUser.setAddress(address);
		return createUser(newUser);
	}

	public User createUser(User newUser) {
		em.persist(newUser);
		return newUser;
	}
	
	public User getUser(int userId) {
		User user = em.find(User.class, userId);
		if(user == null)
			throw new IllegalArgumentException("User not found");
		return user;
	}

	public Collection<User> getAllUsers() {
		return em.createQuery("FROM User", User.class).getResultList();
	}
		
	public int getAllUsersCount() {
		int counter = 0;
		for(User u : getAllUsers()) {
			counter++;
		}
		return counter;
	}
}
