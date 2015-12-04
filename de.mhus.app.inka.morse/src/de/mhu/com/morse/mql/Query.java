package de.mhu.com.morse.mql;

import de.mhu.lib.log.AL;
import de.mhu.lib.statistics.AStatistics;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.utils.MorseException;

public class Query {

	private static AL log = new AL( Query.class );
	protected static AStatistics statistics = new AStatistics( Query.class.getName() );
	
	private ICompiledQuery code;
	protected IChannel db;
	private IConnection connection;
	
	public Query( IConnection pConnection, String in ) throws MorseException {
		
		if ( log.t4() ) log.info( "MQL: " + in );
		
		connection = pConnection;
		String parts[] = QuerySplit.split( in );
		
		if ( parts.length == 0 )
			return; // ERROR
		
		int len = parts.length;
		String srcName = null;
		if ( parts[ parts.length-1 ].startsWith( "@" ) ) {
			srcName = parts[ parts.length-1 ].substring( 1 );
			len--;
		}
		
		// find src
		IChannel src = pConnection.getChannel( srcName );		
		QueryParser parser = src.getParser();
		code = parser.compile( parts, 0, len );
		db = src;
	}

	public Query(IConnection pConnection, String dbName, CompilledQueryMessage message) throws MorseException {
		connection = pConnection;
		db = pConnection.getChannel( dbName );
		code = message;
	}

	public IQueryResult execute() throws MorseException {
		statistics.dec();
		return db.query( this );
	}
	
	public void dump() {
		code.dump();		
	}

	public ICompiledQuery getCode() {
		return code;
	}
	
	public String getDbName() {
		return db.getName();
	}

	public IConnection getConnection() {
		return connection;
	}
}
