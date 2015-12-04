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
 * Created on Sep 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.mhu.com.morse.cmd.ifc;

import de.mhu.com.morse.net.Client;

/**
 * This object should be used synchronized !!!!
 * 
 * @author jesus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Weak {
	
	private String sender;
	private Client client;
	/**
	 * 
	 */
	public Weak(  ) {
		super();
		// server = pServer;
	}

	public void destroy() {
		
	}

	public void setSender(String in) {
		sender = in;
	}
	
	
	/**
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}
	
	public boolean isSender() {
		return sender != null;
	}

	public void setClient(Client in) {
		client = in;
	}

	public Client getClient() {
		return client;
	}

}
