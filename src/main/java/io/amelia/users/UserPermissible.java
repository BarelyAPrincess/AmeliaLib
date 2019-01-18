/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

import com.google.common.collect.Sets;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.amelia.events.Events;
import io.amelia.foundation.EntitySubject;
import io.amelia.foundation.Foundation;
import io.amelia.lang.ReportingLevel;
import io.amelia.permissions.Permissible;
import io.amelia.permissions.PermissibleState;
import io.amelia.support.DateAndTime;
import io.amelia.support.EnumColor;
import io.amelia.support.Strs;
import io.amelia.users.auth.UserAuthenticator;
import io.amelia.users.auth.UserCredentials;
import io.amelia.users.events.UserLoginBeginEvent;
import io.amelia.users.events.UserLoginFailedEvent;
import io.amelia.users.events.UserLoginSuccessEvent;

public abstract class UserPermissible extends Permissible implements EntitySubject
{
	/**
	 * The logged in account associated with this session
	 */
	protected UserEntity entity = null;

	private boolean checkUser()
	{
		if ( entity == null )
			entity = Foundation.getNullEntity().getEntity();

		return !Foundation.isNullEntity( entity );
	}

	protected abstract void failedLogin( UserResult result );

	@Override
	public UserContext getContext()
	{
		return getEntity().getContext();
	}

	public final UserEntity getEntity()
	{
		checkUser();
		return entity;
	}

	public abstract List<String> getIpAddresses();

	/**
	 * Used by {@link #login()} and {@link #login(UserAuthenticator, UUID, Object...)} to maintain persistent login information
	 *
	 * @param key The key to get
	 *
	 * @return The String result
	 */
	public abstract String getVariable( String key );

	/**
	 * See {@link #getVariable(String)}
	 *
	 * @param def Specifies a default value to return if the requested key is null
	 */
	public abstract String getVariable( String key, String def );

	public boolean hasLogin()
	{
		return checkUser();
	}

	/**
	 * Called from subclass once subclass has finished loading
	 */
	protected void initialized()
	{
		login();
	}

	public boolean isInitialized()
	{
		return entity != null;
	}

	/**
	 * Attempts to authenticate using saved Account Credentials
	 */
	public void login()
	{
		String authName = getVariable( "auth" );
		UUID uuid = UUID.fromString( getVariable( "uuid" ) );

		if ( authName != null && !authName.isEmpty() )
		{
			UserAuthenticator auth = UserAuthenticator.byName( authName );
			login( auth, uuid, this );
		}
	}

