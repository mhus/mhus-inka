package de.mhu.com.morse.aco;

import java.util.Date;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class AcoString implements IAco {

	private IAttribute attr;
	public boolean getBoolean(String value) throws MorseException {
		return value != null && value.length() != 0;
	}

	public Date getDate(String value) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public double getDouble(String value) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public int getInteger(String value) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public long getLong(String value) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public Object getObject(String value) throws MorseException {
		return value;
	}

	public String getString(String value) throws MorseException {
		return value;
	}

	public String getRaw(String value) throws MorseException {
		return value;
	}
	
	public void init(IAttribute pAttr) throws MorseException {
		attr = pAttr;
		if ( attr.getType() != IAttribute.AT_STRING )
			throw new MorseException( MorseException.AT_NOT_COMPATIBLE, pAttr.getCanonicalName() );
	}

	public boolean validate(String value) {
		if ( value == null || ( value.length() == 0 && attr.isNotNull() ) || value.length() > attr.getSize() ) return false;
		return true;
	}

}
