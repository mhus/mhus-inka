package de.mhu.com.morse.btc;


import java.util.List;
import java.util.Set;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IChannelProvider;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.sql.SqlDriver;
import de.mhu.com.morse.channel.sql.Table;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.ITable;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.obj.MObject;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public abstract class Btc extends MObject {

	private static AL log = new AL( Btc.class );
	
	protected UserInformation user;
	protected ITypes types;
	protected IAclManager aclManager;
	protected IConnectionServer connection;

	public final void initObject( IType type, IConnectionServer pConnection, ITypes pTypes, UserInformation pUser, IAclManager pAclManager ) throws MorseException {
		setType( type );
		user = pUser;
		connection = pConnection;
		types = pTypes;
		aclManager = pAclManager;
	}
	
	public final void initObject( IType type, IConnectionServer pConnection, IQueryResult obj, ITypes pTypes, UserInformation pUser, IAclManager pAclManager ) throws MorseException {
		initObject(type, pConnection, pTypes, pUser, pAclManager);
		loadData( obj );
	}
	
	public final void loadData( IQueryResult obj ) throws MorseException {
		if ( obj != null ) {
			for ( int i = 0; i < obj.getAttributeCount(); i++ ) {
				IAttribute attr = obj.getAttribute( i );
				if ( attr.isTable() ) {
					ITable d = getTable( attr.getName() );
					ITableRead s = obj.getTable( i );
					while ( s.next() ) {
						d.createRow();
						for ( int j = 0; j < s.getAttributeCount(); j++ )
							try {
								d.setString( j, s.getString( j ) );
							} catch ( Exception e ) {
								log.warn( e );
							}
						d.appendRow();
					}
					s.close();
				} else {
					try {
						setString( attr.getName(), obj.getString( attr.getName() ) );
					} catch ( Exception e ) {
						log.warn( e );
					}
				}
			}
			cleanUp();
		}
	}
	
	public abstract void doInsertCheck() throws MorseException;

	public abstract void doSaveContent(long size) throws MorseException;

	public abstract void doUpdate() throws MorseException;

	public abstract void doDelete()  throws MorseException;

	public abstract IQueryResult loadRendition(int index, Set<String> sharedChannels) throws MorseException;

	public abstract IQueryResult createRendition(int index, String format) throws MorseException;

	public abstract void insertRendition( int index, String format, String contentId, long size ) throws MorseException;
	
	public abstract String deleteRendition(int index) throws MorseException;

	public boolean needSqlHint(int hintSize, String[] hints, Table table, SqlDriver driver) {
		return false;
	}

	public String getSqlHint(int hintSize, String[] hints, Table table, SqlDriver driver ) {
		return null;
	}

	public void doInsert(String newId) {
		// TODO Auto-generated method stub
		
	}
	
}
