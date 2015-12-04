package de.mhu.com.morse.channel.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;

import de.mhu.lib.dtb.Sth;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.channel.mql.UpdateSetParser;
import de.mhu.com.morse.channel.mql.UpdateSetParser.UpdateSetDescription;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.ITable;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ServerTypesUtil;

public class SqlUpdateResult implements IQueryResult {

	private static AL log = new AL( SqlUpdateResult.class );
	private SqlDriver driver;
	private Sth sthUpdate;
	private long result = 0;
	private SqlChannel channel;
	private Sth sth;

	public SqlUpdateResult(SqlDriver pDriver, 
			SqlChannel pChannel, Sth pSth, String sql, Sth pSthUpdate, UpdateSetDescription setDesc, Descriptor selDesc, UserInformation user, boolean isRaw, IType defType, boolean isNoCommit, boolean isNoEvent ) throws SQLException, MorseException {
		driver = pDriver;
		channel = pChannel;
		sthUpdate = pSthUpdate;
		sth = pSth;
		IAclManager aclMananger = driver.getAclManager();
		ITypes types = driver.getTypes();
		ResultSet res = sth.executeQuery( sql );
		Btc btc = null;
		if ( isRaw ) {
			btc = ServerTypesUtil.createBtc( channel.getConnection(), defType );
			btc.initObject(defType, channel.getConnection(), driver.getTypes(), user, driver.getAclManager() );
		}
		long offset = 0;
		long limit  = -1;
		long cnt = 0;
		if ( selDesc.offset != null ) {
			offset = Long.parseLong( selDesc.offset );
		}
		if ( selDesc.limit != null ) {
			limit = Long.parseLong( selDesc.limit );
		}
		while ( res.next() ) {
			String acl = res.getString( 2 );
			if ( isRaw || aclMananger.hasWrite( user, acl ) ) {
				if ( cnt >= offset ) {
					if ( limit >= 0 && result >= limit ) break;
					String typeName = res.getString( 3 );
					String id       = res.getString( 1 );
					IType type = types.get( typeName );
					if ( !isRaw ) btc = ServerTypesUtil.createBtc( channel.getConnection(), type );
					IQueryResult fetchRes = channel.fetch( id, user, false );
					fetchRes.next();
					if ( !isRaw ) 
						btc.initObject(type, channel.getConnection(), fetchRes, driver.getTypes(), user, driver.getAclManager() );
					else
						btc.loadData( fetchRes );
	
					UpdateSetParser.changeBtc( setDesc, btc );
					if ( ! isRaw ) btc.doUpdate();
					SqlUtils.updateBtc( channel, sthUpdate, btc, !isNoCommit, !isNoEvent );
					result++;
				}
				cnt++;
			}
		}
		close();
	}

	public int getErrorCode() {
		return 0;
	}

	public String getErrorInfo() {
		return null;
	}

	public int getPreferedQuereType() {
		return IQueryResult.QUEUE_ONE_PACKAGE;
	}

	public long getReturnCode() {
		return result;
	}

	public InputStream getInputStream() throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public OutputStream getOutputStream() throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}


	public ITableRead getTable(String name) throws MorseException {
		return null;
	}

	public ITableRead getTable(int index) throws MorseException {
		return null;
	}

	public void close() {
		if ( sth != null )
			try {
				sth.release();
			} catch (Exception e) {
				log.error( e );
			}
		if ( sthUpdate != null )
			try {
				sthUpdate.release();
			} catch (Exception e) {
				log.error( e );
			}
	}

	public IAttribute getAttribute(int i) throws MorseException {
		return null;
	}

	public IAttribute getAttribute(String name) throws MorseException {
		return null;
	}

	public int getAttributeCount() {
		return 0;
	}

	public boolean getBoolean(int index) throws MorseException {
		return false;
	}

	public boolean getBoolean(String name) throws MorseException {
		return false;
	}

	public String[] getColumns() throws MorseException {
		return new String[0];
	}

	public Date getDate(String name) throws MorseException {
		return null;
	}

	public Date getDate(int index) throws MorseException {
		return null;
	}

	public double getDouble(String name) throws MorseException {
		return 0;
	}

	public double getDouble(int index) throws MorseException {
		return 0;
	}

	public int getInteger(String name) throws MorseException {
		return 0;
	}

	public int getInteger(int index) throws MorseException {
		return 0;
	}

	public long getLong(String name) throws MorseException {
		return 0;
	}

	public long getLong(int index) throws MorseException {
		return 0;
	}

	public String getString(String name) throws MorseException {
		return null;
	}

	public String getString(int index) throws MorseException {
		return null;
	}

	public boolean next() throws MorseException {
		return false;
	}

	public boolean reset() throws MorseException {
		return false;
	}

	public Object getObject(int index) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getObject(String name) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

}
