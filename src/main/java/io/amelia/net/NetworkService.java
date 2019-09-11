/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net;

import io.amelia.data.TypeBase;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.ApplicationException;

public interface NetworkService
{
	default ConfigData getConfig()
	{
		return ConfigRegistry.config.getChildOrCreate( getConfigBase() );
	}

	default TypeBase getConfigBase()
	{
		return new TypeBase( Networking.ConfigKeys.NET_BASE, getId() );
	}

	default ConfigData getConfig( String key )
	{
		return getConfig().getChild( key );
	}

	String getId();

	void shutdown();

	void start() throws ApplicationException.Error;
}
