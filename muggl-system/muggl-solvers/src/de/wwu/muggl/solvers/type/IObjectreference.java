package de.wwu.muggl.solvers.type;

import java.util.Map;

public interface IObjectreference {

	/**
	 *  Get the type of this object
	 * 
	 */
	public String getObjectType();
	
	/**
	 * Get the id of this object.
	 */
	public String getObjectId();
	
	/**
	 * Get the value map. 
	 * The key is the field name. 
	 * The value is the value of that field.
	 */
	public Map<String, Object> valueMap();
	
	public long getInstantiationNumber();
	
}
