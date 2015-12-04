package de.mhu.com.morse.channel;

import de.mhu.com.morse.utils.MorseException;

/**
 * A connection represents a bundle of all channels. If you work on
 * the connection you work on all channels in the same time. A commit will
 * fire a commit to every channel, rollback in the same way. Attention: not every
 * channel supports transactions.
 * 
 * @see IChannelProvider
 * @see IChannel
 * @author mike
 *
 */

public interface IConnection {

	public void close();

	IChannel getChannel(String srcName) throws MorseException;
	
	public boolean isAutoCommit();
	public void setAutoCommit( boolean in );
	public void rollback() throws MorseException;
	public void commit() throws MorseException;

	public IServer getServer();
	
}
