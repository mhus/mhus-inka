package de.mhu.com.morse.channel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.mhu.lib.ACast;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.init.InitStoreManager;
import de.mhu.com.morse.mql.ErrorResult;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.PropertyQueryDefinition;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.SingleRowResult;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.types.Types;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.AfPluginNode;
import de.mhu.lib.utils.EmptyIterator;
import de.mhu.lib.utils.Properties;

public class InitialChannelDriver extends AfPlugin implements IChannelDriverServer {

	private static Config config = ConfigManager.getConfig( "server" );
	
	private static AL log = new AL( InitialChannelDriver.class );
	private File saveStore;
	private InitStoreManager storeManager = null;
	private QueryParser queryParser;
	private PropertyQueryDefinition qd = new PropertyQueryDefinition();
	private Properties features;
	private String name;
 
	/*
	public void init( String dir ) throws Exception {
		
		store = new File( dir );
		 
	}
	*/
	
	public IChannelServer createChannel(IConnectionServer pConnection) throws MorseException {
		return new Connection();
	}
	
	public IChannel createChannel( IConnection pConnection ) {
		return new Connection();
	}	

	public String getName() {
		return name;
	}
	
	class Connection implements IChannelServer {

		private String canStore;

		public IQueryResult query( Query in ) throws MorseException {
			return query( in, null );
		}
		
		public IQueryResult query( Query in, UserInformation user ) throws MorseException {
			// select * from m_type (WHERE object_id='1234')
			
			ICompiledQuery code = in.getCode();
			if ( code.size() == 0 )
				throw new MorseException( MorseException.QUERY_EMPTY );
			
			switch ( code.getInteger(0) ) {
			case CMql.SELECT:
				return querySelect( code );
			case CMql.RENDITION:
				return queryLoad( code );
			case CMql.EXEC:
				return queryExec( code, user );
			default:
				throw new MorseException( MorseException.QUERY_UNSUPPORTED );
			}
		}

		private IQueryResult queryExec(ICompiledQuery code, UserInformation user) throws MorseException {
			if ( user != null && ! user.isAdministrator() )
				return new ErrorResult( MorseException.ACCESS_DENIED, 0, "" );
			
			String cmd = code.getString( 1 );
			if ( cmd.equals( "reloadinit" ) ) {
				ChannelProvider cp = (ChannelProvider)getSinglePpi( IChannelProvider.class );
				cp.reloadInit( this );
			} else
			if ( cmd.equals( "exit" ) ) {
				AfPluginNode root = getApParent();
				while ( root.getApParent() != null )
					root = root.getApParent();
				String[] plugins = root.getChildPluginNames();
				for ( int i = 0; i < plugins.length; i++ ) {
					try {
						root.disablePlugin( plugins[i] );
					} catch ( Exception e ) {
						log.info( e );
					}
					try {
						root.removePlugin( plugins[i] );
					} catch ( Exception e ) {
						log.info( e );
					}
				}
				System.exit( 0 );
			} else
			if ( cmd.equals( "close" ) ) {
				AfPluginNode root = getApParent();
				while ( root.getApParent() != null )
					root = root.getApParent();
				String[] plugins = root.getChildPluginNames();
				for ( int i = 0; i < plugins.length; i++ ) {
					try {
						root.disablePlugin( plugins[i] );
					} catch ( Exception e ) {
						log.info( e );
					}
					try {
						root.removePlugin( plugins[i] );
					} catch ( Exception e ) {
						log.info( e );
					}
				}
			} else
			if ( cmd.equals( "reloadtypes" ) ) {
				Types types = (Types)getSinglePpi( ITypes.class );
				try {
					types.getApParent().disablePlugin( "types" );
					types.getApParent().enablePlugin( "types" );
				} catch (AfPluginException e) {
					log.error( e );
				}
			} else
			if ( cmd.equals( "reloadchannels" ) ) {
				ChannelProvider cp = (ChannelProvider)getSinglePpi( IChannelProvider.class );
				try {
					cp.apDisable();
					cp.apEnable();
				} catch (AfPluginException e) {
					log.error( e );
				}
			} else
			if ( cmd.equals( "reloadids" ) ) {
				ObjectManager om = (ObjectManager)getSinglePpi( IObjectManager.class );
				om.refresh();
			} else
			if ( cmd.equals( "store" ) ) {
				if ( code.size() > 3 )
					canStore = code.getString( 3 );
				else
					canStore = null;
			} else
				throw new MorseException( MorseException.FUNCTION_NOT_FOUND, cmd );
			
			return new ErrorResult( 0, 0, null );
		}

