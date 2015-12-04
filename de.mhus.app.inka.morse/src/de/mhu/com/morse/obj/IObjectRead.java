package de.mhu.com.morse.obj;

import de.mhu.com.morse.utils.MorseException;

public interface IObjectRead extends ITableRead {

	public ITableRead getTable( String name ) throws MorseException;
	public ITableRead getTable( int index ) throws MorseException;

}
