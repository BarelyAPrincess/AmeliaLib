/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import io.amelia.data.parcel.ParcelCarrier;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ParcelException;

public class MinimalApplication extends BaseApplication
{
	@Override
	public void fatalError( ExceptionReport report, boolean crashOnError )
	{

	}

	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{

	}

	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException.Error
	{

	}

	@Override
	protected void parse() throws Exception
	{

	}

	@Override
	public void sendToAll( ParcelCarrier parcel )
	{

	}
}
