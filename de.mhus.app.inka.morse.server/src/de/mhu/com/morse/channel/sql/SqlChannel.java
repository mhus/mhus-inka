package de.mhu.com.morse.channel.sql;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.mhu.lib.dtb.DbProvider;
import de.mhu.lib.dtb.StatementPool;
import de.mhu.lib.dtb.Sth;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.aco.IAco;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.channel.ITransaction;
import de.mhu.com.morse.channel.mql.UpdateSetParser;
import de.mhu.com.morse.channel.mql.UpdateSetParser.UpdateSetDescription;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.SingleRowResult;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.obj.ITable;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.com.morse.utils.ServerTypesUtil;
import de.mhu.lib.plugin.utils.IAfConfig;
import de.mhu.lib.plugin.utils.IAfLogger;
import de.mhu.lib.utils.ResourceException;

public class SqlChannel implements IChannelServer, DbProvider {

	private static AL log = new AL( SqlChannel.class );
	private StatementPool pool;
	//private IAfConfig config;
	// private ITypes types;
	// private IAfLogger log;
	// private QueryParser queryParser;
	// private String name;
	// private Properties queryParserProperties;
	private SqlDriver driver;
	private IConnectionServer connection;
	private boolean autoCommit = true;
	

	public SqlChannel( SqlDriver pDriver, IConnectionServer pConnection ) throws MorseException {
		
		driver = pDriver;
		connection = pConnection;
		//config = pConfig;
		// types = dri;
		// log = pLogger;
		// queryParser = pQueryParser;
		// name = pName;
		// queryParserProperties = pQueryParserProperties;
		
		try {
			pool = new StatementPool( reconnect(), this );
		} catch (SQLException e) {
			throw new MorseException( MorseException.CANT_CONNECT, e );
		}
		
	}
	
	public Connection reconnect() throws SQLException {
		Connection db = DriverManager.getConnection( driver.getPath(), driver.getUser(), driver.getPass() );
		db.setAutoCommit( true );
		return db;
	}
	
	public IQueryResult query(Query in) throws MorseException {
		return query( in, null );
	}
	
	public IQueryResult query(Query in, UserInformation user ) throws MorseException {
		
		if ( ! driver.getAclManager().hasRead(user, driver.getAccessAcl() ) )
			throw new MorseException( MorseException.ACCESS_DENIED_READ, new String[] { "channel", getName(),driver.getAccessAcl() } );
		
		ICompiledQuery code = in.getCode();
		if ( code.size() == 0 )
			throw new MorseException( MorseException.QUERY_EMPTY );
		
		switch ( code.getInteger( 0 ) ) {
		case CMql.SELECT:
			return querySelect( code, user );
		case CMql.INSERT:
			return queryInsert( code, user );
		case CMql.DELETE:
			return queryDelete( code, user );
		case CMql.UPDATE:
			return queryUpdate( code, user );
		case CMql.FETCH:
			return queryFetch( code, user );
		case CMql.RENDITION:
			return queryRendition( code, user );
		default:
			throw new MorseException( MorseException.QUERY_UNSUPPORTED );
		}
	}

	private IQueryResult queryRendition(ICompiledQuery code, UserInformation user) throws MorseException {
		
		int off=1;
		String id = code.getString( off );
		off++;
		if ( id.length() == 34 && id.charAt( 0 ) == '\'' )
			id = id.substring( 1, 33 );
		ObjectUtil.assetId( id );
		IQueryResult res = fetch( id, user, false );
		if ( ! res.next() ) {
			res.close();
			throw new MorseException( MorseException.OBJECT_NOT_FOUND, id );
		}
		String typeName = res.getString( IAttribute.M_TYPE );
		IType type = driver.getTypes().get( typeName );
		if ( type == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );
		// if ( driver.getAclManager().hasRead( user, type.getAccessAcl() ))
		Btc obj = ServerTypesUtil.createBtc( getConnection(), type );
		obj.initObject( type, connection, res, driver.getTypes(), user, driver.getAclManager() );
		
		res.close();
		
		int index = -3;
		if ( code.getInteger( off + 1 ) == CMql.DEFAULT )
			index = -1;
		else
		if ( code.getInteger( off + 1 ) == CMql.APPEND )
			index = -2;
		else
			index = Integer.parseInt( code.getString( off + 1 ) );
		
		if ( index < -2 )
			throw new MorseException( MorseException.INDEX_NOT_ALLOWED, String.valueOf( index ) );
		
		IQueryResult qres = null;
		Sth sth = null;
		switch ( code.getInteger( off ) ) {
		case CMql.LOAD:
			
			off+=2;
			HashSet<String> shared = null;
			if ( code.getInteger( off ) == CMql.SHARED ) {
				shared = new HashSet<String>();
				off++;
				while ( off < code.size() ) {
					shared.add( code.getString( off ) );
					off++;
					if ( off < code.size() && code.getInteger( off ) == CMql.COMMA )
						off++;
				}
			}
			
			return obj.loadRendition( index, shared );
			
		case CMql.SAVE:
			String format = code.getString( off + 3 );
			qres = obj.createRendition( index, format );
			qres.reset();
			qres.next();
			String rendId = qres.getString( IAttribute.M_ID );
			qres.reset();
			
			return new SaveRenditionQueryResult( qres, obj, rendId, index, format, pool, this );
		case CMql.DELETE:
			String rId = obj.deleteRendition( index );
			// obj.doUpdate();
			driver.getObjectManager().eventContentRemoved( getName(), rId, id, type.getName() );
			try {
				sth = pool.aquireStatement();
				SqlUtils.updateBtc( this, sth, obj, true, true );
			} catch ( ResourceException re ) {
				if ( re instanceof MorseException )
					throw (MorseException)re;
				throw new MorseException( MorseException.ERROR, re );
			} finally {
				if ( sth != null )
					try {
						sth.release();
					} catch ( Exception e ) {
						throw new MorseException( MorseException.ERROR, e );						
					}
			}
			return qres;
		}
		
		return null;
	}

