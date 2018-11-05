/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.parcel;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.ContainerWithValue;
import io.amelia.data.KeyValueTypesTrait;
import io.amelia.lang.ParcelableException;
import io.amelia.support.Reflection;

public class Parcel extends ContainerWithValue<Parcel, Object, ParcelableException.Error> implements KeyValueTypesTrait<ParcelableException.Error>
{
	public static Parcel empty()
	{
		try
		{
			return new Parcel();
		}
		catch ( ParcelableException.Error error )
		{
			// Ignore - should never happen!
			throw new RuntimeException( error );
		}
	}

	private Parcel() throws ParcelableException.Error
	{
		super( Parcel::new, "" );
	}

	public Parcel( @Nonnull String key ) throws ParcelableException.Error
	{
		super( Parcel::new, key );
	}

	protected Parcel( @Nonnull Parcel parent, @Nonnull String key ) throws ParcelableException.Error
	{
		super( Parcel::new, parent, key );
	}

	protected Parcel( @Nonnull Parcel parent, @Nonnull String key, @Nullable Object value ) throws ParcelableException.Error
	{
		super( Parcel::new, parent, key, value );
	}

	@SuppressWarnings( "unchecked" )
	public final <T> T getParcelable( String key ) throws ParcelableException.Error
	{
		if ( !hasChild( key ) )
			return null;

		return ( T ) getChildVoluntary( key ).mapCatchException( Factory::deserialize ).orElseThrowCause( exception -> new ParcelableException.Error( this, exception ) );
	}

	public final <T> T getParcelable( String key, Class<T> objClass ) throws ParcelableException.Error
	{
		if ( !hasChild( key ) )
			return null;

		return ( T ) getChildVoluntary( key ).mapCatchException( child -> Factory.deserialize( child, objClass ) ).orElseThrowCause( exception -> new ParcelableException.Error( this, exception ) );
	}

	@Override
	protected ParcelableException.Error getException( @Nonnull String message, Exception exception )
	{
		return new ParcelableException.Error( this, message, exception );
	}

	/**
	 * Used to serialize an Object to a {@link Parcel} and vice-versa,
	 * as well as, deserialize from bytes, e.g., file or network.
	 */
	public static class Factory
	{
		private static final Map<Class<?>, ParcelSerializer<?>> serializers = new HashMap<>();

		public static <T> T deserialize( @Nonnull Parcel src, @Nonnull Class<? extends ParcelSerializer> serializer ) throws ParcelableException.Error
		{


			return serializer.readFromParcel( src );
		}

		public static <T> T deserialize( @Nonnull Parcel src, @Nonnull Class<?> objClass ) throws ParcelableException.Error
		{
			ParcelSerializer<T> serializer = null;
			if ( ParcelSerializer.class.isAssignableFrom( objClass ) )
			{
				try
				{
					serializer = ( ( Class<ParcelSerializer<T>> ) objClass ).newInstance();
					registerClassSerializer( objClass, serializer );
				}
				catch ( InstantiationException | IllegalAccessException ignore )
				{
					// Ignore
				}
			}
			else
				serializer = getClassSerializer( ( Class<T> ) objClass );

			if ( serializer == null )
			{
				Parcelable parcelable = objClass.getAnnotation( Parcelable.class );
				try
				{
					serializer = parcelable.value().newInstance();
					registerClassSerializer( objClass, serializer );
				}
				catch ( InstantiationException | IllegalAccessException ignore )
				{
					// Ignore
				}
			}

			if ( serializer == null )
			{
				try
				{
					return ( T ) objClass.getConstructor( Parcel.class ).newInstance( src );
				}
				catch ( NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException ignore )
				{
					// Ignore
				}
			}

			if ( serializer == null )
				throw new ParcelableException.Error( src, "The parcel could not be deserialized. The serializer is missing." );

			return serializer.readFromParcel( src );
		}

		@SuppressWarnings( "unchecked" )
		public static <T> T deserialize( @Nonnull Parcel src ) throws ClassNotFoundException, ParcelableException.Error
		{
			if ( !src.hasChild( "$class" ) )
				throw new ParcelableException.Ignorable( null, "Something went wrong! The Parcel doesn't contain reference to which class we're to deserialize to." );
			return deserialize( src, ( Class<T> ) Class.forName( src.getString( "$class" ).get() ) );
		}

		@SuppressWarnings( "unchecked" )
		public static <T> ParcelSerializer<T> getClassSerializer( @Nonnull Class<T> objClass )
		{
			synchronized ( serializers )
			{
				return ( ParcelSerializer<T> ) serializers.get( objClass );
			}
		}

		public static boolean isSerializable( @Nonnull Object obj )
		{
			return !( obj instanceof Parcel ) && ( serializers.containsKey( obj.getClass() ) || Reflection.hasAnnotation( obj.getClass(), Parcelable.class ) );
		}

		public static void registerClassSerializer( @Nonnull Class<?> objClass, @Nonnull ParcelSerializer<?> parcelable )
		{
			synchronized ( serializers )
			{
				if ( serializers.containsKey( objClass ) )
					throw new ParcelableException.Ignorable( null, "The class " + objClass.getSimpleName() + " is already registered." );
				serializers.put( objClass, parcelable );
			}
		}

		public static <T> void serialize( @Nonnull T src, @Nonnull Parcel desc ) throws ParcelableException.Error
		{
			if ( src instanceof Parcel )
				throw new ParcelableException.Error( null, "You can't serialize a Parcel to a Parcel." );

			ParcelSerializer<T> serializer = getClassSerializer( ( Class<T> ) src.getClass() );

			if ( serializer == null )
			{
				Parcelable parcelable = src.getClass().getAnnotation( Parcelable.class );
				try
				{
					serializer = parcelable.value().newInstance();
					registerClassSerializer( src.getClass(), serializer );
				}
				catch ( InstantiationException | IllegalAccessException ignore )
				{
					// Ignore
				}
			}

			if ( serializer == null )
				throw new ParcelableException.Error( null, "We were unable to find a serializer for class " + src.getClass().getSimpleName() );

			serializer.writeToParcel( src, desc );

			desc.setValue( "$class", src.getClass().getSimpleName() );
		}

		public static <T> Parcel serialize( @Nonnull T src ) throws ParcelableException.Error
		{
			Parcel desc = Parcel.empty();
			serialize( src, desc );
			return desc;
		}

		private Factory()
		{
			// Static Access
		}
	}
}
