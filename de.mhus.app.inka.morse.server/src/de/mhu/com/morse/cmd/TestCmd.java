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
 * Created on Sep 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.mhu.com.morse.cmd;

import de.mhu.com.morse.cmd.ifc.Command;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.net.MessageDelegatorImpl;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

/**
 * @author jesus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestCmd extends AfPlugin {

	private static final String CMD_TEST = "test";


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
		
		IMessageDelegator sysMd = (IMessageDelegator)getSinglePpi( IMessageDelegator.class );
		
		MessageDelegatorImpl md = new MyMd( CMD_TEST );
		sysMd.registerCommand( md );
		
		md.registerCommand( new CmdString() );
		
	}
		
	private class MyMd extends MessageDelegatorImpl {
		
		public MyMd(String command) {
			super(command );
		}
		
		public void doAction(IMessage msg, Weak weak) {
			/*
			if ( ! msg.getClient().getUserInformation().hasAdminRights() ) {
				replayToUser( msg.getClient(), weak, -100 );
				return;
			}
			*/
			super.doAction( msg, weak );
		}
		
	}
	
	private class CmdString extends Command {

		private static final String CMD_STRING = "str";

		/**
		 * @param command
		 */
		public CmdString() {
			super( CMD_STRING );
		}

		/* (non-Javadoc)
		 * @see commands.ifc.Command#doAction(net.Message, commands.ifc.Weak)
		 */
		public void doAction( IMessage msg, Weak weak) throws Exception {
			replayToUser( weak, msg.getClient().createMessage( "Umlaute \u00E4 \u00F6 \u00FC \u00DF \u00C4 \u00D6 \u00DC - Raute # - Komma , - Enter \n") );
		}
		
	}
	
}
