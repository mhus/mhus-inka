package de.mhu.com.morse.channel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.cache.CacheEntry;
import de.mhu.com.morse.cache.CacheList;
import de.mhu.com.morse.channel.sql.SqlDriver;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.AfPluginNode;

/**
 * @see IChannelProvider
 * 
 * @author mike
 *
 */
public class ChannelProvider extends AfPlugin implements IChannelProvider {

	private static Config config = ConfigManager.getConfig( "server" );
	private static AL log = new AL( ChannelProvider.class );
	private Hashtable<String,IChannelDriverServer> drivers =  new Hashtable<String,IChannelDriverServer>();
	private Hashtable<String,String> ownedPlugins = new Hashtable<String,String>();
	
	private IConnection defaultCon;
	private IObjectManager objectManager;
	private Hashtable<String, AfPlugin> initializedDbs;
	private Server server = new Server();
	private MyTransaction transaction = new MyTransaction();
	
	protected void apDestroy() throws Exception {
//		 close default connection
		((MyConnection)defaultCon).defaultConnection = false;
		defaultCon.close();
		defaultCon = null;
	}

	protected void apDisable() throws AfPluginException {

	}

	protected void apEnable() throws AfPluginException {
		objectManager = (IObjectManager)((AfPluginNode)getApParent()
				.getPluginByPath( config.getProperty( "core.module.path" ) ))
				.getNodeSinglePpi( IObjectManager.class );
		
		createChannels( "sys" );
		
	}
	
	protected IObjectManager getObjectManager() {
		if ( objectManager == null )
			objectManager = (IObjectManager)getSinglePpi( IObjectManager.class );
		return objectManager;
	}

	protected void apInit() throws Exception {
		
		defaultCon = new MyConnection( true );
		
		createChannels( "init" );
		
		appendPpi( IChannelProvider.class , this );
	}

	private void createChannels( String source ) throws AfPluginException {
		
		AfPluginNode coreNode = (AfPluginNode)getApParent().getPluginByPath( config.getProperty( "core.module.path" ) );
		IChannelDriver[] dbss = (IChannelDriver[])coreNode.getNodePpi( IChannelDriver.class );
		drivers.clear();
		for ( int i = 0; i < dbss.length; i++ ) {
			if ( dbss[i] instanceof IChannelDriverServer ) {
				if ( log().t4() ) log().info( "ADD " + dbss[i].getName() );
				drivers.put( dbss[i].getName(), (IChannelDriverServer)dbss[i] );
			} else
				log().info( "NOT A IChannelDriverServer: " + dbss[i].getName() );
		}
		
		IChannel con = defaultCon.getChannel( source );
		
		IQueryResult res;
		Hashtable<String,String> oldPlugins = new Hashtable<String,String>( ownedPlugins );
		
		try {
			res = con.query( new Query( defaultCon, "SELECT ** FROM m_channel @" + source ) );
			
			// AfPlugin sys = null;
			
			initializedDbs = new Hashtable<String, AfPlugin>();
			
			while ( res.next() ) {
				
				String name = res.getString( "NAME" );
				String clazz = res.getString( "CLASS" );
				String channel = res.getString( "CHANNEL" );
				String acl  = res.getString( "ACL" );
				Map<String,String> features = ObjectUtil.tableToMap( res.getTable( "features" ), "k", "v" );

				String ident = name + '_' + clazz + '_' + source;
				oldPlugins.remove( ident );
				if ( ! ownedPlugins.containsKey( ident ) ) {
					try {
						AfPlugin db = (AfPlugin)Class.forName( clazz ).newInstance();
						if ( db instanceof IChannelDriverServer ) {
							((IChannelDriverServer)db).setChannel( channel );
							((IChannelDriverServer)db).setAccessAcl(acl);
							((IChannelDriverServer)db).setChannelFeatures( features );
						}
						
						initializedDbs.put( channel, db );
						/*
						if ( channel != null && channel.equals( "sys" ) )
							sys = db;
						*/
						String pluginName = coreNode.addPlugin( db, name );
						ownedPlugins.put( ident, pluginName );
						coreNode.enablePlugin( pluginName );
					} catch ( Exception e ) {
						log().error( e );
					}
				}
			}
			
			/*
			if ( source.equals( "init" ) && sys != null && ( sys instanceof SqlAbstractDriver ) ) {
				// copy basics init -> sys
				SqlAbstractDriver sysDriver = (SqlAbstractDriver)sys;
				
				IQueryResult typesRes = con.query( new Query( defaultCon, "SELECT NAME FROM m_type @init" ) );
				while ( typesRes.next() ) {
					res = con.query( new Query( defaultCon, "SELECT * FROM " + typesRes.getString( "NAME" ) + " @init" ) );
					sysDriver.insertRecords( typesRes.getString( "NAME" ).toLowerCase(), res );
					res.close();
				}
				typesRes.close();
				
			}
			*/
			if ( source.equals( "init" ) ) {
				// copy basics init -> sys
				// SqlAbstractDriver sysDriver = (SqlAbstractDriver)sys;
				if ( config.getProperty( "initialize.db", false ) )
					reloadInit( con );
				
			}
			
		} catch (MorseException e) {
			log.error( e );
			throw new AfPluginException( 1, e );
		}
		
		res.close();
		
		// remove old ones ....
		for ( Iterator<String> i = oldPlugins.keySet().iterator(); i.hasNext(); ) {
			String key = i.next();
			String pluginName = (String)oldPlugins.get( key );
			ownedPlugins.remove( key );
			try {
				coreNode.disablePlugin( pluginName );
			} catch ( AfPluginException afpe ) {
				log().error( afpe );
			}
			try {
				coreNode.removePlugin( pluginName );
			} catch ( AfPluginException afpe ) {
				log().error( afpe );
			}
		}
		
		dbss = (IChannelDriver[])coreNode.getNodePpi( IChannelDriver.class );
		drivers.clear();
		for ( int i = 0; i < dbss.length; i++ ) {
			if ( dbss[i] instanceof IChannelDriverServer ) {
				if ( log().t4() ) log().info( "ADD " + dbss[i].getName() );
				drivers.put( dbss[i].getName(), (IChannelDriverServer)dbss[i] );
			} else
				log().info( "NOT A IChannelDriverServer: " + dbss[i].getName() );
		}
		
	}
	
