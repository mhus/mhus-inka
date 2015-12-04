package de.mhu.com.morse.obj;

import java.util.Date;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;

public interface IObject extends IObjectRead, ITableWrite {

	public ITable getTable( String name ) throws MorseException;
	public ITable getTable( int index ) throws MorseException;
	
	public IType getType();
	
}
