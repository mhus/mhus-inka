package de.mhu.com.morse.aaa;

import java.util.Hashtable;

import de.mhu.lib.AMath;
import de.mhu.com.morse.cache.MemoryCache;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPlugin;
import de.mhu.lib.plugin.AfPluginException;

public class AclManager extends AfPlugin implements IAclManager {

	// TODO: clean cache periodically .... !!!! MeMoRy !!!! use java.lang.ref
	
	private static final int BIT_READ=0;
	private static final int BIT_WRITE=1;
	private static final int BIT_CREATE = 2;
	private static final int BIT_DELETE = 3;
	private static final int BIT_GRANT = 4;
	private static final int BIT_ADMIN = 6;
	private static final int BIT_VERSION = 5;
	private static final int BIT_SAVE = 8;
	private static final int BIT_LOAD = 7;
	private static final int BIT_EXEC = 9;	
	
	private IChannelProvider channels;
	private IChannel channel;
	private IConnection connection;
	private MemoryCache<String, Boolean> cache = new MemoryCache<String, Boolean>( "acl", 60 * 1000, true );

	@Override
	protected void apDestroy() throws Exception {
		
	}

	@Override
	protected void apDisable() throws AfPluginException {
		
	}

	@Override
	protected void apEnable() throws AfPluginException {
		// types = (ITypes)getSinglePpi( ITypes.class );
		channels = (IChannelProvider)getSinglePpi( IChannelProvider.class );
		connection = channels.createConnection();
		channel = connection.getChannel( "sys" );
	}

	@Override
	protected void apInit() throws Exception {
		appendPpi( IAclManager.class, this );
	}

	public boolean hasAdmin(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_ADMIN );
	}

	public boolean hasCreate(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_CREATE );
	}

	public boolean hasDelete(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_DELETE );
	}

	public boolean hasGrant(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_GRANT );
	}

	public boolean hasRead(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_READ );
	}

	public boolean hasVersion(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_VERSION );
	}

	public boolean hasSave(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_SAVE );
	}
	
	public boolean hasLoad(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_LOAD );
	}
	
	public boolean hasExec(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_EXEC );
	}
	
	public boolean hasWrite(UserInformation user, String acl) {
		return hasRight( user, acl, BIT_WRITE );
	}

	private synchronized boolean hasRight( UserInformation user, String acl, int bit ) {
		
		if ( user == null || acl == null || acl.length() == 0 ) return true;
		
		if ( user.isAdministrator() ) return true;
		
		boolean out = false;
		
		Boolean rc = cache.get( user.getUserId() + '*' + acl + '*' + bit );
		if ( rc != null )
			return rc;
		
		try {
			IQueryResult res = channel.query( new Query( connection, "SELECT sensitivity,permit FROM " + IType.TYPE_ACL + " WHERE NAME='" + acl + "' @sys" ) );
			if ( res.next() ) {
				
				int aSens = res.getInteger( "sensitivity" );
				ITableRead permit = res.getTable( "permit" );
				
				if ( aSens > user.getSensivity() ) {
					out = false;
				} else {
				
					while ( permit.next() ) {
						String subject = permit.getString( "SUBJECT" );
						if ( user.hasSubject( subject ) ) {
							if ( ! out ) out = AMath.getBit( permit.getLong( "RIGHTS" ), bit );
						}
					}
					
				}				
			} else {
				out = false;
			}
			res.close();
		} catch (Exception e) {
			log().error( e );
		}
		cache.put( user.getUserId() + '*' + acl + '*' + bit, out );
		return out;
	}
	
	

	public String getNewObjectAcl(UserInformation user, IType type ) {
		if ( user == null ) return IAclManager.ADMINISTRATOR;
		return user.getDefaultAcl();
	}

	public String getNewContentAcl(UserInformation user, IType type ) {
		return IAclManager.ADMINISTRATOR;
	}

	public boolean isAdministrator(UserInformation user) {
		if ( user == null ) return true;
		return user.isAdministrator();
	}
	
}
