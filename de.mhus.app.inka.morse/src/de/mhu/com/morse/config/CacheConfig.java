package de.mhu.com.morse.config;

import java.io.File;
import java.io.InputStream;

import de.mhu.lib.config.Config;

public class CacheConfig extends Config {

	public static final String NAME = "cache";

	private int maxMemoryEntries;
	private long nextId = 0;
	private String store;
	private File contentCachePath;
	private long contentCacheTimeout;

	private String shares;

	public void configUpdated() {
		maxMemoryEntries = getProperty( "memory.max", 20 );
		store = getProperty( "store", "." );
		contentCachePath = new File( getProperty( "content.cache.path", "cache/content" ) );
		contentCacheTimeout = getProperty( "content.cache.timeout", 1000 * 10 );
		shares = getProperty( "shares", "" );
		contentCachePath.mkdirs();
	}

	public int getMaxMemoryEntries() {
		return maxMemoryEntries;
	}

	public synchronized File createTmpFile() {
		nextId++;
		return new File( store + "/cache" + nextId + ".tmp" );
	}

	public File getContentCachePath() {
		return contentCachePath;
	}

	public long getContentCacheTimeout() {
		return contentCacheTimeout;
	}

	public String getShares() {
		return shares;
	}

	public File getShareDirectory(String shareChannel) {
		return new File( getProperty( "shares." + shareChannel ) );
	}
	
}
