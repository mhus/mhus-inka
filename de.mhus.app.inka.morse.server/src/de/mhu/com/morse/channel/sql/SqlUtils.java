package de.mhu.com.morse.channel.sql;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.mhu.lib.ASql;
import de.mhu.lib.dtb.Sth;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.channel.IChannelDriverServer;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IQueryFunction;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.channel.ITransaction;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.obj.ITable;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.com.morse.utils.ServerTypesUtil;

public class SqlUtils {
	
	private static AL log = new AL( SqlUtils.class );
	private static Hashtable<String, Btc> hintObjectCache = new Hashtable<String, Btc>();
	
	/**
	 * Find the table names and aliases in the query. The function will fill the
	 * descriptor with all needed tables. 
	 * 
	 * @param off
	 * @param code
	 * @param desc
	 * @return
	 * @throws MorseException
	 */
	public static int findTables( int off, ICompiledQuery code, Descriptor desc ) throws MorseException {
		while ( off < code.size() ) {
			Table newTable = new Table();
			newTable.name = code.getString( off );
			off++;
			// if short ....
			if ( code.getInteger(off ) == CMql.SHORT ) {
				off++;
				newTable.isShort = true;
			}
			if ( code.getInteger( off ) == CMql.OPEN ) {
				off++;
				while ( code.getInteger( off ) != CMql.CLOSE ) {
					newTable.addHint( code.getString( off ) );
					off++;
					if ( code.getInteger( off ) == CMql.COMMA )
						off++;
				}
				off++;
			}
			// alias and name from query
			if ( code.getInteger(off ) == CMql.AS ) {
				off++;
				newTable.alias = code.getString(off);
				off++;
			}
			
			desc.addTable( newTable );
			
			if ( code.getInteger(off) == CMql.COMMA )
				off++;
			else
				return off;
			
		}
		return off;
	}
	
	/**
	 * Check the tables in the descriptor. Find the type / attribute. Collect
	 * all attributes and fill the attrMap of the descriptor.
	 *  
	 * @param desc
	 * @param types
	 * @param user
	 * @param aclm
	 * @throws MorseException
	 */
	public static void checkTables( Descriptor desc, ITypes types, IConnection con, UserInformation user, IAclManager aclm ) throws MorseException {
		for ( int nt = 0; nt < desc.tableSize; nt++ ) {
			Table newTable = desc.tables[ nt ];

			// if type is sub table
			if ( newTable.name.indexOf('.') > 0 ) {
				IAttribute a2 = types.getAttributeByCanonicalName( newTable.name );
				if ( a2 == null )
					throw new MorseException( MorseException.UNKNOWN_TYPE, newTable.name );
				newTable.type = a2.getSourceType();
				newTable.attr = a2;
				//newTable.tableName = "r_" + newTable.name.replace( '.', '_' );
				newTable.tableName = "r_" + newTable.attr.getSourceType().getName() + '_' + newTable.attr.getName();
				// a.alias = a.alias.replace( '.', '_' );
				if ( newTable.isShort )
					throw new MorseException( MorseException.SHORT_NOT_FOR_SUB_TABLES, newTable.name );
				newTable.mqlAlias = newTable.alias;
				if ( newTable.alias == null ) {
					newTable.alias = newTable.name.replace( '.', '_' );
					newTable.mqlAlias = newTable.name;
				}
			} else {
				newTable.type = types.get( newTable.name );
				if ( newTable.type == null ) 
					throw new MorseException( MorseException.UNKNOWN_TYPE, newTable.name );
				if ( newTable.isShort )
					newTable.tableName = "t_" + newTable.type.getName();
				else
					newTable.tableName = "v_" + newTable.type.getName();
				if ( newTable.alias == null )
					newTable.alias = newTable.name;
				newTable.mqlAlias = newTable.alias;
			}
			
			findTableHintObject( con, newTable );
			
			if ( ! aclm.hasRead( user, newTable.type.getAccessAcl() ) )
				throw new MorseException( MorseException.ACCESS_DENIED_READ, new String[] { "type", newTable.name, newTable.type.getAccessAcl(), user.getUserId() } );
			
			// collect all attributes
			if ( newTable.attr != null ) {
				
				// extra M_ID
				
				String name = IAttribute.M_ID;
				
				Object[] entry = desc.attrMap.get( name );
				if ( entry != null && entry.length != 0 ) {
					desc.attrMap.put( name, new Object[ 0 ] ); // Set to ambigious
				} else
				if ( entry == null ) {
					desc.attrMap.put( name, new Object[] { newTable, IAttributeDefault.ATTR_OBJ_M_ID, "d", name } );
				}
				
				// full path
				String nameF = newTable.mqlAlias + '.' + name;
				entry = desc.attrMap.get( nameF );
				if ( entry != null )
					throw new MorseException( MorseException.ATTR_AMBIGIOUS, nameF );
				
				desc.attrMap.put( nameF, new Object[] { newTable, IAttributeDefault.ATTR_OBJ_M_ID, "f", newTable.alias + '.' + name } );
				
				// extra M_POS
				
				name = IAttribute.M_POS;
				
				entry = desc.attrMap.get( name );
				if ( entry != null && entry.length != 0 ) {
					desc.attrMap.put( name, new Object[ 0 ] ); // Set to ambigious
				} else
				if ( entry == null ) {
					desc.attrMap.put( name, new Object[] { newTable, IAttributeDefault.ATTR_OBJ_M_POS, "d", name } );
				}
				
				// full path
				nameF = newTable.mqlAlias + '.' + name;
				entry = desc.attrMap.get( nameF );
				if ( entry != null )
					throw new MorseException( MorseException.ATTR_AMBIGIOUS, nameF );
				
				desc.attrMap.put( nameF, new Object[] { newTable, IAttributeDefault.ATTR_OBJ_M_POS, "f", newTable.alias + '.' + name } );
				
			}
			
			for ( Iterator i = ( newTable.attr == null ? newTable.type.getAttributes() : newTable.attr.getAttributes() ); i.hasNext(); ) {
				IAttribute ax = (IAttribute)i.next();
				
				if ( ! newTable.isShort || ax.getSourceType().getName().equals( newTable.name ) || ax.getName().equals( IAttribute.M_ID ) ) {
					// direct path
					String name = ax.getName();
					
					if ( aclm.hasRead( user, ax.getAccessAcl() ) ) {
						
						Object[] entry = desc.attrMap.get( name );
						if ( entry != null && entry.length != 0 ) {
							desc.attrMap.put( name, new Object[ 0 ] ); // Set to ambigious
						} else
						if ( entry == null ) {
							desc.attrMap.put( name, new Object[] { newTable, ax, "d", name } );
						}
						
						// full path
						name = newTable.mqlAlias + '.' + ax.getName();
						entry = desc.attrMap.get( name );
						if ( entry != null )
							throw new MorseException( MorseException.ATTR_AMBIGIOUS, name );
						
						desc.attrMap.put( name, new Object[] { newTable, ax, "f", newTable.alias + '.' + ax.getName() } );
	
					}

				}
				
			}
		}
	}
	
