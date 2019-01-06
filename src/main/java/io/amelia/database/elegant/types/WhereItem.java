/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant.types;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.amelia.support.Objs;

public abstract class WhereItem
{
	private Divider separator = Divider.NONE;

	private WhereItem()
	{

	}

	public final Divider seperator()
	{
		return separator;
	}

	public final void seperator( Divider seperator )
	{
		this.separator = seperator;
	}

	public abstract String toSqlQuery();

	public abstract Stream<Object> values();

	public enum Divider
	{
		AND( "AND" ),
		OR( "OR" ),
		NONE( "" );

		private String seq;

		Divider( String seq )
		{
			this.seq = seq;
		}

		@Override
		public String toString()
		{
			return seq;
		}
	}

	public static class Group<B extends Where<?, ?>, P> extends WhereItem implements Where<Group<B, P>, P>
	{
		private B back = null;
		private Divider currentSeparator = Divider.NONE;

		/*
		 * key = val
		 * key NOT val
		 * key LIKE val
		 * key < val
		 * key > val
		 *
		 * WHERE
		 * KEY -> DIVIDER -> VALUE
		 * AND
		 * OR
		 */
		private List<WhereItem> elements = new LinkedList<>();
		private P parent = null;

		public Group( B back, P parent )
		{
			this.back = back;
			this.parent = parent;
		}

		@Override
		public Group<B, P> and()
		{
			if ( elements.size() < 1 )
				currentSeparator = Divider.NONE;
			else
				currentSeparator = Divider.AND;
			return this;
		}

		public B back()
		{
			back.where( this );
			return back;
		}

		@Override
		public List<WhereItem> getElements()
		{
			return Collections.unmodifiableList( elements );
		}

		@Override
		public Group<Group<B, P>, P> group()
		{
			Group<Group<B, P>, P> group = new Group<>( this, parent );
			group.seperator( currentSeparator );
			elements.add( group );
			or();
			return group;
		}

		@Override
		public Group<B, P> or()
		{
			if ( elements.size() < 1 )
				currentSeparator = Divider.NONE;
			else
				currentSeparator = Divider.OR;
			return this;
		}

		public P parent()
		{
			back.where( this );
			return parent;
		}

		@Override
		public Divider separator()
		{
			return currentSeparator;
		}

		public int size()
		{
			return elements.size();
		}

		/*@Override
		public Group<B, P> where( Map<String, Object> map )
		{
			for ( Map.Entry<String, Object> e : map.entrySet() )
			{
				String key = e.getKey();
				Object val = e.getValue();

				if ( key.startsWith( "|" ) )
				{
					key = key.substring( 1 );
					or();
				}
				else if ( key.startsWith( "&" ) )
				{
					key = key.substring( 1 );
					and();
				}

				if ( val instanceof Map )
					try
					{
						Group<?, ?> group = group();

						@SuppressWarnings( "unchecked" )
						Map<String, Object> submap = ( Map<String, Object> ) val;
						for ( Map.Entry<String, Object> e2 : submap.entrySet() )
						{
							String key2 = e2.getKey();
							Object val2 = e2.getValue();

							if ( key2.startsWith( "|" ) )
							{
								key2 = key2.substring( 1 );
								group.or();
							}
							else if ( key2.startsWith( "&" ) )
							{
								key2 = key2.substring( 1 );
								group.and();
							}

							where( key2 ).matches( val2 );
						}
					}
					catch ( ClassCastException ee )
					{
						DatabaseManager.getLogger().severe( ee );
					}
				else
					where( key ).matches( val );
			}

			return this;
		}*/

		@Override
		public String toSqlQuery()
		{
			List<String> segments = new LinkedList<>();

			for ( WhereItem e : elements )
			{
				if ( e.seperator() != Divider.NONE && e != elements.get( 0 ) )
					segments.add( e.seperator().toString() );
				segments.add( e.toSqlQuery() );
			}

			if ( segments.size() == 0 )
				return "";

			return "(" + Joiner.on( " " ).join( segments ) + ")";
		}

		public Stream<Object> values()
		{
			return elements.stream().flatMap( WhereItem::values );
		}

		@Override
		public Group<B, P> where( WhereItem element )
		{
			element.seperator( currentSeparator );
			elements.add( element );
			and();

			return this;
		}

		@Override
		public KeyValue<Group<B, P>> where( String key )
		{
			return new KeyValue<>( this, key );
		}

		@Override
		public Group<B, P> whereMatches( Collection<String> valueKeys, Collection<Object> valueValues )
		{
			Group<Group<B, P>, P> group = new Group<>( this, parent );

			List<String> listKeys = new ArrayList<>( valueKeys );
			List<Object> listValues = new ArrayList<>( valueValues );

			for ( int i = 0; i < Math.min( listKeys.size(), listValues.size() ); i++ )
			{
				KeyValue<Group<Group<B, P>, P>> groupElement = group.where( listKeys.get( i ) );
				groupElement.seperator( Divider.AND );
				groupElement.matches( listValues.get( i ) );
			}

			group.parent();
			or();
			return this;
		}

