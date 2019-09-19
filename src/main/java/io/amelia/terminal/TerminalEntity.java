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

import io.amelia.foundation.Kernel;
import io.amelia.support.EnumColor;
import io.amelia.support.Objs;
import io.amelia.users.UserAttachment;
import io.amelia.users.UserResult;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * Used to interact with commands and logs
 */
public abstract class TerminalEntity implements Terminal, CommandSender
{
	protected TerminalHandler handler;
	private String prompt = "";

	public TerminalEntity( TerminalHandler handler )
	{
		this.handler = handler;

		/*try
		{
			loginWithException( AccountAuthenticator.NULL, AccountType.ACCOUNT_NONE.getLocId(), AccountType.ACCOUNT_NONE.getId() );
		}
		catch ( AccountException e )
		{
			if ( e.getResult().hasCause() )
				e.getResult().getCause().printStackTrace();
			AccountManager.getLogger().severe( e.getMessage() );

			handler.kick( "Internal Server Error: " + e.getMessage() );
		}*/
	}

	public void displayWelcomeMessage()
	{
		handler.println( String.format( "%s%s%s", EnumColor.NEGATIVE, EnumColor.GOLD, Kernel.getDevMeta().getProductDescribed() ) );
		handler.println( String.format( "%s%s%s", EnumColor.NEGATIVE, EnumColor.GOLD, Kernel.getDevMeta().getProductCopyright() ) );
	}

	public abstract void finish();

	public TerminalHandler getHandler()
	{
		return handler;
	}

	/*@Override
	public String getIpAddress()
	{
		return handler.getIpAddress();
	}

	@Override
	public List<String> getIpAddresses()
	{
		return Arrays.asList( getIpAddress() );
	}*/

	/*public String getLocId()
	{
		LocationService locationService = ModuleDispatcher.getService( LocationService.class );
		return locationService == null ? null : locationService.getDefaultLocation().getId();
	}

	@Override
	public AccountLocation getLocation()
	{
		LocationService locationService = ModuleDispatcher.getService( LocationService.class );
		return locationService == null ? null : locationService.getDefaultLocation();
	}*/

	/*@Override
	public final AccountPermissible getPermissible()
	{
		return this;
	}*/

	@Override
	public String getVariable( String key )
	{
		return getVariable( key, null );
	}

	@Override
	public UserResult kick( String reason )
	{
		return handler.kick( EnumColor.AQUA + "You are being kicked for reason: " + EnumColor.RESET + reason );
	}

	@Override
	public void getPrompt()
	{
		handler.print( "\r" + prompt );
	}

	@Override
	public void resetPrompt()
	{
		try
		{
			prompt = EnumColor.GREEN + this.uuid().toString() + "@" + InetAddress.getLocalHost().getHostName() + EnumColor.RESET + ":" + EnumColor.BLUE + "~" + EnumColor.RESET + "$ ";
		}
		catch ( UnknownHostException e )
		{
			prompt = EnumColor.GREEN + this.uuid().toString() + "@localhost ~$ ";
		}

		getPrompt();
	}

	/*@Override
	public void sendMessage( UserAttachment sender, Object... objs )
	{
		for ( Object obj : objs )
			try
			{
				handler.println( sender.name() + ": " + Objs.castToStringWithException( obj ) );
			}
			catch ( ClassCastException e )
			{
				handler.println( sender.name() + " sent object " + obj.getClass().getName() + " but we had no idea how to properly output it to your terminal." );
			}
	}

	@Override
	public void sendMessage( Object... objs )
	{
		for ( Object obj : objs )
			try
			{
				handler.println( Objs.castToStringWithException( obj ) );
			}
			catch ( ClassCastException e )
			{
				handler.println( "Received object " + obj.getClass().getName() + " but we had no idea how to properly output it to your terminal." );
			}
	}*/

	@Override
	public void setPrompt( String prompt )
	{
		if ( prompt != null )
			this.prompt = prompt;

		getPrompt();
	}

	/*@Override
	protected void successfulLogin()
	{
		// TODO Unregister from old login, i.e., NONE Account
		// Temp Fix until a better way is found, i.e., logout!
		AccountType.ACCOUNT_NONE.i().unregisterAttachment( this );
		registerAttachment( this );
	}*/

	@Override
	public String toString()
	{
		return String.format( "TerminalEntity{EntityId=%s}", uuid().toString() );
	}
}
