package de.wwu.muggl.symbolic.searchAlgorithms.support;

/**
 * A FifoList offers a List that takes elements of type T and implements the first-in-first-out
 * principle. It has two operations:
 * add: A new element is added as the newest element of the list. It will be the latest element
 * to be returned before the list becomes empty.
 * get: The oldest element will be returned and the list will then either become empty, or the
 * element added after the oldest element will become the oldest one.
 *
 * The FifoList uses entries of type FifoListEntry internally. This is transparent for the usage.
 *
 * A FifoList should be parameterized so it will only process the desired type.
 *
 * @param <T> The type stored in this FifoList.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-02-10
 */
public class FifoList<T> {
	// Fields for the oldest and the newest entry.
	private FifoListEntry<T> oldestEntry;
	private FifoListEntry<T> newestEntry;

	/**
	 * Empty constructor.
	 */
	public FifoList() { }

	/**
	 * Add an element of type T as the newest element to the list. If the list is empty,
	 * it will be the oldest element then either.
	 * @param element The element to be added.
	 */
	public void add(T element) {
		// Generate the new entry.
		FifoListEntry<T> entry = new FifoListEntry<T>(element);
		// Check if the list is empty.
		if (this.oldestEntry == null) {
			this.oldestEntry = entry;
			this.newestEntry = entry;
		} else {
			// Set the successor for the element that will now be the second newest.
			this.newestEntry.setSuccessor(entry);
			// Set the new element as the newest.
			this.newestEntry = entry;
		}
	}

	/**
	 * Get the oldest element from the list. It will be of type T. The list will then either
	 * become empty, or its successor will become the oldest element.
	 * @return The oldest element currently in the list.
	 * @throws EmptyFifoListException If the list is empty.
	 */
	public T get() throws EmptyFifoListException {
		// Is the list empty?
		if (this.oldestEntry == null) throw new EmptyFifoListException("This FifoList is empty.");
		// Fetch the oldestElement;
		FifoListEntry<T> oldestEntry = this.oldestEntry;
		// Get its successor and set it as the oldestEntry.
		this.oldestEntry = oldestEntry.getSuccessor();
		// Return the entries' element.
		return oldestEntry.getElement();
	}

}
