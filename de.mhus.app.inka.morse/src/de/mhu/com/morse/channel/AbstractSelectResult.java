package de.mhu.com.morse.channel;

import java.util.Date;

import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.AbstractObjectRead;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public abstract class AbstractSelectResult  extends AbstractObjectRead implements IQueryResult {

	protected int    errorCode  = 0;
	protected String errorInfo  = null;
	protected long   returnCode = 0;

	public abstract int        getPreferedQuereType();
	
	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public long getReturnCode() {
		return returnCode;
	}

}
