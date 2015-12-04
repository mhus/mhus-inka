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
public class SysCmd extends AfPlugin {

	private static final String CMD_SYS = "sys";
	private static final String CMD_MEM = "mem";
	// private static final String CMD_DB = "db";
	private static final String CMD_GC = "gc";
	private static final String CMD_TIME = "time";
	// private static final String CMD_LIN = "lin";
	
	private long started = System.currentTimeMillis();
	
	protected void apDestroy() throws Exception {
		
	}

	protected void apDisable() throws AfPluginException {
		
	}

	protected void apEnable() throws AfPluginException {
		
	}

	protected void apInit() throws Exception {
		
		IMessageDelegator sysMd = (IMessageDelegator)getSinglePpi( IMessageDelegator.class );
		
		MessageDelegatorImpl md = new MyMd( CMD_SYS );
		sysMd.registerCommand( md );
		
		md.registerCommand( new CmdMemory() );
		//md.registerCommand( new CmdDbInfo() );
		md.registerCommand( new CmdGc() );
		md.registerCommand( new CmdTime() );
		//md.registerCommand( new CmdLin() );
		
	}
	
	public void initFromConfig(String prefix) {
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
	
	private class CmdTime extends Command {
		
		public CmdTime() {
			super( new String[] { CMD_TIME } );
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			
			IMessage ret = msg.getClient().createMessage( getCommands() );
			ret.append( String.valueOf( System.currentTimeMillis() ) );
			
			if ( msg.getCount() == 0 ) {	
				ret.append( String.valueOf( started ) );
			} else {
				for ( int i = 0; i < msg.getCount(); i++ )
					ret.append( msg.getString( i ) ); // is not fast, canbe done in other way
			}
			
			replayToUser( weak, ret );
			
		}
		
		
	}
	private class CmdGc extends Command {

		public CmdGc() {
			super( new String[] { CMD_GC } );
		}
		
		public void doAction(IMessage msg, Weak weak) throws Exception {
			System.gc();
		}
		
	}
	
	/*
	private class CmdDbInfo extends Command {

		public CmdDbInfo() {
			super( new String[] { CMD_SYS, CMD_DB } );
		}

		public void doAction(Message msg, Weak weak) throws Exception {
			Message ret = new Message( getCommands() );
			
			if ( msg.getCount() == 0 ) {
				ret.addParameter( String.valueOf( getServer().getDBPool().getFreeSize() ) );
				ret.addParameter( String.valueOf( getServer().getDBPool().getLockedSize() ) );
				ret.addParameter( String.valueOf( getServer().getWriteConnectionInfo() ) );
			} else
			if ( "locked".equals( msg.getParameter( 0 ) ) ) {
				Object[] list = getServer().getDBPool().getLockedInfo();
				for ( int i = 0; i < list.length; i++ ) {
					ret.addParameter( ((Object[])list[i])[0].toString() );
					ret.addParameter( ((Object[])list[i])[1].toString() );
				}
			
			} else
			if ( "writeunlock".equals( msg.getParameter( 0 ) ) ) {
				getServer().unlockWriteConnecton();
			}
			replayToUser( msg.getClient(), ret );
		}
		
	}
	*/
	
	private class CmdMemory extends Command {


		public CmdMemory() {
			super( new String[] { CMD_MEM } );
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			IMessage ret = msg.getClient().createMessage( getCommands() );
			ret.append( String.valueOf( Runtime.getRuntime().totalMemory() ) );
			ret.append( String.valueOf( Runtime.getRuntime().freeMemory() ) );
			replayToUser( weak, ret );
		}
		
	}

	/*
	private class CmdLin extends Command {

		public CmdLin() {
			super( new String[] { CMD_SYS, CMD_LIN } );
		}

		public void doAction(Message msg, Weak weak) throws Exception {
			Client[] cl = getServer().getTea().getClients();
			Message ret = new Message( getCommands() );
			ret.addParameter( String.valueOf( cl.length ) );
			for ( int i = 0; i < cl.length; i++ )
				ret.addParameter( cl[i].getUserInformation().getUserName() );
			
			replayToUser( msg.getClient(), ret );
		}
		
	
	}
	*/
	
}