	private static void findTableHintObject( IConnection con, Table table) {
		
		if ( table.isShort || table.attr != null ) return;
		
		String typeName = table.type.getName();
		synchronized ( hintObjectCache ) {
			table.hintObject = hintObjectCache.get( typeName );
			if ( table.hintObject == null ) {
				table.hintObject = ServerTypesUtil.createBtc( con, table.type );
				hintObjectCache.put( typeName, table.hintObject );
			}
		}
	}

	/**
	 * Parse the attributes and aliases from the query.
	 * 
	 * @param off
	 * @param code
	 * @param desc
	 * @return
	 * @throws MorseException
	 */
	public static int findAttributes( int off, ICompiledQuery code, Descriptor desc ) throws MorseException {
		while ( off < code.size() ) {
			Attr a = new Attr();
			a.name = code.getString( off );
			off++;
			if ( code.getInteger( off ) == CMql.OPEN ) {
				off++;
				a.function = new LinkedList<String>();
				while ( code.getInteger( off ) != CMql.CLOSE ) {
					a.function.add( code.getString( off ) );
					off++;
					if ( code.getInteger( off ) == CMql.COMMA )
						off++;
				}
				off++;
				if ( off < code.size() && code.getInteger( off ) == CMql.INIT ) {
					off+=2; // INIT (
					LinkedList<String> init = new LinkedList<String>();
					while ( code.getInteger( off ) != CMql.CLOSE ) {
						init.add( code.getString( off ) );
						off++;
						if ( code.getInteger( off ) == CMql.COMMA )
							off++;
					}
					off++; // )
					a.functionInit = init.toArray( new String[ init.size() ] );
				}
			}
			if ( code.getInteger(off ) == CMql.AS ) {
				off++;
				a.alias = code.getString(off);
				off++;
			}
			desc.addAttr( a );
			if ( code.getInteger(off) == CMql.COMMA )
				off++;
			else
				return off;
			
		}
		return off;
	}
	
	/**
	 * Create the where clause from the query code. Stops if the code ends or 
	 * if a close overhanging bracked ")" ends the where clause.
	 * 
	 * @param driver
	 * @param off
	 * @param code
	 * @param desc
	 * @param types
	 * @param sb
	 * @param user
	 * @param aclm
	 * @return
	 * @throws MorseException
	 */
	public static int createWhereClause( IConnectionServer con, SqlDriver driver, int off, ICompiledQuery code, Descriptor desc, ITypes types, StringBuffer sb, UserInformation user, IAclManager aclm ) throws MorseException {
		
		WhereSqlListener sql = new WhereSqlListener( driver, con, types, aclm, user, desc, code, sb );
		return new WhereParser().parse( con,  aclm, user, code, off, sql );
		
	}
	
