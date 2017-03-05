package de.wwu.muggl.instructions.interfaces.data;

/**
 * The implementing instruction is known to load a variable in the sense of data-flow tracking.
 * Please note that the wide instruction will not be marked by this interface, even though it
 * (among others) provides widened access to load.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-03-11
 */
public interface VariableLoading {

	/**
	 * Get the index into the local variables.
	 *
	 * @return The local variable index.
	 */
	int getLocalVariableIndex();

}
