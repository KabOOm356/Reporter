package net.KabOOm356.Util;

import java.util.Map.Entry;

/**
 * A pair of Objects.
 * 
 * @param <K> The type of the key (first object of the pair).
 * @param <V> The type of the value (the second object of the pair).
 */
public class ObjectPair<K,V> implements Entry<K,V>
{
	private K key;
	private V value;
	
	public ObjectPair(K key, V value)
	{
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey()
	{
		return key;
	}

	@Override
	public V getValue()
	{
		return value;
	}
	
	public K setKey(K key)
	{
		K oldKey = this.key;
		
		this.key = key;
		
		return oldKey;
	}

	@Override
	public V setValue(V value)
	{
		V oldValue = this.value;
		
		this.value = value;
		
		return oldValue;
	}
}
