package de.mhu.com.morse.channel.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.mhu.lib.ACast;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.channel.IChannelDriverServer;
import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.PropertyQueryDefinition;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.utils.EmptyIterator;
import de.mhu.lib.utils.Properties;

public class ExecDriver extends AfPlugin implements IChannelDriverServer {

	private static AL log = new AL( ExecDriver.class );
	private static Config config = ConfigManager.getConfig( "server" );
	
	private QueryParser queryParser;
	private PropertyQueryDefinition qd = new PropertyQueryDefinition();
	private IAclManager aclManager;
	private String name;
	private String type = IChannelDriver.CT_EXEC;
	private String accessAcl;
	
	@Override
	protected void apDestroy() throws Exception {
		
	}

	@Override
	protected void apDisable() throws AfPluginException {
		
	}

	@Override
	protected void apEnable() throws AfPluginException {
		aclManager = (IAclManager)getSinglePpi( IAclManager.class );
	}

	@Override
	protected void apInit() throws Exception {
		appendPpi( IChannelDriver.class, this );
	}

	public IChannelServer createChannel(IConnectionServer pConnection)
			throws MorseException {

		return new MyChannel( pConnection );
		
	}

	public void setAccessAcl(String in) {
		accessAcl = in;
	}

	public void setChannel(String in) {
		name = in;
	}

	public String toValidDate(Date date) {
		return null;
	}

	public IChannel createChannel(IConnection pConnection)
			throws MorseException {
		return null;
	}

	public Properties getFeatures() {
		return null; //TODO
	}

	public String getName() {
		return name;
	}

	public Iterator<String> getObjectIds() {
		return new EmptyIterator<String>();
	}

	public String getType() {
		return type;
	}

	public void setFeatures(Properties features) {
		// TODO Auto-generated method stub
	}

	public QueryParser getParser() {
		if ( queryParser == null ) {
			try {
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/exec.properties" ) );
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/statics.properties" ) );				
			} catch (IOException e) {
				log().error( e );
			}
			queryParser = new QueryParser( qd );
		}
		return queryParser;
	}
	
	Properties getQueryParserProperties() {
		return qd;
	}
	
	IAclManager getAclManager() {
		return aclManager;
	}
	
	public boolean canTransaction() {
		return false;
	}

	private class MyChannel implements IChannelServer {

		private IConnectionServer con;

		public MyChannel(IConnectionServer connection) {
			con = connection;
		}

		public void commit() {
			
		}

		public IQueryResult fetch(String id, UserInformation user, boolean stamp) throws MorseException {
			return null;
		}

		public IConnectionServer getConnection() {
			return con;
		}

		public byte[] getDefinition() {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				getQueryParserProperties().store(baos );
			} catch (IOException e) {
				log.error( e );
				return null;
			}
			return baos.toByteArray();
		}

		public IQueryResult query(Query in) throws MorseException {
			return query( in, null );
		}
		
		public IQueryResult query(Query in, UserInformation user) throws MorseException {
			
			if ( ! getAclManager().hasRead(user, accessAcl ) )
				throw new MorseException( MorseException.ACCESS_DENIED_READ, new String[] { "channel", getName(), accessAcl } );
			
			ICompiledQuery code = in.getCode();
			if ( code.size() == 0 )
				throw new MorseException( MorseException.QUERY_EMPTY );
			
			
			switch ( code.getInteger( 0 ) ) {
			case CMql.EXEC:
				return queryExec( code, user );
			default:
				throw new MorseException( MorseException.QUERY_UNSUPPORTED );
			}
		}

		private IQueryResult queryExec(ICompiledQuery code, UserInformation user) throws MorseException {
			
			int off = 1;
			boolean async = false;
			
			if ( code.getInteger( off ) == CMql.ASYNC ) { 
				async = true;
				off++;
			}
			
			String functionName = code.getString( off );
			off++;
			
			LinkedList<Object> attr = new LinkedList<Object>();
			
			if ( off < code.size() ) {
				off++; // (
				
				while ( code.getInteger( off ) != CMql.CLOSE ) {
					String type = null;
					String value = code.getString( off );
					off++;
					if ( code.getInteger( off ) == CMql.AS ) {
						off++;
						type = code.getString( off );
						off++;
					}
					if ( type == null ) {
						if ( value.length() > 1 && value.startsWith( "'" ) && value.endsWith( "'") )
							attr.add( value.substring( 1, value.length() - 1 ) );
						else
							attr.add( value );
					} else
					if ( "string".equals( type ) )
						attr.add( value );
					else
					if ( "int".equals( type ) )
						attr.add( Integer.parseInt( value ) );
					else
					if ( "long".equals( type ) )
						attr.add( Long.parseLong( value ) );
					else
					if ( "double".equals( type ) )
						attr.add( Double.parseDouble( value ) );
					else
					if ( "boolean".equals( type ) )
						attr.add( ACast.toboolean( value, false ) );
					else
						throw new MorseException( MorseException.ATTR_TYPE_UNKNOWN, type );
					
					
					if ( code.getInteger( off ) == CMql.COMMA )
						off++;
				}

			}
			
			IExecFunction functionObject = (IExecFunction)getConnection().getServer().loadFunction( getConnection(), "exec." + functionName );
			functionObject.initFunction( getConnection(), getAclManager(), user );
			
			return functionObject.exec( attr, async );
			
		}

		public void rollback() {
		}

		public void close() {
			
		}

		public String getName() {
			return name;
		}

		public QueryParser getParser() {
			return ExecDriver.this.getParser();
		}

		public boolean lock(String id, UserInformation user) throws MorseException {
			// TODO Auto-generated method stub
			return false;
		}

		public void unlock(String id, boolean force, UserInformation user) throws MorseException {
			// TODO Auto-generated method stub
			
		}

		public void store(IObjectRead obj, boolean commit, UserInformation user) throws MorseException {
			throw new MorseException( MorseException.NOT_SUPPORTED );
		}

		public void setAutoCommit(boolean b) {
			// TODO Auto-generated method stub
			
		}
		
	}

	public void setChannelFeatures(Map<String, String> features) throws MorseException {
		// TODO Auto-generated method stub
		
	}
	
}
