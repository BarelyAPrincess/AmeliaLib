/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logging;

import com.google.common.collect.Lists;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

import io.amelia.foundation.Kernel;
import io.amelia.lang.ExceptionContext;
import io.amelia.logcompat.LogBuilder;
import io.amelia.support.EnumColor;
import io.amelia.support.Exceptions;

class LogRecord implements ILogEvent
{
	final List<LogElement> elements = Lists.newLinkedList();
	String header = null;

	LogRecord()
	{

	}

	@Override
	public void exception( Throwable throwable )
	{
		if ( throwable instanceof ExceptionContext )
		{
			if ( ( ( ExceptionContext ) throwable ).getReportingLevel().isEnabled() )
				log( Level.SEVERE, EnumColor.NEGATIVE + "" + EnumColor.RED + throwable.getClass().getSimpleName() + ": " + throwable.getMessage() );
		}
		else
			log( Level.SEVERE, EnumColor.NEGATIVE + "" + EnumColor.RED + throwable.getClass().getSimpleName() + ": " + throwable.getMessage() );

		if ( Kernel.isDevelopment() )
			log( Level.SEVERE, EnumColor.NEGATIVE + "" + EnumColor.RED + Exceptions.getStackTrace( throwable ) );
	}

	@Override
	public void exceptions( List<Throwable> throwables )
	{
		for ( Throwable throwable : throwables )
			exception( throwable );
	}

	@Override
	public void flush()
	{
		StringBuilder sb = new StringBuilder();

		if ( header != null )
			sb.append( EnumColor.RESET + header );

		for ( LogElement e : elements )
			sb.append( EnumColor.RESET + "" + EnumColor.GRAY + "\n  |-> " + new SimpleDateFormat( "ss.SSS" ).format( e.time ) + " " + e.color + e.msg );

		LogBuilder.get().log( Level.INFO, "\r" + sb.toString() );

		elements.clear();
	}

	@Override
	public void header( String msg, Object... objs )
	{
		header = String.format( msg, objs );
	}

	@Override
	public void log( Level level, String msg, Object... objs )
	{
		if ( objs.length < 1 )
			elements.add( new LogElement( level, msg, EnumColor.fromLevel( level ) ) );
		else
			elements.add( new LogElement( level, String.format( msg, objs ), EnumColor.fromLevel( level ) ) );
	}

	static class LogElement
	{
		EnumColor color;
		Level level;
		String msg;
		long time = System.currentTimeMillis();

		LogElement( Level level, String msg, EnumColor color )
		{
			this.level = level;
			this.msg = msg;
			this.color = color;
		}
	}
}
