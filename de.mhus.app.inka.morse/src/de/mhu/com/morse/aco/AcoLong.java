package de.mhu.com.morse.aco;

import java.util.Date;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class AcoLong implements IAco {

	private IAttribute attr;
	
	public boolean getBoolean(String value) throws MorseException {
		return getLong( value ) != 0;
	}

	public Date getDate(String value) throws MorseException {
		return new Date( getLong( value ) );
	}

	public double getDouble(String value) throws MorseException {
		return getLong( value );
	}

	public int getInteger(String value) throws MorseException {
		return (int)getLong( value );
	}

	public long getLong(String value) throws MorseException {
		if ( value == null ) return Long.parseLong( attr.getDefaultValue() );
		return Long.parseLong( value );
	}

	public Object getObject(String value) throws MorseException {
		return new Long( value );
	}

	public String getString(String value) throws MorseException {
		return value;
	}

	public String getRaw(String value) throws MorseException {
		return value;
	}
	
	public void init(IAttribute pAttr) throws MorseException {
		attr = pAttr;
		if ( attr.getType() != IAttribute.AT_LONG )
			throw new MorseException( MorseException.AT_NOT_COMPATIBLE, pAttr.getCanonicalName() );
	}

	public boolean validate(String value) {
		try {
			long d = Long.parseLong( value );
			if ( d == 0 && attr.isNotNull() )
				return false;
		} catch ( Exception e ) {
			return false;
		}
		return true;
	}

}
