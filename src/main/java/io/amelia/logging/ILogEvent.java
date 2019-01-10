/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logging;

import java.util.List;
import java.util.logging.Level;

/**
 * Interface for {@link LogEvent} and {@link LogRecord}
 */
public interface ILogEvent
{
	void exception( Throwable throwable );

	void exceptions( List<Throwable> throwables );

	void flush();

	void header( String msg, Object... objs );

	void log( Level level, String msg, Object... objs );
}
