package de.mhu.com.morse.channel;

import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.mql.QueryParser;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.IAfPpi;

/**
 * A channel is somthing you can send queries and you also get
 * results. The function of the channel is not compleately
 * defined. It can be a data store (db,fs,log,archive) a index manager or
 * also a command base (exec ...). (or a random generator, a streaming server,
 * a connection to the rest of the world) ...... 42
 *  
 * @author mike
 *
 */
public interface IChannel extends IAfPpi {

	public QueryParser getParser();
	public String getName();
	public IQueryResult query( Query in ) throws MorseException;
	public void close();
	public IConnection getConnection();

}