	public void reloadInit(IChannel con ) throws MorseException {

		IQueryResult typesRes = con.query( new Query( defaultCon, "SELECT NAME FROM m_type @init" ) );
		while ( typesRes.next() ) {
			
			for ( Iterator<AfPlugin> i = initializedDbs.values().iterator(); i.hasNext(); ) {
				AfPlugin usedDb = i.next();
				
				if ( usedDb instanceof SqlDriver ) {
					IQueryResult res = con.query( new Query( defaultCon, "SELECT * FROM " + typesRes.getString( "NAME" ) + " @init" ) );
					((SqlDriver)usedDb).insertRecords( typesRes.getString( "NAME" ).toLowerCase(), res );
					res.close();
				}
			}
		}
		typesRes.close();
		
	}

	public IChannel getDb(String srcName) {
		if ( log().t4() ) log().info( "Load: " + srcName );
		return (IChannel)drivers.get( srcName );
	}

	public IConnection createConnection() throws MorseException {
		return new MyConnection( false );
	}

	class MyConnection implements IConnectionServer {

		private Hashtable<String,IChannelServer> channels = new Hashtable<String,IChannelServer>();
		private boolean autoCommit = true;
		private CacheList eventCache = new CacheList( EventEntry.class );
		private boolean defaultConnection;
		
		public MyConnection(boolean b) {
			defaultConnection = b;
		}

		public void close() {
			if ( defaultConnection ) return;
			synchronized ( channels ) {
				for ( Iterator<IChannelServer> i = channels.values().iterator(); i.hasNext(); ) {
					try {
						i.next().close();
					} catch ( Throwable t ) {
						log.error( t );
					}
				}
				channels = null;
			}
			
		}

		public IChannelServer getChannel(String srcName) throws MorseException {
			
			if ( channels == null )
				throw new MorseException( MorseException.ALREADY_CLOSED );
			
			if ( srcName == null )
				srcName = IChannelDriver.CT_DISTRIBUTOR;

			IChannelServer db = null;
			
			synchronized ( channels ) {
				db = channels.get( srcName );
				if ( db != null ) return db;
				
				IChannelDriverServer dbd = drivers.get( srcName );
				if ( dbd == null ) return null;
				
				try {
					db = dbd.createChannel( this );
				} catch (MorseException e) {
					return null;
				}
				channels.put( srcName, db );
			}
			return db;
		}

