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

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.amelia.permissions.PermissionDefault;
import io.amelia.support.EnumColor;
import io.amelia.terminal.CommandDispatch;
import io.amelia.terminal.CommandSender;
import io.amelia.terminal.Terminal;
import io.amelia.terminal.TerminalInterviewer;
import io.amelia.users.UserException;
import io.amelia.users.UserResult;
import io.amelia.users.auth.UserAuthenticator;

/**
 * Used to login an account to the console
 */
class LoginCommand extends BuiltinCommand
{
	class LoginInterviewerPass implements TerminalInterviewer
	{
		private CommandSender sender;
		
		public LoginInterviewerPass( CommandSender sender )
		{
			this.sender = sender;
		}
		
		@Override
		public String getPrompt()
		{
			return "Password for " + sender.getVariable( "user" ) + ": ";
		}
		
		@Override
		public boolean handleInput( String input )
		{
			String user = sender.getVariable( "user" );
			String pass = input;
			
			/*try
			{
				if ( user != null && pass != null )
				{
					// TODO Unregister Terminal from NONE account, for that matter unregister before overriding any account
					
					UserResult result = sender.getTerminalEntity().loginWithException( UserAuthenticator.PASSWORD, "%", user, pass );
					
					if ( !sender.getTerminalEntity().checkPermission( PermissionDefault.QUERY.getNode() ).isTrue() )
						throw new UserException( AccountDescriptiveReason.UNAUTHORIZED, sender.meta() );
					
					AccountManager.getLogger().info( EnumColor.GREEN + "Successful Console Login [username='" + user + "',password='" + pass + "',userId='" + result.getAccount().getId() + "',displayName='" + result.getAccount().getDisplayName() + "']" );
					
					sender.sendMessage( EnumColor.GREEN + "Welcome " + user + ", you have been successfully logged in." );
				}
			}
			catch ( UserException.Error l )
			{
				if ( l.getAccount() != null )
					AccountManager.getLogger().warning( EnumColor.GREEN + "Failed Console Login [username='" + user + "',password='" + pass + "',userId='" + l.getAccount().getId() + "',displayName='" + l.getAccount().getDisplayName() + "',reason='" + l.getMessage() + "']" );
				
				sender.sendMessage( EnumColor.YELLOW + l.getMessage() );
				
				if ( !AccountType.isNoneAccount( sender ) )
					sender.getPermissible().login( AccountAuthenticator.NULL, AccountType.ACCOUNT_NONE.getLocId(), AccountType.ACCOUNT_NONE.getId() );
				
				return true;
			}*/
			
			sender.setVariable( "user", null );
			return true;
		}
	}
	
	class LoginInterviewerUser implements TerminalInterviewer
	{
		private CommandSender sender;
		
		public LoginInterviewerUser( CommandSender sender )
		{
			this.sender = sender;
		}
		
		@Override
		public String getPrompt()
		{
			try
			{
				return InetAddress.getLocalHost().getHostName() + " login: ";
			}
			catch ( UnknownHostException e )
			{
				return "login: ";
			}
		}
		
		@Override
		public boolean handleInput( String input )
		{
			if ( input == null || input.isEmpty() )
			{
				sender.sendMessage( "Username can't be empty!" );
				return true;
			}
			
			sender.setVariable( "user", input );
			return true;
		}
	}
	
	public LoginCommand()
	{
		super( "login" );
	}
	
	@Override
	public boolean execute( CommandSender sender, String command, String[] args )
	{
		if ( sender instanceof Terminal )
		{
			CommandDispatch.addInterviewer( ( Terminal ) sender, new LoginInterviewerUser( sender ) );
			CommandDispatch.addInterviewer( ( Terminal ) sender, new LoginInterviewerPass( sender ) );
		}
		
		return true;
	}
}
