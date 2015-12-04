package de.mhu.com.morse.server;

import de.mhu.com.morse.aaa.AclManager;
import de.mhu.com.morse.channel.ChannelProvider;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.channel.InitialChannelDriver;
import de.mhu.com.morse.channel.ObjectManager;
import de.mhu.com.morse.channel.fs.FileChannelDriver;
import de.mhu.com.morse.types.Types;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class CoreModule extends AfPlugin {

	private ChannelProvider dbProvider;

	protected void apDestroy() throws Exception {
		// TODO Auto-generated method stub

	}

	protected void apDisable() throws AfPluginException {
		// TODO Auto-generated method stub

	}

	protected void apEnable() throws AfPluginException {
		
		// 1. Create init db to provide base config
		InitialChannelDriver initDb = new InitialChannelDriver();
		getApParent().addPlugin( initDb, "initDb" );
		getApParent().enablePlugin( "initDb" );
		
		// 2. Create type provider to provide types from init db
		Types types = new Types();
		getApParent().getApParent().addPlugin( types, "types" );
		
		// 3. Create Object Manager
		ObjectManager objectManager = new ObjectManager();
		getApParent().addPlugin( objectManager, "object_manager" );
		
		// 4. create acl manager
		AclManager aclManager = new AclManager();
		getApParent().getApParent().addPlugin( aclManager, "acl_manager" );
		
		// 5. create connection / channel provider
		dbProvider = new ChannelProvider();
		getApParent().getApParent().addPlugin( dbProvider , "dbProvider" );
		
		// 6. enable types, load now from sys
		getApParent().getApParent().enablePlugin( "types" );

		/*
		if ( config().getProperty( "remove.init.channel", false ) ) {
			getApParent().disablePlugin( "initDb" );
			getApParent().removePlugin( "initDb" );
		}
		*/
		
		getApParent().getApParent().enablePlugin( "dbProvider" );
		getApParent().enablePlugin( "object_manager" );
		getApParent().getApParent().enablePlugin( "acl_manager" );
		
		
	}

	protected void apInit() throws Exception {
		

		
	}

	public ChannelProvider getDbProvider() {
		return dbProvider;
	}

}
