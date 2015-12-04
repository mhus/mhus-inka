package de.mhu.com.morse.server;

import de.mhu.com.morse.cmd.TypesCmd;
import de.mhu.com.morse.cmd.BaseCmd;
import de.mhu.com.morse.cmd.QueryCmd;
import de.mhu.com.morse.cmd.QueueCmd;
import de.mhu.com.morse.cmd.SysCmd;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.net.MessageDelegator;
import de.mhu.lib.plugin.AfModule;

public class CmdsModule extends AfModule {

	private MessageDelegator md;

	protected void apInit() throws Exception {

		md = new MessageDelegator();
		getApParent().addPlugin( md, "md" );
		getApParent().enablePlugin( "md" );

		SysCmd sysCmds = new SysCmd();
		getApParent().addPlugin( sysCmds, "sys" );
		getApParent().enablePlugin( "sys" );
		
		BaseCmd baseCmds = new BaseCmd();
		getApParent().addPlugin( baseCmds, "base" );
		getApParent().enablePlugin( "base" );		

		QueryCmd queryCmd = new QueryCmd();
		getApParent().addPlugin( queryCmd, "qry" );
		getApParent().enablePlugin( "qry" );
		
		QueueCmd queueCmd = new QueueCmd();
		getApParent().addPlugin( queueCmd, "queue" );
		getApParent().enablePlugin( "queue" );
		
		TypesCmd attrCmd = new TypesCmd();
		getApParent().addPlugin( attrCmd, "attr" );
		getApParent().enablePlugin( "attr" );
		

	}

	public IMessageDelegator getDelegator() {
		return md.getDelegator();
	}
}
