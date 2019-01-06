/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage;

import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.parcel.ParcelChecker;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigLoader;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.hooks.Hook;
import io.amelia.lang.ConfigException;
import io.amelia.lang.ParcelException;
import io.amelia.lang.StorageException;
import io.amelia.storage.backend.FileStorageBackend;
import io.amelia.storage.backend.StorageBackend;
import io.amelia.support.NodePath;
import io.amelia.support.Objs;
import io.amelia.support.StorageConversions;
import io.amelia.support.Streams;

public class HoneyStorage
{
	public static final HoneyStorageProvider PROVIDER = ( HoneyStorageProvider ) FileSystemProvider.installedProviders().stream().filter( provider -> HoneyStorageProvider.SCHEME.equalsIgnoreCase( provider.getScheme() ) ).findAny().orElseGet( HoneyStorageProvider::new );
	private static final List<StorageBackend> backends = new CopyOnWriteArrayList<>();
	private static final List<ConfigLoader> configLoaders = new ArrayList<>();
	private static boolean initialized = false;
	private static FileStorageBackend rootBackend;

	public static void addBackend( @Nonnull StorageBackend storageBackend ) throws StorageException.Error
	{
		if ( !initialized )
			throw new StorageException.Error( "The Storage Registry has not been initialized." );
		for ( StorageBackend backend : backends )
		{
			if ( backend.getMountPath().equals( storageBackend.getMountPath() ) )
				throw new StorageException.Error( "A backend with the exact same mount path was previously added to the HoneyStorage." );
			if ( backend.getMountPath().startsWith( storageBackend.getMountPath() ) || storageBackend.getMountPath().startsWith( backend.getMountPath() ) )
				throw new StorageException.Error( "A backend with a similar mount path was previously added to the HoneyStorage." );
		}

		backends.add( storageBackend );

		if ( storageBackend.getType() == BackendType.CONFIG )
			synchronized ( configLoaders )
			{
				ConfigData config;
				for ( ConfigLoader configLoader : configLoaders )
					try
					{
						loadConfig( storageBackend, configLoader, ConfigLoader.CommitType.AMENDED );
					}
					catch ( ConfigException.Error e )
					{
						Kernel.L.warning( "A ConfigLoader threw an unexpected exception.", e );
					}
			}
	}

	@Hook( ns = "io.amelia.app.parse" )
	public static void hookFoundationParse() throws StorageException.Error
	{
		addConfigLoader( ConfigRegistry.LOADER );
		init();
	}

	public static void addConfigLoader( @Nonnull ConfigLoader configLoader ) throws StorageException.Error
	{
		if ( configLoaders.stream().anyMatch( configLoader::equals ) )
			throw new StorageException.Error( "ConfigLoader already exists." );

		synchronized ( configLoaders )
		{
			configLoaders.add( configLoader );

			if ( initialized )
				Streams.forEachWithException( getBackends().filter( backend -> backend.getType() == BackendType.CONFIG ), backend -> {
					try
					{
						loadConfig( backend, configLoader, ConfigLoader.CommitType.AMENDED );
					}
					catch ( ConfigException.Error e )
					{
						Kernel.L.warning( "A ConfigLoader threw an unexpected exception. {configLoader=" + configLoader + ",storageBackend" + backend + "}", e );
					}
				} );
		}
	}

	public static Optional<StorageBackend> getBackend( NodePath mountPath )
	{
		return getBackends().filter( backend -> backend.getMountPath().equals( mountPath ) ).findAny();
	}

	public static Stream<StorageBackend> getBackends()
	{
		return backends.stream();
	}

	public static Optional<MountPoint> getMountPoint( NodePath origFullPath )
	{
		return Optional.ofNullable( getBackends().map( backend -> backend.matches( origFullPath ) ).filter( Objs::isNotNull ).findAny().orElseGet( () -> rootBackend.getMountPoint( origFullPath ) ) );
	}

	public static FileStorageBackend getRootBackend()
	{
		return rootBackend;
	}

	public static void init() throws StorageException.Error
	{
		try
		{
			if ( initialized )
				throw new StorageException.Error( "Storage registry has already been initialized." );

			rootBackend = new FileStorageBackend( Kernel.getPath( Kernel.PATH_APP ), NodePath.of( "foundation" ) );
			StorageBackend configBackend = new FileStorageBackend( Kernel.getPath( Kernel.PATH_CONFIG ), NodePath.of( "config" ), BackendType.CONFIG );
			backends.add( configBackend );
			backends.add( new FileStorageBackend( Kernel.getPath( Kernel.PATH_CACHE ), NodePath.of( "cache" ) ) );
			backends.add( new FileStorageBackend( Kernel.getPath( Kernel.PATH_LIBS ), NodePath.of( "libs" ) ) );
			backends.add( new FileStorageBackend( Kernel.getPath( Kernel.PATH_LOGS ), NodePath.of( "logs" ), BackendType.LOGS ) );
			backends.add( new FileStorageBackend( Kernel.getPath( Kernel.PATH_PLUGINS ), NodePath.of( "plugins" ) ) );
			backends.add( new FileStorageBackend( Kernel.getPath( Kernel.PATH_STORAGE ), NodePath.of( "storage" ) ) );
			backends.add( new FileStorageBackend( Kernel.getPath( Kernel.PATH_UPDATES ), NodePath.of( "updates" ) ) );

			synchronized ( configLoaders )
			{
				ConfigData config;
				for ( ConfigLoader configLoader : configLoaders )
					try
					{
						loadConfig( configBackend, configLoader, ConfigLoader.CommitType.INITIAL );
					}
					catch ( ConfigException.Error e )
					{
						Kernel.L.warning( "A ConfigLoader threw an unexpected exception.", e );
					}
			}
		}
		catch ( StorageException.Error e )
		{
			initialized = false;
			backends.clear();
			throw e;
		}

		initialized = true;
	}

	public static boolean isInitialized()
	{
		return initialized;
	}

	private static void loadConfig( StorageBackend storageBackend, ConfigLoader configLoader, ConfigLoader.CommitType commitType ) throws ConfigException.Error, StorageException.Error
	{
		ConfigData config;
		if ( ( config = configLoader.beginConfig() ) != null )
		{
			StorageConversions.loadToStacker( storageBackend.getRootPath(), config );
			configLoader.commitConfig( commitType );
		}
	}

	public enum BackendType
	{
		/**
		 * Normal file structure with no specific arrangement of files
		 */
		DEFAULT,
		/**
		 * Config file structure with paths representing the node and the values contained within the files.
		 */
		CONFIG,
		/**
		 * Similar to a unix home directory structure, multiple directories housing users.
		 */
		HOME,
		/**
		 * Extension of HOME with the addition of a config file contained in each user directory.
		 */
		HOME_CONFIG,
		/**
		 * Creates a UNIX like log directory with rotation.
		 */
		LOGS;

		public ParcelChecker getParcelChecker() throws ParcelException.Error
		{
			ParcelChecker parcelChecker = new ParcelChecker();

			if ( this == HOME_CONFIG )
			{
				parcelChecker.setValueType( "config.fileName", ParcelChecker.ValueType.STRING, "config" );
			}

			return parcelChecker;
		}
	}
}
