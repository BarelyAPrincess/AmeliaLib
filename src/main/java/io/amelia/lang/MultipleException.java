/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Used for when multiple exceptions were thrown
 */
public class MultipleException extends Exception
{
	private final List<ExceptionContext> exceptions = new ArrayList<>();

	public MultipleException( List<ExceptionContext> exceptions )
	{
		this.exceptions.addAll( exceptions );
	}

	public void addException( ExceptionContext exception )
	{
		exceptions.add( exception );
	}

	public Stream<ExceptionContext> getExceptions()
	{
		return exceptions.stream();
	}
}
