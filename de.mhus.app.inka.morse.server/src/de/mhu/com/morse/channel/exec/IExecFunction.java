package de.mhu.com.morse.channel.exec;

import java.util.LinkedList;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.IFunctionConfig;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public interface IExecFunction extends IFunctionConfig {

	public void initFunction(IConnectionServer connection, IAclManager aclManager, UserInformation user) throws MorseException;

	public IQueryResult exec(LinkedList<Object> attr, boolean async) throws MorseException;

}
