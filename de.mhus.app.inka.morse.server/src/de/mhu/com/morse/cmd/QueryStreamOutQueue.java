package de.mhu.com.morse.cmd;

import java.io.IOException;
import java.io.InputStream;

import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.usr.IQueue;
import de.mhu.com.morse.utils.MorseException;

public class QueryStreamOutQueue implements IQueue {

	private static AL log = new AL( QueryStreamOutQueue.class );
	private IQueryResult result;
	private InputStream stream;
	private byte[] buffer = new byte[ ConfigManager.getConfig( "server" ).getProperty( "queue.stream.in.buffer.size", 1024 ) ];

	public QueryStreamOutQueue(IQueryResult res) throws MorseException {
		result = res;
		stream = res.getInputStream();
	}

	public void close() {
		if ( result == null ) return;
		result.close();
		result = null;
	}

	public IMessage next( IMessage msg, Weak weak) {
		try {
			int size = stream.read( buffer );
			if ( size < 0 ) return null;
			IMessage res = weak.getClient().createMessage();
			res.append( size );
			res.append( buffer );
			return res;
		} catch (IOException e) {
			log.error( e );
		}
		return null;
	}

	public boolean reset() {
		return false;
	}

}
