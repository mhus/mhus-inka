package de.mhu.com.morse.cache;

import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import de.mhu.lib.AThreadDaemon;
import de.mhu.lib.log.AL;

/**
 * This cache stores the objects in memory for a specified time. Is outtime is zero or less it will
 * use SoftReferences like WeakHashMap. A Deamon Thread checks in the background if entries should be deleted.
 * If the outtime is reached the object is never given back! The Cache is synchronized and can be used by different
 * threads in the same time. There is no need to dispose a cache. If the cahce has no other reference it will be 
 * removed from the Deamon Thread.
 * 
 * @author mike
 *
 * @param <K>
 * @param <V>
 */
public class MemoryCache<K,V> {
	
	private static AL log = new AL(MemoryCache.class);

	private static AThreadDaemon controlThread;
	private static WeakHashMap<MemoryCache, String> caches = new WeakHashMap<MemoryCache, String>();
	private Hashtable<K, CacheEntry<V>> cache = new Hashtable<K, CacheEntry<V>>();
	private long outtime = 0;
	private String name;
	private boolean isSoft;

	static {
		controlThread = new AThreadDaemon( "Memory_Cache_Control" ) {
			public void run() {
				while ( true ) {
					Object[] list = null;
					sleep( 1000 * 60 );
					synchronized ( caches ) {
						list = caches.keySet().toArray();
					}
					for ( int i = 0; i < list.length; i++ ) {
						synchronized( ((MemoryCache)list[ i ]).cache ) {
							if ( log.t5() )
								log.debug( "MCC: start " + list[ i ].getClass().getTypeParameters()[ 0 ] + ',' + list[ i ].getClass().getTypeParameters()[ 1 ]);
							int cnt = 0;
							if ( ((MemoryCache)list[ i ]).size() > 0 ) {
								for ( Iterator<Map.Entry> j = ((MemoryCache)list[ i ]).cache.entrySet().iterator(); j.hasNext(); ) {
									Map.Entry entry = j.next();
									if ( ((CacheEntry)entry.getValue()).isOutdated() ) {
										((MemoryCache)list[ i ]).cache.remove( entry.getKey() );
										cnt++;
									}
								}
							}
							if ( log.t5() )
								log.debug( "MCC: end " + cnt );

						}
						sleep( 1000 * 30 );
						list[ i ] = null;
					}
				}
			}
		};
		controlThread.start();
	}
	
	public static void invalidate( String name ) {
		Object[] list = null;
		synchronized ( caches ) {
			list = caches.keySet().toArray();
		}
		for ( int i = 0; i < list.length; i++ ) {
			if ( name.equals( ((MemoryCache)list[i]).getName() ) ) {
				synchronized( ((MemoryCache)list[ i ]).cache ) {
					if ( log.t5() )
						log.debug( "MCC: invalidate " + name );
					((MemoryCache)list[ i ]).cache.clear();
					((MemoryCache)list[ i ]).refill();
				}
			}
		}
		
	}
	
	public static String[] getNames() {
		Object[] list = null;
		synchronized ( caches ) {
			list = caches.keySet().toArray();
		}
		String[] names = new String[ list.length ];
		for ( int i = 0; i < list.length; i++ ) {
			names[i] = ((MemoryCache)list[i]).getName();
		}
		return names;
	}
	
	
	public MemoryCache( String pName, long pOutTime, boolean pIsSoft ) {
		name = pName;
		isSoft = pIsSoft;
		setOuttime( pOutTime );
	}
	
	public String getName() {
		return name;
	}
	
	public void clear() {
		cache.clear();
	}

	public boolean contains(Object value) {
		synchronized ( cache ) {
			return cache.contains(value);
		}
	}

	public boolean containsKey(Object key) {
		synchronized ( cache ) {
			return cache.containsKey(key);
		}
	}

	public boolean containsValue(Object value) {
		synchronized ( cache ) {
			return cache.containsValue(value);
		}
	}

	public V get(Object key) {
		synchronized ( cache ) {
			CacheEntry<V> entry = cache.get(key);
			if ( entry == null ) return null;
			if ( entry.isOutdated() ) {
				remove( key );
				return null;
			}
			return entry.getValue();
		}
	}

	public boolean isEmpty() {
		synchronized ( cache ) {
			return cache.isEmpty();
		}
	}

	public Enumeration<K> keys() {
		synchronized ( cache ) {
			return cache.keys();
		}
	}

	public Set<K> keySet() {
		synchronized ( cache ) {
			return cache.keySet();
		}
	}

	public V put(K key, V value) {
		synchronized ( cache ) {
			CacheEntry<V> entry = cache.put(key, new CacheEntry<V>( value, outtime, isSoft ) );
			if ( entry == null ) return null;
			return entry.getValue();
		}
	}

	public V remove(Object key) {
		synchronized ( cache ) {
			CacheEntry<V> entry = cache.remove(key);
			if ( entry == null ) return null;
			return entry.getValue();
		}
	}

	public int size() {
		synchronized ( cache ) {
			return cache.size();
		}
	}
	
	/**
	 * Overwrite to refill the cache automatically after invalition.
	 */
	public void refill() {
	}
	
	/**
	 * Set the outtime in milliseconds. if the value euqls or is less zero
	 * the cache will use SoftReferences. If the value is changed the new
	 * value is only for new entries in the cache.
	 * 
	 * @param millis Time in milleseconds to live.
	 */
	public void setOuttime(long millis) {
		outtime = millis;
	}
	
	static class CacheEntry<T> {

		private Object value;
		private long outdated = 0;
		private boolean isSoft;

		public CacheEntry(T in, long outtime, boolean isSoft ) {
			
			this.isSoft = isSoft;
			
			if ( outtime > 0 ) {
				outdated = System.currentTimeMillis() + outtime;
				value = in;
			}
			if ( isSoft ) {
				value = new SoftReference<T>( in );
			}
		}

		public T getValue() {
			if ( value == null ) return null;
			if ( isSoft ) {
				return ((SoftReference<T>)value).get();
			}
			return (T)value;
		}

		public boolean isOutdated() {
			if ( value == null ) return true;
			if ( isSoft ) {
				return ((SoftReference<T>)value).get() == null;
			}
			return System.currentTimeMillis() > outdated;
		}

		@Override
		public boolean equals(Object obj) {
			if ( value == null ) return false;
			if ( isSoft ) {
				T v = ((SoftReference<T>)value).get();
				if ( v == null ) return false;
				return v.equals( obj );
			}
			return value.equals( obj );
		}

		@Override
		public int hashCode() {
			if ( value == null ) return 0;
			if ( isSoft ) {
				T v = ((SoftReference<T>)value).get();
				if ( v == null ) return 0;
				return v.hashCode();
			}
			return value.hashCode();
		}
	
	}
	
}
