package de.mhu.com.morse.channel.sql;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import de.mhu.lib.ASql;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.aco.AcoMPos;
import de.mhu.com.morse.aco.AcoString;
import de.mhu.com.morse.aco.IAco;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IChannelDriverServer;
import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IObjectManager;
import de.mhu.com.morse.channel.sql.helper.SqlHelper;
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
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.dtb.Sth;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.utils.IAfLogger;
import de.mhu.lib.utils.Properties;
import de.mhu.lib.utils.ResourceException;

public class SqlDriver extends AfPlugin implements IChannelDriverServer {

	private static Config config = ConfigManager.getConfig( "server" );
	
	private QueryParser queryParser;
	private PropertyQueryDefinition qd = new PropertyQueryDefinition();
	private String name;
	private ITypes types;
	protected SqlChannel internatConnection;
	private String type = IChannelDriver.CT_DB;
	
	private String pass;
	private String path;
	private String user;
	private String accessAcl;
	private IAclManager aclManager;
	private IObjectManager objectManager;
	// private IChannelProvider channelProvider;
	private int nextTmpId = 0;
	private String schema;
	private SqlHelper helper = null;
	
	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	protected void apDisable() throws AfPluginException {
		if ( internatConnection != null )
			internatConnection.close();
		
	}

	protected void apEnable() throws AfPluginException {
		
		aclManager = (IAclManager)getSinglePpi( IAclManager.class );
		objectManager = (IObjectManager)getSinglePpi( IObjectManager.class );
		// channelProvider = (IChannelProvider)getSinglePpi( IChannelProvider.class );
		
		try {
			internatConnection = (SqlChannel) createChannel( null );
			helper.setInternalConnection( internatConnection.getPool(), schema );
			// validate db types
			types = (ITypes)getSinglePpi( ITypes.class );
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
			
			// String[] oldTypes = p.getProperty( "types", "" ).split( "," );
			
			// write / update tables
			
			boolean changed = false;
			for ( Iterator i = types.getTypes(); i.hasNext(); ) {
				IType type = (IType)i.next();
				if ( type.isInChannel( this ) ) {
					p.setProperty( "type." + type.getName(), type.getName() );
					knownTypes.remove( "type." + type.getName() );
					boolean c = validateType( type );
					if ( c == true ) changed = true;
				}
			}
		
			// remove unused types (not for the sys channel)
			if ( ! "sys".equals( name ) ) {
				for ( Iterator i = knownTypes.values().iterator(); i.hasNext(); ) {
					String key = (String)i.next();
					dropViews( key );
					dropTables( key );
					// TODO drop / remember also r_ tables
				}
			}
			// rewrite views
			
			if ( changed ) {
				for ( Iterator i = types.getTypes(); i.hasNext(); ) {
					IType type = (IType)i.next();
					if ( type.isInChannel( this ) ) {
						createViews( type );
					}
				}
			}
			
			internatConnection.commit();
			
			setFeatures( p );
			
		} catch ( Throwable th ) {
			throw new AfPluginException( 1, th );
		}
	}

	IQueryResult createSelectResult(SqlChannel channel, Descriptor desc, ResultSet res, Sth sth, UserInformation user) {
		return new SqlSelectResult( this, channel, desc, res, sth, user );
	}
	
	protected String getDropViewPrefixSql() {
		return helper.getDropViewPrefixSql();
	}
	
	protected String getCreateViewPrefixSql() {
		return helper.getCreateViewPrefixSql();
	}
	
	protected String getDropTablePrefixSql() {
		return helper.getDropTablePrefixSql();
	}
	
	protected String getCreateTablePrefixSql( IType type, IAttribute attr ) {
		return helper.getCreateTablePrefixSql();
	}
	
	protected String getCreateTableSuffixSql( IType type, IAttribute attr ) {
		return helper.getCreateTableSuffixSql();
	}
	
	protected String getCreateTablePrimaryKeySql(IType type, String[] names) {
		return helper.getCreateTablePrimaryKeySql( names);
	}

	protected String getAlterTablePrefixSql() {
		return helper.getAlterTablePrefixSql();
	}
	