		private IQueryResult queryLoad(ICompiledQuery code) throws MorseException {
			/*
			String id = code.getString( 1 );
			File f = new File( store, "_content/" + id + ".dat" );
			
			return new InternalLoadResult( f );
			*/
			return null;
		}

		private IQueryResult querySelect(ICompiledQuery code) throws MorseException {
			
			LinkedList attr = new LinkedList();
			int off = 1;
			attr.add( code.getString(off) );
			off++;
			while ( code.getInteger(off) == CMql.COMMA ) {
				off++;
				attr.add( code.getString(off) );
				off++;
			}
			
			off++; // FROM
			
			String table = code.getString(off);
			off++;
			
			String whereAttr = null;
			String whereVal  = null;
			
			if ( code.size() > off ) {
				whereAttr = code.getString(off+1).toUpperCase();
				whereVal  = code.getString(off+3);
			}
			
			// File tableDir = new File( store, table );
			List<String> tableList = storeManager.get( table );
			if ( tableList == null )
				throw new MorseException( MorseException.TABLE_NOT_FOUND, table );
			
			if ( attr.size() == 1 && attr.get( 0 ).equals( "*" ) )
				attr.clear();
			
			return new SelectResult( table, tableList, whereAttr, whereVal, (String[])attr.toArray( new String[ attr.size() ]) );
		}

		public void close() {
			// TODO Auto-generated method stub
			
		}

		public void store(IObjectRead obj, boolean commit, UserInformation user) throws MorseException {
			if ( canStore == null || user != null && ! user.isAdministrator() )
				throw new MorseException( MorseException.ACCESS_DENIED );
			String typeName = obj.getString( IAttribute.M_TYPE );
			if ( typeName == null )
				throw new MorseException( MorseException.UNKNOWN_TYPE );
			
			List<String> tableList = storeManager.get( typeName );
			if ( tableList == null )
				throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );
			
			ITypes types = (ITypes)getSinglePpi( ITypes.class );
			IType type = types.get( typeName );
			if ( type == null )
				throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );

			Properties props = new Properties();
			
