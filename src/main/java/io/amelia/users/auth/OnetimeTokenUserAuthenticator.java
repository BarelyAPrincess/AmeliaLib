/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.auth;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.amelia.database.Database;
import io.amelia.database.DatabaseManager;
import io.amelia.database.elegant.ElegantQuerySelect;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.EntityPrincipal;
import io.amelia.lang.DatabaseException;
import io.amelia.lang.ReportingLevel;
import io.amelia.looper.Delays;
import io.amelia.looper.LooperRouter;
import io.amelia.support.DateAndTime;
import io.amelia.support.Encrypt;
import io.amelia.support.EnumColor;
import io.amelia.users.DescriptiveReason;
import io.amelia.users.UserException;
import io.amelia.users.UserPermissible;
import io.amelia.users.Users;

/**
 * Used to authenticate an account using an Account Id and Token combination
 */
public class OnetimeTokenUserAuthenticator extends UserAuthenticator
{
	private final Database db = DatabaseManager.getDefault().getDatabase();

	OnetimeTokenUserAuthenticator()
	{
		super( "token" );

		/* try
		{
			if ( !db.table( "accounts_token" ).exists() )
				db.table( "accounts_token" ).columnCreateVar( "uuid", 255 ).columnCreateVar( "token", 255 ).columnCreateInt( "expires", 12 );
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
		} */

		LooperRouter.getMainLooper().postTaskRepeatingAt( () -> {
			try
			{
				int deleted = db.table( "accounts_token" ).delete().where( "expires" ).moreThan( 0 ).and().where( "expires" ).lessThan( DateAndTime.epoch() ).executeWithException().count();
				if ( deleted > 0 )
					Users.L.info( EnumColor.DARK_AQUA + "The cleanup task deleted " + deleted + " expired login token(s)." );
			}
			catch ( DatabaseException e )
			{
				e.printStackTrace();
			}
		}, 0L, Delays.MINUTE * ConfigRegistry.config.getValue( Users.ConfigKeys.SESSIONS_CLEANUP_INTERVAL ), true );

		// Foundation.getApplication().events().listen(  );
	}

	@Override
	public UserCredentials authorize( EntityPrincipal entity, UserPermissible perm ) throws UserException.Error
	{
		if ( entity == null )
			throw new UserException.Error( entity, DescriptiveReason.INCORRECT_LOGIN );

		String token = perm.getVariable( "token" );

		if ( token == null )
			throw new UserException.Error( entity, new DescriptiveReason( ReportingLevel.L_ERROR, "The account '" + entity.uuid().toString() + "' has no resumable login using the token method." ) );

		return authorize( entity, token );
	}

	@Override
	public UserCredentials authorize( EntityPrincipal entity, Object... credentials ) throws UserException.Error
	{
		if ( entity == null )
			throw new UserException.Error( entity, DescriptiveReason.INCORRECT_LOGIN );

		if ( credentials[0] instanceof UserPermissible )
			return authorize( entity, ( UserPermissible ) credentials[0] );

		if ( credentials.length == 0 || !( credentials[0] instanceof String ) )
			throw new UserException.Error( entity, DescriptiveReason.INTERNAL_ERROR );

		UUID uuid = entity.uuid();
		String token = ( String ) credentials[0];

		try
		{
			if ( token == null || token.isEmpty() )
				throw new UserException.Error( entity, DescriptiveReason.EMPTY_CREDENTIALS );

			ElegantQuerySelect select = db.table( "accounts_token" ).select().where( "uuid" ).matches( uuid ).and().where( "token" ).matches( token ).limit( 1 ).executeWithException();

			if ( select.count() == 0 )
				throw new UserException.Error( entity, DescriptiveReason.INCORRECT_LOGIN );
			// throw AccountResult.INCORRECT_LOGIN.setMessage( "The provided token did not match any saved tokens" + ( Versioning.isDevelopment() ? ", token: " + token : "." ) ).exception();

			if ( select.getInt( "expires" ) >= 0 && select.getInt( "expires" ) < DateAndTime.epoch() )
				throw new UserException.Error( entity, DescriptiveReason.EXPIRED_LOGIN );

			// deleteToken( uuid, token );
			expireToken( uuid.toString(), token );
			return new OnetimeTokenUserCredentials( entity, DescriptiveReason.LOGIN_SUCCESS, token );
		}
		catch ( DatabaseException e )
		{
			throw new UserException.Error( entity, DescriptiveReason.INTERNAL_ERROR, e );
		}
	}

	/**
	 * Deletes provided token from database
	 *
	 * @param uuid  The uuid associated with Token
	 * @param token The login token
	 */
	public boolean deleteToken( @Nonnull String uuid, @Nonnull String token )
	{
		try
		{
			return db.table( "accounts_token" ).delete().where( "uuid" ).matches( uuid ).and().where( "token" ).matches( token ).executeWithException().count() > 0;
		}
		catch ( DatabaseException e )
		{
			return false;
		}
	}

	/**
	 * Expires the provided token from database
	 *
	 * @param uuid  The uuid associated with Token
	 * @param token The login token
	 */
	private boolean expireToken( @Nonnull String uuid, @Nonnull String token )
	{
		try
		{
			return db.table( "accounts_token" ).update().value( "expires", 0 ).where( "uuid" ).matches( uuid ).and().where( "token" ).matches( token ).executeWithException().count() > 0;
		}
		catch ( DatabaseException e )
		{
			return false;
		}
	}

	/**
	 * Issues a new login token used for authenticating users later without storing the password.
	 *
	 * @param user The user to issue to the token to
	 *
	 * @return The issued token, be sure to save the token, authenticate with the token later. Token is valid for 7 days.
	 */
	public String issueToken( @Nonnull EntityPrincipal user )
	{
		String token = Encrypt.randomize( user.uuid().toString() ) + DateAndTime.epoch();
		try
		{
			// if ( db.queryUpdate( "INSERT INTO `accounts_token` (`uuid`,`token`,`expires`) VALUES (?,?,?);", acct.uuid(), token, ( Timings.epoch() + ( 60 * 60 * 24 * 7 ) ) ) < 1 )
			if ( db.table( "accouts_token" ).insert().value( "uuid", user.uuid() ).value( "token", token ).value( "expires", DateAndTime.epoch() + 60 * 60 * 24 * 7 ).executeWithException().count() < 0 )
			{
				Users.L.severe( "We had an unknown issue inserting token '" + token + "' into the database!" );
				return null;
			}
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
			return null;
		}
		return token;
	}

	class OnetimeTokenUserCredentials extends UserCredentials
	{
		private String token;

		OnetimeTokenUserCredentials( EntityPrincipal entity, DescriptiveReason descriptiveReason, String token )
		{
			super( OnetimeTokenUserAuthenticator.this, entity, descriptiveReason );
			this.token = token;
		}

		public String getToken()
		{
			return token;
		}
	}
}
