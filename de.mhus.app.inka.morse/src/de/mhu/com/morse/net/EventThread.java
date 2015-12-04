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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.mhu.lib.AThreadDaemon;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.lib.plugin.utils.IAfLogger;
import de.mhu.lib.statistics.AStatistics;

public class EventThread extends AThreadDaemon
{
	private static final String QUIT="quit";
	private static Config config = ConfigManager.getConfig( "cache" );
	private static AL log = new AL( EventThread.class );
	private AStatistics statClients = new AStatistics( EventThread.class.getName() + "_clients" );
	
	private Selector readSelector;
	private ThreadEventArray array;
	private ByteBuffer writeBuffer;
    private Map clients = Collections.synchronizedMap( new HashMap() );
	private ByteBuffer readBuff;
	private boolean work;
	private IMessageDelegator delegator;
	
	private static long clientId = 0;
	
	// private static final Logger LOG = Logger.getLogger(EventThread.class);
	
	private static int nextId = 0;
	
	private synchronized static int getNextId() {
		nextId++;
		return nextId;
	}

	public EventThread( ThreadEventArray array, IMessageDelegator delegator )
	{
		super( "ET_" + getNextId() );
		this.delegator = delegator;
    	writeBuffer = ByteBuffer.allocateDirect( config.getProperty( "default.buffer.size", 1024 * 20 ) ); 
    	this.array=array;
    	readBuff = ByteBuffer.allocateDirect( config.getProperty( "default.buffer.size", 1024 * 20 ) ); 
	}
	
	public Client serviceSocket(SocketChannel clientChannel, IProtocol protocol ) 
	{
		synchronized( this ) {
			try
			{
				if (!work)
				{
					work=true;
					//Thread thread = new Thread(this);
					start();
				}
				Client client = new Client(this, clientChannel, protocol, delegator );
	            clientChannel.configureBlocking(false);
				clientChannel.register(readSelector, SelectionKey.OP_READ, client);
				synchronized( clients ) {
					clients.put(clientChannel,client);
					statClients.dec();
				}
				if ( log.t4() ) log.info("Connection opened: " + clientChannel.toString() );
				// client.sendMessage( new Message( "hello" ) );
				return client;
			}
			catch (Exception e)
			{
				log.error("Error while starting service socket:",e);
			}
			return null;
		}
	}
	
	public void startUp()
	{
		try
		{
			readSelector = Selector.open();
		}
		catch (Exception e)
		{
			log.error("Error while opening socket",e);
		}

	}
	
	public void run()
	{
		try
		{
		while(work)
		{
			Thread.sleep(100); 
			readSelector.selectNow();
			Set readyKeys = readSelector.selectedKeys();
		        
			 Iterator i = readyKeys.iterator();
			 while (i.hasNext()) 
			 {
			 	
			 	Client client = null;
			 	try {
					 SelectionKey key = (SelectionKey) i.next();
					 i.remove();
					 SocketChannel channel = (SocketChannel) key.channel();
					 client = (Client) key.attachment();
					 
					 readBuff.clear();
			        // reading data into bufor
			        long nbytes = channel.read(readBuff);
			        
			        // check end of the stream
			        if (nbytes == -1) 
			        { 
			            client.disconnect();
			        }
			        else 
			        {
			        	readBuff.flip();
			        	client.write( readBuff );
			        	readBuff.clear();
			        	while (client.hasMessage())
			        	{
			        		IMessage msg = client.nextMessage();
			        		if ( msg.getCount() == 0 ) {
			        			//hmmmm
			        		}
			        		else
			        		{
			        			log.enter( client );
			        			msg.setClient(client);
			        			if ( log.t10() ) log.info( "RX_MSG: "+msg);
			        			
			        			Weak weak = new Weak( );
			        			weak.setClient( client );
		        				client.getDelegator().doAction(msg, weak );
		        				weak.destroy();
			        			log.leave( client );
			        			
			        		}
			        	}
			        }
		        } catch ( Throwable te ) {
		        	if ( te instanceof IOException ) {
		        		if ( log.t4() )
		        			log.info( "Connection closed: " + te.toString() );
		        		if ( log.t10() )
		        			log.info( te );
		        	} else {
		        		log.error("Error while reading from client", te );
		        	}
		        	if ( client != null ) client.disconnect();
		        }

			 }
		}
	}
	catch (Exception e)
	{
		log.error("There is error while reading from socket",e);
		System.exit( 1 );
	}
	}
	
	public synchronized void sendMessage(SocketChannel channel,byte[] mesg) 
	{
		int o = 0;
		do {
			int l = Math.min( writeBuffer.capacity(), mesg.length - o );
		    prepWriteBuffer(mesg, o, l );
		    channelWrite(channel);
		    o+= l;
		} while ( o < mesg.length );
	    
	}
	private void prepWriteBuffer(byte[] mesg, int off, int len ) 
    {
        // fills the buffer from the given string
        // and prepares it for a channel write
        writeBuffer.clear();
        writeBuffer.put( mesg, off, len );
        //writeBuffer.putChar('\n');
        writeBuffer.flip();
    }
        
	
	private void channelWrite(SocketChannel channel) 
	{
        long nbytes = 0;
        long toWrite = writeBuffer.remaining();
        // loop on the channel.write() call since it will not necessarily
        // write all bytes in one shot
        try 
		{
            while (nbytes != toWrite) 
            {
            	nbytes += channel.write(writeBuffer);
            
            	if ( nbytes != toWrite ) {
	            	try 
					{
	            		if ( log.t3() )
	            			log.info( "Write is sleeping :-< " + nbytes + "/" + toWrite );
	            		Thread.sleep( config.getProperty( "default.write.sleep", 50 ) );
					}
	            	catch (InterruptedException e) {
	            		log.info("InterruptedException" );
	            	}
            	}
            	
            }
        }
        catch (ClosedChannelException cce) { 
        	// no log for this, have circular stack with slog command
        }
        catch (Exception e) {log.error("Exception: ",e); } 
        
        // get ready for another write if needed
        writeBuffer.rewind();
        }

	/**
	 * Call of this function only from Client.disconnect() !!!!!!!!!!!!!!!
	 * 
	 * @param channel
	 */
	public void deregisterClient(SocketChannel channel)
	{
		
		synchronized( this ) {
			try
			{
				channel.keyFor( readSelector ).cancel();
				channel.close();
				synchronized( clients ) {
					clients.remove(channel);
				
					log.info("Connection close for: " +channel.socket().getInetAddress());
					statClients.inc();
					if (clients.size()==0)
					{
						//we inform array that this thread can be removed if array wants
						array.threadFree(this);
					}
				}
			}
			catch (Exception e)
			{
				log.error("Error while closing connection",e);
			}
		}
	}
	
	public synchronized int getNumberOfConnections() 
	{
		return clients.size();
	}

	
	/*
	public boolean isConnectionFromUser(String user) 
	{
		Client[] cl = null;
		synchronized( clients ) {
			cl = (Client[])clients.values().toArray( new Client[ clients.size() ] );
		}
		for ( int i = 0; i < cl.length; i++ )
		{
			if (cl[i].getUserInformation().getUserName().equals(user)) return true;
		}
		return false;
	
	}
	*/
	
	public Client[] getClients() 
	{
		synchronized( clients ) {
			return (Client[])clients.values().toArray( new Client[ clients.size() ] );
		}
	
	}
	
	public void stopWorking()
	{
		this.work=false;
	}
}
	


