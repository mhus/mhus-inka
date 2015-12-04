package de.mhu.com.morse.mql;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Hashtable;

import de.mhu.com.morse.obj.AbstractObjectRead;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.utils.MorseException;

public class SingleRowResult extends AbstractObjectRead implements IQueryResult {

	private boolean canNext = true;
	private IAttribute[] attributes;
	private String[] values;
	private String[] cols;
	private Hashtable<String,Integer> names = new Hashtable<String, Integer>();
	
	public SingleRowResult( ITypes types, String[] pAttributes, String[] pValues ) throws MorseException {
		this( types, pAttributes, pAttributes, pValues );
	}
	
	public SingleRowResult( ITypes types, String[] pCols, String[] pAttributes, String[] pValues ) throws MorseException {
		attributes = new IAttribute[ pAttributes.length ];
		cols = pCols;
		values = pValues;
		
		if ( attributes.length != cols.length || attributes.length != values.length )
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE );
		
		for ( int i = 0; i < pAttributes.length; i++ ) {
			attributes[ i ] = types.getAttributeByCanonicalName( pAttributes[ i ] );
			if ( attributes[ i ] == null )
				throw new MorseException( MorseException.ATTR_NOT_FOUND, pAttributes[ i ] );
			if ( attributes[ i ].isTable() )
				throw new MorseException( MorseException.ATTR_IS_A_TABLE, pAttributes[ i ] );

			names.put( cols[i], i );
			
		}
		
	}
	
	public SingleRowResult(IAttribute[] pAttributes, String[] pCols, String[] pValues) throws MorseException {
		attributes = pAttributes;
		cols = pCols;
		values = pValues;
		if ( attributes.length != cols.length || attributes.length != values.length )
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE );
		
	}

	public int getErrorCode() {
		return 0;
	}

	public String getErrorInfo() {
		return null;
	}

	public InputStream getInputStream() throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public OutputStream getOutputStream() throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public int getPreferedQuereType() {
		return IQueryResult.QUEUE_ONE_PACKAGE;
	}

	public long getReturnCode() {
		return 1;
	}

	public ITableRead getTable(String name) throws MorseException {
		return null;
	}

	public ITableRead getTable(int index) throws MorseException {
		return null;
	}

	public void close() {
		canNext = false;
	}

	public IAttribute getAttribute(int i) throws MorseException {
		return attributes[ i ];
	}

	public IAttribute getAttribute(String name) throws MorseException {
		return getAttribute( getIndex( name ) );
	}

	private int getIndex(String name) throws MorseException {
		Integer out = names.get( name );
		if ( out == null )
			throw new MorseException( MorseException.ATTR_NOT_FOUND, name );
		return out;
	}

	public int getAttributeCount() {
		return attributes.length;
	}

	public boolean next() throws MorseException {
		if ( ! canNext ) 
			return false;
		canNext = false;
		return true;
	}

	public boolean reset() throws MorseException {
		canNext = true;
		return true;
	}

	@Override
	public String[] getColumns() throws MorseException {
		return cols;
	}

	@Override
	public String getRawString(String name) throws MorseException {
		return values[ getIndex( name ) ];
	}

	@Override
	public String getRawString(int index) throws MorseException {
		return values[ index ];
	}

}