		@Override
		public Group<B, P> whereMatches( Map<String, Object> values )
		{
			Group<Group<B, P>, P> group = new Group<>( this, parent );

			for ( Map.Entry<String, Object> val : values.entrySet() )
			{
				KeyValue<Group<Group<B, P>, P>> groupElement = group.where( val.getKey() );
				groupElement.seperator( Divider.AND );
				groupElement.matches( val.getValue() );
			}

			group.parent();
			or();
			return this;
		}

		@Override
		public Group<B, P> whereMatches( String key, Object value )
		{
			return new KeyValue<>( this, key ).matches( value );
		}
	}

	public static final class KeyValue<T extends Where<?, ?>> extends WhereItem
	{
		private final String key;
		private final T parent;
		private Operands operator = Operands.EQUAL;
		private Object value = "";

		public KeyValue( T parent, String key )
		{
			Objs.notNull( parent );
			this.key = key;
			this.parent = parent;
		}

		public T between( Object n1, Object n2 )
		{
			moreThan( n1 );
			parent.and().where( key ).lessThan( n2 );
			return parent;
		}

		@Override
		public boolean equals( Object obj )
		{
			if ( !( obj instanceof KeyValue ) )
				throw new IllegalArgumentException( "Received a call to the equals() method for the SQLWhereKeyValue class! We could be wrong but since the object was not an instance of this class, we decided to alert you that if you were attempting to match a key and value, the correct method would be matches()." );
			return super.equals( obj );
		}

		public String key()
		{
			return key;
		}

		public T lessEqualThan( Object n )
		{
			operator = Operands.LESSEREQUAL;
			value = n;
			parent.where( this );
			return parent;
		}

		public T lessThan( Object n )
		{
			operator = Operands.LESSER;
			value = n;
			parent.where( this );
			return parent;
		}

		public T like( String value )
		{
			operator = Operands.LIKE;
			this.value = value;
			parent.where( this );
			return parent;
		}

		/**
		 * Similar to {@link #like(String)}, except will wrap the value with wild card characters if none exist.
		 */
		public T likeWild( String value )
		{
			if ( !value.contains( "%" ) )
				value = "%" + value + "%";

			return like( value );
		}

		public T matches( Object value )
		{
			operator = Operands.EQUAL;
			this.value = value;
			parent.where( this );
			return parent;
		}

		public T moreEqualThan( Object n )
		{
			operator = Operands.GREATEREQUAL;
			value = n;
			parent.where( this );
			return parent;
		}

		public T moreThan( Object n )
		{
			operator = Operands.GREATER;
			value = n;
			parent.where( this );
			return parent;
		}

		public T not( Object value )
		{
			operator = Operands.NOT_EQUAL;
			this.value = value;
			parent.where( this );
			return parent;
		}

		public T notLike( String value )
		{
			operator = Operands.NOT_LIKE;
			this.value = value;
			parent.where( this );
			return parent;
		}

		protected Operands operand()
		{
			return operator;
		}

		public T range( Object n1, Object n2 )
		{
			moreEqualThan( n1 );
			parent.and().where( key ).lessEqualThan( n2 );
			return parent;
		}

		public T regex( String value )
		{
			operator = Operands.REGEXP;
			this.value = value;
			parent.where( this );
			return parent;
		}

		@Override
		public String toSqlQuery()
		{
			return "`" + key + "` " + operator.stringValue() + " ?";
		}

		@Override
		public String toString()
		{
			return toSqlQuery();
		}

		@Override
		public Stream<Object> values()
		{
			return Stream.of( value == null ? "null" : value );
		}

		public enum Divider
		{
			EQUAL( "=" ),
			NOT( "NOT" ),
			LIKE( "LIKE" ),
			LESS( "<" ),
			MORE( ">" );

			private String seq;

			Divider( String seq )
			{
				this.seq = seq;
			}

			@Override
			public String toString()
			{
				return seq;
			}
		}

		enum Operands
		{
			EQUAL( "=" ),
			NOT_EQUAL( "!=" ),
			LIKE( "LIKE" ),
			NOT_LIKE( "NOT LIKE" ),
			GREATER( ">" ),
			GREATEREQUAL( ">=" ),
			LESSER( "<" ),
			LESSEREQUAL( "<=" ),
			REGEXP( "REGEXP" );

			private String operator;

			Operands( String operator )
			{
				this.operator = operator;
			}

			String stringValue()
			{
				return operator;
			}
		}
	}
}
