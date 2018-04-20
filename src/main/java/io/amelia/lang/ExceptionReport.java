/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.Kernel;
import io.amelia.support.Objs;

/**
 * This class is used to analyze and report exceptions
 */
public final class ExceptionReport
{
	private static final Map<Class<? extends Throwable>, ExceptionCallback> registered = new ConcurrentHashMap<>();

	public static void handleSingleException( Throwable cause )
	{
		handleSingleException( cause, true );
	}

	/**
	 * Intended to be used for handling uncaught/unexpected exceptions from anywhere within the application.
	 * However, expected exceptions that don't implement their own exception handling can also use this method.
	 */
	public static void handleSingleException( Throwable cause, boolean crashOnSevere )
	{
		ExceptionReport report = new ExceptionReport();
		report.handleException( cause );
		report.printToLog( Kernel.L );
		ExceptionRegistrar exceptionRegistrar = Kernel.getExceptionRegistrar();
		if ( report.hasErrored() && exceptionRegistrar != null )
			exceptionRegistrar.fatalError( report, crashOnSevere );
	}

	/**
	 * Registers an expected exception to be thrown
	 *
	 * @param callback The Callback to call when such exception is thrown
	 * @param clzs     Classes to be registered
	 */
	@SafeVarargs
	public static void registerException( ExceptionCallback callback, Class<? extends Throwable>... clzs )
	{
		for ( Class<? extends Throwable> clz : clzs )
			registered.put( clz, callback );
	}

	protected final List<ExceptionContext> exceptionContexts = new ArrayList<>();
	private boolean hasErrored = false;

	public ExceptionReport addException( ExceptionContext exception )
	{
		if ( exception != null )
			exceptionContexts.add( exception );
		return this;
	}

