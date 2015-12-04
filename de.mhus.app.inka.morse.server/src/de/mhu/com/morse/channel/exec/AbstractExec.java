package de.mhu.com.morse.channel.exec;

import java.util.LinkedList;
import java.util.Map;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public abstract class AbstractExec implements IExecFunction {

	protected Map<String, String> config;
	private String accessAcl;
	protected String name;
	protected IConnectionServer connection;
	protected UserInformation user;
	protected IAclManager aclManager;

	public abstract IQueryResult exec(LinkedList<Object> attr, boolean async) throws MorseException;

	final public void initFunction(IConnectionServer pConnection, IAclManager pAclManager, UserInformation pUser) throws MorseException {
		if ( ! pAclManager.hasExec( pUser, accessAcl ) )
			throw new MorseException( MorseException.ACCESS_DENIED_EXEC, new String[] {"exec", accessAcl } );
		connection = pConnection;
		user = pUser;
		aclManager = pAclManager;
	}

	final public void initFunction(Map<String, String> pConfig, String pAccessAcl, String pName) throws MorseException {
		config = pConfig;
		accessAcl = pAccessAcl;
		name = pName;
	}

	protected String getProperty( String key, String def ) {
		String out = config.get( key );
		if ( out == null ) return def;
		return out;
	}
	
	protected String getProperty( String key ) {
		return config.get( key );
	}

	public IAclManager getAclManager() {
		return aclManager;
	}

	public IConnectionServer getConnection() {
		return connection;
	}

	public UserInformation getUser() {
		return user;
	}
	
}
