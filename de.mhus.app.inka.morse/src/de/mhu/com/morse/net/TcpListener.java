/***********************************************************
GNU Lesser General Public License

JMorseCore - Permanent Connection Messaging Service
Copyright (C) 2004-2005 Rise s.a.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
************************************************************/
package de.mhu.com.morse.net;

import java.nio.channels.*;
import java.net.*;
import java.io.*;

import de.mhu.lib.AThread;
import de.mhu.lib.AThreadDaemon;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class TcpListener extends AfPlugin implements Runnable
{
    private ServerSocketChannel sSockChan;
    private IThreadArray threadArray;
    private boolean running;
	// private Server server;
	private AThread thread = null;
	private int port;
	private String host;

    private void initServerSocket() {
    try {
        // open non-blocking channel
        sSockChan = ServerSocketChannel.open();
        sSockChan.configureBlocking(false);

        // binding with addres
        // InetAddress addr = InetAddress.getByName( server.getConfig().getString("default.host"));// getLocalHost();
        // sSockChan.socket().bind(new InetSocketAddress(addr, server.getConfig().getInteger("default.port", 6666 )));
        host = "localhost";
        port = 6666;
        if ( ConfigManager.exists( "server" ) ) {
        	Config config = ConfigManager.getConfig( "server" );
        	host = config.getProperty( "host", host );
        	port = config.getProperty( "port", port );
        }
        InetAddress addr = InetAddress.getByName( host );// getLocalHost();
        sSockChan.socket().bind(new InetSocketAddress(addr, port ));
    }
    catch (Exception e) 
	{
        log().error("Error during server initialization", e);
        System.exit( 1 );
    }
    }

    public void run() {
    initServerSocket();

    log().info("listener " + host + ':' + port + " on-line");
    running = true;

    // block while we wait for a client to connect
    while (running) {
        // check for new client connections
        acceptNewConnections();
        
        
        // sleep a bit
        try {
        	Thread.sleep(100);
        }
        catch (InterruptedException ie) {
        }
    }
    }
    
    private void acceptNewConnections() {
    try {
        SocketChannel clientChannel;
        // since sSockChan is non-blocking, this will return immediately 
        // regardless of whether there is a connection available
        while ((clientChannel = sSockChan.accept()) != null) {
        	//LOG.info( "Accept new connection" );
        	threadArray.addNewConnection(clientChannel, new ProtocolBin() ); // need a protocoll factory !!
        
        	/*
        
        if (threadArray.addNewConnection(clientChannel))
        {
        
        LOG.info("new connection from: " + clientChannel.socket().getInetAddress());
        }
        
        else 
        {
        	LOG.info("connection not accepted from: " + clientChannel.socket().getInetAddress());
        	clientChannel.close();
        }*/
        //sendBroadcastMessage("\n\rlogin from: " + clientChannel.socket().getInetAddress(), clientChannel);
        //sendMessage(clientChannel, "Type 'quit' to exit.\n\r");
        }        
    }
    catch (IOException ioe) {
    	log().error("Error while accept(): ", ioe);
    }
    catch (Exception e) {
    	log().error("Error in  acceptNewConnections()", e);
    }
    }

	protected void apDestroy() throws Exception {
		thread.stop();
	}

	protected void apDisable() throws AfPluginException {
		
	}

	protected void apEnable() throws AfPluginException {
		threadArray = (IThreadArray) getSinglePpi( IThreadArray.class );
		
		if ( thread == null ) {
			thread = new AThreadDaemon( this );
			thread.start();
		}
		
	}

	protected void apInit() throws Exception {
		// TODO Auto-generated method stub
		
	}   
    
}
 