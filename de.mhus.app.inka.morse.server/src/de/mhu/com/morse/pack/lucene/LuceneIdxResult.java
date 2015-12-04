package de.mhu.com.morse.pack.lucene;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.AbstractSelectResult;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public class LuceneIdxResult extends AbstractSelectResult {

	private Iterator<Hit> iterator;
	private Hits hits;
	private LinkedList<String> attributes;
	private IAclManager aclManager;
	private UserInformation user;
	private Hit nextElement;

	public LuceneIdxResult(Hits phits, LinkedList<String> pattributes, IAclManager paclManager, UserInformation puser) throws Exception {
		hits = phits;
		attributes = pattributes;
		aclManager = paclManager;
		user = puser;
		
		reset();
	}

	private void findNext() throws CorruptIndexException, IOException {
		if ( iterator == null ) {
			nextElement = null;
			return;
		}
		while ( iterator.hasNext() ) {
			nextElement = iterator.next();
			// acl check is done by AclFilter
			// if ( aclManager.hasRead( user, nextElement.get( IAttribute.M_ACL ) ) )
				return;
		}
		iterator = null;
		nextElement = null;
	}

	@Override
	public int getPreferedQuereType() {
		return QUEUE_FETCH;
	}

	@Override
	public void close() {
		iterator = null;
		nextElement = null;
		hits = null;
	}

	@Override
	public IAttribute getAttribute(String name) throws MorseException {
		return IAttributeDefault.ATTR_OBJ_STRING;
	}

	@Override
	public IAttribute getAttribute(int index) throws MorseException {
		return IAttributeDefault.ATTR_OBJ_STRING;
	}

	@Override
	public int getAttributeCount() {
		return attributes.size();
	}

	@Override
	public String[] getColumns() throws MorseException {
		return attributes.toArray( new String[ attributes.size() ] );
	}

	@Override
	public String getRawString(String name) throws MorseException {
		try {
			if ( "_score".equals( name ) )
				return String.valueOf( nextElement.getScore() );
			if ( "_boost".equals( name ) )
				return String.valueOf( nextElement.getBoost() );
			if ( "_id".equals( name ) )
				return String.valueOf( nextElement.getId() );

			return nextElement.get( name );
		} catch ( Exception e ) {
			throw new MorseException( MorseException.ERROR, e );
		}
	}

	@Override
	public String getRawString(int index) throws MorseException {
		return getRawString( attributes.get( index ) );
	}

	@Override
	public ITableRead getTable(String name) throws MorseException {
		return null;
	}

	@Override
	public ITableRead getTable(int index) throws MorseException {
		return null;
	}

	@Override
	public boolean next() throws MorseException {
		try {
			findNext();
		} catch (Exception e) {
			throw new MorseException( MorseException.ERROR, e );
		}
		return nextElement != null;
	}

	public InputStream getInputStream() throws MorseException {
		return null;
	}

	public OutputStream getOutputStream() throws MorseException {
		return null;
	}

	public boolean reset() throws MorseException {
		if ( hits == null ) return false;
		iterator = hits.iterator();
		try {
			findNext();
		} catch (Exception e) {
			throw new MorseException( MorseException.ERROR, e );
		}

		return true;
	}

}
