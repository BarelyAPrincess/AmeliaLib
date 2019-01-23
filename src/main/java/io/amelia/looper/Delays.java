/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.looper;

import io.amelia.support.DateAndTime;

/**
 * Provides delay constants for the Looper feature
 *
 * Ticks values are only estimates, if the server is running slow or, GOD forbid, fast, the exact delays could vary.
 */
public class Delays
{
	// Milliseconds
	public static final long MILLIS_50 = 50;
	public static final long MILLIS_100 = 100;
	public static final long MILLIS_150 = 150;
	public static final long MILLIS_200 = 200;
	public static final long MILLIS_250 = 250;
	public static final long MILLIS_300 = 300;
	public static final long MILLIS_400 = 400;
	public static final long MILLIS_500 = 500;
	public static final long MILLIS_600 = 600;
	public static final long MILLIS_700 = 700;
	public static final long MILLIS_750 = 750;
	public static final long MILLIS_800 = 800;
	public static final long MILLIS_900 = 900;

	// Seconds
	public static final long SECOND = 1000;
	public static final long SECOND_2 = SECOND * 2;
	public static final long SECOND_2_5 = 50;
	public static final long SECOND_5 = SECOND * 5;
	public static final long SECOND_10 = SECOND * 10;
	public static final long SECOND_15 = SECOND * 15;
	public static final long SECOND_30 = SECOND * 30;
	public static final long SECOND_45 = SECOND * 45;

	// Minutes
	public static final long MINUTE = SECOND * 60;
	public static final long MINUTE_2 = MINUTE * 2;
	public static final long MINUTE_3 = MINUTE * 3;
	public static final long MINUTE_5 = MINUTE * 5;
	public static final long MINUTE_10 = MINUTE * 10;
	public static final long MINUTE_15 = MINUTE * 15;
	public static final long MINUTE_30 = MINUTE * 30;
	public static final long MINUTE_45 = MINUTE * 45;

	// Hours
	public static final long HOUR = MINUTE * 60;
	public static final long HOUR_2 = HOUR * 2;
	public static final long HOUR_3 = HOUR * 3;
	public static final long HOUR_4 = HOUR * 4;
	public static final long HOUR_5 = HOUR * 5;
	public static final long HOUR_6 = HOUR * 6;
	public static final long HOUR_8 = HOUR * 8;
	public static final long HOUR_10 = HOUR * 10;
	public static final long HOUR_12 = HOUR * 12;
	public static final long HOUR_14 = HOUR * 14;
	public static final long HOUR_16 = HOUR * 16;
	public static final long HOUR_18 = HOUR * 18;
	public static final long HOUR_20 = HOUR * 20;
	public static final long HOUR_22 = HOUR * 22;
	public static final long HOUR_24 = HOUR * 24;

	public static long ticks()
	{
		return Math.floorDiv( DateAndTime.epoch(), 20 );
	}
}
