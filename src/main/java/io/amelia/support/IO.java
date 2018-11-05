/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import io.amelia.foundation.Kernel;
import io.amelia.injection.Libraries;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.UncaughtException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class IO
{
	public static final String PATH_SEPERATOR = File.separator;
	private static final char[] BYTE2CHAR = new char[256];
	private static final String[] BYTE2HEX = new String[256];
	private static final String[] BYTEPADDING = new String[16];
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	private static final int EOF = -1;
	private static final String[] HEXDUMP_ROWPREFIXES = new String[65536 >>> 4];
	private static final String[] HEXPADDING = new String[16];
	private static final Kernel.Logger L = Kernel.getLogger( IO.class );
	private static final String NEWLINE = "\n";
	public static final List<String> excutableExts = Lists.newArrayList( "sh", "py" );

	static
	{
		int i;

		// Generate the lookup table for byte-to-hex-dump conversion
		for ( i = 0; i < BYTE2HEX.length; i++ )
			BYTE2HEX[i] = String.format( " %02X", i );

		// Generate the lookup table for hex dump paddings
		for ( i = 0; i < HEXPADDING.length; i++ )
		{
			int padding = HEXPADDING.length - i;
			StringBuilder buf = new StringBuilder( padding * 3 );
			for ( int j = 0; j < padding; j++ )
				buf.append( "   " );
			HEXPADDING[i] = buf.toString();
		}

		// Generate the lookup table for byte dump paddings
		for ( i = 0; i < BYTEPADDING.length; i++ )
		{
			int padding = BYTEPADDING.length - i;
			StringBuilder buf = new StringBuilder( padding );
			for ( int j = 0; j < padding; j++ )
				buf.append( ' ' );
			BYTEPADDING[i] = buf.toString();
		}

		// Generate the lookup table for byte-to-char conversion
		for ( i = 0; i < BYTE2CHAR.length; i++ )
			if ( i <= 0x1f || i >= 0x7f )
				BYTE2CHAR[i] = '.';
			else
				BYTE2CHAR[i] = ( char ) i;

		// Generate the lookup table for the start-offset header in each row (up to 64KiB).
		for ( i = 0; i < HEXDUMP_ROWPREFIXES.length; i++ )
		{
			StringBuilder buf = new StringBuilder( 12 );
			buf.append( NEWLINE );
			buf.append( Long.toHexString( i << 4 & 0xFFFFFFFFL | 0x100000000L ) );
			buf.setCharAt( buf.length() - 9, '|' );
			buf.append( '|' );
			HEXDUMP_ROWPREFIXES[i] = buf.toString();
		}
	}

	/**
	 * Appends the prefix of each hex dump row. Uses the look-up table for the buffer <= 64 KiB.
	 */
	private static void appendHexDumpRowPrefix( StringBuilder dump, int row, int rowStartIndex )
	{
		if ( row < HEXDUMP_ROWPREFIXES.length )
			dump.append( HEXDUMP_ROWPREFIXES[row] );
		else
		{
			dump.append( NEWLINE );
			dump.append( Long.toHexString( rowStartIndex & 0xFFFFFFFFL | 0x100000000L ) );
			dump.setCharAt( dump.length() - 9, '|' );
			dump.append( '|' );
		}
	}

	public static File buildFile( boolean absolute, String... args )
	{
		return new File( ( absolute ? PATH_SEPERATOR : "" ) + buildPath( args ) );
	}

	public static File buildFile( File file, String... args )
	{
		return new File( file, joinPath( args ) );
	}

	public static File buildFile( String... args )
	{
		return buildFile( false, args );
	}

	public static Path buildPath( boolean absolute, String... args )
	{
		return Paths.get( ( absolute ? PATH_SEPERATOR : "" ) + buildPath( args ) );
	}

	public static Path buildPath( Path file, String... args )
	{
		return Paths.get( joinPath( args ) ).resolve( file );
	}

	public static Path buildPath( String... args )
	{
		return buildPath( false, args );
	}

	public static Byte[] bytesToBytes( byte[] bytes )
	{
		Byte[] newBytes = new Byte[bytes.length];
		for ( int i = 0; i < bytes.length; i++ )
			newBytes[i] = bytes[i];
		return newBytes;
	}

	public static String bytesToStringUTFNIO( byte[] bytes )
	{
		if ( bytes == null )
			return null;

		CharBuffer cBuffer = ByteBuffer.wrap( bytes ).asCharBuffer();
		return cBuffer.toString();
	}

	public static boolean checkMd5( File file, String expectedMd5 ) throws IOException
	{
		if ( expectedMd5 == null || file == null || !file.exists() )
			return false;

		String md5 = Encrypt.md5Hex( new FileInputStream( file ) );
		return md5 != null && md5.equals( expectedMd5 );
	}

	public static void closeQuietly( @Nullable Closeable closeable )
	{
		try
		{
			if ( closeable != null )
				closeable.close();
		}
		catch ( IOException e )
		{
			// Do Nothing
		}
	}

	public static int copy( InputStream input, OutputStream output ) throws IOException
	{
		long count = copyLarge( input, output );
		if ( count > Integer.MAX_VALUE )
			return -1;
		return ( int ) count;
	}

	public static boolean copy( File inFile, File outFile )
	{
		return copy( inFile.toPath(), outFile.toPath() );
	}

	/**
	 * This method copies one file to another location
	 *
	 * @param inPath  the source filename
	 * @param outPath the target filename
	 *
	 * @return true on success
	 */
	@SuppressWarnings( "resource" )
	public static boolean copy( Path inPath, Path outPath )
	{
		if ( !Files.exists( inPath ) )
			return false;

		if ( Files.isDirectory( inPath ) )
		{
			try
			{
				forceCreateDirectory( outPath );
				Files.list( inPath ).forEach( file -> copy( file, file.getFileName().resolve( outPath ) ) );
			}
			catch ( IOException e )
			{
				return false;
			}
		}
		else
		{
			InputStream in = null;
			OutputStream out = null;

			try
			{
				in = Files.newInputStream( inPath );
				out = Files.newOutputStream( outPath );

				copy( in, out );
			}
			catch ( IOException ioe )
			{
				return false;
			}
			finally
			{
				closeQuietly( in );
				closeQuietly( out );
			}
		}

		return true;
	}

	public static long copyLarge( InputStream input, OutputStream output ) throws IOException
	{
		return copyLarge( input, output, new byte[DEFAULT_BUFFER_SIZE] );
	}

	public static long copyLarge( InputStream input, OutputStream output, BiConsumer<Long, Integer> progressConsumer ) throws IOException
	{
		return copyLarge( input, output, new byte[DEFAULT_BUFFER_SIZE], progressConsumer );
	}

	public static long copyLarge( InputStream input, OutputStream output, byte[] buffer ) throws IOException
	{
		return copyLarge( input, output, buffer, null );
	}

	public static long copyLarge( InputStream input, OutputStream output, byte[] buffer, BiConsumer<Long, Integer> progressConsumer ) throws IOException
	{
		long count = 0;
		int n = 0;
		while ( EOF != ( n = input.read( buffer ) ) )
		{
			output.write( buffer, 0, n );
			count += n;

			if ( progressConsumer != null )
				progressConsumer.accept( count, input.available() );
		}
		return count;
	}

	private static long copyToFile( InputStream inputStream, File file ) throws IOException
	{
		return copy( inputStream, new FileOutputStream( file ) );
	}

	private static long copyToPath( InputStream inputStream, Path path ) throws IOException
	{
		return copy( inputStream, new FileOutputStream( path.toFile() ) );
	}

	public static void deleteIfExists( File file ) throws IOException
	{
		deleteIfExists( file.toPath() );
	}

	public static void deleteIfExists( Path path ) throws IOException
	{
		if ( !Files.exists( path ) )
			return;

		if ( Files.isDirectory( path, LinkOption.NOFOLLOW_LINKS ) )
			Streams.forEachWithException( Files.list( path ), IO::deleteIfExists );

		Files.delete( path );
	}

	public static String dirname( @Nonnull File path )
	{
		return dirname( path, 1 );
	}

	public static String dirname( @Nonnull File path, int levels )
	{
		Objs.notNull( path );
		Objs.notFalse( levels > 0 );

		path = path.getAbsoluteFile();

		while ( levels > 0 && path.getParent() != null )
		{
			levels--;
			path = path.getParentFile();
		}

		return path.getName();
	}

	/**
	 * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
	 * The returned array will be double the length of the passed array, as it takes two characters to represent any
	 * given byte.
	 *
	 * @param data a byte[] to convert to Hex characters
	 *
	 * @return A char[] containing hexadecimal characters
	 */
	public static char[] encodeHex( final byte[] data )
	{
		char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		final int l = data.length;
		final char[] out = new char[l << 1];
		// two characters form the hex value.
		for ( int i = 0, j = 0; i < l; i++ )
		{
			out[j++] = chars[( 0xF0 & data[i] ) >>> 4];
			out[j++] = chars[0x0F & data[i]];
		}
		return out;
	}

	public static byte[] decodeHex( @Nonnull String data )
	{
		if ( data.contains( " " ) )
			data = data.replaceAll( " ", "" );
		return DatatypeConverter.parseHexBinary( data );
	}

	public static String encodeHexPretty( final byte... data )
	{
		return Arrays.stream( encodeHexStringArray( data ) ).map( c -> "0x" + c ).collect( Collectors.joining( " " ) );
	}

	/**
	 * Converts an array of bytes into a String representing the hexadecimal values of each byte in order. The returned
	 * String will be double the length of the passed array, as it takes two characters to represent any given byte.
	 *
	 * @param data a byte[] to convert to Hex characters
	 *
	 * @return A String containing hexadecimal characters
	 */
	public static String encodeHexString( final byte[] data )
	{
		return new String( encodeHex( data ) );
	}

	public static String[] encodeHexStringArray( final byte... data )
	{
		char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		final int l = data.length;
		final String[] out = new String[l << 1];
		// two characters form the hex value.
		for ( int i = 0, j = 0; i < l; i++ )
			out[j++] = chars[( 0xF0 & data[i] ) >>> 4] + "" + chars[0x0F & data[i]];
		return out;
	}

	public static Set<String> entriesToSet( String archivePath, Enumeration<? extends ZipEntry> entries )
	{
		Set<String> result = new HashSet<>(); // avoid duplicates in case it is a subdirectory
		while ( entries.hasMoreElements() )
		{
			String name = entries.nextElement().getName();
			if ( name.startsWith( archivePath ) )
			{ // filter according to the path
				String entry = name.substring( archivePath.length() );
				int checkSubdir = entry.indexOf( "/" );
				if ( checkSubdir >= 0 )
					// if it is a subdirectory, we just return the directory name
					entry = entry.substring( 0, checkSubdir );
				result.add( entry );
			}
		}
		return result;
	}

	public static void extractLibraries( @Nonnull Path jarPath, @Nonnull Path basePath )
	{
		try
		{
			basePath = Paths.get( "libraries" ).resolve( basePath );
			forceCreateDirectory( basePath );

			if ( !Files.isRegularFile( jarPath ) || jarPath.getFileName().toString().toLowerCase().endsWith( ".jar" ) )
				L.severe( "There was a problem with the provided jar file, it was either null, not existent or did not end with jar." );

			JarFile jar = new JarFile( jarPath.toFile() );

			try
			{
				ZipEntry libDir = jar.getEntry( "libraries" );

				if ( libDir != null ) // && libDir.isDirectory() )
				{
					Enumeration<JarEntry> entries = jar.entries();
					while ( entries.hasMoreElements() )
					{
						JarEntry entry = entries.nextElement();
						if ( entry.getName().startsWith( libDir.getName() ) && !entry.isDirectory() && entry.getName().endsWith( ".jar" ) )
						{
							Path lib = Paths.get( entry.getName().substring( libDir.getName().length() + 1 ) ).resolve( basePath );

							if ( !Files.exists( lib ) )
							{
								forceCreateDirectory( lib.getParent() );
								L.info( EnumColor.GOLD + "Extracting bundled library '" + entry.getName() + "' to '" + lib.toString() + "'." );
								InputStream is = jar.getInputStream( entry );
								OutputStream out = Files.newOutputStream( lib );
								copy( is, out );
								is.close();
								out.close();
							}

							Libraries.loadLibrary( lib );
						}
					}
				}
			}
			finally
			{
				jar.close();
			}
		}
		catch ( Throwable t )
		{
			L.severe( "We had a problem extracting bundled libraries from jar file '" + jarPath.toString() + "'", t );
		}
	}

	public static boolean extractNatives( @Nonnull Path libPath, @Nonnull Path basePath ) throws IOException
	{
		List<String> nativesExtracted = new ArrayList<>();
		boolean foundArchMatchingNative = false;

		basePath = Paths.get( "natives" ).resolve( basePath );
		forceCreateDirectory( basePath );

		if ( !Files.isRegularFile( libPath ) || libPath.getFileName().toString().toLowerCase().endsWith( ".jar" ) )
			throw new IOException( "The library " + libPath.toString() + " either does not exist or is not a jar file." );

		JarFile jar = new JarFile( libPath.toFile() );
		Enumeration<JarEntry> entries = jar.entries();

		while ( entries.hasMoreElements() )
		{
			JarEntry entry = entries.nextElement();

			if ( !entry.isDirectory() && ( entry.getName().endsWith( ".so" ) || entry.getName().endsWith( ".dll" ) || entry.getName().endsWith( ".jnilib" ) || entry.getName().endsWith( ".dylib" ) ) )
				try
				{
					File internal = new File( entry.getName() );
					String newName = internal.getName();

					String os = System.getProperty( "os.name" );
					if ( os.contains( " " ) )
						os = os.substring( 0, os.indexOf( " " ) );
					os = os.replaceAll( "\\W", "" );
					os = os.toLowerCase();

					String parent = internal.getParentFile().getName();

					if ( parent.startsWith( os ) || parent.startsWith( "windows" ) || parent.startsWith( "linux" ) || parent.startsWith( "darwin" ) || parent.startsWith( "osx" ) || parent.startsWith( "solaris" ) || parent.startsWith( "cygwin" ) || parent.startsWith( "mingw" ) || parent.startsWith( "msys" ) )
						newName = parent + "/" + newName;

					if ( Arrays.asList( OSInfo.NATIVE_SEARCH_PATHS ).contains( parent ) )
						foundArchMatchingNative = true;

					Path lib = Paths.get( newName ).resolve( basePath );

					if ( Files.exists( lib ) && nativesExtracted.contains( lib.toString() ) )
						;
					L.warning( EnumColor.GOLD + "We found more than one library with the same destination '" + lib.toString() + "', if these files for different architectures, they must be in separate directories, i.e., windows, linux-x86, linux-x86_64, etc." );

					if ( Files.exists( lib ) )
					{
						forceCreateDirectory( lib );
						L.info( EnumColor.GOLD + "Extracting native library '" + entry.getName() + "' to '" + lib.toString() + "'." );
						InputStream is = jar.getInputStream( entry );
						OutputStream out = Files.newOutputStream( lib );
						byte[] buf = new byte[0x1000];
						while ( true )
						{
							int r = is.read( buf );
							if ( r == -1 )
								break;
							out.write( buf, 0, r );
						}
						is.close();
						out.close();
					}

					if ( !nativesExtracted.contains( lib.toString() ) )
						nativesExtracted.add( lib.toString() );
				}
				catch ( FileNotFoundException e )
				{
					jar.close();
					throw new IOException( "There was a problem extracting native library '" + entry.getName() + "' from jar file '" + libPath.toString() + "'", e );
				}
		}

		jar.close();

		if ( nativesExtracted.size() > 0 )
		{
			if ( !foundArchMatchingNative )
				L.warning( "We found native libraries contained within jar '" + libPath.toString() + "' but according to conventions none of them had the required architecture, the dependency may fail to load the required native if our theory is correct." );

			String path = basePath.toString().contains( " " ) ? "\"" + basePath.toString() + "\"" : basePath.toString();
			System.setProperty( "java.library.path", System.getProperty( "java.library.path" ) + ":" + path );

			try
			{
				Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
			}
			catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e )
			{
				L.severe( "We could not force the ClassAppLoader to reinitialize the LD_LIBRARY_PATH variable. You may need to set '-Djava.library.path=" + basePath.toString() + "' on next load because one or more dependencies may fail to load their native libraries.", e );
			}
		}

		return nativesExtracted.size() > 0;
	}

	public static boolean extractNatives( @Nonnull Map<String, List<String>> natives, @Nonnull Path libPath, @Nonnull Path basePath ) throws IOException
	{
		if ( !Objs.containsKeys( natives, Arrays.asList( OSInfo.NATIVE_SEARCH_PATHS ) ) )
			L.warning( String.format( "%sWe were unable to locate any natives libraries that match architectures '%s' within plugin '%s'.", EnumColor.DARK_GRAY, Strs.join( OSInfo.NATIVE_SEARCH_PATHS ), libPath.toString() ) );

		List<String> nativesExtracted = new ArrayList<>();
		basePath = Paths.get( "natives" ).resolve( basePath );
		forceCreateDirectory( basePath );

		if ( !Files.isRegularFile( libPath ) || libPath.getFileName().toString().toLowerCase().endsWith( ".jar" ) )
			throw new IOException( "The jar file " + libPath.toString() + " either does not exist or is not a jar file." );

		JarFile jar = new JarFile( libPath.toFile() );

		for ( String arch : OSInfo.NATIVE_SEARCH_PATHS )
		{
			List<String> libs = natives.get( arch.toLowerCase() );
			if ( libs != null && !libs.isEmpty() )
				for ( String lib : libs )
					try
					{
						ZipEntry entry = jar.getEntry( lib );

						if ( entry == null || entry.isDirectory() )
						{
							entry = jar.getEntry( "natives/" + lib );

							if ( entry == null || entry.isDirectory() )
								L.warning( String.format( "We were unable to load the native lib '%s' for arch '%s' for it was non-existent (or it's a directory) within plugin '%s'.", lib, arch, libPath ) );
						}

						if ( entry != null && !entry.isDirectory() )
						{
							if ( !entry.getName().endsWith( ".so" ) && !entry.getName().endsWith( ".dll" ) && !entry.getName().endsWith( ".jnilib" ) && !entry.getName().endsWith( ".dylib" ) )
								L.warning( String.format( "We found native lib '%s' for arch '%s' within plugin '%s', but it did not end with a known native library ext. We will extract it anyways but you may have problems.", lib, arch, libPath ) );

							File newNative = new File( basePath + "/" + arch + "/" + new File( entry.getName() ).getName() );

							if ( !newNative.exists() )
							{
								newNative.getParentFile().mkdirs();
								L.info( String.format( "%sExtracting native library '%s' to '%s'.", EnumColor.GOLD, entry.getName(), newNative.getAbsolutePath() ) );
								InputStream is = jar.getInputStream( entry );
								FileOutputStream out = new FileOutputStream( newNative );
								copy( is, out );
								is.close();
								out.close();
							}

							nativesExtracted.add( entry.getName() );
							// L.severe( String.format( "We were unable to load the native lib '%s' for arch '%s' within plugin '%s' for an unknown reason.", lib, arch, libFile ) );
						}
					}
					catch ( FileNotFoundException e )
					{
						jar.close();
						throw new IOException( String.format( "We had a problem extracting native library '%s' from jar file '%s'", lib, libPath.toString() ), e );
					}
		}

		// Enumeration<JarEntry> entries = jar.entries();
		jar.close();

		if ( nativesExtracted.size() > 0 )
		{
			LibraryPath path = new LibraryPath();
			path.add( basePath.toString() );
			for ( String arch : OSInfo.NATIVE_SEARCH_PATHS )
				path.add( basePath.toString() + "/" + arch );
			path.set();

			try
			{
				Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
			}
			catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e )
			{
				L.severe( "We could not force the ClassAppLoader to reinitialize the LD_LIBRARY_PATH variable. You may need to set '-Djava.library.path=" + basePath.toString() + "' on next load because one or more dependencies may fail to load their native libraries.", e );
			}
		}

		return nativesExtracted.size() > 0;
	}

	public static void extractResourceDirectory( String path, Path dest ) throws IOException
	{
		extractResourceDirectory( path, dest, IO.class );
	}

	public static void extractResourceDirectory( @Nonnull String path, @Nonnull Path dest, @Nonnull Class<?> clazz ) throws IOException
	{
		Objs.notEmpty( path );
		Objs.notNull( dest );
		Objs.notNull( clazz );

		if ( !Files.isDirectory( dest ) )
			throw new IOException( "Specified destination '" + IO.relPath( dest ) + "' is not a directory or does not exist." );

		final File jarFile = new File( clazz.getProtectionDomain().getCodeSource().getLocation().getPath() );

		if ( jarFile.isFile() )
		{  // Run with JAR file
			final JarFile jar = new JarFile( jarFile );
			final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
			while ( entries.hasMoreElements() )
			{
				final JarEntry entry = entries.nextElement();
				if ( entry.getName().startsWith( path + "/" ) )
				{ //filter according to the path
					Path savePath = Paths.get( entry.getName() ).resolve( dest );
					if ( entry.isDirectory() )
						forceCreateDirectory( savePath );
					else if ( savePath.getParent() != null )
					{
						forceCreateDirectory( savePath.getParent() );
						copyToPath( jar.getInputStream( entry ), savePath );
					}
				}
			}
			jar.close();
		}
		else
		{ // Run with IDE
			final URL url = clazz.getResource( "/" + path );
			if ( url != null )
			{
				try
				{
					final Path dir = Paths.get( url.toURI() );

					if ( !Files.isDirectory( dir ) )
						throw new IOException( "Specified resource path '" + IO.relPath( dir ) + "' is not a directory." );

					Files.list( dir ).forEach( file -> copy( file, file.getFileName().resolve( dest ) ) );
				}
				catch ( URISyntaxException ex )
				{
					// never happens
				}
			}
		}
	}

	public static void extractResourceZip( @Nonnull String res, @Nonnull Path dest ) throws IOException
	{
		extractResourceZip( res, dest, IO.class );
	}

	public static void extractResourceZip( @Nonnull String res, @Nonnull Path dest, @Nonnull Class<?> clz ) throws IOException
	{
		Path cache = Kernel.getPath( Kernel.PATH_CACHE );
		forceCreateDirectory( cache );

		Path temp = Paths.get( "temp.zip" ).resolve( cache );

		putResource( clz, res, temp );

		extractZip( temp, dest, null );
	}

	public static void extractZip( @Nonnull Path zipPath, @Nonnull Path dest ) throws IOException
	{
		extractZip( zipPath, dest, null );
	}

	public static void extractZip( @Nonnull Path zipPath, @Nonnull Path dest, BiConsumer<String, Integer> progressConsumer ) throws IOException
	{
		Objs.notFalse( Files.isRegularFile( zipPath ) );
		Objs.notFalse( Files.isDirectory( dest ) );

		ZipFile zip = new ZipFile( zipPath.toFile() );
		int count = 0;

		try
		{
			Enumeration<? extends ZipEntry> entries = zip.entries();

			while ( entries.hasMoreElements() )
			{
				count++;

				ZipEntry entry = entries.nextElement();
				Path savePath = Paths.get( entry.getName() ).resolve( dest );
				if ( entry.isDirectory() )
					forceCreateDirectory( savePath );
				else if ( savePath.getParent() != null )
				{
					forceCreateDirectory( savePath.getParent() );
					copyToPath( zip.getInputStream( entry ), savePath );

					// Temporary workaround for file execution based on file extension.
					String ext = getFileExtension( entry.getName() );
					if ( ext != null && Sys.isUnixLikeOS() && excutableExts.contains( ext.toLowerCase() ) )
						Files.setPosixFilePermissions( savePath, Lists.newHashSet( PosixFilePermission.OWNER_EXECUTE ) );

					// TODO Report bytes copied
					if ( progressConsumer != null )
						progressConsumer.accept( entry.getName(), count );
				}
			}
		}
		finally
		{
			closeQuietly( zip );
		}
	}

	public static void fileExists( File file )
	{
		Objs.notFalse( file.exists(), String.format( "%s does not exist!", file.getAbsolutePath() ) );
	}

	public static void forceCreateDirectory( @Nonnull Path path ) throws IOException
	{
		if ( Files.isDirectory( path ) )
			return;
		if ( Files.isRegularFile( path ) )
			deleteIfExists( path );
		Files.createDirectories( path );
	}

	public static long getCreation( @Nonnull Path path ) throws IOException
	{
		return Files.readAttributes( path, BasicFileAttributes.class ).creationTime().toMillis();
	}

	@Nullable
	public static String getFileExtension( @Nonnull File file )
	{
		return getFileExtension( file.getName() );
	}

	@Nullable
	public static String getFileExtension( @Nonnull String fileName )
	{
		return Objs.ifPresentGet( Strs.regexCapture( fileName, ".*\\.(.*)$" ), String::toLowerCase );
	}

	@Nullable
	public static String getFileExtension( @Nonnull Path path )
	{
		return getFileExtension( path.getFileName().toString() );
	}

	public static String getFileName( @Nonnull String path )
	{
		for ( String separator : new String[] {File.pathSeparator, "\\", "/"} )
			if ( path.contains( separator ) )
				return path.substring( path.lastIndexOf( separator ) + 1 );
		return path;
	}

	public static long getLastAccess( @Nonnull Path path ) throws IOException
	{
		return Files.readAttributes( path, BasicFileAttributes.class ).lastAccessTime().toMillis();
	}

	public static long getLastModified( @Nonnull Path path ) throws IOException
	{
		return Files.readAttributes( path, BasicFileAttributes.class ).lastModifiedTime().toMillis();
	}

	public static String getLocalName( @Nonnull Path path )
	{
		return Strs.regexCapture( path.getFileName().toString(), "^(.*)\\.[a-zA-Z0-9]+$" );
	}

	public static String getLocalName( @Nonnull File path )
	{
		return getLocalName( path.toPath() );
	}

	public static String getLocalName( @Nonnull String path )
	{
		return getLocalName( Paths.get( path ) );
	}

	public static String getParentPath( @Nonnull String path )
	{
		for ( String separator : new String[] {File.pathSeparator, "\\", "/"} )
			if ( path.contains( separator ) )
				return path.substring( 0, path.lastIndexOf( separator ) );
		return "";
	}

	/**
	 * List directory contents for a resource folder. Not recursive.
	 * This is basically a brute-force implementation.
	 * Works for regular files and also JARs.
	 *
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param path  Should end with "/", but not start with one.
	 *
	 * @return Just the name of each member item, not the full paths.
	 *
	 * @throws URISyntaxException
	 * @throws IOException
	 * @author Greg Briggs
	 */
	public static String[] getResourceListing( Class<?> clazz, String path ) throws URISyntaxException, IOException
	{
		URL dirURL = clazz.getClassLoader().getResource( path );

		if ( dirURL == null )
		{
			/*
			 * In case of a jar file, we can't actually find a directory.
			 * Have to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace( ".", "/" ) + ".class";
			dirURL = clazz.getClassLoader().getResource( me );
		}

		if ( dirURL.getProtocol().equals( "file" ) )
			/* A file path: easy enough */
			return new File( dirURL.toURI() ).list();

		if ( dirURL.getProtocol().equals( "jar" ) || dirURL.getProtocol().equals( "zip" ) )
		{
			/* A JAR or ZIP path */
			String archivePath = dirURL.getPath().substring( 5, dirURL.getPath().indexOf( "!" ) ); // strip out only the archive file
			Set<String> result;
			if ( dirURL.getProtocol().equals( "jar" ) )
			{
				JarFile jar = new JarFile( URLDecoder.decode( archivePath, "UTF-8" ) );
				result = entriesToSet( archivePath, jar.entries() );
				jar.close();
			}
			else// if ( dirURL.getProtocol().equals( "zip" ) )
			{
				ZipFile zip = new ZipFile( URLDecoder.decode( archivePath, "UTF-8" ) );
				result = entriesToSet( archivePath, zip.entries() );
				zip.close();
			}
			return result.toArray( new String[result.size()] );
		}

		throw new UnsupportedOperationException( "Cannot list files for URL " + dirURL );
	}

	public static void gzFile( File srcFile ) throws IOException
	{
		gzFile( srcFile, new File( srcFile + ".gz" ) );
	}

	public static void gzFile( File srcFile, File destFile ) throws IOException
	{
		gzFile( srcFile.toPath(), destFile.toPath() );
	}

	public static void gzFile( Path srcPath ) throws IOException
	{
		gzFile( srcPath, Paths.get( srcPath.toString() + ".gz" ) );
	}

	public static void gzFile( Path srcPath, Path destPath ) throws IOException
	{
		InputStream is = null;
		OutputStream os = null;

		try
		{
			is = Files.newInputStream( srcPath );
			os = new GZIPOutputStream( Files.newOutputStream( destPath ) );

			copy( is, os );
		}
		finally
		{
			closeQuietly( is );
			closeQuietly( os );
		}
	}

	public static String hex2Readable( byte... elements )
	{
		if ( elements == null )
			return "";

		// TODO Char Dump
		String result = "";
		char[] chars = encodeHex( elements );
		for ( int i = 0; i < chars.length; i = i + 2 )
			result += " 0x" + chars[i] + chars[i + 1];

		if ( result.length() > 0 )
			result = result.substring( 1 );

		return result;
	}

	public static String hex2Readable( int... elements )
	{
		byte[] e2 = new byte[elements.length];
		for ( int i = 0; i < elements.length; i++ )
			e2[i] = ( byte ) elements[i];
		return hex2Readable( e2 );
	}

	public static String hexDump( ByteBuf buf )
	{
		return hexDump( buf, buf.readerIndex() );
	}

	public static String hexDump( ByteBuf buf, int highlightIndex )
	{
		if ( buf == null )
			return "Buffer: null!";

		if ( buf.capacity() < 1 )
			return "Buffer: 0B!";

		StringBuilder dump = new StringBuilder();

		final int startIndex = 0;
		final int endIndex = buf.capacity();
		final int length = endIndex - startIndex;
		final int fullRows = length >>> 4;
		final int remainder = length & 0xF;

		int highlightRow = -1;

		dump.append( NEWLINE + "         +-------------------------------------------------+" + NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" + NEWLINE + "+--------+-------------------------------------------------+----------------+" );

		if ( highlightIndex > -1 )
		{
			highlightRow = highlightIndex >>> 4;
			highlightIndex = highlightIndex - ( 16 * highlightRow ) - 1;

			if ( highlightIndex < 0 )
				highlightIndex = 16 + highlightIndex;

			dump.append( NEWLINE ).append( "|        |" ).append( Strs.repeat( "   ", highlightIndex ) ).append( " $$" ).append( Strs.repeat( "   ", 15 - highlightIndex ) );
			dump.append( " |" ).append( Strs.repeat( " ", highlightIndex ) ).append( "$" ).append( Strs.repeat( " ", 15 - highlightIndex ) ).append( "|" );
		}

		// Dump the rows which have 16 bytes.
		for ( int row = 0; row < fullRows; row++ )
		{
			int rowStartIndex = row << 4;

			// Per-row prefix.
			appendHexDumpRowPrefix( dump, row, rowStartIndex );

			// Hex dump
			int rowEndIndex = rowStartIndex + 16;
			for ( int j = rowStartIndex; j < rowEndIndex; j++ )
				dump.append( BYTE2HEX[buf.getUnsignedByte( j )] );
			dump.append( " |" );

			// ASCII dump
			for ( int j = rowStartIndex; j < rowEndIndex; j++ )
				dump.append( BYTE2CHAR[buf.getUnsignedByte( j )] );
			dump.append( '|' );

			if ( highlightIndex > -1 && highlightRow == row + 1 )
				dump.append( " <--" );
		}

		// Dump the last row which has less than 16 bytes.
		if ( remainder != 0 )
		{
			int rowStartIndex = fullRows << 4;
			appendHexDumpRowPrefix( dump, fullRows, rowStartIndex );

			// Hex dump
			int rowEndIndex = rowStartIndex + remainder;
			for ( int j = rowStartIndex; j < rowEndIndex; j++ )
				dump.append( BYTE2HEX[buf.getUnsignedByte( j )] );
			dump.append( HEXPADDING[remainder] );
			dump.append( " |" );

			// Ascii dump
			for ( int j = rowStartIndex; j < rowEndIndex; j++ )
				dump.append( BYTE2CHAR[buf.getUnsignedByte( j )] );
			dump.append( BYTEPADDING[remainder] );
			dump.append( '|' );

			if ( highlightIndex > 0 && highlightRow > fullRows + 1 )
				dump.append( " <--" );
		}

		dump.append( NEWLINE + "+--------+-------------------------------------------------+----------------+" );

		return dump.toString();
	}

	public static boolean isAbsolute( String dir )
	{
		return dir.startsWith( "/" ) || dir.startsWith( ":\\", 1 );
	}

	public static void isDirectory( Path directory )
	{
		Objs.notFalse( Files.isDirectory( directory ), String.format( "%s is not a directory!", relPath( directory ) ) );
	}

	public static void isDirectory( File directory )
	{
		Objs.notFalse( directory.isDirectory(), String.format( "%s is not a directory!", directory.getAbsolutePath() ) );
	}

	public static boolean isDirectoryEmpty( File directory )
	{
		Objs.notNull( directory );
		if ( directory.isDirectory() )
		{
			String[] lst = directory.list();
			return lst != null && lst.length == 0;
		}
		return false;
	}

	public static String joinPath( @Nonnull String... paths )
	{
		return Arrays.stream( paths ).filter( n -> !Objs.isEmpty( n ) ).map( n -> Strs.trimAll( n, '/' ) ).collect( Collectors.joining( PATH_SEPERATOR ) );
	}

	@SuppressWarnings( "unchecked" )
	public static <P extends Path> Stream<P> listFiles( P p ) throws IOException
	{
		return Files.list( p ).map( path -> ( P ) path );
	}

	@SuppressWarnings( "unchecked" )
	public static <P extends Path> Stream<P> listRecursive( P p ) throws IOException
	{
		return listFiles( p ).flatMap( path -> {
			Stream<P> of = Stream.of( path );
			try
			{
				if ( Files.isRegularFile( path ) )
					return of;
				return Stream.concat( of, listFiles( path ) );
			}
			catch ( IOException e )
			{
				return of;
			}
		} );
	}

	public static Map<String, List<File>> mapExtensions( File[] files )
	{
		Map<String, List<File>> result = new TreeMap<>();
		for ( File f : files )
			result.compute( getFileExtension( f ).toLowerCase(), ( k, l ) -> l == null ? new ArrayList<>() : l ).add( f );
		return result;
	}

	public static Map<Long, List<File>> mapLastModified( File[] files )
	{
		Map<Long, List<File>> result = new TreeMap<>();
		for ( File f : files )
			result.compute( f.lastModified(), ( k, l ) -> l == null ? new ArrayList<>() : l ).add( f );
		return result;
	}

	public static <P extends Path> boolean matches( P path, String regexPattern )
	{
		try
		{
			return path.toRealPath( LinkOption.NOFOLLOW_LINKS ).toString().matches( regexPattern );
		}
		catch ( IOException e )
		{
			return path.toString().matches( regexPattern );
		}
	}

	public static void putResource( Class<?> clz, String resource, Path path ) throws IOException
	{
		try
		{
			InputStream is = clz.getClassLoader().getResourceAsStream( resource );
			if ( is == null )
				throw new IOException( String.format( "The resource %s does not exist.", resource ) );
			FileOutputStream os = new FileOutputStream( path.toFile() );
			copy( is, os );
			is.close();
			os.close();
		}
		catch ( FileNotFoundException e )
		{
			throw new IOException( e );
		}
	}

	public static void putResource( String resource, Path path ) throws IOException
	{
		putResource( IO.class, resource, path );
	}

	public static List<String> readFileToLines( @Nonnull Path file, @Nonnull String ignorePrefix ) throws IOException
	{
		return readFileToStream( file, ignorePrefix ).collect( Collectors.toList() );
	}

	public static List<String> readFileToLines( @Nonnull Path file ) throws IOException
	{
		return readFileToStream( file ).collect( Collectors.toList() );
	}

	public static List<String> readFileToLines( @Nonnull File file, @Nonnull String ignorePrefix ) throws FileNotFoundException
	{
		return readFileToStream( file, ignorePrefix ).collect( Collectors.toList() );
	}

	public static List<String> readFileToLines( @Nonnull File file ) throws FileNotFoundException
	{
		return readFileToStream( file ).collect( Collectors.toList() );
	}

	public static Stream<String> readFileToStream( @Nonnull File file, @Nonnull String ignorePrefix ) throws FileNotFoundException
	{
		return new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) ).lines().filter( s -> !s.toLowerCase().startsWith( ignorePrefix.toLowerCase() ) );
	}

	public static Stream<String> readFileToStream( @Nonnull File file ) throws FileNotFoundException
	{
		return new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) ).lines();
	}

	public static Stream<String> readFileToStream( @Nonnull Path file, @Nonnull String ignorePrefix ) throws IOException
	{
		return new BufferedReader( new InputStreamReader( Files.newInputStream( file ) ) ).lines().filter( s -> !s.toLowerCase().startsWith( ignorePrefix.toLowerCase() ) );
	}

	public static Stream<String> readFileToStream( @Nonnull Path file ) throws IOException
	{
		return new BufferedReader( new InputStreamReader( Files.newInputStream( file ) ) ).lines();
	}

	public static byte[] readFileToBytes( @Nonnull File file ) throws IOException
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream( file );
			return readStreamToBytes( in );
		}
		finally
		{
			closeQuietly( in );
		}
	}

	public static byte[] readFileToBytes( @Nonnull Path path ) throws IOException
	{
		InputStream in = null;
		try
		{
			in = Files.newInputStream( path );
			return readStreamToBytes( in );
		}
		finally
		{
			closeQuietly( in );
		}
	}

	public static String readFileToString( @Nonnull File file ) throws IOException
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream( file );
			return readStreamToString( in );
		}
		finally
		{
			closeQuietly( in );
		}
	}

	public static String readFileToString( @Nonnull Path path ) throws IOException
	{
		InputStream in = null;
		try
		{
			in = Files.newInputStream( path );
			return readStreamToString( in );
		}
		finally
		{
			closeQuietly( in );
		}
	}

	@Nonnull
	public static ByteBuffer readImageToBuffer( @Nonnull InputStream is ) throws IOException
	{
		BufferedImage bufferedimage = ImageIO.read( is );
		int[] aint = bufferedimage.getRGB( 0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), ( int[] ) null, 0, bufferedimage.getWidth() );
		ByteBuffer bytebuffer = ByteBuffer.allocate( 4 * aint.length );

		for ( int i : aint )
		{
			bytebuffer.putInt( i << 8 | i >> 24 & 255 );
		}

		bytebuffer.flip();
		return bytebuffer;
	}

	public static String readLine( @Nonnull ByteBuf buf )
	{
		if ( !buf.isReadable() || buf.readableBytes() < 1 )
			return null;

		StringBuilder op = new StringBuilder();
		while ( buf.isReadable() && buf.readableBytes() > 0 )
		{
			byte bb = buf.readByte();
			if ( bb == '\n' )
				break;
			op.append( ( char ) bb );
		}
		return op.toString();
	}

	public static ByteArrayOutputStream readStreamToByteArray( InputStream inputStream ) throws IOException
	{
		try
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ( ( nRead = inputStream.read( data, 0, data.length ) ) != -1 )
				buffer.write( data, 0, nRead );

			buffer.flush();
			return buffer;
		}
		finally
		{
			closeQuietly( inputStream );
		}
	}

	public static ByteBuf readStreamToByteBuf( InputStream inputStream ) throws IOException
	{
		try
		{
			ByteBuf buffer = Unpooled.buffer( inputStream.available() );

			int nRead;
			byte[] data = new byte[16384];

			while ( ( nRead = inputStream.read( data, 0, data.length ) ) != -1 )
				buffer.writeBytes( data, 0, nRead );

			return buffer;
		}
		finally
		{
			closeQuietly( inputStream );
		}
	}

	public static byte[] readStreamToBytes( InputStream is ) throws IOException
	{
		return readStreamToByteArray( is ).toByteArray();
	}

	public static List<String> readStreamToLines( @Nonnull InputStream is )
	{
		return readStreamToLines( is, null );
	}

	public static List<String> readStreamToLines( @Nonnull InputStream is, @Nullable String ignorePrefix )
	{
		return readStreamToStream( is, ignorePrefix ).collect( Collectors.toList() );
	}

	public static ByteBuffer readStreamToNIOBuffer( InputStream inputStream ) throws IOException
	{
		try
		{
			ByteBuffer buffer = ByteBuffer.allocate( inputStream.available() );

			int nRead;
			byte[] data = new byte[16384];

			while ( ( nRead = inputStream.read( data, 0, data.length ) ) != -1 )
				buffer.put( data, 0, nRead );

			return buffer;
		}
		finally
		{
			closeQuietly( inputStream );
		}
	}

	public static Stream<String> readStreamToStream( @Nonnull InputStream is, @Nullable String ignorePrefix )
	{
		Objs.notNull( is );

		return new BufferedReader( new InputStreamReader( is ) ).lines().filter( s -> ignorePrefix == null || !s.toLowerCase().startsWith( ignorePrefix.toLowerCase() ) );
	}

	public static Stream<String> readStreamToStream( @Nonnull InputStream is ) throws FileNotFoundException
	{
		Objs.notNull( is );

		return new BufferedReader( new InputStreamReader( is ) ).lines();
	}

	public static String readStreamToString( @Nonnull InputStream inputStream ) throws IOException
	{
		return Strs.encodeDefault( readStreamToByteArray( inputStream ).toByteArray() );
	}

	public static List<File> recursiveFiles( @Nonnull final File dir )
	{
		return recursiveFiles( dir, -1 );
	}

	/**
	 * Gathers files into a list with each entry being the full file
	 *
	 * @param start        Starting file
	 * @param current      Current file
	 * @param depth        Current depth - used internally
	 * @param maxDepth     The maximum depth to traverse
	 * @param regexPattern The regex pattern to match to each full path. Be sure to use forward slash `/` on all OSs, including Windows
	 *
	 * @return Return a recursive list of files
	 */
	private static List<File> recursiveFiles( @Nonnull final File start, @Nonnull final File current, @Nonnegative final int depth, final int maxDepth, @Nullable final String regexPattern )
	{
		final List<File> files = new ArrayList<>();

		current.list( ( dir, name ) -> {
			dir = new File( dir, name );

			if ( dir.isDirectory() && ( depth < maxDepth || maxDepth < 1 ) )
				files.addAll( recursiveFiles( start, dir, depth + 1, maxDepth, regexPattern ) );

			if ( dir.isFile() )
			{
				String filename = dir.getAbsolutePath();
				filename = filename.substring( start.getAbsolutePath().length() + 1 );
				if ( regexPattern == null || filename.replaceAll( "\\\\", "/" ).matches( regexPattern ) )
					files.add( dir );
			}

			return false;
		} );

		return files;
	}

	public static List<File> recursiveFiles( @Nonnull final File dir, final int maxDepth )
	{
		return recursiveFiles( dir, maxDepth, null );
	}

	public static List<File> recursiveFiles( @Nonnull final File dir, final int maxDepth, @Nullable final String regexPattern )
	{
		return recursiveFiles( dir, dir, 0, maxDepth, regexPattern );
	}

	/**
	 * Constructs a relative path from the server root
	 *
	 * @param file The file you wish to get relative to
	 *
	 * @return The relative path to the file, will return absolute if file is not relative to server root
	 */
	@Nullable
	public static String relPath( @Nullable Path file )
	{
		return file == null ? null : relPath( file.toAbsolutePath().toString() );
	}

	@Nullable
	public static String relPath( @Nullable Path file, @Nonnull File relTo )
	{
		return file == null ? null : relPath( file.toAbsolutePath().toString(), relTo.getAbsolutePath() );
	}

	@Nullable
	public static String relPath( @Nullable Path file, @Nonnull Path relTo )
	{
		return file == null ? null : relPath( file.toAbsolutePath().toString(), relTo.toAbsolutePath().toString() );
	}

	@Nullable
	public static String relPath( @Nullable String file )
	{
		return relPath( file, Kernel.getPath().toString() );
	}

	@Nullable
	public static String relPath( @Nullable String file, @Nonnull String relTo )
	{
		return file == null ? null : file.startsWith( relTo ) ? file.substring( relTo.length() + 1 ) : file;
	}

	/**
	 * Constructs a relative path from the server root
	 *
	 * @param file The file you wish to get relative to
	 *
	 * @return The relative path to the file, will return absolute if file is not relative to server root
	 */
	@Nullable
	public static String relPath( @Nullable File file )
	{
		return file == null ? null : relPath( file.getAbsolutePath() );
	}

	@Nullable
	public static String relPath( @Nullable File file, @Nonnull File relTo )
	{
		return file == null ? null : relPath( file.getAbsolutePath(), relTo.getAbsolutePath() );
	}

	public static String resourceToString( String resource ) throws IOException
	{
		return resourceToString( resource, IO.class );
	}

	public static String resourceToString( String resource, Class<?> clz ) throws IOException
	{
		InputStream is = clz.getClassLoader().getResourceAsStream( resource );

		if ( is == null )
			return null;

		return new String( readStreamToBytes( is ), "UTF-8" );
	}

	@Deprecated
	public static boolean setDirectoryAccess( File file )
	{
		if ( file.exists() && file.isDirectory() && file.canRead() && file.canWrite() )
			L.finest( "This application has read and write access to directory \"" + relPath( file ) + "\"!" );
		else
			try
			{
				if ( file.exists() && file.isFile() )
					Objs.notFalse( file.delete(), "failed to delete directory!" );
				Objs.notFalse( file.mkdirs(), "failed to create directory!" );
				Objs.notFalse( file.setWritable( true ), "failed to set directory writable!" );
				Objs.notFalse( file.setReadable( true ), "failed to set directory readable!" );

				L.fine( "Setting read and write access for directory \"" + relPath( file ) + "\" was successful!" );
			}
			catch ( IllegalArgumentException e )
			{
				L.severe( "Exception encountered while handling access to path '" + relPath( file ) + "' with message '" + e.getMessage() + "'" );
				return false;
			}
		return true;
	}

	@Deprecated
	public static void setDirectoryAccessWithException( File file )
	{
		if ( !setDirectoryAccess( file ) )
			throw new UncaughtException( ReportingLevel.E_ERROR, "Experienced a problem setting read and write access to directory \"" + relPath( file ) + "\"!" );
	}

	public static void setGroupReadWritePermissions( Path path ) throws IOException
	{
		Files.setPosixFilePermissions( path, Lists.newHashSet( PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE ) );
	}

	public static void setOthersReadWritePermissions( Path path ) throws IOException
	{
		Files.setPosixFilePermissions( path, Lists.newHashSet( PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE ) );
	}

	public static void setOwnerReadWritePermissions( Path path ) throws IOException
	{
		Files.setPosixFilePermissions( path, Lists.newHashSet( PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE ) );
	}

	public static void writeStringToFile( String data, File file ) throws IOException
	{
		writeStringToFile( data, file, false );
	}

	public static void writeStringToFile( String data, File file, boolean append ) throws IOException
	{
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter( new FileWriter( file, append ) );
			out.write( data );
		}
		finally
		{
			closeQuietly( out );
		}
	}

	public static void writeStringToOutputStream( @Nonnull String content, @Nonnull OutputStream output ) throws IOException
	{
		output.write( Strs.decodeDefault( content ) );
	}

	public static void writeStringToPath( String data, Path path ) throws IOException
	{
		OutputStream out = null;
		try
		{
			out = Files.newOutputStream( path );
			writeStringToOutputStream( data, out );
		}
		finally
		{
			closeQuietly( out );
		}
	}

	public static void zipDir( Path src, Path dest ) throws IOException
	{
		if ( Files.isDirectory( dest ) )
			dest = dest.resolve( "temp.zip" );

		ZipOutputStream out = new ZipOutputStream( Files.newOutputStream( dest ) );
		try
		{
			zipDirRecursive( src, src, out );
		}
		finally
		{
			out.close();
		}
	}

	public static void zipDir( File src, File dest ) throws IOException
	{
		if ( dest.isDirectory() )
			dest = new File( dest, "temp.zip" );

		ZipOutputStream out = new ZipOutputStream( new FileOutputStream( dest ) );
		try
		{
			zipDirRecursive( src, src, out );
		}
		finally
		{
			out.close();
		}
	}

	private static void zipDirRecursive( Path origPath, Path dirObj, ZipOutputStream out ) throws IOException
	{
		byte[] tmpBuf = new byte[1024];
		for ( Path path : Files.list( dirObj ).collect( Collectors.toList() ) )
		{
			if ( Files.isDirectory( path ) )
			{
				zipDirRecursive( origPath, path, out );
				continue;
			}
			InputStream is = Files.newInputStream( path );
			out.putNextEntry( new ZipEntry( relPath( path, origPath ) ) );
			copy( is, out );
			out.closeEntry();
			is.close();
		}
	}

	private static void zipDirRecursive( File origPath, File dirObj, ZipOutputStream out ) throws IOException
	{
		File[] files = dirObj.listFiles();
		byte[] tmpBuf = new byte[1024];

		for ( int i = 0; i < files.length; i++ )
		{
			if ( files[i].isDirectory() )
			{
				zipDirRecursive( origPath, files[i], out );
				continue;
			}
			FileInputStream in = new FileInputStream( files[i].getAbsolutePath() );
			out.putNextEntry( new ZipEntry( relPath( files[i], origPath ) ) );
			int len;
			while ( ( len = in.read( tmpBuf ) ) > 0 )
				out.write( tmpBuf, 0, len );
			out.closeEntry();
			in.close();
		}
	}

	private IO()
	{
		// Static Access
	}

	static class LibraryPath
	{
		private List<String> libPath = new ArrayList<>();

		LibraryPath()
		{
			read();
		}

		void add( String path )
		{
			if ( path.contains( " " ) )
				path = "\"" + path + "\"";
			if ( libPath.contains( path ) )
				return;
			libPath.add( path );
		}

		void read()
		{
			String prop = System.getProperty( "java.library.path" );
			if ( !Objs.isNull( prop ) )
				libPath.addAll( Arrays.asList( prop.split( ":" ) ) );
		}

		void set()
		{
			System.setProperty( "java.library.path", Strs.join( libPath, ":" ) );
		}
	}

	/**
	 * Separate class for native platform ID which is only loaded when native libs are loaded.
	 */
	public static class OSInfo
	{
		public static final String ARCH_NAME;
		public static final String CPU_ID;
		public static final String[] NATIVE_SEARCH_PATHS;
		public static final String OS_ID;

		static
		{
			final Object[] strings = AccessController.doPrivileged( new PrivilegedAction<Object[]>()
			{
				@Override
				public Object[] run()
				{
					// First, identify the operating system.
					boolean knownOs = true;
					String osName;
					// let the user override it.
					osName = System.getProperty( "chiori.os-name" );
					if ( osName == null )
					{
						String sysOs = System.getProperty( "os.name" );
						if ( sysOs == null )
						{
							osName = "unknown";
							knownOs = false;
						}
						else
						{
							sysOs = sysOs.toUpperCase( Locale.US );
							if ( sysOs.startsWith( "LINUX" ) )
								osName = "linux";
							else if ( sysOs.startsWith( "MAC OS" ) )
								osName = "macosx";
							else if ( sysOs.startsWith( "WINDOWS" ) )
								osName = "win";
							else if ( sysOs.startsWith( "OS/2" ) )
								osName = "os2";
							else if ( sysOs.startsWith( "SOLARIS" ) || sysOs.startsWith( "SUNOS" ) )
								osName = "solaris";
							else if ( sysOs.startsWith( "MPE/IX" ) )
								osName = "mpeix";
							else if ( sysOs.startsWith( "HP-UX" ) )
								osName = "hpux";
							else if ( sysOs.startsWith( "AIX" ) )
								osName = "aix";
							else if ( sysOs.startsWith( "OS/390" ) )
								osName = "os390";
							else if ( sysOs.startsWith( "OS/400" ) )
								osName = "os400";
							else if ( sysOs.startsWith( "FREEBSD" ) )
								osName = "freebsd";
							else if ( sysOs.startsWith( "OPENBSD" ) )
								osName = "openbsd";
							else if ( sysOs.startsWith( "NETBSD" ) )
								osName = "netbsd";
							else if ( sysOs.startsWith( "IRIX" ) )
								osName = "irix";
							else if ( sysOs.startsWith( "DIGITAL UNIX" ) )
								osName = "digitalunix";
							else if ( sysOs.startsWith( "OSF1" ) )
								osName = "osf1";
							else if ( sysOs.startsWith( "OPENVMS" ) )
								osName = "openvms";
							else if ( sysOs.startsWith( "IOS" ) )
								osName = "iOS";
							else
							{
								osName = "unknown";
								knownOs = false;
							}
						}
					}
					// Next, our CPU ID and its compatible variants.
					boolean knownCpu = true;
					ArrayList<String> cpuNames = new ArrayList<>();

					String cpuName = System.getProperty( "jboss.modules.cpu-name" );
					if ( cpuName == null )
					{
						String sysArch = System.getProperty( "os.arch" );
						if ( sysArch == null )
						{
							cpuName = "unknown";
							knownCpu = false;
						}
						else
						{
							boolean hasEndian = false;
							boolean hasHardFloatABI = false;
							sysArch = sysArch.toUpperCase( Locale.US );
							if ( sysArch.startsWith( "SPARCV9" ) || sysArch.startsWith( "SPARC64" ) )
								cpuName = "sparcv9";
							else if ( sysArch.startsWith( "SPARC" ) )
								cpuName = "sparc";
							else if ( sysArch.startsWith( "X86_64" ) || sysArch.startsWith( "AMD64" ) )
								cpuName = "x86_64";
							else if ( sysArch.startsWith( "I386" ) )
								cpuName = "i386";
							else if ( sysArch.startsWith( "I486" ) )
								cpuName = "i486";
							else if ( sysArch.startsWith( "I586" ) )
								cpuName = "i586";
							else if ( sysArch.startsWith( "I686" ) || sysArch.startsWith( "X86" ) || sysArch.contains( "IA32" ) )
								cpuName = "i686";
							else if ( sysArch.startsWith( "X32" ) )
								cpuName = "x32";
							else if ( sysArch.startsWith( "PPC64" ) )
								cpuName = "ppc64";
							else if ( sysArch.startsWith( "PPC" ) || sysArch.startsWith( "POWER" ) )
								cpuName = "ppc";
							else if ( sysArch.startsWith( "ARMV7A" ) || sysArch.contains( "AARCH32" ) )
							{
								hasEndian = true;
								hasHardFloatABI = true;
								cpuName = "armv7a";
							}
							else if ( sysArch.startsWith( "AARCH64" ) || sysArch.startsWith( "ARM64" ) || sysArch.startsWith( "ARMV8" ) || sysArch.startsWith( "PXA9" ) || sysArch.startsWith( "PXA10" ) )
							{
								hasEndian = true;
								cpuName = "aarch64";
							}
							else if ( sysArch.startsWith( "PXA27" ) )
							{
								hasEndian = true;
								cpuName = "armv5t-iwmmx";
							}
							else if ( sysArch.startsWith( "PXA3" ) )
							{
								hasEndian = true;
								cpuName = "armv5t-iwmmx2";
							}
							else if ( sysArch.startsWith( "ARMV4T" ) || sysArch.startsWith( "EP93" ) )
							{
								hasEndian = true;
								cpuName = "armv4t";
							}
							else if ( sysArch.startsWith( "ARMV4" ) || sysArch.startsWith( "EP73" ) )
							{
								hasEndian = true;
								cpuName = "armv4";
							}
							else if ( sysArch.startsWith( "ARMV5T" ) || sysArch.startsWith( "PXA" ) || sysArch.startsWith( "IXC" ) || sysArch.startsWith( "IOP" ) || sysArch.startsWith( "IXP" ) || sysArch.startsWith( "CE" ) )
							{
								hasEndian = true;
								String isaList = System.getProperty( "sun.arch.isalist" );
								if ( isaList != null )
								{
									if ( isaList.toUpperCase( Locale.US ).contains( "MMX2" ) )
										cpuName = "armv5t-iwmmx2";
									else if ( isaList.toUpperCase( Locale.US ).contains( "MMX" ) )
										cpuName = "armv5t-iwmmx";
									else
										cpuName = "armv5t";
								}
								else
									cpuName = "armv5t";
							}
							else if ( sysArch.startsWith( "ARMV5" ) )
							{
								hasEndian = true;
								cpuName = "armv5";
							}
							else if ( sysArch.startsWith( "ARMV6" ) )
							{
								hasEndian = true;
								hasHardFloatABI = true;
								cpuName = "armv6";
							}
							else if ( sysArch.startsWith( "PA_RISC2.0W" ) )
								cpuName = "parisc64";
							else if ( sysArch.startsWith( "PA_RISC" ) || sysArch.startsWith( "PA-RISC" ) )
								cpuName = "parisc";
							else if ( sysArch.startsWith( "IA64" ) )
								// HP-UX reports IA64W for 64-bit Itanium and IA64N when running
								// in 32-bit mode.
								cpuName = sysArch.toLowerCase( Locale.US );
							else if ( sysArch.startsWith( "ALPHA" ) )
								cpuName = "alpha";
							else if ( sysArch.startsWith( "MIPS" ) )
								cpuName = "mips";
							else
							{
								knownCpu = false;
								cpuName = "unknown";
							}

							boolean be = false;
							boolean hf = false;

							if ( knownCpu && hasEndian && "big".equals( System.getProperty( "sun.cpu.endian", "little" ) ) )
								be = true;

							if ( knownCpu && hasHardFloatABI )
							{
								String archAbi = System.getProperty( "sun.arch.abi" );
								if ( archAbi != null )
								{
									if ( archAbi.toUpperCase( Locale.US ).contains( "HF" ) )
										hf = true;
								}
								else
								{
									String libPath = System.getProperty( "java.library.path" );
									if ( libPath != null && libPath.toUpperCase( Locale.US ).contains( "GNUEABIHF" ) )
										hf = true;
								}
								if ( hf )
									cpuName += "-hf";
							}

							if ( knownCpu )
							{
								switch ( cpuName )
								{
									case "i686":
										cpuNames.add( "i686" );
									case "i586":
										cpuNames.add( "i586" );
									case "i486":
										cpuNames.add( "i486" );
									case "i386":
										cpuNames.add( "i386" );
										break;
									case "armv7a":
										cpuNames.add( "armv7a" );
										if ( hf )
											break;
									case "armv6":
										cpuNames.add( "armv6" );
										if ( hf )
											break;
									case "armv5t":
										cpuNames.add( "armv5t" );
									case "armv5":
										cpuNames.add( "armv5" );
									case "armv4t":
										cpuNames.add( "armv4t" );
									case "armv4":
										cpuNames.add( "armv4" );
										break;
									case "armv5t-iwmmx2":
										cpuNames.add( "armv5t-iwmmx2" );
									case "armv5t-iwmmx":
										cpuNames.add( "armv5t-iwmmx" );
										cpuNames.add( "armv5t" );
										cpuNames.add( "armv5" );
										cpuNames.add( "armv4t" );
										cpuNames.add( "armv4" );
										break;
									default:
										cpuNames.add( cpuName );
										break;
								}
								if ( hf || be )
									for ( int i = 0; i < cpuNames.size(); i++ )
									{
										String name = cpuNames.get( i );
										if ( be )
											name += "-be";
										if ( hf )
											name += "-hf";
										cpuNames.set( i, name );
									}
								cpuName = cpuNames.get( 0 );
							}
						}
					}

					// Finally, search paths.
					final int cpuCount = cpuNames.size();
					String[] searchPaths = new String[cpuCount];
					if ( knownOs && knownCpu )
						for ( int i = 0; i < cpuCount; i++ )
						{
							final String name = cpuNames.get( i );
							searchPaths[i] = osName + "-" + name;
						}
					else
						searchPaths = new String[0];

					return new Object[] {osName, cpuName, osName + "-" + cpuName, searchPaths};
				}
			} );
			OS_ID = strings[0].toString();
			CPU_ID = strings[1].toString();
			ARCH_NAME = strings[2].toString();
			NATIVE_SEARCH_PATHS = ( String[] ) strings[3];
		}
	}

	public static class PathComparatorByCreated implements Comparator<Path>
	{
		boolean descending;

		public PathComparatorByCreated()
		{
			this( true );
		}

		public PathComparatorByCreated( boolean descending )
		{
			this.descending = descending;
		}

		@Override
		public int compare( Path leftPath, Path rightPath )
		{
			long left = Objs.getOrDefault( () -> getCreation( leftPath ), 0L );
			long right = Objs.getOrDefault( () -> getCreation( rightPath ), 0L );

			return descending ? Long.compare( left, right ) : Long.compare( right, left );
		}
	}

	public static class PathComparatorByLastModified implements Comparator<Path>
	{
		boolean descending;

		public PathComparatorByLastModified()
		{
			this( true );
		}

		public PathComparatorByLastModified( boolean descending )
		{
			this.descending = descending;
		}

		@Override
		public int compare( Path leftPath, Path rightPath )
		{
			long left = Objs.getOrDefault( () -> getLastModified( leftPath ), 0L );
			long right = Objs.getOrDefault( () -> getLastModified( rightPath ), 0L );

			return descending ? Long.compare( left, right ) : Long.compare( right, left );
		}
	}

	public static class SortableFile implements Comparable<SortableFile>
	{
		public File f;
		public long t;

		public SortableFile( File file )
		{
			f = file;
			t = file.lastModified();
		}

		@Override
		public int compareTo( SortableFile o )
		{
			long u = o.t;
			return t < u ? -1 : t == u ? 0 : 1;
		}
	}

	public static char byteToChar( short inx )
	{
		return BYTE2CHAR[inx];
	}

	public static String bytesToAscii( byte[] bytes )
	{
		StringBuilder dump = new StringBuilder();
		for ( byte aByte : bytes )
			dump.append( BYTE2CHAR[aByte & 0xFF] );
		return dump.toString();
	}
}
