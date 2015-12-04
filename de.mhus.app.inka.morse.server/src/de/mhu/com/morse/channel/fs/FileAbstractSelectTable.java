package de.mhu.com.morse.channel.fs;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class FileAbstractSelectTable extends AbstractSelectResult{

	int cnt = -1;
	int pos = 0;
	int index = -1;
	private IAttribute tableAttr;
	private String[] cols;
	private FileAbstractSelectResult result;
	private String name;
	
	FileAbstractSelectTable( FileAbstractSelectResult pResult, String pName ) throws MorseException {
		result = pResult;
		name = pName;
		
		cnt = result.getSize( name );
		index = result.findIndexOf( name );
		tableAttr = result.getAttribute( index );
		LinkedList ll = new LinkedList();
		for ( Iterator i = tableAttr.getAttributes(); i.hasNext(); ) {
			ll.add( ((IAttribute)i.next()).getName() );
		}
		cols = (String[])ll.toArray( new String[ ll.size() ] );
	}
	
	public String getRawString(String name2) throws MorseException {
		return result.getRawString( name, pos-1, name2 );
	}

	public boolean next() throws MorseException {
		if ( pos >= cnt ) return false;
		pos++;
		return true;
	}

	public String[] getColumns() throws MorseException {
		return cols;
	}

	public String getRawString(int i) throws MorseException {
		return getRawString( cols[ i ] );
	}

	public IAttribute getAttribute(int i) throws MorseException {
		return tableAttr.getAttribute( cols[ i ] );
	}

	public IAttribute getAttribute(String name2) throws MorseException {
		return tableAttr.getAttribute( name2 );
	}
	
	public void close() {
	}

	public int getAttributeCount() {
		return cols.length;
	}

	@Override
	public int getPreferedQuereType() {
		return 0;
	}

	@Override
	public ITableRead getTable(String name) throws MorseException {
		return null;
	}

	@Override
	public ITableRead getTable(int index) throws MorseException {
		return null;
	}

	public boolean reset() throws MorseException {
		pos = 0;
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
