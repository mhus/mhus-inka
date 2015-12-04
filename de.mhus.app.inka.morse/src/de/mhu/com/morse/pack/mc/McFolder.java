package de.mhu.com.morse.pack.mc;

import de.mhu.com.morse.utils.MorseException;

public class McFolder extends McObject {

	public void setParent(String parentId) throws MorseException {
		setString( "mc_parent", parentId );
	}

}
