/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.terminal;

/**
 * Thrown when an unhandled exception occurs during the execution of a Command
 */
@SuppressWarnings( "serial" )
public class CommandException extends RuntimeException
{
	/**
	 * Creates a new instance of <code>CommandException</code> without detail message.
	 */
	public CommandException()
	{
	}
	
	/**
	 * Constructs an instance of <code>CommandException</code> with the specified detail message.
	 * 
	 * @param msg
	 *            the detail message.
	 */
	public CommandException( String msg )
	{
		super( msg );
	}
	
	public CommandException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}
