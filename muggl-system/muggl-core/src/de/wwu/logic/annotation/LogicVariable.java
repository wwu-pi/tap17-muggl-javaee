package de.wwu.logic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a local variable should be treated as a logic variable and not be instantiated
 * with concrete values.
 * 
 * @author Tim Majchrzak
 * @version 1.0.0, 2010-07-15
 */
@Documented
@Target({ElementType.LOCAL_VARIABLE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicVariable {}
