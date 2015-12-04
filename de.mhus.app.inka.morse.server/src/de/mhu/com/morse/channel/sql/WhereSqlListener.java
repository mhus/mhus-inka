package de.mhu.com.morse.channel.sql;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.ASql;
import de.mhu.lib.dtb.Sth;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.channel.IChannelDriverServer;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IQueryFunction;
import de.mhu.com.morse.channel.IQueryWhereFunction;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.com.morse.utils.MorseException;

public class WhereSqlListener implements WhereParser.IWhereListener {

	private StringBuffer sb = null;
	private Descriptor desc;
	private SqlDriver driver;
	private IConnectionServer con;
	private ITypes types;
	private IAclManager aclm;
	private UserInformation user;
	private ICompiledQuery code;
	private boolean needComma;
	
	public WhereSqlListener( SqlDriver pDriver, IConnectionServer pCon, ITypes pTypes, IAclManager pAclm, UserInformation pUser, Descriptor pDesc, ICompiledQuery pCode, StringBuffer dest ) {
		desc = pDesc;
		driver = pDriver;
		con = pCon;
		types = pTypes;
		aclm = pAclm;
		user = pUser;
		code = pCode;
		sb = dest;
	}
	
	public int appendTableSelect(String name, int off) throws MorseException {
		
		name = name.toLowerCase();
		if ( ! AttributeUtil.isAttrName( name, true ) )
			throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, name );
		Object[] obj = desc.attrMap.get( name ); 
		if ( obj == null )
			throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, name );
		if ( obj.length == 0 )
			throw new MorseException( MorseException.ATTR_AMBIGIOUS, name );
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
		SqlUtils.checkTables( desc2, types, con, user, aclm );
		SqlUtils.checkAttributes( con, desc2, user, aclm );
		off+=2;
		off = SqlUtils.createWhereClause( con, driver, off, code, desc2, types, sb, user, aclm );
	
		// sb.append( ')' );
		off++;
		return off;
	}

	public void brackedClose() {
		sb.append( ')' );
	}

	public void brackedOpen() {
		sb.append( '(' );
	}

	public void compareEQ(String left, String right) {
		sb.append( left ).append( '=' ).append( right );
	}

	public void compareGT(String left, String right) {
		sb.append( left ).append( '>' ).append( right );
	}

	public void compareGTEQ(String left, String right) {
		sb.append( left ).append( ">=" ).append( right );
	}

	public void compareINBegin(String left) {
		sb.append( left ).append( " IN (" );
		needComma = false;
	}

	public void compareINEnd() {
		sb.append( ')' );		
	}

	public void compareINValue(String string) {
		if ( needComma )
			sb.append( ',' );
		needComma = true;
		sb.append( string );
	}

	public void compareLIKE(String left, String right) {
		sb.append( left ).append( " LIKE " ).append( right );
	}

	public void compareLT(String left, String right) {
		sb.append( left ).append( '<' ).append( right );
	}

	public void compareLTEQ(String left, String right) {
		sb.append( left ).append( "<=" ).append( right );
	}

	public void compareNOTEQ(String left, String right) {
		sb.append( left ).append( "!=" ).append( right );
	}

	public int compareSubSelect(String name, int off, boolean distinct) throws MorseException {
		Descriptor desc2 = new Descriptor();
		off = SqlUtils.findAttributes(off, code, desc2);
		
		if ( desc.attrSize == 0 )
			throw new MorseException( MorseException.NO_ATTRIBUTES );
		
		off++; // FROM
		
		// find all tables / types
		off = SqlUtils.findTables(off, code, desc2 );
		SqlUtils.checkTables( desc2, types, con, user, aclm );
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

		if ( code.getInteger( off ) == CMql.WHERE ) {
			if ( ! hasWhere ) {
				sb2.append( " WHERE (" );
			} else {
				sb2.append( " AND (" );
			}
			off++;
			off = SqlUtils.createWhereClause( con, driver, off, code, desc2, types, sb2, user, aclm );
		}
		sb.append( name ).append( " IN ( " ).append( sb2.toString() ).append( " ) ");
		off++; // )
		return off;
	}

	public String executeFunction( IQueryFunction function, LinkedList<Object> functionAttrs ) throws MorseException {
		// Object[] obj = desc.attrMap.get( aName.toLowerCase() );
		if ( function instanceof IQuerySqlFunction ) {
			String[] attrs = (String[])functionAttrs.toArray( new String[ functionAttrs.size() ] );
			for ( int j = 0; j < attrs.length; j++ ) {
				attrs[j] = SqlUtils.checkAttribute( driver, null, attrs[j], desc, user );
			}
				
			return ((IQuerySqlFunction)function).appendSqlCommand( driver, attrs  );
		} else {
			Object[] values = new Object[ functionAttrs.size() ];
			Class[]  classes = new Class[ functionAttrs.size() ];
			int cnt = 0;
			for ( Iterator i = functionAttrs.iterator(); i.hasNext(); ) {
				values[cnt] = i.next();
				classes[cnt] = values[cnt].getClass();
				cnt++;
			}
			
			if ( function instanceof IQueryWhereFunction ) 
				return ((IQueryWhereFunction)function).getSingleResult( values );
			else {
				try {
					function.getClass().getMethod( "append", classes ).invoke( function, values );
				} catch (Exception e) {
					throw new MorseException( MorseException.ERROR, e );
				}
				return function.getResult();
			}
		}
	}

	public void appendInFunction( String left, IQueryFunction function, LinkedList<Object> functionAttrs) throws MorseException {
		Sth sth = null;
		String tmpName = null;
		try {
			Object[] obj = desc.attrMap.get( left.toLowerCase() );
			tmpName = "x_" + driver.getNextTmpId();
			String drop = driver.getDropTmpTableSql( tmpName );
			sth = driver.internatConnection.getPool().aquireStatement();
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

			sth.executeUpdate( create );
			sth.executeUpdate( driver.getCreateTmpIndexSql( 1, tmpName, "v" ) );
			
			if ( ! ( function instanceof IQueryWhereFunction ) )
				throw new MorseException( MorseException.FUNCTION_NOT_COMPATIBLE );
			
			Iterator<String> res = ((IQueryWhereFunction)function).getRepeatingResult( (Object[])functionAttrs.toArray( new Object[ functionAttrs.size() ] ) );
			while ( res.hasNext() ) {
				String insert = "INSERT INTO " + tmpName + "(v) VALUES (" + SqlUtils.getValueRepresentation(driver, (IAttribute)obj[1], res.next() ) + ")";
				sth.executeUpdate( insert );
			}

		} catch ( Exception sqle ) {
			if ( sqle instanceof MorseException ) throw (MorseException)sqle;
			throw new MorseException( MorseException.ERROR, sqle );
		} finally {
			try { sth.release(); } catch ( Exception ex ) {}
		}
		desc.addTmpTable( tmpName );
		sb.append( " IN ( SELECT v FROM " ).append( tmpName ).append( " ) ");
	}

	public void operatorAnd() {
		sb.append( " AND " );
	}

	public void operatorNot() {
		sb.append( " NOT " );
	}

	public void operatorOr() {
		sb.append( " OR " );
	}

	public String transformAttribute(String name) throws MorseException {
		Object[] obj = desc.attrMap.get( name ); 
		if ( obj == null )
			throw new MorseException( MorseException.UNKNOWN_ATTRIBUTE, name );
		if ( obj.length == 0 )
			throw new MorseException( MorseException.ATTR_AMBIGIOUS, name );
		String tName = (String)obj[3];
		/*
		int pos = tName.indexOf('.');
		if ( pos < 0 )
			tName = IAttribute.M_ID;
		else
			tName = tName.substring( 0, pos + 1 ) + IAttribute.M_ID;
		*/
		return driver.getColumnName( tName );
		
		// return SqlUtils.checkAttribute( driver, null, name, desc, user );
	}

	public Object transformValue( String attrName, String name) throws MorseException {
		if ( ! AttributeUtil.isValue( name ) )
			throw new MorseException( MorseException.WRONG_VALUE_FORMAT, name );
		
		if ( attrName != null ) {
			Object[] obj = desc.attrMap.get( attrName.toLowerCase() );
			if ( obj != null && obj.length != 0 && obj[1] != null ) {
				IAttribute attr = (IAttribute)obj[1];
				String value = name;
				if ( name.length() > 1 && name.charAt( 0 ) == '\'' && name.charAt( name.length() - 1 ) == '\'' )
					value = ASql.unescape( name.substring( 1, name.length() - 1 ) );
				if ( ! attr.getAco().validate( value ) )
					throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] { attrName, name } );
				return SqlUtils.getValueRepresentation( driver, attr, value );
			} else {
				IAttribute attr = IAttributeDefault.getAttribute( attrName );
				if ( attr != null )
					return SqlUtils.getValueRepresentation( driver, attr, name );
			}
		}
		return name;
	}

}
