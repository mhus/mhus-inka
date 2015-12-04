package de.mhu.com.morse.pack.mc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IObjectListener;
import de.mhu.com.morse.channel.IObjectManager;
import de.mhu.com.morse.channel.idx.IIdx;
import de.mhu.com.morse.channel.idx.IdxDriver;
import de.mhu.com.morse.channel.sql.SqlSelectResult;
import de.mhu.com.morse.channel.sql.helper.SqlHelper;
import de.mhu.com.morse.mql.ErrorResult;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.SingleRowResult;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.ASql;
import de.mhu.lib.dtb.DbProvider;
import de.mhu.lib.dtb.StatementPool;
import de.mhu.lib.dtb.Sth;
import de.mhu.lib.log.AL;
import de.mhu.lib.utils.ResourceException;

public class McIdx implements IIdx, IObjectListener, DbProvider {

	private static AL log = new AL( McIdx.class );
	
	private ITypes types;

	private String user;

	private String path;

	private String pass;

	private String schema;

	private SqlHelper helper;

	private String prefix;

	private StatementPool pool;

	private IAclManager aclManager;

	private IConnection defaultConnection;

	private String[] columns;

	private IAttribute[] attributes;

	private String[] versions;

	private HashSet<String> listenedAttributes = new HashSet<String>();
	
	public void initIndex(IdxDriver idxDriver, Map<String, String> tableToMap) throws MorseException {
		
		path   = tableToMap.get( "url" );
		user   = tableToMap.get( "user" );
		pass   = tableToMap.get( "pass" );
		schema = tableToMap.get( "schema" );
		prefix  = tableToMap.get( "prefix" );
		if ( prefix == null ) prefix = "mc_idx";
		
		try {
			helper = (SqlHelper) Class.forName( tableToMap.get( "helper" ) ).newInstance();
		} catch (Exception e) {
			throw new MorseException( MorseException.ERROR, tableToMap.get( "helper" ), e );
		}
		
		try {
			pool = new StatementPool( reconnect(), this );
		} catch (SQLException e) {
			throw new MorseException( MorseException.CANT_CONNECT, e );
		}
		
		helper.setInternalConnection( pool, schema );
		
		types = idxDriver.getTypeManager();
		aclManager = idxDriver.getAclManager();
		defaultConnection = idxDriver.getChannelProvider().getDefaultConnection();
		
		columns = new String [] { 
				CMc.PARENT, 
				IAttribute.M_ID, 
				CMc.NAME, 
				IAttribute.M_ACL, 
				"link", 
				"path", 
				CMc.V_CHRONICLE_ID, 
				CMc.V_VERSION, 
				IAttribute.M_STAMP 
		};
		attributes = new IAttribute[] {
				types.getAttributeByCanonicalName( CMc.MC_OBJECT + '.' + CMc.PARENT ),
				types.getAttributeByCanonicalName( IType.TYPE_OBJECT + '.' + IAttribute.M_ID ),
				types.getAttributeByCanonicalName( CMc.MC_OBJECT + '.' + CMc.NAME ),
				types.getAttributeByCanonicalName( IType.TYPE_OBJECT + '.' + IAttribute.M_ACL ),
				IAttributeDefault.ATTR_OBJ_BOOLEAN,
				IAttributeDefault.ATTR_OBJ_STRING,
				types.getAttributeByCanonicalName( CMc.MC_DOCUMENT + '.' + CMc.V_CHRONICLE_ID ),
				types.getAttributeByCanonicalName( CMc.MC_DOCUMENT + '.' + CMc.V_VERSION ),
				types.getAttributeByCanonicalName( IType.TYPE_OBJECT + '.' + IAttribute.M_STAMP )
		};
		
		versions = new String[] {
				"current",
				"active",
				"staging",
				"wip"
		};
		
		listenedAttributes.add( CMc.PARENT );
		listenedAttributes.add( CMc.MC_LINKS );
		listenedAttributes.add( CMc.NAME );
		listenedAttributes.add( IAttribute.M_ACL );
		listenedAttributes.add( CMc.V_VERSION );
		listenedAttributes.add( IAttribute.M_STAMP );
		for ( String ver : versions )
			listenedAttributes.add( "v_" + ver );
		
		try {
			for ( int i = 0; i < versions.length; i++ ) {
				if ( ! helper.existsTable( prefix + "_" + versions[i] ) )
					createTable( prefix + "_" + versions[i] );
			}
			if ( ! helper.existsTable( prefix + "_f" ) )
				createTable( prefix + "_f" );
			
		} catch (Exception e) {
			
			throw new MorseException( MorseException.ERROR, e );
		}
		
		idxDriver.getObjectManager().registerObjectListener( this );
		
	}

