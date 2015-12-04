package de.mhu.com.morse.mql;

import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public class ServerQuery extends Query {

	public ServerQuery(IConnection pConnection, String dbName, CompilledQueryMessage message) throws MorseException {
		super(pConnection, dbName, message);
	}

	public ServerQuery(IConnection pConnection, String in) throws MorseException {
		super(pConnection, in);
	}

	public IQueryResult execute( UserInformation user ) throws MorseException {
		statistics.dec();
		if ( db instanceof IChannelServer )
			return ((IChannelServer)db).query( this, user );
		throw new MorseException( MorseException.ACCESS_DENIED, "no server channel" );
			
	}
}
