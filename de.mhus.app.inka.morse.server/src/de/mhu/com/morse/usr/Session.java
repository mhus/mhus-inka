package de.mhu.com.morse.usr;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.net.Client;
import de.mhu.com.morse.net.Client.Listener;

public class Session implements Listener {

	private static AL log = new AL( Session.class );
	
	private IQueue[] queues = new IQueue[10];
	private IConnection[] dbs = new IConnection[ 10 ];
	private IChannelProvider dbProvider;
	private UserInformation user;
	
	public Session( IChannelProvider pDbProvider, UserInformation pUser, Client client ) {
		dbProvider = pDbProvider;
		dbs[0] = dbProvider.getDefaultConnection();
		user = pUser;
		client.register( this );
	}
	
	public int createQueue( IQueue queue ) {
		synchronized (queues) {
			for ( int i = 0; i < queues.length; i++ )
				if ( queues[i] == null ) {
					queues[i] = queue;
					return i;
				}
			
			// auto extend
			// TODO max amount of queues !!!
			IQueue[] newQueues = new IQueue[ queues.length + 10 ];
			System.arraycopy(queues, 0, newQueues, 0, queues.length );
			int ret = queues.length;
			queues = newQueues;
			return ret;
		}
	}
	
	public IQueue getQueue( int index ) {
		synchronized (queues) {
			return queues[index];
		}
	}
	
	public void closeQueue( int index ) {
		synchronized (queues) {
			if ( queues[index] == null ) return;
			queues[index].close();
			queues[index]=null;
		}
	}

	public IConnection getDbConnection( int db ) {
		return dbs[ db ];
	}
	
	public UserInformation getUser() {
		return user;
	}

	public void clientClosed(Client src) {
		
		log.debug( "disconnect session for " + user.getUserId() );
		
		for ( int i = 0; i < queues.length; i++ ) {
			if ( queues[ i ] != null ) {
				try {
					queues[ i ].close();
				} catch ( Throwable t ) {
					log.info( t );
				}
				queues[ i ] = null;
			}
		}
		for ( int i = 0; i < dbs.length; i++ ) {
			if ( dbs[ i ] != null ) {
				try {
					dbs[ i ].close();
				} catch ( Throwable t ) {
					log.info( t );
				}
				dbs[ i ] = null;
			}
		}
		
		dbProvider = null;
		user = null;
	}
	
}
