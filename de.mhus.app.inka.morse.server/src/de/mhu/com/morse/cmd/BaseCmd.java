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
package de.mhu.com.morse.cmd;


import de.mhu.com.morse.cmd.ifc.Command;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.net.IMessage;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

/**
 * @author jesus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BaseCmd extends AfPlugin {

	private IMessageDelegator sysMd;
	
	protected void apDestroy() throws Exception {
		
	}

	protected void apDisable() throws AfPluginException {
		
	}

	protected void apEnable() throws AfPluginException {
		
	}

	protected void apInit() throws Exception {
		
		sysMd = (IMessageDelegator)getSinglePpi( IMessageDelegator.class );
		
		sysMd.registerCommand( new QuitCmd() );
		sysMd.registerCommand( new PingCmd() );
		sysMd.registerCommand( new SenderCmd() );
		sysMd.registerCommand( new SessionCmd() );
		
	}
	
	private class QuitCmd extends Command {

		private static final String CMD_QUIT = "quit";

		/**
		 * @param command
		 */
		public QuitCmd() {
			super( CMD_QUIT );
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			msg.unshift( CMD_QUIT );
			msg.getClient().disconnect();
		}
		
	}
	
	private class PingCmd extends Command {

		private static final String CMD_PING = "ping";

		/**
		 * @param command
		 */
		public PingCmd() {
			super( CMD_PING );
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			msg.unshift( CMD_PING );
			replayToUser( weak, msg );
		}
		
	}
	
	private class SenderCmd extends Command {

		private static final String CMD = "r"; // "r" for return to

		/**
		 * @param command
		 */
		public SenderCmd() {
			super( CMD );
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			weak.setSender( msg.getString( 0 ) );
			msg.shiftParameter();
			
			sysMd.doAction(msg, weak );
			
		}
		
	}
	
	private class SessionCmd extends Command {

		private static final String CMD = "s";

		/**
		 * @param command
		 */
		public SessionCmd() {
			super( CMD );
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			// TODO weak.setSession( msg.getString( 0 ) );
			msg.shiftParameter();
			
			sysMd.doAction(msg, weak );
			
		}
		
	}
}
