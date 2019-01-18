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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.support.IO;

public class PropDevMeta implements DevMetaProvider
{
	private Properties prop = new Properties();

	public PropDevMeta() throws ApplicationException.Error, IOException
	{
		this( "build.properties" );
	}

	public PropDevMeta( @Nonnull Path propFile ) throws IOException, ApplicationException.Error
	{
		loadProp( Files.newInputStream( propFile ) );
	}

	public PropDevMeta( @Nonnull File propFile ) throws FileNotFoundException, ApplicationException.Error
	{
		loadProp( new FileInputStream( propFile ) );
	}

	public PropDevMeta( @Nonnull String fileName ) throws ApplicationException.Error, IOException
	{
		this( Kernel.class, fileName );
	}

	public PropDevMeta( @Nonnull Class<?> cls, @Nonnull String fileName ) throws ApplicationException.Error, IOException
	{
		InputStream is = cls.getClassLoader().getResourceAsStream( fileName );
		if ( is == null )
			Kernel.L.warning( "The DevMeta file \"" + fileName + "\" does not exist!" );
		else
			loadProp( is );
	}

	public PropDevMeta( @Nonnull InputStream is ) throws ApplicationException.Error
	{
		loadProp( is );
	}

	public String getProperty( @Nonnull String key )
	{
		return prop.getProperty( key );
	}

	private void loadProp( @Nonnull InputStream is ) throws ApplicationException.Error
	{
		try
		{
			prop.load( is );
		}
		catch ( IOException e )
		{
			throw new ApplicationException.Error( e );
		}
		finally
		{
			IO.closeQuietly( is );
		}
	}
}
