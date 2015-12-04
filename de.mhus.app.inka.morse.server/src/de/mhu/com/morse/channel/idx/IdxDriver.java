package de.mhu.com.morse.channel.idx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.channel.IChannelDriverServer;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IObjectManager;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.PropertyQueryDefinition;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.utils.EmptyIterator;
import de.mhu.lib.utils.Properties;

public class IdxDriver extends AfPlugin implements IChannelDriverServer {

	private static Config config = ConfigManager.getConfig( "server" );
	
	private IAclManager aclManager;
	private ITypes types;
	private Map<String, String> properties;
	private Properties features = new Properties();
	private String name;
	private String accessAcl;
	private Hashtable<String, IIdx> indexes;
	private QueryParser queryParser;
	private PropertyQueryDefinition qd = new PropertyQueryDefinition();

	private IObjectManager objectManager;

	private IChannelProvider channelProvider;
	
	@Override
	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void apDisable() throws AfPluginException {

		if ( indexes == null ) return;
		Object[] list = indexes.values().toArray();
		for ( int i = 0; i < list.length; i++ )
			try {
				((IIdx)list[i]).closeIndex();
			} catch ( Exception e ) {
				log().error( e );
			}
			
		indexes.clear();
		indexes = null;
			
	}

	public IChannelProvider getChannelProvider() {
		if ( channelProvider == null )
			channelProvider = (IChannelProvider)getSinglePpi( IChannelProvider.class );
		return channelProvider;
	}
	
	@Override
	protected void apEnable() throws AfPluginException {
		aclManager = (IAclManager)getSinglePpi( IAclManager.class );
		objectManager = (IObjectManager)getSinglePpi( IObjectManager.class );
		types = (ITypes)getSinglePpi( ITypes.class );
		IChannelProvider provider = (IChannelProvider)getSinglePpi( IChannelProvider.class );
		if ( provider == null ) return;
		
		IConnection connection = provider.getDefaultConnection();
		indexes = new Hashtable<String, IIdx>();
		
		String mql = "SELECT ** FROM m_idx where channel='@" + getName() + "' @sys";
		IQueryResult res = new Query( connection, mql ).execute();
		while ( res.next() ) {
			String name = res.getString( "name" );
			log().info( "ADD " + name );
			try {
				String functionName = "idx." + res.getString( "function" );
				IIdx function = (IIdx)connection.getServer().loadFunction( connection, functionName );
				if ( function ==  null ) {
					log().error( "Function not found " + functionName + " for index " + name );
				} else {
					function.initIndex( this, ObjectUtil.tableToMap( res.getTable( "features" ), "k", "v" ) );
					indexes.put( name, function );
				}
			} catch ( Exception e ) {
				log().error( e );
			}
		}
		res.close();
		
	}

	@Override
	protected void apInit() throws Exception {
		appendPpi( IChannelDriver.class, this );
	}

	public boolean canTransaction() {
		return false;
	}

	public IChannel createChannel(IConnection pConnection) throws MorseException {
		return new MyChannel( pConnection );
	}

	public IChannelServer createChannel(IConnectionServer pConnection) throws MorseException {
		return new MyChannel( pConnection );
	}

	public void setAccessAcl(String in) {
		accessAcl = in;
	}

	public void setChannel(String in) {
		name = in;
	}

	public String toValidDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getFeatures() {
		return features;
	}

	public String getName() {
		return name;
	}

	public Iterator<String> getObjectIds() {
		return new EmptyIterator<String>();
	}

	public String getType() {
		return IChannelDriver.CT_IDX;
	}

	public void setFeatures(Properties features) {
		this.features = features;
	}

	public void setChannelFeatures(Map<String, String> features) throws MorseException {
		properties = features;
	}

	public QueryParser getParser() {
		if ( queryParser == null ) {
			try {
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/idx.properties" ) );
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/statics.properties" ) );				
			} catch (IOException e) {
				log().error( e );
			}
			queryParser = new QueryParser( qd );
		}
		return queryParser;
	}

	class MyChannel implements IChannelServer {
		
		private IConnection connection;
		
		public MyChannel(IConnection pConnection) {
			connection = pConnection;
		}

		public void commit() {
		}

		public IQueryResult fetch(String id, UserInformation user, boolean stamp)
				throws MorseException {
			return null;
		}

		public IConnectionServer getConnection() {
			if ( connection instanceof IConnectionServer )
				return (IConnectionServer)connection;
			return null;
		}

		public byte[] getDefinition() {
			IdxDriver.this.getParser();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				qd.store(baos );
			} catch (IOException e) {
				log().error( e );
				return null;
			}
			return baos.toByteArray();
		}

		public boolean lock(String id, UserInformation user)
				throws MorseException {
			return false;
		}

		public IQueryResult query(Query in, UserInformation user)
				throws MorseException {
			// TODO Auto-generated method stub
			ICompiledQuery code = in.getCode();
			
			// collect names
			int off = 1; // INDEX
			LinkedList<String> names = new LinkedList<String>();
			while ( off < code.size() && code.getInteger( off ) == CMql.NaN ) {
				names.add( code.getString( off ) );
				off++;
				if ( off < code.size() && code.getInteger( off ) == CMql.COMMA )
					off++;
			}
			
			// find index
			IIdx idx = indexes.get( names.get( 0 ) );
			if ( idx == null )
				throw new MorseException( MorseException.IDX_NOT_FOUND, names.get( 0 ) );
			
			// SELECT
			if ( code.getInteger( off ) == CMql.SELECT ) {
				// collect attributes
				LinkedList<String> attributes = new LinkedList<String>();
				off++;
				while ( off < code.size() && code.getInteger( off ) == CMql.NaN ) {
					attributes.add( code.getString( off ) );
					off++;
					if ( off < code.size() && code.getInteger( off ) == CMql.COMMA )
						off++;
				}
				
				// collect WHERE
				LinkedList<String[]> where = new LinkedList<String[]>();
				if ( off < code.size() && code.getInteger( off ) == CMql.WHERE ) {
					off++;
					while ( off < code.size() && code.getInteger( off ) == CMql.NaN ) {
						where.add( new String[] {  code.getString( off ), code.getString( off + 2 ) } );
						off+=3;
						if ( off < code.size() && code.getInteger( off ) == CMql.COMMA )
							off++;
					}
				}
				
				return idx.select( getConnection(), user, names, attributes, where );
			} else
			if ( code.getInteger( off ) == CMql.REBUILD ) {
				return idx.rebuild( getConnection(), user, names );
			}
			
			throw new MorseException( MorseException.ERROR, names.toString() );
		}

		public void rollback() {
		}

		public void setAutoCommit(boolean b) {
		}

		public void store(IObjectRead obj, boolean commit, UserInformation user)
				throws MorseException {
		}

		public void unlock(String id, boolean force, UserInformation user)
				throws MorseException {
		}

		public void close() {
		}

		public String getName() {
			return IdxDriver.this.getName();
		}

		public QueryParser getParser() {
			return IdxDriver.this.getParser();
		}

		public IQueryResult query(Query in) throws MorseException {
			return query( in, null );
		}
		
	}

	public IObjectManager getObjectManager() {
		return objectManager;
	}
	
	public ITypes getTypeManager() {
		return types;
	}

	public IAclManager getAclManager() {
		return aclManager;
	}
	
}
