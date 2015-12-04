package de.mhu.com.morse.aco;

import java.util.Date;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class AcoEnum implements IAco {

	private IAttribute attr;
	private String[] values;
	
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
		for ( int i = 0; i < values.length; i++ )
			if ( values[i].equals( value ) ) return i;
		try {
			int i = Integer.parseInt( value );
			if ( i >= 0 && i < values.length )
				return i;
		} catch ( NumberFormatException e ) {}
		try {
			int i = Integer.parseInt( attr.getDefaultValue() );
			if ( i >= 0 && i < values.length )
				return i;
		} catch ( NumberFormatException e ) {}
		return 0;
	}

	public long getLong(String value) throws MorseException {
		return getInteger(value);
	}

	public Object getObject(String value) throws MorseException {
		return getString(value);
	}

	public String getString(String value) throws MorseException {
		return values[ getInteger( value ) ];
	}

	public String getRaw(String value) throws MorseException {
		return String.valueOf( getInteger( value ) );
	}
	
	public void init(IAttribute pAttr) throws MorseException {
		attr = pAttr;
		if ( attr.getType() != IAttribute.AT_INT )
			throw new MorseException( MorseException.AT_NOT_COMPATIBLE, pAttr.getCanonicalName() );
		values = pAttr.getExtraValue().split( "," );
	}


	public boolean validate(String value) {
		if ( value == null || value.length() == 0 ) return false;
		for ( int i = 0; i < values.length; i++ )
			if ( values[i].equals( value ) ) return true;
		try {
			int i = Integer.parseInt( value );
			if ( i >= 0 && i < values.length )
				return true;
		} catch ( NumberFormatException e ) {}
		return false;
	}

}
