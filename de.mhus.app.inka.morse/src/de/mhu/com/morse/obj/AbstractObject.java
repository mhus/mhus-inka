package de.mhu.com.morse.obj;

import java.util.Date;

import de.mhu.lib.ACast;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public abstract class AbstractObject extends AbstractObjectRead implements IObject {

	public abstract ITable     getTable    ( String name ) throws MorseException;
	public abstract ITable     getTable    ( int index   ) throws MorseException;

	protected abstract void setRawString( String name, String value ) throws MorseException;
	protected abstract void setRawString( int index, String value ) throws MorseException;
	
	public void setString( String name, String value ) throws MorseException {
		if ( getAttribute( name ).getAco().validate( value ) )
			setRawString( name, value );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( name ).getCanonicalName(),value} );
	}
	public void setString(int index, String value) throws MorseException {
		if ( getAttribute( index ).getAco().validate( value ) )
			setRawString( index, value );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( index ).getCanonicalName(),value} );
	}
	
	public void setInteger(String name, int value ) throws MorseException {
		String v = String.valueOf( value );
		if ( getAttribute( name ).getAco().validate( v ) )
			setRawString( name, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( name ).getCanonicalName(),v} );		
	}
	
	public void setInteger(int index, int value ) throws MorseException {
		String v = String.valueOf( value );
		if ( getAttribute( index ).getAco().validate( v ) )
			setRawString( index, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( index ).getCanonicalName(),v} );		
	}
	
	public void setLong(String name, long value) throws MorseException {
		String v = String.valueOf( value );
		if ( getAttribute( name ).getAco().validate( v ) )
			setRawString( name, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( name ).getCanonicalName(),v} );		
	}
	
	public void setLong(int index, long value ) throws MorseException {
		String v = String.valueOf( value );
		if ( getAttribute( index ).getAco().validate( v ) )
			setRawString( index, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( index ).getCanonicalName(),v} );
	}
	
	public void setDouble(String name, double value) throws MorseException {
		String v = String.valueOf( value );
		if ( getAttribute( name ).getAco().validate( v ) )
			setRawString( name, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( name ).getCanonicalName(),v} );
	}
	public void setDouble(int index, double value ) throws MorseException {
		String v = String.valueOf( value );
		if ( getAttribute( index ).getAco().validate( v ) )
			setRawString( index, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( index ).getCanonicalName(),v} );
	}
	
	public void setDate(String name, Date value) throws MorseException {
		String v = ACast.toString( value );
		if ( getAttribute( name ).getAco().validate( v ) )
			setRawString( name, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( name ).getCanonicalName(),v} );
	}
	public void setDate(int index, Date value ) throws MorseException {
		String v = ACast.toString( value );
		if ( getAttribute( index ).getAco().validate( v ) )
			setRawString( index, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( index ).getCanonicalName(),v} );		
	}
	
	public void setBoolean(int index, boolean value) throws MorseException {
		String v = value ? "1" : "0";
		if ( getAttribute( index ).getAco().validate( v ) )
			setRawString( index, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( index ).getCanonicalName(),v} );		
	}

	public void setBoolean(String name, boolean value) throws MorseException {
		String v = value ? "1" : "0";
		if ( getAttribute( name ).getAco().validate( v ) )
			setRawString( name, v );
		else
			throw new MorseException( MorseException.ATTR_VALUE_NOT_VALIDE, new String[] {getAttribute( name ).getCanonicalName(),v} );
	}
	
}
