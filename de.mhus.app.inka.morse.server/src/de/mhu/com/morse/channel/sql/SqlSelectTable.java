package de.mhu.com.morse.channel.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.ACast;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.dtb.Sth;
import de.mhu.lib.log.AL;
import de.mhu.lib.utils.ResourceException;
import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.config.QueryConfig;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.utils.MorseException;

public class SqlSelectTable extends AbstractSelectResult {

	private static AL log = new AL( SqlSelectTable.class );
	private static QueryConfig config = (QueryConfig)ConfigManager.getConfig( QueryConfig.NAME );
	private Sth sth;
	private ResultSet res;
	private Attr attr;
	private String[] colsNames;
	private Hashtable<String, Integer> colsIndex;
	private StackTraceElement[] startTrace;
	private SqlSelectResult result;

	public SqlSelectTable(SqlSelectResult pResult, int index ) throws SQLException, ResourceException {
		if ( config.isSthTrace() )
			startTrace = Thread.currentThread().getStackTrace();
		
		result = pResult;
		
		sth = result.con.getPool().aquireStatement();
		
		attr = result.desc.attrs[index];
		
		StringBuffer sb = new StringBuffer();
		sb.append( "SELECT " );
		String[] cols = getColumns();
		for ( int i = 0; i < cols.length; i++ ) {
			if ( i != 0 ) sb.append(',');
			sb.append( result.driver.getColumnName( cols[i] ) ).append( ' ' ).append( cols[i] );
		}
		sb.append( " FROM r_" ).append( attr.attr.getSourceType().getName() ).append( '_' )
		.append( attr.attrName ).append( " WHERE " ).append( result.driver.getColumnName( IAttribute.M_ID ) ).append( "='" )
		.append( result.getRawString( attr.table.internalId ) ).append( "' ORDER BY " ).append( result.driver.getColumnName( IAttribute.M_POS ) );
		
		String sql = sb.toString();
		// if ( log.t8() ) log.info( "SQL: " + sql );
		res = sth.executeQuery( sql );
		
	}

	public synchronized void close() {
		if ( sth == null ) return;
		
		result.intReleaseResource( this );
		
		try {
			res.close();
		} catch ( Exception e ) {
			if ( log.t3() ) log.error( e );
		}
		try {
			sth.release();
		} catch (Exception e) {
			if ( log.t3() ) log.error( e );
		}
		sth = null;
		res = null;
		result = null;
		
	}

	public IAttribute getAttribute(int i) throws MorseException {
		return attr.attr.getAttribute( colsNames[ i ] );
	}

	public String[] getColumns() throws MorseException {
		initCols();
		return colsNames;
	}
	
	private synchronized void initCols() {
		if ( colsNames != null ) return;
		LinkedList<String> out = new LinkedList<String>();
		colsIndex = new Hashtable<String,Integer>();
		int cnt = 0;
		for ( Iterator x = attr.attr.getAttributes(); x.hasNext(); ) {
			String name = ((IAttribute)x.next()).getName();
			out.add( name );
			colsIndex.put( name.toLowerCase(), cnt );
			cnt++;
		}
		
		colsNames = (String[])out.toArray( new String[ out.size() ] );
		
	}

	public String getRawString(String name) throws MorseException {
		try {
			return res.getString( name );
		} catch ( SQLException e ) {
			throw new MorseException( 0, e );
		}
	}

	public String getRawString(int i) throws MorseException {
		try {
			return res.getString( i + 1 );
		} catch ( SQLException e ) {
			throw new MorseException( 0, e );
		}
	}

	public boolean next() throws MorseException {
		try {
			return res.next();
		} catch ( SQLException e ) {
			throw new MorseException( 0, e );
		}
	}

	public int getAttributeCount() {
		initCols();
		return colsNames.length;
	}

	@Override
	public IAttribute getAttribute(String name) throws MorseException {
		
		initCols();
		Integer i = colsIndex.get( name.toLowerCase() );
		if ( i == null )
			throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
		return getAttribute( i );
		
		// return attr.attr.getAttribute( name.toLowerCase() );
	}

	@Override
	// dummy
	public int getPreferedQuereType() {
		return 0;
	}

	@Override
	// dummy
	public ITableRead getTable(String name) throws MorseException {
		return null;
	}

	@Override
	// dummy
	public ITableRead getTable(int index) throws MorseException {
		return null;
	}

	public boolean reset() throws MorseException {
		try {
			res.beforeFirst();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	public InputStream getInputStream() throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public OutputStream getOutputStream() throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public void finalize() {
		if ( sth != null ) {
			if ( config.isSthTrace() ) log.debug( ACast.toString( "Auto-Close", startTrace ) );
			close();
		}
	}
	
}