	public IQueryResult fetch( String id, UserInformation user, boolean stamp ) throws MorseException {
		
		String type = null;
		String vstamp = null;
		Sth sth = null;
		try {
			sth = pool.aquireStatement();
			IQueryResult out = null;
			String sql = "SELECT " + driver.getColumnName( IAttribute.M_TYPE ) + ',' + driver.getColumnName( IAttribute.M_STAMP ) + " FROM t_" + IType.TYPE_OBJECT + " WHERE " + driver.getColumnName( IAttribute.M_ID ) + "='" + id + "'";
			ResultSet res = sth.executeQuery(sql);
			if ( res.next() ) {
				type = res.getString( 1 );
				vstamp = res.getString( 2 );
			} else {
				res.close();
				throw new MorseException( MorseException.TYPE_NOT_FOUND );
			}
			res.close();
			
			if ( stamp )
				return new SingleRowResult( new IAttribute[] { IAttributeDefault.ATTR_OBJ_INT }, new String[] { IAttribute.M_STAMP }, new String[] { vstamp } );
			
			Descriptor desc = new Descriptor();
			
			Table a = new Table();
			a.name = type;
			a.alias = a.name;
			a.type = driver.getTypes().get( type );
			
			desc.addTable( a );
			
			if ( a.type == null )
				throw new MorseException( MorseException.TYPE_NOT_FOUND, type );
			
			if ( ! driver.getAclManager().hasRead(user, a.type.getAccessAcl() ) )
				throw new MorseException( MorseException.ACCESS_DENIED_READ, new String[] { "type", type,a.type.getAccessAcl() } );
			
			Attr ax = new Attr();
			ax.name="**";
			desc.addAttr( ax );
			SqlUtils.checkTables( desc, driver.getTypes(), getConnection(), user, driver.getAclManager() );
			SqlUtils.collectAttributes( desc, driver.getTypes(), user, getAclManager() );
			StringBuffer sb = new StringBuffer();
			SqlUtils.createSelect( driver, desc, sb, false );
			sb.append( " WHERE " + driver.getColumnName( IAttribute.M_ID ) + "='" + id + "'" );
			sql = sb.toString();
			res = sth.executeQuery(sql);
			
			a.internalAcl = driver.getColumnName( IAttribute.M_ACL );
			a.internalId = driver.getColumnName( IAttribute.M_ID );
			
			return driver.createSelectResult( this, desc, res, sth, user );
		
		} catch ( Exception sqle ) {			
			if ( sth != null )
				try {
					sth.release();
				} catch ( Exception e ) {
					throw new MorseException( MorseException.ERROR, e );					
				}
			if ( sqle instanceof MorseException )
				throw (MorseException)sqle;
			throw new MorseException( MorseException.ERROR, sqle );
		}
		
	}
	
	private IQueryResult queryFetch(ICompiledQuery code, UserInformation user) throws MorseException {
		
		int off = 1;
		// boolean short_ = false;
		boolean stamp = false;
		
		String id = code.getString(off);
		if ( id.length() == 34 && id.startsWith( "'" ) && id.endsWith( "'" ) )
			id = id.substring( 1, 33 );
		ObjectUtil.assetId( id );
		off++;
		
		if ( off < code.size() && code.getInteger( off ) == CMql.STAMP ) {
			stamp = true;
			off++;
		}
		
//		if ( code.size() > off && code.getInteger( off ) == QConst.SHORT ) {
//			short_ = true;
//		}
		
		return fetch( id, user, stamp );
		
	}

