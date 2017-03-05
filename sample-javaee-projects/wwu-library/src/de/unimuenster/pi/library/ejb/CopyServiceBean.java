package de.unimuenster.pi.library.ejb;

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.unimuenster.pi.library.jpa.Book;
import de.unimuenster.pi.library.jpa.CD;
import de.unimuenster.pi.library.jpa.Copy;
import de.unimuenster.pi.library.jpa.Loan;
import de.unimuenster.pi.library.jpa.User;

/**
 * Session Bean implementation class CopyServiceBean.
 * 
 * @author Henning Heitkoetter
 */
@Stateless
public class CopyServiceBean { 
	@PersistenceContext
	private EntityManager em;

	public Collection<Copy> getCopiesOfBook(Book m) {
		return em.createQuery("FROM Copy c WHERE c.medium=:m", Copy.class).setParameter("m", m).getResultList();
	}
	
	public Collection<Copy> getCopiesOfCD(CD m) {
		return em.createQuery("FROM Copy c WHERE c.medium=:m", Copy.class).setParameter("m", m).getResultList();
	}
	
	public int list_getCopiesOfCD(CD cd) {
		int counter = 0;
		for(Copy l : getCopiesOfCD(cd)) {
			counter++;
		}
		return counter;
	}

	public Collection<Copy> createCDCopies(CD m, int count) {
		Collection<Copy> result = new ArrayList<Copy>();
		m = em.find(CD.class, m.getId());
		for(int i = 0; i < count; i++){
			Copy newCopy = new Copy();
			newCopy.setMedium(m);
			em.persist(newCopy);
			result.add(newCopy);
		}
		return result;
	}
	
	public Collection<Copy> createBookCopies(Book m, int count) {
		Collection<Copy> result = new ArrayList<Copy>();
		m = em.find(Book.class, m.getId());
		for(int i = 0; i < count; i++){
			Copy newCopy = new Copy();
			newCopy.setMedium(m);
			em.persist(newCopy);
			result.add(newCopy);
		}
		return result;
	}

	public Copy getCopy(int invNo) {
		return em.find(Copy.class, invNo);
	}
	
	public int getCopiesOfBookCount(Book book) {
		int counter = 0;
		for(Copy l : getCopiesOfBook(book)) {
			counter++;
		}
		return counter;
	}
}
