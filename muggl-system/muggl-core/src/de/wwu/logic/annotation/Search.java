package de.wwu.logic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.wwu.logic.annotation.enums.SearchStrategy;

/**
 * Indicates that a local variable should be treated as a logic variable and not be instantiated
 * with concrete values.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-08-28
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Search {
	/**
	 * Determines the search strategy to be used.
	 */
	SearchStrategy strategy();

	/**
	 * If iterative deepening depth first search is used, the initial depth can be set.
	 */
	int deepeningStartingDepth() default 5;

	/**
	 * If iterative deepening depth first search is used, the deepness increment can be set.
	 */
	int deepeningIncrement() default 5;
}
