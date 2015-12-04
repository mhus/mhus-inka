package de.mhu.com.morse.channel;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.IAfPpi;

/**
 * The channel provider allows the system to create
 * new connections. A connection consists of a bundle of
 * channels. You can also use the defoult connection.
 * This is for reading of if no other connection is available 
 * at the moment. Think about it: Creating a connection can
 * be expensive if you need a lot of channels (channels should
 * only be instanciated if first time needed).
 * 
 * @see IConnection
 * @author mike
 *
 */
public interface IChannelProvider extends IAfPpi {

	IConnection createConnection() throws MorseException;

	IConnection getDefaultConnection();

}
