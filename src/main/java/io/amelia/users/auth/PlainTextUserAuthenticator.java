/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.auth;

import java.util.UUID;

import io.amelia.database.Database;
import io.amelia.database.DatabaseManager;
import io.amelia.database.elegant.ElegantQuerySelect;
import io.amelia.foundation.EntityPrincipal;
import io.amelia.foundation.Foundation;
import io.amelia.lang.DatabaseException;
import io.amelia.support.DateAndTime;
import io.amelia.support.Encrypt;
import io.amelia.support.Objs;
import io.amelia.users.DescriptiveReason;
import io.amelia.users.UserException;
import io.amelia.users.UserPermissible;
import io.amelia.users.Users;

/**
 * Used to authenticate an account using a Username and Password combination
 */
public final class PlainTextUserAuthenticator extends UserAuthenticator
{
	private final Database db = DatabaseManager.getDatabase();

	PlainTextUserAuthenticator()
	{
		super( "plaintext" );

		try
		{
			if ( !db.table( "accounts_plaintext" ).exists() )
				db.table( "accounts_plaintext" ).columnCreateVar( "acctId", 255 ).columnCreateVar( "password", 255 ).columnCreateInt( "expires", 12 );
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public UserCredentials authorize( EntityPrincipal entityPrincipal, UserPermissible permissible ) throws UserException.Error
	{
		/**
		 * Session logins can not be resumed using plain text. See {@link UserCredentials#saveCredentialsToSession}
		 */
		throw new UserException.Error( entityPrincipal, DescriptiveReason.FEATURE_NOT_IMPLEMENTED );
	}

	@Override
	public UserCredentials authorize( EntityPrincipal entityPrincipal, Object... credentials ) throws UserException.Error
	{
		if ( credentials.length < 1 || !( credentials[0] instanceof String ) )
			throw new UserException.Error( entityPrincipal, DescriptiveReason.INTERNAL_ERROR );

		String pass = ( String ) credentials[0];

		if ( entityPrincipal == null )
			throw new UserException.Error( Foundation.getNullEntity(), DescriptiveReason.INCORRECT_LOGIN );

		if ( Objs.isEmpty( pass ) )
			throw new UserException.Error( entityPrincipal, DescriptiveReason.EMPTY_CREDENTIALS );

		UUID uuid = entityPrincipal.uuid();
		String password = null;

		try
		{
			ElegantQuerySelect select = db.table( "accounts_plaintext" ).select().where( "uuid" ).matches( uuid.toString() ).limit( 1 ).executeWithException();

			if ( select == null || select.count() < 1 )
				throw new UserException.Error( entityPrincipal, DescriptiveReason.PASSWORD_UNSET );

			if ( select.getInt( "expires" ) > -1 && select.getInt( "expires" ) < DateAndTime.epoch() )
				throw new UserException.Error( entityPrincipal, DescriptiveReason.EXPIRED_LOGIN );

			password = select.getString( "password" );
		}
		catch ( UserException.Error e )
		{
			throw e;
		}
		catch ( DatabaseException e )
		{
			throw new UserException.Error( entityPrincipal, DescriptiveReason.INTERNAL_ERROR, e );
		}

		// TODO Encrypt all passwords
		if ( password.equals( pass ) || password.equals( Encrypt.md5Hex( pass ) ) || Encrypt.md5Hex( password ).equals( pass ) )
			return new PlainTextUserCredentials( entityPrincipal, DescriptiveReason.LOGIN_SUCCESS );
		else
			throw new UserException.Error( entityPrincipal, DescriptiveReason.INCORRECT_LOGIN );
	}

	/**
	 * Similar to {@link #setPassword(EntityPrincipal, String, int)} except password never expires
	 */
	public void setPassword( EntityPrincipal entityPrincipal, String password )
	{
		setPassword( entityPrincipal, password, -1 );
	}

	/**
	 * Sets the Account Password which is stored in a separate getTable for security
	 *
	 * @param entityPrincipal The Account to set password for
	 * @param password        The password to set
	 * @param expires         The password expiration. Use -1 for no expiration
	 *
	 * @return True if we successfully set the password
	 */
	public boolean setPassword( EntityPrincipal entityPrincipal, String password, int expires )
	{
		try
		{
			if ( db.table( "accounts_plaintext" ).insert().value( "uuid", entityPrincipal.uuid().toString() ).value( "password", password ).value( "expires", expires ).executeWithException().count() < 0 )
			{
				Users.L.severe( "We had an unknown issue inserting password for uuid '" + entityPrincipal.uuid() + "' into the database!" );
				return false;
			}

			// db.queryUpdate( "INSERT INTO `accounts_plaintext` (`acctId`,`password`,`expires`) VALUES ('" + acct.uuid() + "','" + password + "','" + expires + "');" );
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	class PlainTextUserCredentials extends UserCredentials
	{
		PlainTextUserCredentials( EntityPrincipal entity, DescriptiveReason descriptiveReason )
		{
			super( PlainTextUserAuthenticator.this, entity, descriptiveReason );
		}
	}
}
