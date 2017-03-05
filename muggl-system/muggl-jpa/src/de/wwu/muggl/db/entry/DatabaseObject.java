package de.wwu.muggl.db.entry;

import de.wwu.muggl.solvers.type.IObjectreference;

public interface DatabaseObject extends IObjectreference {
	
	/**
	 * Get the name of the class that has initialized this object.
	 */
	public String getInitializedClassName();
	
	/**
	 * Clone this object.
	 */
	public DatabaseObject getClone();

}
