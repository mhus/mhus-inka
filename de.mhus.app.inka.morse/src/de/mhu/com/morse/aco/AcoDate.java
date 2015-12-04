package de.mhu.com.morse.aco;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.mhu.lib.ACast;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class AcoDate implements IAco {

	private IAttribute attr;
	// private SimpleDateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss.SSS" );
	
	public boolean getBoolean(String value) throws MorseException {
		return value != null && value.length() != 0;
	}

	public Date getDate(String value) throws MorseException {
		return ACast.toDate( value );
	}

	public double getDouble(String value) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public int getInteger(String value) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public long getLong(String value) throws MorseException {
		return getDate( value ).getTime();
	}

	public Object getObject(String value) throws MorseException {
		return getDate(value);
	}

	public String getString(String value) throws MorseException {
		/*
		synchronized ( dateFormat ) {
			return dateFormat.format( getDate( value ) );
		}
		*/
		return ACast.toString( getDate( value ) );		
	}

	public String getRaw(String value) throws MorseException {
		/*
		synchronized ( dateFormat ) {
			return dateFormat.format( getDate( value ) );
		}
		*/
		return ACast.toString( getDate( value ) );
	}
	
	public void init(IAttribute pAttr) throws MorseException {
		attr = pAttr;
		if ( attr.getType() != IAttribute.AT_DATE )
			throw new MorseException( MorseException.AT_NOT_COMPATIBLE, pAttr.getCanonicalName() );
	}


	public boolean validate(String value) {
		
		if ( ACast.toDate( value ).getTime() == 0 && attr.isNotNull() ) return false;
		return true;
	}

}
