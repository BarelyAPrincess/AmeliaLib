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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.EntityPrincipal;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Hook;
import io.amelia.foundation.ProvidesClass;
import io.amelia.lang.ApplicationException;
import io.amelia.support.Objs;
import io.amelia.support.Streams;

public class HoneyUsers extends Users
{
	private static HoneyUsers instance;

	@ProvidesClass( EntityPrincipal.class )
	public static UserResult getEntityPrincipal( UUID uuid )
	{
		return getInstance().getUser( uuid );
	}

	@ProvidesClass( Users.class )
	public static HoneyUsers getInstance()
	{
		if ( instance == null )
			instance = new HoneyUsers();
		return instance;
	}

	@Hook( hookClass = Foundation.class, hookAction = Foundation.HOOK_ACTION_INIT )
	public static void hookBindingsInit() throws ApplicationException.Error
	{
		Foundation.initProviders( HoneyUsers.class );
	}

	protected boolean isDebugEnabled = ConfigRegistry.config.getValue( ConfigKeys.DEBUG_ENABLED );

	public HoneyUsers()
	{
		super();
	}

	@Override
	public void addUserCreator( UserCreator userCreator )
	{
		userCreators.add( userCreator );
		userCreator.load();
	}

	@Override
	public UserContext createUser( @Nonnull UUID uuid ) throws UserException.Error
	{
		return createUser( uuid, getDefaultUserCreator() );
	}

	@Override
	public UserContext createUser( @Nonnull UUID uuid, @Nonnull UserCreator userCreator ) throws UserException.Error
	{
		if ( !userCreator.isEnabled() )
			throw new UserException.Error( null, DescriptiveReason.FEATURE_DISABLED.getReportingLevel(), DescriptiveReason.FEATURE_DISABLED.getReasonMessage() );
		UserContext userContext = userCreator.create( uuid );
		users.add( userContext );
		return userContext;
	}

	@Override
	public String getDisplayNameFormat()
	{
		return ConfigRegistry.config.getValue( ConfigKeys.DISPLAY_NAME_FORMAT );
	}

	@Override
	public String getSingleSignonMessage()
	{
		return ConfigRegistry.config.getValue( ConfigKeys.SINGLE_SIGNON_MESSAGE );
	}

	@Override
	public UserResult getUser( @Nonnull UUID uuid )
	{
		UserResult userResult = new UserResult( uuid );
		getUser( userResult );
		return userResult;
	}

	/**
	 * Resolves a user using the registered user creators.
	 * Never authenticates.
	 */
	public void getUser( @Nonnull UserResult userResult )
	{
		UUID uuid = userResult.uuid();

		if ( Objs.anyMatch( uuid, Foundation.getNullEntity().uuid(), Foundation.getRootEntity().uuid() ) )
		{
			userResult.setUser( ( UserContext ) ( uuid == Foundation.getNullEntity().uuid() ? Foundation.getNullEntity() : Foundation.getRootEntity() ) );
			userResult.setDescriptiveReason( DescriptiveReason.LOGIN_SUCCESS );
			return;
		}

		Optional<UserContext> foundResult = getUsers().filter( user -> uuid.equals( user.uuid() ) ).findAny();
		if ( foundResult.isPresent() )
		{
			userResult.setUser( foundResult.get() );
			userResult.setDescriptiveReason( DescriptiveReason.LOGIN_SUCCESS );
			return;
		}

		List<UserResult> pendingUserResults = new ArrayList<>();

		for ( UserCreator creator : userCreators )
		{
			userResult = creator.resolve( uuid );

			if ( userResult == null )
				continue;
			if ( userResult.getReportingLevel().isSuccess() )
				return;

			if ( isDebugEnabled )
				L.info( "Failure in creator " + creator.getClass().getSimpleName() + ". {descriptionMessage=" + userResult.getDescriptiveReason().getReasonMessage() + "}" );
			if ( isDebugEnabled && userResult.hasCause() )
				userResult.getCause().printStackTrace();

			pendingUserResults.add( userResult );
		}

		// Sort ReportingLevels based on their position in the reportingLevelSeverityArray.
		pendingUserResults.sort( ( left, right ) -> {
			int leftSeverity = Arrays.binarySearch( reportingLevelSeverityArray, left.getReportingLevel() );
			int rightSeverity = Arrays.binarySearch( reportingLevelSeverityArray, right.getReportingLevel() );
			return Integer.compare( leftSeverity >= 0 ? leftSeverity : Integer.MAX_VALUE, rightSeverity >= 0 ? rightSeverity : Integer.MAX_VALUE );
		} );

		userResult = pendingUserResults.stream().findFirst().orElse( null );

		if ( userResult == null )
		{
			userResult = new UserResult( uuid );
			userResult.setDescriptiveReason( DescriptiveReason.INCORRECT_LOGIN );
			return;
		}

		return;
	}

	@Override
	public Optional<UserCreator> getUserCreator( String name )
	{
		return getUserCreators().filter( userCreator -> name.equalsIgnoreCase( userCreator.name() ) ).findAny();
	}

	@Override
	public Stream<UserCreator> getUserCreators()
	{
		return userCreators.stream();
	}

	@Override
	public Stream<UserContext> getUsers()
	{
		return users.stream();
	}

	@Override
	protected boolean hasUserCreator( String userCreatorName )
	{
		return userCreators.stream().anyMatch( userCreator -> userCreatorName.equals( userCreator.name() ) );
	}

	@Override
	public boolean isSingleSignonEnabled()
	{
		return ConfigRegistry.config.getValue( ConfigKeys.SINGLE_SIGNON );
	}

	@Override
	void put( UserContext userContext ) throws UserException.Error
	{
		if ( Objs.anyMatch( userContext.uuid(), Foundation.getNullEntity().uuid(), Foundation.getRootEntity().uuid() ) )
			throw new UserException.Error( userContext, DescriptiveReason.INTERNAL_ERROR );
		if ( users.stream().anyMatch( user -> user.compareTo( userContext ) == 0 ) )
			return;
		userContext.validate();
		users.add( userContext );
	}

	@Override
	public void removeUserContext( UserContext userContext )
	{
		users.remove( userContext );
	}

	@Override
	void unload( @Nonnull UUID uuid ) throws UserException.Error
	{
		Streams.forEachWithException( users.stream().filter( user -> uuid.equals( user.uuid() ) ), UserContext::unload );
	}

	@Override
	void unload() throws UserException.Error
	{
		// TODO
		Streams.forEachWithException( users.stream(), UserContext::unload );
	}

	@Override
	public boolean userExists( @Nonnull String uuid )
	{
		return getUsers().anyMatch( user -> uuid.equals( user.uuid() ) );
	}
}
