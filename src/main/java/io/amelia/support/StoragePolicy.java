package io.amelia.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;

public class StoragePolicy
{
	private final boolean caseSensitive;
	private Function<Path, Strategy> handlerFunction = null;
	private volatile List<LayoutEntry> layouts = new CopyOnWriteArrayList<>();

	public StoragePolicy()
	{
		this( true );
	}

	public StoragePolicy( boolean caseSensitive )
	{
		this.caseSensitive = caseSensitive;
	}

	public void enforcePolicy( @Nonnull Path path ) throws StorageException.Error
	{
		if ( !Files.isDirectory( path ) )
			throw new StorageException.Error( "Path must be a directory: " + IO.relPath( path ) );

		List<LayoutEntry> layouts = new CopyOnWriteArrayList<>( this.layouts );

		try
		{
			IO.listRecursive( path ).forEach( entry -> {
				try
				{
					for ( LayoutEntry layoutEntry : layouts )
						if ( Files.isDirectory( entry ) == layoutEntry.directoryLayout && Strs.equals( entry.toRealPath( LinkOption.NOFOLLOW_LINKS ).toString(), layoutEntry.name, caseSensitive ) )
						{
							// Do nothing for all other strategies.
							if ( layoutEntry.strategy == Strategy.DELETE )
								IO.deleteIfExists( entry );

							layouts.remove( layoutEntry );
						}

					if ( handlerFunction != null )
						if ( handlerFunction.apply( entry ) == Strategy.DELETE )
							IO.deleteIfExists( entry );
				}
				catch ( IOException e )
				{
					e.printStackTrace();
					// Ignore
				}
			} );

			for ( LayoutEntry layoutEntry : layouts )
				if ( layoutEntry.strategy == Strategy.CREATE )
					if ( layoutEntry.directoryLayout )
						Files.createDirectory( path.resolve( layoutEntry.name ) );
					else
						Files.createFile( path.resolve( layoutEntry.name ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			// Ignore
		}
	}

	/**
	 * Sets the function that will process objects and containers that weren't handled by the designated layout.
	 *
	 * @param handlerFunction The handler function.
	 */
	public void setHandler( Function<Path, Strategy> handlerFunction )
	{
		this.handlerFunction = handlerFunction;
	}

	public void setLayoutDirectory( String name, Strategy strategy )
	{
		LayoutEntry entry = new LayoutEntry();

		entry.name = name;
		entry.directoryLayout = true;
		entry.strategy = strategy;

		layouts.add( entry );
	}

	public void setLayoutFile( String name, Strategy strategy )
	{
		LayoutEntry entry = new LayoutEntry();

		entry.name = name;
		entry.directoryLayout = false;
		entry.strategy = strategy;

		layouts.add( entry );
	}

	public enum Strategy
	{
		DELETE,
		OPTIONAL,
		CREATE
	}

	private class LayoutEntry
	{
		boolean directoryLayout;
		String name;
		Strategy strategy;
	}
}
