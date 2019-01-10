/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.plugins.events;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.data.parcel.ParcelSender;
import io.amelia.events.ApplicationEvent;
import io.amelia.events.Cancellable;
import io.amelia.foundation.EntityPrincipal;
import io.amelia.support.Lists;

/**
 * Fired when a system message will be delivered
 */
@Deprecated
public class MessageEvent extends ApplicationEvent implements Cancellable
{
	private final ParcelSender sender;
	private boolean cancelled = false;
	private List<Object> objs;
	private Set<ParcelReceiver> recipients;

	public MessageEvent( final ParcelSender sender, final Set<ParcelReceiver> recipients, final Object... objs )
	{
		this.sender = sender;
		this.recipients = recipients;
		this.objs = Arrays.asList( objs );
	}

	public void addMessage( Object obj )
	{
		objs.add( obj );
	}

	public void addRecipient( ParcelReceiver acct )
	{
		recipients.add( acct );
	}

	public boolean containsRecipient( EntityPrincipal entityPrincipal )
	{
		return recipients.stream().anyMatch( recipient -> recipient instanceof EntityPrincipal && ( ( EntityPrincipal ) recipient ).uuid().equals( entityPrincipal.uuid() ) );
	}

	public Collection<Object> getMessages()
	{
		return objs;
	}

	@SuppressWarnings( "unchecked" )
	public <T> List<T> getObjectMessages( Class<T> clz )
	{
		return objs.stream().filter( o -> o.getClass() == clz ).map( o -> ( T ) o ).collect( Collectors.toList() );
	}

	public Collection<ParcelReceiver> getRecipients()
	{
		return recipients;
	}

	public ParcelSender getSender()
	{
		return sender;
	}

	public List<String> getStringMessages()
	{
		return getObjectMessages( String.class );
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	public Object removeMessage( int index )
	{
		return objs.remove( index );
	}

	public boolean removeMessage( Object obj )
	{
		return objs.remove( obj );
	}

	public boolean removeRecipient( EntityPrincipal entityPrincipal )
	{
		Set<ParcelReceiver> recipientsCopy = new HashSet<>( recipients );
		recipients.stream().filter( recipient -> recipient instanceof EntityPrincipal && ( ( EntityPrincipal ) recipient ).uuid().equals( entityPrincipal.uuid() ) ).forEach( recipientsCopy::remove );
		int count = recipients.size();
		recipients = recipientsCopy;
		return recipients.size() != count;
	}

	@Override
	public void setCancelled( boolean cancel )
	{
		cancelled = cancel;
	}

	/**
	 * WARNING! This will completely clear and reset the messages.
	 *
	 * @param objs The new messages
	 */
	public void setMessages( Object... objs )
	{
		this.objs = Collections.singletonList( objs );
	}

	/**
	 * WARNING! This will completely clear and reset the messages.
	 *
	 * @param objs The new messages
	 */
	public void setMessages( Iterable<Object> objs )
	{
		this.objs = Lists.newArrayList( objs );
	}

	public void setRecipients( Set<ParcelReceiver> recipients )
	{
		this.recipients = recipients;
	}
}
