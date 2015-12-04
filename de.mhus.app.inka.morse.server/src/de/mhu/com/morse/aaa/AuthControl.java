package de.mhu.com.morse.aaa;

import java.util.LinkedList;
import java.util.Map;

import de.mhu.lib.ASql;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.mql.ServerQuery;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;

public class AuthControl {

	private static AL log = new AL( AuthControl.class );
	
	private String loginName;
	private String channelName;
	private boolean finished;
	private boolean allowed;
	private String method;
	private Map<String, String> attr;
	private LinkedList<Object[]> methods;
	private int toDo;
	private IConnection con;
	private IAuth methodObject;

	public AuthControl( IConnection pCon, String pLoginName ) throws MorseException {
		
		con = pCon;
		loginName = pLoginName;
		channelName = "sys";
		finished = false;
		allowed  = false;
		
		Query query = new Query( con, "SELECT auth_method,auth_attr FROM m_user WHERE login_name = '" + loginName + "' AND is_active=1 @" + channelName );
		IQueryResult result = query.execute();
		if ( ! result.next() ) {
			finished = true;
			result.close();
			if ( log.t10() )
				log.info( "User not found: " + loginName );
			return;
		}
		method = result.getString( "auth_method" );
		attr   = ObjectUtil.tableToMap( result.getTable( "auth_attr" ), "k", "v" );
		result.close();
		
		ServerQuery qMethod = new ServerQuery( con, "SELECT method FROM m_auth WHERE m_id='" + ASql.escape( method ) + "' @sys" );
		IQueryResult rMethod = qMethod.execute();
		if ( ! rMethod.next() ) {
			finished = true;
			rMethod.close();
			if ( log.t10() )
				log.info( "Method not found: " + loginName + " " + method );
			return;
		}
		methods = ObjectUtil.tableToList( rMethod.getTable( "method" ), new String[] { "function", "rule" } );
		rMethod.close();

		toDo = 0;
		getMethod();
	}

	private void getMethod() throws MorseException {
		if ( methods.size() <= toDo ) {
			finished = true;
			return;
		}
		methodObject = (IAuth)con.getServer().loadFunction( con, "auth." + methods.get( toDo )[0] );
		methodObject.init( con, attr );
	}

	public String getLoginName() {
		return loginName;
	}
	
	public String getUserChannelName() {
		return channelName;
	}

	public boolean isAllowed() {
		return allowed;
	}
	
	public boolean isFinished() {
		return finished;
	}

	public void setAnswer(byte[] string) throws MorseException {
		if ( finished ) return;
		
		methodObject.setAnswer( string );
		if ( "allow".equals( methods.get( toDo )[1] ) ) {
			if ( methodObject.isAllow() ) {
				allowed = true;
				finished = true;
				methodObject = null;
				return;
			}
			
		} else {
			if ( ! methodObject.isAllow() ) {
				allowed = false;
				finished = true;
				methodObject = null;
				return;
			}
		}
		
		toDo++;
		getMethod();
	}

	public byte[] getQuestion() {
		if ( finished ) return new byte[0];
		return methodObject.getQuestion();
	}
	
	public static byte[] encrypt(String x)   throws Exception
	  {
	     java.security.MessageDigest d =null;
	     d = java.security.MessageDigest.getInstance("SHA-1");
	     d.reset();
	     d.update(x.getBytes());
	     return  d.digest();
	  }

	
}
