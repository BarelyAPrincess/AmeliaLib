/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import org.fusesource.jansi.Ansi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * All supported color values for chat
 *
 * TODO Implement background color codes
 */
public enum EnumColor
{
	/**
	 * Represents black
	 */
	BLACK( Ansi.ansi().fgBright( Ansi.Color.BLACK ).boldOff().toString(), '0', 0x00 ),
	/**
	 * Represents dark blue
	 */
	DARK_BLUE( Ansi.ansi().fgBright( Ansi.Color.BLUE ).boldOff().toString(), '1', 0x1 ),
	/**
	 * Represents dark green
	 */
	DARK_GREEN( Ansi.ansi().fgBright( Ansi.Color.GREEN ).boldOff().toString(), '2', 0x2 ),
	/**
	 * Represents dark blue (aqua)
	 */
	DARK_AQUA( Ansi.ansi().fgBright( Ansi.Color.CYAN ).boldOff().toString(), '3', 0x3 ),
	/**
	 * Represents dark red
	 */
	DARK_RED( Ansi.ansi().fgBright( Ansi.Color.RED ).boldOff().toString(), '4', 0x4 ),
	/**
	 * Represents dark purple
	 */
	DARK_PURPLE( Ansi.ansi().fgBright( Ansi.Color.MAGENTA ).boldOff().toString(), '5', 0x5 ),
	/**
	 * Represents gold
	 */
	GOLD( Ansi.ansi().fgBright( Ansi.Color.YELLOW ).boldOff().toString(), '6', 0x6 ),
	/**
	 * Represents gray
	 */
	GRAY( Ansi.ansi().fgBright( Ansi.Color.WHITE ).boldOff().toString(), '7', 0x7 ),
	/**
	 * Represents dark gray
	 */
	DARK_GRAY( Ansi.ansi().fgBright( Ansi.Color.BLACK ).bold().toString(), '8', 0x8 ),
	/**
	 * Represents blue
	 */
	BLUE( Ansi.ansi().fgBright( Ansi.Color.BLUE ).bold().toString(), '9', 0x9 ),
	/**
	 * Represents green
	 */
	GREEN( Ansi.ansi().fgBright( Ansi.Color.GREEN ).bold().toString(), 'a', 0xA ),
	/**
	 * Represents aqua
	 */
	AQUA( Ansi.ansi().fgBright( Ansi.Color.CYAN ).bold().toString(), 'b', 0xB ),
	/**
	 * Represents red
	 */
	RED( Ansi.ansi().fgBright( Ansi.Color.RED ).bold().toString(), 'c', 0xC ),
	/**
	 * Represents light purple
	 */
	LIGHT_PURPLE( Ansi.ansi().fgBright( Ansi.Color.MAGENTA ).bold().toString(), 'd', 0xD ),
	/**
	 * Represents yellow
	 */
	YELLOW( Ansi.ansi().fgBright( Ansi.Color.YELLOW ).bold().toString(), 'e', 0xE ),
	/**
	 * Represents white
	 */
	WHITE( Ansi.ansi().fgBright( Ansi.Color.WHITE ).bold().toString(), 'f', 0xF ),
	/**
	 * Represents magical characters that change around randomly
	 */
	MAGIC( Ansi.ansi().a( Ansi.Attribute.BLINK_SLOW ).toString(), 'k', 0x10, true ),
	/**
	 * Makes the text bold.
	 */
	BOLD( Ansi.ansi().a( Ansi.Attribute.INTENSITY_BOLD ).toString(), 'l', 0x11, true ),
	/**
	 * Makes a line appear through the text.
	 */
	STRIKETHROUGH( Ansi.ansi().a( Ansi.Attribute.STRIKETHROUGH_ON ).toString(), 'm', 0x12, true ),
	/**
	 * Makes the text appear underlined.
	 */
	UNDERLINE( Ansi.ansi().a( Ansi.Attribute.UNDERLINE ).toString(), 'n', 0x13, true ),
	/**
	 * Makes the text italic.
	 */
	ITALIC( Ansi.ansi().a( Ansi.Attribute.ITALIC ).toString(), 'o', 0x14, true ),
	/**
	 * Resets all previous chat colors or formats.
	 */
	RESET( Ansi.ansi().a( Ansi.Attribute.RESET ).fg( Ansi.Color.DEFAULT ).toString(), 'r', 0x15 ),

