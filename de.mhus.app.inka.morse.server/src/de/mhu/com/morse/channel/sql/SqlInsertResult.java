package de.mhu.com.morse.channel.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;

public class SqlInsertResult implements IQueryResult {

	private String id;
	private boolean next = false;

	public SqlInsertResult(String newId) {
		id = newId;
	}

	public int getErrorCode() {
		return 0;
	}

	public String getErrorInfo() {
		return null;
	}

	public long getReturnCode() {
		return 0;
	}

	public ITableRead getTable(String name) throws MorseException {
		return null;
	}

	public ITableRead getTable(int index) throws MorseException {
		return null;
	}

	public void close() {
		next = true;
	}

	public IAttribute getAttribute(int i) throws MorseException {
		if ( i == 0 )
			return IAttributeDefault.ATTR_OBJ_M_ID;
		return null;
	}

	public boolean getBoolean(int index) throws MorseException {
		return false;
	}

	public boolean getBoolean(String string) throws MorseException {
		return false;
	}

	public String[] getColumns() throws MorseException {
		return new String[] { IAttribute.M_ID };
	}

	public Date getDate(String string) throws MorseException {
		return null;
	}

	public Date getDate(int index) throws MorseException {
		return null;
	}

	public double getDouble(String string) throws MorseException {
		return 0;
	}

	public double getDouble(int index) throws MorseException {
		return 0;
	}

	public int getInteger(String string) throws MorseException {
		return 0;
	}

	public int getInteger(int index) throws MorseException {
		return 0;
	}

	public long getLong(String string) throws MorseException {
		return 0;
	}

	public long getLong(int index) throws MorseException {
		return 0;
	}

	public String getString(String name) throws MorseException {
		if ( IAttribute.M_ID.equals( name ) )
			return id;
		return null;
	}

	public String getString(int index) throws MorseException {
		if ( index == 0 )
			return id;
		return null;
	}

	public boolean next() throws MorseException {
		if ( ! next ) {
			next = true;
			return true;
		}
		return false;
	}

	public int getPreferedQuereType() {
		return QUEUE_ONE_PACKAGE;
	}

	public int getAttributeCount() {
		return 1;
	}

	public IAttribute getAttribute(String name) throws MorseException {
		return IAttributeDefault.ATTR_OBJ_M_ID;
	}

	public boolean reset() throws MorseException {
		next = false;
		return true;
	}

	public InputStream getInputStream() throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public OutputStream getOutputStream() throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public Object getObject(int index) throws MorseException {
		return getString(index);
	}

	public Object getObject(String name) throws MorseException {
		return getString(name);
	}

}
