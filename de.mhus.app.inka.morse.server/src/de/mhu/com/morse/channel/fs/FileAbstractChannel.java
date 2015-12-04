package de.mhu.com.morse.channel.fs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IObjectManager;
import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.mql.ErrorResult;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.mql.SingleRowResult;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectFileStore;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.com.morse.utils.ServerTypesUtil;
import de.mhu.lib.utils.Properties;

public class FileAbstractChannel implements IChannelServer{

	private static AL log = new AL( FileAbstractChannel.class );
	private IConnectionServer connection;
	private FileAbstractDriver driver;
	private IAclManager aclManager;
	private ITypes typeProvider;
	private Hashtable<String,ObjectFileStore[]> fileStore;
	private IObjectManager objectManager;
	private boolean autoCommit;

	public FileAbstractChannel(FileAbstractDriver pDriver, IConnectionServer pConnection) {
		driver = pDriver;
		connection = pConnection;
		aclManager = driver.getAclManager();
		typeProvider = driver.getTypes();
		fileStore = driver.getFileStore();
		objectManager = driver.getObjectManager();
	}

	public IQueryResult query( Query in ) throws MorseException {
		return query( in, null );
	}
	
	public IQueryResult query( Query in, UserInformation user ) throws MorseException {
					
		if ( ! aclManager.hasRead(user, driver.getAccessAcl() ) )
			throw new MorseException( MorseException.ACCESS_DENIED_READ, new String[] { "channel", getName(),driver.getAccessAcl() } );

		ICompiledQuery code = in.getCode();
		if ( code.size() == 0 )
			throw new MorseException( MorseException.QUERY_EMPTY );
		
		switch ( code.getInteger( 0 ) ) {
		case CMql.SELECT:
			return querySelect( code, user );
		case CMql.SAVE:
			return querySave( code, user );
		case CMql.LOAD:
			return queryLoad( code, user );
		case CMql.FETCH:
			return queryFetch( code, user );
		default:
			throw new MorseException( MorseException.QUERY_UNSUPPORTED );
		}
	}
	
	private IQueryResult queryFetch(ICompiledQuery code, UserInformation user) {
		
		// TODO check rights for channel
		
		String id = code.getString( 1 );
		
		try {
			Properties current = new Properties();
			Reader reader = driver.getFileStore().get( IType.TYPE_OBJECT )[0].getReader( id );
			current.load( reader );
			reader.close();
			
			String typeName = current.getProperty( IAttribute.M_TYPE );
			if ( typeName == null )
				throw new MorseException( MorseException.OBJECT_NOT_FOUND, id );
			
			IType type = typeProvider.get( typeName );
			if ( type == null )
				throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );

			ObjectFileStore[] store = fileStore.get( typeName );
			if ( store == null )
				throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );
			
			if ( ! aclManager.hasRead( user, type.getAccessAcl() ) )
				throw new MorseException( MorseException.ACCESS_DENIED_READ, new String[]{ "type", typeName, type.getAccessAcl() } );
			
