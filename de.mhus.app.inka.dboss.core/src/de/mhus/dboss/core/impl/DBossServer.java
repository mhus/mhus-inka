package de.mhus.dboss.core.impl;

import java.util.Set;

import de.mhus.dboss.core.DBossException;
import de.mhus.dboss.core.IAdministration;
import de.mhus.dboss.core.IChannel;
import de.mhus.dboss.core.ILoader;
import de.mhus.dboss.core.IServer;
import de.mhus.dboss.core.IServerConfig;
import de.mhus.dboss.core.ISession;
import de.mhus.dboss.core.IType;
import de.mhus.lib.MEventHandler;

public class DBossServer implements IServer {

	private IServerConfig   config;
	private XChannelHandler channels = new XChannelHandler();
	private XTypeHandler    types = new XTypeHandler();
	private XSessionHandler sessions = new XSessionHandler();
	
	public DBossServer(IServerConfig config) throws DBossException {
		this.config=config;
		reloadMeta();
	}
	
	public IServerConfig getConfig() {
		return config;
	}
	
	public void reloadMeta() throws DBossException {
		
		channels.close();
		channels.clear();
		
		types.clear();
		
		ILoader loader = config.getClassLoader();
		
		AChannel init = (AChannel)loader.newInstance(config.getInitialChannelClass(),this);
		channels.put(Const.INIT_CHANNEL,init);
		
		//TODO
	
	}
	
	
	@Override
	public void close() {
		channels.close();
		config=null;
		channels=null;
		types=null;
	}

	@Override
	public ISession createSession() {
		Session session = new Session(this);
		sessions.registerWeak(session);
		return null;
	}

	@Override
	public IType getType(String name) {
		return types.get(name);
	}

	@Override
	public Set<String> getTypeNames() {
		return types.keySet();
	}

	@Override
	public IChannel getChannel(String name) {
		return channels.get(name);
	}

	@Override
	public Set<String> getChannelNames() {
		return channels.keySet();
	}

	public void removeSession(Session session) {
		sessions.unregister(session);
	}

	@Override
	public IAdministration getAdministration() {
		// TODO Auto-generated method stub
		return null;
	}

}
