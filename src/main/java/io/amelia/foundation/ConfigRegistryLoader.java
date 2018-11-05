/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import io.amelia.lang.ConfigException;

public interface ConfigRegistryLoader
{
	/**
	 * Seeks to load configuration from ideally the default storage.
	 *
	 * @param config The instance to load the config into.
	 *
	 * @throws ConfigException.Error Optional
	 */
	void loadConfig( ConfigData config ) throws ConfigException.Error;
}
