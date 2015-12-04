package de.mhu.com.morse.channel;

import java.io.FileNotFoundException;

import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.IAfPpi;

public interface IObjectManager extends IAfPpi, IObjectListener {

	public String newObjectId( IType type, IChannelDriver driver );
	public String findObject( String id ) throws MorseException;
	
	public void registerObjectListener( IObjectListener listener );
	public void unregisterObjectListener( IObjectListener listener );
	
	public void lock( String id, String name, long timeout ) throws MorseException;
	public void unlock( String id, String name ) throws MorseException;
	
}