	/*
	public static int createWhereClauseOld( IConnectionServer con, SqlAbstractDriver driver, int off, ICompiledQuery code, Descriptor desc, ITypes types, StringBuffer sb, UserInformation user, IAclManager aclm ) throws MorseException {
		
		int brackedCount = 0;
		
		for ( int i = off; i < code.size(); i++ ) {
			int c = code.getInteger( i );
			switch ( c ) {
			case CMql.OPEN:
				sb.append( '(' );
				brackedCount++;
				break;
			case CMql.CLOSE:
				sb.append( ')' );
				brackedCount--;
				if ( brackedCount < 0 )
					return i; // maybe its a subclause, so this is not my bracked
				break;
			case CMql.AND:
				sb.append( " AND " );
				break;
			case CMql.OR:
				sb.append( " OR " );
				break;
			case CMql.NOT:
				sb.append( " NOT " );
				break;
			case CMql.NaN:
				
				String aName = code.getString( i );
				i++;
				int c2 = code.getInteger( i );
				if ( c2 == CMql.EQ && code.getInteger( i + 1 ) == CMql.OPEN ) {
					// table
					aName = aName.toLowerCase();
					if ( ! AttributeUtil.isAttrName( aName, true ) )
						throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, aName );
					Object[] obj = desc.attrMap.get( aName ); 
					if ( obj == null )
						throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, aName );
					if ( obj.length == 0 )
						throw new MorseException( MorseException.ATTR_AMBIGIOUS, aName );
					String tName = (String)obj[3];
					int pos = tName.indexOf('.');
					if ( pos < 0 )
						tName = IAttribute.M_ID;
					else
						tName = tName.substring( 0, pos + 1 ) + IAttribute.M_ID;
					
					sb.append( driver.getColumnName( tName ) );
					sb.append( " IN ( SELECT " );
					sb.append( driver.getColumnName( IAttribute.M_ID ) );
					sb.append( " FROM r_" );
					sb.append( ((IAttribute)obj[1]).getSourceType().getName() ).append( '_' ).append( ((IAttribute)obj[1]).getName() );
					sb.append( " WHERE " );
					
					Descriptor desc2 = new Descriptor();
					Attr a = new Attr();
					a.name = IAttribute.M_ID;
					desc2.addAttr( a );
					// find all tables / types
					Table newTable = new Table();
					newTable.name = ((IAttribute)obj[1]).getSourceType().getName() + '.' + ((IAttribute)obj[1]).getName();
					desc2.addTable( newTable );
					checkTables( desc2, types, con, user, aclm );
					checkAttributes( con, desc2, user, aclm );
					i+=2;
					i = createWhereClause( con, driver, i, code, desc2, types, sb, user, aclm );
				
					// sb.append( ')' );
					i++;
				} else {
					sb.append( ' ' ).append( checkAttribute( driver, null, aName, desc, user ) ).append( ' ' );
					// attribute
					boolean printValue = true;
					switch ( c2 ) {
					case CMql.EQ:
						sb.append( '=' );
						break;
					case CMql.NOT:
						sb.append( "!=" );
						break;
					case CMql.GT:
						sb.append( '>' );
						break;
					case CMql.GTEQ:
						sb.append( ">=" );
						break;
					case CMql.LT:
						sb.append( '<' );
						break;
					case CMql.LTEQ:
						sb.append( "<=" );
						break;
					case CMql.LIKE:
						sb.append( "LIKE" );
						break;
					case CMql.IN:
						// TODO check if IN SELECT is allowed
						i++; // IN
						if ( code.getInteger( i ) == CMql.OPEN ) {
							i++; // (
							if ( code.getInteger( i ) == CMql.SELECT ) {
								i++; // SELECT
								// if distinct
								boolean distinct = false;
								if ( code.getInteger( i ) == CMql.DISTINCT ) {
									distinct = true;
									i++;
								}
								Descriptor desc2 = new Descriptor();
								i = SqlUtils.findAttributes(i, code, desc2);
								
								if ( desc.attrSize == 0 )
									throw new MorseException( MorseException.NO_ATTRIBUTES );
								
								i++; // FROM
								
								// find all tables / types
								i = SqlUtils.findTables(i, code, desc2 );
								checkTables( desc2, types, con, user, aclm );
								SqlUtils.checkAttributes( con, desc2, user, aclm );
								SqlUtils.postCheckAttributes( desc2 );
								SqlUtils.checkFunctions( con, desc2, desc2, user, driver.getAclManager() );
								
								StringBuffer sb2 = new StringBuffer();
								SqlUtils.createSelect( driver, desc2, sb2, distinct );
								boolean hasWhere = false;
								
								if ( SqlUtils.needHintWhere( driver, desc2 ) ) {
									
									if ( ! hasWhere ) {
										sb2.append( " WHERE (" );
									} else {
										sb2.append( " AND (" );
									}
									
									SqlUtils.createHintWhereClause( con, driver, desc2, driver.getTypes(), sb2, user, aclm );
									sb2.append( " ) " );
									hasWhere = true;
									
								}

								if ( code.getInteger( i ) == CMql.WHERE ) {
									if ( ! hasWhere ) {
										sb2.append( " WHERE (" );
									} else {
										sb2.append( " AND (" );
									}
									i++;
									i = createWhereClause( con, driver, i, code, desc2, types, sb2, user, aclm );
								}
								sb.append( " IN ( " ).append( sb2.toString() ).append( " ) ");
								i++; // )
								printValue = false;
							} else {
								sb.append( " IN ( " );
								while ( true ) {
									sb.append( checkAttribute(driver, aName, code.getString( i ), desc, user ) );
									i++;
									if ( code.getInteger( i ) == CMql.COMMA ) {
										sb.append( " , " );
										i++;
									} else
										break;
								}
								sb.append( " ) " );
								i++; // )
								printValue = false;
							} 
						} else {
							String aValue = code.getString( i );
							// is function
							i++; // (
							i++;
							LinkedList<String> functionAttrs = new LinkedList<String>();
							while ( code.getInteger( i ) != CMql.CLOSE ) {
								functionAttrs.add( code.getString( i ) );
								i++;
								if ( code.getInteger( i ) == CMql.COMMA )
									i++;
							}
							i++; // )
							LinkedList<String> functionInit = new LinkedList<String>();
							if ( i < code.size() && code.getInteger( i ) == CMql.INIT ) {
								i++; // INIT
								i++; // (
								while ( code.getInteger( i ) != CMql.CLOSE ) {
									functionInit.add( code.getString( i ) );
									i++;
									if ( code.getInteger( i ) == CMql.COMMA )
										i++;
								}
							}
							i++; // )
							IQueryFunction function = (IQueryFunction)con.getServer().loadFunction( con, "query." + aValue.toLowerCase() );
							function.initFunction( con, aclm, user, (String[])functionInit.toArray( new String[ functionInit.size() ] ) );
							Object[] obj = desc.attrMap.get( aName.toLowerCase() );
							String tmpName = "x_" + driver.getNextTmpId();
							String drop = driver.getDropTmpTableSql( tmpName );
							Sth sth = driver.internatConnection.getPool().aquireStatement();
							if ( drop != null ) {
								try {
									sth.executeUpdate( drop );
								} catch ( SQLException sqle ) {
								}
							}
							String create = new StringBuffer()
								.append( driver.getCreateTmpTablePrefixSql() )
								.append( ' ' )
								.append( tmpName )
								.append( " ( v " )
								.append( driver.getColumnDefinition( (IAttribute)obj[1], false ) )
								.append( ") ")
								.append( driver.getCreateTmpTableSuffixSql() )
								.toString();
							 
							try {
								sth.executeUpdate( create );
								sth.executeUpdate( driver.getCreateTmpIndexSql( 1, tmpName, "v" ) );
								
								Iterator<String> res = function.getRepeatingResult( (String[])functionAttrs.toArray( new String[ functionAttrs.size() ] ) );
								while ( res.hasNext() ) {
									String insert = "INSERT INTO " + tmpName + "(v) VALUES (" + getValueRepresentation(driver, (IAttribute)obj[1], res.next() ) + ")";
									sth.executeUpdate( insert );
								}

							} catch ( SQLException sqle ) {
								throw new MorseException( MorseException.ERROR, sqle );
							} finally {
								sth.release();
							}
							desc.addTmpTable( tmpName );
							sb.append( " IN ( SELECT v FROM " ).append( tmpName ).append( " ) ");
							printValue = false;
						}
						break;
					default:
						throw new MorseException( MorseException.UNKNOWN_SYMBOL, new String[] { String.valueOf( c2 ), code.getString( i ), sb.toString() } );
					}
					
					i++;
					String aValue = code.getString( i );
					if ( printValue ) {
						
						if ( i < code.size() && code.getInteger( i+1 ) == CMql.OPEN ) {
							// is function
							i++; // (
							i++;
							LinkedList<String> functionAttrs = new LinkedList<String>();
							while ( code.getInteger( i ) != CMql.CLOSE ) {
								functionAttrs.add( code.getString( i ) );
								i++;
								if ( code.getInteger( i ) == CMql.COMMA )
									i++;
							}
							i++; // )
							LinkedList<String> functionInit = new LinkedList<String>();
							if ( i < code.size() && code.getInteger( i ) == CMql.INIT ) {
								i++; // INIT
								i++; // (
								while ( code.getInteger( i ) != CMql.CLOSE ) {
									functionInit.add( code.getString( i ) );
									i++;
									if ( code.getInteger( i ) == CMql.COMMA )
										i++;
								}
							}
							i++; // )
							IQueryFunction function = (IQueryFunction)con.getServer().loadFunction( con, "query." + aValue.toLowerCase() );
							function.initFunction( con, aclm, user, (String[])functionInit.toArray( new String[ functionInit.size() ] ) );
							
							Object[] obj = desc.attrMap.get( aName.toLowerCase() );
							if ( function instanceof IQuerySqlFunction ) {
								String[] attrs = (String[])functionAttrs.toArray( new String[ functionAttrs.size() ] );
								for ( int j = 0; j < attrs.length; j++ ) {
									attrs[j] = checkAttribute( driver, null, attrs[j], desc, user );
								}
									
								sb.append( ((IQuerySqlFunction)function).appendSqlCommand( driver, attrs  ) );
							} else {
								String res = function.getSingleResult( (String[])functionAttrs.toArray( new String[ functionAttrs.size() ] ) );
								sb.append( ' ' ).append( getValueRepresentation(driver, (IAttribute)obj[1], res ) ).append( ' ' );
							}							
						} else {
							sb.append( ' ' ).append( checkAttribute( driver, aName, aValue, desc, user ) ).append( ' ' );
						}
					}
				}				
				break;
			default:
				// throw new MorseException( "Unknow symbol " + c + " (" + code.getString( i ) + ')' );
				return i;
			}
		}
		return code.size();
	}
	*/
	
