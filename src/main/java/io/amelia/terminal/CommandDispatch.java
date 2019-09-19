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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import io.amelia.events.Events;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ExceptionReport;
import io.amelia.support.EnumColor;
import io.amelia.support.Exceptions;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.terminal.commands.BuiltinCommand;
import io.amelia.terminal.events.CommandIssuedEvent;

import com.google.common.collect.Maps;

import org.sqlite.date.ExceptionUtils;

/**
 * This is the Command Dispatch for executing a command from a console.
 */
public final class CommandDispatch
{
	public static final Kernel.Logger L = Kernel.getLogger( CommandDispatch.class );
	private static Map<Terminal, TerminalInterviewer> activeInterviewer = new ConcurrentHashMap<>();
	private static Map<Terminal, List<TerminalInterviewer>> interviewers = new ConcurrentHashMap<>();
	private static final Pattern PATTERN_ON_SPACE = Pattern.compile( " ", Pattern.LITERAL );
	private static List<CommandContext> pendingCommands = Collections.synchronizedList( new ArrayList<>() );
	private static List<Command> registeredCommands = Collections.synchronizedList( new ArrayList<>() );

	static
	{
		BuiltinCommand.registerBuiltinCommands();
		// CommandDispatch.registerCommand( new PermissionCommand() );
	}

	public static void addInterviewer( Terminal handler, TerminalInterviewer interviewer )
	{
		if ( interviewers.get( handler ) == null )
			interviewers.put( handler, new ArrayList<TerminalInterviewer>( Arrays.asList( interviewer ) ) );
		else
			interviewers.get( handler ).add( interviewer );
	}

	private static Command getCommand( String sentCommandLabel )
	{
		for ( Command command : registeredCommands )
			if ( command.getName().equals( sentCommandLabel.toLowerCase() ) || command.getAliases().contains( sentCommandLabel.toLowerCase() ) )
				return command;
		return null;
	}

	public static void handleCommands()
	{
		for ( Entry<Terminal, List<TerminalInterviewer>> entry : interviewers.entrySet() )
			if ( activeInterviewer.get( entry.getKey() ) == null )
				if ( entry.getValue().isEmpty() )
				{
					interviewers.remove( entry.getKey() );
					entry.getKey().resetPrompt();
				}
				else
				{
					TerminalInterviewer i = entry.getValue().remove( 0 );
					activeInterviewer.put( entry.getKey(), i );
					entry.getKey().setPrompt( i.getPrompt() );
				}

		while ( !pendingCommands.isEmpty() )
		{
			CommandContext command = pendingCommands.remove( 0 );

			try
			{
				TerminalInterviewer i = activeInterviewer.get( command.getTerminal() );
				Terminal terminal = command.getTerminal();

				if ( i != null )
				{
					if ( i.handleInput( command.getCommand() ) )
						activeInterviewer.remove( command.getTerminal() );
					else
						command.getTerminal().showPrompt();
				}
				else
				{
					CommandIssuedEvent event = new CommandIssuedEvent( command.getCommand(), terminal );

					Events.getInstance().callEvent( event );

					if ( event.isCancelled() )
					{
						terminal.sendMessage( EnumColor.RED + "Your entry was cancelled by the event system." );
						return;
					}

					String[] args = PATTERN_ON_SPACE.split( command.getCommand() );

					if ( args.length > 0 )
					{
						String sentCommandLabel = args[0].toLowerCase();
						Command target = getCommand( sentCommandLabel );

						if ( target != null )
							try
							{
								// if ( target.testPermission( terminal ) )
								// target.execute( command.getTerminal(), sentCommandLabel, Arrays.copyOfRange( args, 1, args.length ) );

								return;
							}
							catch ( CommandException ex )
							{
								throw ex;
							}
							catch ( Throwable ex )
							{
								command.getTerminal().sendMessage( EnumColor.RED + "Unhandled exception executing '" + command.getCommand() + "' in " + target + "\n" + Exceptions.getStackTrace( ex ) );

								throw new CommandException( "Unhandled exception executing '" + command.getCommand() + "' in " + target, ex );
							}
					}

					terminal.sendMessage( EnumColor.YELLOW + "Your entry was unrecognized, type \"help\" for help." );
				}
			}
			catch ( Exception ex )
			{
				L.warning( "Unexpected exception while parsing console command \"" + command.getCommand() + '"', ex );
			}
		}
	}

	public static void issueCommand( TerminalEntity handler, String command )
	{
		Objs.notNull( handler, "Handler cannot be null" );
		Objs.notNull( command, "CommandLine cannot be null" );

		L.info( "The remote connection '" + handler + "' issued the command '" + command + "'." );

		pendingCommands.add( new CommandContext( handler, command ) );
	}

	public static void registerCommand( Command command )
	{
		if ( getCommand( command.getName() ) == null )
			registeredCommands.add( command );
	}
}
