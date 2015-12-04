package de.mhu.com.morse.channel.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import de.mhu.lib.dtb.StatementPool;
import de.mhu.lib.dtb.Sth;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class SaveRenditionQueryResult implements IQueryResult {

	private static final AL log = new AL( SaveRenditionQueryResult.class );
	
	private IQueryResult res;
	private Btc obj;
	private String rendId;
	private String format;
	private int index;

	private StatementPool pool;

	private SqlChannel channel;

	public SaveRenditionQueryResult(IQueryResult qres, Btc pObj, String pRendId, int pIndex, String pFormat, StatementPool pPool, SqlChannel pChannel  ) {
		res = qres;
		obj = pObj;
		rendId = pRendId;
		index = pIndex;
		format = pFormat;
		pool = pPool;
		channel = pChannel;
	}

	public void close() {
		res.close();
		long size = 0;
		try {
			size = res.getLong( 0 );
		} catch (MorseException e) {
		}

		try {
			obj.insertRendition(index, format, rendId, size );
			// obj.doUpdate();
//			 done if stream finish writing driver.getObjectManager().eventContentSaved(getName(), rendId, id, type.getName() );
			Sth sth = null;
			try {
				sth = pool.aquireStatement();
				SqlUtils.updateBtc( channel, sth, obj, true, true );
			} finally {
				sth.release();
			}
		} catch ( Exception e) {
			log.error( e );
		}
	}

	public IAttribute getAttribute(int i) throws MorseException {
		return res.getAttribute(i);
	}

	public IAttribute getAttribute(String name) throws MorseException {
		return res.getAttribute(name);
	}

	public int getAttributeCount() {
		return res.getAttributeCount();
	}

	public boolean getBoolean(int index) throws MorseException {
		return res.getBoolean(index);
	}

	public boolean getBoolean(String name) throws MorseException {
		return res.getBoolean(name);
	}

	public String[] getColumns() throws MorseException {
		return res.getColumns();
	}

	public Date getDate(int index) throws MorseException {
		return res.getDate(index);
	}

	public Date getDate(String name) throws MorseException {
		return res.getDate(name);
	}

	public double getDouble(int index) throws MorseException {
		return res.getDouble(index);
	}

	public double getDouble(String name) throws MorseException {
		return res.getDouble(name);
	}

	public int getErrorCode() {
		return res.getErrorCode();
	}

	public String getErrorInfo() {
		return res.getErrorInfo();
	}

	public InputStream getInputStream() throws MorseException {
		return res.getInputStream();
	}

	public int getInteger(int index) throws MorseException {
		return res.getInteger(index);
	}

	public int getInteger(String name) throws MorseException {
		return res.getInteger(name);
	}

	public long getLong(int index) throws MorseException {
		return res.getLong(index);
	}

	public long getLong(String name) throws MorseException {
		return res.getLong(name);
	}

	public Object getObject(int index) throws MorseException {
		return res.getObject(index);
	}

	public Object getObject(String name) throws MorseException {
		return res.getObject(name);
	}

	public OutputStream getOutputStream() throws MorseException {
		return res.getOutputStream();
	}

	public int getPreferedQuereType() {
		return res.getPreferedQuereType();
	}

	public long getReturnCode() {
		return res.getReturnCode();
	}

	public String getString(int index) throws MorseException {
		return res.getString(index);
	}

	public String getString(String name) throws MorseException {
		return res.getString(name);
	}

	public ITableRead getTable(int index) throws MorseException {
		return res.getTable(index);
	}

	public ITableRead getTable(String name) throws MorseException {
		return res.getTable(name);
	}

	public boolean next() throws MorseException {
		return res.next();
	}

	public boolean reset() throws MorseException {
		return res.reset();
	}

}
