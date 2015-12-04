package de.mhu.com.morse.channel.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectFileStore;

public class InternalLoadResult implements IQueryResult {

	private static AL log = new AL( InternalLoadResult.class );
	
	private InputStream is;

	public InternalLoadResult( File f ) throws MorseException {
		
		try {
			is = new FileInputStream( f );
		} catch (FileNotFoundException e) {
			throw new MorseException( MorseException.ERROR, e );
		}
		
	}

	public int getErrorCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getErrorInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getInputStream() throws MorseException {
		return is;
	}

	public OutputStream getOutputStream() throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPreferedQuereType() {
		return IQueryResult.QUEUE_STREAM_OUT;
	}

	public long getReturnCode() {
		// TODO Auto-generated method stub
		return 0;
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
		if ( is == null ) return;
		try {
			is.close();
		} catch (IOException e) {
			log.error( e );
		}
		is = null;
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
