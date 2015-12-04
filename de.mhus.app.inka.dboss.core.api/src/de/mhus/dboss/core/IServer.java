package de.mhus.dboss.core;

import java.util.Set;

public interface IServer {

	Set<String> getTypeNames();
	
	IType getType(String name);
	
	Set<String> getChannelNames();
	
	IChannel getChannel(String name);
	
	ISession createSession();
	
	IAdministration getAdministration();
	
	void close();

}
