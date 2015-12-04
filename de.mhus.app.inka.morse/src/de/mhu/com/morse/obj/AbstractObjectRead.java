package de.mhu.com.morse.obj;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;

public abstract class AbstractObjectRead implements IObjectRead {

	public abstract String     getRawString( String name ) throws MorseException;
	public abstract String     getRawString( int index   ) throws MorseException;
	public abstract IAttribute getAttribute( String name ) throws MorseException;
	public abstract IAttribute getAttribute( int index   ) throws MorseException;
	public abstract boolean    next()                      throws MorseException;
	public abstract ITableRead     getTable    ( String name ) throws MorseException;
	public abstract ITableRead     getTable    ( int index   ) throws MorseException;
	public abstract void       close();
	public abstract int        getAttributeCount();
	public abstract String[]   getColumns()                throws MorseException;
	
	public boolean getBoolean(int index) throws MorseException {
		return getAttribute( index ).getAco().getBoolean( getRawString( index ) );
	}

	public boolean getBoolean(String name) throws MorseException {
		return getAttribute( name ).getAco().getBoolean( getRawString( name ) );
	}

	public Date getDate(String name) throws MorseException {
		return getAttribute( name ).getAco().getDate( getRawString( name ) );
	}

	public Date getDate(int index) throws MorseException {
		return getAttribute( index ).getAco().getDate( getRawString( index ) );
	}

	public Object getObject(String name) throws MorseException {
		return getAttribute( name ).getAco().getObject( getRawString( name ) );
	}

	public Object getObject(int index) throws MorseException {
		return getAttribute( index ).getAco().getObject( getRawString( index ) );
	}

	public double getDouble(String name) throws MorseException {
		return getAttribute( name ).getAco().getDouble( getRawString( name ) );
	}

	public double getDouble(int index) throws MorseException {
		return getAttribute( index ).getAco().getDouble( getRawString( index ) );
	}

	public int getInteger(String name) throws MorseException {
		return getAttribute( name ).getAco().getInteger( getRawString( name ) );
	}

	public int getInteger(int index) throws MorseException {
		return getAttribute( index ).getAco().getInteger( getRawString( index ) );
	}

	public long getLong(String name) throws MorseException {
		return getAttribute( name ).getAco().getLong( getRawString( name ) );
	}

	public long getLong(int index) throws MorseException {
		return getAttribute( index ).getAco().getLong( getRawString( index ) );
	}

	public String getString(String name) throws MorseException {
		return getAttribute( name ).getAco().getString( getRawString( name ) );
	}

	public String getString(int index) throws MorseException {
		return getAttribute( index )
			.getAco()
			.getString( getRawString( index ) );
	}
	
}
