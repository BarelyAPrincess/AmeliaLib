/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ContainerException;
import io.amelia.support.BiFunctionWithException;
import io.amelia.support.ConsumerWithException;
import io.amelia.support.Maps;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.Streams;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryWithCause;

@SuppressWarnings( "unchecked" )
public abstract class ContainerBase<BaseClass extends ContainerBase<BaseClass, ExceptionClass>, ExceptionClass extends ApplicationException.Error>
{
	public static final int LISTENER_CHILD_ADD_BEFORE = 0x00;
	public static final int LISTENER_CHILD_ADD_AFTER = 0x01;
	public static final int LISTENER_CHILD_REMOVE_BEFORE = 0x02;
	public static final int LISTENER_CHILD_REMOVE_AFTER = 0x03;
	protected final List<BaseClass> children = new ArrayList<>();
	private final BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator;
	private final Map<Integer, ContainerListener.Container> listeners = new ConcurrentHashMap<>();
	protected ContainerOptions containerOptions = null;
	protected BitSet flags = new BitSet(); // We use BitSet so extending classes can implement their own special flags.
	protected BaseClass parent;
	private String localName;

	protected ContainerBase( @Nonnull BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator )
	{
		this.creator = creator;
		this.parent = null;
		this.localName = "";
	}

	protected ContainerBase( @Nonnull BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator, @Nonnull String localName ) throws ExceptionClass
	{
		this( creator, null, localName );
	}

	protected ContainerBase( @Nonnull BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator, @Nullable BaseClass parent, @Nonnull String localName ) throws ExceptionClass
	{
		// TODO Should root entries be forced to be nameless or should this only apply for special cases?
		// if ( parent == null && localName.length() != 0 )
		//	throwException( "Root must remain nameless." );
		// TODO Upper and lower case is permitted, however, we should implement a filter that prevents duplicate keys with varying case, e.g., WORD vs. Word - would be the same key.
		if ( !localName.matches( "[a-zA-Z0-9*_]*" ) )
			throwException( String.format( "The local name '%s' can only contain characters a-z, A-Z, 0-9, *, and _.", localName ) );
		this.creator = creator;
		this.parent = parent;
		this.localName = localName;
	}

