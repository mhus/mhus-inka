package de.mhu.com.morse.obj;

import java.util.Date;

import de.mhu.com.morse.utils.MorseException;

public interface ITable extends ITableRead,ITableWrite {

	public int getSize();
	public void setCursor( int pos ) throws MorseException;
	public int getCursor();
	
	public void createRow() throws MorseException;
	public void appendRow() throws MorseException;
	public void insertRow( int pos ) throws MorseException;
	// public void copyRow() throws MorseException;
	public void removeRow() throws MorseException;
	public void removeRow( int pos ) throws MorseException;
	
}
