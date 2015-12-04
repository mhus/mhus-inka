package de.mhus.dboss.core.impl;

import de.mhus.dboss.core.DBossException;

public class XChannelHandler extends MMap<String,AChannel> {

	public void put(AChannel channel) throws DBossException {
		synchronized (this) {
			if (containsKey(channel.getName()))
				throw new DBossException("already contains channel: "+channel.getName());
			put(channel.getName(),channel);
		}
	}
	
	public void close() {
		for ( AChannel channel : values() )
			channel.close();
	}

}
