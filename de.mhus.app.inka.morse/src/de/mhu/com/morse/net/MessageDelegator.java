package de.mhu.com.morse.net;

import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class MessageDelegator extends AfPlugin {

	private MessageDelegatorImpl delegator;

	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	protected void apDisable() throws AfPluginException {
		// TODO Auto-generated method stub
		
	}

	protected void apEnable() throws AfPluginException {
		// TODO Auto-generated method stub
		
	}

	protected void apInit() throws Exception {

		delegator = new MessageDelegatorImpl();
		appendPpi( IMessageDelegator.class, delegator );
		
	}

	/**
	 * @return the delegator
	 */
	public IMessageDelegator getDelegator() {
		return delegator;
	}

}
