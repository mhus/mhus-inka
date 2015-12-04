package de.mhu.com.morse.channel;

import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public interface IConnectionServer extends IConnection, IObjectListener {

	IChannelServer getChannel(String srcName) throws MorseException;
	
	public ITransaction startTransaction() throws MorseException;
	public void maybeCommit( ITransaction tr ) throws MorseException;
	public void maybeRollback( ITransaction tr ) throws MorseException;
	public void stopTransaction(ITransaction tr) throws MorseException;

	/**
	 * Fetch the specified Object. The store is automatically located via
	 * ObjectManager.
	 * 
	 * @param id ObjectId of the searched object.
	 * @param user User how wants to have the object
	 * @param stamp If you only need the stamp say true (faster then get all data)
	 * @return The Object in a result set or an errorCode.
	 * @throws MorseException
	 */
	public IQueryResult fetch(String id, UserInformation user, boolean stamp ) throws MorseException;
	
	public IObjectManager getObjectManager();

	public boolean lock(String id, UserInformation user ) throws MorseException;

	public void unlock(String id, boolean force,UserInformation user ) throws MorseException;
	
}
