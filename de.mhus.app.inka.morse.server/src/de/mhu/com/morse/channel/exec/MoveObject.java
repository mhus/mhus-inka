package de.mhu.com.morse.channel.exec;

import java.util.Iterator;
import java.util.LinkedList;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IQueryFunction;
import de.mhu.com.morse.channel.ITransaction;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;

public class MoveObject implements IExec, IQueryFunction {

	private IConnectionServer con;
	private UserInformation user;
	private String newStore;
	private long cnt = 0;

	/**
	 * Move one object to another store
	 * Usage: objectid,destination store
	 */
	public Object[] execute(Exec exec, LinkedList<Object> attr) throws ExecException, MorseException {
		String id = (String)attr.get( 0 );
		user = exec.getUser();
		con  = exec.getConnection();		
		newStore = (String)attr.get( 1 );

		action( id );
		
		return null;
	}
	
	private boolean action( String id ) throws MorseException {
		
		boolean isInit = newStore.equals( IChannelDriver.C_INIT );
		
		ObjectUtil.assetId( id );
		String oldStore = con.getObjectManager().findObject( id );
		if ( oldStore.equals( newStore ) )
			return false;
		
		 IQueryResult obj = con.fetch( id, user, false ); 
		 obj.next();
		 String type = obj.getString( IAttribute.M_TYPE );
		 ITransaction tr = con.startTransaction();
		 try {
			 con.getChannel( newStore ).store( obj, false, user );
			 if ( ! isInit ) {
				 IQueryResult rDelete = new Query( con, 
						 "DELETE FROM " + type + 
						 " WHERE " + IAttribute.M_ID + "='" + id + 
						 "' `enable:-btc,-event,-commit` @" + oldStore )
				 			.execute();
				 try {
					 if ( rDelete.getErrorCode() != 0 || rDelete.getReturnCode() != 1 )
						 throw new MorseException( MorseException.ERROR );
				 } finally {
					 rDelete.close();
				 }
				 con.eventObjectDeleted( oldStore, id, type );
				 con.eventObjectCreated( newStore, id, type );
				 con.maybeCommit( tr );
				 tr = null;
			 }
		 } finally {
			 if ( tr != null ) con.maybeRollback( tr );
		 }
		return true;
	}

	public Iterator<String> getRepeatingResult(String[] attr) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public String getResult() {
		return String.valueOf( cnt );
	}

	public String getSingleResult(String[] attr) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}

	public IAttribute getType() {
		return IAttributeDefault.ATTR_OBJ_LONG;
	}

	public void initFunction(IConnectionServer con, IAclManager aclm, UserInformation user, String[] functionInit) throws MorseException {
		this.con = con;
		this.user = user;
		this.newStore = functionInit[ 0 ];
	}

	public void append( String id ) throws MorseException {
		if ( action( id ) )
			cnt++;
	}
	
	public void resetFunction() {
		cnt = 0;
	}

}
