package de.mhu.com.morse.cmd;

import de.mhu.com.morse.cmd.ifc.Command;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.usr.IQueue;
import de.mhu.com.morse.usr.Session;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

/**
 * q.c  queue close
 * q.n  queue next
 * q.id queue id
 * 
 * @author mike
 *
 */
public class QueueCmd extends AfPlugin {

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
		sysMd.registerCommand( new CloseCmd() );
		sysMd.registerCommand( new NextCmd() );
		sysMd.registerCommand( new ResetCmd() );
	}

	class CloseCmd extends Command {
		
		public CloseCmd() {
			super( "q.c" );
		}
		
		public void doAction(IMessage msg, Weak weak) throws Exception {
			int index = msg.getInteger( 0 );
			((Session)weak.getClient().getUserObject()).closeQueue( index );
		}
		
	}
	class ResetCmd extends Command {
		
		public ResetCmd() {
			super( "q.r" );
		}
		
		public void doAction(IMessage msg, Weak weak) throws Exception {
			int index = msg.getInteger( 0 );
			boolean b = ((Session)weak.getClient().getUserObject()).getQueue( index ).reset();
			IMessage ret = weak.getClient().createMessage();
			ret.append( b ? (byte)1 : (byte)0 );
			replayToUser( weak, ret );
		}
		
	}
	
	class NextCmd extends Command {
		
		public NextCmd() {
			super( "q.n" );
		}
		
		public void doAction(IMessage msg, Weak weak) throws Exception {
			int index = msg.shiftInteger();
			IQueue queue = ((Session)weak.getClient().getUserObject()).getQueue( index );
			IMessage res = queue.next( msg, weak );
			if ( res == null ) {
				((Session)weak.getClient().getUserObject()).closeQueue( index );
				replayToUser(weak, MorseException.TX_QUEUE_END );
				return;
			}
			replayToUser( weak, res );
		}
		
	}
	
}
