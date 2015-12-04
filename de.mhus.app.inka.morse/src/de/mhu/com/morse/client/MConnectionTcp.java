package de.mhu.com.morse.client;

import java.io.IOException;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.cmd.ISingleCmd;
import de.mhu.com.morse.net.Client;
import de.mhu.com.morse.net.Connection;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.net.Client.Listener;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.AfPluginNode;
import de.mhu.lib.plugin.AfPluginRoot;
import de.mhu.lib.plugin.utils.ALLogger;

public class MConnectionTcp extends MConnection {
	
	private AL log = new AL(MConnectionTcp.class);
	private Connection con;
	private Listener closeListener;
	private AfPluginRoot root;
	private AfPluginNode node;
	private ISingleCmd singleCmd;
	private String host;
	private int port;
	private String conName;
	
	public MConnectionTcp () throws Exception  {
		closeListener = new Client.Listener() {

			public void clientClosed(Client src) {
				try {
					actionRemoteClosed();
				} catch (Exception e) {
					log.error( e );
					con = null;
					conName = null;
				}
			}

		};

		root = new AfPluginRoot();
		root.enable();

		ALLogger logger = new ALLogger();
		//XmlConfig config = new XmlConfig( "config_client.xml" );
		root.addPlugin( logger, "log" );
		//root.addPlugin( config, "config" );
		root.refreshTools();

		node = new AfPluginNode();

		String name = root.addPlugin( node, "nio" );
		root.enablePlugin( name );

		ClientModul client = new ClientModul();
		name = node.addPlugin( client, "client" );
		node.enablePlugin( name );

		singleCmd = client.getSingle();
	}
	
	public MConnectionTcp(String host, int port) throws Exception {
		this();
		setProperty( HOST, host );
		setProperty( PORT, port );
	}

	protected void actionRemoteClosed() throws Exception {
		
		String target = null;
		String myHost = host;
		int    myPort = port;
		
		// reconnect
		
		do {
		 
			if ( target != null ) {
				int pos = target.lastIndexOf(':');
				if ( pos > -1 ) {
					myHost = target.substring( 0, pos );
					myPort = Integer.parseInt( target.substring( pos + 1 ) );
				} else
				if ( pos == 0 )
					myPort = Integer.parseInt( target.substring( 1 ) );
				else
					myHost = target;
				target = null;
			}
			
			if ( con != null ) {
				con.getClient().unregister( closeListener );
				try {
					node.disablePlugin( conName );
					node.removePlugin( conName );
				} catch (AfPluginException e) {
					log.error( e );
				}
				con     = null;
				conName = null;
			}
			
			con = new Connection();
			con.setHost(myHost);
			con.setPort(myPort);
			conName = node.addPlugin( con, "con" );
			node.enablePlugin( conName );
			con.getClient().register( closeListener );

			target = service();
			
		} while ( target != null );
		
		login();
		
		types = new MTypes( this );
		
	}
	
	/*
	public void setEntranceAddress( String pHost, int pPort ) {
		host = pHost;
		port = pPort;
	}
	*/
	
	public void connect() throws Exception {
		
		if ( con != null )
			throw new Exception( "Already connected" );
		
		actionRemoteClosed();
	}

	public boolean isConnected() {
		return con != null;
	}
	
	public void close() {
		Connection c = con;
		con = null;
		c.getClient().disconnect();
	}

	public IMessage createMessage() {
		if ( con == null )
			try {
				actionRemoteClosed();
			} catch (Exception e) {
				log.error( e );
			}
		return con.getClient().createMessage();
	}

	public void sendMessage(IMessage msg) throws MorseException {
		try {
			con.getClient().sendMessage(msg);
		} catch (IOException e) {
			throw new MorseException( MorseException.ERROR, e );
		}
	}

	public IMessage sendAndWait(IMessage msg, long timeout ) throws MorseException {
		try {
			return singleCmd.sendAndWait(msg, timeout);
		} catch (IOException e) {
			throw new MorseException( MorseException.ERROR, e );
		}
	}

	@Override
	public void setProperty(int key, String value) {
		switch ( key ) {
		case HOST:
			host = value;
			break;
		case PORT:
			setProperty( key, Integer.parseInt( value ) );
			break;
		}
	}

	@Override
	public void setProperty(int key, int value) {
		switch ( key ) {
		case PORT:
			port = value;
			break;
		}
	}
	
}
