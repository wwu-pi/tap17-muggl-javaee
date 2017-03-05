package de.wwu.muggl.symbolic.structures;

/**
 * A Loop symbolizes a sequence of instructions that form a loop in the program flow.
 * Since loops are always characterized by conditional jumps in the backward direction,
 * the sequence of instructions start at to and runs to from inclusively.<br />
 *<br />
 * For each Loop there is a count. Each time the instruction at pc from is reached and
 * the backward jump is actually taken, the counter is increased by one. Otherwise, the
 * counter should be reseted. The method isCountGreaterEqual() can be used to check
 * whether a maximum has been reached already.<br />
 *<br />
 * Last modified: 2009-01-05
 *
 * @author Tim Majchrzak
 * @version 1.0.0
 */
public class Loop {
	private int from;
	private int to;
	private long count;

	/**
	 * Initialize the loop with the originating and the destination pc, setting the
	 * counter to zwero.
	 * @param from The destinating pc.
	 * @param to The originating pc.
	 */
	public Loop(int from, int to) {
		this.from = from;
		this.to = to;
		this.count = 0;
	}

	/**
	 * Getter for the originating pc.
	 * @return The originating pc.
	 */
	public int getFrom() {
		return this.from;
	}

	/**
	 * Getter for the destination pc.
	 * @return The destination pc.
	 */
	public int getTo() {
		return this.to;
	}

	/**
	 * Increase the counter by one.
	 */
	public void incCount() {
		this.count++;
	}

	/**
	 * Check whether the counter is greater or equal to the supplied maximum.
	 * @param maximum The maximum to check the counter against.
	 * @return true, if the counter is greater or equal, false otherwise.
	 */
	public boolean isCountGreaterEqual(int maximum) {
		if (this.count >= maximum) return true;
		return false;
	}

	/**
	 * Return a string representation of this loop.
	 * @return A string representation of this loop.
	 */
	@Override
	public String toString() {
		return String.valueOf(this.from) + " - " + String.valueOf(this.to) + ": " + String.valueOf(this.count) + " times";
	}

}
