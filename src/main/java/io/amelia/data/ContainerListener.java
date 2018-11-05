/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.util.Arrays;
import java.util.EnumSet;

public class ContainerListener
{
	private ContainerListener()
	{

	}

	public enum Flags
	{
		/**
		 * The listener will be removed once triggered.
		 */
		FIRE_ONCE,
		/**
		 * The listener will only trigger for events on the same node.
		 */
		NO_RECURSIVE,
		/**
		 * The listener will be called from the same thread that triggered the event.
		 * Keep in mind that this will block until returned and it's the only way to throw exceptions to prevent a change.
		 */
		SYNCHRONIZED
	}

	@FunctionalInterface
	public interface OnChildAdd<B>
	{
		void listen( B target );
	}

	@FunctionalInterface
	public interface OnChildRemove<B>
	{
		void listen( B target, B orphan );
	}

	@FunctionalInterface
	public interface OnValueChange<B, T>
	{
		void listen( B target, T oldValue, T newValue );
	}

	@FunctionalInterface
	public interface OnValueRemove<B, T>
	{
		void listen( B target, T oldValue );
	}

	@FunctionalInterface
	public interface OnValueStore<B, T>
	{
		void listen( B target, T newValue );
	}

	static abstract class Container
	{
		final EnumSet<Flags> flags;
		final int type;

		public Container( int type, Flags... flags )
		{
			this.type = type;
			this.flags = EnumSet.copyOf( Arrays.asList( flags ) );
		}

		abstract void call( Object[] objs ) throws Exception;
	}
}