			// fill properties
			for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
				IAttribute attr = i.next();
				if ( attr.isTable() ) {
					ITableRead table = obj.getTable( attr.getName() );
					table.reset();
					int cnt = 0;
					while ( table.next() ) {
						for ( Iterator<IAttribute> j = attr.getAttributes(); j.hasNext(); ) {
							IAttribute tAttr = j.next();
							String value = table.getString( tAttr.getName() );
							if ( value == null ) value="";
							props.setProperty( attr.getName().toUpperCase() + '.' + cnt + tAttr.getName().toUpperCase(), value );
						}
						cnt++;
					}
					props.setProperty( attr.getName().toUpperCase() + ".SIZE", String.valueOf( cnt ) );
				} else {
					String value = obj.getString( attr.getName() );
					if ( value == null ) value="";
					props.setProperty( attr.getName().toUpperCase(), value );
				}
			}
			props.setProperty( "_DESTINATION", canStore );
			
			// find new object id
			
			String mid = "_" + type.getName() + "_";
			if ( props.getProperty( "NAME" ) != null )
				mid = mid + props.getProperty( "NAME" );
			else
				mid = mid + props.getProperty( IAttribute.M_ID.toUpperCase());
			if ( mid.length() > 29 ) mid = mid.substring( 0, 29 );
			int cnt = 0;
			do {
				String name = mid;
				if ( cnt > 0 ) name = name + '_' + Integer.toHexString( cnt );
				while ( name.length() < 32 ) name = name + '_';
				if ( ! new File( saveStore, IType.TYPE_OBJECT + '/' + name + ".txt" ).exists() ) {
					mid = name;
					break;
				}
				if ( cnt > 255 ) {
					mid = props.getProperty( IAttribute.M_ID.toUpperCase());
					break;
				}
			} while ( true );
			props.setProperty( IAttribute.M_ID.toUpperCase(), mid );
			
			// store
			
			IType t = type;
			while ( t != null ) {
				File tableDir = new File( saveStore, t.getName() );
				store( tableDir, props.getProperty( IAttribute.M_ID.toUpperCase()), props );
				t = t.getSuperType();
			}
		}
		
		private void store( File typeStore, String id, Properties p) {
			
			try {
				FileOutputStream fos = new FileOutputStream( new File( typeStore, id + ".txt" ) );
				p.store( fos );
				fos.close();
				
			} catch ( Exception e ) {
				log.error( e );
			}
		}

		public String getName() {
			return name;
		}

		public synchronized QueryParser getParser() {
			if ( queryParser == null ) {
				try {
					String resName = config.getProperty( "resource.package" ) + "/InitialDb.properties";
					InputStream res = getClass()
					.getClassLoader()
					.getResourceAsStream( resName );
					qd.load( res );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error( e );
				}
				queryParser = new QueryParser( qd );
			}
			return queryParser;
		}

		public byte[] getDefinition() {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				qd.store(baos );
			} catch (IOException e) {
				log.error( e );
				return null;
			}
			return baos.toByteArray();
		}

		public IQueryResult fetch(String id, UserInformation user, boolean stamp) throws MorseException {
			
			List<String> tableList = storeManager.get( IType.TYPE_OBJECT );
			
			if ( ! tableList.contains( id ) )
				throw new MorseException( MorseException.OBJECT_NOT_FOUND, id );
			
			Properties current = storeManager.fetch( id );
			if ( stamp ) {
				return new SingleRowResult( new IAttribute[] { IAttributeDefault.ATTR_OBJ_INT }, new String[] { IAttribute.M_STAMP }, new String[] { current.getProperty( IAttribute.M_STAMP )} );
			}
			
			String type = current.getProperty( IAttribute.M_TYPE.toUpperCase() );

			tableList = storeManager.get( type );
			return new SelectResult( type, tableList, IAttribute.M_ID, id, new String[] { "*" } );
		}

		public IConnectionServer getConnection() {
			// TODO Auto-generated method stub
			return null;
		}

		public void commit() {
			// TODO Auto-generated method stub
			
		}

		public void rollback() {
			// TODO Auto-generated method stub
			
		}

		public boolean lock(String id, UserInformation user) throws MorseException {
			// TODO Auto-generated method stub
			return false;
		}

		public void unlock(String id, boolean force, UserInformation user) throws MorseException {
			// TODO Auto-generated method stub
			
		}

		public void setAutoCommit(boolean b) {
			// TODO Auto-generated method stub
			
		}

	}
	
	class SelectResult implements IQueryResult {

		private String[] list;
		private String whereAttr;
		private String whereVal;
		private int listPos;
		private Properties current = null;
		private String[] attr;
		private String type;

		public SelectResult(String ptype, List<String> pTableList, String pWhereAttr, String pWhereVal, String[] pAttr ) {
			
			if ( IAttribute.M_ID.equals( pWhereAttr ) ) {
				list = new String[] { pWhereVal };
			} else {
				list = pTableList.toArray( new String[ pTableList.size() ] );
				whereAttr = pWhereAttr;
				whereVal  = pWhereVal;
			}
			listPos = 0;
			attr = pAttr;
			type = ptype;
		}
		
		public String getString( String name )  throws MorseException {
			return current.getProperty( name.toUpperCase() );
		}
		
		public String getString( int pos )  throws MorseException {
			return current.getProperty( attr[ pos ] );
		}
		
		public ITableRead getTable( String name ) throws MorseException {
			return new MyTable( this, name );
		}
		
		public int getSize( String name ) {	
			try {
				return Integer.parseInt( current.getProperty( name.toUpperCase() + ".SIZE" ) );
			} catch ( NumberFormatException e ) {
				return 0;
			}
		}
		
		public String getString( String table, int pos, String name ) {
			return current.getProperty( table.toUpperCase() + '.' + pos + '.' + name.toUpperCase() );
		}
		
		public boolean next() throws MorseException {
						
			while ( true ) {
				
				if ( listPos >= list.length ) return false;

				try {
					current = storeManager.fetch( list[ listPos ] );					
					listPos++;
					
					if ( whereAttr != null ) {
						if ( whereVal.equals( current.getProperty( whereAttr ) ) )
							return true;
					} else
						return true;
					
				} catch ( Exception e ) {
					listPos = list.length;
					throw new MorseException( 0, e );
				}
				
				
			}
			
		}

		public String[] getColumns() throws MorseException {
			return attr;
		}

		public ITableRead getTable(int index) throws MorseException {
			return getTable( attr[ index ] );
		}

		public IAttribute getAttribute(int i) throws MorseException {
			return null;
		}

		public void close() {
			// TODO Auto-generated method stub
			
		}

		public int getErrorCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getErrorInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		public long getReturnCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		public Date getDate(String string) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public Date getDate(int index) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public double getDouble(String string) throws MorseException {
			return Double.parseDouble( getString( string ) );
		}

		public double getDouble(int index) throws MorseException {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getInteger(String string) throws MorseException {
			return Integer.parseInt( getString( string ) );
		}

		public int getInteger(int index) throws MorseException {
			// TODO Auto-generated method stub
			return 0;
		}

		public long getLong(String string) throws MorseException {
			return Long.parseLong( getString( string ) );
		}

		public long getLong(int index) throws MorseException {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean getBoolean(int index) throws MorseException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean getBoolean(String string) throws MorseException {
			return ACast.toboolean( getString( string ), false );
		}

		public int getPreferedQuereType() {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getAttributeCount() {
			return 0;
		}

		public IAttribute getAttribute(String name) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean reset() throws MorseException {
			// TODO Auto-generated method stub
			return false;
		}

		public InputStream getInputStream() throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public OutputStream getOutputStream() throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getObject(int index) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getObject(String name) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

	}

	class MyTable implements ITableRead {
		int cnt = -1;
		int pos = 0;
		String name;
		private SelectResult result;
		
		MyTable( SelectResult pResult, String pName ) {
			name = pName;
			result = pResult;
			cnt = result.getSize( name );
		}
		
		public String getString(String name2) throws MorseException {
			return result.getString( name, pos-1, name2 );
		}

		public boolean next() throws MorseException {
			if ( pos >= cnt ) return false;
			pos++;
			return true;
		}

		public String[] getColumns() throws MorseException {
			return new String[0]; // TODO
		}

		public String getString(int i) throws MorseException {
			return ""; // TODO
		}

		public IAttribute getAttribute(int i) throws MorseException {
			return null;
		}

		public void close() {
		}

		public Date getDate(String string) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public Date getDate(int index) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public double getDouble(String string) throws MorseException {
			return Double.parseDouble( getString( string ) );
		}

		public double getDouble(int index) throws MorseException {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getInteger(String string) throws MorseException {
			return Integer.parseInt( getString( string ) );
		}

		public int getInteger(int index) throws MorseException {
			// TODO Auto-generated method stub
			return 0;
		}

		public long getLong(String string) throws MorseException {
			return Long.parseLong( getString( string ) );
		}

		public long getLong(int index) throws MorseException {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean getBoolean(int index) throws MorseException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean getBoolean(String string) throws MorseException {
			return ACast.toboolean( getString( string ), false );
		}

		public int getAttributeCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		public IAttribute getAttribute(String name) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean reset() throws MorseException {
			// TODO Auto-generated method stub
			return false;
		}

		public Object getObject(int index) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getObject(String name) throws MorseException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public String getType() {
		return "sys";
	}

	public Iterator<String> getObjectIds() {
		return new EmptyIterator<String>();
	}

	public Properties getFeatures() {
		return features;
	}

	public void setFeatures(Properties features) {
		this.features = features;
	}

	protected void apDestroy() throws Exception {
		
	}

	protected void apDisable() throws AfPluginException {
		
	}

	protected void apEnable() throws AfPluginException {
		// TODO Auto-generated method stub
		
	}

	protected void apInit() throws Exception {
		// init( config.getProperty( "init.db.store" ) );
		reloadTypes();
		appendPpi( IChannelDriver.class, this );
	}


	public void setAccessAcl(String in) {
		// TODO Auto-generated method stub
		
	}

	public void setChannel(String in) {
		// TODO Auto-generated method stub
		
	}

	public void setPass(String in) {
		// TODO Auto-generated method stub
		
	}

	public void setPath(String in) {
		// TODO Auto-generated method stub
		
	}

	public void setUser(String in) {
		// TODO Auto-generated method stub
		
	}

	public String toValidDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canTransaction() {
		return false;
	}

	public void setChannelFeatures(Map<String, String> features) throws MorseException {
		// TODO Auto-generated method stub
		
	}

	public void reloadTypes() {
		storeManager = new InitStoreManager();
		name = config.getProperty( "init.db.name" );
	}
	
	
	
}
