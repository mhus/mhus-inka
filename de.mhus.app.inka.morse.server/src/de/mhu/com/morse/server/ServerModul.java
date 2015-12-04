package de.mhu.com.morse.server;

import de.mhu.com.morse.cmd.BaseCmd;
import de.mhu.com.morse.cmd.LinCmd;
import de.mhu.com.morse.cmd.QueryCmd;
import de.mhu.com.morse.cmd.QueueCmd;
import de.mhu.com.morse.cmd.SysCmd;
import de.mhu.com.morse.cmd.TestCmd;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.net.MessageDelegator;
import de.mhu.com.morse.net.TcpListener;
import de.mhu.com.morse.net.ThreadEventArray;
import de.mhu.lib.plugin.AfModule;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class ServerModul extends AfModule {

	private IMessageDelegator cmdDelegator;

	public ServerModul(IMessageDelegator pCmdDelegator) {
		cmdDelegator = pCmdDelegator;
	}

	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub

	}

	protected void apDisable() throws AfPluginException {
		// TODO Auto-generated method stub

	}

	protected void apEnable() throws AfPluginException {
		getApParent().enablePlugin( "listener" );
	}

	protected void apInit() throws Exception {

		MessageDelegator md = new MessageDelegator();
		getApParent().addPlugin( md, "md" );
		getApParent().enablePlugin( "md" );
		
		ThreadEventArray tea = new ThreadEventArray();
		getApParent().addPlugin( tea, "tea" );
		
		TcpListener listener = new TcpListener();
		getApParent().addPlugin(listener, "listener" );
		
		getApParent().enablePlugin( "tea" );
		
		TestCmd testCmds = new TestCmd();
		getApParent().addPlugin( testCmds, "test" );
		getApParent().enablePlugin( "test" );
		
		SysCmd sysCmds = new SysCmd();
		getApParent().addPlugin( sysCmds, "sys" );
		getApParent().enablePlugin( "sys" );
		
		BaseCmd baseCmds = new BaseCmd();
		getApParent().addPlugin( baseCmds, "base" );
		getApParent().enablePlugin( "base" );		

		LinCmd linCmds = new LinCmd( cmdDelegator );
		getApParent().addPlugin( linCmds, "lin" );
		getApParent().enablePlugin( "lin" );		
		
		
	}

}
