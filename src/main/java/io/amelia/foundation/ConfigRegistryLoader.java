package io.amelia.foundation;

import io.amelia.lang.ConfigException;

public interface ConfigRegistryLoader
{
	/**
	 * Seeks to load configuration from ideally the default filesystem.
	 *
	 * @param config The instance to load the config into.
	 *
	 * @throws ConfigException.Error Optional
	 */
	void loadConfig( ConfigMap config ) throws ConfigException.Error;

	/**
	 * Allows for additional configuration to be loaded from alternative filesystems, such as SQL or Network, which can only be loaded up by the first round of config.
	 *
	 * @param config The instance to load the config into.
	 *
	 * @throws ConfigException.Error Optional
	 */
	void loadConfigAdditional( ConfigMap config ) throws ConfigException.Error;
}
