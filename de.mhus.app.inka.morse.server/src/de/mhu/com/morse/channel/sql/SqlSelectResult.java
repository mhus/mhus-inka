package de.mhu.com.morse.channel.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.ACast;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.dtb.Sth;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.config.QueryConfig;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.lib.plugin.utils.IAfLogger;

public class SqlSelectResult extends AbstractSelectResult {

	private static AL log = new AL( SqlSelectResult.class );
	private static QueryConfig config = (QueryConfig)ConfigManager.getConfig( QueryConfig.NAME );
	
	protected ResultSet res;
	protected Sth sth;
	protected SqlChannel con;
	private UserInformation user;
	Descriptor desc;
	private int[] sqlIndexMap;
	SqlDriver driver;
	private String[] colsNames;
	private Hashtable<String, Integer> colsIndex;
	private long rowCnt = 0;
	private long offset = 0;
	private long limit  = -1;

	private Hashtable<String, Attr> functionsMap;
	private StackTraceElement[] startTrace;
	private LinkedList<SqlSelectTable> resources = new LinkedList<SqlSelectTable>();
	
	public SqlSelectResult( SqlDriver pDriver, SqlChannel pcon, Descriptor pDesc, ResultSet pres, Sth psth, UserInformation pUser) {
		
		if ( config.isSthTrace() )
			startTrace = Thread.currentThread().getStackTrace();
		
		desc = pDesc;
		res = pres;
		sth = psth;
		con = pcon;
		driver = pDriver;
		user = pUser;
		
		if ( res == null ) {
			errorCode = 1;
			returnCode = -1;
			return;
		}
		
		sqlIndexMap = new int[ desc.attrSize ];
		functionsMap = new Hashtable<String, Attr>();
		int cnt = 1;
		for ( int i = 0; i < desc.attrSize; i++ ) {
			if ( ! desc.attrs[i].attr.isTable() && desc.attrs[i].function == null ) {
				sqlIndexMap[ i ] = cnt;
				cnt++;
			} else
			if ( desc.attrs[i].function != null ) {
				sqlIndexMap[ i ] = -2;
				functionsMap.put( desc.attrs[i].alias, desc.attrs[i] );
			} else
				sqlIndexMap[ i ] = -1;
		}
		errorCode  = 0;
		returnCode = 0;
		
		if ( desc.offset != null ) {
			offset = Long.parseLong( desc.offset );
		}
		if ( desc.limit != null ) {
			limit = Long.parseLong( desc.limit );
		}
	
		try {
			while ( rowCnt < offset && next() ) {}
		} catch ( MorseException e ) {
			log.info( e );
		}
	}

	public ITableRead getTable(String name) throws MorseException {
		initCols();
		Integer i = colsIndex.get( name.toLowerCase() );
		if ( i == null )
			throw new MorseException( MorseException.TABLE_NOT_FOUND, name );
		return getTable( i );			
	}

	public ITableRead getTable(int index) throws MorseException {
		try {
			SqlSelectTable table = new SqlSelectTable( this, index );
			synchronized ( resources ) {
				resources.add( table );
			}
			return table;
		} catch (Exception e) {
			if ( e instanceof MorseException ) throw (MorseException)e;
			throw new MorseException( MorseException.SQL_EXCEPTION, e.toString(), e );
		}
	}
	
	void intReleaseResource( SqlSelectTable table ) {
		if ( resources == null ) return;
		synchronized ( resources ) {
			resources.remove( table );
		}
	}

	public synchronized void close() {
		if ( sth == null ) return;
		try {
			res.close();
			
			synchronized ( resources ) {
				Object[] list = resources.toArray();
				resources.clear();
				for ( int i = 0; i < list.length; i++ )
					((SqlSelectTable)list[i]).close();
			}
			
			if ( desc.tmpTables != null ) {
				for ( Iterator<String> i = desc.tmpTables.iterator(); i.hasNext(); )
					try {
						sth.executeUpdate( driver.getDropTmpTableSql( i.next() ) );
					} catch ( SQLException sqle ) {
						log.info( sqle );
					}
			}
			
			sth.release();
		} catch ( Exception e ) {
			if ( log.t3() ) log.debug( e );
		}
		sth = null;
		res = null;
		resources = null;
		desc = null;
	}

	public IAttribute getAttribute(int i) throws MorseException {
		return desc.attrs[i].attr;
	}
	
	@Override
	public IAttribute getAttribute(String name) throws MorseException {
		initCols();
		Integer i = colsIndex.get( name.toLowerCase() );
		if ( i == null )
			throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
		return getAttribute( i );
	}

	public String[] getColumns() throws MorseException {
		initCols();
		return colsNames;
	}
	
	private synchronized void initCols() {
		if ( colsNames != null ) return;
		colsNames = new String[ desc.attrSize ];
		colsIndex = new Hashtable<String,Integer>();
		for ( int i = 0; i < desc.attrSize; i++ ) {
			colsNames[i] = desc.attrs[i].alias;
			colsIndex.put( desc.attrs[i].alias.toLowerCase(), i );
		}
	}

	public String getRawString(String name) throws MorseException {
		try {
			String out = getRawFunction( name );
			if ( out != null ) return out;
			return res.getString( name );
		} catch (SQLException e) {
			throw new MorseException( 0, e );
		}
	}

	public String getRawString(int i) throws MorseException {
		try {
			int sqlIndex = sqlIndexMap[ i ];
			if ( sqlIndex == -1 )
				throw new MorseException( MorseException.ATTR_IS_A_TABLE, getColumns()[ i ] );
			if ( sqlIndex == -2 ) {
				// it's a function
				return getRawFunction( getColumns()[ i ] );
			}
			return res.getString( sqlIndex );
		} catch (SQLException e) {
			throw new MorseException( 0, e );
		}
	}

	private String getRawFunction(String name) {
		Attr a = functionsMap.get( name );
		if ( a== null ) return null;
		return a.functionObject.getResult();
	}

	public boolean next() throws MorseException {
		if ( limit >= 0 && rowCnt >= ( limit + offset ) ) 
			return false;
		try {
			IAclManager aclManager = con.getAclManager();
			while ( true ) {
				boolean ret = res.next();
				if ( ! ret ) return false;
				
				// check if row is accessable
				for ( int i = 0; i < desc.attrSizeInt; i++ ) {
					Attr ai = desc.attrsInt[i];
					if ( ai.attrName.equals( IAttribute.M_ACL ) && ! aclManager.hasRead(user, getRawString( ai.alias ) ) )
						continue; // not - try next row
				}
				
				// update functions
				for ( Iterator<Attr> i = functionsMap.values().iterator(); i.hasNext(); )
					SqlUtils.executeFunction( i.next(), this );
				
				rowCnt++;
				return true;
			}
		} catch (SQLException e) {
			throw new MorseException( 0, e );
		}
	}

	public int getPreferedQuereType() {
		return QUEUE_FETCH;
	}

	public int getAttributeCount() {
		return desc.attrSize;
	}

	public boolean reset() throws MorseException {
		try {
			res.first();
			
			// reset functions
			for ( Iterator<Attr> i = functionsMap.values().iterator(); i.hasNext(); )
				i.next().functionObject.resetFunction();
			
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
