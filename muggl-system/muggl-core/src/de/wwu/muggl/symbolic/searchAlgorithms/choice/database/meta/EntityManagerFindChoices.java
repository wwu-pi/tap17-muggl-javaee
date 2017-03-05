package de.wwu.muggl.symbolic.searchAlgorithms.choice.database.meta;

public enum EntityManagerFindChoices {

	/**
	 * throw an IllegalArgumentException, because the given id reference is null
	 */
	ID_NULL_REFERENCE,

	/**
	 * entity with given id is not available in database
	 */
	ENTITY_NOT_EXISTENT,
	
	/**
	 * entity with given id is available in database
	 */
	ENTITY_EXISTS;
	
}
