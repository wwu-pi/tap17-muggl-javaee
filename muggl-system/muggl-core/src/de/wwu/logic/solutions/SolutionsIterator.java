package de.wwu.logic.solutions;

import java.util.Iterator;

/**
 * Iterator for {@link Solutions}.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-20
 * @param <E> The type of the solution.
 */
public class SolutionsIterator<E> implements Iterator<Solution<E>> {
	protected Iterator<Solution<E>> iterator;
	
	/**
	 * Initialize the Iterator. This constructor has package visibility only.
	 * 
	 * @param solutions The solutions to iterate.
	 */
	SolutionsIterator(Solutions<E> solutions) {
		this.iterator = solutions.getSolutions().iterator();
	}
	
	/**
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.iterator.hasNext();
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Solution<E> next() {
		return this.iterator.next();
	}

	/**
	 * This operation is not supported.
	 * 
	 * @throws UnsupportedOperationException If called.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("You must not remove solutions.");
	}

}
