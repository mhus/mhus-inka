package de.mhus.dboss.core.examples;

import de.mhus.dboss.core.DBossException;
import de.mhus.dboss.core.FDBoss;
import de.mhus.dboss.core.IServer;
import de.mhus.dboss.core.IServerConfig;
import de.mhus.dboss.core.SimpleServerConfig;

public class E01CreateServer {

	
	public static void main(String[] args) throws DBossException {
	
		IServerConfig config = new SimpleServerConfig();
		IServer server = FDBoss.createServer(config);
		
		
		
		server.close();
	}
	
}
