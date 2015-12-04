package de.mhu.com.morse.obj;

import java.util.Date;

import de.mhu.com.morse.utils.MorseException;

public interface ITableWrite {

	public void setString( String name, String value ) throws MorseException;
	public void setString(int index, String value) throws MorseException;
	public void setInteger(String name, int value ) throws MorseException;
	public void setInteger(int index, int value ) throws MorseException;
	public void setLong(String name, long value) throws MorseException;
	public void setLong(int index, long value ) throws MorseException;
	public void setDouble(String name, double value) throws MorseException;
	public void setDouble(int index, double value ) throws MorseException;
	public void setDate(String name, Date value) throws MorseException;
	public void setDate(int index, Date value ) throws MorseException;
	public void setBoolean(int index, boolean value) throws MorseException;
	public void setBoolean(String name, boolean value) throws MorseException;
	
	public boolean isNew();
	public boolean isDirty();
	public boolean isDirty( int index ) throws MorseException;
	public boolean isDirty( String name ) throws MorseException;
	
}
