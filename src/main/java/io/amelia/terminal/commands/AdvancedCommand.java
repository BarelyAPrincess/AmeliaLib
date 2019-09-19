/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.terminal.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.amelia.support.EnumColor;
import io.amelia.terminal.Command;
import io.amelia.terminal.CommandSender;
import io.amelia.terminal.commands.advanced.AutoCompleteChoicesException;
import io.amelia.terminal.commands.advanced.CommandBinding;
import io.amelia.terminal.commands.advanced.CommandHandler;
import io.amelia.terminal.commands.advanced.CommandListener;
import io.amelia.terminal.commands.advanced.CommandSyntax;

import com.google.common.base.Joiner;

public class AdvancedCommand extends Command
{
	protected Map<String, Map<CommandSyntax, CommandBinding>> listeners = new LinkedHashMap<>();

	public AdvancedCommand( String name )
	{
		super( name );
	}

	public AdvancedCommand( String name, String permission )
	{
		super( name, permission );
	}

	@Override
	public boolean execute( CommandSender handler, String command, String[] args )
	{
		try
		{
			if ( args.length > 0 )
			{
				Map<CommandSyntax, CommandBinding> callMap = listeners.get( command );

				if ( callMap == null )
					return false;

				CommandBinding selectedBinding = null;
				int argumentsLength = 0;
				String arguments = Joiner.on( " " ).join( args );

				for ( Entry<CommandSyntax, CommandBinding> entry : callMap.entrySet() )
				{
					CommandSyntax syntax = entry.getKey();
					if ( !syntax.isMatch( arguments ) )
						continue;
					if ( selectedBinding != null && syntax.getRegexp().length() < argumentsLength )
						continue;

					CommandBinding binding = entry.getValue();
					binding.setParams( syntax.getMatchedArguments( arguments ) );
					selectedBinding = binding;
				}

				if ( selectedBinding == null )
				{
					handler.sendMessage( EnumColor.RED + "Error in command syntax. Check command help." );
					return true;
				}
				
				/*if ( !selectedBinding.checkPermissions( handler.getPermissibleEntity() ) )
				{
					PermissionDispatcher.getLogger().warning( "Entity " + handler.getId() + " tried to access command \"" + command + " " + arguments + "\", but doesn't have permission to do this." );
					handler.sendMessage( EnumColor.RED + "Sorry, you don't have the required permissions." );
					return true;
				}*/

				try
				{
					selectedBinding.call( handler, selectedBinding.getParams() );
				}
				catch ( InvocationTargetException e )
				{
					if ( e.getTargetException() instanceof AutoCompleteChoicesException )
					{
						AutoCompleteChoicesException autocomplete = ( AutoCompleteChoicesException ) e.getTargetException();
						handler.sendMessage( "Autocomplete for <" + autocomplete.getArgName() + ">:" );
						handler.sendMessage( "    " + Joiner.on( "   " ).join( autocomplete.getChoices() ) );
					}
					else
						throw new RuntimeException( e.getTargetException() );
				}
				catch ( Exception e )
				{
					// PermissionDispatcher.getLogger().severe( "There is bogus command handler for " + command + " command. (Is appropriate plugin is update?)" );
					if ( e.getCause() != null )
						e.getCause().printStackTrace();
					else
						e.printStackTrace();
				}

				return true;
			}
			else
				return false; // TODO Print which subcommands are available
		}
		catch ( Throwable t )
		{
			// TODO Make it so log messages can be sent to InteractiveConsoles, like a getLogger() for consoles
			handler.sendMessage( "Sorry, there was a severe exception encountered while executing this command. :(" );
			// PermissionDispatcher.getLogger().severe( String.format( "Exception encountered while %s was executing %s %s", handler.name(), command, Joiner.on( " " ).join( args ) ), t );
			return true;
		}
	}

	public List<CommandBinding> getCommands()
	{
		List<CommandBinding> commands = new LinkedList<>();

		for ( Map<CommandSyntax, CommandBinding> map : listeners.values() )
			commands.addAll( map.values() );

		return commands;
	}

	public AdvancedCommand register( CommandListener listener )
	{
		for ( Method method : listener.getClass().getMethods() )
		{
			if ( !method.isAnnotationPresent( CommandHandler.class ) )
				continue;

			CommandHandler cmdAnnotation = method.getAnnotation( CommandHandler.class );

			if ( !getName().equals( cmdAnnotation.name() ) || !getAliases().contains( cmdAnnotation.name() ) )
				addAliases( cmdAnnotation.name() );

			Map<CommandSyntax, CommandBinding> commandListeners = listeners.get( cmdAnnotation.name().toLowerCase() );
			if ( commandListeners == null )
			{
				commandListeners = new LinkedHashMap<CommandSyntax, CommandBinding>();
				listeners.put( cmdAnnotation.name().toLowerCase(), commandListeners );
			}

			commandListeners.put( new CommandSyntax( cmdAnnotation.syntax() ), new CommandBinding( listener, method ) );
		}

		listener.onRegistered( this );

		return this;
	}
}
