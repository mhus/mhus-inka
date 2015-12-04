package de.mhus.dboss.core.impl;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import de.mhus.dboss.core.IType;
import de.mhus.lib.MEventHandler;

public class MMap<K,V> implements Map<K, V> {

	private Hashtable<K,V> byName = new Hashtable<K,V>();

	@Override
	public void clear() {
		synchronized (this) {
			byName.clear();
		}
	}

	@Override
	public boolean containsKey(Object key) {
		synchronized (this) {
			return byName.containsKey(key);
		}
	}

	@Override
	public boolean containsValue(Object value) {
		synchronized (this) {
			return byName.containsValue(value);
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		synchronized (this) {
			return byName.entrySet();
		}
	}

	@Override
	public V get(Object key) {
		synchronized (this) {
			return byName.get(key);
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (this) {
			return byName.isEmpty();
		}
	}

	@Override
	public Set<K> keySet() {
		synchronized (this) {
			return byName.keySet();
		}
	}

	@Override
	public V put(K key, V value) {
		synchronized (this) {
			return byName.put(key, value);
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		synchronized (this) {
			byName.putAll(m);
		}
	}

	@Override
	public V remove(Object key) {
		synchronized (this) {
			return byName.remove(key);
		}
	}

	@Override
	public Collection<V> values() {
		synchronized (this) {
			return byName.values();
		}
	}

	@Override
	public int size() {
		synchronized (this) {
			return byName.size();
		}
	}
	
}
