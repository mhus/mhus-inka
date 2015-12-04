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
/*
 * Created on Sep 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.mhu.com.morse.net;

import java.util.HashSet;
import java.util.Set;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.net.Client.Listener;


/**
 * @author jesus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BcGroup implements Listener {

	private static final AL LOG = new AL( BcGroup.class );
	
	private Set listeners = new HashSet();	
	private Client[] listenersArray = null;
	
	public void register( Client cli ) {
		synchronized( listeners ) {
			if ( listeners.add( cli ) ) {
				cli.register( this );
				listenersArray = (Client[])listeners.toArray( new Client[ listeners.size() ] );
			}
		}
	}
	
	public void unregister( Client cli ) {
		synchronized( listeners ) {
			if ( listeners.remove( cli ) ) {
				cli.unregister( this );
				listenersArray = (Client[])listeners.toArray( new Client[ listeners.size() ] );
			}
		}
		
	}
	
	public void doAction( IMessage msg ) {
		// send to all listeners
		Client[] la = listenersArray;
		if ( la == null ) return;
		for ( int i = 0; i < la.length; i++ )
			try {
				la[i].sendMessage( msg );
			} catch ( Throwable e ) {
				LOG.warn( e );
			}
		
	}

	/* (non-Javadoc)
	 * @see client.Client.Listener#clientClosed(client.Client)
	 */
	public void clientClosed(Client src) {
		unregister( src );
	}

}
