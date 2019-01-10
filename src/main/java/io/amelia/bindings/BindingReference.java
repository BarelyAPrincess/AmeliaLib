/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.bindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.support.Lists;
import io.amelia.support.Objs;

class BindingReference<S>
{
	final Class<S> objClass;
	final List<S> objInstances = new ArrayList<>();
	Supplier<S> objSupplier = null;

	BindingReference( @Nonnull S objInstance )
	{
		this.objClass = ( Class<S> ) objInstance.getClass();
		this.objInstances.add( objInstance );
	}

	BindingReference( @Nonnull List<S> objInstances )
	{
		Objs.notEmpty( objInstances );
		this.objClass = ( Class<S> ) Lists.first( objInstances ).get().getClass();
		this.objInstances.addAll( objInstances );
	}

	BindingReference( @Nonnull Class<S> objClass, @Nonnull Supplier<S> objSupplier )
	{
		this.objClass = objClass;
		this.objSupplier = objSupplier;
	}

	public void addInstance( S instance )
	{
		objInstances.add( instance );
	}

	public void clear()
	{
		objInstances.clear();
	}

	public Stream<S> getInstances()
	{
		if ( objInstances.isEmpty() && objSupplier != null )
			objInstances.add( objSupplier.get() );
		return objInstances.stream();
	}

	public Class<S> getObjClass()
	{
		return objClass;
	}
}