	private void createTable(String name) throws ResourceException, SQLException {
		
		log.info( "Create Tables for " + name );
		StringBuffer create = new StringBuffer().append( helper.getCreateTablePrefixSql() )
				.append( ' ' )
				.append( name ).append( " (" );
		for ( int i = 0; i < columns.length; i++ ) {
			if ( i != 0 ) create.append( ',' );
			create.append( helper.getColumnName( columns[ i ] ) )
			.append( ' ' )
			.append( helper.getColumnDefinition( attributes[ i ], false ) );
			
		}
		
		create.append( " ) " )
			.append( helper.getCreateTableSuffixSql() );
		
		if ( log.t4() ) log.info( "CREATE: " + create.toString() );
		Sth sth = pool.aquireStatement();
		sth.executeUpdate( create.toString() );
				
		// TODO create index over p
		
		sth.release();

	}

	public Connection reconnect() throws SQLException {
		Connection db = DriverManager.getConnection( path, user, pass );
		db.setAutoCommit( true );
		return db;
	}
	
	public void closeIndex() {
		
	}

	public IQueryResult select(IConnectionServer connection, UserInformation user, LinkedList<String> names,
			LinkedList<String> attributes, LinkedList<String[]> where) throws MorseException {
		
		if ( log.t10() ) log.debug( "SELECT: " + names );

		// Objective:
		// mc,folder,[ folders | act | sta | wip | cur ]
		// id=<parent>
		
		// check acl ?
		
		if ( names.size() != 2 )
			throw new MorseException( MorseException.ERROR, "Wrong list of names" );
		
		if ( where.size() == 0 )
			throw new MorseException( MorseException.ERROR, "Empty where clause" );
		
		if ( attributes.size() == 0 )
			throw new MorseException( MorseException.NO_ATTRIBUTES );
		
		Sth sth = null;
		try {
			sth = pool.aquireStatement();
		} catch ( Exception e ) {
			throw new MorseException( MorseException.ERROR, e );
		}
		String[] cols = new String[ attributes.size() ];
		IAttribute[] attrs = new IAttribute[ attributes.size() ];
		StringBuffer sql = new StringBuffer();
		sql.append( "SELECT " );
		for ( int i = 0; i < cols.length; i++ ) {
			cols[ i ] = attributes.get( i ).toLowerCase();
			for ( int j = 0; j < columns.length; j++ ) {
				if ( cols[ i ].equals( columns[ j ] ) ) {
					attrs[ i ] = this.attributes[ j ];
					break;
				}
			}
			if ( attrs[ i ] == null )
				throw new MorseException( MorseException.ATTR_NOT_FOUND, cols[ i ] );
			
			if ( i != 0 ) sql.append( ',' );
			sql.append( cols[i] );
			
		}
		
		if ( ! attributes.contains( IAttribute.M_ACL ) )
			sql.append( "," + IAttribute.M_ACL );

		sql.append( " FROM " ).append( prefix ).append( '_' );
		
		String idxName = names.get( 1 );
		if ( idxName.equals( "folders" ) )
			sql.append( 'f' );
		else {
			for ( int i = 0; i <= versions.length; i++ ) {
				if ( i == versions.length )
					throw new MorseException( MorseException.UNKNOWN_INDEX, idxName );
				if ( idxName.equals( versions[i] ) ) {
					sql.append( versions[i] );
					break;
				}
			}
		}
		
		sql.append( " WHERE " );
		
		// create WHERE
		String offset = null;
		String limit  = null;
		
		boolean isFirst = true;
		for ( Iterator<String[]> i = where.iterator(); i.hasNext(); ) {
			String[] v = i.next();
			if ( v[0].equals( "offset" ) ) {
				offset = v[1];
			} else
			if ( v[0].equals( "limit" ) ) {
				limit = v[1];
			} else {
				if ( ! isFirst )
					sql.append( " AND " );
				if ( v[0].equals( "folder") ) {
					sql.append( "path LIKE " );
					if ( v[1].startsWith( "'" ) && v[1].endsWith( "'" ) )
						sql.append( '\'' ).append( ASql.escape( v[1].substring( 1, v[1].length() - 1 ) ) ).append( "%'" );
					else
						sql.append( '\'' ).append( ASql.escape( v[1] ) ).append( "%'" );
				} else {
					sql.append( v[0] ).append( '=' );
					if ( v[1].startsWith( "'" ) && v[1].endsWith( "'" ) )
						sql.append( '\'' ).append( ASql.escape( v[1].substring( 1, v[1].length() - 1 ) ) ).append( '\'' );
					else
						sql.append( ASql.escape( v[1] ) ); // TODO !!!!!! Security !
				}
				isFirst = false;
			}
		}
		
		
		ResultSet res = null;
		try {
			res = sth.executeQuery( sql.toString() );
		} catch (SQLException e) {
			try { sth.release(); } catch ( Exception ex ) { log.error( ex ); }
			throw new MorseException( MorseException.SQL_EXCEPTION, e );
		}
		
		return new McIdxResult( cols, attrs, res , sth, aclManager, user, offset, limit );
	}