	public ExceptionReport addException( ReportingLevel level, String msg, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof UncaughtException )
			{
				( ( UncaughtException ) throwable ).setReportingLevel( level );
				exceptionContexts.add( ( ExceptionContext ) throwable );
			}
			else
				exceptionContexts.add( new UncaughtException( level, msg, throwable ) );
		return this;
	}

	public ExceptionReport addException( ReportingLevel level, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof UncaughtException )
			{
				( ( UncaughtException ) throwable ).setReportingLevel( level );
				exceptionContexts.add( ( ExceptionContext ) throwable );
			}
			else
				exceptionContexts.add( new UncaughtException( level, throwable ) );
		return this;
	}

	public Stream<ExceptionContext> getExceptions( Predicate<ExceptionContext> exceptionPredicate )
	{
		return getExceptions().filter( exceptionPredicate );
	}

	public Stream<ExceptionContext> getExceptions()
	{
		return exceptionContexts.stream();
	}

	public Stream<ExceptionContext> getIgnorableExceptions()
	{
		return getExceptions().filter( e -> e.getReportingLevel().isIgnorable() );
	}

	public Stream<ExceptionContext> getSevereExceptions()
	{
		return getExceptions().filter( e -> !e.getReportingLevel().isIgnorable() );
	}

	public final void handleException( Throwable cause )
	{
		handleException( cause, null );
	}

	/**
	 * Processes and appends the throwable to the context provided.
	 *
	 * @param cause            The exception thrown
	 * @param exceptionContext The EvalContext associated with the eval request
	 */
	public final void handleException( @Nonnull Throwable cause, ExceptionContext exceptionContext )
	{
		/* Give an IException a chance to self-handle the exception report */
		if ( cause instanceof ExceptionContext )
		{
			// TODO Might not be desirable if a handle method was to return severe but did not provide any exception or debug information to the ExceptionReport. How can we force this behavior?
			ReportingLevel reportingLevel = ( ( ExceptionContext ) cause ).handle( this, exceptionContext );
			if ( reportingLevel != null )
			{
				hasErrored = !reportingLevel.isIgnorable();
				return;
			}
		}

		/* Parse each IException and set hasErrored if one or more IExceptions produced Non-Ignorable Exceptions */
		if ( cause instanceof MultipleException )
		{
			for ( ExceptionContext e : ( ( MultipleException ) cause ).getExceptions() )
				handleException( ( Throwable ) e, exceptionContext );
			return;
		}

		Map<Class<? extends Throwable>, ExceptionCallback> assignable = new HashMap<>();

		for ( Entry<Class<? extends Throwable>, ExceptionCallback> entry : registered.entrySet() )
			if ( cause.getClass().equals( entry.getKey() ) )
			{
				ReportingLevel e = entry.getValue().callback( cause, this, exceptionContext );
				if ( e != null )
				{
					hasErrored = !e.isIgnorable();
					return;
				}
			}
			else if ( entry.getKey().isAssignableFrom( cause.getClass() ) )
				assignable.put( entry.getKey(), entry.getValue() );

		if ( assignable.size() == 1 )
		{
			ReportingLevel e = assignable.values().toArray( new ExceptionCallback[0] )[0].callback( cause, this, exceptionContext );
			if ( e != null )
			{
				hasErrored = !e.isIgnorable();
				return;
			}
		}
		else if ( assignable.size() > 1 )
			for ( Entry<Class<? extends Throwable>, ExceptionCallback> entry : assignable.entrySet() )
			{
				for ( Class<?> iface : cause.getClass().getInterfaces() )
					if ( iface.equals( entry.getKey() ) )
					{
						ReportingLevel e = entry.getValue().callback( cause, this, exceptionContext );
						if ( e != null )
						{
							hasErrored = !e.isIgnorable();
							return;
						}
						break;
					}

				Class<?> superClass = cause.getClass();
				if ( superClass != null )
					do
					{
						if ( superClass.equals( entry.getKey() ) )
						{
							ReportingLevel e = entry.getValue().callback( cause, this, exceptionContext );
							if ( e != null )
							{
								hasErrored = !e.isIgnorable();
								return;
							}
							break;
						}
						superClass = cause.getClass();
					}
					while ( superClass != null );
			}

		/*
		 * Handle the remainder unhandled run of the mill exceptions
		 * NullPointerException, ArrayIndexOutOfBoundsException, IOException, StackOverflowError, ClassFormatError
		 */
		addException( ReportingLevel.E_UNHANDLED, cause );
	}

	public boolean hasErrored()
	{
		return hasErrored;
	}

	/**
	 * Checks if exception is present by class name
	 *
	 * @param clz The exception to check for
	 *
	 * @return Is it present
	 */
	public boolean hasException( @Nonnull Class<? extends Throwable> clz )
	{
		for ( ExceptionContext context : exceptionContexts )
		{
			Throwable throwable = context.getThrowable();

			if ( throwable.getCause() != null && clz.isAssignableFrom( context.getThrowable().getClass() ) )
				return true;

			if ( clz.isAssignableFrom( throwable.getClass() ) )
				return true;
		}

		return false;
	}

	public boolean hasExceptions()
	{
		return !exceptionContexts.isEmpty();
	}

	public boolean hasIgnorableExceptions()
	{
		return getIgnorableExceptions().count() > 0;
	}

	public boolean hasSevereExceptions()
	{
		return getSevereExceptions().count() > 0;
	}

	public void printIgnorableToLog( Kernel.Logger logger )
	{
		logger.warning( printSevereToString() );
	}

	public String printIgnorableToString()
	{
		// TODO Make this method better at what it does and make the output even prettier!
		StringBuilder builder = new StringBuilder();
		Supplier<Stream<ExceptionContext>> ignorableStream = this::getIgnorableExceptions;

		if ( ignorableStream.get().count() > 0 )
		{
			builder.append( "We Encountered " ).append( ignorableStream.get().count() ).append( " Ignorable Exception(s):" ).append( "\n" );
			ignorableStream.get().forEach( throwable -> builder.append( throwable.printStackTraceToString() ) );
		}

		return builder.toString();
	}

	public void printSevereToLog( Kernel.Logger logger )
	{
		logger.warning( printSevereToString() );
	}

	public String printSevereToString()
	{
		// TODO Make this method better at what it does and make the output even prettier!
		StringBuilder builder = new StringBuilder();
		Supplier<Stream<ExceptionContext>> severeStream = this::getSevereExceptions;

		if ( severeStream.get().count() > 0 )
		{
			builder.append( "We Encountered " ).append( severeStream.get().count() ).append( " Severe Exception(s):" ).append( "\n" );
			severeStream.get().forEach( throwable -> builder.append( throwable.printStackTraceToString() ) );
		}

		return builder.toString();
	}

	public void printToLog( Kernel.Logger logger )
	{
		String ignorable = printIgnorableToString();
		String severe = printSevereToString();

		if ( !Objs.isEmpty( ignorable ) )
			logger.warning( ignorable );
		if ( !Objs.isEmpty( severe ) )
			logger.severe( severe );
	}

	public String printToString()
	{
		return printIgnorableToString() + "\n" + printSevereToString();
	}

	/**
	 * Filters each severe exception through the supplied predicate.
	 * If there are no exceptions or the predicate never returned true, the exception supplied by exceptionSupplier is thrown.
	 * If exceptionSupplier is null, the method just returns.
	 *
	 * @param exceptionPredicate Predicate for testing each exception.
	 * @param exceptionSupplier  The Supplier to produce a new exception is not exception will be thrown.
	 *
	 * @throws MultipleException if multiple exceptions pass the predicate.
	 * @throws E                 otherwise.
	 */
	@SuppressWarnings( "unchecked" )
	public <E extends Exception> void throwExceptions( @Nonnull Predicate<ExceptionContext> exceptionPredicate, @Nullable Supplier<? extends Exception> exceptionSupplier ) throws E
	{
		List<ExceptionContext> exceptionContexts = getExceptions( exceptionPredicate ).collect( Collectors.toList() );

		if ( exceptionContexts.size() == 1 )
		{
			Throwable throwable = exceptionContexts.get( 0 ).getThrowable();
			if ( throwable instanceof Exception )
				throw ( E ) throwable;
			else
				throw new UncaughtException( throwable );
		}
		else if ( exceptionContexts.size() > 1 )
			throw ( E ) new MultipleException( exceptionContexts );
		else if ( exceptionSupplier != null )
			throw exceptionSupplier.get();
	}

	public <E extends Exception> void throwSevereExceptions() throws E
	{
		throwExceptions( ExceptionContext::notIgnorable, null );
	}

	/*
	 * private static final ThreadLocal<Yaml> YAML_INSTANCE = new ThreadLocal<Yaml>()
	 * {
	 *
	 * @Override
	 * protected Yaml initialValue()
	 * {
	 * DumperOptions opts = new DumperOptions();
	 * opts.setDefaultFlowStyle( DumperOptions.FlowStyle.FLOW );
	 * opts.setDefaultScalarStyle( DumperOptions.ScalarStyle.DOUBLE_QUOTED );
	 * opts.setPrettyFlow( true );
	 * opts.setWidth( Integer.MAX_VALUE ); // Don't wrap scalars -- json no like
	 * return new Yaml( opts );
	 * }
	 * };
	 */
	//private static final URL GIST_POST_URL;
	/*
	 * static
	 * {
	 * try
	 * {
	 * GIST_POST_URL = new URL( "https://api.github.com/gists" );
	 * }
	 * catch ( MalformedURLException e )
	 * {
	 * throw new ExceptionInInitializerError( e );
	 * }
	 * }
	 */
}
