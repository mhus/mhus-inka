package de.mhu.com.morse.channel.sql;

import java.util.Hashtable;
import java.util.LinkedList;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;

public class Descriptor {

	public Attr[] attrs = new Attr[ 30 ];
	public int    attrSize = 0;
	public Table[] tables = new Table[ 10 ];
	public int     tableSize = 0;
	
	public Attr[] attrsInt = new Attr[ 10 ];
	public int    attrSizeInt = 0;
	public Table[] tablesInt = new Table[ 10 ];
	public int     tableSizeInt = 0;
	public Hashtable<String, Object[]> attrMap = new Hashtable<String, Object[]>();
	public String offset = null;
	public String limit = null;
	public LinkedList<String> tmpTables;
	
	public void addAttr( Attr in ) throws MorseException {
		if ( attrSize > attrs.length ) throw new MorseException( MorseException.TO_MUTCH_ATTRIBUTES );
		attrs[ attrSize ] = in;
		attrSize++;
	}
	
	public void addTable( Table in ) throws MorseException {
		if ( tableSize > tables.length ) throw new MorseException( MorseException.TO_MUTCH_TABLES );
		tables[ tableSize ] = in;
		tableSize++;
	}

	public void addInternalAttr(Attr in) throws MorseException {
		if ( attrSizeInt > attrsInt.length ) throw new MorseException( MorseException.TO_MUTCH_ATTRIBUTES );
		attrsInt[ attrSizeInt ] = in;
		attrSizeInt++;
	}
	
	public void addInternalTable( Table in ) throws MorseException {
		if ( tableSizeInt > tablesInt.length ) throw new MorseException( MorseException.TO_MUTCH_TABLES );
		tablesInt[ tableSizeInt ] = in;
		tableSizeInt++;
	}

	public void addTmpTable(String tmpName) {
		if ( tmpTables == null )
			tmpTables = new LinkedList<String>();
		tmpTables.add( tmpName );
	}
	
}