	/** 
	 * found a limit and / or offset on the end of the clause
	 * 
	 * @param driver
	 * @param off
	 * @param code
	 * @param desc
	 * @param types
	 * @param sb
	 * @param user
	 * @param aclm
	 * @return
	 */
	public static int createLimitClause(SqlDriver driver, int off, ICompiledQuery code, Descriptor desc, ITypes types, StringBuffer sb, UserInformation user, IAclManager aclm) {
		
		if ( code.getInteger( off ) == CMql.OFFSET ) {
			desc.offset = code.getString( off+1 );
			off+=2;
		}
		if ( code.getInteger( off ) == CMql.LIMIT ) {
			desc.limit = code.getString( off+1 );
			off+=2;
		}
		
		return off;
	}

	/**
	 * found a order by at the end of the clause
	 * 
	 * @param driver
	 * @param off
	 * @param code
	 * @param desc
	 * @param types
	 * @param sb
	 * @param user
	 * @param aclm
	 * @return
	 * @throws MorseException
	 */
	public static int createOrderClause(SqlDriver driver, int off, ICompiledQuery code, Descriptor desc, ITypes types, StringBuffer sb, UserInformation user, IAclManager aclm) throws MorseException {
		off+=2; // ORDER BY
		sb.append( " ORDER BY" );
		boolean asc = true;
		for ( int i = off; i < code.size(); i++ ) {
			switch( code.getInteger( i ) ) {
			case CMql.NaN:
				String attrName = code.getString( i );
				Object[] obj = desc.attrMap.get( attrName );
				if ( obj == null )
					throw new MorseException( MorseException.ATTR_NOT_FOUND, attrName );
				if ( obj.length == 0 )
					throw new MorseException( MorseException.ATTR_AMBIGIOUS, attrName );
				sb.append( ' ' );
				sb.append( driver.getColumnName( attrName ) ); // TODO Wrong ??
				asc = true;
				if ( i+1 < code.size() && code.getInteger( i+1 ) == CMql.DESC )
					asc = false;
				if ( ! asc )
					sb.append( " DESC" );
				break;
			case CMql.COMMA:
				sb.append( ',' );
				asc = true;
				break;
			case CMql.ASC:
			case CMql.DESC:
				break;
			default:
				return i;
			}
		}
		return code.size();
	}

	/*
	public static String checkValue(String in, Descriptor desc, UserInformation user) throws MorseException {
		if ( AttributeUtil.isValue( in ) ) return in;
		in = in.toLowerCase();
		if ( ! AttributeUtil.isAttrName( in, true ) )
			throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, in );
		Object[] obj = desc.attrMap.get( in ); 
		if ( obj == null )
			throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, in );
		if ( obj.length == 0 )
			throw new MorseException( MorseException.ATTR_AMBIGIOUS, in );

		return (String)obj[3];
	}
	 */
	
	public static void createHintWhereClause( IConnection con, SqlDriver driver, Descriptor desc, ITypes types, StringBuffer sb, UserInformation user, IAclManager aclm ) throws MorseException {
		for ( int i = 0; i < desc.tableSize; i++ )
			if ( desc.tables[ i ].hintObject != null && desc.tables[i].hintObject.needSqlHint( desc.tables[i].hintSize, desc.tables[i].hints, desc.tables[ i ], driver ) ) {
				String hint = desc.tables[ i ].hintObject.getSqlHint( desc.tables[i].hintSize, desc.tables[i].hints, desc.tables[ i ], driver );
				if ( hint != null ) {
					if ( i != 0 )
						sb.append( " AND " );
					sb.append(  hint );
				}
			}
		
	}
	
	public static boolean needHintWhere(SqlDriver driver, Descriptor desc  ) {
		for ( int i = 0; i < desc.tableSize; i++ )
			if ( desc.tables[ i ].hintObject != null && desc.tables[i].hintObject.needSqlHint( desc.tables[i].hintSize, desc.tables[i].hints, desc.tables[i], driver ) )
				return true;
		return false;
	}
	
	
	/**
	 * Validate the attribute or value.
	 * 
	 */
	
