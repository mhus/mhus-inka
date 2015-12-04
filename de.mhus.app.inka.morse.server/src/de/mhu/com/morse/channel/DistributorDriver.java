package de.mhu.com.morse.channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.PropertyQueryDefinition;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.utils.EmptyIterator;
import de.mhu.lib.utils.Properties;

/**
 * The distributor channel send the incommin queries to the default
 * channel for this kind of query. For this it needs all query
 * definitions from the default channels and at least the common
 * default choice line from default.properties. It is recomanded
 * that the default definitions not overlap and use the same static
 * identifier.
 * 
 * @author mike
 *
 */

public class DistributorDriver extends AfPlugin implements IChannelDriverServer {

	private static Config config = ConfigManager.getConfig( "server" );
	private static AL log = new AL( DistributorDriver.class );
	private QueryParser queryParser;
	private PropertyQueryDefinition qd = new PropertyQueryDefinition();

	private String name;
	private String acl;
	private IChannelProvider channelProvider;
	private IObjectManager objectManager;

	public IChannelServer createChannel(  IConnectionServer pConnection ) throws MorseException {
		return new MyChannel( pConnection );
	}

	public IChannelServer createChannel(IConnection pConnection) throws MorseException {
		if ( pConnection instanceof IConnectionServer )
			return createChannel( (IConnectionServer)pConnection );
		throw new MorseException( MorseException.ERROR, "NOT A Server Channel: " + pConnection );
	}

	public Properties getFeatures() {
		return new Properties();
	}

	public String getName() {
		return name;
	}

	public Iterator<String> getObjectIds() {
		return new EmptyIterator<String>();
	}

	public String getType() {
		return IChannelDriver.CT_DISTRIBUTOR;
	}

	public void setFeatures(Properties features) {
		
	}

	public void setAccessAcl(String in) {
		acl = in;
	}

	public void setChannel(String in) {
		name = in;
	}

	public QueryParser getParser() {
		if ( queryParser == null ) {
			try {
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/idx.properties" ) );
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/exec.properties" ) );
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/fs.properties" ) );
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/db.properties" ) );
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/statics.properties" ) );				
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/default.properties" ) );				
			} catch (IOException e) {
				log.error( e );
			}
			queryParser = new QueryParser( qd );
		}
		return queryParser;
	}
	
	public IChannelProvider getChannelProvider() {
		if ( channelProvider == null )
			channelProvider = (IChannelProvider)getSinglePpi( IChannelProvider.class );
		return channelProvider;
	}   
	
	class MyChannel implements IChannelServer {

		private IConnectionServer connection;

		public MyChannel(IConnectionServer pConnection) {
			connection = pConnection;
		}

		public void close() {
			
		}

		public byte[] getDefinition() {
			getParser();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				qd.store(baos );
			} catch (IOException e) {
				log.error( e );
				return null;
			}
			return baos.toByteArray();
		}

		public String getName() {
			return name;
		}

		public QueryParser getParser() {
			return DistributorDriver.this.getParser();
		}

		public IQueryResult query(Query in) throws MorseException {
			return query( in ,null );
		}
		
		public IQueryResult query(Query in, UserInformation user) throws MorseException {

			switch ( in.getCode().getInteger( 0 ) ) {
			case CMql.SELECT:
			case CMql.INSERT:
			case CMql.UPDATE:
			case CMql.DELETE:
			case CMql.RENDITION:
				return ((IChannelServer)in.getConnection().getChannel( "db" )).query( in, user );
			case CMql.FETCH:
				return ((IChannelServer)in.getConnection().getChannel( objectManager.findObject( in.getCode().getString( 1 ) ) )).query( in, user );
			case CMql.LOAD:
				return ((IChannelServer)in.getConnection().getChannel( objectManager.findObject( in.getCode().getString( 1 ) ) )).query( in, user );
			case CMql.SAVE:
				return ((IChannelServer)in.getConnection().getChannel( "fs" )).query( in, user );
			case CMql.EXEC:
				return ((IChannelServer)in.getConnection().getChannel( "exec" )).query( in, user );
			case CMql.INDEX:
				return ((IChannelServer)in.getConnection().getChannel( "idx" )).query( in, user );
				
			default:
				throw new MorseException( MorseException.QUERY_UNSUPPORTED );	
			}
		}

		public IQueryResult fetch(String id, UserInformation user, boolean stamp) throws MorseException {
			IChannel channel = getChannelProvider().getDefaultConnection().getChannel( objectManager.findObject( id ) );
			if ( channel instanceof IChannelServer )
				return ((IChannelServer)channel).fetch( id, user, stamp );
			return null;
		}

		public IConnectionServer getConnection() {
			return connection;
		}

		public void commit() {
			
		}

		public void rollback() {
			
		}

		public boolean lock(String id, UserInformation user) throws MorseException {
			IChannel channel = getChannelProvider().getDefaultConnection().getChannel( objectManager.findObject( id ) );
			if ( channel instanceof IChannelServer )
				return ((IChannelServer)channel).lock( id, user );
			return false;
		}

		public void unlock(String id, boolean force, UserInformation user) throws MorseException {
			IChannel channel = getChannelProvider().getDefaultConnection().getChannel( objectManager.findObject( id ) );
			if ( channel instanceof IChannelServer )
				((IChannelServer)channel).unlock( id, force, user );
		}

		public void store(IObjectRead obj, boolean commit, UserInformation user) throws MorseException {
			throw new MorseException( MorseException.NOT_SUPPORTED );
		}

		public void setAutoCommit(boolean b) {
		}
		
	}

	@Override
	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void apDisable() throws AfPluginException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void apEnable() throws AfPluginException {
		objectManager = (IObjectManager)getSinglePpi( IObjectManager.class );
	}

	@Override
	protected void apInit() throws Exception {
		appendPpi( IChannelDriver.class, this );
	}

	public String toValidDate(Date date) {
		return null;
	}

	public boolean canTransaction() {
		return false;
	}

	public void setChannelFeatures(Map<String, String> features) throws MorseException {
		
	}
	
}
