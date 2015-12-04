package de.mhus.dboss.core.impl;

import de.mhus.dboss.core.DBossException;
import de.mhus.dboss.core.IObject;
import de.mhus.dboss.core.IResult;
import de.mhus.dboss.core.IServer;
import de.mhus.dboss.core.ISession;

public class Session implements ISession {

	private XSessionChannelHandler channels = new XSessionChannelHandler();
	private DBossServer server;
	private boolean closed=false;

	public Session(DBossServer dBossServer) {
		server = dBossServer;
	}

	@Override
	public void abort() throws DBossException {
		channels.abort();
	}

	@Override
	public void close() {
		if (closed) return;
		closed=true;
		server.removeSession(this);
		channels.close();
		channels=null;
	}
	
	@Override
	public void finalize() {
		close();
	}

	@Override
	public void commit() throws DBossException {
		channels.commit();		
	}

	@Override
	public IObject create(String type) throws DBossException {
		return create(type,Const.DEFAULT_CHANNEL);
	}

	@Override
	public IObject create(String type, String channel) throws DBossException {
		return channels.get(channel).create(type);
	}
	
	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public IResult query(String query) throws DBossException {
		return query(query,Const.DEFAULT_CHANNEL);
	}

	@Override
	public IResult query(String query, String channel) throws DBossException {
		return channels.get(channel).query(query);
	}

}