	private IQueryResult queryUpdate(ICompiledQuery code, UserInformation user) throws MorseException {
		
		IAclManager aclManager = driver.getAclManager();
		int off = 1;
		boolean isNoBtc    = false;
		boolean isNoCommit = false;
		boolean isNoEvent  = false;
		boolean isNoError  = false;
		
		if ( code.isFeature( "-btc" ) ) {
			isNoBtc = true;
			if ( ! aclManager.isAdministrator( user ) )
				throw new MorseException( MorseException.ACCESS_DENIED, "-btc" );
		}
		if ( code.isFeature( "-commit" ) ) {
			isNoBtc = true;
		}
		if ( code.isFeature( "-event" ) ) {
			isNoBtc = true;
			if ( ! aclManager.isAdministrator( user ) )
				throw new MorseException( MorseException.ACCESS_DENIED, "-event" );
		}
		if ( code.isFeature( "-error") ) {
			isNoError = true;
		}
		
		String table = code.getString( off );
		off++;
		IType type = driver.getTypes().get( table ); 
		if ( type == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, table );
		
		if ( ! aclManager.hasWrite(user, type.getAccessAcl() ) )
			throw new MorseException( MorseException.ACCESS_DENIED_WRITE, new String[] { "type", table, type.getAccessAcl() } );
		
		Table t = new Table();
		t.name  = table;
		t.alias = table;
		t.type  = type;
		t.internalId = IAttribute.M_ID;
		t.internalAcl = IAttribute.M_ACL;
		
		if ( code.getInteger( off ) == CMql.OPEN ) {
			off++;
			while ( code.getInteger( off ) != CMql.CLOSE ) {
				t.addHint( code.getString( off ) );
				off++;
				if ( code.getInteger( off ) == CMql.COMMA )
					off++;
			}
			off++;
		}
		
		off++; // SET
		
		UpdateSetDescription setDesc = new UpdateSetParser.UpdateSetDescription();
		off = UpdateSetParser.parse( off, code, type, aclManager, user, driver, setDesc, isNoError );
		
		// create select
		StringBuffer sb = new StringBuffer();
		Descriptor selDesc = new Descriptor();
		
		selDesc.addTable( t );
		
		SqlUtils.checkTables( selDesc, driver.getTypes(), getConnection(), user, aclManager );
		
		sb.append( "SELECT DISTINCT " )
			.append( driver.getColumnName( IAttribute.M_ID ) )
			.append( ',' ).append( driver.getColumnName( IAttribute.M_ACL ) )
			.append( ',' ).append( driver.getColumnName( IAttribute.M_TYPE ) )
			.append( " FROM v_" ).append( type.getName() ).append( " AS " ).append( type.getName() );
		
		boolean hasWhere = false;
		
		if ( SqlUtils.needHintWhere( driver, selDesc ) ) {
			
			if ( ! hasWhere ) {
				sb.append( " WHERE (" );
			} else {
				sb.append( " AND (" );
			}
			
			SqlUtils.createHintWhereClause( getConnection(), driver, selDesc, driver.getTypes(), sb, user, getAclManager() );
			sb.append( " ) " );
			hasWhere = true;
			
		}
		
		if ( code.getInteger( off ) == CMql.WHERE ) {
			off++;
			if ( ! hasWhere ) {
				sb.append( " WHERE (" );
			} else {
				sb.append( " AND (" );
			}
			off = SqlUtils.createWhereClause( getConnection(), driver, off, code, selDesc, driver.getTypes(), sb, user, aclManager);
			sb.append( ")" );
		}
		
		// create the order / limit / offset
		
		while ( off < code.size() ) {
			switch ( code.getInteger( off ) ) {
				case CMql.ORDER:
					off = SqlUtils.createOrderClause( driver, off, code, selDesc, driver.getTypes(), sb, user, driver.getAclManager() );
					break;
				case CMql.OFFSET:
				case CMql.LIMIT:
					off = SqlUtils.createLimitClause( driver, off, code, selDesc, driver.getTypes(), sb, user, driver.getAclManager() );
					break;
				default:
					off++; // TODO: should throw an exception
			}
		}
		
		String sql = sb.toString();

		Sth sth = null;
		Sth sthUpdate = null;
		try {
			sth = pool.aquireStatement();
			sthUpdate = pool.aquireStatement();
			return new SqlUpdateResult( driver, this, sth, sql, sthUpdate, setDesc, selDesc, user, isNoBtc, type, isNoCommit, isNoEvent );
		} catch ( Exception e ) {
			if ( sth != null ) try { sth.release(); } catch ( Exception ex ) { log.error( ex ); }
			if ( sthUpdate != null ) try { sthUpdate.release();  } catch ( Exception ex ) { log.error( ex ); }
			throw new MorseException( MorseException.ERROR, e );
		}
		
	}

