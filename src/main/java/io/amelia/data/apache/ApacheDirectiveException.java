/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.apache;

public class ApacheDirectiveException extends Exception
{
	private final ApacheDirective directive;

	public ApacheDirectiveException( String reason )
	{
		super( reason );
		directive = null;
	}

	public ApacheDirectiveException( String reason, ApacheDirective directive )
	{
		super( reason );
		this.directive = directive;
	}

	public int getLineNumber()
	{
		return directive != null ? directive.lineNum : -1;
	}

	public String getSource()
	{
		return directive != null ? directive.source : null;
	}
}