	protected String getCreateIndexSql( int index, IType type, IAttribute subTable, IAttribute attr ) {
		return helper.getCreateIndexSql(
				index, 
				
				( subTable != null ? "r_" : "t_" ) 
				+ type.getName() 
				+ ( subTable != null ? "_" + subTable.getName() : "" )
				+ "_" + attr.getName(),
				
				( subTable != null ? "r_" : "t_" ) 
				+ type.getName()
				+ ( subTable != null ? "_" + subTable.getName() : "" ),
				
				new String[] {
					( subTable != null ? getColumnName( IAttribute.M_ID ) + "," : "" ) 
					+ getColumnName( attr.getName() )
				}
				
		);
	}
	
	protected String getColumnDefinition( IAttribute attr, boolean primaryKey ) throws MorseException {
		return helper.getColumnDefinition(attr, primaryKey);
	}
	
	public String toValidDate(Date date) {
		return helper.toValidDate(date);
	}

	public String getLockSql( String id, String table ) {
		return helper.getLockSql(id, table);
	}
	
	public String getUnlockSql( String id, String table ) {
		return helper.getUnlockSql(id, table);
	}

	protected String getCreateTmpTablePrefixSql() {
		return helper.getCreateTmpTablePrefixSql();
	}
	
	protected String getCreateTmpTableSuffixSql() {
		return helper.getCreateTmpTableSuffixSql();
	}
	
	protected String getDropTmpTableSql( String name ) {
		return helper.getDropTmpTableSql(name);
	}
	
	protected String getCreateTmpIndexSql( int index, String table, String attr ) {
		return helper.getCreateTmpIndexSql(index, table, attr);
	}

	private void dropViews( String typeName ) throws ResourceException {
		
		Sth sth = null;
		try {
			sth = internatConnection.getPool().aquireStatement();
			try {
				String sql = getDropViewPrefixSql() + " v_" + typeName;
				sth.executeUpdate( sql );
			} catch( Exception e ) {
				log().error( e );
			}
			/*
			try {
				String sql = getDropViewPrefixSql() + " w_" + typeName;
				sth.executeUpdate( sql );
			} catch( Exception e ) {
				log().error( e );
			}
			*/
		} finally {
			if ( sth != null )
				sth.release();
		}
	}
	
	private void dropTables( String typeName ) throws MorseException {
		
		Sth sth = null;
		try {
			sth = internatConnection.getPool().aquireStatement();
			try {
				String sql = getDropTablePrefixSql() + " t_" + typeName;
				sth.executeUpdate( sql );
			} catch( Exception e ) {
				log().error( e );
			}
		} catch ( ResourceException re ) {
			if ( re instanceof MorseException ) throw (MorseException)re;
			throw new MorseException( MorseException.ERROR, re );
		} finally {
			if ( sth != null )
				try { sth.release(); } catch ( Exception ex ) { log().error( ex ); }
		}
	}
	
