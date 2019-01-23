/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.translator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;

import io.amelia.data.ContainerBase;
import io.amelia.data.ContainerWithValue;
import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.lang.ParcelableException;
import io.amelia.storage.sql.SQLPath;
import io.amelia.support.IO;
import io.amelia.support.Namespace;
import io.amelia.support.Streams;

/**
 * Stores information to a path (file or database) similarly to how it's done with SQL
 */
public abstract class TableTranslator
{
	public static TableTranslator fromPath( Path path )
	{
		if ( !Files.isDirectory( path ) )
			throw new IllegalArgumentException( "" );
		if ( path instanceof SQLPath )
			return new SQL( path );
		return new File( path );
	}

	public static TableTranslator empty()
	{
		return new NULL();
	}

	protected final TreeSet<String> keys = new TreeSet( String.CASE_INSENSITIVE_ORDER );
	protected final Parcel root = Parcel.empty();
	protected String activeKey;
	protected final Path path;

	private TableTranslator( Path path )
	{
		this.path = path;
		disableWrite();
	}

	void disableWrite()
	{
		root.addFlag( ContainerBase.Flags.NO_FLAG_RECURSION, ContainerBase.Flags.READ_ONLY );
	}

	void enableWrite()
	{
		root.removeFlag( ContainerBase.Flags.READ_ONLY );
	}

	void bake()
	{
		keys.clear();
		root.getKeys().stream().map( Namespace::getString ).forEach( keys::add );
	}

	public void delete()
	{
		if ( activeKey == null )
			throw new IllegalStateException( "The pointer is null." );
	}

	public Parcel insert( String key )
	{
		synchronized ( root )
		{
			enableWrite();
			Parcel record = root.getChildOrCreate( key );
			record.addFlag( ContainerWithValue.Flags.VALUES_ONLY );
			keys.add( record.getLocalName() );
			disableWrite();
			return record;
		}
	}

	public void move( String key )
	{
		if ( !keys.contains( key ) )
			throw new IllegalStateException( "That pointer does not exist. {key=" + key + "}" );
		activeKey = key;
	}

	public void moveFirst()
	{
		activeKey = keys.first();
	}

	public void moveLast()
	{
		activeKey = keys.last();
	}

	public boolean movePrevious()
	{
		if ( activeKey == null )
			throw new IllegalStateException( "The pointer is null." );
		String previous = keys.lower( activeKey );
		if ( previous == null )
			return false;
		activeKey = previous;
		return true;
	}

	public boolean moveNext()
	{
		if ( activeKey == null )
			throw new IllegalStateException( "The pointer is null." );
		String next = keys.higher( activeKey );
		if ( next == null )
			return false;
		activeKey = next;
		return true;
	}

	public Parcel getRecord()
	{
		if ( activeKey == null || !root.hasChild( activeKey ) )
			throw new IllegalStateException( "The pointer is null." );
		return root.getChild( activeKey );
	}

	public int size()
	{
		return keys.size();
	}

	public void clear()
	{
		root.getChildren().forEach( ContainerWithValue::destroy );
		keys.clear();
	}

	public static class File extends TableTranslator
	{
		private File( Path path )
		{
			super( path );
		}

		public void save() throws ParcelableException.Error
		{
			synchronized ( root )
			{
				// TODO Discover common keys and limit children to only those keys
				// child.only();
				Streams.forEachWithException( root.getChildren(), ContainerBase::trimChildren );
				try
				{
					IO.writeStringToPath( ParcelLoader.encodeYaml( root ), path );
				}
				catch ( IOException e )
				{
					throw new ParcelableException.Error( root, "There was an issue writing the parcel to path " + IO.relPath( path ), e );
				}
			}
		}

		public void load() throws ParcelableException.Error
		{
			synchronized ( root )
			{
				try
				{
					clear();
					Parcel parcel = ParcelLoader.decodeYaml( path );
					Streams.forEachWithException( parcel.getChildren(), node -> root.addChild( null, node ) );
					bake();
				}
				catch ( IOException e )
				{
					throw new ParcelableException.Error( root, "There was an issue reading the parcel from path " + IO.relPath( path ), e );
				}
			}
		}
	}

	public static class SQL extends TableTranslator
	{
		private SQL( Path path )
		{
			super( path );
		}
	}

	public static class NULL extends TableTranslator
	{
		private NULL()
		{
			super( null );
		}
	}
}