	private IQueryResult queryDelete(ICompiledQuery code, UserInformation user) throws MorseException {

		int off = 2; // DELETE FROM
		
		IAclManager aclManager = driver.getAclManager();
		boolean isNoBtc    = false;
		boolean isNoCommit = false;
		boolean isNoEvent  = false;
		
		if ( code.isFeature( "-btc" ) ) {
			isNoBtc = true;
			if ( ! aclManager.isAdministrator( user ) )
				throw new MorseException( MorseException.ACCESS_DENIED, "-btc" );
		}
		if ( code.isFeature( "-commit" ) ) {
			isNoBtc = true;
		}
		if ( code.isFeature( "-event" ) ) {
			isNoBtc = true;
			if ( ! aclManager.isAdministrator( user ) )
				throw new MorseException( MorseException.ACCESS_DENIED, "-event" );
		}
		
		String table = code.getString( off );
		off++;
		IType type = driver.getTypes().get( table ); 
		if ( type == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, table );
		
		if ( ! aclManager.hasDelete(user, type.getAccessAcl() ) )
			throw new MorseException( MorseException.ACCESS_DENIED_DELETE, new String[] { "type", table, type.getAccessAcl() } );
		
		Descriptor selDesc = new Descriptor();
		Table t = new Table();
		t.name  = table;
		t.alias = table;
		t.type  = type;
		t.internalId = IAttribute.M_ID;
		t.internalAcl = IAttribute.M_ACL;
		selDesc.addTable( t );
		
		SqlUtils.checkTables(selDesc, driver.getTypes(), getConnection(), user, aclManager);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append( "SELECT DISTINCT " )
		.append( driver.getColumnName( IAttribute.M_ID ) )
		.append( ',' ).append( driver.getColumnName( IAttribute.M_ACL ) )
		.append( ',' ).append( driver.getColumnName( IAttribute.M_TYPE ) )
		.append( " FROM v_" ).append( type.getName() );
	
		if ( code.getInteger( off ) == CMql.WHERE ) {
			off++;
			sb.append( " WHERE (" );
			off = SqlUtils.createWhereClause( getConnection(), driver, off, code, selDesc, driver.getTypes(), sb, user, aclManager);
			sb.append( ")" );
		}
		
		// create the order / limit / offset
		
		while ( off < code.size() ) {
			switch ( code.getInteger( off ) ) {
				case CMql.ORDER:
					off = SqlUtils.createOrderClause( driver, off, code, selDesc, driver.getTypes(), sb, user, driver.getAclManager() );
					break;
				case CMql.OFFSET:
				case CMql.LIMIT:
					off = SqlUtils.createLimitClause( driver, off, code, selDesc, driver.getTypes(), sb, user, driver.getAclManager() );
					break;
				default:
					off++; // TODO: should throw an exception
			}
		}
		
		String sql = sb.toString();
		
		Sth sth = null;
		Sth sthDelete = null;
		try {
			sth = pool.aquireStatement();
			sthDelete = pool.aquireStatement();
			return new SqlDeleteResult( driver, this, sth, sql, sthDelete, user, selDesc, isNoBtc, isNoCommit, isNoEvent );
		} catch ( Exception e ) {
			if ( sth != null ) try { sth.release(); } catch ( Exception ex ) { log.error( ex ); }
			if ( sthDelete != null ) try { sthDelete.release(); } catch ( Exception ex ) { log.error( ex ); }
			throw new MorseException( MorseException.ERROR, e );
		}
	}

