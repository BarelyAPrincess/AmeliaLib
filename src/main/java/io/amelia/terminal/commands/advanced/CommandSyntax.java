/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.terminal.commands.advanced;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandSyntax
{
	protected List<String> arguments = new LinkedList<>();
	protected String originalSyntax;
	protected String regexp;

	public CommandSyntax( String syntax )
	{
		originalSyntax = syntax;

		regexp = prepareSyntaxRegexp( syntax );
	}

	public Map<String, String> getMatchedArguments( String str )
	{
		Map<String, String> matchedArguments = new HashMap<>( arguments.size() );

		if ( arguments.size() > 0 )
		{
			Matcher argMatcher = Pattern.compile( regexp ).matcher( str );

			if ( argMatcher.find() )
				for ( int index = 1; index <= argMatcher.groupCount(); index++ )
				{
					String argumentValue = argMatcher.group( index );
					if ( argumentValue == null || argumentValue.isEmpty() )
						continue;

					if ( argumentValue.startsWith( "\"" ) && argumentValue.endsWith( "\"" ) )
						argumentValue = argumentValue.substring( 1, argumentValue.length() - 1 );

					matchedArguments.put( arguments.get( index - 1 ), argumentValue );
				}
		}
		return matchedArguments;
	}

	public String getRegexp()
	{
		return regexp;
	}

	public boolean isMatch( String str )
	{
		return str.matches( regexp );
	}

	private String prepareSyntaxRegexp( String syntax )
	{
		String expression = syntax;

		Matcher argMatcher = Pattern.compile( "(?:[\\s]+)?((\\<|\\[)([^\\>\\]]+)(?:\\>|\\]))" ).matcher( expression );
		// Matcher argMatcher = Pattern.compile("(\\<|\\[)([^\\>\\]]+)(?:\\>|\\])").matcher(expression);

		int index = 0;
		while ( argMatcher.find() )
		{
			if ( argMatcher.group( 2 ).equals( "[" ) )
				expression = expression.replace( argMatcher.group( 0 ), "(?:(?:[\\s]+)(\"[^\"]+\"|[^\\s]+))?" );
			else
				expression = expression.replace( argMatcher.group( 1 ), "(\"[^\"]+\"|[\\S]+)" );

			arguments.add( index++, argMatcher.group( 3 ) );
		}

		return expression;
	}
}
