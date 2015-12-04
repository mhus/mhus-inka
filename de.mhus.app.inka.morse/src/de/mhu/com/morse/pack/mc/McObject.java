package de.mhu.com.morse.pack.mc;

import java.util.Date;

import de.mhu.com.morse.obj.BtoObject;
import de.mhu.com.morse.utils.MorseException;

public class McObject extends BtoObject {

	public void setName( String name ) throws MorseException {
		setString( "name", name );
	}
	
	public String getName() throws MorseException {
		return getString( "name" );
	}
	

	public void setModifyDate(Date date) throws MorseException {
		setDate( "mc_modified", date );
	}

	public void setCreatedDate(Date date) throws MorseException {
		setDate( "mc_created", date );
	}
	
}
