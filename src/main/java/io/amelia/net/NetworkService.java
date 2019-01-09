package io.amelia.net;

import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.ApplicationException;

public interface NetworkService
{
	default ConfigData getConfig()
	{
		return ConfigRegistry.getChildOrCreate( "config.network." + getId() );
	}

	default ConfigData getConfig( String key )
	{
		return getConfig().getChild( key );
	}

	String getId();

	void shutdown();

	void start() throws ApplicationException.Error;
}