	private IQueryResult queryInsert(ICompiledQuery code, UserInformation user) throws MorseException {
		IAclManager aclManager = driver.getAclManager();
		boolean isNoBtc    = false;
		boolean isNoCommit = false;
		boolean isNoEvent  = false;
		boolean isNoError  = false;
		if ( code.isFeature( "-btc" ) ) {
			isNoBtc = true;
			if ( ! aclManager.isAdministrator( user ) )
				throw new MorseException( MorseException.ACCESS_DENIED, "-btc" );
		}
		if ( code.isFeature( "-commit" ) ) {
			isNoBtc = true;
		}
		if ( code.isFeature( "-event" ) ) {
			isNoBtc = true;
			if ( ! aclManager.isAdministrator( user ) )
				throw new MorseException( MorseException.ACCESS_DENIED, "-event" );
		}
		if ( code.isFeature( "-error" ) ) {
			isNoError = true;
		}
		
		int off = 2; // INSERT INTO
		String typeName = code.getString( off );
		off++;
		IType type = driver.getTypes().get( typeName );
		if ( type == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, typeName );
		
		if ( ! aclManager.hasCreate(user, type.getAccessAcl() ) )
			throw new MorseException( MorseException.ACCESS_DENIED_CREATE, new String[] { "type", typeName,type.getAccessAcl() } );
		
		Descriptor desc = new Descriptor();
		LinkedList<Boolean> canSet = null;
		if ( isNoError ) canSet = new LinkedList<Boolean>();
		
		if ( code.getInteger( off ) == CMql.OPEN ) {
			off++;
			while ( code.getInteger( off ) != CMql.CLOSE ) {
				Attr a = new Attr();
				a.name = code.getString( off );
				off++;
				a.attr = type.getAttribute( a.name );
				boolean set = true;
				if ( a.attr == null )
					throw new MorseException( MorseException.ATTR_NOT_FOUND, typeName + '.' + a.name );
				if ( a.attr.getType() == IAttribute.AT_TABLE )
						throw new MorseException( MorseException.ATTR_IS_A_TABLE, typeName + '.' + a.name );
				if ( !aclManager.hasCreate( user, a.attr.getAccessAcl() ) ) {
					if ( isNoError )
						set =false;
					else
						throw new MorseException( MorseException.ACCESS_DENIED_CREATE, new String[] { "attr", typeName + '.' + a.name,a.attr.getAccessAcl() } );
				}
				if ( a.attr.getSourceType().getName().equals( IType.TYPE_OBJECT ) ) {
					if ( isNoError )
						set = false;
					else
						throw new MorseException( MorseException.ATTR_CAN_T_SET, typeName + '.' + a.name );
				}
				
				if ( set )
					desc.addAttr( a );
				
				if ( isNoError )
					canSet.add( set );
				
				if ( code.getInteger( off ) == CMql.COMMA )
					off++; // COMMA
			}
			off++;
		} else {
			// find all attributes automatically
			for ( Iterator i = type.getAttributes(); i.hasNext(); ) {
				Attr a = new Attr();
				a.attr = (IAttribute)i.next();
				a.name = a.attr.getName();
				if ( 	! a.attr.getSourceType().getName().equals( IType.TYPE_OBJECT ) &&
						a.attr.getType() != IAttribute.AT_TABLE && 
						aclManager.hasCreate( user, a.attr.getAccessAcl() ) ) {
					desc.addAttr( a );
					if ( isNoError )
						canSet.add( true );
				}
			}
		}
		
		Btc obj = ServerTypesUtil.createBtc( getConnection(), type );
		obj.initObject( type, connection, driver.getTypes(), user, aclManager );
		
		if ( code.getInteger( off ) == CMql.VALUES ) {
			 off++;
			 off++; // ( - OPEN
			 int cnt = 0;
			 while ( code.getInteger( off ) != CMql.CLOSE ) {
				 if ( cnt >= desc.attrSize )
					 throw new MorseException( MorseException.WRONG_ATTR_COUNT, String.valueOf( cnt ) );
				 String value = code.getString( off );
				 off++;
				 if ( value.length() > 1 && value.charAt( 0 ) == '\'' && value.charAt( value.length() - 1 ) == '\'' )
					 value = value.substring( 1, value.length() - 1 );
				 
				 if ( !isNoError || canSet.get( cnt ) )
					 obj.setString( desc.attrs[ cnt ].name, value );
				 
				 if ( code.getInteger( off ) == CMql.COMMA )
					off++; // COMMA
				 cnt++;
			 }
			 off++;
			 if ( !isNoError && cnt != desc.attrSize )
				 throw new MorseException( MorseException.WRONG_ATTR_COUNT, String.valueOf( cnt ) );
		}
		
		// load sub-table values
		// LinkedList<Descriptor> subDesc = new LinkedList<Descriptor>();
		
		while ( code.getInteger( off ) == CMql.APPEND ) {
			off++;
			String attrName = code.getString( off );
			off++;
			IAttribute attr = type.getAttribute( attrName );

			if ( isNoError ) canSet.clear();
			boolean set = true;
			
			if ( attr == null )
				throw new MorseException( MorseException.ATTR_NOT_FOUND, typeName + '.' + attrName );
			
			if ( attr.getType() != IAttribute.AT_TABLE )
				throw new MorseException( MorseException.ATTR_NOT_A_TABLE, typeName + '.' + attrName );
			
			if ( !aclManager.hasCreate( user, attr.getAccessAcl() ) ) {
				if ( isNoError )
					set = false;
				else
					throw new MorseException( MorseException.ACCESS_DENIED_CREATE, new String[] { "attr", typeName + '.' + attrName,attr.getAccessAcl() } );
			}
			
			ITable objTable = obj.getTable( attrName );
			
			Descriptor descT = new Descriptor();
			Table t = new Table();
			t.attr = attr;
			descT.addTable( t );
			if ( code.getInteger( off ) == CMql.OPEN ) {
				off++;
				while ( code.getInteger( off ) != CMql.CLOSE ) {
					boolean set2 = true;
					Attr a = new Attr();
					a.name = code.getString( off );
					off++;
					a.attr = attr.getAttribute( a.name );
					if ( a.attr == null )
						throw new MorseException( MorseException.ATTR_NOT_FOUND, typeName + '.' + attr.getName() + '.' + a.name );
					if ( !aclManager.hasCreate( user, a.attr.getAccessAcl() ) ) {
						if ( isNoError )
							set2 = false;
						else
							throw new MorseException( MorseException.ACCESS_DENIED_CREATE, new String[] { "attr", a.attr.getCanonicalName(),a.attr.getAccessAcl() } );
					}
					if ( a.attr.getName().equals( IAttribute.M_ID ) || a.attr.getName().equals( IAttribute.M_POS ) ) {
						if ( isNoError )
							set2 = false;
						else
							throw new MorseException( MorseException.ATTR_CAN_T_SET, a.attr.getCanonicalName() );
					}
					
					if ( set2 )
						descT.addAttr( a );
					
					if ( isNoError )
						canSet.add( set2 );
					
					if ( code.getInteger( off ) == CMql.COMMA )
						off++; // COMMA
				}
				off++;
			} else {
				// collect all attributes
				for ( Iterator i = attr.getAttributes(); i.hasNext(); ) {
					Attr a = new Attr();
					a.attr = (IAttribute)i.next();
					a.name = a.attr.getName();
					if ( 	! a.attr.getName().equals( IAttribute.M_ID ) &&
							! a.attr.getName().equals( IAttribute.M_POS ) && 
							aclManager.hasCreate( user, a.attr.getAccessAcl() ) ) {
						descT.addAttr( a );
						if ( isNoError ) canSet.add( true );
					}
				}
			}
			
			objTable.createRow();
			
			if ( code.getInteger( off ) == CMql.VALUES ) {
				// sub values for main table 
				off++;
				off++; // ( OPEN
				int cnt = 0;
				 while ( code.getInteger( off ) != CMql.CLOSE ) {
					 if ( cnt >= descT.attrSize )
						 throw new MorseException( MorseException.WRONG_ATTR_COUNT, String.valueOf( cnt ) );
					 String value = code.getString( off );
					 off++;
					 if ( value.length() > 1 && value.charAt( 0 ) == '\'' && value.charAt( value.length() - 1 ) == '\'' )
						 value = value.substring( 1, value.length() - 1 );
					 
					 if ( !isNoError || canSet.get( cnt ) )
						 objTable.setString( descT.attrs[ cnt ].name, value );
					 
					 if ( code.getInteger( off ) == CMql.COMMA )
						off++; // COMMA
					 cnt++;
				 }
				 off++;
				 if ( !isNoError && cnt != descT.attrSize )
					 throw new MorseException( MorseException.WRONG_ATTR_COUNT, new String[] { attr.getCanonicalName(), String.valueOf( cnt ) } );
			}
			
			if ( set )
				objTable.appendRow();
			
		}
		
		if ( ! isNoBtc ) obj.doInsertCheck();
		
		// execute main table
		
		LinkedList<IType> superTypes = new LinkedList<IType>();
		IType cur = type;
		do {
			superTypes.addFirst( cur );
			cur = cur.getSuperType();
		} while ( cur != null );
		Sth sth = null;
		ITransaction tr = null;
		try {
			sth = pool.aquireStatement();
			String newId = driver.getObjectManager().newObjectId( type, driver );
			if ( ! isNoBtc ) obj.doInsert( newId );
			
			tr = connection.startTransaction();
			
			for ( Iterator<IType> i = superTypes.iterator(); i.hasNext(); ) {
				cur = i.next();
				StringBuffer sb = new StringBuffer();
				sb.append( "INSERT INTO t_" ).append( cur.getName() ).append( " (" ).append( driver.getColumnName( IAttribute.M_ID ) );
				if ( cur.getName().equals( IType.TYPE_OBJECT ) ) {
					sb.append( ',' ).append( driver.getColumnName( IAttribute.M_TYPE ) );				
					sb.append( ',' ).append( driver.getColumnName( IAttribute.M_ACL ) );
					sb.append( ") VALUES (" );
					sb.append( '\'' ).append( newId ).append( '\'' );
					sb.append( ",'" ).append( type.getName() ).append( '\'' );
					sb.append( ",'" ).append( obj.getString( IAttribute.M_ACL ) ).append( '\'' );
				} else {
					for ( int j = 0; j < obj.getAttributeCount(); j++ ) {
						if ( !obj.getAttribute( j ).isTable() && obj.getAttribute( j ).getSourceType().getName().equals( cur.getName() ) )
							sb.append( ',' ).append( driver.getColumnName( obj.getAttribute( j ).getName() ) );
					}
					sb.append( ") VALUES (" );
					sb.append( '\'' ).append( newId ).append( '\'' );
					for ( int j = 0; j < obj.getAttributeCount(); j++ ) {
						if ( !obj.getAttribute( j ).isTable() && obj.getAttribute( j ).getSourceType().getName().equals( cur.getName() ) ) {
							// IAco aco = obj.getAttribute( j ).getAco();
							sb.append( ',' );
							sb.append( SqlUtils.getValueRepresentation( driver, obj.getAttribute( j ), obj.getString( j ) ) );
						}
					}
				}
				sb.append( ')' );
				String sql = sb.toString();
				sth.executeUpdate( sql );
			}
			
			for ( int a = 0; a < obj.getAttributeCount(); a++ ) {
				IAttribute attr = obj.getAttribute( a );
				if ( attr.isTable() ) {
					ITable table = obj.getTable( a );
					table.reset();
					while ( table.next() ) {
						StringBuffer sb = new StringBuffer();
						sb	.append( "INSERT INTO r_" ).append( attr.getSourceType().getName() ).append( '_' )
							.append( attr.getName() ).append( " (" )
							.append( driver.getColumnName( IAttribute.M_ID ) ).append(',')
							.append( driver.getColumnName( IAttribute.M_POS ) );
						for ( int i = 0; i < table.getAttributeCount(); i++ )
							sb.append( ',' ).append( driver.getColumnName( table.getAttribute( i ).getName() ) );
						sb.append( ") VALUES ( " );
						sb.append( '\'' ).append( newId ).append( "'," ).append( table.getCursor() );
						for ( int i = 0; i < table.getAttributeCount(); i++ )
							sb.append( ',' ).append( SqlUtils.getValueRepresentation( driver, table.getAttribute( i ), table.getString( i ) ) );
						sb.append( ')' );
						
						String sql = sb.toString();
						sth.executeUpdate( sql );
					}
				}
			}
			
			if ( !isNoEvent ) connection.eventObjectCreated( getName(), newId, typeName );
			if ( !isNoCommit ) 
				connection.maybeCommit( tr );
			else
				connection.stopTransaction( tr );
			tr = null;
			
			return driver.createInsertResult( this, newId );
			
		} catch ( Exception e ) {
			if ( e instanceof MorseException ) throw (MorseException)e;
			throw new MorseException( MorseException.ERROR, e.toString(), e );
		} finally {
			
			if ( tr != null ) {
				if ( !isNoCommit ) 
					connection.maybeRollback( tr );
				else
					connection.stopTransaction( tr );
			}
			
			if ( sth != null )
				try { sth.release(); } catch ( Exception ex ) { log.error( ex ); }
		}

	}

