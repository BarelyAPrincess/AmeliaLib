package io.amelia.database.elegant.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiFunction;

import io.amelia.support.Objs;

public class HashMapSet<K, V> extends HashSet<Map<K, V>>
{
	public <CK, CV> HashMapSet<CK, CV> castMap( Class<CK> keyClass, Class<CV> valueClass )
	{
		return castMap( ( m, e ) -> m.put( Objs.castTo( e.getKey(), keyClass ), Objs.castTo( e.getValue(), valueClass ) ) );
	}

	public <CK> HashMapSet<CK, V> castMapKey( Class<CK> keyClass )
	{
		return castMap( ( m, e ) -> m.put( Objs.castTo( e.getKey(), keyClass ), e.getValue() ) );
	}

	public <CV> HashMapSet<K, CV> castMapValue( Class<CV> valueClass )
	{
		return castMap( ( m, e ) -> m.put( e.getKey(), Objs.castTo( e.getValue(), valueClass ) ) );
	}

	public <CK, CV> HashMapSet<CK, CV> castMap( BiFunction<HashMap<CK, CV>, Map.Entry<K, V>, Object> func )
	{
		HashMapSet<CK, CV> set = new HashMapSet<>();
		for ( Map<K, V> oldMap : this )
		{
			HashMap<CK, CV> newMap = new HashMap<>();
			for ( Map.Entry<K, V> entry : oldMap.entrySet() )
				func.apply( newMap, entry );
			set.add( newMap );
		}
		return set;
	}
}
