package de.mhu.com.morse.channel.mql;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IChannelDriverServer;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.channel.sql.Descriptor;
import de.mhu.com.morse.channel.sql.SqlUtils;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public class WhereParser {

	public static int parse( int off, 
			ICompiledQuery code, 
			IType type, 
			IAclManager aclManager, 
			UserInformation user, 
			IChannelDriverServer driver,
			WhereDescription desc ) throws MorseException {

		return off;
		
	}
	
	public static class WhereDescription {
		
	}
}
