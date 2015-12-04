package de.mhu.com.morse.cmd;

import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.utils.MorseException;

public class TimeoutException extends MorseException {

	public TimeoutException(IMessage msg) {
		super( MorseException.CLIENT_TIMEOUT );
	}

}
