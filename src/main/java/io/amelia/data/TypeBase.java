/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.support.Namespace;

/**
 * Outlines a config key along with it's default value.
 */
public class TypeBase
{
	private final TypeBase parent;
	private final Namespace path;

	public TypeBase( @Nonnull String path )
	{
		this( null, Namespace.of( path ) );
	}

	public TypeBase( @Nonnull Namespace path )
	{
		this( null, path );
	}

	public TypeBase( @Nullable TypeBase parent, @Nonnull String path )
	{
		this( parent, Namespace.of( path ) );
	}

	public TypeBase( @Nullable TypeBase parent, @Nonnull Namespace path )
	{
		this.parent = parent;
		this.path = path;
	}

	public Namespace getPath()
	{
		return ( parent == null ? Namespace.empty() : parent.getPath() ).append( path );
	}

	public String getStringPath()
	{
		return getPath().getString();
	}

	public static class TypeBoolean extends TypeWithDefault<Boolean>
	{
		public TypeBoolean( String path, Supplier<Boolean> def )
		{
			super( path, def );
		}

		public TypeBoolean( String path, Boolean def )
		{
			super( path, def );
		}

		public TypeBoolean( TypeBase parent, String path, Supplier<Boolean> def )
		{
			super( parent, path, def );
		}

		public TypeBoolean( TypeBase parent, String path, Boolean def )
		{
			super( parent, path, def );
		}

		public TypeBoolean( Namespace path, Supplier<Boolean> def )
		{
			super( path, def );
		}

		public TypeBoolean( Namespace path, Boolean def )
		{
			super( path, def );
		}

		public TypeBoolean( TypeBase parent, Namespace path, Supplier<Boolean> def )
		{
			super( parent, path, def );
		}

		public TypeBoolean( TypeBase parent, Namespace path, Boolean def )
		{
			super( parent, path, def );
		}
	}

	public static class TypeColor extends TypeWithDefault<Color>
	{
		public TypeColor( String path, Supplier<Color> def )
		{
			super( path, def );
		}

		public TypeColor( String path, Color def )
		{
			super( path, def );
		}

		public TypeColor( TypeBase parent, String path, Supplier<Color> def )
		{
			super( parent, path, def );
		}

		public TypeColor( TypeBase parent, String path, Color def )
		{
			super( parent, path, def );
		}

		public TypeColor( Namespace path, Supplier<Color> def )
		{
			super( path, def );
		}

		public TypeColor( Namespace path, Color def )
		{
			super( path, def );
		}

		public TypeColor( TypeBase parent, Namespace path, Supplier<Color> def )
		{
			super( parent, path, def );
		}

		public TypeColor( TypeBase parent, Namespace path, Color def )
		{
			super( parent, path, def );
		}
	}

	public static class TypeDouble extends TypeWithDefault<Double>
	{
		public TypeDouble( String path, Supplier<Double> def )
		{
			super( path, def );
		}

		public TypeDouble( String path, Double def )
		{
			super( path, def );
		}

		public TypeDouble( TypeBase parent, String path, Supplier<Double> def )
		{
			super( parent, path, def );
		}

		public TypeDouble( TypeBase parent, String path, Double def )
		{
			super( parent, path, def );
		}

		public TypeDouble( Namespace path, Supplier<Double> def )
		{
			super( path, def );
		}

		public TypeDouble( Namespace path, Double def )
		{
			super( path, def );
		}

		public TypeDouble( TypeBase parent, Namespace path, Supplier<Double> def )
		{
			super( parent, path, def );
		}

		public TypeDouble( TypeBase parent, Namespace path, Double def )
		{
			super( parent, path, def );
		}
	}

	public static class TypeEnum<T extends Enum<T>> extends TypeWithDefault<T>
	{
		private final Class<T> enumClass;

		public TypeEnum( String path, T def, Class<T> enumClass )
		{
			super( path, def );
			this.enumClass = enumClass;
		}

