package de.unimuenster.pi.library.ejb;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.unimuenster.pi.library.jpa.Copy;
import de.unimuenster.pi.library.jpa.Loan;
import de.unimuenster.pi.library.jpa.User;

/**
 * Session Bean implementation class LoanServiceBean
 * 
 * @author Henning Heitkoetter
 */
@Stateless
public class LoanServiceBean {
	@PersistenceContext
	private EntityManager em;

	public Collection<Loan> getLoansOfUser(User user) {
		return em.createQuery("FROM Loan l WHERE l.user = :user", Loan.class).setParameter("user", user).getResultList();
	}
	
	public void returnLoan(Loan loanToReturn, int userId) {
		loanToReturn = em.find(Loan.class, loanToReturn.getId());
		if(loanToReturn.getUser().getUid() == userId){
			loanToReturn.setCopy(null);
			loanToReturn.setUser(null);
			em.remove(loanToReturn);
		}
		else
			throw new IllegalArgumentException("Argument 'user' and user of argument 'loanToReturn' do not match.");
	}

	public void loan(int userId, int copyToLoanInvNo, int loanNumber) {
		Copy copyToLoan = em.find(Copy.class, copyToLoanInvNo);
		if(copyToLoan == null)
			throw new IllegalArgumentException("Invalid inventory number.");
		if(copyToLoan.getLoan() != null)
			throw new IllegalArgumentException("This copy is already lent out.");
		
		User user = em.find(User.class, userId);
		
		Loan newLoan = new Loan();
		newLoan.setId(loanNumber);
		newLoan.setUser(user);
		newLoan.setCopy(copyToLoan);
		
		em.persist(newLoan);
	}

	public int getLoansOfUserCount(User user) {
		int counter = 0;
		for(Loan l : getLoansOfUser(user)) {
			counter++;
		}
		return counter;
	}
}
