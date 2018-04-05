package io.amelia.storage.driver;

import java.util.function.Supplier;
import java.util.stream.Stream;

import io.amelia.lang.StorageException;
import io.amelia.storage.driver.entries.BaseEntry;

public class MySQLDriver<Entry extends BaseEntry> extends StorageDriver
{
	public MySQLDriver( Supplier entryMaker )
	{
		super( entryMaker );
	}

	@Override
	public BaseEntry getEntry( String localName ) throws StorageException.Error
	{
		return null;
	}

	@Override
	public Stream streamEntries( String regexPattern )
	{
		return null;
	}
}
