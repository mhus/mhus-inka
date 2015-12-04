package de.mhu.com.morse.cmd;

import java.io.IOException;
import java.util.Hashtable;

import de.mhu.com.morse.cmd.ifc.Command;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class SingleCmd extends AfPlugin implements ISingleCmd {

	private IMessageDelegator sysMd;
	private static long nextId = 0;
	private static Hashtable cmdRegister = new Hashtable();

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
		sysMd = (IMessageDelegator)getSinglePpi( IMessageDelegator.class );
		appendPpi(ISingleCmd.class, this );
	}

	public IMessage sendAndWait( IMessage msg, long timeout ) throws IOException, MorseException {
		
		long checktime = 1000;// TODO config !!!!
		boolean noTimeout = true;
		if ( timeout > 0 ) {
			checktime = Math.min( checktime, timeout );
			noTimeout = false;
		}
		
		Object monitor = new Object();
		Cmd cmd = new Cmd( monitor );
		msg.unshift( cmd.getCommand() );
		msg.unshift( "r" );
		
		msg.getClient().sendMessage( msg );
		
		try {
			synchronized( monitor ) {
				while ( true ) {
					if ( cmd.hasResult() ) break;
					monitor.wait( checktime );
					if ( !noTimeout ) {
						timeout-=checktime;
						if ( timeout <= 0 ) break;
					}
					if ( ! msg.getClient().isConnected() ) {
						log().warn( "Disconnected while waiting for reply: " + msg.toString() );
						break;
					}
				}
			}
		} catch (Throwable e) {
			log().error( e );
		}
		if ( ! cmd.hasResult() )
			throw new TimeoutException( msg );
		return cmd.getResult();
	}
	
	private static String createId() {
		
		synchronized (cmdRegister) {
			String id = null;
			do {
				nextId++;
				if ( nextId == Long.MAX_VALUE )
					nextId = 0;
				id = "_s" + nextId;
			} while ( cmdRegister.containsKey( id ) );
			cmdRegister.put( id, "" );
			return id;
		}
	}
	
	class Cmd extends Command {

		private IMessage result;
		private Object monitor;

		public Cmd( Object pMonitor ) {
			super( createId( ) );
			synchronized (cmdRegister) {
				cmdRegister.put( getCommand(), this );
			}
			monitor = pMonitor;
			sysMd.registerCommand( this );
		}

		public IMessage getResult() throws MorseException {
			String rcType = result.shiftString();
			if ( rcType.length() != 0 ) {
				if ( "rc".equals( rcType ) ) {
					long returnCode = result.shiftLong();
					// rc,?,rc,attr1,attr2....
					String[] out = new String[ result.getCount()+1 ];
					out[ 0 ] = String.valueOf( returnCode );
					for ( int i = 1; i < out.length; i++ )
						out[ i ] = result.shiftString();
					
					throw new MorseException( MorseException.RX_RETURN_CODE, out );
					
				} else
				if ( "null".equals( rcType ) )
					return null;
				if ( "e".equals( rcType ) ) {
					String[] out = new String[ result.getCount() ];
					for ( int i = 0; i < out.length; i++ )
						out[ i ] = result.shiftString();
					
					throw new MorseException( MorseException.RX_ERROR, out );
				}
				if ( "me".equals( rcType ) ) {
					long returnCode = result.shiftLong();
					String[] out = new String[ result.getCount() ];
					for ( int i = 0; i < out.length; i++ )
						out[ i ] = result.shiftString();
					throw new MorseException( returnCode, out );
				}
				throw new MorseException( MorseException.CLIENT_UNKNOWN_RC_CODE, rcType );
			}
			return result;
		}

		public boolean hasResult() {
			return result != null;
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			
			sysMd.unregisterCommand( getCommand() );
			synchronized (cmdRegister) {
				cmdRegister.remove( getCommand() );
			}
			
			result = msg;
			
			try {
				if ( monitor != null )
					synchronized ( monitor ) {
						monitor.notify();
					}
			} catch ( Throwable t ) {
				log().error( t );
			}
			
		}
		
	}
	
}
