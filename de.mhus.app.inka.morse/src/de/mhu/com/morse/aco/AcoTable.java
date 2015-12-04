package de.mhu.com.morse.aco;

import java.util.Date;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class AcoTable implements IAco {

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
	
	public void init(IAttribute pAttr) {
		attr = pAttr;
	}

	public boolean validate(String value) {
		if ( value == null || ( value.length() == 0 && attr.isNotNull() ) || value.length() > attr.getSize() ) return false;
		return true;
	}

}
