/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ContainerException;
import io.amelia.support.BiFunctionWithException;
import io.amelia.support.Maps;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.OptionalExt;
import io.amelia.support.Streams;

@SuppressWarnings( "unchecked" )
public abstract class ContainerBase<BaseClass extends ContainerBase<BaseClass, ExceptionClass>, ExceptionClass extends ApplicationException.Error>
{
	public static final int LISTENER_CHILD_ADD = 0x00;
	public static final int LISTENER_CHILD_REMOVE = 0x01;
	protected final List<BaseClass> children = new ArrayList<>();
	private final BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator;
	private final Map<Integer, ContainerListener.Container> listeners = new ConcurrentHashMap<>();
	protected EnumSet<ContainerWithValue.Flag> flags = EnumSet.noneOf( ContainerWithValue.Flag.class );
	protected BaseClass parent;
	protected ContainerOptions containerOptions = null;
	private String localName;

	protected ContainerBase( @Nonnull BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator, @Nonnull String localName ) throws ExceptionClass
	{
		this( creator, null, localName );
	}

	protected ContainerBase( @Nonnull BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator, BaseClass parent, @Nonnull String localName ) throws ExceptionClass
	{
		// TODO Upper and lower case is permitted, however, we should implement a filter that prevents duplicate keys with varying case, e.g., WORD vs. Word - would be the same key.
		if ( !localName.matches( "[a-zA-Z0-9*_]*" ) )
			throwException( String.format( "The local name '%s' can only contain characters a-z, A-Z, 0-9, *, and _.", localName ) );
		this.creator = creator;
		this.parent = parent;
		this.localName = localName;
	}

