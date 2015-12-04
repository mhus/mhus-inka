package de.mhu.com.morse.utils;

import de.mhu.com.morse.aco.AcoAcl;
import de.mhu.com.morse.aco.AcoBoolean;
import de.mhu.com.morse.aco.AcoDate;
import de.mhu.com.morse.aco.AcoDouble;
import de.mhu.com.morse.aco.AcoEnum;
import de.mhu.com.morse.aco.AcoInt;
import de.mhu.com.morse.aco.AcoLong;
import de.mhu.com.morse.aco.AcoMId;
import de.mhu.com.morse.aco.AcoString;
import de.mhu.com.morse.aco.AcoTable;
import de.mhu.com.morse.aco.IAco;
import de.mhu.com.morse.channel.IChannel;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.types.IAttribute;

public class AcoUtil {

	public static IAco getAco(IConnection con, int type, String acoName) throws MorseException {
		
		if ( acoName == null || acoName.length() == 0 ) {
			switch ( type ) {
			case IAttribute.AT_STRING:
				return new AcoString();
			case IAttribute.AT_INT:
				return new AcoInt();
			case IAttribute.AT_LONG:
				return new AcoLong();
			case IAttribute.AT_ID:
				return new AcoMId();
			case IAttribute.AT_BOOLEAN:
				return new AcoBoolean();
			case IAttribute.AT_DOUBLE:
				return new AcoDouble();
			case IAttribute.AT_ACL:
				return new AcoAcl();
			case IAttribute.AT_DATE:
				return new AcoDate();
			case IAttribute.AT_TABLE:
				return new AcoTable();
			}
		}

		if ( IAttribute.ACO_ENUM.equals( acoName) ) {
			return new AcoEnum();
		}
		
		return (IAco)con.getServer().loadFunction( con, "aco." + acoName );
		
	}

}
