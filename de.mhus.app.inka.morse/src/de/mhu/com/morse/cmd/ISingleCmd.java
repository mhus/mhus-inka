package de.mhu.com.morse.cmd;

import java.io.IOException;

import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.IAfPpi;

public interface ISingleCmd extends IAfPpi {

	public IMessage sendAndWait( IMessage msg, long timeout ) throws IOException, MorseException;

}
