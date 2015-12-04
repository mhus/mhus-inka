/***********************************************************
GNU Lesser General Public License

JMorseCore - Permanent Connection Messaging Service
Copyright (C) 2004-2005 Rise s.a.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
************************************************************/
package de.mhu.com.morse.usr;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.cache.MemoryCache;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;

public class UserInformation 
{
	
	private static final AL log = new AL( UserInformation.class );
	
	private MemoryCache<String,String> subjects = null;
	private String id;
	private int sensitivity;
	private boolean isAdmin;
	private IConnection con;
	
	public UserInformation(IConnection pCon, IQueryResult result) throws MorseException
	{
		con = pCon;
		id = result.getString( IAttribute.M_ID );
		sensitivity = result.getInteger( "sensitivity" );
		// find group and role ids and add to subjects
		// SELECT m_id FROM m_group WHERE user.id=' id '
		subjects = new MemoryCache<String,String>( "user_subjects_" + id, 0, false ) {
			
			public void refill() {
				try {
					put( id, "" );
					String mql = "SELECT m_id FROM m_group.user WHERE m_group.user.id='" + id + "' @sys";
					Query query = new Query( con, mql );
					IQueryResult res = query.execute();
					while ( res.next() )
						put( res.getString( IAttribute.M_ID), "" );
					res.close();
				} catch ( MorseException e ) {
					log.error( id, e );
				}
			}
		};
		subjects.refill();
		isAdmin = result.getBoolean( "administrator" );
	}

	public int getSensivity() {
		return sensitivity;
	}

	public boolean hasSubject(String subject) {
		return subjects.containsKey( subject );
	}

	public String getUserId() {		
		return id;
	}

	public String getDefaultAcl() {
		return ""; //TODO
	}

	public boolean isAdministrator() {
		return isAdmin;
	}
	
}
