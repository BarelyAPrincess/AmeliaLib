/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.looper;

import io.amelia.looper.queue.EntryAbstract;

@FunctionalInterface
public interface LooperTask<E extends Exception>
{
	/**
	 * Computes a result, or throws an exception if unable to do so.
	 *
	 * @param entry The EntryAbstract that contained this LooperTask
	 *
	 * @throws E if unable to compute a result
	 */
	void execute( EntryAbstract entry ) throws E;
}
