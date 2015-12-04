package de.mhu.com.morse.aco;

import java.util.Date;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class AcoDouble implements IAco {

	private IAttribute attr;
	
	public boolean getBoolean(String value) throws MorseException {
		return getDouble( value ) != 0;
	}

	public Date getDate(String value) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public double getDouble(String value) throws MorseException {
		return Double.parseDouble( value );
	}

	public int getInteger(String value) throws MorseException {
		return (int)getDouble(value);
	}

	public long getLong(String value) throws MorseException {
		return (long)getDouble(value);
	}

	public Object getObject(String value) throws MorseException {
		return new Double( value );
	}

	public String getString(String value) throws MorseException {
		return value;
	}

	public String getRaw(String value) throws MorseException {
		return value;
	}
	
	public void init(IAttribute pAttr) throws MorseException {
		attr = pAttr;
		if ( attr.getType() != IAttribute.AT_DOUBLE )
			throw new MorseException( MorseException.AT_NOT_COMPATIBLE, pAttr.getCanonicalName() );
	}


	public boolean validate(String value) {
		try {
			double d = Double.parseDouble( value );
			if ( d == 0 && attr.isNotNull() )
				return false;
		} catch ( Exception e ) {
			return false;
		}
		return true;
	}

}
