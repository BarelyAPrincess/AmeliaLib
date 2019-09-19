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

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.amelia.support.Namespace;
import io.amelia.support.Strs;

/**
 * The base class for Console Commands
 */
public abstract class Command
{
	protected final Set<String> aliases = Sets.newHashSet();
	protected String description = "";
	protected final String name;
	private String permission = null;
	protected String permissionMessage = null;
	protected String usageMessage = null;

	public Command( String name )
	{
		this.name = name.toLowerCase();
	}

	public Command( String name, String permission )
	{
		this( name );

		if ( permission != null )
		{
			Namespace ns = Namespace.of( permission );

			if ( !ns.containsOnlyValidChars() )
				throw new RuntimeException( "We detected that the required permission '" + ns.getString() + "' for command '" + name + "' contains invalid characters, this is most likely a programmers bug." );

			this.permission = ns.getString();
		}
	}

	public void addAliases( String... alias )
	{
		aliases.addAll( Arrays.asList( alias ) );
	}

	/**
	 * Executes the command, returning its success
	 *
	 * @param sender  Source object which is executing this command
	 * @param command The alias of the command used
	 * @param args    All arguments passed to the command, split via ' '
	 *
	 * @return true if the command was successful, otherwise false
	 */
	public abstract boolean execute( CommandSender sender, String command, String[] args );

	public Collection<String> getAliases()
	{
		return aliases;
	}

	/**
	 * Gets a brief description of this command
	 *
	 * @return Description of this command
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Returns the name of this command
	 *
	 * @return Name of this command
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the permission required by users to be able to perform this command
	 *
	 * @return Permission name, or null if none
	 */
	public String getPermission()
	{
		return permission;
	}

	/**
	 * Returns a message to be displayed on a failed permission check for this command
	 *
	 * @return Permission check failed message
	 */
	public String getPermissionMessage()
	{
		return permissionMessage;
	}

	/**
	 * Gets the permission required by users to be able to perform this command
	 *
	 * @return Permission name, or null if none
	 */
	/*public Permission getPermissionNode()
	{
		return PermissionDispatcher.i().getNode( permission );
	}*/

	/**
	 * Gets an example usage of this command
	 *
	 * @return One or more example usages
	 */
	public String getUsage()
	{
		return usageMessage;
	}

	/**
	 * Sets the list of aliases to request on registration for this command.
	 *
	 * @param aliases aliases to register
	 *
	 * @return this command object, for chaining
	 */
	public Command setAliases( List<String> aliases )
	{
		this.aliases.clear();
		this.aliases.addAll( Strs.toLowerCase( aliases ) );
		return this;
	}

	public Command setAliases( String... aliases )
	{
		return setAliases( Arrays.asList( aliases ) );
	}

	/**
	 * Sets a brief description of this command.
	 *
	 * @param description new command description
	 *
	 * @return this command object, for chaining
	 */
	public Command setDescription( String description )
	{
		this.description = description;
		return this;
	}

	/**
	 * Sets the message sent when a permission check fails
	 *
	 * @param permissionMessage new permission message, null to indicate default message, or an empty string to indicate no message
	 *
	 * @return this command object, for chaining
	 */
	public Command setPermissionMessage( String permissionMessage )
	{
		this.permissionMessage = permissionMessage;
		return this;
	}

	/**
	 * Sets the example usage of this command
	 *
	 * @param usage new example usage
	 *
	 * @return this command object, for chaining
	 */
	public Command setUsage( String usage )
	{
		usageMessage = usage;
		return this;
	}

	/*
	 * Tests the given {@link TerminalHandler} to see if they can perform this command.
	 * <p>
	 * If they do not have permission, they will be informed that they cannot do this.
	 *
	 * @param source InteractiveConsoleHandler to test
	 *
	 * @return true if they can use it, otherwise false
	 */
	/*public boolean testPermission( CommandSender source )
	{
		if ( source == null )
			return false;

		if ( testPermissionSilent( source ) )
			return true;

		if ( source instanceof MessageReceiver )
			if ( permissionMessage == null )
				source.sendMessage( EnumColor.RED + "I'm sorry, but you do not have permission to perform the command '" + name + "'." );
			else if ( permissionMessage.length() != 0 )
				for ( String line : permissionMessage.replace( "<permission>", permission ).split( "\n" ) )
					source.sendMessage( line );

		return false;
	}*/

	/*
	 * Tests the given {@link TerminalHandler} to see if they can perform this command.
	 * <p>
	 * No error is sent to the sender.
	 *
	 * @param source User to test
	 *
	 * @return true if they can use it, otherwise false
	 */
	/*public boolean testPermissionSilent( CommandSender source )
	{
		if ( permission == null || permission.length() == 0 )
			return true;

		// TODO split permissions
		for ( String p : permission.split( ";" ) )
			if ( source.getPermissibleEntity().checkPermission( p ).isTrue() )
				return true;

		return false;
	}*/

	@Override
	public String toString()
	{
		return getClass().getName() + '(' + name + ')';
	}
}
