package de.mhu.com.morse.channel;

import de.mhu.com.morse.utils.MorseException;

public interface IServer {

	public Object loadFunction( IConnection con, String name ) throws MorseException;

}