	public IQueryResult rebuild(IConnectionServer connection, UserInformation user, LinkedList<String> names) throws MorseException {

		if ( ! aclManager.isAdministrator( user ) )
			throw new MorseException( MorseException.ACCESS_DENIED );
		
		if ( log.t6() ) log.debug( "REBUILD: " + names );
		
		Sth sth = null;
		try {
			sth = pool.aquireStatement();
			sth.executeUpdate( "DELETE FROM " + prefix + "_f" );
			
			IQueryResult channels = new Query( connection, "SELECT channel FROM m_channel @sys" ).execute();
			while ( channels.next() ) {
				String name = channels.getString( "channel" );
				if ( ! name.equals( "*" ) ) {
					try {
						IQueryResult res = new Query( connection, "SELECT m_type,m_id,mc_parent,name,m_acl,mc_links FROM mc_folder @" + name ).execute();
						while ( res.next() ) {
							insertObject( res );
						}
						res.close();
					} catch ( Throwable e ) {
						if ( log.t3() ) log.warn( "Rebuild from Channel " + name + ": " + e );
					}
				}
			}
			channels.close();
			
			StringBuffer sb = new StringBuffer();
			for ( int i = 0; i < versions.length; i++ ) {
				if ( i != 0 ) sb.append( ',' );
				sb.append( "v_" ).append( versions[i] );
				sth.executeUpdate( "DELETE FROM " + prefix + "_" + versions[i] );
			}
			String versionAttribStr = sb.toString();
			
			channels = new Query( connection, "SELECT channel FROM m_channel @sys" ).execute();
			while ( channels.next() ) {
				String name = channels.getString( "channel" );
				if ( ! name.equals( "*" ) ) {
					try {
						IQueryResult res = new Query( connection, "SELECT m_type,m_id,mc_parent,name,m_acl,mc_links,v_version,v_chronicle_id,m_stamp," + versionAttribStr + " FROM mc_document (all) @" + name ).execute();
						while ( res.next() ) {
							insertObject( res );
						}
						res.close();
						
					} catch ( Throwable e ) {
						if ( log.t3() ) log.warn( "Rebuild from Channel " + name + ": " + e );
					}
				}
			}
			channels.close();
			
			
			if ( log.t6() ) log.debug( "REBUILD PATHES" );
			rebuildPathes( "", "", null );				
			
		} catch (Exception e) {
			if ( e instanceof MorseException ) throw (MorseException)e;
			throw new MorseException( MorseException.ERROR, e );
		}
		
		return new ErrorResult( 0, 0, "" );
	}

