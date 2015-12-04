package de.mhu.com.morse.channel.sql;

import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;

public class Table {

	public boolean isShort;
	public String tableName;
	public IAttribute attr;
	public IType type;
	public String alias;
	public String name;
	public String internalAcl;
	public String internalId;
	public Table depTable;
	public String mqlAlias;
	public String[] hints;
	public int hintSize = 0;
	public Btc hintObject;
	
	public void addHint(String string) throws MorseException {
		if ( hints == null )
			hints = new String[ 3 ];
		if ( hintSize >= hints.length )
			throw new MorseException( MorseException.TO_MUTCH_TABLE_HINTS, tableName );
		hints[ hintSize ] = string;
		hintSize++;
	}
	
}
