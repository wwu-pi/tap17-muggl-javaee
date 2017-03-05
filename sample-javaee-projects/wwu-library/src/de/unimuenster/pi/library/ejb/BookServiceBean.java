package de.unimuenster.pi.library.ejb;

import java.util.Collection;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.unimuenster.pi.library.jpa.Book;

/**
 * Session Bean implementation class BookService.
 * 
 * Also provides an exemplary web service with two operations.
 * @author Henning Heitkoetter
 */
@Stateless
public class BookServiceBean {
	@PersistenceContext
	private EntityManager em;

	public Book createBook(String name, String author, String isbn) {
		Book newBook = new Book();
		newBook.setTitle(name);
		newBook.setAuthor(author);
		newBook.setIsbn(isbn);
		return createBook(newBook);
	}
	
	public Book createBook(Book book) {
		if (em.createQuery("SELECT COUNT(*) FROM Book b WHERE b.isbn = :isbn", Long.class)
				.setParameter("isbn", book.getIsbn())
				.getSingleResult() > 0)
			throw new EJBException("ISBN already in database");

		em.persist(book);
		return book;
	}

	public Book getBook(int bookId) {
		Book book = em.find(Book.class, bookId);
		if (book == null)
			throw new IllegalArgumentException("Book with not found");
		return book;
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Collection<Book> getAllBooks() {
		return em.createQuery("FROM Book", Book.class).getResultList();
	}
	
	public int getAllBooksCount() {
		int counter = 0;
		for(Book l : getAllBooks()) {
			counter++;
		}
		return counter;
	}
}