	private void createViews( IType type ) throws MorseException, SQLException {
		
		Sth sth = null;
		try {
			String name      = type.getName();
			IType  superType = type.getSuperType();
			String superName = type.getSuperName();
			sth = internatConnection.getPool().aquireStatement();
			boolean notExisits = true;
		
			String table = "v_" + name;
			StringBuffer create = new StringBuffer();			
			create.append( getCreateViewPrefixSql() ).append( ' ').append( table ).append( " AS SELECT t_" + IType.TYPE_OBJECT + '.' + getColumnName( IAttribute.M_ID ) );
			
			if ( superName != null && superName.length() != 0 ) {
				
				Hashtable usedTypes = new Hashtable();
				usedTypes.put( name , "" );
				for ( Iterator i = type.getAttributes(); i.hasNext(); ) {
					IAttribute attr = (IAttribute)i.next();
					if ( 		
							 	! attr.isTable() 
							&& 	! attr.getName().equals( IAttribute.M_ID ) 
						) {
						
						create.append( ",t_" ).append( attr.getSourceType().getName() ).append( '.' ).append( getColumnName( attr.getName() ) );
						usedTypes.put( attr.getSourceType().getName(), attr.getSourceType() );
					}
					
				}
				
				create.append( " FROM t_" + IType.TYPE_OBJECT );
				for ( Iterator i = usedTypes.keySet().iterator(); i.hasNext(); ) {
					String n = (String)i.next();
					if ( ! n.equals( IType.TYPE_OBJECT ) ) {
						create.append( ",t_" ).append( n );
					}
				}
				
				create.append( " WHERE " );
				usedTypes.put( IType.TYPE_OBJECT, "" );
				boolean isFirst = true;
				for ( Iterator i = usedTypes.keySet().iterator(); i.hasNext(); ) {
					String n = (String)i.next();
					if ( ! n.equals( name ) ) {
						if ( ! isFirst )
							create.append( " AND " );
						create.append( "t_" ).append( n ).append( "." + getColumnName( IAttribute.M_ID ) + "=t_" ).append( name ).append( "." + getColumnName( IAttribute.M_ID ) );
						isFirst = false;
					}
				}
				
				
			} else {
				
				create.append( " FROM t_" + IType.TYPE_OBJECT );
				
			}
			
			String drop = getDropViewPrefixSql() + ' ' + table;
			if ( log().t4() ) log().info( "VIEW: " + drop );
			try {
				sth.executeUpdate( drop );
			} catch ( Exception ex ) {
				// not really important ...
				log().info( "DROP VIEW: " + ex );
			}
			if ( log().t4() ) log().info( "VIEW: " + create.toString() );
			sth.executeUpdate( create.toString() );
			
		} catch ( ResourceException re ) {
			if ( re instanceof MorseException ) throw (MorseException)re;
			throw new MorseException( MorseException.ERROR, re );
		} finally {
			if ( sth != null )
				try { sth.release(); } catch ( Exception ex ) { log().error( ex ); } 
		}
	
	}
	
	protected boolean existsTable( String name ) throws SQLException {
		return helper.existsTable( name );
	}
	
	protected ResultSet getTableColumns( String name ) throws SQLException {
		return helper.getTableColumns( name );
	}
	
