/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import javax.annotation.Nonnull;

import io.amelia.lang.ConfigException;

public interface ConfigLoader
{
	void commitConfig( @Nonnull CommitType type ) throws ConfigException.Error;

	ConfigData beginConfig() throws ConfigException.Error;

	ConfigData config() throws ConfigException.Error;

	void destroy();

	boolean hasBeganConfig();

	enum CommitType
	{
		AMENDED,
		INITIAL,
	}
}
