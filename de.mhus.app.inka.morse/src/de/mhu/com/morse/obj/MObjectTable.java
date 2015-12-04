package de.mhu.com.morse.obj;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;

public class MObjectTable extends MObject implements ITable {

	protected LinkedList<Object[][]> rows = new LinkedList<Object[][]>();
	protected int cursor = -1;
	private boolean create;
	private IAttribute superAttr;
	
	public MObjectTable(IType pType, IAttribute pAttribute) throws MorseException {
		type = pType;
		superAttr = pAttribute;
		
		int cnt = 0;
		for ( Iterator<IAttribute> i = superAttr.getAttributes(); i.hasNext(); ) {
			IAttribute attr = i.next();
			cnt++;
		}
		attributes = new IAttribute[ cnt ];
		colNames   = new String[ cnt ];
		values     = new Object[ cnt ][];
		cnt = 0;
		for ( Iterator<IAttribute> i = superAttr.getAttributes(); i.hasNext(); ) {
			IAttribute attr = i.next();
			attributes[ cnt ] = attr;
			colNames [ cnt ] = attr.getName();
			attrIndex.put( colNames[ cnt ], cnt );
			values[ cnt ] = new Object[ 2 ];
			values[ cnt ][0] = attr.getDefaultValue();
			cnt++;
		}
		
	}

	public void appendRow() throws MorseException {
		if ( ! create )
			throw new MorseException( MorseException.NO_ROW_CREATED );
		create = false;
		dirty  = true;
		rows.add( values );
		if ( cursor != -1 )
			values = rows.get( cursor );
		else
			cursor = rows.size()-1;
	}

	public void createRow() throws MorseException {
		values = new Object[ getAttributeCount() ][];
		for ( int i = 0; i < getAttributeCount(); i++ ) {
			values[ i ] = new Object[ 2 ];
			IAttribute attr = getAttribute( i );
			if ( ! attr.isTable() )
				values[ i ][ 0 ] = attr.getDefaultValue();
		}
		create = true;
	}

	public int getCursor() {
		return cursor;
	}

	public int getSize() {
		return rows.size();
	}

	public void insertRow(int pos) throws MorseException {
		if ( ! create )
			throw new MorseException( MorseException.NO_ROW_CREATED );
		create = false;
		dirty  = true;
		rows.add( pos, values );
		if ( pos <= cursor ) cursor++;
		values = rows.get( cursor );
	}

	public void setCursor(int pos) throws MorseException {
		if ( pos < 0 || pos >= rows.size() )
			throw new MorseException( MorseException.OUT_OF_BOUND, String.valueOf( pos ) );
		cursor = pos;
		values = rows.get( cursor );
	}

	public boolean reset() throws MorseException {
		cursor = -1;
		values = null;
		return true;
	}
	
	public boolean next() throws MorseException {
		cursor++;
		if ( cursor < rows.size() ) {
			values = rows.get( cursor );
			return true;
		}
		values = null;
		return false;
	}

	public void copyRow() throws MorseException {
		Object[][] old = values;
		values = new Object[ getAttributeCount() ][];
		for ( int i = 0; i < getAttributeCount(); i++ ) {
			IAttribute attr = getAttribute( i );
			if ( ! attr.isTable() ) {
				values[ i ] = new Object[ 2 ];
				values[ i ][ 0 ] = old[ i ][ 0 ];
				values[ i ][ 1 ] = old[ i ][ 1 ];
			}
			
		}
		create = true;
	}

	public void removeRow() throws MorseException {
		if ( create ) {
			values = rows.get( cursor );
			create =false;
			return;
		}
		rows.remove( cursor );
		cursor--;
		if ( cursor < 0 )
			cursor = 0;
		if ( cursor >= rows.size() )
			values = null;
		else
			values = rows.get( cursor );
	}

	public void removeRow(int pos) throws MorseException {
		if ( create ) return;
		rows.remove( pos );
		if ( cursor >= pos )
			cursor--;
		if ( cursor < 0 )
			cursor = 0;
		if ( cursor >= rows.size() )
			values = null;
		else
			values = rows.get( cursor );		
	}
	
	protected void cleanUp() {
		for ( Iterator<Object[][]> i = rows.iterator(); i.hasNext(); ) {
			Object[][] v = i.next();
			for ( int j = 0; j < v.length; j++ ) {
				if ( v[ j ][ 1 ] != null ) {
					//values[ i ][ 0 ] = values[ i ][ 1 ];
					v[ j ][ 1 ] = null;
				}
			}
		}
		dirty = false;
	}
}
