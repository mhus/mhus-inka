package de.mhu.com.morse.channel.fs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Map;
import de.mhu.lib.utils.Properties;

import de.mhu.lib.ACast;
import de.mhu.lib.AFile;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.io.SizeCountWriter;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IChannelDriverServer;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IObjectManager;
import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.channel.sql.SqlUtils;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.PropertyQueryDefinition;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectFileStore;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.com.morse.utils.ServerTypesUtil;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.utils.EmptyIterator;
import de.mhu.lib.utils.Properties;
import de.mhu.lib.utils.SingleIterator;

public class FileAbstractDriver extends AfPlugin implements IChannelDriverServer {

	private static Config config = ConfigManager.getConfig( "server" );
	
	protected static final FilenameFilter filter = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			return ( name.length() == 36 && name.endsWith( ".txt" ) );
		}
		
	};
	
	private File root;
	private Hashtable<String,ObjectFileStore[]> fileStore = new Hashtable<String, ObjectFileStore[]>();
	private QueryParser queryParser;
	private PropertyQueryDefinition qd = new PropertyQueryDefinition();
	private String name;
	private ITypes typeProvider;
	private String type = "fs";
	private IObjectManager objectManager;

	private IChannelProvider channelProvider;

	private IAclManager aclManager;

	private String accessAcl;

	public FileAbstractDriver() {	
	}
	
	public FileAbstractDriver( File pStore ) {
		root = pStore;
	}
	
	protected void apDestroy() throws Exception {
		
	}

	protected void apDisable() throws AfPluginException {
		// TODO Auto-generated method stub
		
	}

	protected void apEnable() throws AfPluginException {
		typeProvider = (ITypes)getSinglePpi( ITypes.class );
		objectManager = (IObjectManager)getSinglePpi( IObjectManager.class );
		aclManager = (IAclManager)getSinglePpi( IAclManager.class );
		
		Properties p = getFeatures();
		Hashtable knownTypes = new Hashtable();
		for ( Iterator i = p.keySet().iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			if ( key.startsWith( "type." ) )
				knownTypes.put( key, p.getProperty( key ) );
		}
		for ( Iterator i = knownTypes.keySet().iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			p.remove( key );
		}
		
		for ( Iterator i = typeProvider.getTypes(); i.hasNext(); ) {
			IType type = (IType)i.next();
			if ( type.isInChannel( this ) ) {
				p.setProperty( "type." + type.getName(), type.getName() );
				knownTypes.remove( "type." + type.getName() );
				
				File tableDir = new File( root, type.getName() + "/index" );
				if ( ! tableDir.exists() ) {
					log().info( "Create Type " + type.getName() );
					if ( ! tableDir.mkdirs() ) {
						log().error( "Can't create dir " + tableDir );
					}
				}
				File contentDir = new File( root, type.getName() + "/cnt" );
				fileStore.put( type.getName(), new ObjectFileStore[] { new ObjectFileStore( tableDir ), new ObjectFileStore( contentDir ) } );
				
			}
		}
		
//		 remove unused types
		for ( Iterator i = knownTypes.values().iterator(); i.hasNext(); ) {
			String key = (String)i.next();
			File f = new File( root, key );
			if ( f.exists() )
				AFile.deleteDir( f );
		}
		
		setFeatures( p );
		
	}

	protected void apInit() throws Exception {
		
		appendPpi( IChannelDriver.class, this );
		
	}
		
	public IChannelServer createChannel( IConnectionServer pConnection ) {
		return new FileAbstractChannel( this, pConnection );
	}	

	public IChannelServer createChannel(IConnection pConnection) throws MorseException {
		if ( pConnection instanceof IConnectionServer )
			return createChannel( (IConnectionServer)pConnection );
		throw new MorseException( MorseException.ERROR, "NOT A Server Channel: " + pConnection );
	}

	public String getName() {
		return name;
	}
	
	public IChannelProvider getChannelProvider() {
		if ( channelProvider == null )
			channelProvider = (IChannelProvider)getSinglePpi( IChannelProvider.class );
		return channelProvider;
	}  
	
	void storeObject( IType type, Btc obj, String id ) throws MorseException, IOException {
		IType cur = type;
		do {
			Properties p = new Properties();
			
			if ( cur.getName().equals( IType.TYPE_OBJECT ) ) {
				p.setProperty( IAttribute.M_ID, id );
				p.setProperty( IAttribute.M_TYPE, type.getName() );
				p.setProperty( IAttribute.M_ACL, obj.getString( IAttribute.M_ACL ) );
			} else {
				for ( int j = 0; j < obj.getAttributeCount(); j++ ) {
					if ( !obj.getAttribute( j ).isTable() && obj.getAttribute( j ).getSourceType().getName().equals( cur.getName() ) )
						p.setProperty( obj.getAttribute( j ).getName(), ObjectUtil.toString( SqlUtils.getValueRepresentation( FileAbstractDriver.this, obj.getAttribute( j ), obj.getString( j ) ) ) );
				}
			}
			Writer w = fileStore.get( cur.getName() )[0].getWriter( id );
			p.store( w );
			w.close();
			
			cur = cur.getSuperType();
		} while ( cur != null );

	}

	public String getType() {
		return type;
	}

	public Iterator<String> getObjectIds() {
		return fileStore.get( IType.TYPE_OBJECT )[0].idIterator();
	}
	
	public Properties getFeatures() {
		try {
			File f = new File( root, "features.txt" );
			Properties p = new Properties();
			Reader fis = new FileReader( f );
			p.load( fis );
			fis.close();
			return p;
		} catch ( Exception e ) {
			log().error( e );
			return new Properties();
		}
	}

	public void setFeatures(Properties features) {
		try {
			File f = new File( root, "features.txt" );
			Writer fos = new FileWriter( f );
			features.store( fos );
			fos.close();
		} catch ( Exception e ) {
			log().error( e );
		}
		
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

	public IAclManager getAclManager() {
		return aclManager;
	}

	public String getAccessAcl() {
		return accessAcl;
	}

	public ITypes getTypes() {
		return typeProvider;
	}

	public Hashtable<String, ObjectFileStore[]> getFileStore() {
		return fileStore;
	}

	public IObjectManager getObjectManager() {
		return objectManager;
	}
	
	public synchronized QueryParser getParser() {
		if ( queryParser == null ) {
			try {
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/fs.properties" ) );
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/statics.properties" ) );
			} catch (IOException e) {
				log().error( e );
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
			log().error( e );
			return null;
		}
		return baos.toByteArray();
	}

	public boolean canTransaction() {
		return false;
	}

	public void setChannelFeatures(Map<String, String> features) throws MorseException {
		root = new File( features.get( "path" ) );
	}

	
}