	public static String checkAttribute( SqlDriver driver, String attrName, String in, Descriptor desc, UserInformation user) throws MorseException {
		
		if ( AttributeUtil.isValue( in ) ) {
			if ( attrName != null ) {
				Object[] obj = desc.attrMap.get( attrName.toLowerCase() );
				// dont need check ...
				IAttribute attr = (IAttribute)obj[1];
				String value = in;
				if ( in.length() > 1 && in.charAt( 0 ) == '\'' && in.charAt( in.length() - 1 ) == '\'' )
					value = ASql.unescape( in.substring( 1, in.length() - 1 ) );
				if ( ! attr.getAco().validate( value ) )
					throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] { attrName, in } );
				return ObjectUtil.toString( getValueRepresentation( driver, attr, value ) );
			}
			return in;
		}
		in = in.toLowerCase();
		if ( ! AttributeUtil.isAttrName( in, true ) )
			throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, in );
		Object[] obj = desc.attrMap.get( in ); 
		if ( obj == null )
			throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, in );
		if ( obj.length == 0 )
			throw new MorseException( MorseException.ATTR_AMBIGIOUS, in );

		return driver.getColumnName( (String)obj[3] );
	}
	
	/**
	 * Format the attribute into a string whitch can be placed in a sql query.
	 * 
	 * @param driver
	 * @param attr
	 * @param value
	 * @return
	 * @throws MorseException
	 */
	public static Object getValueRepresentation( IChannelDriverServer driver, IAttribute attr, String value ) throws MorseException {
		try {
			switch ( attr.getType() ) {
			case IAttribute.AT_ACL:
			case IAttribute.AT_ID:
			case IAttribute.AT_STRING:
				return '\'' + ASql.escape( attr.getAco().getString( value ) ) + '\'';
			case IAttribute.AT_STRING_RAW:
				return attr.getAco().getString( value );
			case IAttribute.AT_BOOLEAN:
			case IAttribute.AT_INT:
				return attr.getAco().getInteger( value );
			case IAttribute.AT_LONG:
				return attr.getAco().getLong( value );
			case IAttribute.AT_DOUBLE:
				return attr.getAco().getDouble( value );
			case IAttribute.AT_TABLE:
				throw new MorseException( MorseException.ATTR_IS_A_TABLE );
			case IAttribute.AT_DATE:
				return '\'' + driver.toValidDate( attr.getAco().getDate( value ) )+ '\'';
			}
		} catch ( Exception e ) {
			throw new MorseException( MorseException.ERROR, new String[] { attr == null ? "null" : attr.getCanonicalName(), value }, e );
		}
		throw new MorseException( MorseException.UNKNOWN_TYPE,new String[] { attr == null ? "null" : attr.getCanonicalName(), value } );
	}

	/**
	 * Analyse the found attributes and collect all needed. This function
	 * resolve attributes like "*" or "**" and try to find for every attribute
	 * the corresponding Business Attribute and Type.
	 * 
	 * @param desc
	 * @param types
	 * @param user
	 * @param aclm
	 * @return
	 * @throws MorseException
	 */
	public static boolean collectAttributes(Descriptor desc, ITypes types, UserInformation user, IAclManager aclm) throws MorseException {
		
		if ( desc.attrSize != 1 || !desc.attrs[0].name.endsWith( "*" ) )
			return false;
		
		String single = desc.attrs[0].name;
		boolean tablesAlso = false;
		if ( single.endsWith( "**" ) ) {
			tablesAlso = true;
			if ( single.length() == 2 ) {
				single = null;
			} else {
				single = single.substring( 0, single.length() - 2 );
			}
		} else {
			if ( single.length() == 1 ) {
				single = null;
			} else {
				single = single.substring( 0, single.length() - 1 );
			}
		}
		
		desc.attrSize = 0;

		for ( Iterator<Map.Entry<String, Object[]>> i = desc.attrMap.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<String, Object[]> entry = i.next();
			String name  = entry.getKey();
			Object[] obj = entry.getValue();
			if ( obj.length != 0 ) {
				if ( ( desc.tableSize == 1 && "d".equals( obj[2] ) ) || ( desc.tableSize != 1 && "f".equals( obj[2] ) ) ) {
					if ( ( tablesAlso || ! ((IAttribute)obj[1]).isTable() ) && ( single == null || single.equals( ((Table)obj[0]).name ) ) ) {
						Attr a  = new Attr();
						a.table = (Table)obj[0];
						a.attr  = (IAttribute)obj[1];
						a.name     = name;
						a.orgName  = name;
						a.attrName = a.attr.getName();
						if ( ((IAttribute)obj[1]).getName().equals( IAttribute.M_ID )  )
							((Table)obj[0]).internalId = a.alias;
						if ( ((IAttribute)obj[1]).getName().equals( IAttribute.M_ACL )  )
							((Table)obj[0]).internalAcl = a.alias;
						desc.addAttr( a );
					}
				}
			}
		}

		return true;
	}
	
	public static void checkFunctions( IConnectionServer con, Descriptor desc, Descriptor attrDescriptor, UserInformation user, IAclManager aclm ) throws MorseException {
		for ( int i = 0; i < desc.attrSize; i++ ) {
			Attr a = desc.attrs[ i ];
			if ( a.function != null ) {
				checkFunction( con, user, aclm, a, attrDescriptor );
				if ( desc.attrs[ i ].alias == null )
					desc.attrs[ i ].alias = desc.attrs[ i ].name;
			}
		}
	}
	
	public static void checkAttributes( IConnection con, Descriptor desc, UserInformation user, IAclManager aclm ) throws MorseException {
		for ( int i = 0; i < desc.attrSize; i++ ) {
			Attr a = desc.attrs[ i ];
			if ( a.function != null ) {
				// noting ... done by checkFunctions()
			} else
			if ( AttributeUtil.isValue( a.name ) ) {
				// nothing ....
			} else
			if ( AttributeUtil.isAttrName( a.name, true ) ) {
			
				Object[] obj = desc.attrMap.get( a.name );
				
				if ( obj == null )
					throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, a.name );
				if ( obj.length == 0 )
					throw new MorseException( MorseException.ATTR_AMBIGIOUS, a.name );
	
				a.orgName  = a.name;
				a.name = (String)obj[3];
				a.attr = (IAttribute)obj[1];
				a.table = (Table)obj[0];
				a.attrName = a.attr.getName();
				
			}
			else
				throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, a.name );
			
		}
	}
	
	public static void checkFunction( IConnectionServer con, UserInformation user, IAclManager aclm, Attr a, Descriptor desc ) throws MorseException {
		
		if ( a.function == null )
			throw new MorseException( MorseException.FUNCTION_NOT_FOUND, a.name );
		
		a.functionObject = (IQueryFunction)con.getServer().loadFunction( con, "query." + a.name.toLowerCase() );
		a.functionObject.initFunction( con, aclm, user, a.functionInit );
		a.attr = a.functionObject.getType();
		
		Class[] classes = new Class[ a.function.size() ];
		int j = 0;
		for ( Iterator<String> x = a.function.iterator(); x.hasNext(); ) {
			String key = x.next();
			if ( key == null || key.length() == 0 || "*".equals( key )  )
				classes[ j ] = String.class;
			else
			if ( key.startsWith( "'" ) && key.endsWith( "'" ) )
				classes[ j ] = String.class;
			else {
				int sel = -1;
				for ( int i = 0; i < desc.attrSize; i++ )
					if ( key.equals( desc.attrs[ i ].alias ) ) {
						sel = i;
						break;
					}
				if ( sel < 0 )
					throw new MorseException( MorseException.ATTR_NOT_FOUND, key );
				switch ( desc.attrs[ sel ].attr.getType() ) {
				case IAttribute.AT_ACL:
				case IAttribute.AT_ID:
				case IAttribute.AT_STRING:
					classes[ j ] = String.class;
					break;
				case IAttribute.AT_STRING_RAW:
					classes[ j ] = Object.class;
					break;
				case IAttribute.AT_INT:
					classes[ j ] = int.class;
					break;
				case IAttribute.AT_LONG:
					classes[ j ] = long.class;
					break;
				case IAttribute.AT_BOOLEAN:
					classes[ j ] = boolean.class;
					break;
				case IAttribute.AT_DATE:
					classes[ j ] = Date.class;
					break;
				case IAttribute.AT_DOUBLE:
					classes[ j ] = double.class;
					break;
				case IAttribute.AT_TABLE:
					classes[ j ] = ITableRead.class;
					break;
				}
			}
			
			try {
				a.functionMethod = a.functionObject.getClass().getMethod( "append", classes );
			} catch (Exception e) {
				log.debug( e );
				throw new MorseException( MorseException.FUNCTION_NO_METHOD, a.name, e );
			}
			
		}
		
	}
	
	public static void executeFunction( Attr a, IObjectRead result ) throws MorseException {
		
		Class[] types = a.functionMethod.getParameterTypes();
		Object[] values = new Object[ types.length ];
		Iterator<String> keyIter = a.function.iterator();
		for ( int i = 0; i < types.length; i++ ) {
			if ( types[ i ] == String.class ) {
				String key = keyIter.next();
				if ( key == null || key.length() == 0 || "*".equals( key )  )
					values[ i ] = null;
				else
				if ( key.startsWith( "'" ) && key.endsWith( "'" ) )
					values[ i ] = key.substring( 1, key.length() - 1 );
				else
					values[ i ] = result.getString( key );
			} else
			if ( types[ i ] == Object.class )
				values[ i ] = result.getString( keyIter.next() ); // TODO getObject (?!)
			else
			if ( types[ i ] == int.class )
				values[ i ] = result.getInteger( keyIter.next() );
			else
			if ( types[ i ] == long.class )
				values[ i ] = result.getLong( keyIter.next() );
			else
			if ( types[ i ] == boolean.class )
				values[ i ] = result.getBoolean( keyIter.next() );
			else
			if ( types[ i ] == Date.class )
				values[ i ] = result.getDate( keyIter.next() );
			else
			if ( types[ i ] == double.class )
				values[ i ] = result.getDouble( keyIter.next() );
			else
			if ( types[ i ] == ITableRead.class )
				values[ i ] = result.getTable( keyIter.next() );
			else
				throw new MorseException( MorseException.FUNCTION_NO_METHOD, a.name );
		}
		// invoke
		try {
			a.functionMethod.invoke( a.functionObject, values );
		} catch (Exception e) {
			log.debug( e );
			throw new MorseException( MorseException.FUNCTION_NO_METHOD, a.name, e );
		}
	}
	
	public static void postCheckAttributes( Descriptor desc ) {
		for ( int i = 0; i < desc.attrSize; i++ ) {
			Attr a = desc.attrs[ i ];
			if ( a.alias == null )
				a.alias = a.orgName;
		}
	}
	
	/**
	 * Create a sql select statement.
	 * 
	 * @param driver
	 * @param desc
	 * @param sb
	 * @param distinct
	 */
	public static void createSelect( SqlDriver driver, Descriptor desc, StringBuffer sb, boolean distinct ) {
		
		sb.append( "SELECT " );
		if ( distinct ) sb.append( "DISTINCT " );
		
		boolean isFirst = true;
		for ( int i = 0; i < desc.attrSize; i++ ) {
			Attr a = desc.attrs[ i ];
			if ( ! a.attr.isTable() && a.function == null ) {
				if ( ! isFirst ) sb.append( ',' );
				if ( a.alias == null ) {
					if ( a.name.startsWith( a.table.alias + '.' ) ) {
						a.alias = a.name.replace( '.', '_' );
						sb.append( driver.getColumnName( a.name ) ).append( ' ' ).append( a.alias );
					} else
					if ( a.name.startsWith( a.table.mqlAlias + '.' ) ) {
						a.alias = a.table.alias + '_' + a.attrName;
						sb.append( a.table.alias + '.' + driver.getColumnName( a.attrName ) ).append( ' ' ).append( a.alias );
					} else {
						a.alias = a.name;
						sb.append( a.table.alias ).append( '.' ).append( driver.getColumnName( a.name ) ).append( ' ' ).append( a.name );
					}
				} else {
					if ( a.name.indexOf( '.' ) < 0 )
						sb.append( a.table.alias + '.' + driver.getColumnName( a.name ) ).append( ' ' ).append( a.alias );
					else
						sb.append( driver.getColumnName( a.name ) ).append( ' ' ).append( a.alias.replace( '.', '_' ) );
				}
				isFirst = false;
			} else {
				if ( a.alias == null )
					a.alias = a.name;
			}
		}
		
		for ( int i = 0; i < desc.attrSizeInt; i++ ) {
			Attr a = desc.attrsInt[ i ];
			if ( ! isFirst ) sb.append( ',' );
			String name = a.name;
			if ( ! name.startsWith( a.table.alias + '.' ) )
				name = a.table.alias + '.' + a.name;
			sb.append( driver.getColumnName( name ) ).append( ' ' ).append( a.alias );
			isFirst = false;
		}
		
		sb.append( " FROM " );
		isFirst = true;
				
		for ( int i = 0; i < desc.tableSize; i++ ) {
			Table t = desc.tables[ i ];
			if ( ! isFirst ) sb.append( ',' );
			sb.append( t.tableName ).append( driver.getTableAs() ).append( t.alias );
			isFirst = false;
		}
		
		for ( int i = 0; i < desc.tableSizeInt; i++ ) {
			Table t = desc.tablesInt[ i ];
			if ( ! isFirst ) sb.append( ',' );
			sb.append( t.tableName ).append( driver.getTableAs() ).append( t.alias );
			isFirst = false;
		}
		
	}

	/**
	 * Collect internal needed attributes.
	 * 
	 * @param desc
	 * @param user
	 * @throws MorseException
	 */
	public static void findInternalAttributes(Descriptor desc, UserInformation user) throws MorseException {
		
		// TODO its overhead if a attr is already in a table, insert special "id" in attr check
		// create this only if internalId is null
		
		for ( int i = 0; i < desc.tableSize; i++ ) {
			Table t = desc.tables[ i ];
			if ( t.isShort ) {
				// its a short table ... need access informations
				Table tn = new Table();
				tn.alias = "t" + i + "__";
				tn.name  = IType.TYPE_OBJECT;
				tn.tableName = "t_" + IType.TYPE_OBJECT;
				// t.type ???
				tn.depTable = t;
				desc.addInternalTable( tn );
				
				if ( user != null ) {
					Attr a = new Attr();
					a.table = tn;
					// a.attr  = ax; ???
					a.name  = tn.alias + '.' + IAttribute.M_ACL;
					a.attrName = IAttribute.M_ACL;
					a.alias = "ia" + i + "__";
					desc.addInternalAttr( a );
					t.internalAcl = a.alias;
				}
				
				Attr a = new Attr();
				a.table = tn;
				// a.attr  = ax; ???
				a.name  = tn.alias + '.' + IAttribute.M_ID;
				a.attrName = IAttribute.M_ID;
				a.alias = "ii" + i + "__";
				desc.addInternalAttr( a );
				t.internalId = a.alias;
				
			} else
			if ( t.attr != null ) {
				// its a sub table ... need access
				Table tn = new Table();
				tn.alias = "t" + i + "__";
				tn.name  = IType.TYPE_OBJECT;
				tn.tableName = "t_" + IType.TYPE_OBJECT;
				// t.type ???
				tn.depTable = t;
				desc.addInternalTable( tn );
				
				if ( user != null ) {
					Attr a = new Attr();
					a.table = tn;
					// a.attr  = ax; ???
					a.name  = tn.alias + '.' + IAttribute.M_ACL;
					a.attrName = IAttribute.M_ACL;
					a.alias = "ia" + i + "__";
					desc.addInternalAttr( a );
					t.internalAcl = a.alias;
				}
				
				Attr a = new Attr();
				a.table = tn;
				// a.attr  = ax; ???
				a.name  = tn.alias + '.' + IAttribute.M_ID;
				a.attrName = IAttribute.M_ID;
				a.alias = "ii" + i + "__";
				desc.addInternalAttr( a );
				t.internalId = a.alias;
				
			} else {
				if ( t.internalAcl == null && user != null ) {
					
					Attr a = new Attr();
					a.table = t;
					// a.attr  = ax; ???
					a.name  = t.alias + '.' + IAttribute.M_ACL;
					a.attrName = IAttribute.M_ACL;
					a.alias = "ia" + i + "__";
					desc.addInternalAttr( a );
					t.internalAcl = a.alias;
				}
				if ( t.internalId == null ) {
					Attr a = new Attr();
					a.table = t;
					// a.attr  = ax; ???
					a.name  = t.alias + '.' + IAttribute.M_ID;
					a.attrName = IAttribute.M_ID;
					a.alias = "ii" + i + "__";
					desc.addInternalAttr( a );
					t.internalId = a.alias;
				}
			}
			
		}
	}

	public static boolean needInternalWhere(Descriptor desc ) {
		return desc.tableSizeInt != 0;
	}
	
	public static void createInternalWhere( SqlDriver driver, Descriptor desc, StringBuffer sb) {
		for ( int i = 0; i < desc.tableSizeInt; i++ ) {
			Table t = desc.tablesInt[ i ];
			if ( i != 0 )
				sb.append( " AND " );
			sb.append( t.alias ).append( '.' ).append( driver.getColumnName( IAttribute.M_ID ) ). append( '=' ).append( t.depTable.alias ).append( '.' ).append( driver.getColumnName( IAttribute.M_ID ) );
		}
	}

	/**
	 * Convert values to ... hmmm.
	 * 
	 * @param driver
	 * @param attr
	 * @param value
	 * @return
	 * @throws MorseException
	 */
	public static String getValueRaw(IChannelDriverServer driver, IAttribute attr, String value) throws MorseException {
		try {
			switch ( attr.getType() ) {
			case IAttribute.AT_ACL:
			case IAttribute.AT_ID:
			case IAttribute.AT_STRING:
				if ( value.length() < 2 ) return value;
				if ( value.charAt( 0 ) == '\'' && value.charAt( value.length() - 1 ) == '\'' )
					return value.substring( 1, value.length() - 1 );
				return value;
			case IAttribute.AT_STRING_RAW:
			case IAttribute.AT_BOOLEAN:
			case IAttribute.AT_INT:
			case IAttribute.AT_LONG:
			case IAttribute.AT_DOUBLE:
			case IAttribute.AT_TABLE:
			case IAttribute.AT_DATE:
				return value;
			}
		} catch ( Exception e ) {
			throw new MorseException( MorseException.ERROR, new String[] { attr.getCanonicalName(), value }, e );
		}
		throw new MorseException( MorseException.UNKNOWN_TYPE,new String[] { attr.getCanonicalName(), value } );
	}

	/**
	 * Updates all chaned attributes from a BTC object into the database.
	 *  
	 * @param channel
	 * @param sthUpdate
	 * @param btc
	 * @param isEvents 
	 * @param isCommit 
	 * @throws MorseException
	 */
	public static void updateBtc(SqlChannel channel, Sth sthUpdate, Btc btc, boolean isCommit, boolean isEvents) throws MorseException {
		
		if ( ! btc.isDirty() ) return;
		SqlDriver driver = channel.getDriver();
		ITransaction tr = channel.getConnection().startTransaction();
		LinkedList<String> updates = new LinkedList<String>();
		try {
			for ( int i = 0; i < btc.getAttributeCount(); i++ ) {
				if ( btc.isDirty( i ) ) {
					IAttribute attr = btc.getAttribute( i );
					updates.add( attr.getName() );
					if ( attr.isTable() ) {
						String tableName = "r_" + attr.getSourceType().getName() + '_' + attr.getName();
						String sql = "DELETE FROM " + tableName
						+ " WHERE " + driver.getColumnName( IAttribute.M_ID ) + "='" + btc.getObjectId() + '\'';
						if ( log.t4() ) log.info( "UPDATE SQL: " + sql );
						sthUpdate.executeUpdate( sql );
						
						ITable t = btc.getTable( i );
						t.reset();
						int col = 0;
						while ( t.next() ) {
							StringBuffer sb = new StringBuffer();
							sb.append( "INSERT INTO " ).append( tableName ).append( " (" )
							.append( driver.getColumnName( IAttribute.M_ID ) ).append( ',' )
							.append( driver.getColumnName( IAttribute.M_POS ) );
							for ( Iterator<IAttribute> j = attr.getAttributes(); j.hasNext(); ) {
								sb.append( ',' ).append( driver.getColumnName( j.next().getName() ) );
							}
							sb.append( ") VALUES ('" ).append( btc.getObjectId() ).append( "'," )
							.append( col );
							int cnt = 0;
							for ( Iterator<IAttribute> j = attr.getAttributes(); j.hasNext(); ) {
								sb.append( ',' ).append( SqlUtils.getValueRepresentation( driver, j.next(), t.getString( cnt ) ) );
								cnt++;
							}
							sb.append( ')' );
							sql = sb.toString();
							if ( log.t4() ) log.info( "UPDATE SQL: " + sql );
							sthUpdate.executeUpdate( sql );
							col++;
						}
						
					} else {
						String sql = "UPDATE t_" + attr.getSourceType().getName()
						+ " SET " + driver.getColumnName( attr.getName() ) + '='
						+ SqlUtils.getValueRepresentation( driver, attr, btc.getString( i ) )
						+ " WHERE " + driver.getColumnName( IAttribute.M_ID ) 
						+ "='" + btc.getObjectId() + '\'';
						if ( log.t4() ) log.info( "UPDATE SQL: " + sql );
						sthUpdate.executeUpdate( sql );
					}
				}
			}
			if ( isEvents ) channel.getConnection().eventObjectUpdated(channel.getName(), btc.getObjectId(), btc.getType().getName(), updates.toArray( new String[ updates.size() ] ) );
			if ( isCommit ) {
				channel.getConnection().maybeCommit( tr );
				tr = null;
			}
			
		} catch ( SQLException sqle ) {
			log.error( sqle );
			if ( isCommit ) {
				channel.getConnection().maybeRollback( tr );
				tr = null;
			}
			throw new MorseException( MorseException.ERROR, sqle );
		} finally {
			if ( tr != null ) channel.getConnection().stopTransaction( tr );
		}
		
	}
	
	/**
	 * Delete a object in the database.
	 * 
	 * @param channel
	 * @param sthDelete
	 * @param btc
	 * @param isEvent 
	 * @param isCommit 
	 * @throws MorseException
	 */
	public static void delete(SqlChannel channel, Sth sthDelete, Btc btc, boolean isCommit, boolean isEvent) throws MorseException {
		
		ITransaction tr = channel.getConnection().startTransaction();
		try {
			IType type = btc.getType();
			for ( Iterator<IAttribute> i = type.getAttributes(); i.hasNext(); ) {
				IAttribute attr = i.next();
				if ( attr.isTable() ) {
					String sql = "DELETE FROM r_" + attr.getSourceType().getName() + '_' + attr.getName() + " WHERE " + channel.getDriver().getColumnName( IAttribute.M_ID ) + "='" + btc.getObjectId() + '\'';
					if ( log.t4() ) log.info( "DETETE SQL: " + sql );
					sthDelete.executeUpdate( sql );
				}
			}
			do {
				String sql = "DELETE FROM t_" + type.getName() + " WHERE " + channel.getDriver().getColumnName( IAttribute.M_ID ) + "='" + btc.getObjectId() + '\'';
				if ( log.t4() ) log.info( "DETETE SQL: " + sql );
				sthDelete.executeUpdate( sql );
				type =type.getSuperType();
			} while ( type != null );
			if ( isEvent ) channel.getConnection().eventObjectDeleted( channel.getName(), btc.getObjectId(), btc.getType().getName() );
			if ( isCommit ) {
				channel.getConnection().maybeCommit( tr );
				tr = null;
			}
		} catch ( SQLException sqle ) {
			log.error( sqle );
			if ( isCommit ) {
				channel.getConnection().maybeRollback( tr );
				tr = null;
			}
			throw new MorseException( MorseException.ERROR, sqle );
		} finally {
			if ( tr != null ) channel.getConnection().stopTransaction( tr );
		}
			
	}
	
}
