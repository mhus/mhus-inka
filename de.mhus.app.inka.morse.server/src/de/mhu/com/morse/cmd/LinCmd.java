package de.mhu.com.morse.cmd;

import de.mhu.com.morse.aaa.AuthControl;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.cmd.ifc.Command;
import de.mhu.com.morse.cmd.ifc.IMessageDelegator;
import de.mhu.com.morse.cmd.ifc.Weak;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.net.MessageDelegatorImpl;
import de.mhu.com.morse.usr.Session;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class LinCmd extends AfPlugin {

	private IChannelProvider provider;
	public IMessageDelegator delegator;

	public LinCmd( IMessageDelegator nextDelegator ) {
		delegator = nextDelegator;
	}
	
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
		sysMd.registerCommand( new IntLinCmd() );
		sysMd.registerCommand( new IntServiceCmd() );
		sysMd.registerCommand( new IntAuthCmd() );
		provider = (IChannelProvider)getSinglePpi( IChannelProvider.class );
	}

	class IntServiceCmd extends Command {
		
		public IntServiceCmd() {
			super("srv");
		}
	
		public void doAction(IMessage msg, Weak weak) throws Exception {
			
			String ql = "SELECT target FROM m_service WHERE name = '" + msg.getString( 0 ) + "' @sys";
			Query query = new Query( provider.getDefaultConnection(), ql );
			IQueryResult result = query.execute();
			
			IMessage ret = msg.getClient().createMessage();
			
			while ( result.next() ) {
				ret.append( result.getString( "target" ) );
			}
			result.close();
			
			replayToUser(weak, ret );
		}

	}
	
	class IntAuthCmd extends Command {

		public IntAuthCmd() {
			super("auth");
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			Object uo = weak.getClient().getUserObject();
			if ( uo == null ) {
				weak.getClient().setUserObject( new AuthControl( provider.getDefaultConnection(), msg.getString( 0 ) ) );
				uo = weak.getClient().getUserObject();
			} else {
				((AuthControl)uo).setAnswer( msg.getByteArray( 0 ) );
			}
			AuthControl auth = (AuthControl)uo;
			IMessage ret = msg.getClient().createMessage();
			ret.append( auth.isAllowed() ? 1 : 0 );
			ret.append( auth.isFinished() ? 1 : 0 );
			ret.append( auth.getQuestion() );
			replayToUser(weak, ret );
			
		}
	}
	
	class IntLinCmd extends Command {

		public IntLinCmd() {
			super("lin");
		}

		public void doAction(IMessage msg, Weak weak) throws Exception {
			
			Object uo = weak.getClient().getUserObject();
			if ( uo == null || ! ( uo instanceof AuthControl ) || !((AuthControl)uo).isAllowed() ) {
				IMessage ret = msg.getClient().createMessage();
				ret.append( 0 );
				replayToUser(weak, ret );
			}
			
			AuthControl auth = (AuthControl)uo;
			
			String ql = "SELECT * FROM m_user WHERE login_name = '" + auth.getLoginName() + "' @" + auth.getUserChannelName();
			Query query = new Query( provider.getDefaultConnection(), ql );
			IQueryResult result = query.execute();
			
			IMessage ret = msg.getClient().createMessage();
			
			if ( result.next() ) {
				Session session = new Session( provider, new UserInformation( provider.getDefaultConnection(), result ), weak.getClient() );
				msg.getClient().setUserObject( session );
				msg.getClient().setDelegator( delegator );
				ret.append( 1 );
			} else {
				ret.append( 0 );
			}
			result.close();
			
			replayToUser(weak, ret );
		}
		
	}

}