	private IQueryResult querySelect(ICompiledQuery code, UserInformation user) throws MorseException {
		
		int off = 1;
		
		// if distinct
		boolean distinct = false;
		
		if ( code.getInteger( off ) == CMql.DISTINCT ) {
			distinct = true;
			off++;
		}
		
		// init descriptors
		
		Descriptor desc = new Descriptor();
		Descriptor findDesc = null;
		
		// find all attributes / functions defined in select and remember
		
		off = SqlUtils.findAttributes(off, code, desc);
		if ( desc.attrSize == 0 )
			throw new MorseException( MorseException.NO_ATTRIBUTES );

		// if a FIND is defined read attributes / functions from the statement
		
		if ( code.getInteger( off ) == CMql.FIND ) {
			off++;
			findDesc = new Descriptor();
			off = SqlUtils.findAttributes(off, code, findDesc);
		}
		
		off++; // FROM
		
		// find all tables / types defined in the statement
		
		off = SqlUtils.findTables(off, code, desc );
		SqlUtils.checkTables(desc, driver.getTypes(), getConnection(), user, getAclManager() );
		
		// if "*" or "**" then find all attributes
		
		if ( ! SqlUtils.collectAttributes( desc, driver.getTypes(), user, getAclManager() ) ) {
			
			// collectAttr will automatically put the type on the attr.
			// if not a ** or * table, this must map the types
			// find type / table / function for attribute
			
			SqlUtils.checkAttributes( getConnection(), desc, user, getAclManager() );
		}
		
		if ( desc.attrSize == 0 )
			throw new MorseException( MorseException.NO_ATTRIBUTES );
		
		// find internal needed attributes
		
		SqlUtils.findInternalAttributes( desc, user );
		
		// maybe need to set the correct alias
		
		SqlUtils.postCheckAttributes( desc );

		// check and create the functions - now we have all attribute aliases, they are needed
		
		SqlUtils.checkFunctions( getConnection(), desc, desc, user, driver.getAclManager() );
		
		// if a find is defined init all functions
		
		if ( findDesc != null ) {
			SqlUtils.checkFunctions( getConnection(), findDesc, desc, user, driver.getAclManager() );
		}
		
		// create the select part of the query
		
		StringBuffer sb = new StringBuffer();
		SqlUtils.createSelect( driver, desc, sb, distinct );
		
		// create the WHERE part of the query
		  
		boolean hasWhere = false;
		
		if ( SqlUtils.needHintWhere( driver, desc ) ) {
			
			if ( ! hasWhere ) {
				sb.append( " WHERE (" );
			} else {
				sb.append( " AND (" );
			}
			
			SqlUtils.createHintWhereClause( getConnection(), driver, desc, driver.getTypes(), sb, user, getAclManager() );
			sb.append( " ) " );
			hasWhere = true;
			
		}
		
		if ( off < code.size() && code.getInteger( off ) == CMql.WHERE ) {
			
			if ( ! hasWhere ) {
				sb.append( " WHERE (" );
			} else {
				sb.append( " AND (" );
			}
			off++;
			
			off = SqlUtils.createWhereClause( getConnection(), driver, off, code, desc, driver.getTypes(), sb, user, getAclManager() );
			sb.append( " ) " );
			hasWhere = true;
		}
		
		if ( SqlUtils.needInternalWhere( desc ) ) {
			if ( ! hasWhere ) {
				sb.append( " WHERE " );
			} else {
				sb.append( " AND " );
			}
			
			SqlUtils.createInternalWhere( driver, desc, sb );
		}
		
		// create the order / limit / offset
		
		while ( off < code.size() ) {
			switch ( code.getInteger( off ) ) {
				case CMql.ORDER:
					off = SqlUtils.createOrderClause( driver, off, code, desc, driver.getTypes(), sb, user, driver.getAclManager() );
					break;
				case CMql.OFFSET:
				case CMql.LIMIT:
					off = SqlUtils.createLimitClause( driver, off, code, desc, driver.getTypes(), sb, user, driver.getAclManager() );
					break;
				default:
					off++; // TODO: should throw an exception
			}
		}
		
		Sth sth = null;
		try {
			sth = pool.aquireStatement();
			String sql = sb.toString();
			ResultSet res = sth.executeQuery( sql );
			if ( findDesc != null )
				return new FindResult( getConnection(), findDesc, driver.createSelectResult( this, desc, res, sth, user ) );
			else
				return driver.createSelectResult( this, desc, res, sth, user );
		} catch (Exception e) {
			if ( sth != null ) try { sth.release(); } catch ( Exception ex ) { log.error( ex ); }
			throw new MorseException( MorseException.SQL_EXCEPTION, e.toString(), e );
		}

	}

