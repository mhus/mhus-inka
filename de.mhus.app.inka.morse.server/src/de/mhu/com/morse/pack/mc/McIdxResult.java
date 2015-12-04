package de.mhu.com.morse.pack.mc;

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

public class McIdxResult extends AbstractSelectResult {

	private static AL log = new AL( McIdxResult.class );
	private static QueryConfig config = (QueryConfig)ConfigManager.getConfig( QueryConfig.NAME );
	
	protected ResultSet res;
	protected Sth sth;
	private UserInformation user;
	private String[] colsNames;
	private Hashtable<String, Integer> colsIndex;
	private long rowCnt = 0;
	private long offset = 0;
	private long limit  = -1;

	private StackTraceElement[] startTrace;
	private IAclManager aclManager;
	private IAttribute[] attrs;
	
	public McIdxResult( String[] pColNames, IAttribute[] pAttrs, ResultSet pres, Sth psth, IAclManager pAclManager, UserInformation pUser,
			String pOffset,
			String pLimit
			
			) {
		
		if ( config.isSthTrace() )
			startTrace = Thread.currentThread().getStackTrace();
		
		aclManager = pAclManager;
		res = pres;
		sth = psth;
		user = pUser;
		attrs = pAttrs;
		colsNames = pColNames;
		
		if ( res == null ) {
			errorCode = 1;
			returnCode = -1;
			return;
		}
		
		errorCode  = 0;
		returnCode = 0;
		
		if ( pOffset != null ) {
			offset = Long.parseLong( pOffset );
		}
		if ( pLimit != null ) {
			limit = Long.parseLong( pLimit );
		}
	
		colsIndex = new Hashtable<String,Integer>();
		for ( int i = 0; i < colsNames.length; i++ ) {
			colsIndex.put( colsNames[i].toLowerCase(), i );
		}
		
		try {
			while ( rowCnt < offset && next() ) {}
		} catch ( MorseException e ) {
			log.info( e );
		}
	}

	public ITableRead getTable(String name) throws MorseException {
		throw new MorseException( MorseException.ATTR_NOT_A_TABLE, name );		
	}

	public ITableRead getTable(int index) throws MorseException {
		throw new MorseException( MorseException.ATTR_NOT_A_TABLE, String.valueOf( index ) );	
	}
	
	public synchronized void close() {
		if ( sth == null ) return;
		try {
			res.close();
			
			sth.release();
		} catch ( Exception e ) {
			if ( log.t3() ) log.debug( e );
		}
		sth = null;
		res = null;
		attrs = null;
	}

	public IAttribute getAttribute(int i) throws MorseException {
		return attrs[i];
	}
	
	@Override
	public IAttribute getAttribute(String name) throws MorseException {
		Integer i = colsIndex.get( name.toLowerCase() );
		if ( i == null )
			throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
		return getAttribute( i );
	}

	public String[] getColumns() throws MorseException {
		return colsNames;
	}
	
	public String getRawString(String name) throws MorseException {
		try {
			return res.getString( name );
		} catch (SQLException e) {
			throw new MorseException( 0, e );
		}
	}

	public String getRawString(int i) throws MorseException {
		try {
			return res.getString( i+1 );
		} catch (SQLException e) {
			throw new MorseException( 0, e );
		}
	}

	public boolean next() throws MorseException {
		if ( limit >= 0 && rowCnt >= ( limit + offset ) ) 
			return false;
		try {
			while ( true ) {
				boolean ret = res.next();
				if ( ! ret ) return false;
				
				// check if row is accessable
				if ( ! aclManager.hasRead(user, res.getString( "m_acl" ) ) )
						continue;
				
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
		return attrs.length;
	}

	public boolean reset() throws MorseException {
		try {
			res.first();
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
