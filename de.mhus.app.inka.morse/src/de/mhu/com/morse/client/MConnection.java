package de.mhu.com.morse.client;

import java.io.IOException;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.cmd.ISingleCmd;
import de.mhu.com.morse.mql.IQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.net.Client;
import de.mhu.com.morse.net.Connection;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.net.Client.Listener;
import de.mhu.com.morse.obj.BtoObject;
import de.mhu.com.morse.obj.IBtoAutoExtension;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.AfPluginNode;
import de.mhu.lib.plugin.AfPluginRoot;
import de.mhu.lib.plugin.utils.ALLogger;
import de.mhu.lib.plugin.utils.XmlConfig;

public abstract class MConnection {

	public static final int HOST = 1;
	public static final int PORT = 2;
	
	private static AL log = new AL( MConnection.class );
	private MSession defaultSession;
	private String loginName;
	private String serviceName;
	protected MTypes types;
	private IAuthHandler authHandler;

	public MConnection() {
		defaultSession = new MSession( this, null );
	}

	public MSession getSession() {
		return defaultSession;
	}
	
	public abstract void connect() throws Exception;
	
	public abstract boolean isConnected();
	
	public abstract void close();
	
	protected void login() throws MorseException {
		
		IMessage msg = createMessage();
		msg.append( "auth" );
		msg.append( loginName );
		IMessage ret = sendAndWait( msg, 60000 );
		int allowed  = ret.shiftInteger();
		int finished = ret.shiftInteger();
		byte[] question = ret.shiftByteArray();
		
		while ( allowed == 0 ) {
			if ( finished == 1)
				throw new MorseException( MorseException.ACCESS_DENIED );
			byte[] answer = authHandler.question( question );
			msg = createMessage();
			msg.append( "auth" );
			msg.append( answer );
			ret = sendAndWait( msg, 60000 );
			allowed  = ret.shiftInteger();
			finished = ret.shiftInteger();
			question = ret.shiftByteArray();
		}
		
		msg = createMessage();
		msg.append( "lin" );
		
		ret = sendAndWait( msg, 60000 );

		if ( ret.getInteger( 0 ) == 0 )
			throw new MorseException( MorseException.ACCESS_DENIED );
		
	}
	
	protected String service() throws MorseException {
		
		IMessage msg = createMessage();
		msg.append( "srv" );
		msg.append( serviceName );
		
		IMessage ret = sendAndWait( msg, 60000 );

		for ( int i = 0; i < ret.getCount(); i++ ) {
			if ( "current".equals( ret.getString( i ) ) )
				return null;
		}
		
		if ( ret.getCount() > 0 )
			return ret.getString( 0 );
		
		throw new MorseException( MorseException.ERROR, "Service not found" );
		
	}

	public abstract IMessage createMessage() throws MorseException;

	public abstract void sendMessage(IMessage msg) throws MorseException;

	public abstract IMessage sendAndWait(IMessage msg, long timeout ) throws MorseException;

	public void setAuth(String pLoginName, IAuthHandler handler ) {
		loginName = pLoginName;
		authHandler = handler;
	}

	public void setService(String in) {
		serviceName = in;
	}
	
	public ITypes getTypeModel() {
		return types;
	}




	public BtoObject createObject(String type) throws MorseException {
		IType t = getTypeModel().get( type );
		if ( t == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, type );
		
		IType x = t;
		BtoObject obj = null;
		while ( x != null ) {
			obj = (BtoObject)getDefaultConnection().getServer().loadFunction( getDefaultConnection(), "bto." + x.getName() );
			if ( obj != null ) break;
			x = x.getSuperType();
		}
		if ( obj == null )
			obj = new BtoObject();
		
		obj.setType( t );
		return obj;
	}

	public BtoObject loadObject( IConnection con, String id ) throws MorseException {
		ObjectUtil.assetId( id );
		
		IQueryResult res = new Query( con, "FETCH " + id ).execute();
		if ( ! res.next() ) {
			res.close();
			return null;
		}
		String type = res.getString( IAttribute.M_TYPE );
		IType t = getTypeModel().get( type );
		if ( t == null )
			throw new MorseException( MorseException.TYPE_NOT_FOUND, type );
		
		IType x = t;
		BtoObject obj = null;
		while ( x != null ) {
			obj = (BtoObject)getDefaultConnection().getServer().loadFunction( getDefaultConnection(), "bto." + x.getName() );
			if ( obj != null ) break;
			x = x.getSuperType();
		}
		if ( obj == null )
			obj = new BtoObject();
		
		if ( obj instanceof IBtoAutoExtension ) {
			obj = ((IBtoAutoExtension)obj).autoExtend( t, res );
		}
		
		obj.setType( t );
		obj.loadData( res );
		res.close();
		
		return obj;

	}



	public IConnection getDefaultConnection() {
		return getSession().getDbProvider().getDefaultConnection();
	}

	public abstract void setProperty(int key, String value );
	
	public abstract void setProperty(int key, int value );
	
}
