package io.amelia.storage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import io.amelia.lang.StorageException;
import io.amelia.support.Strs;

public class StorageContainerPolicy
{
	private final boolean caseSensitive;
	private Function<StorageEntry, Strategy> handlerFunction = null;
	private volatile List<LayoutEntry> layouts = new CopyOnWriteArrayList<>();

	public StorageContainerPolicy()
	{
		this( true );
	}

	public StorageContainerPolicy( boolean caseSensitive )
	{
		this.caseSensitive = caseSensitive;
	}

	public void enforcePolicy( StorageContainerEntry entryContainer ) throws StorageException.Error
	{
		List<LayoutEntry> layouts = new CopyOnWriteArrayList<>( this.layouts );

		entryContainer.streamEntriesRecursive().forEach( entry -> {
			for ( LayoutEntry layoutEntry : layouts )
				if ( entry.isContainer() == layoutEntry.containerLayout && Strs.equals( entry.getFullPath(), layoutEntry.name, caseSensitive ) )
				{
					// Do nothing for all other strategies.
					if ( layoutEntry.strategy == Strategy.DELETE )
						entry.delete();

					layouts.remove( layoutEntry );
				}

			if ( handlerFunction != null )
				if ( handlerFunction.apply( entry ) == Strategy.DELETE )
					entry.delete();
		} );

		for ( LayoutEntry layoutEntry : layouts )
			if ( layoutEntry.strategy == Strategy.CREATE )
				entryContainer.createObjectEntry( layoutEntry.name );
	}

	/**
	 * Sets the function that will process objects and containers that weren't handled by the designated layout.
	 *
	 * @param handlerFunction The handler function.
	 */
	public void setHandler( Function<StorageEntry, Strategy> handlerFunction )
	{
		this.handlerFunction = handlerFunction;
	}

	public void setLayoutContainer( String name, Strategy strategy )
	{
		LayoutEntry entry = new LayoutEntry();

		entry.name = name;
		entry.containerLayout = true;
		entry.strategy = strategy;

		layouts.add( entry );
	}

	public void setLayoutObject( String name, Strategy strategy )
	{
		LayoutEntry entry = new LayoutEntry();

		entry.name = name;
		entry.containerLayout = false;
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
		boolean containerLayout;
		String name;
		Strategy strategy;
	}
}
