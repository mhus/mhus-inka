package de.mhu.com.morse.channel.exec;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Hashtable;

import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class ExecResult extends AbstractSelectResult {

	private IAttribute[] types;
	private String[] names;
	private Hashtable<String, Integer> indexMap = new Hashtable<String, Integer>();
	private Object[] ret;
	private boolean canNext = true;

	public ExecResult(Object[] pRet) {
		
		ret = pRet;
		types = new IAttribute[ ret.length ];
		names = new String[ ret.length ];
		
		for ( int i = 0; i < ret.length; i++ ) {
			types[ i ] = ExecVariant.getTypeFor( ret[ i ] );
			names[ i ] = ExecVariant.getNameFor( i, ret[ i ] );
			indexMap.put( names[ i ], i );
		}
		
	}

	@Override
	public int getPreferedQuereType() {
		return IQueryResult.QUEUE_ONE_PACKAGE;
	}

	@Override
	public void close() {
	}

	@Override
	public IAttribute getAttribute(String name) throws MorseException {
		return getAttribute( getIndex( name ) );
	}

	private int getIndex(String name) {
		return indexMap.get( name );
	}

	@Override
	public IAttribute getAttribute(int index) throws MorseException {
		return types[ index ];
	}

	@Override
	public int getAttributeCount() {
		return types.length;
	}

	@Override
	public String[] getColumns() throws MorseException {
		return names;
	}

	@Override
	public String getRawString(String name) throws MorseException {
		return getRawString( getIndex( name ) );
	}

	@Override
	public String getRawString(int index) throws MorseException {
		return ExecVariant.getStringValueFor( ret[ index ] );
	}

	@Override
	public ITableRead getTable(String name) throws MorseException {
		return null;
	}

	@Override
	public ITableRead getTable(int index) throws MorseException {
		return null;
	}

	@Override
	public boolean next() throws MorseException {
		if ( canNext ) {
			canNext = false;
			return true;
		}
		return false;
	}

	public InputStream getInputStream() throws MorseException {
		return null;
	}

	public OutputStream getOutputStream() throws MorseException {
		return null;
	}

	public boolean reset() throws MorseException {
		canNext = true;
		return true;
	}

}
