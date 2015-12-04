package de.mhu.com.morse.cmd.ifc;

import de.mhu.com.morse.net.MessageDelegatorImpl;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class MdPlugin extends AfPlugin {

	private String name;
	private MessageDelegatorImpl md;

	public MdPlugin( String pName ) {
		name = pName;
	}
	
	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

	protected void apDisable() throws AfPluginException {
		removePpi( IMessageDelegator.class , md );
	}

	protected void apEnable() throws AfPluginException {
		// TODO Auto-generated method stub
		
	}

	protected void apInit() throws Exception {
		md = new MessageDelegatorImpl( name );
		appendPpi( IMessageDelegator.class , md );
	}

}