	public final int addChildAddAfterListener( ContainerListener.OnChildAdd<BaseClass> function, ContainerListener.Flags... flags )
	{
		return addListener( new ContainerListener.Container( LISTENER_CHILD_ADD_AFTER, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0] );
			}
		} );
	}

	public final int addChildAddBeforeListener( ContainerListener.OnChildAdd<BaseClass> function, ContainerListener.Flags... flags )
	{
		return addListener( new ContainerListener.Container( LISTENER_CHILD_ADD_BEFORE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0] );
			}
		} );
	}

	public final int addChildRemoveAfterListener( ContainerListener.OnChildRemove<BaseClass> function, ContainerListener.Flags... flags )
	{
		return addListener( new ContainerListener.Container( LISTENER_CHILD_REMOVE_AFTER, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( BaseClass ) objs[1] );
			}
		} );
	}

	public final int addChildRemoveBeforeListener( ContainerListener.OnChildRemove<BaseClass> function, ContainerListener.Flags... flags )
	{
		return addListener( new ContainerListener.Container( LISTENER_CHILD_REMOVE_BEFORE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( BaseClass ) objs[1] );
			}
		} );
	}

	public final BaseClass addFlag( int... flags )
	{
		disposalCheck();
		for ( int flag : flags )
		{
			if ( flag == Flags.DISPOSED )
				throw new ContainerException( "The DISPOSED flag is reserved for internal use only." );
			this.flags.set( flag );
		}
		return ( BaseClass ) this;
	}

	protected final int addListener( ContainerListener.Container container )
	{
		return Maps.firstKeyAndPut( listeners, container );
	}

	protected void callParentRecursive( ConsumerWithException<BaseClass, ExceptionClass> callback ) throws ExceptionClass
	{
		callback.accept( ( BaseClass ) this );
		if ( parent != null )
			parent.callParentRecursive( callback );
	}

	protected void callRecursive( ConsumerWithException<BaseClass, ExceptionClass> callback ) throws ExceptionClass
	{
		callback.accept( ( BaseClass ) this );
		Streams.forEachWithException( getChildren(), child -> child.callRecursive( callback ) );
	}

	protected void canCreateChild( BaseClass node, String key ) throws ExceptionClass
	{
		// Always Permitted
	}

	public final <C> Stream<C> collect( Function<BaseClass, C> function )
	{
		disposalCheck();
		return Stream.concat( Stream.of( function.apply( ( BaseClass ) this ) ), children.stream().flatMap( c -> c.collect( function ) ) ).filter( Objects::nonNull );
	}

	public void copyChild( @Nonnull BaseClass child ) throws ExceptionClass
	{
		disposalCheck();
		notFlag( Flags.READ_ONLY );

		for ( BaseClass oldChild : child.children )
		{
			BaseClass newChild = getChildVoluntary( oldChild.getName() ).orElse( null );
			if ( newChild == null )
				setChild( oldChild );
			else
			{
				notFlag( Flags.NO_OVERRIDE );
				newChild.notFlag( Flags.READ_ONLY );
				newChild.copyChild( oldChild );
			}
		}

		flags = ( BitSet ) child.flags.clone();
	}

	@Nonnull
	protected VoluntaryWithCause<BaseClass, ExceptionClass> createChild( @Nonnull String key )
	{
		disposalCheck();
		if ( hasFlag( Flags.READ_ONLY ) )
			return VoluntaryWithCause.withException( getException( getCurrentPath() + " is read only.", null ) );
		try
		{
			BaseClass child = creator.apply( ( BaseClass ) this, key );
			callParentRecursive( container -> container.canCreateChild( ( BaseClass ) this, key ) );
			fireListenerWithException( LISTENER_CHILD_ADD_BEFORE, child );
			children.add( child );
			fireListener( LISTENER_CHILD_ADD_AFTER, child );
			return VoluntaryWithCause.ofWithCause( child );
		}
		catch ( Exception e )
		{
			return VoluntaryWithCause.withException( ( ExceptionClass ) e );
		}
	}

	protected void destroy() throws ExceptionClass
	{
		disposalCheck();
		notFlag( Flags.READ_ONLY );
		removeFromParent();
		for ( BaseClass child : children )
			child.destroy();
		children.clear();
		flags.clear();
		flags.set( Flags.DISPOSED );
		setDirty( true );
	}

	public void destroyChild( String key ) throws ExceptionClass
	{
		if ( hasFlag( Flags.READ_ONLY ) )
			throwException( getCurrentPath() + " is read only." );
		getChildVoluntary( key ).ifPresent( ContainerBase::destroy );
	}

	public VoluntaryWithCause<BaseClass, ExceptionClass> destroyChildAndCreate( String key )
	{
		if ( hasFlag( Flags.READ_ONLY ) )
			return Voluntary.withException( getException( getCurrentPath() + " is read only.", null ) );
		return getChildVoluntary( key ).ifPresentCatchException( ContainerBase::destroy ).hasNotErroredFlat( value -> createChild( key ) );
	}

	public void destroyChildren() throws ExceptionClass
	{
		Streams.forEachWithException( getChildren(), ContainerBase::destroy );
	}

	protected final void disposalCheck() throws ContainerException
	{
		if ( hasFlag( Flags.DISPOSED ) )
			throw new ContainerException( getCurrentPath() + " has been disposed." );
	}

	/**
	 * Makes a clone of this container with the exception of skipping the parent, you'll manually add the parent if this was recursive.
	 */
	public BaseClass duplicate()
	{
		try
		{
			BaseClass clone = creator.apply( null, localName );

			listeners.values().forEach( clone::addListener );
			clone.flags = flags;
			clone.parent = null;
			clone.containerOptions = containerOptions;

			for ( BaseClass child : children )
				clone.setChild( child.duplicate() );

			return clone;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	protected VoluntaryWithCause<BaseClass, ExceptionClass> findChild( @Nonnull String key, boolean create )
	{
		disposalCheck();
		Objs.notNull( key );

		if ( create && hasFlag( Flags.READ_ONLY ) )
			return Voluntary.withException( getException( getCurrentPath() + " is read only.", null ) );

		Namespace ns = Namespace.of( key, getOptions().getSeparator() );
		if ( ns.getNodeCount() == 0 )
			return VoluntaryWithCause.ofWithCause( ( BaseClass ) this );

		String first = ns.getStringFirst();
		VoluntaryWithCause<BaseClass, ExceptionClass> found = null;

		for ( BaseClass child : children )
			if ( child.getName() == null )
				children.remove( child );
			else if ( first.equalsIgnoreCase( child.getName() ) )
			{
				found = VoluntaryWithCause.ofWithCause( child );
				break;
			}

		if ( found == null && !create )
			return VoluntaryWithCause.emptyWithCause();
		if ( found == null )
			found = createChild( first );

		if ( ns.getNodeCount() <= 1 )
			return found;
		else
			return found.flatMapWithCause( child -> child.findChild( ns.subString( 1 ), create ) );
	}

	final BaseClass findFlag( int flag )
	{
		disposalCheck();
		return ( BaseClass ) ( flags.get( flag ) ? this : parent == null ? null : parent.findFlag( flag ) );
	}

	void fireListener( int type, Object... objs )
	{
		try
		{
			fireListenerWithException( true, type, objs );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	void fireListenerWithException( int type, Object... objs ) throws ExceptionClass
	{
		fireListenerWithException( true, type, objs );
	}

	void fireListenerWithException( boolean local, int type, Object... objs ) throws ExceptionClass
	{
		if ( hasParent() )
			parent.fireListenerWithException( false, type, objs );
		for ( Map.Entry<Integer, ContainerListener.Container> entry : listeners.entrySet() )
			if ( entry.getValue().type == type )
			{
				if ( entry.getValue().flags.contains( ContainerListener.Flags.FIRE_ONCE ) )
					listeners.remove( entry.getKey() );
				if ( local || !entry.getValue().flags.contains( ContainerListener.Flags.NO_RECURSIVE ) )
					if ( entry.getValue().flags.contains( ContainerListener.Flags.SYNCHRONIZED ) )
					{
						try
						{
							entry.getValue().call( objs );
						}
						catch ( Exception e )
						{
							throw getException( "Exception thrown by listener", e );
						}
					}
					else
						Kernel.getExecutorParallel().execute( () -> {
							try
							{
								entry.getValue().call( objs );
							}
							catch ( Exception e )
							{
								e.printStackTrace();
							}
						} );
			}
	}

	public final Stream<BaseClass> getAllChildren()
	{
		disposalCheck();
		return Streams.recursive( ( BaseClass ) this, ContainerBase::getChildren );
	}

	public final BaseClass getChild( @Nonnull String key ) throws NoSuchElementException
	{
		return findChild( key, false ).orElseThrow( NoSuchElementException::new );
	}

	public final BaseClass getChild( @Nonnull TypeBase type ) throws NoSuchElementException
	{
		return getChildOrCreate( type.getPath() );
	}

	public int getChildCount()
	{
		return children.size();
	}

	public int getChildCountOf( String key )
	{
		return getChildVoluntary( key ).map( ContainerBase::getChildCount ).orElse( -1 );
	}

	public final BaseClass getChildOrCreate( @Nonnull String key )
	{
		return findChild( key, true ).get();
	}

	public final BaseClass getChildOrCreate( @Nonnull TypeBase type )
	{
		return getChildOrCreate( type.getPath() );
	}

	public final VoluntaryWithCause<BaseClass, ExceptionClass> getChildVoluntary( @Nonnull String key )
	{
		return findChild( key, false );
	}

	public final Stream<BaseClass> getChildren()
	{
		disposalCheck();
		return children.stream();
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

	protected abstract ExceptionClass getException( @Nonnull String message, @Nullable Exception exception );

	public BitSet getFlags()
	{
		return flags;
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
		return hasParent() ? getParent().getNamespace().append( localName ) : Namespace.of( localName, getOptions().getSeparator() );
	}

	/**
	 * Protected or public?
	 */
	public ContainerOptions getOptions()
	{
		if ( parent != null )
			return parent.getOptions();
		if ( containerOptions == null )
			containerOptions = new ContainerOptions();
		return containerOptions;
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

	public final boolean hasChild( String key )
	{
		return getChild( key ) != null;
	}

	public final boolean hasChildren()
	{
		return children.size() > 0;
	}

	protected final boolean hasFlag( int flag )
	{
		return flags.get( flag ) || ( parent != null && !parent.hasFlag( Flags.NO_FLAG_RECURSION ) && parent.hasFlag( flag ) );
	}

	public final boolean hasParent()
	{
		return parent != null;
	}

	public boolean isDirty()
	{
		return hasFlag( Flags.DIRTY );
	}

	public final boolean isDisposed()
	{
		return hasFlag( Flags.DISPOSED );
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

	public final BaseClass move( @Nonnull String targetPath ) throws ExceptionClass
	{
		Objs.notEmpty( targetPath );

		Namespace ns = Namespace.of( targetPath );

		BaseClass newParent = parent;

		for ( int i = 0; i < ns.getNodeCount(); i++ )
		{
			String node = ns.getStringNode( i );

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
					newParent = newParent == null ? creator.apply( null, node ) : newParent.getChildOrCreate( node ); // Shift us one child down
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

	public final void moveChild( @Nonnull BaseClass child, @Nonnull String targetKey ) throws ExceptionClass
	{
		moveChild( child, targetKey, false );
	}

	public final void moveChild( @Nonnull BaseClass child, @Nonnull String targetKey, boolean mergeIfExists ) throws ExceptionClass
	{
		disposalCheck();
		notFlag( Flags.READ_ONLY );

		BaseClass current = findChild( targetKey, false ).orElse( null );
		if ( current != null )
		{
			// Child already exists at the targeted location.
			if ( current == child )
				return;

			notFlag( Flags.NO_OVERRIDE );
			if ( mergeIfExists )
			{
				current.copyChild( child );
				return;
			}
			else
				current.destroy();
		}

		createChild( targetKey ).ifPresent( element -> element.copyChild( child ) );
		child.destroy();
	}

	public void notFlag( int flag ) throws ExceptionClass
	{
		if ( hasFlag( flag ) )
			throwException( getCurrentPath() + " has " + flag + " flag." );
	}

	/**
	 * Polls a child from this stacker. If it exists, it's removed from it's parent, i.e., isolated on its own.
	 *
	 * @param key The child key
	 *
	 * @return found instance or null if does not exist.
	 */
	public VoluntaryWithCause<BaseClass, ExceptionClass> pollChild( String key )
	{
		return getChildVoluntary( key ).ifPresentCatchException( ContainerBase::removeFromParent );
	}

	public final void removeAllListeners()
	{
		listeners.clear();
	}

	public final BaseClass removeFlag( int... flags )
	{
		disposalCheck();
		for ( int flag : flags )
			this.flags.set( flag, false );
		return ( BaseClass ) this;
	}

	public final BaseClass removeFlagRecursive( int... flags )
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
			parent.notFlag( Flags.READ_ONLY );
			fireListenerWithException( LISTENER_CHILD_REMOVE_BEFORE, parent, this );
			parent.children.remove( this );
			fireListener( LISTENER_CHILD_REMOVE_AFTER, parent, this );
			parent = null;
			setDirty( true );
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
		notFlag( Flags.READ_ONLY );

		if ( children.contains( child ) )
			return;

		BaseClass current = findChild( child.getName(), false ).orElse( null );
		if ( current != null )
		{
			notFlag( Flags.NO_OVERRIDE );
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
		setDirty( true );
	}

	public void setDirty( boolean dirty )
	{
		if ( dirty )
			addFlag( Flags.DIRTY );
		else
			removeFlag( Flags.DIRTY );
	}

	public void setName( String localName )
	{
		this.localName = localName;
	}

	protected final void throwException( String message ) throws ExceptionClass
	{
		throw getException( message, null );
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

	public static class Flags
	{
		// Helps guarantee there will be no flag collisions.
		private static volatile AtomicInteger nextFlag = new AtomicInteger( 0 );
		// Values and children can never be written to this object
		public static final int READ_ONLY = getNextFlag();
		// This object will be ignored if there is an attempt to write it to persistent disk
		public static final int NO_SAVE = getNextFlag();
		// Prevents the overwriting of existing children and values
		public static final int NO_OVERRIDE = getNextFlag();
		// Prevents flags from recurring to children
		public static final int NO_FLAG_RECURSION = getNextFlag();
		// SPECIAL FLAG - DO NOT USE
		public static final int DISPOSED = getNextFlag();
		// TODO Indicates this ContainerBase was modified by a method call. This flag has to explicitly be removed to do proper checks.
		public static final int DIRTY = getNextFlag();

		protected static int getLastFlag()
		{
			return nextFlag.get();
		}

		protected static int getNextFlag()
		{
			return nextFlag.incrementAndGet();
		}

		Flags()
		{
			// Static Access
		}
	}

	public class ContainerOptions
	{
		private String separator = ".";

		public String getSeparator()
		{
			return separator;
		}

		public String getSeparatorReplacement()
		{
			return "_".equals( separator ) ? "-" : "_";
		}

		public void setSeparator( String separator )
		{
			this.separator = separator;
		}
	}
}
