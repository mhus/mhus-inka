package de.mhu.com.morse.btc;

import java.util.List;
import java.util.Set;

import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.MorseException;

public class ObjectBtc extends Btc {

	@Override
	public void doInsertCheck() throws MorseException {
		setString( IAttribute.M_TYPE, type.getName() );
		setString( IAttribute.M_ACL, aclManager.getNewObjectAcl( user, getType() ) );
	}

	@Override
	public void doUpdate() throws MorseException {
		String lock = getString( IAttribute.M_LOCK );
		if ( lock != null && lock.length() != 0 && user != null && ! lock.equals( user.getUserId() ) )
			throw new MorseException( MorseException.OBJECT_IS_LOCKED, getObjectId() );
		setLong( IAttribute.M_STAMP, getLong( IAttribute.M_STAMP ) + 1 );
	}

	@Override
	public void doDelete() throws MorseException {
		String lock = getString( IAttribute.M_LOCK );
		if ( lock != null && lock.length() != 0 && user != null && ! lock.equals( user.getUserId() ) )
			throw new MorseException( MorseException.OBJECT_IS_LOCKED, getObjectId() );
	}

	@Override
	public void doSaveContent(long size) throws MorseException {
		String lock = getString( IAttribute.M_LOCK );
		if ( lock != null && lock.length() != 0 && user != null && ! lock.equals( user.getUserId() ) )
			throw new MorseException( MorseException.OBJECT_IS_LOCKED, getObjectId() );
	}

	@Override
	public IQueryResult createRendition(int index, String format) throws MorseException {
		String lock = getString( IAttribute.M_LOCK );
		if ( lock != null && lock.length() != 0 && user != null && ! lock.equals( user.getUserId() ) )
			throw new MorseException( MorseException.OBJECT_IS_LOCKED, getObjectId() );
		return null;
	}
	
	@Override
	public void insertRendition( int index, String format, String contentId, long size ) throws MorseException {
		String lock = getString( IAttribute.M_LOCK );
		if ( lock != null && lock.length() != 0 && user != null && ! lock.equals( user.getUserId() ) )
			throw new MorseException( MorseException.OBJECT_IS_LOCKED, getObjectId() );		
	}
	
	@Override
	public String deleteRendition(int index) throws MorseException {
		String lock = getString( IAttribute.M_LOCK );
		if ( lock != null && lock.length() != 0 && user != null && ! lock.equals( user.getUserId() ) )
			throw new MorseException( MorseException.OBJECT_IS_LOCKED, getObjectId() );
		return null;
	}

	@Override
	public IQueryResult loadRendition(int index, Set<String> sharedChannels ) throws MorseException {
		return null;
	}

}