		public void commit() throws MorseException {
			
			if ( channels == null )
				throw new MorseException( MorseException.ALREADY_CLOSED );
			
			getObjectManager();
			
			synchronized ( transaction ) {
				
				while ( transaction.isTransaction() )
					try {
						transaction.wait();
					} catch ( IllegalMonitorStateException e ) { log.error( e ); }
					catch ( InterruptedException e ) {}
					
				internCommit();
				
			}
		}

		private void internCommit() {
			synchronized ( channels ) {
				for ( Iterator<IChannelServer> i = channels.values().iterator(); i.hasNext(); )
					i.next().commit();
				
				for ( Iterator i = eventCache.iterator(); i.hasNext(); ) {
					EventEntry entry = (EventEntry)i.next();
					switch ( entry.getT() ) {
					case EventEntry.CREATED:
						objectManager.eventObjectCreated( entry.getChannel(), entry.getId(), entry.getType() );
						break;
					case EventEntry.UPDATE:
						objectManager.eventObjectUpdated( entry.getChannel(), entry.getId(), entry.getType(), entry.getAttributes() );
						break;
					case EventEntry.DELETED:
						objectManager.eventObjectDeleted( entry.getChannel(), entry.getId(), entry.getType() );
						break;
					case EventEntry.SAVED:
						objectManager.eventContentSaved( entry.getChannel(), entry.getId(), entry.getParentId(), entry.getType() );
						break;
					case EventEntry.REMOVED:
						objectManager.eventContentRemoved( entry.getChannel(), entry.getId(), entry.getParentId(), entry.getType() );
						break;
						
					}
				}
				
				eventCache.clear();
			}
		}
		
		public boolean isAutoCommit() {
			return autoCommit;
		}

		public void rollback() throws MorseException {
			
			if ( channels == null )
				throw new MorseException( MorseException.ALREADY_CLOSED );
			
			synchronized ( transaction ) {
				while ( transaction.isTransaction() )
					try {
						transaction.wait();
					} catch ( IllegalMonitorStateException e ) { log.error( e ); }
					catch ( InterruptedException e ) {}
					
				internRollback();
				
			}
		}
		
		private void internRollback() {
			synchronized ( channels ) {
				for ( Iterator<IChannelServer> i = channels.values().iterator(); i.hasNext(); )
					i.next().rollback();
				eventCache.clear();
			}
		}

		public void setAutoCommit(boolean in) {
			synchronized ( transaction ) {
				while ( transaction.isTransaction() )
					try {
						transaction.wait();
					} catch ( IllegalMonitorStateException e ) { log.error( e ); }
					catch ( InterruptedException e ) {}
				autoCommit = in;
				synchronized ( channels ) {
					for ( Iterator<IChannelServer> i = channels.values().iterator(); i.hasNext(); )
						i.next().setAutoCommit( autoCommit );
				}
			}
		}
		
		public ITransaction startTransaction() throws MorseException {
			
			synchronized ( transaction ) {
				while ( transaction.isTransaction() )
					try {
						transaction.wait();
					} catch ( IllegalMonitorStateException e ) { log.error( e ); }
					catch ( InterruptedException e ) {}

				transaction.startTransaction();
					
				if ( autoCommit ) {
					if ( channels == null )
						throw new MorseException( MorseException.ALREADY_CLOSED );
					
					synchronized ( channels ) {
						for ( Iterator<IChannelServer> i = channels.values().iterator(); i.hasNext(); )
							i.next().setAutoCommit( false );
					}
				}
			}
			
			return transaction;
		}
		
		public void stopTransaction( ITransaction tr ) throws MorseException {
			if ( tr != transaction )
				throw new MorseException( MorseException.INVALID_TRANSACTION );
			if ( ! tr.isTransaction() )
				throw new MorseException( MorseException.TRANSACTION_NOT_RUNNING );
			
			synchronized ( transaction ) {
				synchronized ( channels ) {
					for ( Iterator<IChannelServer> i = channels.values().iterator(); i.hasNext(); )
						i.next().setAutoCommit( true );
				}
				transaction.stopTransaction();				
			}
		}
		
		public void maybeCommit( ITransaction tr ) throws MorseException {
			
			if ( tr != transaction )
				throw new MorseException( MorseException.INVALID_TRANSACTION );
			if ( ! tr.isTransaction() )
				throw new MorseException( MorseException.TRANSACTION_NOT_RUNNING );
			
			synchronized ( transaction ) {
				if ( autoCommit )
					internCommit();
				synchronized ( channels ) {
					for ( Iterator<IChannelServer> i = channels.values().iterator(); i.hasNext(); )
						i.next().setAutoCommit( true );
				}
				transaction.stopTransaction();
			}
		}

