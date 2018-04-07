package io.amelia.storage;

import io.amelia.foundation.ConfigMap;
import io.amelia.foundation.ConfigRegistryLoader;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ConfigException;
import io.amelia.lang.StorageException;
import io.amelia.storage.file.FileStorageDriver;
import io.amelia.storage.methods.ContainerToStackerMethod;

public class ConfigStorageLoader implements ConfigRegistryLoader
{
	@Override
	public void loadConfig( ConfigMap config ) throws ConfigException.Error
	{
		try
		{
			new ContainerToStackerMethod().toStacker( new FileStorageDriver( Kernel.getPath( Kernel.PATH_CONFIG, true ) ), config, ConfigMap::new );
		}
		catch ( StorageException.Error e )
		{
			throw ConfigException.error( config, e );
		}
	}
}
