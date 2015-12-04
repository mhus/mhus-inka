package de.mhu.com.morse.channel;

import java.util.Iterator;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public interface IQueryFunction {

	public IAttribute getType();

	public String getResult();

	public void initFunction(IConnectionServer con, IAclManager aclm, UserInformation user, String[] functionInit) throws MorseException;

	public void resetFunction();

}
