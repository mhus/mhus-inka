package de.mhu.com.morse.channel.fs;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectFileStore;
import de.mhu.lib.utils.EmptyIterator;
import de.mhu.lib.utils.Properties;
import de.mhu.lib.utils.SingleIterator;

public class FileAbstractSelectResult extends AbstractSelectResult {

	private static AL log = new AL( FileAbstractSelectResult.class );
	private Iterator<String> list;
	private String whereAttr;
	private String whereVal;
	private int listPos;
	private Properties current = null;
	private Attr[] attr;
	private IType type;
	private Hashtable<String, String> aliasMap = new Hashtable<String, String>();
	private ObjectFileStore store;
	private String currentId;
	private FileAbstractDriver driver;
	private boolean isOneResult;

	public FileAbstractSelectResult( FileAbstractDriver pDriver, IType pType, ObjectFileStore pStore, String pWhereAttr, String pWhereVal, Attr[] pAttr ) throws MorseException {
		
		driver = pDriver;
		store = pStore;
		type = pType;
		attr = pAttr;
		whereAttr = pWhereAttr;
		whereVal  = pWhereVal;
		
		for ( int i = 0; i < pAttr.length; i++ )
			aliasMap.put( attr[i].attrAlias, pAttr[i].attrName.toLowerCase() );
		
		reset();
	}
	
	public String getRawString( String name )  throws MorseException {
		return current.getProperty( name.toLowerCase() );
	}
	
	public String getRawString( int pos )  throws MorseException {
		return current.getProperty( attr[ pos ].attrName.toLowerCase() );
	}
	
	public ITableRead getTable( final String name ) throws MorseException {
		return new FileAbstractSelectTable( this, name );
	}
	
	int findIndexOf( String name ) {
		
		for ( int i = 0; i < attr.length; i++ )
			if ( attr[i].attrAlias.equals( name ) )
				return i;
		return -1;
	}
	
	int getSize( String name ) {	
		try {
			return Integer.parseInt( current.getProperty( ((String)aliasMap.get( name )) + ".size" ) );
		} catch ( NumberFormatException e ) {
			return 0;
		}
	}
	
	public String getRawString( String table, int pos, String name ) {
		String k = ((String)aliasMap.get( table )) + '.' + pos + '.' + name.toLowerCase();
		return current.getProperty( k );
	}
	
	public boolean next() throws MorseException {
					
		while ( true ) {
			
			if ( ! list.hasNext() ) return false;

			currentId = list.next();
			
			try {
				current = new Properties();
				IType curType = type;
				do {
					Reader reader = driver.getFileStore().get( type.getName() )[0].getReader( currentId );
					current.load( reader );
					reader.close();
					
					curType = curType.getSuperType();
					
				} while ( curType != null );
				
				listPos++;
				
				if ( whereAttr != null ) {
					if ( whereVal.equals( current.getProperty( whereAttr ) ) )
						return true;
				} else
					return true;
				
			} catch ( Exception e ) {

				throw new MorseException( 0, e );
			}
			
		}
		
	}

	public String[] getColumns() throws MorseException {
		String[] out = new String[ attr.length ];
		for ( int i = 0; i < attr.length; i++ )
			out[i] = attr[i].attrAlias;
		return out;
	}

	public IAttribute getAttribute(int i) throws MorseException {
		return attr[i].attr;
	}
	
	public IAttribute getAttribute(String name) throws MorseException {
		return type.getAttribute( name );
	}

	public ITableRead getTable(int index) throws MorseException {
		return getTable( attr[ index ].attrAlias );
	}

	public void close() {
		list = null;
	}

	public int getPreferedQuereType() {
		return isOneResult ? QUEUE_ONE_PACKAGE : QUEUE_FETCH;
	}

	public int getAttributeCount() {
		return attr.length;
	}

	public boolean reset() throws MorseException {
		try {
			isOneResult = false;
			if ( IAttribute.M_ID.equals( whereAttr ) ) {
				if ( store.exists( whereVal ) )
					list = new SingleIterator<String>( whereVal );
				else
					list = new EmptyIterator<String>();
				isOneResult = true;
			} else {
				list = store.idIterator();
			}
			listPos = 0;

		} catch ( MorseException e ) {
			list = null;
			log.error( e );
			errorCode  = -1;
		}
		return true;
	}

	public InputStream getInputStream() throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

	public OutputStream getOutputStream() throws MorseException {
		// TODO Auto-generated method stub
		return null;
	}

}
