package de.mhu.com.morse.channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.AThread;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.cmd.TimeoutException;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectFileStore;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.IAfPpi;

/**
 * The objectmanager knows about all objects in all channels (expect the channel wants to hide). It also
 * creates new object ids. The data is stored on filesystems. If you use shared servers make shure the 
 * all use the same filesystem. Make also shure the filesystem has a atomic touch() operation. If not
 * the server is able to run in big trouble because of race conditions.
 * 
 * @author mike
 *
 */
public class ObjectManager extends AfPlugin implements IObjectManager {

	private static char[] allowed = new char[] {'_','0','1','2','3','4','5','6','7','8','9',
		'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	
	private Config config = ConfigManager.getConfig( "server" );
	
	ObjectFileStore store = null;
	private LinkedList<IObjectListener> objectListeners = new LinkedList<IObjectListener>();

	private String morseId;

	private long creasyId = -System.currentTimeMillis();
	private static AL log = new AL( ObjectManager.class );
	
	protected void apDestroy() throws Exception {
		
	}

	protected void apDisable() throws AfPluginException {
		
	}

	protected void apEnable() throws AfPluginException {
		
		// clear
		
		// refresh
		if ( config.getProperty( "initialize.ids", false ) )
			refresh();
 	
	}
	
	public void refresh() throws MorseException {
		IChannelDriver[] ppis = (IChannelDriver[])getPpi( IChannelDriver.class );
		for ( int i = 0; i < ppis.length; i++ ) {
			log().info( "Loading objects from " + ppis[i].getName() );
			long cnt = 0;
			for ( Iterator j = ppis[i].getObjectIds(); j.hasNext(); ) {
				String id = (String)j.next();
				try {
					Writer wr = store.getWriter( id );
					wr.write( ppis[i].getName() );
					wr.close();
					cnt++;
				} catch (IOException e) {
					log.error( e );
				}
			}
			log().info( "Objects: " + cnt );
		}
	}

	protected void apInit() throws Exception {
		
		store = new ObjectFileStore( new File( config.getProperty( "om.store", "objects" ) + "/index" ) );
		morseId = config.getProperty( "morse.base.id", "000" );
		if ( morseId.equals( "000" ) )
			log.info( "Morse Base is a Development Base" );
		ObjectUtil.assetMBaseId( morseId );
		appendPpi( IObjectManager.class, this );
	}

	public String newObjectId(IType type, IChannelDriver driver ) {
		
		while ( true ) {
			StringBuffer sb = new StringBuffer( 32 );
			sb.append( morseId );
			toIdString( System.currentTimeMillis(), sb );
			while ( sb.length() < 16 ) sb.append( '_' );
			toIdString( creasyId++, sb );
			
			if ( sb.length() > 32 ) sb.delete( 31, sb.length() );
			while ( sb.length() < 32 ) sb.append( '_' );
			
			String l = sb.toString();
			try {
				if ( store.touch( l ) ) {  // atomic !!!! no other sync needed
					try {
						Writer wr = store.getWriter( l );
						wr.write( driver.getName() );
						wr.close();
					} catch (IOException e) {
						log.error( e );
					}
					return l;
				}
			} catch (MorseException e) {
				log.error( e );
			} catch (IOException e) {
				log.error( e );
			}
		}
	
	}

	private void toIdString(long l, StringBuffer sb) {
		if ( l < 0 ) {
			sb.append( allowed[0] );
			l = -l;
		}
		do {
			sb.append( allowed[ (int)(l % allowed.length) ] );
			l = l / allowed.length;
		} while ( l > 0 );
	}

	public String findObject(String id) throws MorseException {
		String channel = null;
		try {
			Reader r = store.getReader( id );
			LineNumberReader lnr = new LineNumberReader( r );
			channel = lnr.readLine();
			r.close();
		} catch ( IOException e ) {
			throw new MorseException( MorseException.ERROR, e );
		}
		return channel;
	}

	public void eventContentSaved(String channel, String id, String parentId, String parentType) {
		if ( log.t3() ) log.info( "SAVED " + channel + ' ' + id + ' ' + parentId + ' ' + parentType );
		synchronized ( objectListeners ) {
			for ( Iterator<IObjectListener> i = objectListeners.iterator(); i.hasNext(); )
				try {
					i.next().eventContentSaved(channel, id, parentId, parentType);
				} catch ( Throwable t ) {
					log.error( t );
				}
		}
	}

	public void eventContentRemoved(String channel, String id, String parentId, String parentType) {
		if ( log.t3() ) log.info( "REMOVED " + channel + ' ' + id + ' ' + parentId + ' ' + parentType );
		synchronized ( objectListeners ) {
			for ( Iterator<IObjectListener> i = objectListeners.iterator(); i.hasNext(); )
				try {
					i.next().eventContentRemoved(channel, id, parentId, parentType);
				} catch ( Throwable t ) {
					log.error( t );
				}
		}
	}
	
	public void eventObjectCreated(String channel, String id, String type) {
		if ( log.t3() ) log.info( "CREATED " + channel + ' ' + id + ' ' + type );
		try {
			Writer wr = store.getWriter( id );
			wr.write( channel );
			wr.close();
		} catch (Exception e) {
			log.error( e );
		}
		synchronized ( objectListeners ) {
			for ( Iterator<IObjectListener> i = objectListeners.iterator(); i.hasNext(); )
				try {
					i.next().eventObjectCreated(channel, id, type);
				} catch ( Throwable t ) {
					log.error( t );
				}
		}
	}

	public void eventObjectDeleted(String channel, String id, String type) {
		if ( log.t3() ) log.info( "DELETED " + channel + ' ' + id + ' ' + type );
		try {
			store.delete( id );
		} catch (Exception e) {
			log.error( e );
		}
		synchronized ( objectListeners ) {
			for ( Iterator<IObjectListener> i = objectListeners.iterator(); i.hasNext(); )
				try {
					i.next().eventObjectDeleted(channel, id, type);
				} catch ( Throwable t ) {
					log.error( t );
				}
		}
	}

	public void eventObjectUpdated(String channel, String id, String type, String[] attributes) {
		if ( log.t3() ) log.info( "UPDATED " + channel + ' ' + id + ' ' + type );
		try {
			Writer wr = store.getWriter( id );
			wr.write( channel );
			wr.close();
		} catch (Exception e) {
			log.error( e );
		}
		synchronized ( objectListeners ) {
			for ( Iterator<IObjectListener> i = objectListeners.iterator(); i.hasNext(); )
				try {
					i.next().eventObjectUpdated(channel, id, type, attributes);
				} catch ( Throwable t ) {
					log.error( t );
				}
		}
	}

	public void registerObjectListener( IObjectListener listener ) {
		synchronized ( objectListeners ) {
			objectListeners.add( listener );
		}
	}
	
	public void unregisterObjectListener( IObjectListener listener ) {
		synchronized ( objectListeners ) {
			objectListeners.remove( listener );
		}
	}

	public void lock(String id, String name, long timeout) throws MorseException {
		
		if ( log.t4() )
			log.info( "LOCK: " + id + ' ' + name );
		
		File idFile = ObjectFileStore.getFileForId( store.getRoot(), id );
		if ( ! idFile.exists() ) throw new MorseException( MorseException.OBJECT_NOT_FOUND, id );
		File dest = new File( idFile, "_" + name );

		try {
			while ( ! dest.createNewFile() ) {
				AThread.sleep( 100 );
				timeout=-100;
				if ( timeout <= 0 )
					throw new MorseException( MorseException.CANT_LOCK, new String[] { id, name } );
			}
		} catch ( IOException ioe ) {
			throw new MorseException( MorseException.ERROR, ioe );
		}
	}

	public void unlock(String id, String name ) throws MorseException {

		if ( log.t4() )
			log.info( "UNLOCK: " + id + ' ' + name );

		File idFile = ObjectFileStore.getFileForId( store.getRoot(), id );
		if ( ! idFile.exists() ) throw new MorseException( MorseException.OBJECT_NOT_FOUND, id );
		File dest = new File( idFile, "_" + name );
		dest.delete();
		
	}
	
}
