/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.Map;

/**
 * Represents a pair with a Key and Value Object
 */
public class Pair<LV, RV> implements Map.Entry<LV, RV>
{
	private LV leftValue;
	private RV rightValue;

	public Pair()
	{
		this.leftValue = null;
		this.rightValue = null;
	}

	public Pair( Map.Entry<LV, RV> entry )
	{
		this.leftValue = entry.getKey();
		this.rightValue = entry.getValue();
	}

	public Pair( LV leftValue, RV rightValue )
	{
		this.leftValue = leftValue;
		this.rightValue = rightValue;
	}

	@Override
	public LV getKey()
	{
		return leftValue;
	}

	public LV getLeft()
	{
		return leftValue;
	}

	public RV getRight()
	{
		return rightValue;
	}

	@Override
	public RV getValue()
	{
		return rightValue;
	}

	public LV setKey( LV leftValue )
	{
		LV old = this.leftValue;
		this.leftValue = leftValue;
		return old;
	}

	public LV setLeft( LV leftValue )
	{
		return setKey( leftValue );
	}

	public RV setRight( RV rightValue )
	{
		return setValue( rightValue );
	}

	@Override
	public RV setValue( RV rightValue )
	{
		RV old = this.rightValue;
		this.rightValue = rightValue;
		return old;
	}
}
