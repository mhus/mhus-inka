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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;


public class Client 
{
	private static AL log = new AL( Client.class );
	private EventThread eventThread;
	private SocketChannel channel;
	private IProtocol protocol;
	private MyOutputStream myOutputStream = new MyOutputStream();
	
	private Set listeners = new HashSet();	
	private Listener[] listenersArray = null;
	private byte[] writeBuffer = new byte[ 1024 ];
	
	private String remote = "?";
	private Object userObject = null;
	private IMessageDelegator delegator;
	
	public Client(EventThread eventThread, SocketChannel channel, IProtocol protocol, IMessageDelegator pDelegator )
	{
		delegator = pDelegator;
		this.eventThread=eventThread;
		this.channel=channel;
		this.protocol = protocol;
		remote = channel.socket().getInetAddress().toString();
	}
	
	public IMessage createMessage( ) {
		IMessage msg = protocol.createMessage();
		msg.setClient( this );
		return msg;
	}
	
	public IMessage createMessage( String cmd ) {
		IMessage msg = protocol.createMessage();
		msg.setClient( this );
		msg.append( cmd );
		return msg;
	}
	
	public IMessage createMessage( String[] cmd ) {
		IMessage msg = protocol.createMessage();
		msg.setClient( this );
		for ( int i = 0; i < cmd.length; i++ )
			msg.append( cmd[i] );
		return msg;
	}
	
	public boolean hasMessage()
	{
		return protocol.hasMessage();
	}
	
	public IMessage nextMessage()
	{
		return protocol.nextMessage();
	}

	public synchronized void sendMessage(IMessage msg) throws IOException
	{
		if ( log.t3() )
			log.info( "TX_MSG: " + msg.toString() );
		protocol.encodeMsg( msg, myOutputStream );
	}

	public EventThread getEventThread() 
	{
		return eventThread;
	}

	public void register( Listener l ) {
		synchronized ( listeners ) {
			if ( listeners.add( l ) )
				listenersArray = (Listener[])listeners.toArray( new Listener[ listeners.size()] );
		}
	}
	
	public void unregister( Listener l ) {
		synchronized ( listeners ) {
			if ( listeners.remove( l ) )
				listenersArray = (Listener[])listeners.toArray( new Listener[ listeners.size()] );
		}
	}
	
	public static interface Listener {
		public void clientClosed( Client src );
	}

	/**
	 * 
	 */
	public void disconnect() {
		eventThread.deregisterClient(channel);
		channel = null;
		// fire event
		Listener[] la = listenersArray;
		if ( la != null )
			for ( int i = 0; i < la.length; i++ )
				try {
					la[i].clientClosed( this );
				} catch ( Throwable e ) {}
		
		listeners.clear();
		listenersArray = null;
		
	}

	public boolean isConnected() {
		return channel != null;
	}
	
	public void write(ByteBuffer readBuff) {
		if ( readBuff.hasArray() ) {
			protocol.write( readBuff.array(), readBuff.arrayOffset() + readBuff.position(), readBuff.remaining() );
		} else {
			while ( readBuff.remaining() != 0 ) {
				int len = Math.min( writeBuffer.length, readBuff.remaining() );
				readBuff.get( writeBuffer, 0, len );
				protocol.write( writeBuffer, 0, len );
			}
		}
	}
	
	private class MyOutputStream extends OutputStream {
		byte[] buffer = new byte[1];
		
		public void write(int i) throws IOException {
			buffer[0] = (byte)i;
			write( buffer );
		}
		
		public void write(byte in[]) throws IOException {
			eventThread.sendMessage( channel, in );
	    }
		
	}
	
	public String toString() {
		return remote;
	}

	public Object getUserObject() {
		return userObject;
	}
	
	public void setUserObject( Object in ) {
		userObject = in;
	}
	
	/**
	 * @return the delegator
	 */
	public IMessageDelegator getDelegator() {
		return delegator;
	}

	/**
	 * @param delegator the delegator to set
	 */
	public void setDelegator(IMessageDelegator delegator) {
		this.delegator = delegator;
	}
	
}
