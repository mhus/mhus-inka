package de.mhus.dboss.core;

public class SimpleServerConfig implements IServerConfig {

	ILoader classLoader = new SimpleClassLoader();
	
	@Override
	public String getServerClass() {
		return "de.mhus.dboss.core.impl.DBossServer";
	}

	@Override
	public String getInitialChannelClass() {
		return "de.mhus.dboss.core.impl.SimpleInitialChannel";
	}

	@Override
	public ILoader getClassLoader() {
		return classLoader;
	}

}
