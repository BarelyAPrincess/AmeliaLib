/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
import io.amelia.support.NodeStack;
import io.amelia.support.Objs;
import io.amelia.support.Streams;
import io.amelia.support.Voluntary;

public abstract class ContainerBase<BaseClass extends ContainerBase<BaseClass, ExceptionClass>, ExceptionClass extends ApplicationException.Error>
{
	public static final int LISTENER_CHILD_ADD_BEFORE = 0x00;
	public static final int LISTENER_CHILD_ADD_AFTER = 0x01;
	public static final int LISTENER_CHILD_REMOVE_BEFORE = 0x02;
	public static final int LISTENER_CHILD_REMOVE_AFTER = 0x03;
	protected final List<BaseClass> children = new CopyOnWriteArrayList<>();
	private final BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator;
	private final Map<Integer, ContainerListener.Container> listeners = new ConcurrentHashMap<>();
	protected ContainerOptions containerOptions = null;
	protected BitSet flags = new BitSet(); // We use BitSet so extending classes can implement their own special flags.
	protected BaseClass parent;
	@Nonnull
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
		// TODO Upper and lower case is permitted, however, we should implement a filter that prevents duplicate keys with varying case, e.g., WORD vs. Word - would be the same.
		if ( localName.length() > 0 && !isValidateName( localName ) ) // Allow empty names - for now.
			throwException( String.format( "The local name '%s' must match A-Z, a-z, 0-9, asterisk, underline, and period.", localName ) );
		this.creator = creator;
		this.parent = parent;
		this.localName = localName;
	}

	public final BaseClass addFlag( int... flags )
	{
		notDisposed();
		for ( int flag : flags )
		{
			if ( flag == Flags.DISPOSED )
				throw new ContainerException( "The DISPOSED flag is reserved for internal use only." );
			this.flags.set( flag );
		}
		return ( BaseClass ) this;
	}

	protected <Cause extends Exception> void callParentRecursive( ConsumerWithException<BaseClass, Cause> callback ) throws Cause
	{
		callback.accept( ( BaseClass ) this );
		if ( parent != null )
			parent.callParentRecursive( callback );
	}

	protected <Cause extends Exception> void callRecursive( ConsumerWithException<BaseClass, Cause> callback ) throws Cause
	{
		callback.accept( ( BaseClass ) this );
		Streams.forEachWithException( getChildren(), child -> child.callRecursive( callback ) );
	}

	protected void canCreateChild( BaseClass node, String key ) throws ExceptionClass
	{
		// Always Permitted
	}

	public final void addChild( @Nullable String newChildName, @Nonnull BaseClass child )
	{
		addChild( newChildName, child, ConflictStrategy.IGNORE );
	}

	public final void addChild( @Nullable String newChildName, @Nonnull BaseClass child, @Nonnull ConflictStrategy conflictStrategy )
	{
		notDisposed();
		notReadOnly();

		if ( conflictStrategy == ConflictStrategy.CLEAR )
			getChildren().forEach( ContainerBase::destroy );

		if ( newChildName == null || newChildName.length() == 0 )
			newChildName = child.getLocalName();
		if ( !isValidateName( newChildName ) )
			throw new ContainerException( "New child name must not be empty and contain only A-Z, a-z, 0-9, asterisk, underline, and period." );

		if ( children.contains( child ) )
		{
			child.setLocalName( newChildName );
			return;
		}

		Voluntary<BaseClass> existing = childFind( Namespace.of( newChildName ) );
		if ( existing.isPresent() )
			if ( conflictStrategy == ConflictStrategy.IGNORE )
				return;
			else if ( conflictStrategy == ConflictStrategy.OVERWRITE )
			{
				existing.get().notFlag( Flags.NO_OVERRIDE );
				existing.get().destroy();
			}
			else if ( conflictStrategy == ConflictStrategy.MERGE )
			{
				existing.get().merge( child );
				child.removeFromParent(); // Just because it might be expected by the implementation.
				return;
			}
		// else continue

		// Remove from parent
		child.removeFromParent();

		// Update node name
		if ( !newChildName.equals( child.getLocalName() ) )
			child.setLocalName( newChildName );

		// Add to this node
		child.parent = ( BaseClass ) this;
		child.containerOptions = null;
		children.add( child );
		setDirty( true );
	}

	@Nonnull
	protected Voluntary<BaseClass> childCreate( @Nonnull String key )
	{
		try
		{
			return Voluntary.of( childCreateWithException( key ) );
		}
		catch ( Exception exceptionClass )
		{
			exceptionClass.printStackTrace();
			return Voluntary.empty();
		}
	}

	@Nonnull
	protected BaseClass childCreateWithException( @Nonnull String key ) throws ExceptionClass
	{
		notDisposed();
		notReadOnly();
		BaseClass child = creator.apply( ( BaseClass ) this, key );
		callParentRecursive( container -> container.canCreateChild( ( BaseClass ) this, key ) );
		listenerFireWithException( LISTENER_CHILD_ADD_BEFORE, child );
		children.add( child );
		listenerFire( LISTENER_CHILD_ADD_AFTER, child );
		return child;
	}

	public void childDestroy( String key )
	{
		notReadOnly();
		getChildVoluntary( key ).ifPresent( ContainerBase::destroy );
	}

	public final void childDestroyAndAdd( @Nullable String newChildName, @Nonnull BaseClass child, @Nonnull ConflictStrategy conflictStrategy )
	{
		if ( newChildName == null || newChildName.length() == 0 )
			newChildName = child.getLocalName();
		if ( !isValidateName( newChildName ) )
			throw new ContainerException( "New child name must not be empty and contain only A-Z, a-z, 0-9, asterisk, underline, and period." );

		childFind( Namespace.of( newChildName ) ).ifPresent( ContainerBase::destroy );
		addChild( newChildName, child, conflictStrategy );
	}

	public Voluntary<BaseClass> childDestroyAndCreate( String key ) throws ExceptionClass
	{
		notReadOnly();
		getChildVoluntary( key ).ifPresent( ContainerBase::destroy );
		return childCreate( key );
	}

	protected Voluntary<BaseClass> childFind( @Nonnull String childPath )
	{
		return childFind( Namespace.of( childPath ) );
	}

	protected Voluntary<BaseClass> childFind( @Nonnull NodeStack childPath )
	{
		notDisposed();
		if ( childPath.getNodeCount() == 0 )
			return Voluntary.of( ( BaseClass ) this );

		String childName = childPath.getStringFirst();
		return children.stream().filter( child -> childName.equals( child.getLocalName() ) ).findFirst().map( child -> child.childFind( childPath.dropFirst() ) ).orElseGet( Voluntary::empty );
	}

	protected BaseClass childFindOrCreate( @Nonnull String childPath )
	{
		return childFindOrCreate( Namespace.of( childPath ) );
	}

	protected BaseClass childFindOrCreate( @Nonnull NodeStack childPath )
	{
		notDisposed();
		notReadOnly();

		if ( childPath.getNodeCount() == 0 )
			return ( BaseClass ) this;

		String childName = childPath.getStringFirst();
		return Voluntary.of( children.stream().filter( child -> childName.equals( child.getLocalName() ) ).findFirst() ).ifAbsentGet( () -> Voluntary.notEmpty( childCreate( childName ) ) ).map( child -> child.childFindOrCreate( childPath.dropFirst() ) ).orElseThrow( () -> new RuntimeException( "General Internal Failure" ) );
	}

	public final <C> Stream<C> collect( Function<BaseClass, C> function )
	{
		notDisposed();
		return Stream.concat( Stream.of( function.apply( ( BaseClass ) this ) ), children.stream().flatMap( c -> c.collect( function ) ) ).filter( Objects::nonNull );
	}

	protected void destroy()
	{
		if ( isDisposed() )
			return;
		notReadOnly();
		for ( BaseClass child : children )
			child.destroy();
		removeFromParent();
		children.clear();
		flags.clear();
		flags.set( Flags.DISPOSED );
	}

	/**
	 * Makes a clone of this container with the exception of skipping the parent, you'll manually add the parent if this was recursive.
	 */
	public BaseClass duplicate()
	{
		try
		{
			BaseClass clone = creator.apply( null, localName );

			listeners.values().forEach( clone::listenerAdd ); // Copy listeners
			clone.flags = BitSet.valueOf( flags.toLongArray() ); // Clone flags BitSet
			clone.parent = null; // Guarantee it has no parent
			clone.containerOptions = containerOptions; // Copy container options

			for ( BaseClass child : children )
				clone.addChild( null, child.duplicate() );

			return clone;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	final BaseClass findFlag( int flag )
	{
		return ( BaseClass ) ( flags.get( flag ) ? this : parent == null ? null : parent.findFlag( flag ) );
	}

	public int getChildCount()
	{
		return children.size();
	}

	public int getChildCountOf( String key )
	{
		return getChildVoluntary( key ).map( ContainerBase::getChildCount ).orElse( -1 );
	}

	public final BaseClass getChild( @Nonnull NodeStack key ) throws NoSuchElementException
	{
		return childFind( key ).orElseThrow( NoSuchElementException::new );
	}

	public final BaseClass getChild( @Nonnull String key ) throws NoSuchElementException
	{
		return childFind( Namespace.of( key ) ).orElseThrow( NoSuchElementException::new );
	}

	public final BaseClass getChild( @Nonnull TypeBase type ) throws NoSuchElementException
	{
		return getChild( type.getPath() );
	}

	public final BaseClass getChildOrCreate( @Nonnull String key )
	{
		return getChildOrCreate( Namespace.of( key ) );
	}

	public final BaseClass getChildOrCreate( @Nonnull NodeStack key )
	{
		return childFindOrCreate( key );
	}

	public final BaseClass getChildOrCreate( @Nonnull TypeBase type )
	{
		return getChildOrCreate( type.getPath() );
	}

	public final Voluntary<BaseClass> getChildVoluntary( @Nonnull String key )
	{
		return getChildVoluntary( Namespace.of( key ) );
	}

	public final Voluntary<BaseClass> getChildVoluntary( @Nonnull NodeStack key )
	{
		return childFind( key );
	}

	public final Stream<BaseClass> getChildren()
	{
		return children.stream();
	}

	public final Set<String> getChildrenNames()
	{
		return children.stream().map( ContainerBase::getLocalName ).collect( Collectors.toSet() );
	}

	public final Stream<BaseClass> getChildrenRecursive()
	{
		notDisposed();
		return Streams.recursive( ( BaseClass ) this, ContainerBase::getChildren );
	}

	public final String getCurrentPath()
	{
		return getNamespace().reverseOrder().getString();
	}

	public final String getDomainChild()
	{
		return Namespace.parseDomain( getCurrentPath() ).getChild().getString();
	}

	public final String getDomainTLD()
	{
		return Namespace.parseDomain( getCurrentPath() ).getTld().getString();
	}

	protected abstract ExceptionClass getException( @Nonnull String message, @Nullable Exception exception );

	public BitSet getFlags()
	{
		return flags;
	}

	public final Set<Namespace> getKeys()
	{
		notDisposed();
		return children.stream().map( ContainerBase::getLocalName ).map( Namespace::of ).collect( Collectors.toSet() );
	}

	public final Set<Namespace> getKeysDeep()
	{
		notDisposed();
		return Stream.concat( getKeys().stream(), getChildren().flatMap( n -> n.getKeysDeep().stream().map( s -> n.getLocalName() + "." + s ) ).map( Namespace::of ) ).collect( Collectors.toSet() );
	}

	/**
	 * Gets the name of this individual {@link BaseClass}, in the path.
	 *
	 * @return Name of this node
	 */
	@Nonnull
	public final String getLocalName()
	{
		return localName;
	}

	public final Namespace getNamespace()
	{
		if ( localName.length() == 0 )
			return Namespace.empty();
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
		notDisposed();
		return parent;
	}

	public final Stream<BaseClass> getParents()
	{
		notDisposed();
		return Streams.transverse( parent, BaseClass::getParent );
	}

	public final BaseClass getRoot()
	{
		return parent == null ? ( BaseClass ) this : parent.getRoot();
	}

	public final boolean hasChild( String key )
	{
		return hasChild( Namespace.of( key ) );
	}

	public final boolean hasChild( NodeStack key )
	{
		return childFind( key ) != null;
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

	public boolean isValidateName( @Nullable String name )
	{
		if ( name == null || name.length() == 0 )
			return false;
		if ( !name.matches( "[A-Za-z0-9*_.]*" ) )
			return false;
		return true;
	}

	protected final int listenerAdd( ContainerListener.Container container )
	{
		return Maps.firstKeyAndPut( listeners, container );
	}

	public final int listenerChildAddAfter( ContainerListener.OnChildAdd<BaseClass> function, ContainerListener.Flags... flags )
	{
		return listenerAdd( new ContainerListener.Container( LISTENER_CHILD_ADD_AFTER, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0] );
			}
		} );
	}

	public final int listenerChildAddBefore( ContainerListener.OnChildAdd<BaseClass> function, ContainerListener.Flags... flags )
	{
		return listenerAdd( new ContainerListener.Container( LISTENER_CHILD_ADD_BEFORE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0] );
			}
		} );
	}

	public final int listenerChildRemoveAfter( ContainerListener.OnChildRemove<BaseClass> function, ContainerListener.Flags... flags )
	{
		return listenerAdd( new ContainerListener.Container( LISTENER_CHILD_REMOVE_AFTER, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( BaseClass ) objs[1] );
			}
		} );
	}

	boolean listenerFire( int type, Object... objs )
	{
		try
		{
			listenerFireWithException( true, type, objs );
			return true;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return false;
		}
	}

	void listenerFireWithException( int type, Object... objs ) throws ExceptionClass
	{
		listenerFireWithException( true, type, objs );
	}

	void listenerFireWithException( boolean local, int type, Object... objs ) throws ExceptionClass
	{
		if ( hasParent() )
			parent.listenerFireWithException( false, type, objs );
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

	public final void listenerRemove( int inx )
	{
		listeners.remove( inx );
	}

	public final void listenerRemoveAll()
	{
		listeners.clear();
	}

	public final int listenerRemoveChildBefore( ContainerListener.OnChildRemove<BaseClass> function, ContainerListener.Flags... flags )
	{
		return listenerAdd( new ContainerListener.Container( LISTENER_CHILD_REMOVE_BEFORE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( BaseClass ) objs[1] );
			}
		} );
	}

	public void merge( @Nonnull BaseClass other )
	{
		notDisposed();
		notReadOnly();
		for ( BaseClass node : children )
			getChildOrCreate( node.getLocalName() ).merge( node );
		flags = ( BitSet ) other.flags.clone();
		setDirty( true );
	}

	/**
	 * Moves this node within the tree using absolutePath as the absolute navigator.
	 * Last node will be treated as the new node name
	 *
	 * e.g.:
	 * subnode/newname = /parent/thisnode -> /subnode/newname
	 * parent/subnode/thisnode = /parent/thisnode -> /parent/subnode/thisnode
	 */
	public final BaseClass moveAbsolute( @Nonnull NodeStack absolutePath ) throws ExceptionClass
	{
		notDisposed();
		notReadOnly();

		if ( absolutePath.getNodeCount() == 0 )
			throw new ContainerException( "Path must contain at least one node." );
		BaseClass targetParent = getRoot();
		if ( targetParent == this )
			targetParent = null;

		String targetLocalName = absolutePath.getLocalName();
		absolutePath = absolutePath.getParent();

		for ( int i = 0; i < absolutePath.getNodeCount(); i++ )
		{
			String node = absolutePath.getStringNode( i );

			if ( node.equals( "." ) )
			{
				// Ignore
			}
			else if ( node.equals( ".." ) )
			{
				// We have a parent, so shift us to the parent of the parent.
				if ( targetParent != null && targetParent.hasParent() )
					targetParent = targetParent.parent;
			}
			else
			{
				targetParent = targetParent == null ? childCreateWithException( node ) : targetParent.getChildOrCreate( Namespace.of( node ) );
			}
		}

		if ( targetParent != parent )
		{
			removeFromParent();
			if ( targetParent != null )
				targetParent.addChild( targetLocalName, ( BaseClass ) this );
		}

		return ( BaseClass ) this;
	}

	/**
	 * Moves this node within the tree using relativePath as the relative navigator.
	 * e.g.:
	 * subnode = /parent/thisnode -> /parent/subnode/thisnode
	 * ../parentB = /parent/thisnode -> /parentB/thisnode
	 */
	public final BaseClass moveRelative( @Nonnull NodeStack relativePath ) throws ExceptionClass
	{
		notDisposed();
		notReadOnly();

		if ( relativePath.getNodeCount() == 0 )
			return ( BaseClass ) this; // No Change
		BaseClass targetParent = parent;

		for ( int i = 0; i < relativePath.getNodeCount(); i++ )
		{
			String node = relativePath.getStringNode( i );

			if ( node.equals( "." ) )
			{
				// Ignore
			}
			else if ( node.equals( ".." ) )
			{
				// We have a parent, so shift us to the parent of the parent.
				if ( targetParent != null && targetParent.hasParent() )
					targetParent = targetParent.parent;
			}
			else
			{
				targetParent = targetParent == null ? childCreateWithException( node ) : targetParent.getChildOrCreate( Namespace.of( node ) );
			}
		}

		if ( targetParent != parent )
		{
			removeFromParentWithException();
			if ( targetParent != null )
				targetParent.addChild( null, ( BaseClass ) this );
		}

		return ( BaseClass ) this;
	}

	protected final void notDisposed()
	{
		if ( hasFlag( Flags.DISPOSED ) )
			throw new ContainerException( getCurrentPath() + " has been disposed." );
	}

	public void notFlag( int flag )
	{
		if ( hasFlag( flag ) )
			throw new ContainerException( getCurrentPath() + " has " + flag + " flag." );
	}

	public void notReadOnly()
	{
		if ( hasFlag( Flags.READ_ONLY ) )
			throw new ContainerException( getCurrentPath() + " is read only." );
	}

	/**
	 * Polls a child from this stacker. If it exists, it's removed from it's parent, i.e., isolated on its own.
	 *
	 * @param key The child key
	 *
	 * @return found instance or null if does not exist.
	 */
	public Voluntary<BaseClass> pollChild( String key )
	{
		return getChildVoluntary( key ).ifPresent( ContainerBase::removeFromParent );
	}

	public final BaseClass removeFlag( int... flags )
	{
		notDisposed();
		for ( int flag : flags )
			this.flags.set( flag, false );
		return ( BaseClass ) this;
	}

	public final BaseClass removeFlagRecursive( int... flags )
	{
		notDisposed();
		if ( parent != null )
			parent.removeFlagRecursive( flags );
		return removeFlag( flags );
	}

	public final BaseClass removeFromParent()
	{
		if ( hasParent() )
		{
			parent.notFlag( Flags.READ_ONLY );
			if ( listenerFire( LISTENER_CHILD_REMOVE_BEFORE, parent, this ) )
			{
				parent.children.remove( this );
				listenerFire( LISTENER_CHILD_REMOVE_AFTER, parent, this );
				parent = null;
				setDirty( true );
			}
		}
		return ( BaseClass ) this;
	}

	public final BaseClass removeFromParentWithException() throws ExceptionClass
	{
		if ( hasParent() )
		{
			parent.notFlag( Flags.READ_ONLY );
			listenerFireWithException( LISTENER_CHILD_REMOVE_BEFORE, parent, this );
			parent.children.remove( this );
			listenerFire( LISTENER_CHILD_REMOVE_AFTER, parent, this );
			parent = null;
			setDirty( true );
		}
		return ( BaseClass ) this;
	}

	public void setDirty( boolean dirty )
	{
		if ( hasFlag( Flags.DISPOSED ) )
			return; // Ignore
		if ( dirty )
			addFlag( Flags.DIRTY );
		else
			removeFlag( Flags.DIRTY );
	}

	public void setLocalName( @Nonnull String localName )
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
	public void trimChildren()
	{
		children.stream().filter( ContainerBase::isTrimmable ).forEach( ContainerBase::destroy );
		children.forEach( ContainerBase::trimChildren );
	}

	public enum ConflictStrategy
	{
		// All children are first removed.
		CLEAR,
		// Conflicting children will be merged.
		MERGE,
		// Conflicting children will be first destroyed.
		OVERWRITE,
		// Conflicts are ignored.
		IGNORE
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