	private void rebuildPathes( String parent, String path, String target ) throws ResourceException, SQLException {

		if ( path == null ) {
			if ( parent.length() != 0 ) {
				for ( int i = -1; i < versions.length; i++ ) {
					String ver = "f";
					if ( i >= 0 ) ver = versions[i];
					Sth sth1 = pool.aquireStatement();
					try {
						ResultSet res = sth1.executeQuery( "SELECT path FROM " + prefix + "_" + ver + " WHERE m_id='" + parent + "'" );
						if ( res.next() ) {
							path = res.getString( 1 );
							res.close();
							break;
						}
						res.close();
					} finally {
						if ( sth1 != null ) sth1.release();
					}
				}
			} else {
				path = "";
			}
		}
		
		for ( int i = -1; i < versions.length; i++ ) {
			String ver = "f";
			if ( i >= 0 ) ver = versions[i];

			Sth sth1 = pool.aquireStatement();
			try {
				
				ResultSet res = sth1.executeQuery( "SELECT m_id,name FROM " + prefix + "_" + ver + " WHERE mc_parent='" + parent + "'" );
				LinkedList<String[]> list = new LinkedList<String[]>();
				while ( res.next() ) {
					if ( target == null || target.equals( res.getString( 1 ) ) )
						list.add( new String[] { res.getString( 1 ), res.getString( 2 ) } );
				}
				res.close();
				
				for ( String[] next : list )
					sth1.executeUpdate( "UPDATE " + prefix + "_" + ver + " SET path='" + path + '/' + next[1] + "' WHERE m_id='" + next[0] + "'" );
				
				
				sth1.release();
				sth1 = null;
				
				for ( String[] next : list )
					rebuildPathes( next[0], path + '/' + next[1], null );
					
			} finally {
				if ( sth1 != null ) sth1.release();
			}
			
		}		
	}

	private void insertObject( IObjectRead res ) throws ResourceException, SQLException {
		
		IType type = types.get( res.getString( IAttribute.M_TYPE ) );
		if ( type.isInstanceOf( CMc.MC_FOLDER ) ) {
			Sth sth = pool.aquireStatement();
			String c    = res.getString( IAttribute.M_ID );
			String p    = res.getString( CMc.PARENT );
			String name = res.getString( CMc.NAME );
			String acl  = res.getString( "m_acl" );
			sth.executeUpdate( "INSERT INTO " + prefix + "_f (mc_parent,m_id,name,m_acl,link,v_version,v_chronicle_id,m_stamp) " +
					"VALUES ('" + p + "','" + c + "','" + ASql.escape( name ) + "','" + acl + "',0,0,'',0)" );
			ITableRead links = res.getTable( "mc_links" );
			for ( links.reset(); links.next(); ) {
				p    = res.getString( "id" );
				sth.executeUpdate( "INSERT INTO " + prefix + "_f (mc_parent,m_id,name,m_acl,link,v_version,v_chronicle_id,m_stamp) " +
						"VALUES ('" + p + "','" + c + "','" + ASql.escape( name ) + "','" + acl + "',1,0,'',0)" );
			}
			links.close();
			sth.release();
		} else
		if ( type.isInstanceOf( CMc.MC_DOCUMENT ) ) {
			// document
			for ( int i = 0; i < versions.length; i++ ) {
				try {
					if ( res.getBoolean( "v_" + versions[i] ) ) {
						Sth sth = pool.aquireStatement();
						String c    = res.getString( IAttribute.M_ID );
						String p    = res.getString( CMc.PARENT );
						String name = res.getString( CMc.NAME );
						String acl  = res.getString( "m_acl" );
						String version = res.getString( "v_version" );
						String chronicle = res.getString( "v_chronicle_id" );
						String stamp = res.getString( "m_stamp" );
						
						sth.executeUpdate( "INSERT INTO " + prefix + "_" + versions[i] + " (mc_parent,m_id,name,m_acl,link,v_version,v_chronicle_id,m_stamp) " +
								"VALUES ('" + p + "','" + c + "','" + ASql.escape( name ) + "','" + acl + "',0," + version +",'" + chronicle + "'," + stamp + ")" );
						ITableRead links = res.getTable( "mc_links" );
						for ( links.reset(); links.next(); ) {
							p    = res.getString( "id" );
							sth.executeUpdate( "INSERT INTO " + prefix + "_" + versions[i] + " (mc_parent,m_id,name,m_acl,link,v_version,v_chronicle_id,m_stamp) " +
									"VALUES ('" + p + "','" + c + "','" + ASql.escape( name ) + "','" + acl + "',1," + version +",'" + chronicle + "'," + stamp + ")" );
						}
						links.close();
						sth.release();
					}
				} catch ( MorseException e ) {
					if ( log.t5() ) log.warn( versions[i], e );
				}
			}
		}	
	}
	
