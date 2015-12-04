package de.mhu.com.morse.cmd;

import java.io.IOException;
import java.io.OutputStream;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.usr.IQueue;
import de.mhu.com.morse.utils.MorseException;

public class QueryStreamInQueue implements IQueue {

	private static AL log = new AL( QueryStreamInQueue.class );
	private OutputStream stream;
	private IQueryResult result;

	public QueryStreamInQueue(IQueryResult res) throws MorseException {
		result = res;
		stream = res.getOutputStream();
	}

	public void close() {
		if ( result == null ) return;
		result.close();
		result = null;
	}

	public IMessage next( IMessage msg, Weak weak) {
		if ( result == null ) return null;
		try {
			msg.write( 0, stream );
		} catch (IOException e) {
			log.error( e );
			return null;
		}
		IMessage res = weak.getClient().createMessage();
		res.append( (byte)1 );
		return res;
	}

	public boolean reset() {
		return false;
	}

}
