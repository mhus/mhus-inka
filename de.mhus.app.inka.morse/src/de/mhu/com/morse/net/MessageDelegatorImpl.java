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
 * Created on 2005-08-09
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.mhu.com.morse.net;

import java.sql.SQLException;
import java.util.*;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.cmd.CmdException;
import de.mhu.com.morse.cmd.ifc.Command;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.lib.plugin.utils.IAfLogger;



public class MessageDelegatorImpl extends Command implements IMessageDelegator
{
	private static AL log = new AL( MessageDelegatorImpl.class );
	
	private Hashtable commands;
	
	public MessageDelegatorImpl()
	{
		super( new String[] { "MD" } );
		commands=new Hashtable();
	}

	public MessageDelegatorImpl( String in )
	{
		super( in );
		commands=new Hashtable();
	}
	
	public void registerCommand(Command com)
	{
		String[] cmds = com.getCommands();
		for ( int i = 0; i < cmds.length; i++ ) {
			if ( log.t1() ) log.info( "Register: " + cmds[i] );
			Object out = commands.put( cmds[i], com );
			if ( out != null )
				log.warn( "Overwrite command: " + cmds[i] + " (" + out.getClass().getName() + " => " + out.getClass().getName() + ")" );
		}	
	}
	
	public void unregisterCommand(String command)
	{
		commands.remove(command);
	}
	
	public void doAction(IMessage msg, Weak weak )
	{
		Command service = (Command) commands.get( msg.getString( 0 ) );
		if (service==null) log.warn("Unknown command: '"+msg.getString( 0 ) + "'");
		else {
			long start = System.currentTimeMillis();
			try {
				msg.shiftParameter();
				service.doAction(msg, weak );
			} catch ( CmdException t ) {
				// log.error( t );
				service.replayToUser( weak, t.getReturnCode() );
			} catch ( Throwable t ) {
				log.error( t );
				service.replayToUser( weak, t );
			}
						
			long diff = System.currentTimeMillis() - start;
			if ( diff > 1000 && log.t3() ) // TODO from config !!!
				log.info("Execution of "+service.getCommand()+" tooks: "+ diff +"ms");
			
		}
	}	
	
}
