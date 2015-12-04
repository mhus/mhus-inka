package de.mhu.com.morse.channel.idx;

import java.util.LinkedList;
import java.util.Map;

import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.idx.IdxDriver.MyChannel;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public interface IIdx {

	public void initIndex(IdxDriver idxDriver, Map<String, String> features) throws MorseException;

	public void closeIndex();

	public IQueryResult select( IConnectionServer connection, UserInformation user, LinkedList<String> names,
			LinkedList<String> attributes, LinkedList<String[]> where) throws MorseException;

	public IQueryResult rebuild( IConnectionServer connection, UserInformation user, LinkedList<String> names ) throws MorseException;

}
