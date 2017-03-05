package de.wwu.muggl.instructions.interfaces.data;

/**
 * The implementing instruction is known to use a variable in the sense of data-flow tracking.Please
 * note that the wide instruction will not be marked by this interface, even though it (among
 * others) provides widened access to store.
 *
 * @author Tim Majchrzak
 * @version 1.0.0, 2009-03-20
 */
public interface VariableUsing {

}
