package de.mhus.dboss.core.impl;

import de.mhus.dboss.core.DBossException;
import de.mhus.lib.MEventHandler;

public class XSessionChannelHandler extends MMap<String,ASessionChannel> {

	public void put(ASessionChannel channel) throws DBossException {
		synchronized (this) {
			put(channel.getChannel().getName(),channel);
		}
	}
	
	public void close() {
		for ( ASessionChannel channel : values() )
			channel.close();
	}

	public void abort() {
		for ( ASessionChannel channel : values() )
			channel.abort();
	}

	public void commit() {
		for ( ASessionChannel channel : values() )
			channel.commit();
	}


}
