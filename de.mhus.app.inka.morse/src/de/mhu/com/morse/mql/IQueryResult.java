package de.mhu.com.morse.mql;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.utils.MorseException;

public interface IQueryResult extends IObjectRead {
	
	public static final int QUEUE_ONE_PACKAGE = 0;
	public static final int QUEUE_FETCH  = 1;
	public static final int QUEUE_STREAM_IN  = 2;
	public static final int QUEUE_STREAM_OUT = 3;
	
	public int getErrorCode();
	public String getErrorInfo();
	public long getReturnCode();
	public int getPreferedQuereType();
	
	public OutputStream getOutputStream() throws MorseException;
	public InputStream getInputStream() throws MorseException;
	
}
