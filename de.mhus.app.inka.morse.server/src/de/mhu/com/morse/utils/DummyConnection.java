package de.mhu.com.morse.utils;

import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IServer;
import de.mhu.com.morse.channel.Server;

public class DummyConnection implements IConnection {

	private IChannel channel;
	private IServer server = new Server();

	public DummyConnection( IChannel channel ) {
		this.channel = channel;
	}
	
	public void close() {
	}

	public IChannel getChannel(String srcName) {
		return channel;
	}

	public void commit() {
		
	}

	public boolean isAutoCommit() {
		return true;
	}

	public void rollback() {
		
	}

	public void setAutoCommit(boolean in) {
		
	}

	public IServer getServer() {
		return server;
	}

}
