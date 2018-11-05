package io.amelia.storage;

import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.ConfigRegistryLoader;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ConfigException;
import io.amelia.lang.StorageException;
import io.amelia.storage.backend.FileBackend;
import io.amelia.storage.backend.StorageBackend;
import io.amelia.support.StorageConversions;
import io.amelia.support.Streams;

public class StorageRegistry
{
	public static final StorageProvider PROVIDER = ( StorageProvider ) FileSystemProvider.installedProviders().stream().filter( provider -> StorageProvider.SCHEME.equalsIgnoreCase( provider.getScheme() ) ).findAny().orElseGet( StorageProvider::new );

	private static final List<StorageBackend> backends = new CopyOnWriteArrayList<>();

	public static void addBackend( @Nonnull StorageBackend backend ) throws StorageException.Error
	{
		if ( backends.stream().anyMatch( backend::equals ) )
			throw new StorageException.Error( "A backend with the same prefix was previously added to the StorageRegistry." );

		backends.add( backend );

		if ( backend.getType() == BackendType.CONFIG )
			loadConfig( backend );
	}

	static void loadConfig( StorageBackend backend ) throws StorageException.Error
	{
		StorageConversions.loadToStacker( Kernel.getPath( Kernel.PATH_CONFIG, true ), ConfigRegistry.config );
	}

	public static Optional<MountPoint> getMountPoint( Path origPath )
	{
		return getBackends().filter( backend -> origPath.startsWith( backend.getMountPoint() ) ).findAny();



	}

	public static Stream<StorageBackend> getBackends()
	{
		return backends.stream();
	}

	public static void initDefault() throws StorageException.Error
	{
		if ( backends.size() > 0 )
			throw new StorageException.Error( "Storage registry is already initialized." );

		backends.add( new FileBackend( "app", Kernel.getPath( Kernel.PATH_APP ) ) );
		backends.add( new FileBackend( "config", Kernel.getPath( Kernel.PATH_CONFIG ), BackendType.CONFIG ) );
		backends.add( new FileBackend( "cache", Kernel.getPath( Kernel.PATH_CACHE ) ) );
		backends.add( new FileBackend( "libs", Kernel.getPath( Kernel.PATH_LIBS ) ) );
		backends.add( new FileBackend( "logs", Kernel.getPath( Kernel.PATH_LOGS ), BackendType.LOGS ) );
		backends.add( new FileBackend( "plugins", Kernel.getPath( Kernel.PATH_PLUGINS ) ) );
		backends.add( new FileBackend( "storage", Kernel.getPath( Kernel.PATH_STORAGE ) ) );
		backends.add( new FileBackend( "updates", Kernel.getPath( Kernel.PATH_UPDATES ) ) );
	}

	public static void loadConfig() throws ConfigException.Error
	{
		ConfigRegistry.init( new Loader() );
	}

	// TODO Might need to be converted to classes so implementer can specify type specific configuration
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
		LOGS,
	}

	private static class Loader implements ConfigRegistryLoader
	{
		@Override
		public void loadConfig( ConfigData config ) throws ConfigException.Error
		{
			try
			{
				Streams.forEachWithException( getBackends().filter( backend -> backend.getType() == BackendType.CONFIG ), StorageRegistry::loadConfig );
			}
			catch ( StorageException.Error e )
			{
				throw new ConfigException.Error( config, e );
			}
		}
	}
}