		public void maybeRollback( ITransaction tr ) throws MorseException {
			
			if ( tr != transaction )
				throw new MorseException( MorseException.INVALID_TRANSACTION );
			if ( ! tr.isTransaction() )
				throw new MorseException( MorseException.TRANSACTION_NOT_RUNNING );
			
			synchronized ( transaction ) {
				if ( autoCommit )
					internRollback();
				synchronized ( channels ) {
					for ( Iterator<IChannelServer> i = channels.values().iterator(); i.hasNext(); )
						i.next().setAutoCommit( true );
				}
				transaction.stopTransaction();
			}
		}

		public void eventContentSaved(String channel, String id, String parentId, String parentType) {
			eventCache.append( new EventEntry( EventEntry.SAVED, channel, id, parentType, new String[] { parentId } ) );
		}

		public void eventContentRemoved(String channel, String id, String parentId, String parentType) {
			eventCache.append( new EventEntry( EventEntry.REMOVED, channel, id, parentType, new String[] { parentId } ) );
		}
		
		public void eventObjectCreated(String channel, String id, String type) {
			eventCache.append( new EventEntry( EventEntry.CREATED, channel, id, type, null ) );
		}

		public void eventObjectDeleted(String channel, String id, String type) {
			eventCache.append( new EventEntry( EventEntry.DELETED, channel, id, type, null ) );			
		}

		public void eventObjectUpdated(String channel, String id, String type, String[] attributes) {
			eventCache.append( new EventEntry( EventEntry.UPDATE, channel, id, type, attributes ) );						
		}

		public IQueryResult fetch(String id, UserInformation user, boolean stamp ) throws MorseException {
			String channel = objectManager.findObject( id );
			return getChannel( channel ).fetch( id , user, stamp );
		}

		public boolean lock(String id, UserInformation user ) throws MorseException {
			String channel = objectManager.findObject( id );
			return getChannel( channel ).lock( id, user );
		}

		public void unlock(String id, boolean force, UserInformation user ) throws MorseException {
			String channel = objectManager.findObject( id );
			getChannel( channel ).unlock( id, force, user );
		}
		
		public IObjectManager getObjectManager() {
			return objectManager;
		}

		public IServer getServer() {
			return server;
		}

	}

	public IConnection getDefaultConnection() {
		return defaultCon;
	}
	
	public static class EventEntry extends CacheEntry {

		public static final int REMOVED = 4;
		public static final int SAVED = 3;
		public static final int DELETED = 0;
		public static final int UPDATE = 2;
		public static final int CREATED = 1;
		private String channel;
		private String id;
		private String type;
		private String[] attributes;
		private int t;

		public EventEntry( int t, String channel, String id, String type, String[] attributes ) {
			this.t = t;
			this.channel = channel;
			this.id = id;
			this.type = type;
			this.attributes = attributes;
		}

		public String getParentId() {
			return attributes[0];
		}

		public String[] getAttributes() {
			return attributes;
		}

		public String getType() {
			return type;
		}

		public String getChannel() {
			return channel;
		}

		public String getId() {
			return id;
		}

		public int getT() {
			return t;
		}

		@Override
		public void load(ObjectInputStream stream) throws IOException {
			t = stream.readInt();
			channel = stream.readUTF();
			id = stream.readUTF();
			type = stream.readUTF();
			int size = stream.readInt();
			if ( size == -1 )
				attributes = null;
			else {
				attributes = new String[ size ];
				for ( int i = 0; i < size; i++ )
					attributes[ i ] = stream.readUTF();
			}
		}

		@Override
		public void save(ObjectOutputStream stream) throws IOException {
			stream.write( t );
			stream.writeUTF( channel );
			stream.writeUTF( id );
			stream.writeUTF( type );
			if ( attributes == null )
				stream.write( -1 );
			else {
				stream.write( attributes.length );
				for ( int i = 0; i < attributes.length; i++ )
					stream.writeUTF( attributes[i] );
			}
		}
		
	}
	
	class MyTransaction implements ITransaction {

		private boolean transaction = false;
		
		public boolean isTransaction() {
			return transaction;
		}

		public void stopTransaction() {
			if ( log.t4() )
				log.info( "STOP" );
			transaction = false;
			this.notify();
		}

		public void startTransaction() {
			if ( log.t4() )
				log.info( "START" );
			transaction = true;
		}
		
	};

}
