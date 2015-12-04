package de.mhu.com.morse.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.config.CacheConfig;

public class CacheList {

	private static AL log = new AL( CacheList.class );
	private long size = 0;
	private LinkedList<CacheEntry> memory = new LinkedList<CacheEntry>();
	private Class clazz;
	private File tmpFile;
	private ObjectOutputStream oos;
	private static CacheConfig config = (CacheConfig)ConfigManager.getConfig( "cache" );
	
	public CacheList( Class clazz ) {
		this.clazz = clazz;
		tmpFile = config.createTmpFile();
	}

	public void append( CacheEntry obj ) {
		synchronized ( memory ) {
			memory.add( obj );
			size++;
			
			if ( oos == null && size > config.getMaxMemoryEntries() ) {
				try {
					oos = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream( tmpFile ) ) );
					for ( Iterator<CacheEntry> i = memory.iterator(); i.hasNext(); )
						i.next().save( oos );
					memory.clear();
				} catch ( Exception e ) {
					log.error( e );
				}
			}
			
		}
	}
	
	public long getSize() {
		return size;
	}
	
	public void clear() {
		synchronized ( memory ) {
			try {
				if ( oos != null )
					oos.close();
				tmpFile.delete();
			} catch (IOException e) {
				log.error( e );
			}
			oos = null;
			memory.clear();
			size = 0;
		}
	}

	public Iterator<CacheEntry> iterator() {
		if ( oos != null ) {
			try {
				oos.flush();
				oos.close();
			} catch (IOException e) {
				log.error( e );
			}
			try {
				oos = null;
				return new FileCacheIterator( clazz, tmpFile, size );
			} catch (IOException e) {
				log.error( e );
			}
		}
		return memory.iterator();
	}
	
	private static class FileCacheIterator implements Iterator<CacheEntry> {

		private ObjectInputStream ois;
		private long  size;
		private Class clazz;
		private long  pos = 0;

		public FileCacheIterator(Class clazz, File tmpFile, long size) throws FileNotFoundException, IOException {
			ois = new ObjectInputStream( new FileInputStream( tmpFile ) );
			this.size = size;
			this.clazz = clazz;
		}

		public boolean hasNext() {
			if ( pos >= size && ois != null ) {
				try {
					ois.close();
				} catch (IOException e) {
					log.error( e );
				}
				ois = null;
			}
			return pos < size;
		}

		public CacheEntry next() {
			if ( pos >= size ) return null;
			pos++;
			try {
				CacheEntry entry = (CacheEntry)clazz.newInstance();
				entry.load( ois );
				return entry;
			} catch ( Exception e ) {
				log.error( e );
			}
			pos = size;
			return null;
		}

		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