	private boolean validateType(IType type) throws SQLException, MorseException {
		
		Sth sth = null;
		ResultSet res = null;
		try {
			String name      = type.getName();
			IType  superType = type.getSuperType();
			String superName = type.getSuperName();
			IAttribute objectIdAttr = types.getAttributeByCanonicalName( IType.TYPE_OBJECT + '.' + IAttribute.M_ID );
			sth = internatConnection.getPool().aquireStatement();
			
			String table = "t_" + name;
			
			if ( ! existsTable( table ) ) {
				log().info( "Create Tables for " + name );
				
				StringBuffer create = new StringBuffer().append( getCreateTablePrefixSql( type, null ) ).append( ' ' ).append( table ).append( " (" );
				create.append( getColumnName( IAttribute.M_ID ) ).append( ' ' ).append( getColumnDefinition( objectIdAttr, true ) );
				
				for ( Iterator i = type.getAttributes(); i.hasNext(); ) {
					IAttribute attr = (IAttribute)i.next();
					if ( attr.getSourceType().getName().equals( name ) && ! attr.isTable() && ! attr.getName().equals( IAttribute.M_ID ) ) {
						create.append(',').append( getColumnName( attr.getName() ) ).append( ' ' ).append( getColumnDefinition( attr, false ) );
					}
				}
				
				create.append(',').append( getCreateTablePrimaryKeySql( type, new String[] { getColumnName( IAttribute.M_ID ) } ) );
				create.append( " ) " );
				create.append( getCreateTableSuffixSql( type, null ) );
				
				if ( log().t4() ) log().info( "CREATE: " + create.toString() );
				sth.executeUpdate( create.toString() );
				// internatConnection.db.commit();
				
				for ( Iterator i = type.getAttributes(); i.hasNext(); ) {
					IAttribute attr = (IAttribute)i.next();
					if ( ! attr.getName().equals( IAttribute.M_ID ) && attr.getSourceType().getName().equals( type.getName() ) ) {
						String sql = getCreateIndexSql( attr.getIndexType(), type, null, attr );
						if ( sql != null ) {
							if ( log().t4() ) log().info( "CREATE: " + sql );
							sth.executeUpdate( sql );						
						}
					}
				}
				
				for ( Iterator i = type.getAttributes(); i.hasNext(); ) {
					IAttribute attr = (IAttribute)i.next();
					if ( attr.getSourceType().getName().equals( name ) && attr.isTable() ) {
						create = new StringBuffer().append( getCreateTablePrefixSql( type, attr ) ).append( " r_" ).append( name ).append('_').append( attr.getName() ).append( " (" );
						create.append( getColumnName( IAttribute.M_ID ) ).append( ' ' ).append( getColumnDefinition( objectIdAttr, true ) );
						create.append( ',' ).append( getColumnName( IAttribute.M_POS ) ).append( ' ' ).append( getColumnDefinition( IAttributeDefault.ATTR_OBJ_M_POS, true ) );
						for ( Iterator j = attr.getAttributes(); j.hasNext(); ) {
							IAttribute a = (IAttribute)j.next();
							create.append(',').append( getColumnName( a.getName() ) ).append( ' ' ).append( getColumnDefinition( a, false ) );
						}
						create.append(',').append( getCreateTablePrimaryKeySql( type, new String[] { getColumnName( IAttribute.M_ID ), getColumnName( IAttribute.M_POS ) } ) );
						create.append( " ) " ).append( getCreateTableSuffixSql( type, attr ) );
						
						if ( log().t4() ) log().info( "CREATE: " + create.toString() );
						sth.executeUpdate( create.toString() );
						// internatConnection.db.commit();
						
						for ( Iterator j = attr.getAttributes(); j.hasNext(); ) {
							IAttribute a = (IAttribute)j.next();
							if ( ! attr.getName().equals( IAttribute.M_ID ) && ! attr.getName().equals( IAttribute.M_POS ) ) {
								String sql = getCreateIndexSql( a.getIndexType(), type, attr, a );
								if ( sql != null ) {
									if ( log().t4() ) log().info( "CREATE: " + sql );
									sth.executeUpdate( sql );						
								}
							}
						}
					}
				}
				
				return true;
						
				
			} else {
				
				// validate attributes
				
				res = getTableColumns( table );
				
				Hashtable cols = new Hashtable();
				
				boolean changed = false;
				
				while ( res.next() ) {
					String columnName = getAttrName( res.getString("COLUMN_NAME") ); 
//					String datatype = res.getString("TYPE_NAME"); 
//					int datasize = res.getInt("COLUMN_SIZE"); 
//					int digits = res.getInt("DECIMAL_DIGITS"); 
//					int nullable = res.getInt("NULLABLE"); 
//					boolean isNull = (nullable == 1); 
					
					IAttribute attr = type.getAttribute( columnName );
					if ( attr == null ) {
						StringBuffer alter = new StringBuffer();
						alter.append( "ALTER TABLE " ).append( table ).append( " DROP " ).append( columnName );
						if ( log().t4() ) log().info( "ALTER: " + alter.toString() );
						sth.executeUpdate( alter.toString() );
						// internatConnection.db.commit();
						changed =true;
					}

					cols.put( columnName.toUpperCase(), columnName );
				}
				
				res.close();
				
				for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
					IAttribute attr = i.next();
					if ( attr.getSourceType().getName().equals( name ) && 
							! attr.isTable() &&
							cols.get( attr.getName().toUpperCase() ) == null ) {
						
						StringBuffer alter = new StringBuffer();
						alter	.append( getAlterTablePrefixSql() ).append( ' ' )
								.append( table ).append( " ADD " )
								.append( getColumnName( attr.getName() ) ).append( ' ' )
								.append( getColumnDefinition( attr, false ) );
						
						if ( log().t4() ) log().info( "ALTER: " + alter.toString() );
						sth.executeUpdate( alter.toString() );
						// internatConnection.db.commit();
						changed =true;
					}
					
				}
				
				res = internatConnection.getPool().getDb().getMetaData().getColumns( null, schema, "r_" + name + "_%" , "%" );
				
				while ( res.next() ) {
					
					String tableName = res.getString("TABLE_NAME").substring( name.length() + 3 );
					
					IAttribute attr = type.getAttribute( tableName );
					
					if ( attr == null ) {
						StringBuffer alter = new StringBuffer();
						alter.append( "DROP TABLE " ).append( "r_" ).append( name ).append( '_' ).append( tableName );
						if ( log().t4() ) log().info( "ALTER: " + alter.toString() );
						try {
							sth.executeUpdate( alter.toString() );
						} catch ( SQLException e ) {
							log().error( e );
						}
						// internatConnection.db.commit();
						changed =true;
					}
					
				}
				
				res.close();
				res = null;
				
				for ( Iterator i = type.getAttributes(); i.hasNext(); ) {
					IAttribute attr = (IAttribute)i.next();
					if ( attr.getSourceType().getName().equals( name ) && attr.isTable() ) {
						
						if ( ! existsTable( "r_" + name + "_" + attr.getName() ) ) {
							
							StringBuffer alter = new StringBuffer();
							alter	.append( getCreateTablePrefixSql( type, attr ) ).append( " r_" )
									.append( name ).append( '_' ).append( attr.getName() )
									.append( " (" );
							alter	.append( getColumnName( IAttribute.M_ID ) ).append( ' ' )
									.append( getColumnDefinition( objectIdAttr, true ) );
							alter	.append( ',' ).append( getColumnName( IAttribute.M_POS ) ).append( ' ' )
									.append( getColumnDefinition( IAttributeDefault.ATTR_OBJ_M_POS, true ) );
							for ( Iterator j = attr.getAttributes(); j.hasNext(); ) {
								IAttribute a = (IAttribute)j.next();
								alter	.append(',').append( getColumnName( a.getName() ) ).append( ' ' )
										.append( getColumnDefinition( a, false ) );
							}
							alter	.append(',').append( 
									getCreateTablePrimaryKeySql( type, new String[] { 
											getColumnName( IAttribute.M_ID ), getColumnName( IAttribute.M_POS ) } ) );
							
							alter.append( " ) " ).append( getCreateTableSuffixSql( type, attr ) );
							
							if ( log().t4() ) log().info( "CREATE: " + alter.toString() );
							sth.executeUpdate( alter.toString() );
							// internatConnection.db.commit();		
							
							for ( Iterator j = attr.getAttributes(); j.hasNext(); ) {
								IAttribute a = (IAttribute)j.next();
								if ( ! attr.getName().equals( IAttribute.M_ID ) && ! attr.getName().equals( IAttribute.M_POS ) ) {
									String sql = getCreateIndexSql( a.getIndexType(), type, attr, a );
									if ( sql != null ) {
										if ( log().t4() ) log().info( "CREATE: " + sql );
										sth.executeUpdate( sql );						
									}
								}
							}
							
							changed =true;
							
						}
						
					}
				}
				
				if ( changed ) return true;
			}
			
		} catch ( ResourceException re ) {
			if ( re instanceof MorseException ) throw (MorseException)re;
			throw new MorseException( MorseException.ERROR, re );
		} finally {
			if ( sth != null ) try { sth.release(); } catch ( Exception ex ) { log().error( ex ); }
		}
		