	FAINT( Ansi.ansi().a( Ansi.Attribute.INTENSITY_FAINT ).toString(), 'z', 0x16 ),

	NEGATIVE( Ansi.ansi().a( Ansi.Attribute.NEGATIVE_ON ).toString(), 'x', 0x17 );

	/**
	 * The special character which prefixes all chat color codes. Use this if you need to dynamically convert color
	 * codes from your custom format.
	 */
	public static final char COLOR_CHAR = '\u00A7';
	public static final String QUOTATION_COLOR = EnumColor.AQUA.getColorString();
	private static final Map<Character, EnumColor> BY_CHAR = new HashMap<>();
	private static final Map<Integer, EnumColor> BY_ID = new HashMap<>();
	private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile( "(?i)" + COLOR_CHAR + "[0-9A-FK-OR]" );

	static
	{
		for ( EnumColor color : values() )
		{
			BY_ID.put( color.intCode, color );
			BY_CHAR.put( color.code, color );
		}
	}

	public static String format( Level level, String rawMessage )
	{
		rawMessage = translateAlternateColorCodes( '&', rawMessage );

		StringBuilder finalMessage = new StringBuilder();
		EnumColor lastFgColor = null;
		// EnumColor lastBgColor = null;
		EnumColor lastFormat = null;
		boolean quoteStart = false;
		for ( int i = 0; i < rawMessage.length(); i++ )
			if ( rawMessage.charAt( i ) == COLOR_CHAR )
			{
				i++;
				EnumColor enumColor = getByChar( rawMessage.charAt( i ) );
				if ( enumColor != null )
					if ( enumColor.isColor() )
						lastFgColor = enumColor;
					else if ( enumColor.isFormat() )
						lastFormat = enumColor;
				finalMessage.append( enumColor.getColorString() );
			}
			else if ( rawMessage.charAt( i ) == '"' )
			{
				// TODO Make this a configurable option with added regex patterns
				quoteStart = !quoteStart;
				if ( quoteStart )
					// TODO Change quotation color based log level
					finalMessage.append( QUOTATION_COLOR ).append( '"' );
				else
				{
					finalMessage.append( '"' );
					if ( lastFgColor != null )
						finalMessage.append( lastFgColor.getColorString() );
					if ( lastFormat != null )
						finalMessage.append( lastFormat.getColorString() );
					// TODO Offer reset color and format separate
					if ( lastFgColor == null && lastFormat == null )
						finalMessage.append( RESET.getColorString() );
				}
			}
			else
				finalMessage.append( rawMessage.charAt( i ) );

		return finalMessage.toString() + EnumColor.RESET.getColorString();
	}

	public static EnumColor fromLevel( Level var1 )
	{
		if ( var1 == Level.FINEST || var1 == Level.FINER || var1 == Level.FINE )
			return GRAY;
		else if ( var1 == Level.INFO )
			return WHITE;
		else if ( var1 == Level.WARNING )
			return GOLD;
		else if ( var1 == Level.SEVERE )
			return RED;
		else if ( var1 == Level.CONFIG )
			return DARK_PURPLE;
		else
			return WHITE;
	}

	/**
	 * Gets the color represented by the specified color code
	 *
	 * @param code Code to check
	 *
	 * @return Associative ConsoleColor with the given code, or null if it doesn't exist
	 */
	public static EnumColor getByChar( char code )
	{
		return BY_CHAR.get( code );
	}

