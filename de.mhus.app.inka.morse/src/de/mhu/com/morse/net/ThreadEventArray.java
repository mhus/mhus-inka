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

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.com.morse.cmd.SysCmd;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class ThreadEventArray extends AfPlugin implements IThreadArray
{
	private static int MAX_CONNECTIONS;
	private static int OPTIMAL_THREADS_NUMBER;
	private List eventThreadArray = Collections.synchronizedList( new ArrayList() ); 
	private IMessageDelegator delegator;
	
	
	public synchronized Client addNewConnection(SocketChannel clientChannel, IProtocol protocol )
	{
		 
		EventThread thread = getThreadWithMinimumConnections();
		int minConn = thread.getNumberOfConnections();
		
		if (minConn==MAX_CONNECTIONS)
		{
			thread = createEventThread();
		}
		
		return thread.serviceSocket(clientChannel, protocol);
	}
	
	private EventThread getThreadWithMinimumConnections()
	{
		EventThread thread = (EventThread) eventThreadArray.get(0);
		int minConn = thread.getNumberOfConnections();
		for (int i=1;i<eventThreadArray.size();i++)
		{
			EventThread nextThread = (EventThread) eventThreadArray.get(i);
			if (nextThread.getNumberOfConnections()<minConn)
			{
				thread = nextThread;
				minConn = thread.getNumberOfConnections();			
			}
		}
		return thread;
	}
	
	private EventThread createEventThread()
	{
//		creating new thread to service new connections
		EventThread thread;
		synchronized(eventThreadArray)
		{
			thread = new EventThread( this, delegator );
			thread.startUp();
			eventThreadArray.add(thread);
			log().info("Creating new thread for connections");
			log().info("Actual number of thread = "+eventThreadArray.size());
		}
		return thread;
	}
	
	private void removeEventThread(EventThread thread)
	{
		synchronized (eventThreadArray)
		{
			thread.stopWorking();
			eventThreadArray.remove(thread);
			log().info("Removed free thread");
			log().info("Actual number of thread = "+eventThreadArray.size());
		}
	}
	
	/*
	public boolean isUserLoggedIn(String user) 
	{
		EventThread[] list = (EventThread[])eventThreadArray.toArray( new EventThread[ eventThreadArray.size() ] );
		for ( int i = 0; i < list.length; i++ )
			if (list[i].isConnectionFromUser(user)) return true;
		
		return false;
	}
	*/
	
	public Client[] getClients() 
	{
		EventThread[] list = (EventThread[])eventThreadArray.toArray( new EventThread[ eventThreadArray.size() ] );
		HashSet out = new HashSet();
		for ( int i = 0; i < list.length; i++ ) {
			
			Client[] cl = list[i].getClients();
			for ( int j = 0; j < cl.length; j++ )
				out.add( cl[j] );
			
		}
		
		return (Client[])out.toArray( new Client[ out.size() ] );
	}
	
	public void threadFree(EventThread thread) 
	{
		
		//we can check if there is too much threads ..
		if (eventThreadArray.size()>OPTIMAL_THREADS_NUMBER)
		{
			//TODO: there is a synchronazion problem removeEventThread(thread);
		}

	}

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

		 // int startThreadNumber, MessageDelegator delegator
		 
		delegator = (IMessageDelegator)getSinglePpi( IMessageDelegator.class );
		
		// this.delegator = delegator;
		eventThreadArray = new ArrayList();
		MAX_CONNECTIONS = 30;
		OPTIMAL_THREADS_NUMBER = 3;
		if ( ConfigManager.exists( "server" ) ) {
        	Config config = ConfigManager.getConfig( "server" );
        	MAX_CONNECTIONS = config.getProperty( "max_connections", MAX_CONNECTIONS );
        	OPTIMAL_THREADS_NUMBER = config.getProperty( "start_thread_nr", OPTIMAL_THREADS_NUMBER );
        }
		for (int i=0;i<3;i++)
		{
			createEventThread();
		}

		appendPpi( IThreadArray.class, this );
	}
	
	
}
