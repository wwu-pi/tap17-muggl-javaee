package de.wwu.muggl.symbolic.searchAlgorithms.depthFirst;

import java.util.Stack;

import de.wwu.muggl.symbolic.searchAlgorithms.SearchAlgorithm;
import de.wwu.muggl.symbolic.searchAlgorithms.choice.ChoicePoint;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.Pop;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.Push;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.VmPop;
import de.wwu.muggl.symbolic.searchAlgorithms.depthFirst.trailelements.VmPush;

/**
 * The StackToTrail extends the java.util.Stack. Is overrides the functionality for push and pop. In
 * general, it will invoke the super method whenever the methods push() and pop() are invoked. If it
 * is not set to restoring mode, it will also add information to the trail of the current
 * ChoicePoint.<br />
 * <br />
 * The trail can be used to track back to a former state of the execution. Hence, when pushing an
 * item the command to pop will be added to the trail, and when popping an item the command to push
 * it will be added to the trail.
 *
 * @param <E> Used to make the StackToTrail generic.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-10
 */
public class StackToTrail<E> extends Stack<E> {
	// Fields.
	private boolean isVmStack;
	private SearchAlgorithm searchAlgorithm;
	private boolean restoringMode;

	/**
	 * Initialize a new StackToTrail.
	 * @param isVmStack If set to true, this StackToTrail should be used as a virtual machine stack. It should be used as a operand stack otherwise.
	 * @param searchAlgorithm The currently used search algorithm.
	 */
	public StackToTrail(boolean isVmStack, SearchAlgorithm searchAlgorithm) {
		super();
		this.isVmStack = isVmStack;
		this.searchAlgorithm = searchAlgorithm;
		this.restoringMode = false;
	}

	/**
	 * Push an item onto the stack. If there is a ChoicePoint set and this StackToTrail
	 * is not in restoring mode, add the command to pop to the trail.
	 * @param item The item to push onto the stack.
	 * @return The supplied item.
	 */
	@Override
	public E push(E item) {
		if (!this.restoringMode) {
			ChoicePoint choicePoint = this.searchAlgorithm.getCurrentChoicePoint();
			if (choicePoint != null && choicePoint.hasTrail()) {
				if (this.isVmStack) {
					choicePoint.addToTrail(new VmPop());
				} else {
					choicePoint.addToTrail(new Pop());
				}
			}
		}

		return super.push(item);
	}

	/**
	 * Pop an item from the stack. If there is a ChoicePoint set and this StackToTrail
	 * is not in restoring mode, add the command to push this item to the trail.
	 * @return The popped item.
	 */
	@Override
	public synchronized E pop() {
		E item = super.pop();
		if (!this.restoringMode) {
			ChoicePoint choicePoint = this.searchAlgorithm.getCurrentChoicePoint();
			if (choicePoint != null && choicePoint.hasTrail()) {
				if (this.isVmStack) {
					choicePoint.addToTrail(new VmPush(item));
				} else {
					choicePoint.addToTrail(new Push(item));
				}
			}
		}

		return item;
	}

	/**
	 * Setter for the restoring mode.
	 * @param restoringMode true enables restoring mode, false disables it.
	 */
	public void setRestoringMode(boolean restoringMode) {
		this.restoringMode = restoringMode;
	}

	/**
	 * Indicates whether some other object is equal to this one.
	 *
	 * @param obj The object to check equality with.
	 * @return true, if the three fields for the StackToTrail are equal and if the inherited stack
	 *         is equal; false otherwise.
	 * @see java.util.Vector#equals(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public synchronized boolean equals(Object obj) {
		if (obj instanceof StackToTrail) {
			StackToTrail<E> stack = (StackToTrail<E>) obj;
			if (stack.isVmStack == this.isVmStack && stack.searchAlgorithm == this.searchAlgorithm && stack.restoringMode == this.restoringMode) {
				return super.equals(obj);
			}
		}
		return false;
	}
	
	/**
	 * Returns the hash code value for this stack.
	 * 
	 * @return The hash code value for this stack.
	 * @see java.util.Vector#hashCode()
	 */
	@Override
	public synchronized int hashCode() {
		return super.hashCode();
	}

}