		public TypeEnum( TypeBase parent, String path, T def, Class<T> enumClass )
		{
			super( parent, path, def );
			this.enumClass = enumClass;
		}

		public TypeEnum( TypeBase parent, String path, Supplier<T> def, Class<T> enumClass )
		{
			super( parent, path, def );
			this.enumClass = enumClass;
		}

		public TypeEnum( String path, Supplier<T> def, Class<T> enumClass )
		{
			super( path, def );
			this.enumClass = enumClass;
		}

		public TypeEnum( Namespace path, T def, Class<T> enumClass )
		{
			super( path, def );
			this.enumClass = enumClass;
		}

		public TypeEnum( TypeBase parent, Namespace path, T def, Class<T> enumClass )
		{
			super( parent, path, def );
			this.enumClass = enumClass;
		}

		public TypeEnum( TypeBase parent, Namespace path, Supplier<T> def, Class<T> enumClass )
		{
			super( parent, path, def );
			this.enumClass = enumClass;
		}

		public TypeEnum( Namespace path, Supplier<T> def, Class<T> enumClass )
		{
			super( path, def );
			this.enumClass = enumClass;
		}

		public Class<T> getEnumClass()
		{
			return enumClass;
		}
	}

	public static class TypeFile extends TypeWithDefault<File>
	{
		public TypeFile( String path, Supplier<File> def )
		{
			super( path, def );
		}

		public TypeFile( String path, File def )
		{
			super( path, def );
		}

		public TypeFile( TypeBase parent, String path, Supplier<File> def )
		{
			super( parent, path, def );
		}

		public TypeFile( TypeBase parent, String path, File def )
		{
			super( parent, path, def );
		}

		public TypeFile( Namespace path, Supplier<File> def )
		{
			super( path, def );
		}

		public TypeFile( Namespace path, File def )
		{
			super( path, def );
		}

		public TypeFile( TypeBase parent, Namespace path, Supplier<File> def )
		{
			super( parent, path, def );
		}

		public TypeFile( TypeBase parent, Namespace path, File def )
		{
			super( parent, path, def );
		}
	}

	public static class TypeInteger extends TypeWithDefault<Integer>
	{
		public TypeInteger( String path, Supplier<Integer> def )
		{
			super( path, def );
		}

		public TypeInteger( String path, Integer def )
		{
			super( path, def );
		}

		public TypeInteger( TypeBase parent, String path, Supplier<Integer> def )
		{
			super( parent, path, def );
		}

		public TypeInteger( TypeBase parent, String path, Integer def )
		{
			super( parent, path, def );
		}

		public TypeInteger( Namespace path, Supplier<Integer> def )
		{
			super( path, def );
		}

		public TypeInteger( Namespace path, Integer def )
		{
			super( path, def );
		}

		public TypeInteger( TypeBase parent, Namespace path, Supplier<Integer> def )
		{
			super( parent, path, def );
		}

		public TypeInteger( TypeBase parent, Namespace path, Integer def )
		{
			super( parent, path, def );
		}
	}

	public static class TypeLong extends TypeWithDefault<Long>
	{
		public TypeLong( String path, Supplier<Long> def )
		{
			super( path, def );
		}

		public TypeLong( String path, Long def )
		{
			super( path, def );
		}

		public TypeLong( TypeBase parent, String path, Supplier<Long> def )
		{
			super( parent, path, def );
		}

		public TypeLong( TypeBase parent, String path, Long def )
		{
			super( parent, path, def );
		}

		public TypeLong( Namespace path, Supplier<Long> def )
		{
			super( path, def );
		}

		public TypeLong( Namespace path, Long def )
		{
			super( path, def );
		}

		public TypeLong( TypeBase parent, Namespace path, Supplier<Long> def )
		{
			super( parent, path, def );
		}

		public TypeLong( TypeBase parent, Namespace path, Long def )
		{
			super( parent, path, def );
		}
	}

