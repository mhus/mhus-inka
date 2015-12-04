package de.mhu.com.morse.utils;

import java.util.Iterator;
import java.util.List;

import de.mhu.com.morse.channel.IChannelDriver;
import de.mhu.com.morse.types.IType;

public class TypesUtil {

	public static boolean isInChannel( List channels, IChannelDriver driver) {
		boolean out = false;
		for ( Iterator i = channels.iterator(); i.hasNext(); ) {
			String def = (String)i.next();
			if ( def.equals( "*" ) )
				out = true;
			else
			if ( def.equals( driver.getType() ) )
				return true;
			else
			if ( def.equals( "@" + driver.getName() ) )
				return true;
			else
			if ( def.equals( "!@" + driver.getName() ) )
				return false;
			else
			if ( def.equals( "!" + driver.getType() ) )
				return false;
		}
		return out;
	}

}
