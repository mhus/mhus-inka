package de.mhus.dboss.core;

public interface ISession {

	void commit() throws DBossException;
	void abort() throws DBossException;
	
	IObject create(String type) throws DBossException;
	IObject create(String type, String channel) throws DBossException;

	IResult query(String query) throws DBossException;
	IResult query(String query, String channel) throws DBossException;
	
	IServer getServer();
	
	void close();
	
}
