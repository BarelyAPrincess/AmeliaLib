package io.amelia.storage.methods;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageContainerTrait;
import io.amelia.storage.StorageContext;
import io.amelia.storage.StorageEntry;
import io.amelia.storage.StorageEntryContainer;
import io.amelia.storage.StorageEntryObject;
import io.amelia.storage.StorageMapper;
import io.amelia.storage.StorageMethod;
import io.amelia.support.Strs;
import io.amelia.support.data.ParcelLoader;
import io.amelia.support.data.StackerWithValue;

public class ContainerToStackerMethod implements StorageMethod
{
	private final StorageContainerTrait storageContainer;

	public ContainerToStackerMethod( @Nonnull StorageContainerTrait storageContainer )
	{
		this.storageContainer = storageContainer;
	}

	public <Stacker extends StackerWithValue> void toStacker( @Nonnull Stacker parcel, @Nonnull Supplier<Stacker> supplier, @Nonnull StorageContainerTrait storageContainer, @Nonnull String nestingPrefix ) throws StorageException.Error
	{
		for ( StorageEntry nextEntry : storageContainer.streamEntries( new ParcelStorageMapper() ).collect( Collectors.toList() ) )
		{
			String newNestingPrefix = Strs.join( new String[] {nestingPrefix, nextEntry.getLocalName()}, "." );

			try
			{
				if ( nextEntry.isContainer() )
					toStacker( parcel, supplier, ( StorageContainerTrait ) nextEntry, newNestingPrefix );
				else
					( ( StorageEntryConfig ) nextEntry ).parseToStacker( parcel, supplier, newNestingPrefix );
			}
			catch ( Exception e )
			{
				throw StorageException.error( "Failed to load configuration node " + nextEntry.getFullPath(), e );
			}
		}
	}

	public <Stacker extends StackerWithValue> void toStacker( @Nonnull Stacker stacker, @Nonnull Supplier<Stacker> supplier ) throws StorageException.Error
	{
		toStacker( stacker, supplier, storageContainer, "" );
	}

	private class ParcelStorageMapper implements StorageMapper
	{
		@Override
		public StorageEntry mapStorageContext( StorageContext storageContext ) throws StorageException.Error
		{
			try
			{
				return storageContext.isContainer() ? new StorageEntryContainer( storageContext ) : new StorageEntryConfig( storageContext );
			}
			catch ( IOException e )
			{
				throw StorageException.error( e );
			}
		}
	}

	private class StorageEntryConfig extends StorageEntryObject
	{
		public StorageEntryConfig( StorageContext storageContext ) throws IOException
		{
			super( storageContext );
		}

		private <Stacker extends StackerWithValue> void parseToStacker( @Nonnull Stacker stacker, @Nonnull Supplier<Stacker> supplier, @Nonnull String nesting ) throws StorageException.Error
		{
			String name = getLocalName().toLowerCase();
			Map<String, Object> map;

			// TODO Add more supported types, .e.g., `.groovy` using the ScriptingFactory.
			if ( name.endsWith( ".yaml" ) || name.endsWith( ".yml" ) )
				map = ParcelLoader.decodeYamlToMap( getContentAsString() );
			else if ( name.endsWith( ".json" ) )
				map = ParcelLoader.decodeJsonToMap( getContentAsString() );
			else if ( name.endsWith( ".list" ) )
				map = ParcelLoader.decodeListToMap( getContentAsString() );
			else if ( name.endsWith( ".properties" ) )
				map = ParcelLoader.decodePropToMap( getContentAsString() );
			else
				throw StorageException.ignorable( "Could not parse file " + getFullPath() );

			ParcelLoader.decodeMap( map, stacker );
		}
	}
}
