package de.dctm.bridge;

import java.util.Hashtable;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;

import de.mhu.lib.ASql;

public class DMConnection  {
	
	public static IDfClientX clientx = new DfClientX();
	private static Hashtable connections = new Hashtable();
	
	private IDfSessionManager sMgr = null;
	private IDfSession session = null;
	private IDfClient client = null;
	private String sessionId;
	private String userName;
	private String host;
	private int port;
	private String userPass;
	
	public DMConnection( String pUser, String pPass, String pDocBase ) throws DfException {
				
        try {
  
        	userName = pUser;
        	userPass = pPass;
        	
        	client = clientx.getLocalClient( );
        	IDfTypedObject config = client.getClientConfig();
        	host = config.getString ("primary_host" );
        	port = config.getInt("primary_port" );
        	
//        	create a Session Manager object
            sMgr = client.newSessionManager();
            
//          create an IDfLoginInfo object named loginInfoObj
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser(pUser);
            loginInfoObj.setPassword(pPass);
            loginInfoObj.setDomain(null);
            
//			bind the Session Manager to the login info
            sMgr.setIdentity(pDocBase, loginInfoObj);
            session = sMgr.newSession(pDocBase);
            
//            System.out.println( "Session ID: " + session.getSessionId() );
 
            sessionId = session.getSessionId();
            connections.put( sessionId, this );
            
        } catch( DfException e ) {
        	e.printStackTrace();
        	disconnect();
        	throw e;
        }
	}
	
	
	public void disconnect() {
		if ( isConnected() ) {
			connections.remove( sessionId );
			sMgr.release(session);
			sMgr = null;
			session = null;
		}
	}
	
	public boolean isConnected() {
		return ( sMgr != null && session != null );
	}
	
	public IDfSession getSession() {
		return session;
	}

	public IDfCollection getChilds(IDfId id) throws Exception {
		IDfPersistentObject obj = session.getObject( id );
		if ( ! ( obj instanceof IDfFolder ) ) {
			//throw new Exception( "Object is not folder or cabinet: " + id );
			return null;
		}
		IDfFolder folder = (IDfFolder) obj;
        if( folder == null ) {
            throw new Exception( "Folder or cabinet does not exist in the Docbase: " + id );
        }
        
        // Get the folder contents
        return folder.getContents("object_name,r_object_type,r_object_id");
	}
	
	public IDfSysObject getExistingObject ( String strObjId ) throws DfException {
		if ( strObjId == null ) return null;
        IDfId myId = clientx.getId(strObjId);
		return getExistingObject( myId );
	}
	
	public IDfSysObject getExistingObject ( IDfId myId ) throws DfException {
		if ( myId == null ) return null;
        IDfSysObject sysObj = (IDfSysObject)session.getObject(myId);
        if( sysObj == null ) {
//            System.out.println("--- Object can not be found: " + myId );
            return null;
        } else {
//            System.out.println("--- Object named '" + sysObj.getObjectName() + "' was found.");
            return sysObj;
        }
    }
	
	public IDfSysObject getExistingObject ( String path, String lang ) throws DfException {
		int pos = path.lastIndexOf( '/' );
		
		String dql = null;
		if ( pos <= 0 ) {
			dql = "SELECT r_object_id FROM dm_cabinet WHERE object_name='" + ASql.escape( path.substring( 1 ) ) + "' AND language_code='" + ASql.escape( lang ) + "'";
		} else {
			String folder = path.substring( 0, pos );
			String name   = path.substring( pos+1 );
			dql = "SELECT r_object_id FROM dm_sysobject WHERE FOLDER('" + ASql.escape( folder ) + "') AND object_name='" + ASql.escape( name ) + "' AND language_code='" + ASql.escape( lang ) + "'";
		}
//System.out.println( dql );			
		IDfQuery query = createQuery( dql );
		IDfCollection res = query.execute( getSession(), IDfQuery.EXEC_QUERY );
		IDfId objectId = null;
		if ( res.next() )
			objectId = res.getId( "r_object_id" );
		res.close();
		
		return getExistingObject( objectId );
		
	}
	
	public IDfPersistentObject getPersistentObject ( String strObjId ) throws DfException {
        IDfId myId = clientx.getId(strObjId);
		return getPersistentObject( myId );
	}
	public IDfPersistentObject getPersistentObject ( IDfId myId ) throws DfException {
		IDfPersistentObject sysObj = (IDfPersistentObject)session.getObject(myId);
        if( sysObj == null ) {
            System.out.println("--- Object can not be found: " + myId );
            return null;
        } else {
//            System.out.println("--- Object named '" + sysObj.getObjectId() + "' was found.");
            return sysObj;
        }
    }
	
	public IDfQuery createQuery( String dql ) {
		IDfQuery query = clientx.getQuery();
		if ( dql != null ) query.setDQL( dql );
		return query;
	}


	public static DMConnection findConnection(String dctmSession) {
		return (DMConnection)connections.get( dctmSession );
	}


	public String getUserName() {
		return userName;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getDocbaseName() throws DfException {
		return getSession().getDocbaseName() + '@' + getHost() + ':' + getPort();
	}
	
	public static String escape( String in ) {
		if ( in == null ) return null;
		if ( in.indexOf( '\'') < 0 ) return in;
		return in.replaceAll( "'", "''" );
	}


	public DMConnection cloneConnection() throws DfException {
		System.out.println( "!!! Create DMConnection clone !!!");
		return new DMConnection( userName, userPass, getSession().getDocbaseName() );
	}

	public static void setDocbroker( String host, int port ) throws DfException {
		IDfTypedObject config;
		IDfClient client = clientx.getLocalClient( );
		config = client.getClientConfig();
		config.removeAll( "backup_host" );
		config.removeAll( "backup_port" );
		config.removeAll( "backup_service" );
		config.removeAll( "backup_timeout" );
		config.setString ("primary_host", host);
		config.setInt("primary_port", port);
			
	}

}
