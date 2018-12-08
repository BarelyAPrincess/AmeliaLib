/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public abstract class StorageWatchService implements WatchService
{
	private final WatchKey CLOSE_KEY = new StorageWatchKey( ( Path ) null, ( StorageWatchService ) null )
	{
		public void cancel()
		{
		}

		public boolean isValid()
		{
			return true;
		}
	};

	private final Object closeLock = new Object();
	private final LinkedBlockingDeque<WatchKey> pendingKeys = new LinkedBlockingDeque();
	private volatile boolean closed;

	protected StorageWatchService()
	{
	}

	private void checkKey( WatchKey var1 )
	{
		if ( var1 == this.CLOSE_KEY )
		{
			this.enqueueKey( var1 );
		}

		this.checkOpen();
	}

	private void checkOpen()
	{
		if ( this.closed )
		{
			throw new ClosedWatchServiceException();
		}
	}

	public final void close() throws IOException
	{
		Object var1 = this.closeLock;
		synchronized ( this.closeLock )
		{
			if ( !this.closed )
			{
				this.closed = true;
				this.implClose();
				this.pendingKeys.clear();
				this.pendingKeys.offer( this.CLOSE_KEY );
			}
		}
	}

	final Object closeLock()
	{
		return this.closeLock;
	}

	final void enqueueKey( WatchKey var1 )
	{
		this.pendingKeys.offer( var1 );
	}

	abstract void implClose() throws IOException;

	final boolean isOpen()
	{
		return !this.closed;
	}

	public final WatchKey poll()
	{
		this.checkOpen();
		WatchKey var1 = ( WatchKey ) this.pendingKeys.poll();
		this.checkKey( var1 );
		return var1;
	}

	public final WatchKey poll( long var1, TimeUnit var3 ) throws InterruptedException
	{
		this.checkOpen();
		WatchKey var4 = ( WatchKey ) this.pendingKeys.poll( var1, var3 );
		this.checkKey( var4 );
		return var4;
	}

	abstract WatchKey register( Path var1, WatchEvent.Kind<?>[] var2, WatchEvent.Modifier... var3 ) throws IOException;

	public final WatchKey take() throws InterruptedException
	{
		this.checkOpen();
		WatchKey var1 = ( WatchKey ) this.pendingKeys.take();
		this.checkKey( var1 );
		return var1;
	}
}
