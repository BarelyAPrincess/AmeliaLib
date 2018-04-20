package io.amelia.data;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * Outlines a config key along with it's default value.
 */
public class TypeBase
{
	private final TypeBase parent;
	private final String path;

	public TypeBase( String path )
	{
		this( null, path );
	}

	public TypeBase( TypeBase parent, String path )
	{
		this.parent = parent;
		this.path = path.replace( '\\', '.' ).replace( '/', '.' );
	}

	public String getPath()
	{
		return ( parent == null ? "" : parent.getPath() + "." ) + path;
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
	}

	protected static class TypeWithDefault<DefValue> extends TypeBase
	{
		private final Supplier<DefValue> def;

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

		public TypeWithDefault( TypeBase parent, String path, DefValue def )
		{
			super( parent, path );
			this.def = () -> def;
		}

		public DefValue getDefault()
		{
			return def.get();
		}
	}
}
