package de.mhu.com.morse.client;

import java.io.IOException;

import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.net.IMessage;
import de.mhu.lib.plugin.AfPluginException;

public class MSession {

	private MConnection con;
	private String id;
	private MChannelProvider dbProvider;

	public MSession(MConnection pConnection, String pId ) {
		con = pConnection;
		id = pId;
		dbProvider = new MChannelProvider( this );

	}

	public MConnection getConnection() {
		return con;
	}

	public IMessage sendAndWait(IMessage msg, int timeout) throws IOException, AfPluginException {
		if ( id != null ) {
			msg.unshift( id );
			msg.unshift( "s" );
		}
		return con.sendAndWait( msg, timeout);
	}
	
	public IChannelProvider getDbProvider() {
		return dbProvider;
	}
	

}
