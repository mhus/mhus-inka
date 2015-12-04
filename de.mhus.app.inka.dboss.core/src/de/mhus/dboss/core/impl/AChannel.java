package de.mhus.dboss.core.impl;

import de.mhus.dboss.core.IChannel;

public abstract class AChannel implements IChannel {

	private DBossServer server;

	public AChannel(DBossServer server) {
		this.server=server;
	}
	
	public DBossServer getServer() {
		return server;
	}
	
	public abstract ASessionChannel createSessionChannel();
	
	public abstract void close();

}
