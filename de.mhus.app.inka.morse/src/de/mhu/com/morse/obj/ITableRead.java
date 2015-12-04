package de.mhu.com.morse.obj;

import java.util.Date;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;

public interface ITableRead {

	public boolean next() throws MorseException;
	public boolean reset() throws MorseException;
	
	public String getString( String name ) throws MorseException;
	public String getString(int index) throws MorseException;
	public Object getObject(int index) throws MorseException;
	public Object getObject(String name) throws MorseException;
	public int getInteger(String name) throws MorseException;
	public int getInteger(int index ) throws MorseException;
	public long getLong(String name) throws MorseException;
	public long getLong(int index ) throws MorseException;
	public double getDouble(String name) throws MorseException;
	public double getDouble(int index ) throws MorseException;
	public Date getDate(String name) throws MorseException;
	public Date getDate(int index ) throws MorseException;
	public boolean getBoolean(int index) throws MorseException;
	public boolean getBoolean(String name) throws MorseException;
	
	public String[] getColumns() throws MorseException;
	public int getAttributeCount();
	public IAttribute getAttribute( int i ) throws MorseException;
	public IAttribute getAttribute( String name ) throws MorseException;
	public void close();
	
}
