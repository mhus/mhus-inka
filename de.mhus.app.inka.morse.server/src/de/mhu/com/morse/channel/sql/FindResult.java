package de.mhu.com.morse.channel.sql;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IQueryFunction;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.AbstractObjectRead;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;

public class FindResult extends AbstractSelectResult {

	private static AL log = new AL( FindResult.class );
	private IAttribute[] attributeTypes;
	private String[] values;
	private String[] names;
	private boolean canNext = true;
	private Hashtable<String, Integer> index;
	
	public FindResult(IConnection con, Descriptor desc, IQueryResult result) {
		
		
		try {
			
			
			// execute
			
			while ( result.next() ) {
				
				for ( int i = 0; i < desc.attrSize; i++ ) {
					if ( desc.attrs[ i ].functionObject != null ) {
						
						SqlUtils.executeFunction( desc.attrs[ i ], result );
						
					}
					
				}
				
			}
			
			attributeTypes = new IAttribute[ desc.attrSize ];
			values = new String[ desc.attrSize ];
			names  = new String[ desc.attrSize ];
			index = new Hashtable<String, Integer>();
			for ( int i = 0; i < desc.attrSize; i++ ) {
				attributeTypes[ i ] = desc.attrs[ i ].functionObject.getType();
				values[ i ] = desc.attrs[ i ].functionObject.getResult();
				names[ i ] = desc.attrs[ i ].alias;
				if ( names[ i ] == null )
					names[ i ] = desc.attrs[ i ].name;
				index.put( names[ i ], i );
			}
			
			returnCode = 1;
			
		} catch (MorseException e) {
			log.error( e );
			errorCode = -1;
			errorInfo = e.toString();
			returnCode = 0;
			canNext = false;
		} catch ( Throwable e ) {
			log.error( e );
			errorCode = -1;
			errorInfo = e.toString();
			returnCode = 0;
			canNext = false;			
		}
		
		result.close();
		
	}

	@Override
	public void close() {
		
	}

	@Override
	public IAttribute getAttribute(String name) throws MorseException {
		return attributeTypes[ getIndex( name ) ];
	}

	private int getIndex(String name) {
		return index.get( name );
	}

	@Override
	public IAttribute getAttribute(int index) throws MorseException {
		return attributeTypes[ index ];
	}

	@Override
	public int getAttributeCount() {
		return attributeTypes.length;
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
		return values[ index ];
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
			canNext  = false;
			return true;
		}
		return false;
	}

	public boolean reset() throws MorseException {
		if ( errorCode != 0 )
			return false;
		canNext = true;
		return true;
	}

	@Override
	public int getPreferedQuereType() {
		return IQueryResult.QUEUE_ONE_PACKAGE;
	}

	public InputStream getInputStream() throws MorseException {
		return null;
	}

	public OutputStream getOutputStream() throws MorseException {
		return null;
	}

}
