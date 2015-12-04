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
 * Created on 2005-08-10
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.mhu.com.morse.cmd.ifc;

import java.io.IOException;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.net.Client;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.utils.MorseException;

public abstract class Command 
{
	private static AL log = new AL( Command.class );
	private String[] command;
	
	public abstract void doAction(IMessage msg, Weak weak) throws Exception;
	
	public Command(String command)
	{
		this( new String[] { command } );
	}
	
	
	/**
	 * @param strings
	 */
	public Command(String[] command ) {
		this.command = command;
	}

	public void replayToUser( Weak weak, long returnCode ) {
		replayToUser(weak, returnCode, null );
	}
	
	public void replayToUser( Weak weak, Throwable error ) {
		
		IMessage msg = weak.getClient().createMessage();
		if ( error instanceof MorseException ) {
			MorseException me = (MorseException)error;
			msg.append( "me" );
			msg.append( me.getMessageId() );
			for ( int i = 0; i < me.getAttrSize(); i++ )
				msg.append( me.getAttr( i ) );
			msg.append( "RUE_ID: " + me.getUinqueId() );
			Throwable t = error;
			while ( t.getCause() != null ) {
				t = t.getCause();
				msg.append( t.toString() );
			}
			
		} else {
			msg.append( "e" );
			msg.append( error.toString() );
			Throwable t = error;
			while ( t.getCause() != null ) {
				t = t.getCause();
				msg.append( t.toString() );
			}
		}
		
		if ( weak.isSender() )
			msg.unshift( weak.getSender() );
		
		try {
			weak.getClient().sendMessage(msg);
		} catch (IOException e) {
			log.error( e );
		}
		
	}

	public void replayToUser( Weak weak, long returnCode, String info )
	{
		IMessage msg = weak.getClient().createMessage();
		
		msg.append( "rc" );
		msg.append( returnCode );
		
		if ( info != null )
				msg.append( info );
		
		if ( weak.isSender() )
			msg.unshift( weak.getSender() );
		
		try {
			weak.getClient().sendMessage(msg);
		} catch (IOException e) {
			log.error( e );
		}
		
	}
		
	public void replayToUser(Weak weak, IMessage msg )
	{
		if ( weak.isSender() ) {
			msg.unshift( "" );
			msg.unshift( weak.getSender() );
		}
		
		try {
			weak.getClient().sendMessage(msg);
		} catch (IOException e) {
			log.error( e );
		}
	}
	
	
	/**
	 * @return
	 */
	public String getCommand() {
		return command[ command.length - 1 ];
	}
	
	public String[] getCommands() {
		return command;
	}
	
}