	public final int addChildAddListener( ContainerListener.OnChildAdd<BaseClass> function, ContainerListener.Flags... flags )
	{
		return addListener( new ContainerListener.Container( LISTENER_CHILD_ADD, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0] );
			}
		} );
	}

	public final int addChildRemoveListener( ContainerListener.OnChildRemove<BaseClass> function, ContainerListener.Flags... flags )
	{
		return addListener( new ContainerListener.Container( LISTENER_CHILD_REMOVE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( BaseClass ) objs[1] );
			}
		} );
	}

	public final BaseClass addFlag( Flag... flags )
	{
		disposalCheck();
		for ( Flag flag : flags )
		{
			if ( flag.equals( Flag.DISPOSED ) )
				throw new ContainerException( "The DISPOSED flag is reserved for internal use only." );
			this.flags.add( flag );
		}
		return ( BaseClass ) this;
	}

	protected final int addListener( ContainerListener.Container container )
	{
		return Maps.firstKeyAndPut( listeners, container );
	}

	public final <C> Stream<C> collect( Function<BaseClass, C> function )
	{
		disposalCheck();
		return Stream.concat( Stream.of( function.apply( ( BaseClass ) this ) ), children.stream().flatMap( c -> c.collect( function ) ) ).filter( Objects::nonNull );
	}

	@Nonnull
	private OptionalExt<BaseClass, ExceptionClass> createChild( String key )
	{
		disposalCheck();
		if ( hasFlag( Flag.READ_ONLY ) )
			return OptionalExt.withException( getException( getCurrentPath() + " is read only." ) );
		try
		{
			BaseClass child = creator.apply( ( BaseClass ) this, key );
			children.add( child );
			fireListener( LISTENER_CHILD_ADD, child );
			return OptionalExt.ofNeverNull( child );
		}
		catch ( Exception e )
		{
			return OptionalExt.withException( ( ExceptionClass ) e );
		}
	}

	protected void destroy() throws ExceptionClass, ExceptionClass
	{
		disposalCheck();
		notFlag( Flag.READ_ONLY );
		removeFromParent();
		for ( BaseClass child : children )
			child.destroy();
		children.clear();
		flags = EnumSet.of( Flag.DISPOSED );
	}

	public void destroyChild( String key ) throws ExceptionClass
	{
		if ( hasFlag( Flag.READ_ONLY ) )
			throwException( getCurrentPath() + " is read only." );
		getChild( key ).ifPresentThrowException( ContainerBase::destroy );
	}

	public OptionalExt<BaseClass, ExceptionClass> destroyChildAndCreate( String key )
	{
		if ( hasFlag( Flag.READ_ONLY ) )
			return OptionalExt.withException( getException( getCurrentPath() + " is read only." ) );
		getChild( key ).ifPresentWithException( ContainerBase::destroy );
		return createChild( key );
	}

	protected final void disposalCheck() throws ContainerException
	{
		if ( hasFlag( Flag.DISPOSED ) )
			throw new ContainerException( getCurrentPath() + " has been disposed." );
	}

	public final BaseClass empty( @Nonnull String key ) throws ExceptionClass
	{
		return creator.apply( null, key );
	}

	public final BaseClass empty() throws ExceptionClass
	{
		return empty( "" );
	}

	protected OptionalExt<BaseClass, ExceptionClass> findChild( @Nonnull String key, boolean create )
	{
		disposalCheck();
		Objs.notNull( key );

		if ( create && hasFlag( Flag.READ_ONLY ) )
			return OptionalExt.withException( getException( getCurrentPath() + " is read only." ) );

		Namespace ns = Namespace.parseString( key, getOptions().getSeparator() );
		if ( ns.getNodeCount() == 0 )
			return OptionalExt.ofNeverNull( ( BaseClass ) this );

		String first = ns.getFirst();
		OptionalExt<BaseClass, ExceptionClass> found = null;

		for ( BaseClass child : children )
			if ( child.getName() == null )
				children.remove( child );
			else if ( first.equalsIgnoreCase( child.getName() ) )
			{
				found = OptionalExt.ofNeverNull( child );
				break;
			}

		if ( found == null && !create )
			return OptionalExt.empty();
		if ( found == null )
			found = createChild( first );

		if ( ns.getNodeCount() <= 1 )
			return found;
		else
			return found.flatMap( ( child, exception ) -> child.findChild( ns.subString( 1 ), create ) );
	}

	final BaseClass findFlag( Flag flag )
	{
		disposalCheck();
		return ( BaseClass ) ( flags.contains( flag ) ? this : parent == null ? null : parent.findFlag( flag ) );
	}

	void fireListener( int type, Object... objs )
	{
		fireListener( true, type, objs );
	}

	void fireListener( boolean local, int type, Object... objs )
	{
		if ( hasParent() )
			parent.fireListener( false, type, objs );
		for ( Map.Entry<Integer, ContainerListener.Container> entry : listeners.entrySet() )
			if ( entry.getValue().type == type )
			{
				if ( entry.getValue().flags.contains( ContainerListener.Flags.FIRE_ONCE ) )
					listeners.remove( entry.getKey() );
				if ( local || !entry.getValue().flags.contains( ContainerListener.Flags.NO_RECURSIVE ) )
					Kernel.getExecutorParallel().execute( () -> entry.getValue().call( objs ) );
			}
	}

	public final OptionalExt<BaseClass, ExceptionClass> getChild( @Nonnull String key )
	{
		return findChild( key, false );
	}

	public final OptionalExt<BaseClass, ExceptionClass> getChild( @Nonnull TypeBase type )
	{
		return getChild( type.getPath() );
	}

	public final BaseClass getChildOrCreate( @Nonnull String key )
	{
		return findChild( key, true ).get();
	}

	public final BaseClass getChildOrCreate( @Nonnull TypeBase type )
	{
		return getChildOrCreate( type.getPath() );
	}

	public final Stream<BaseClass> getChildren()
	{
		disposalCheck();
		return children.stream();
	}

	public final Stream<BaseClass> getAllChildren()
	{
		disposalCheck();
		return Streams.recursive( ( BaseClass ) this, ContainerBase::getChildren );
	}

	public final String getCurrentPath()
	{
		return getNamespace().reverseOrder().getString();
	}

	public final String getDomainChild()
	{
		disposalCheck();
		return Namespace.parseDomain( getCurrentPath() ).getChild().getString();
	}

	public final String getDomainTLD()
	{
		disposalCheck();
		return Namespace.parseDomain( getCurrentPath() ).getTld().getString();
	}

	public Flag[] getFlags()
	{
		return flags.toArray( new Flag[0] );
	}

	public final Set<String> getKeys()
	{
		disposalCheck();
		return children.stream().map( ContainerBase::getName ).collect( Collectors.toSet() );
	}

	public final Set<String> getKeysDeep()
	{
		disposalCheck();
		return Stream.concat( getKeys().stream(), getChildren().flatMap( n -> n.getKeysDeep().stream().map( s -> n.getName() + "." + s ) ) ).sorted().collect( Collectors.toSet() );
	}

	/**
	 * Gets the name of this individual {@link BaseClass}, in the path.
	 *
	 * @return Name of this node
	 */
	public final String getName()
	{
		return localName;
	}

	public final Namespace getNamespace()
	{
		disposalCheck();
		if ( Objs.isEmpty( localName ) )
			return new Namespace();
		return hasParent() ? getParent().getNamespace().append( localName ) : Namespace.parseString( localName, getOptions().getSeparator() );
	}

	public final BaseClass getParent()
	{
		disposalCheck();
		return parent;
	}

	public final Stream<BaseClass> getParents()
	{
		disposalCheck();
		return Streams.transverse( parent, BaseClass::getParent );
	}

	public final BaseClass getRoot()
	{
		return parent == null ? ( BaseClass ) this : parent.getRoot();
	}

	protected ContainerOptions getOptions()
	{
		if ( parent != null )
			return parent.getOptions();
		if ( containerOptions == null )
			containerOptions = new ContainerOptions();
		return containerOptions;
	}

	public final boolean hasChild( String key )
	{
		return getChild( key ) != null;
	}

	public final boolean hasChildren()
	{
		return children.size() > 0;
	}

	protected final boolean hasFlag( Flag flag )
	{
		return flags.contains( flag ) || ( parent != null && !parent.hasFlag( Flag.NO_FLAG_RECURSION ) && parent.hasFlag( flag ) );
	}

	public final boolean hasParent()
	{
		return parent != null;
	}

	public final boolean isDisposed()
	{
		return hasFlag( Flag.DISPOSED );
	}

	/**
	 * Indicates that it is safe to remove this child from its parent as it contains no critical data.
	 * Exact implementation depends on the implementing subclass.
	 *
	 * @see ContainerWithValue#isTrimmable0()
	 * @see #trimChildren();
	 */
	public final boolean isTrimmable()
	{
		if ( !isTrimmable0() )
			return false;
		for ( BaseClass child : children )
			if ( !child.isTrimmable() )
				return false;
		return true;
	}

	protected abstract boolean isTrimmable0();

	public void copyChild( @Nonnull BaseClass child ) throws ExceptionClass
	{
		disposalCheck();
		notFlag( Flag.READ_ONLY );

		for ( BaseClass oldChild : child.children )
		{
			BaseClass newChild = getChild( oldChild.getName() ).orElse( null );
			if ( newChild == null )
				setChild( oldChild );
			else
			{
				notFlag( Flag.NO_OVERRIDE );
				newChild.notFlag( Flag.READ_ONLY );
				newChild.copyChild( oldChild );
			}
		}

		flags.addAll( child.flags );
	}

	public final BaseClass move( @Nonnull String targetPath ) throws ExceptionClass
	{
		Objs.notEmpty( targetPath );

		Namespace ns = Namespace.parseString( targetPath );

		BaseClass newParent = parent;

		for ( int i = 0; i < ns.getNodeCount(); i++ )
		{
			String node = ns.getNode( i );

			if ( Objs.isEmpty( node ) )
			{
				if ( i == 0 ) // First node is empty, so remove from parent.
					newParent = null;
				// Otherwise, do nothing.
			}
			else if ( node.equals( ".." ) )
			{
				if ( hasParent() ) // We have a parent, so shift us to the parent of the parent.
					newParent = parent.parent;
			}
			else
			{
				if ( i == ns.getNodeCount() - 1 ) // We're on the last node, so set this to our new localName.
					this.localName = node;
				else
					newParent = newParent == null ? empty( node ) : newParent.getChildOrCreate( node ); // Shift us one child down
			}
		}

		if ( newParent != parent )
		{
			removeFromParent();
			if ( newParent != null )
				newParent.setChild( ( BaseClass ) this );
		}

		return ( BaseClass ) this;
	}

	public void notFlag( Flag flag ) throws ExceptionClass
	{
		if ( hasFlag( flag ) )
			throwException( getCurrentPath() + " has " + flag.name() + " flag." );
	}

	/**
	 * Polls a child from this stacker. If it exists, it's removed from it's parent, i.e., isolated on its own.
	 *
	 * @param key The child key
	 *
	 * @return found instance or null if does not exist.
	 */
	public OptionalExt<BaseClass, ExceptionClass> pollChild( String key )
	{
		return getChild( key ).map( ContainerBase::removeFromParent );
	}

	public final void removeAllListeners()
	{
		listeners.clear();
	}

	public final BaseClass removeFlag( Flag... flags )
	{
		disposalCheck();
		this.flags.removeAll( Arrays.asList( flags ) );
		return ( BaseClass ) this;
	}

	public final BaseClass removeFlagRecursive( Flag... flags )
	{
		disposalCheck();
		if ( parent != null )
			parent.removeFlagRecursive( flags );
		return removeFlag( flags );
	}

	public final BaseClass removeFromParent() throws ExceptionClass
	{
		if ( hasParent() )
		{
			parent.notFlag( Flag.READ_ONLY );
			parent.children.remove( this );
			fireListener( LISTENER_CHILD_REMOVE, parent, this );
			parent = null;
		}
		return ( BaseClass ) this;
	}

	public final void removeListener( int inx )
	{
		listeners.remove( inx );
	}

	public final void setChild( @Nonnull BaseClass child ) throws ExceptionClass
	{
		setChild( child, false );
	}

	public final void setChild( @Nonnull BaseClass child, boolean mergeIfExists ) throws ExceptionClass
	{
		disposalCheck();
		notFlag( Flag.READ_ONLY );

		if ( children.contains( child ) )
			return;

		BaseClass current = findChild( child.getName(), false ).orElse( null );
		if ( current != null )
		{
			notFlag( Flag.NO_OVERRIDE );
			if ( mergeIfExists )
			{
				current.copyChild( child );
				return;
			}
			else
				current.destroy();
		}

		child.removeFromParent();

		child.parent = ( BaseClass ) this;
		child.containerOptions = null;
		children.add( child );
	}

	public final void moveChild( @Nonnull BaseClass child, @Nonnull String targetKey ) throws ExceptionClass
	{
		moveChild( child, targetKey, false );
	}

	public final void moveChild( @Nonnull BaseClass child, @Nonnull String targetKey, boolean mergeIfExists ) throws ExceptionClass
	{
		disposalCheck();
		notFlag( Flag.READ_ONLY );

		BaseClass current = findChild( targetKey, false ).orElse( null );
		if ( current != null )
		{
			// Child already exists at the targeted location.
			if ( current == child )
				return;

			notFlag( Flag.NO_OVERRIDE );
			if ( mergeIfExists )
			{
				current.copyChild( child );
				return;
			}
			else
				current.destroy();
		}

		createChild( targetKey ).ifPresentThrowException( element -> element.copyChild( child ) );
		child.destroy();
	}

	public int getChildCount()
	{
		return children.size();
	}

	public int getChildCountOf( String key )
	{
		return getChild( key ).map( ContainerBase::getChildCount ).orElse( -1 );
	}

	protected abstract ExceptionClass getException( String message );

	protected final void throwException( String message ) throws ExceptionClass
	{
		throw getException( message );
	}

	/**
	 * Attempts to remove each sub-node based on if {@link #isTrimmable()} returns true.
	 */
	public void trimChildren() throws ExceptionClass
	{
		for ( BaseClass child : children )
			if ( child.isTrimmable() )
				child.destroy();
			else
				child.trimChildren();
	}

	public enum Flag
	{
		/* Values and children can never be written to this object */
		READ_ONLY,
		/* This object will be ignored, if there is an attempt to write it to persistent disk */
		NO_SAVE,
		/* Prevents the overwriting of existing children and values */
		NO_OVERRIDE,
		/* Prevents flags from recurring to children */
		NO_FLAG_RECURSION,
		/* SPECIAL FLAG */
		DISPOSED
	}

	public class ContainerOptions
	{
		private String separator = ".";

		public String getSeparator()
		{
			return separator;
		}

		public void setSeparator( String separator )
		{
			this.separator = separator;
		}
	}
}