	private void removeObject( String id ) throws SQLException, ResourceException {
		
		Sth sth = pool.aquireStatement();
		sth.executeUpdate( "DELETE FROM " + prefix + "_f WHERE m_id='" + id + "'" );
		sth.executeUpdate( "DELETE FROM " + prefix + "_current WHERE m_id='" + id + "'" );
		sth.release();
		
	}
	
	// ObjectListener events
	
	public void eventContentRemoved(String channel, String id, String parentId,
			String parentType) {
	}

	public void eventContentSaved(String channel, String id, String parentId,
			String parentType) {
	}

	public void eventObjectCreated(String channel, String id, String type) {
		
		IType t = types.get( type );
		if ( t == null || ! t.isInstanceOf( CMc.MC_OBJECT ) ) return;
		
		if ( log.t10() ) log.debug( "CREATED: " + id );
		
		try {
			IQueryResult res = new Query( defaultConnection, "FETCH " + id ).execute();
			if ( ! res.next() ) {
				res.close();
				log.debug( "Not found: " + id );
				return;
			}
			insertObject( res );
			rebuildPathes( res.getString( CMc.PARENT ), null, id );
			res.close();
		} catch ( Exception e ) {
			log.error( e );
		}
	}

	public void eventObjectDeleted(String channel, String id, String type) {

		IType t = types.get( type );
		if ( t == null || ! t.isInstanceOf( CMc.MC_OBJECT ) ) return;
		
		if ( log.t10() ) log.debug( "DELETED: " + id );
		
		try {
			removeObject( id );
		} catch ( Exception e ) {
			log.error( e );
		}
	}

	public void eventObjectUpdated(String channel, String id, String type,
			String[] attributes) {

		IType t = types.get( type );
		if ( t == null || ! t.isInstanceOf( CMc.MC_OBJECT ) ) return;
		
		boolean stamp = false;
		if ( attributes != null ) {
			boolean ok = false;
			stamp = true;
			for ( String attr : attributes )
				if ( listenedAttributes.contains( attr ) ) {
					ok = true;
					if ( ! attr.equals( IAttribute.M_STAMP ) ) {
						stamp = false;
						break;
					}
				}
			if ( ! ok ) return;
		}
		
		if ( log.t10() ) log.debug( "UPDATED: " + id );
		
		try {

			if ( stamp ) {
				// TODO update only stamp ...
			} else {
				IQueryResult res = new Query( defaultConnection, "FETCH " + id ).execute();
				if ( ! res.next() ) {
					res.close();
					log.debug( "Not found: " + id );
					return;
				}
				
				removeObject( id );
				insertObject( res );
				
				rebuildPathes( res.getString( CMc.PARENT ), null, id );
	
				res.close();
			}
		} catch ( Exception e ) {
			log.error( e );
		}
		
	}

}
