package de.wwu.muggl.symbolic.searchAlgorithms.support;

/**
 * A FifoListEntry is internally used by a FifoList. It hence has package visibility only.
 * Upon initialization it takes a element tha is to be added to the list. It then offers
 * the possibilitie to get this element, aswell as setting and getting a successor.
 *
 * @param <T> The type stored in this FifoListEntry.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2007-12-02
 */
class FifoListEntry<T> {
	// Fields for the encapsulated element and the successing FifoListEntry.
	private T element;
	private FifoListEntry<T> successor;

	/**
	 * Initialize with an element.
	 * @param element The element to encapsulate.
	 */
	FifoListEntry(T element) {
		this.element = element;
	}

	/**
	 * Getter for the element.
	 * @return The encapsulated element.
	 */
	T getElement() {
		return this.element;
	}

	/**
	 * Getter for the successor.
	 * @return The successing FifoListEntry.
	 */
	FifoListEntry<T> getSuccessor() {
		return this.successor;
	}

	/**
	 * Setter for the successor.
	 * @param successor The successing FifoListEntry.
	 */
	void setSuccessor(FifoListEntry<T> successor) {
		this.successor = successor;
	}

}
