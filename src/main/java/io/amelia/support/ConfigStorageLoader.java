/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import io.amelia.foundation.ConfigMap;
import io.amelia.foundation.ConfigRegistryLoader;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ConfigException;
import io.amelia.lang.StorageException;
import io.amelia.support.StorageConversions;

public class ConfigStorageLoader implements ConfigRegistryLoader
{
	@Override
	public void loadConfig( ConfigMap config ) throws ConfigException.Error
	{
		try
		{
			StorageConversions.loadToStacker( Kernel.getPath( Kernel.PATH_CONFIG, true ), config, ConfigMap::new );
		}
		catch ( StorageException.Error e )
		{
			throw ConfigException.error( config, e );
		}
	}

	@Override
	public void loadConfigAdditional( ConfigMap config ) throws ConfigException.Error
	{
		// Currently unused. :(
	}
}
