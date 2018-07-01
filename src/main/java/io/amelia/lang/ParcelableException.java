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

import io.amelia.data.ContainerBase;

public class ParcelableException
{
	private ParcelableException()
	{

	}

	public static class Error extends ApplicationException.Error
	{
		protected final ContainerBase node;

		public <T extends ContainerBase> Error( T node )
		{
			super();
			this.node = node;
		}

		public <T extends ContainerBase> Error( T node, String message )
		{
			super( message );
			this.node = node;
		}

		public <T extends ContainerBase> Error( T node, String message, Throwable cause )
		{
			super( message, cause );
			this.node = node;
		}

		public <T extends ContainerBase> Error( T node, Throwable cause )
		{
			super( cause );
			this.node = node;
		}
	}

	public static class Ignorable extends ApplicationException.Ignorable
	{
		protected final ContainerBase node;

		public <T extends ContainerBase> Ignorable( T node )
		{
			super();
			this.node = node;
		}

		public <T extends ContainerBase> Ignorable( T node, String message )
		{
			super( message );
			this.node = node;
		}

		public <T extends ContainerBase> Ignorable( T node, String message, Throwable cause )
		{
			super( message, cause );
			this.node = node;
		}

		public <T extends ContainerBase> Ignorable( T node, Throwable cause )
		{
			super( cause );
			this.node = node;
		}
	}
}
