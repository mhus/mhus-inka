package de.mhu.com.morse.mql;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class ErrorResult implements IQueryResult {

	private long rc;
	private int errorCode;
	private String errorInfo;

	public ErrorResult(long pRc, int pErrorCode, String pErrorInfo ) {
		rc = pRc;
		errorCode = pErrorCode;
		errorInfo = pErrorInfo;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public InputStream getInputStream() throws MorseException {
		return null;
	}

	public OutputStream getOutputStream() throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPreferedQuereType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getReturnCode() {
		return rc;
	}

	public ITableRead getTable(String name) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public ITableRead getTable(int index) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public IAttribute getAttribute(int i) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public IAttribute getAttribute(String name) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAttributeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getBoolean(int index) throws MorseException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getBoolean(String name) throws MorseException {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] getColumns() throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDate(String name) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getDate(int index) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public double getDouble(String name) throws MorseException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getDouble(int index) throws MorseException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInteger(String name) throws MorseException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInteger(int index) throws MorseException {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLong(String name) throws MorseException {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLong(int index) throws MorseException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getString(String name) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(int index) throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean next() throws MorseException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean reset() throws MorseException {
		// TODO Auto-generated method stub
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
