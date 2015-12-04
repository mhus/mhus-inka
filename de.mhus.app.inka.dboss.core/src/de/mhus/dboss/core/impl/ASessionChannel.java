package de.mhus.dboss.core.impl;

import de.mhus.dboss.core.IObject;
import de.mhus.dboss.core.IResult;

public abstract class ASessionChannel {

	private AChannel channel;

	public ASessionChannel(AChannel channel) {
		this.channel=channel;
	}
	
	public AChannel getChannel() {
		return channel;
	}

	public abstract void close();

	public abstract void abort();

	public IObject create(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	public abstract void commit();

	public abstract IResult query(String query);

}
