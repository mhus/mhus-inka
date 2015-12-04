package de.mhus.dboss.core;

public class FDBoss {

	public static IServer createServer(IServerConfig config) throws DBossException {
		
		ILoader loader = config.getClassLoader();
		
		IServer server = (IServer)loader.newInstance(config.getServerClass(),config);
		return server;
		
	}
	
}
