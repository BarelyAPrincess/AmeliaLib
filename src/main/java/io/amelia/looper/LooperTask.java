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

@FunctionalInterface
public interface LooperTask<E extends Exception>
{
	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @throws E if unable to compute a result
	 */
	void execute() throws E;
}
