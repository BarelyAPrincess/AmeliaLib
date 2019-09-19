/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.amelia.foundation.Foundation;
import io.amelia.permissions.event.PermissibleEntityEvent;

public abstract class PermissibleGroup extends PermissibleEntity implements Comparable<PermissibleGroup>
{
	private int rank = -1;
	private int weight = 0;

	public PermissibleGroup( UUID uuid, String groupName )
	{
		super( uuid, groupName );
	}

	@Override
	public final int compareTo( PermissibleGroup o )
	{
		return getWeight() - o.getWeight();
	}

	// TODO THIS!!! New Ref Groups
	public Map<String, Collection<PermissibleGroup>> getAllParentGroups()
	{
		return new HashMap<>();
		// return Collections.unmodifiableMap( groups );
	}

	// TODO Prevent StackOverflow
	public Stream<PermissibleEntity> getChildEntities( boolean recursive, References refs )
	{
		Supplier<Stream<PermissibleEntity>> supplier = () -> Foundation.getPermissions().getEntities().filter( entity -> entity.getGroups( refs ).anyMatch( group -> group == this ) );
		if ( recursive )
			return Stream.concat( supplier.get(), getChildGroups( true, refs ).flatMap( group -> group.getChildEntities( true, refs ) ) );
		else
			return supplier.get();
	}

	public Stream<PermissibleEntity> getChildEntities( References refs )
	{
		return getChildEntities( false, refs );
	}

	// TODO Prevent StackOverflow
	public Stream<PermissibleGroup> getChildGroups( boolean recursive, References refs )
	{
		Stream<PermissibleGroup> result = Foundation.getPermissions().getGroups().filter( entity -> entity.getGroups( refs ).anyMatch( group -> group == this ) );
		if ( recursive )
			result = Stream.concat( result, result.flatMap( group -> group.getChildGroups( true, refs ) ) );
		return result;
	}

	public Stream<PermissibleGroup> getChildGroups( References refs )
	{
		return getChildGroups( false, refs );
	}

	// XXX THIS TOO!
	public Map<String, String> getOptions()
	{
		return new HashMap<>();
	}

	public Stream<String> getParentGroupsNames( References refs )
	{
		return getGroups( refs ).map( PermissibleGroup::getName );
	}

	public int getRank()
	{
		return rank;
	}

	public String getRankLadder()
	{
		return null;// TODO Auto-generated method stub
	}

	public final int getWeight()
	{
		return weight;
	}

	public boolean isRanked()
	{
		return rank >= 0;
	}

	public void setDefault( boolean isDef )
	{
		// TODO Auto-generated method stub
	}

	public void setRank( int rank )
	{
		this.rank = rank;
	}

	public void setRankLadder( String rank )
	{
		// TODO Auto-generated method stub
	}

	public final void setWeight( int weight )
	{
		this.weight = weight;
		Foundation.getPermissions().callEvent( new PermissibleEntityEvent( this, PermissibleEntityEvent.Action.WEIGHT_CHANGED ) );
	}
}