		return false;
	}

	protected void apInit() throws Exception {
		
		// name = config().getProperty( "name" );
		helper.sqlInit();
		appendPpi( IChannelDriver.class, this );
		
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}

	public IChannelServer createChannel( IConnectionServer pConnection ) throws MorseException {
		getParser();
		return new SqlChannel( this, pConnection );
	}

	public IChannelServer createChannel(IConnection pConnection) throws MorseException {
		if ( pConnection instanceof IConnectionServer )
			return createChannel( (IConnectionServer)pConnection );
		throw new MorseException( MorseException.ERROR, "NOT A Server Channel: " + pConnection );
	}
	
	public QueryParser getParser() {
		if ( queryParser == null ) {
			try {
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/db.properties" ) );
				qd.load( getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.package" ) + "/statics.properties" ) );				
			} catch (IOException e) {
				log().error( e );
			}
			queryParser = new QueryParser( qd );
		}
		return queryParser;
	}
	
	public Iterator getObjectIds() {
		try {
			final Sth st = internatConnection.getPool().aquireStatement();
			final ResultSet res = st.executeQuery( "SELECT " + getColumnName( IAttribute.M_ID ) + " FROM t_" + IType.TYPE_OBJECT );
			return new Iterator() {

				private boolean isNext;

				{
					isNext = res.next();
				}
				public boolean hasNext() {
					return isNext;
				}

				public Object next() {
					try {
					if ( ! isNext ) return null;
					String val = res.getString( 1 );
					isNext = res.next();
					if ( ! isNext ) {
						res.close();
						st.release();
					}
					return val;
					} catch ( Exception e ) {
						log().error( e );
						return null;
					}
				}

				public void remove() {
				}
				
			};
		} catch (SQLException e) {
			log().error( e );
		} catch (ResourceException e) {
			log().error( e );
		}
		return null;
	}
	
	protected void checkFeatures() {
		
		Sth sth = null;
		try {
			if ( ! existsTable( "features_" ) ) {
				
				String sql = getCreateTablePrefixSql( null, null ) + " features_ ( k " + 
							 getColumnDefinition( IAttributeDefault.ATTR_OBJ_STRING, false ) + 
							 ", v " + getColumnDefinition( IAttributeDefault.ATTR_OBJ_STRING, false ) + " ) " +
							 getCreateTableSuffixSql( null, null );

				sth = internatConnection.getPool().aquireStatement();
				sth.executeUpdate( sql );
			}
		} catch ( Exception e ) {
			log().error( e );
		} finally {
			if ( sth != null )
				try {
					sth.release();
				} catch (Exception e) {
					log().error( e );
				}
		}
				
	}
	
	public Properties getFeatures() {
		
		checkFeatures();
		
		String sql = "SELECT k,v FROM features_";
		Properties p = new Properties();
		Sth sth = null;
		ResultSet res = null;
		try {
			sth = internatConnection.getPool().aquireStatement();
			res = sth.executeQuery( sql );
			while ( res.next() )
				p.put( res.getString(1), res.getString(2) );
		} catch ( Exception e ) {
			log().error( e );
		} finally {
			try {
				if ( res != null )
					res.close();
				if ( sth != null )
					sth.release();
			} catch (Exception e) {
				log().error( e );
			}
		}
		return p;
	}

	public void setFeatures(Properties features) {
		
		checkFeatures();
		
		features.setProperty( "m_modify_date", new Date().toGMTString() );
		Sth sth = null;
		try {
			sth = internatConnection.getPool().aquireStatement();
			String sql = "DELETE FROM features_";
			sth.executeUpdate( sql );
			
			for ( Iterator i = features.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry entry = (Map.Entry)i.next();
				sql = "INSERT INTO features_ (k,v) VALUES ('" + ASql.escape( (String)entry.getKey() ) + "','" + ASql.escape( (String)entry.getValue() ) + "')";
				sth.executeUpdate( sql );
			}
			
			internatConnection.commit();
			
		} catch ( Exception e ) {
			log().error( e );
			internatConnection.rollback();
		} finally {
			try {
				if ( sth != null )
					sth.release();
			} catch (Exception e) {
				log().error( e );
			}
		}
	}

	public void insertRecords(String typeName, IQueryResult res) {
		Sth sth = null;
		try {
			sth = internatConnection.getPool().aquireStatement();
			IType type = types.get( typeName );
			while ( res.next() ) {
				
				String dbName = res.getString( "_DESTINATION" );
				if ( dbName != null && dbName.length() != 0 && dbName.equals( name ) ) {
				
					String id = res.getString( IAttribute.M_ID );
					String sql = "DELETE FROM t_" + typeName + " WHERE " + getColumnName( IAttribute.M_ID ) + "='" + id + "'";
					if ( log().t4() ) log().info( sql );
					sth.executeUpdate(sql);
					StringBuffer sqlNames  = new StringBuffer().append( "INSERT INTO t_" ).append( typeName ).append( " ( " ).append( getColumnName( IAttribute.M_ID ) );
					StringBuffer sqlValues = new StringBuffer().append( " VALUES (" ).append( '\'' ).append( id ).append( '\'' );
					for ( Iterator<IAttribute> a = type.getAttributes(); a.hasNext(); ) {
						IAttribute attr = a.next();
						if ( attr.getSourceType().getName().equals( typeName ) && !attr.getName().equals( IAttribute.M_ID ) ) {
							if ( attr.isTable() ) {
							
								// the same ....
								sql = "DELETE FROM r_" + typeName + "_" + attr.getName() + " WHERE " + getColumnName( IAttribute.M_ID ) + "='" + id + "'";
								if ( log().t4() ) log().info( sql );
								sth.executeUpdate(sql);
								ITableRead resTable = res.getTable( attr.getName() );
								int cnt = 0;
								while ( resTable.next() ) {
									StringBuffer sqlNamesT  = new StringBuffer().append( "INSERT INTO r_" ).append( typeName ).append( '_' ).append( attr.getName() ).append( " ( " ).append( getColumnName( IAttribute.M_ID ) ).append(',').append( getColumnName( IAttribute.M_POS ) );
									StringBuffer sqlValuesT = new StringBuffer().append( " VALUES (" ).append( '\'' ).append( id ).append( "'," ).append( cnt );
									
									for ( Iterator<IAttribute> t = attr.getAttributes(); t.hasNext(); ) {
										IAttribute attrTable = t.next();
										sqlNamesT.append( ',' ).append( getColumnName( attrTable.getName() ) );
										sqlValuesT.append( ',' );
										if ( AttributeUtil.needQuots( attrTable.getType() ) )
											sqlValuesT.append( '\'' );
										sqlValuesT.append( ASql.escape( resTable.getString( attrTable.getName() ) ) );
										if ( AttributeUtil.needQuots( attrTable.getType() ) )
											sqlValuesT.append( '\'' );
										
									}
									sqlNamesT.append( ')' );
									sqlValuesT.append( ')' );
									sqlNamesT.append( sqlValuesT.toString() );
									sql = sqlNamesT.toString();
									if ( log().t4() ) log().info( sql );								
									sth.executeUpdate( sql );
									
									cnt++;
								}
							
							} else {
								sqlNames.append( ',' ).append( getColumnName( attr.getName() ) );
								sqlValues.append( ',' );
								if ( AttributeUtil.needQuots( attr.getType() ) )
									sqlValues.append( '\'' );
								sqlValues.append( ASql.escape( res.getString( attr.getName() ) ) );
								if ( AttributeUtil.needQuots( attr.getType() ) )
									sqlValues.append( '\'' );
							}
						}
	
					}
									
					sqlNames.append( ')' );
					sqlValues.append( ')' );
					sqlNames.append( sqlValues.toString() );
					sql = sqlNames.toString();
					if ( log().t4() ) log().info( sql );
					sth.executeUpdate( sql );
					
				}
				
			}
			
			internatConnection.commit();
			
		} catch ( Exception e ) {
			log().error( e );
			internatConnection.rollback();
		} finally {
			try {
				if ( sth != null )
					sth.release();
			} catch (Exception e) {
				log().error( e );
			}
		}	
	}
	
	public void setChannel(String in) {
		name = in;
	}

	public void setPass(String in) {
		pass = in;
	}

	public void setPath(String in) {
		path = in;
	}

	public void setUser(String in) {
		user = in;
	}
	
	public void setChannelFeatures( Map<String, String> features ) throws MorseException {
		path = features.get( "url" );
		user = features.get( "user" );
		pass = features.get( "pass" );
		schema = features.get( "schema" );
		try {
			helper = (SqlHelper) getClass().forName( features.get( "helper" ) ).newInstance();
		} catch (Exception e) {
			throw new MorseException( MorseException.ERROR, features.get( "helper" ), e );
		}
	}
	
	public void setAccessAcl( String in ) {
		accessAcl = in;
	}

	public String getUser() {
		return user;
	}

	String getPass() {
		return pass;
	}

	String getPath() {
		return path;
	}

	ITypes getTypes() {
		return types;
	}

	QueryParser getQueryParser() {
		return queryParser;
	}

	Properties getQueryParserProperties() {
		return qd;
	}

	IAclManager getAclManager() {
		return aclManager;
	}
	
	IObjectManager getObjectManager() {
		return objectManager;
	}

	String getAccessAcl() {
		return accessAcl;
	}

	public String getColumnName(String attrName) {
		return helper.getColumnName( attrName );
	}
	
	public String getAttrName(String columnName) {
		return helper.getAttrName( columnName );
	}

	public String getTableAs() {
		return helper.getTableAs();
	}
 
	public IQueryResult createInsertResult(SqlChannel channel, String newId ) {		
		return new SqlInsertResult( newId );
	}

	public boolean canTransaction() {
		return true;
	}
	/*
	public IChannelProvider getChannelProvider() {
		if ( channelProvider == null )
			channelProvider = (IChannelProvider)getSinglePpi( IChannelProvider.class );
		return channelProvider;
	}    
	*/

	public synchronized long getNextTmpId() {
		return nextTmpId++;
	}
}
