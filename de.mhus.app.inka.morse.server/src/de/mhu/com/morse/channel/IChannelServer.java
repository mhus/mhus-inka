package de.mhu.com.morse.channel;

import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.obj.IObjectRead;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public interface IChannelServer extends IChannel {

	public IConnectionServer getConnection();

	public IQueryResult query( Query in, UserInformation user ) throws MorseException;
	public IQueryResult fetch( String id, UserInformation user, boolean stamp ) throws MorseException;
	public void store( IObjectRead obj, boolean commit, UserInformation user ) throws MorseException;
	public byte[] getDefinition();
	
	public void rollback();
	public void commit();

	public boolean lock(String id, UserInformation user) throws MorseException;

	public void unlock(String id, boolean force, UserInformation user) throws MorseException;

	public void setAutoCommit(boolean b);
	
}
