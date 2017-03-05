package de.wwu.muggl.vm.support;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class simply extends java.util.ArrayList by a single method: addIfNotContained(E e).
 * It takes an object of Type E as its argument and will add it to the ArrayList if it is
 * not yet contained in it. This means that a code statement like
 * CheckingArrayList<Object> list = new CheckingArrayList<Object>();
 * list.addIfNotContained(a);
 * is equivalent to
 * CheckingArrayList<Object> list = new CheckingArrayList<Object>();
 * if (list.contains(a)) list.add(a);
 * However, using the method provided here should be a lot easier in most cases.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 *
 * @param <E> The Type to instantiate this CheckingArrayList with.
 */
public class CheckingArrayList<E> extends ArrayList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Inherited constructor.
	 * @param initialCapacity the initial capacity of the list
	 * see ArrayList#ArrayList(int)
	 */
	public CheckingArrayList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Inherited constructor.
	 * see ArrayList#ArrayList()
	 */
	public CheckingArrayList() {
		super();
	}

	/**
	 * Inherited constructor.
	 * @param c the collection whose elements are to be placed into this list
	 * @see ArrayList#ArrayList(Collection)
	 */
	public CheckingArrayList(Collection<? extends E> c) {
		super(c);
	}

	/**
	 * Add e if e is not yet contained in this ArrayList.
	 * @param e The element to be added if it not yet in this ArrayList.
	 */
	public void addIfNotContained(E e) {
		if (!contains(e)) add(e);
	}

}