	public void close() {
		
	}

	public String getName() {
		return driver.getName();
	}

	public QueryParser getParser() {
		return driver.getQueryParser();
	}

	public byte[] getDefinition() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			driver.getQueryParserProperties().store(baos );
		} catch (IOException e) {
			log.error( e );
			return null;
		}
		return baos.toByteArray();
	}
	
	public StatementPool getPool() {
		return pool;
	}
	
	IAclManager getAclManager() {
		return driver.getAclManager();
	}

	public SqlDriver getDriver() {
		return driver;
	}

	public IConnectionServer getConnection() {
		return connection;
	}

	public void commit() {
		if ( autoCommit ) return;
		try {
			pool.getDb().commit();
		} catch (SQLException e) {
			log.error( e );
		}
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void rollback() {
		if ( autoCommit ) return;
		try {
			pool.getDb().rollback();
		} catch (SQLException e) {
			log.error( e );
		}
	}

	public void setAutoCommit(boolean in) {
		autoCommit = in;
		try {
			pool.getDb().setAutoCommit( in );
		} catch (SQLException e) {
			log.error( e );
		}
	}

	public boolean lock(String id, UserInformation user) throws MorseException {

		if ( log.t4() )
			log.info( "LOCK: " + id + ' ' + user );
		
		ObjectUtil.assetId( id );
		if ( user == null ) throw new MorseException( MorseException.UNKNOWN_USER );
		
		String lockSql = driver.getLockSql( id, IType.TYPE_OBJECT );
		String unlockSql = driver.getUnlockSql( id, IType.TYPE_OBJECT );
		Sth sth = null;
		ITransaction tr = null;
		try {
			sth = pool.aquireStatement();
			tr = connection.startTransaction();
			if ( log.t4() )
				log.debug( "xLOCK: " + id + ' ' + user );
			sth.executeUpdate( lockSql );
			
			IQueryResult res = fetch( id, user, false );
			if ( ! res.next() ) {
				res.close();
				throw new MorseException( MorseException.ACCESS_DENIED, id );
			}
			String lock = res.getString( IAttribute.M_LOCK );
			res.close();
			if ( ObjectUtil.validateId( lock ) && ! lock.equals( user.getUserId() ) )
				return false;

			String sql = "UPDATE r_" + IType.TYPE_OBJECT + " SET " + driver.getColumnName( IAttribute.M_LOCK ) + "='" + user.getUserId() + '"';
			sth.executeUpdate( sql );
			
			connection.maybeCommit( tr );
			tr = null;
			
		} catch ( Exception sqle ) {
			log.error( sqle );
			throw new MorseException( MorseException.ERROR, sqle );
		} finally {
			
			if ( tr != null ) 
				connection.maybeRollback( tr );
			
			try {
				if ( log.t4() )
					log.debug( "xUNLOCK: " + id + ' ' + user );
				sth.executeUpdate( unlockSql );
				sth.release();
			} catch (Exception e) {
				log.error( e );
				throw new MorseException( MorseException.ERROR, e );
			}
			
		}
		
		return false;
	}

	public void unlock(String id, boolean force, UserInformation user) throws MorseException {

		if ( log.t4() )
			log.info( "UNLOCK: " + id + ' ' + user );
		
		ObjectUtil.assetId( id );
		if ( !force && user == null ) throw new MorseException( MorseException.UNKNOWN_USER );
		
		String lockSql = driver.getLockSql( id, IType.TYPE_OBJECT );
		String unlockSql = driver.getUnlockSql( id, IType.TYPE_OBJECT );
		Sth sth = null;
		ITransaction tr = null;
		try {
			sth = pool.aquireStatement();
			tr = connection.startTransaction();
			if ( log.t4() )
				log.debug( "xLOCK: " + id + ' ' + user );
			sth.executeUpdate( lockSql );
			
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
		
			String sql = "UPDATE r_" + IType.TYPE_OBJECT + " SET " + driver.getColumnName( IAttribute.M_LOCK ) + "=''";
			sth.executeUpdate( sql );
			connection.maybeCommit( tr );
			
		} catch ( Exception sqle ) {
			log.error( sqle );
			throw new MorseException( MorseException.ERROR, sqle );
		} finally {
			
			if ( tr != null ) 
				connection.maybeRollback( tr );
			
			try {
				if ( log.t4() )
					log.debug( "xUNLOCK: " + id + ' ' + user );
				sth.executeUpdate( unlockSql );
				sth.release();
			} catch (Exception e) {
				log.error( e );
				throw new MorseException( MorseException.ERROR, e );
			}
		}
		
		
	}
	
	public void store(IObjectRead obj, boolean commit, UserInformation user) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}
	
}
