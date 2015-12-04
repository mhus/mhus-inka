package de.mhu.com.morse.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberInputStream;
import java.io.LineNumberReader;

import de.mhu.lib.AFile;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.config.CacheConfig;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectFileStore;
import de.mhu.com.morse.utils.ObjectUtil;

public class ContentCache {
	
	private static final AL log = new AL( ContentCache.class ); 
	
	private static ContentCache instance = null;
	
	public static synchronized ContentCache getInstance() {
		if ( instance == null )
			instance = new ContentCache();
		return instance;
	}

	private CacheConfig config;
	
	private ContentCache() {
		config = (CacheConfig)ConfigManager.getConfig( CacheConfig.NAME );
	}

	public InputStream getInputStream( IConnection con, String id, int rendition ) throws MorseException, FileNotFoundException {
		return new FileInputStream( getFileInt(con, id, rendition));
	}
	
	public FileReader getReader( IConnection con, String id, int rendition ) throws MorseException, FileNotFoundException {
		return new FileReader( getFileInt(con, id, rendition));
	}
	
	public File getFile( IConnection con, String id, int rendition ) throws MorseException {
		return new File( getFileInt(con, id, rendition).getAbsolutePath() ) {

			@Override
			public boolean canWrite() {
				return false;
			}

			@Override
			public boolean createNewFile() throws IOException {
				return false;
			}

			@Override
			public boolean delete() {
				return false;
			}

			@Override
			public void deleteOnExit() {
			}

			@Override
			public boolean mkdir() {
				return false;
			}

			@Override
			public boolean mkdirs() {
				return false;
			}

			@Override
			public boolean renameTo(File dest) {
				return false;
			}

			@Override
			public boolean setLastModified(long time) {
				return false;
			}

			@Override
			public boolean setReadOnly() {
				return true;
			}
			
		};
	}
	
	private synchronized File getFileInt( IConnection con, String id, int rendition ) throws MorseException {
		if ( log.t3() ) log.info( id );
		ObjectUtil.assetId( id );
		File path = config.getContentCachePath();
		File xpath = new File( path, new String( ObjectFileStore.getPathForId(id) ) );
		xpath.mkdirs();
		
		File store = new File( xpath, String.valueOf( rendition ) );
		File storeInfo = new File( xpath, String.valueOf( rendition ) + ".txt" );
		try {
			boolean needLoad = true;
			String shareChannel = "";
			String sharePath = "";
			try {
				if ( storeInfo.exists() ) {
					if ( log.t4() ) log.info( "Exists" );
					
					LineNumberReader r = new LineNumberReader( new FileReader( storeInfo ) );
					String vstamp = r.readLine();
					long timeout  = Long.parseLong( r.readLine() );
					shareChannel  = r.readLine();
					sharePath     = r.readLine();
					r.close();
					
					if ( sharePath.length() == 0 && !store.exists() ) {
						needLoad = true;
					} else {
						if ( timeout <= System.currentTimeMillis() ) {
							// no timeout 
							needLoad = false;							
						} else {
							// timeout check vstamp
							String mql = "FETCH " + id + " STAMP";
							Query query = new Query( con, mql );
							IQueryResult res = query.execute();
							res.next();
							String newVstamp = res.getString( 0 );
							res.close();
							if ( vstamp.equals( newVstamp ) ) {
								needLoad = false;
								// refresh timeout
								FileWriter writer = new FileWriter( storeInfo );
								writer.write( vstamp );
								writer.write( '\n' );
								writer.write( String.valueOf( System.currentTimeMillis() + config.getContentCacheTimeout() ) );
								writer.write( '\n' );
								writer.write( shareChannel );
								writer.write( '\n' );
								writer.write( sharePath );
								writer.write( '\n' );
								writer.close();
							}
						}
					}
				}
			} catch ( Throwable t ) {
			}
			
			if ( ! needLoad ) {
				if ( log.t4() ) log.info( "no reload" );
			
				if ( sharePath.length() == 0 )
					return store;
				
				return new File( config.getShareDirectory( shareChannel ), sharePath );
			}
			
			if ( log.t4() ) log.info( "reload" );
			
			store.getParentFile().mkdirs();
			FileOutputStream fos = new FileOutputStream( store );
			String shared = "";
			String shares = config.getShares();
			if ( shares.length() > 0 ) {
				shared = " SHARED " + shares;
			}
			String mql = "RENDITION " + id + " LOAD `"+rendition+"`" + shared;
			Query query = new Query( con, mql );
			IQueryResult res = query.execute();
			if ( res.next() ) {
				shareChannel = res.getString( "channel" );
				sharePath    = res.getString( "path" );
				store = new File( config.getShareDirectory( shareChannel ), sharePath );
			} else {
				InputStream is = res.getInputStream();
				AFile.copyFile( is, fos );
				is.close();
				shareChannel = "";
				sharePath = "";
			}
			res.close();
			
			mql = "FETCH " + id + " STAMP";
			query = new Query( con, mql );
			res = query.execute();
			res.next();
			String newVstamp = res.getString( 0 );
			res.close();
			
			FileWriter writer = new FileWriter( storeInfo );
			writer.write( newVstamp );
			writer.write( '\n' );
			writer.write( String.valueOf( System.currentTimeMillis() + config.getContentCacheTimeout() ) );
			writer.write( '\n' );
			writer.write( shareChannel );
			writer.write( '\n' );
			writer.write( sharePath );
			writer.write( '\n' );
			writer.close();
			
		} catch (IOException e) {
			throw new MorseException( MorseException.ERROR, e );
		}
		return store;
	}
	
}
