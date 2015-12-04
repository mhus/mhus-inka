package de.mhu.com.morse.cmd.ifc;

import de.mhu.com.morse.net.IMessage;
import de.mhu.lib.plugin.IAfPpi;

public interface IMessageDelegator extends IAfPpi {

	public void doAction(IMessage msg, Weak weak );
	public void registerCommand(Command com);
	public void unregisterCommand(String command);
	
}