	public static class TypePath extends TypeWithDefault<Path>
	{
		public TypePath( String path, Supplier<Path> def )
		{
			super( path, def );
		}

		public TypePath( String path, Path def )
		{
			super( path, def );
		}

		public TypePath( TypeBase parent, String path, Supplier<Path> def )
		{
			super( parent, path, def );
		}

		public TypePath( TypeBase parent, String path, Path def )
		{
			super( parent, path, def );
		}

		public TypePath( Namespace path, Supplier<Path> def )
		{
			super( path, def );
		}

		public TypePath( Namespace path, Path def )
		{
			super( path, def );
		}

		public TypePath( TypeBase parent, Namespace path, Supplier<Path> def )
		{
			super( parent, path, def );
		}

		public TypePath( TypeBase parent, Namespace path, Path def )
		{
			super( parent, path, def );
		}
	}

	public static class TypeString extends TypeWithDefault<String>
	{
		public TypeString( String path, Supplier<String> def )
		{
			super( path, def );
		}

		public TypeString( String path, String def )
		{
			super( path, def );
		}

		public TypeString( TypeBase parent, String path, Supplier<String> def )
		{
			super( parent, path, def );
		}

		public TypeString( TypeBase parent, String path, String def )
		{
			super( parent, path, def );
		}

		public TypeString( Namespace path, Supplier<String> def )
		{
			super( path, def );
		}

		public TypeString( Namespace path, String def )
		{
			super( path, def );
		}

		public TypeString( TypeBase parent, Namespace path, Supplier<String> def )
		{
			super( parent, path, def );
		}

		public TypeString( TypeBase parent, Namespace path, String def )
		{
			super( parent, path, def );
		}
	}

	public static class TypeStringList extends TypeWithDefault<List<String>>
	{
		public TypeStringList( String path, Supplier<List<String>> def )
		{
			super( path, def );
		}

		public TypeStringList( String path, List<String> def )
		{
			super( path, def );
		}

		public TypeStringList( TypeBase parent, String path, Supplier<List<String>> def )
		{
			super( parent, path, def );
		}

		public TypeStringList( TypeBase parent, String path, List<String> def )
		{
			super( parent, path, def );
		}

		public TypeStringList( Namespace path, Supplier<List<String>> def )
		{
			super( path, def );
		}

		public TypeStringList( Namespace path, List<String> def )
		{
			super( path, def );
		}

		public TypeStringList( TypeBase parent, Namespace path, Supplier<List<String>> def )
		{
			super( parent, path, def );
		}

		public TypeStringList( TypeBase parent, Namespace path, List<String> def )
		{
			super( parent, path, def );
		}
	}

	public static class TypeWithDefault<DefValue> extends TypeBase
	{
		private final Supplier<DefValue> def;

		public TypeWithDefault( Namespace path, Supplier<DefValue> def )
		{
			this( null, path, def );
		}

		public TypeWithDefault( Namespace path, DefValue def )
		{
			this( null, path, def );
		}

		public TypeWithDefault( String path, Supplier<DefValue> def )
		{
			this( null, path, def );
		}

		public TypeWithDefault( String path, DefValue def )
		{
			this( null, path, def );
		}

		public TypeWithDefault( TypeBase parent, String path, Supplier<DefValue> def )
		{
			super( parent, path );
			this.def = def;
		}

		public TypeWithDefault( TypeBase parent, Namespace path, Supplier<DefValue> def )
		{
			super( parent, path );
			this.def = def;
		}

		public TypeWithDefault( @Nonnull TypeBase parent, @Nonnull String path, @Nullable DefValue def )
		{
			super( parent, path );
			this.def = () -> def;
		}

		public TypeWithDefault( @Nonnull TypeBase parent, @Nonnull Namespace path, @Nullable DefValue def )
		{
			super( parent, path );
			this.def = () -> def;
		}

		public DefValue getDefault()
		{
			return def.get();
		}

		public Supplier<DefValue> getDefaultSupplier()
		{
			return def;
		}
	}
}
