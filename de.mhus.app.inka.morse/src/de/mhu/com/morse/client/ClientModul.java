package de.mhu.com.morse.client;

import de.mhu.com.morse.cmd.ISingleCmd;
import de.mhu.com.morse.cmd.SingleCmd;
import de.mhu.com.morse.cmd.SysCmd;
import de.mhu.com.morse.net.MessageDelegator;
import de.mhu.com.morse.net.ThreadEventArray;
import de.mhu.lib.plugin.AfModule;
import de.mhu.lib.plugin.AfPluginException;

public class ClientModul extends AfModule {

	private ISingleCmd single;

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

		MessageDelegator md = new MessageDelegator();
		getApParent().addPlugin( md, "md" );
		getApParent().enablePlugin( "md" );
		
		ThreadEventArray tea = new ThreadEventArray();
		getApParent().addPlugin( tea, "tea" );
		getApParent().enablePlugin( "tea" );

		SysCmd sysCmds = new SysCmd();
		getApParent().addPlugin( sysCmds, "sys" );
		getApParent().enablePlugin( "sys" );
		
		SingleCmd singleCmd = new SingleCmd();
		getApParent().addPlugin( singleCmd, "single" );
		getApParent().enablePlugin( "single" );
				
		single = singleCmd;
	}

	/**
	 * @return the single
	 */
	public ISingleCmd getSingle() {
		return single;
	}

}