	/**
	 * Gets the color represented by the specified color code
	 *
	 * @param code Code to check
	 *
	 * @return Associative ConsoleColor with the given code, or null if it doesn't exist
	 */
	public static EnumColor getByChar( String code )
	{
		Objs.notNull( code, "Code cannot be null" );
		Objs.notNegative( code.length(), "Code must have at least one char" );

		return BY_CHAR.get( code.charAt( 0 ) );
	}

	public static EnumColor getById( int id )
	{
		return BY_ID.get( id );
	}

	/**
	 * Gets the ChatColors used at the end of the given input string.
	 *
	 * @param input Input string to retrieve the colors from.
	 *
	 * @return Any remaining ChatColors to pass onto the next line.
	 */
	public static String getLastColors( String input )
	{
		String result = "";
		int length = input.length();

		// Search backwards from the end as it is faster
		for ( int index = length - 1; index > -1; index-- )
		{
			char section = input.charAt( index );
			if ( section == COLOR_CHAR && index < length - 1 )
			{
				char c = input.charAt( index + 1 );
				EnumColor color = getByChar( c );

				if ( color != null )
				{
					result = color.toString() + result;

					// Once we find a color or reset we can stop searching
					if ( color.isColor() || color.equals( RESET ) )
						break;
				}
			}
		}

		return result;
	}

	/**
	 * Used when chaining colors together.
	 */
	public static String join( EnumColor... colors )
	{
		return Arrays.stream( colors ).map( EnumColor::toString ).collect( Collectors.joining() );
	}

	public static String removeAltColors( String var )
	{
		var = var.replaceAll( "&.", "" );
		var = var.replaceAll( "§.", "" );
		return var;
	}

	/**
	 * Strips the given message of all color codes
	 *
	 * @param input String to strip of color
	 *
	 * @return A copy of the input string, without any coloring
	 */
	public static String stripColor( final String input )
	{
		if ( input == null )
			return null;

		return STRIP_COLOR_PATTERN.matcher( input ).replaceAll( "" );
	}

	/**
	 * Translates a string using an alternate color code character into a string that uses the internal
	 * ConsoleColor.COLOR_CODE color code character. The alternate color code character will only be replaced if it is
	 * immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
	 *
	 * @param altColorChar    The alternate color code character to replace. Ex: &
	 * @param textToTranslate Text containing the alternate color code character.
	 *
	 * @return Text containing the ChatColor.COLOR_CODE color code character.
	 */
	public static String translateAlternateColorCodes( char altColorChar, String textToTranslate )
	{
		char[] b = textToTranslate.toCharArray();
		for ( int i = 0; i < b.length - 1; i++ )
			if ( b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf( b[i + 1] ) > -1 )
			{
				b[i] = EnumColor.COLOR_CHAR;
				b[i + 1] = Character.toLowerCase( b[i + 1] );
			}
		return new String( b );
	}

	private final char code;
	private final String colorString;
	private final int intCode;
	private final boolean isFormat;
	private final String toString;

	EnumColor( String colorString, char code, int intCode )
	{
		this( colorString, code, intCode, false );
	}

	EnumColor( String colorString, char code, int intCode, boolean isFormat )
	{
		this.colorString = colorString;
		this.code = code;
		this.intCode = intCode;
		this.isFormat = isFormat;
		this.toString = new String( new char[] {COLOR_CHAR, code} );
	}

	/**
	 * Gets the char value associated with this color
	 *
	 * @return A char value of this color code
	 */
	public char getChar()
	{
		return code;
	}

	public String getColorString()
	{
		return colorString;
	}

	/**
	 * Checks if this code is a color code as opposed to a format code.
	 */
	public boolean isColor()
	{
		return !isFormat && this != RESET;
	}

	/**
	 * Checks if this code is a format code as opposed to a color code.
	 */
	public boolean isFormat()
	{
		return isFormat;
	}

	@Override
	public String toString()
	{
		return toString;
	}}
