/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import io.amelia.lang.ApplicationException;

/**
 * INTERNAL USE ONLY
 * You should attempt to caught and mute this exception within your main() class.
 */
public class FoundationCrashException extends ApplicationException.Runtime
{
	private static final long serialVersionUID = -4937198089020390887L;

	FoundationCrashException()
	{
		super();
	}
}
