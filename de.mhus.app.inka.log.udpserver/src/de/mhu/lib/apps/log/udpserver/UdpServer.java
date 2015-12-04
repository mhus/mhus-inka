/*
 *  Copyright (C) 2002-2004 Mike Hummel
 *
 *  This library is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published
 *  by the Free Software Foundation; either version 2.1 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package de.mhu.lib.apps.log.udpserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.mhu.lib.AEventHandler;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.dtb.DbProvider;
import de.mhu.lib.log.AL;
import de.mhu.lib.log.Appender;
import de.mhu.lib.log.ConsoleAppender;
import de.mhu.lib.log.DbAppender;
import de.mhu.lib.log.AL.LogEventHandler;

public class UdpServer {

	private String host;
	private int port;
	private DatagramSocket connection;
	private byte[] buf;
	private DatagramPacket packet;

	private LogEventHandler eventHandler = new LogEventHandler();
	
	public UdpServer( int pPort ) throws SocketException {
		port = pPort;
		buf = new byte[1024 * 10];
	    packet = new DatagramPacket(buf, buf.length);
		connection = new DatagramSocket( port ); 
	}
	
	public synchronized void readNext() throws IOException {
		
	    connection.receive(packet);
	    
	    try {
	    	ByteArrayInputStream bais = new ByteArrayInputStream( buf );
	    	ObjectInputStream ois = new ObjectInputStream( bais );
	    	ois.readInt();
	    	String remote = ois.readUTF();
	    	int type = ois.readInt();
	    	long time = ois.readLong();
	    	String name = ois.readUTF();
	    	String id = ois.readUTF(); 
	    	long threadId = ois.readLong();
	    	StackTraceElement[] stack = (StackTraceElement[])ois.readObject();
			Object in = ois.readObject();
			
			// System.out.println( remote + ";" + System.currentTimeMillis() + ";" + AL.NAMES[ type ] + ";" + time + ";" + name + ";" + msg + ";" + threadId );
			
			synchronized( eventHandler ) {
					eventHandler.fireLogEvent(type, time, remote + ':' + name, id, threadId, stack, in);
			}
			
	    } catch ( Exception e ) {
	    	e.printStackTrace();
	    }
	}
	
	public AEventHandler<Appender> eventLog() {
		return eventHandler;
	}
	
	/**
	 * -console -mysql jdbc\:mysql\://localhost/test user pass table
	 * @param args
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception {
		final ArgsParser argp = new ArgsParser( args );
		
		UdpServer server = new UdpServer( 6610 );
		
		if ( argp.isSet( "console" ) )
			new ConsoleAppender(server.eventLog() );
		
		if ( argp.isSet( "mysql" ) ) {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			new DbAppender( new DbProvider() {

				public Connection reconnect() throws SQLException {
					System.out.println( "Connect to: " + argp.getValue( "mysql", 0 ) + ' ' + argp.getValue( "mysql", 1 ) + " Table: " + argp.getValue( "mysql", 3 ) );
					Connection db = DriverManager.getConnection( argp.getValue( "mysql", 0 ), argp.getValue( "mysql", 1 ), argp.getValue( "mysql", 2 ) );
					db.setAutoCommit( true );
					return db;
				}
				
			}, argp.getValue( "mysql", 3 ), server.eventLog() );
		}
		
		while( true ) server.readNext();
	}
	
}
