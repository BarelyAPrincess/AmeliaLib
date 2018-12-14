package io.amelia.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.parcel.Parcel;

public class Parcels
{
	public static Optional<String> parseFormatString( @Nonnull Parcel data, @Nullable String format )
	{
		if ( Strs.isEmpty( format ) )
			return Optional.empty();

		// Add whitespace so when starting with $, it doesn't get missed.
		format = " " + format;

		Pattern pattern = Pattern.compile( "[^\\\\](\\$\\([a-zA-Z0-9-_]+\\})" );
		Matcher matcher = pattern.matcher( format );

		for ( ; matcher.find(); )
		{
			String match = matcher.group( 1 );
			String key = match.substring( 2, match.length() - 1 );
			format = format.replace( match, data.getString( key ).orElse( "{null}" ) );

			matcher = pattern.matcher( format );
		}

		return Optional.of( format.substring( 1 ) );
	}

	public Parcels()
	{
		// Static Access
	}
}
