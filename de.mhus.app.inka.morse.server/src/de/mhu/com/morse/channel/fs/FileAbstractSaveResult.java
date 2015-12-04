package de.mhu.com.morse.channel.fs;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

import de.mhu.lib.io.SizeCountOutputStream;
import de.mhu.lib.io.SizeCountWriter;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.channel.ITransaction;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectFileStore;

public class FileAbstractSaveResult implements IQueryResult {

	private static AL log = new AL( FileAbstractSaveResult.class );
	private String id;
	private Btc btc;
	private boolean next = false;
	private SizeCountOutputStream sizeCount;
	private FileAbstractChannel channel;
	private String parentId;
	private String parentType;

	public FileAbstractSaveResult( FileAbstractChannel pChannel, String pNewId, Btc obj, ObjectFileStore[] fs, String pParentId, String pParentType ) throws MorseException, IOException {
		id = pNewId;
		btc = obj;
		channel = pChannel;
		parentId = pParentId;
		parentType = pParentType;
		sizeCount = new SizeCountOutputStream( new BufferedOutputStream( fs[1].getOutputStream( id ) ) );
	}

	public int getErrorCode() {
		return 0;
	}

	public String getErrorInfo() {
		return null;
	}

	public int getPreferedQuereType() {
		return IQueryResult.QUEUE_STREAM_IN;
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
		ITransaction tr = null;
		try {
			tr = channel.getConnection().startTransaction();
			sizeCount.flush();
			sizeCount.close();
			btc.doSaveContent( sizeCount.getSize() );
			channel.getDriver().storeObject( btc.getType(), btc, id );
			channel.getConnection().eventContentSaved( channel.getName(), id, parentId, parentType );
			channel.getConnection().maybeCommit( tr );
		} catch (Throwable e) {
			log.error( e );
			try {
				channel.getConnection().maybeRollback( tr );
			} catch (MorseException e1) {
				log.error( e1 );
			}
		}
		
		next = true;
	}

	public IAttribute getAttribute(int i) throws MorseException {
		if ( i == 0 )
			return IAttributeDefault.ATTR_OBJ_M_ID;
		return null;
	}

	public IAttribute getAttribute(String name) throws MorseException {
		return IAttributeDefault.ATTR_OBJ_M_ID;
	}

	public int getAttributeCount() {
		return 1;
	}

	public boolean getBoolean(int index) throws MorseException {
		return false;
	}

	public boolean getBoolean(String name) throws MorseException {
		return false;
	}

	public String[] getColumns() throws MorseException {
		return new String[] { IAttribute.M_ID };
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
		if ( index == 0 )
			return sizeCount.getSize();
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

	public boolean reset() throws MorseException {
		next = false;
		return true;
	}

	public OutputStream getOutputStream() throws MorseException {
		return sizeCount;
	}
	
	public InputStream getInputStream() throws MorseException {
		return null;
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
