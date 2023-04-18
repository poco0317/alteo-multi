package com.etterna.multi.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface HelpMessage {
	
	/**
	 * Description of the command
	 */
	public String desc() default "";
	
	/**
	 * How to use the command
	 */
	public String usage() default "";

	/**
	 * Does this command require you to be a global moderator?
	 */
	public boolean requiresMod() default false;
	
	/**
	 * Does this command require you to be a room operator?
	 */
	public boolean requiresOper() default false;
	
	/**
	 * Does this command require you to be a room owner?
	 */
	public boolean requiresOwner() default false;
}
