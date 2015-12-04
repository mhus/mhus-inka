package de.mhu.com.morse.cmd;

import java.util.Iterator;

import de.mhu.lib.AThread;
import de.mhu.lib.AThreadDaemon;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IChannelServer;
import de.mhu.com.morse.channel.QueryResultQueue;
import de.mhu.com.morse.cmd.ifc.Command;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.mql.CompilledQueryMessage;
import de.mhu.com.morse.mql.IQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.ServerQuery;
import de.mhu.com.morse.usr.IQueue;
import de.mhu.com.morse.usr.Session;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.net.Client;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class QueryCmd extends AfPlugin {

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
		// sysMd.registerCommand( new IntQueryTstCmd() );
		// sysMd.registerCommand( new IntQueryStrCmd() );
		sysMd.registerCommand( new IntQueryCmd() );
		sysMd.registerCommand( new IntDbDef() );
		sysMd.registerCommand( new IntChannelCommit() );
		sysMd.registerCommand( new IntChannelRollback() );
		sysMd.registerCommand( new IntChannelSetAutoCommit() );
		
	}
/*
	class IntQueryTstCmd extends Command {

		public IntQueryTstCmd() {
			super("qry.tst");
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			
			Query query = new Query( weak.getSession().getDbConnection( 0 ), msg.getString( 0 ) );
			IMessage res = toMessage( msg.getClient(), query.execute() );
			replayToUser(weak, res);
		}
		
	}
*/
/*
	class IntQueryStrCmd extends Command {

		public IntQueryStrCmd() {
			super("qry.str");
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			
			Query query = new Query( weak.getSession().getDbConnection( msg.getInteger( 0 ) ), msg.getString( 1 ) );
			IQueryResult res = query.execute();
			long rc = res.getReturnCode();
			IMessage rcMsg = msg.getClient().createMessage();
			rcMsg.append( rc );
			if ( rc < 0 ) {
				rcMsg.append( res.getReturnCode() );
				rcMsg.append( res.getErrorInfo() );
			} else {
				int id = weak.getSession().createQueue( new QueryResultQueue( res ) );
				rcMsg.append( id );
				
				String[] cols = res.getColumns();
				rcMsg.append( cols.length );
				for ( int i = 0; i < cols.length; i++ ) {
					rcMsg.append( cols[i] );
					rcMsg.append( res.getAttribute( i ).getCanonicalName() );
					rcMsg.append( res.getAttribute( i ).getType() );
				}
				
			}
			replayToUser( weak, rcMsg );
		}
		
	}
	
	public static IMessage toMessage(Client client, IQueryResult result) throws MorseException {
		
		IMessage msg = client.createMessage();
		String[] cols = result.getColumns();
		msg.append( cols.length );
		for ( int i = 0; i < cols.length; i++ ) {
			msg.append( cols[i] );
			msg.append( result.getAttribute( i ).getCanonicalName() );
			msg.append( result.getAttribute( i ).getType() );
		}
		
		while ( result.next() ) {
			for ( int i = 0; i < cols.length; i++ ) {
				
				IAttribute attr = result.getAttribute( i );
				if ( attr.isTable() ) {
					ITable table = result.getTable( i );
					String[] colsTable = table.getColumns();
					
					while ( table.next() ) {
						msg.append( colsTable.length );
						for ( int j = 0; j < colsTable.length; j++ )
							msg.append( table.getString( j ) );
						
					}
					msg.append( 0 );
					table.close();
					
				} else
					msg.append( result.getString( i ) );
			}
		}
		
		return msg;
	}
	*/
	
	class IntQueryCmd extends Command {

		public IntQueryCmd() {
			super("qry");
		}

		public void doAction(IMessage msg, final Weak weak) throws Exception {
			
			int dbId = msg.getInteger( 0 );
			String dbName = msg.getString( 1 );
			msg.shiftParameter();
			msg.shiftParameter();
			
			final ServerQuery query = new ServerQuery( ((Session)weak.getClient().getUserObject()).getDbConnection( dbId ), dbName, new CompilledQueryMessage( msg ) );
			AThreadDaemon thread = new AThreadDaemon() {
				public void run() {
					
					IQueryResult res = null;
					try {
						res = query.execute( ((Session)weak.getClient().getUserObject()).getUser() );
					
						long rc = res.getReturnCode();
						IMessage rcMsg = weak.getClient().createMessage();
						rcMsg.append( rc );
						if ( rc < 0 ) {
							rcMsg.append( res.getErrorCode() );
							rcMsg.append( res.getErrorInfo() );
						} else {
							int type = res.getPreferedQuereType();
							rcMsg.append( type );
							
							String[] cols = res.getColumns();
							if ( cols == null ) cols = new String[0];
							rcMsg.append( cols.length );
							for ( int i = 0; i < cols.length; i++ ) {
								rcMsg.append( cols[i] );
								rcMsg.append( res.getAttribute( i ).getCanonicalName() );
								rcMsg.append( res.getAttribute( i ).getType() );
								/*
								if ( res.getAttribute( i ).isTable() ) {
									
									for ( Iterator j = res.getAttribute( i ).getAttributes(); j.hasNext(); ) {
										IAttribute a2 = (IAttribute)j.next();
										rcMsg.append( 1 );
										rcMsg.append( a2.getName() );
										rcMsg.append( a2.getType() );
									}
									rcMsg.append( 0 );
								}
								*/
							}
							
							if ( type == IQueryResult.QUEUE_FETCH ) {
								QueryResultQueue qrq = new QueryResultQueue( res );
								int id = ((Session)weak.getClient().getUserObject()).createQueue( qrq );
								rcMsg.append( id );
								qrq.appendToMsg( rcMsg );
								rcMsg.append( 0 );
							} else
							if ( type == IQueryResult.QUEUE_STREAM_IN ) {
								int id = ((Session)weak.getClient().getUserObject()).createQueue( new QueryStreamInQueue( res ) );
								rcMsg.append( id );
							} else
							if ( type == IQueryResult.QUEUE_STREAM_OUT ) {
								int id = ((Session)weak.getClient().getUserObject()).createQueue( new QueryStreamOutQueue( res ) );
								rcMsg.append( id );
							}
							
							if ( type != IQueryResult.QUEUE_FETCH ) {
								QueryResultQueue helper = new QueryResultQueue( res );
								while ( helper.appendToMsg( rcMsg ) ) {
								}
								rcMsg.append( 0 );
							}
							
							if ( type == IQueryResult.QUEUE_ONE_PACKAGE ) {
								res.close();
								res = null;
							}
						
							replayToUser( weak, rcMsg );
							
						}
					} catch ( Throwable e ) {
						log().info( e );
						replayToUser( weak, e );
						if ( res != null )
							res.close();
					}
				}
			};
			thread.start();
		}
		
	}
	
	class IntDbDef extends Command {
		
		public IntDbDef() {
			super("db.def");
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			IMessage ret = weak.getClient().createMessage();
			IChannel channel = ((Session)weak.getClient().getUserObject()).getDbConnection(0).getChannel( msg.getString(0) );
			if ( ! ( channel instanceof IChannelServer ) )
				throw new MorseException( MorseException.ACCESS_DENIED, "no server channel" );
			
			ret.append(
					((IChannelServer)channel).getDefinition()
					);
			replayToUser( weak, ret );
		}
		
	}
	
	class IntChannelCommit extends Command {
		
		public IntChannelCommit() {
			super("ch.c");
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			int dbId = msg.shiftByte();
			((Session)weak.getClient().getUserObject()).getDbConnection( dbId ).commit();
			replayToUser( weak, 0 );
		}
		
	}
	
	class IntChannelRollback extends Command {
		
		public IntChannelRollback() {
			super("ch.r");
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			int dbId = msg.shiftByte();
			((Session)weak.getClient().getUserObject()).getDbConnection( dbId ).rollback();
			replayToUser( weak, 0 );
		}
		
	}
	
	class IntChannelSetAutoCommit extends Command {
		
		public IntChannelSetAutoCommit() {
			super("ch.sac");
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			int dbId = msg.shiftByte();
			if ( msg.getCount() > 0 )
				((Session)weak.getClient().getUserObject()).getDbConnection( dbId ).setAutoCommit( msg.shiftByte() == 1 );
			IMessage res = weak.getClient().createMessage();
			res.append( (byte)( ((Session)weak.getClient().getUserObject()).getDbConnection( dbId ).isAutoCommit() ? 1 : 0  ) );
			replayToUser( weak, res );

		}
		
	}
}