	/**
	 * Attempts to authenticate the Account Id using the specified {@link UserAuthenticator} and Credentials
	 *
	 * @param auth     The {@link UserAuthenticator}
	 * @param uuid     The Account Id
	 * @param credObjs The Account Credentials. Exact credentials depend on what UserAuthenticator was provided.
	 *
	 * @return The {@link UserResult}
	 */
	public UserResult login( UserAuthenticator auth, UUID uuid, Object... credObjs )
	{
		UserResult result = new UserResult( uuid );
		UserContext user = null;

		try
		{
			if ( auth != null )
			{
				Foundation.getUsers().getUser( result );
				user = result.getContext();

				if ( user == null )
					return result;

				user.getCreator().loginBegin( user, this, uuid, credObjs );

				PermissibleState reason = Foundation.getPermissions().checkPermissibleState( uuid );

				if ( reason != PermissibleState.PERMITTED )
				{
					if ( reason == PermissibleState.NOT_WHITELISTED )
						result.setDescriptiveReason( DescriptiveReason.USER_NOT_WHITELISTED );
					else if ( reason == PermissibleState.BANNED )
						result.setDescriptiveReason( DescriptiveReason.USER_BANNED );
					else
						result.setDescriptiveReason( DescriptiveReason.FEATURE_NOT_IMPLEMENTED );

					return result;
				}

				UserLoginBeginEvent event = new UserLoginBeginEvent( user, this, credObjs );
				Events.getInstance().callEvent( event );

				if ( !event.getDescriptiveReason().getReportingLevel().isIgnorable() )
				{
					result.setDescriptiveReason( event.getDescriptiveReason() );
					return result;
				}

				UserCredentials credentials = auth.authorize( user, credObjs );

				if ( credentials.getDescriptiveReason().getReportingLevel().isSuccess() )
				{
					result.setDescriptiveReason( DescriptiveReason.LOGIN_SUCCESS );

					UserEntity userEntity = user.getEntity();
					userEntity.setLastUsedCredentials( credentials );

					if ( userEntity.countAttachments() > 1 && Foundation.getUsers().isSingleSignonEnabled() )
						for ( UserAttachment userAttachment : userEntity.getAttachments() )
							if ( userAttachment instanceof Kickable )
								( ( Kickable ) userAttachment ).kick( Foundation.getUsers().getSingleSignonMessage() );

					user.setValue( "lastLogin", DateAndTime.epoch() );

					// XXX Should we track all past IPs or only the current ones and what about local logins?
					Set<String> ips = Sets.newLinkedHashSet();
					if ( user.getString( "lastLoginIp" ) != null )
						Strs.split( user.getString( "lastLoginIp" ).orElse( "" ), "|" ).forEach( ips::add );
					ips.addAll( getIpAddresses() );

					if ( ips.size() > 5 )
						user.setValue( "lastLoginIp", Strs.join( new LinkedList<>( ips ).subList( ips.size() - 5, ips.size() ), "|" ) );
					else if ( ips.size() > 0 )
						user.setValue( "lastLoginIp", Strs.join( ips, "|" ) );
					else
						user.setValue( "lastLoginIp", "" );
					setVariable( "uuid", user.uuid().toString() );

					user.save();

					entity = userEntity;

					successfulLogin();
					user.getContext().getCreator().loginSuccess( result );
					Events.getInstance().callEvent( new UserLoginSuccessEvent( this, result ) );
				}
				else
					result.setDescriptiveReason( credentials.getDescriptiveReason() );
			}
			else
				result.setDescriptiveReason( new DescriptiveReason( ReportingLevel.L_ERROR, "The Authenticator was null!" ) );
		}
		catch ( UserException.Error e )
		{
			if ( e.getDescriptiveReason() == null )
				result.setDescriptiveReason( new DescriptiveReason( e.getReportingLevel(), e.getMessage() ) );
			else
				result.setDescriptiveReason( e.getDescriptiveReason() );

			if ( e.hasCause() )
				result.setCause( e.getCause() );
		}
		catch ( Throwable t )
		{
			result.setCause( t );
			result.setDescriptiveReason( DescriptiveReason.INTERNAL_ERROR );
		}

		if ( !result.getReportingLevel().isSuccess() )
		{
			failedLogin( result );
			if ( user != null )
				user.getContext().getCreator().loginFailed( result );
			Events.getInstance().callEvent( new UserLoginFailedEvent( result ) );
		}

		if ( Foundation.getUsers().isDebugEnabled() )
		{
			if ( !result.getReportingLevel().isIgnorable() && result.hasCause() )
				result.getCause().printStackTrace();

			Users.L.info( ( result.getReportingLevel().isSuccess() ? EnumColor.GREEN : EnumColor.YELLOW ) + "Session Login: [id='" + uuid + "',reason='" + result.getDescriptiveReason().getReasonMessage() + "']" );
		}

		return result;
	}

	public UserResult loginWithException( UserAuthenticator auth, UUID uuid, Object... credObjs ) throws UserException.Error
	{
		UserResult result = login( auth, uuid, credObjs );
		if ( !result.getReportingLevel().isSuccess() && result.hasCause() )
			throw result.getCause();
		return result;
	}

	public UserResult logout()
	{
		if ( !Foundation.isNullEntity( entity ) )
		{
			Users.L.info( EnumColor.GREEN + "Successful Logout: [id='" + entity.uuid() + "',name='" + entity.name() + "',ipAddresses='" + entity.getIpAddresses() + "']" );

			entity = null;
			checkUser();
			destroyEntity();

			setVariable( "auth", null );
			setVariable( "locId", null );
			setVariable( "acctId", null );
			setVariable( "token", null );

			return new UserResult( entity.getContext() ).setDescriptiveReason( DescriptiveReason.LOGOUT_SUCCESS );
		}

		return new UserResult( entity.getContext() ).setDescriptiveReason( DescriptiveReason.INTERNAL_ERROR );
	}

	@Override
	public String name()
	{
		return entity.name();
	}

	protected void registerAttachment( UserAttachment attachment )
	{
		checkUser();
		entity.registerAttachment( attachment );
	}

	public abstract UserRoot getWebroot();

	public abstract void setVariable( String key, String value );

	protected abstract void successfulLogin() throws UserException.Error;

	protected void unregisterAttachment( UserAttachment attachment )
	{
		checkUser();
		entity.unregisterAttachment( attachment );
	}

	@Override
	public UUID uuid()
	{
		return entity == null ? null : entity.uuid();
	}
}
