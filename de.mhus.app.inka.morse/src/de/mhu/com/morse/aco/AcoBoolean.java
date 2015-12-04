package de.mhu.com.morse.aco;

import java.util.Date;

import de.mhu.lib.ACast;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class AcoBoolean implements IAco {

	private IAttribute attr;
	
	public boolean getBoolean(String value) throws MorseException {
		return ACast.toboolean( value, false );
	}

	public Date getDate(String value) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public double getDouble(String value) throws MorseException {
		return getBoolean(value) ? 1 : 0;
	}

	public int getInteger(String value) throws MorseException {
		return getBoolean(value) ? 1 : 0;
	}

	public long getLong(String value) throws MorseException {
		return getBoolean(value) ? 1 : 0;
	}

	public Object getObject(String value) throws MorseException {
		return new Boolean( getBoolean(value) );
	}

	public String getString(String value) throws MorseException {
		return getBoolean(value) ? "T" : "F";
	}
	
	public String getRaw(String value) throws MorseException {
		return getBoolean(value) ? "1" : "0";
	}

	public void init(IAttribute pAttr) throws MorseException {
		attr = pAttr;
		if ( attr.getType() != IAttribute.AT_BOOLEAN )
			throw new MorseException( MorseException.AT_NOT_COMPATIBLE, pAttr.getCanonicalName() );
	}


	public boolean validate(String value) {
			return ACast.toboolean( value, true ) == ACast.toboolean( value, false );
	}

}
