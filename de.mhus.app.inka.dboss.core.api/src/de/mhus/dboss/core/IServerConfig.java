package de.mhus.dboss.core;

public interface IServerConfig {

	String getServerClass();

	String getInitialChannelClass();

	ILoader getClassLoader();

}
