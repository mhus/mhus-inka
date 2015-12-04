package de.mhu.com.morse.usr;

import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.net.IMessage;

public interface IQueue {

	public void close();

	public IMessage next( IMessage msg, Weak weak );

	public boolean reset();

}