			LinkedList<Attr> attr = new LinkedList<Attr>();
			for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
				Attr a = new Attr();
				a.attr = i.next();
				a.attrName = a.attrAlias = a.attr.getName();
				if ( aclManager.hasRead(user, a.attr.getAccessAcl() ) )
					attr.add( a );
			}
			
			return new FileAbstractSelectResult( driver, type, store[0], IAttribute.M_ID, id, (Attr[])attr.toArray( new Attr[ attr.size() ]) );
			
		} catch ( Exception e ) {
			log.error( e );
			return new ErrorResult( 1, 1, e.toString() );
		}
	}

	private IQueryResult queryLoad(ICompiledQuery code, UserInformation user ) {
		
		String id = code.getString( 1 );
		boolean shared = code.size() > 2 && code.getInteger( 2 ) == CMql.SHARED;
		
		try {
			Properties current = new Properties();
			Reader reader = driver.getFileStore().get( IType.TYPE_OBJECT )[0].getReader( id );
			current.load( reader );
			reader.close();
			
			String typeName = current.getProperty( IAttribute.M_TYPE );
			if ( typeName == null )
				throw new MorseException( MorseException.OBJECT_NOT_FOUND, id );
			
			IType type = typeProvider.get( typeName );
			if ( type == null )
				throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );

			ObjectFileStore[] store = fileStore.get( typeName );
			if ( store == null )
				throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );
			
			if ( ! aclManager.hasRead( user, type.getAccessAcl() ) )
				throw new MorseException( MorseException.ACCESS_DENIED_READ, new String[]{ "type", typeName, type.getAccessAcl() } );
			
			if ( ! shared )
				return new FileAbstractLoadResultStream( this, id, store[1] );
			else {
				return new  SingleRowResult( new IAttribute[] { IAttributeDefault.ATTR_OBJ_STRING, IAttributeDefault.ATTR_OBJ_STRING },
						new String[] { "channel", "path" },
						new String[] { getName(), store[1].getRelativePath( id ) }
						);
			}
				
		} catch ( Exception e ) {
			log.error( e );
			return new ErrorResult( 1, 1, e.toString() );
		}
	}

	private IQueryResult querySave(ICompiledQuery code, UserInformation user ) throws MorseException {
		
		String parentId = code.getString( 4 );
		String format = code.getString( 6 );
		String typeName = code.getString( 2 );
		IType type = typeProvider.get( typeName );
		if ( type == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );
		ObjectFileStore[] fs = fileStore.get( typeName );
		if ( fs == null )
			throw new MorseException( MorseException.TABLE_NOT_FOUND, typeName );
		
		if ( ! aclManager.hasCreate(user, type.getAccessAcl() ) )
			throw new MorseException( MorseException.ACCESS_DENIED_CREATE, new String[] { "type", type.getName(),type.getAccessAcl() } );

		IQueryResult parent = getConnection().fetch( parentId, user, false );
		if ( ! parent.next() ) {
			parent.close();
			throw new MorseException( MorseException.OBJECT_NOT_FOUND, parentId );
		}
		String parentType = parent.getString( IAttribute.M_TYPE );
		parent.close();
		
		String newId = objectManager.newObjectId( typeProvider.get( IType.TYPE_MC_CONTENT), driver );
		
		Btc obj = ServerTypesUtil.createBtc( getConnection(), type );
		obj.initObject( type, connection, null, typeProvider, user, aclManager );
		obj.setString( IAttribute.MC_FORMAT, format );
		obj.setString( IAttribute.M_ACL, aclManager.getNewContentAcl( user, type ) );
		obj.doInsertCheck();
		
		try {
			
			driver.storeObject( type, obj, newId );
			// TODO create files in m_object etc.
			return new FileAbstractSaveResult( this, newId, obj, fs, parentId, parentType );

		} catch ( Exception e ) {
			log.error( e );
			// TODO rollback !!!!
		}
		return null;
	}

	private IQueryResult querySelect(ICompiledQuery code, UserInformation user ) throws MorseException {
		
		LinkedList<Attr> attr = new LinkedList<Attr>();
		int off = 1;

		while ( code.getInteger(off) !=  CMql.FROM ) {
			Attr a = new Attr();
			a.attrName = code.getString( off );
			a.attrAlias = a.attrName;
			off++;
			if ( code.getInteger(off ) == CMql.AS ) {
				off++;
				a.attrAlias = code.getString(off);
				off++;
			}
			if ( code.getInteger(off) == CMql.COMMA )
				off++;
			attr.add( a );
		}
		
		if ( attr.size() == 0 )
			throw new MorseException( MorseException.NO_ATTRIBUTES );
		
		off++; // FROM
		
		String table = code.getString(off);
		off++;
		
		IType type = typeProvider.get( table );
		if ( type == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, table );
		
		if ( ! aclManager.hasRead(user, type.getAccessAcl() ) )
			throw new MorseException( MorseException.ACCESS_DENIED_READ, new String[] { "type", type.getName(),type.getAccessAcl() } );

		if ( attr.size() == 1 && ((Attr)attr.get(0)).attrName.endsWith( "*" ) ) {
			attr.clear();
			for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
				Attr a = new Attr();
				a.attr = i.next();
				a.attrName = a.attrAlias = a.attr.getName(); 
				if ( aclManager.hasRead(user, a.attr.getAccessAcl() ) )
					attr.add( a );
			}
		}
		
		// append type to attr
		for ( Iterator i = attr.iterator(); i.hasNext(); ) {
			Attr a = (Attr)i.next();
			IAttribute ta = type.getAttribute( a.attrName );
			if ( ta == null )
				throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, a.attrName );
			a.attr = ta;
		}
		
		String whereAttr = null;
		String whereVal  = null;
		
		if ( code.size() > off ) {
			whereAttr = code.getString(off+1).toLowerCase();
			whereVal  = code.getString( off+3 );
			if ( whereVal.startsWith("'") && whereVal.endsWith("'") )
				whereVal = whereVal.substring( 1, whereVal.length() - 1 );
		}
		
		ObjectFileStore[] store = fileStore.get( table );
		if ( store == null )
			throw new MorseException( MorseException.TABLE_NOT_FOUND, table );
		
		
		//if ( attr.size() == 1 && attr.get( 0 ).equals( "*" ) )
		//	attr.clear();
		
		return new FileAbstractSelectResult( driver, type, store[0], whereAttr, whereVal, (Attr[])attr.toArray( new Attr[ attr.size() ]) );
	}

	public void close() {
		
	}

	public String getName() {
		return driver.getName();
	}

	public QueryParser getParser() {
		return driver.getParser();
	}

	public byte[] getDefinition() {
		return driver.getDefinition();
	}

	public IQueryResult fetch(String id, UserInformation user, boolean stamp) throws MorseException {
		ObjectFileStore[] store = driver.getFileStore().get( IType.TYPE_OBJECT );
		if ( store == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, IType.TYPE_OBJECT );
		Reader reader = null;
		Properties prop = new Properties();
		try {
			reader = store[0].getReader( id );
			prop.load( reader );
			reader.close();
		} catch (IOException e) {
			// throw new MorseException( MorseException.ERROR, e );
			if ( log.t3() ) log.error( e );
			throw new MorseException( MorseException.OBJECT_NOT_FOUND, id );
		}
		if ( stamp )
			return new SingleRowResult( new IAttribute[] { IAttributeDefault.ATTR_OBJ_INT }, new String[] { IAttribute.M_STAMP }, new String[] { prop.getProperty( IAttribute.M_STAMP )} );
		String typeName = prop.getProperty( IAttribute.M_TYPE );
		IType type = typeProvider.get( typeName );
		if ( type == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );
		LinkedList<Attr> attr = new LinkedList<Attr>();
		for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
			Attr a = new Attr();
			a.attr = i.next();
			a.attrAlias = a.attrName = a.attr.getName();
			attr.add( a );
		}
		return new FileAbstractSelectResult( driver, type, driver.getFileStore().get( typeName )[0], IAttribute.M_ID, id, attr.toArray( new Attr[ attr.size() ] ) );
	}

	public IConnectionServer getConnection() {
		return connection;
	}

	public void commit() {

	}

	public void rollback() {
	}

	FileAbstractDriver getDriver() {
		return driver;
	}

	public boolean lock(String id, UserInformation user) throws MorseException {
		
		if ( log.t4() )
			log.info( "LOCK: " + id + ' ' + user );
		
		ObjectUtil.assetId( id );
		if ( user == null ) throw new MorseException( MorseException.UNKNOWN_USER );
		
//		 create lock file
		ObjectFileStore ofs = driver.getFileStore().get( IType.TYPE_OBJECT )[0];
		try {
			if ( log.t4() )
				log.debug( "xLOCK: " + id + ' ' + user );
			if ( ! ofs.lock( id ) )
				throw new MorseException( MorseException.ERROR );
		} catch (IOException e) {
			throw new MorseException( MorseException.ERROR, e );
		}
		
		try {
			
			IQueryResult res = fetch( id, user, false );
			if ( ! res.next() ) {
				res.close();
				throw new MorseException( MorseException.ACCESS_DENIED, id );
			}
			String lock = res.getString( IAttribute.M_LOCK );
			res.close();
			if ( ObjectUtil.validateId( lock ) && ! lock.equals( user.getUserId() ) )
				return false;
		
			Properties p = new Properties();
			Reader reader = ofs.getReader( id );
			p.load( reader );
			reader.close();
			p.setProperty( IAttribute.M_LOCK, user.getUserId() );
			Writer writer = ofs.getWriter( id );
			p.store( writer );
			writer.close();
			
		} catch ( Exception e ) {
			log.error( e );
			return false;
		} finally {
			if ( log.t4() )
				log.debug( "xUNLOCK: " + id + ' ' + user );
			ofs.unlock( id );
		}
		
		return true;
	}

	public void unlock(String id, boolean force, UserInformation user) throws MorseException {
		
		if ( log.t4() )
			log.info( "UNLOCK: " + id + ' ' + user );

		ObjectUtil.assetId( id );
		if ( !force && user == null ) throw new MorseException( MorseException.UNKNOWN_USER );
		
		// create lock file
		ObjectFileStore ofs = driver.getFileStore().get( IType.TYPE_OBJECT )[0];

		try {
			if ( log.t4() )
				log.debug( "xLOCK: " + id + ' ' + user );
			if ( ! ofs.lock( id ) )
				throw new MorseException( MorseException.ERROR );
		} catch (IOException e) {
			throw new MorseException( MorseException.ERROR, e );
		}
		
		try {
			
			IQueryResult res = fetch( id, user, false );
			if ( ! res.next() ) {
				res.close();
				throw new MorseException( MorseException.ACCESS_DENIED, id );
			}
			String lock = res.getString( IAttribute.M_LOCK );
			res.close();
			
			if ( ! force && ObjectUtil.validateId( lock ) && ! lock.equals( user.getUserId() ) ) {
				throw new MorseException( MorseException.NOT_OWNER, id );
			}
					
			Properties p = new Properties();
			Reader reader = ofs.getReader( id );
			p.load( reader );
			reader.close();
			p.setProperty( IAttribute.M_LOCK, "" );
			Writer writer = ofs.getWriter( id );
			p.store( writer );
			writer.close();
			
		} catch ( Exception e ) {
			log.error(  e );
		} finally {
			if ( log.t4() )
				log.debug( "xUNLOCK: " + id + ' ' + user );
			ofs.unlock( id );
		}
		
	}

	public void store(IObjectRead obj, boolean commit, UserInformation user) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public void setAutoCommit(boolean b) {
		// TODO Auto-generated method stub
		
	}
	
}
