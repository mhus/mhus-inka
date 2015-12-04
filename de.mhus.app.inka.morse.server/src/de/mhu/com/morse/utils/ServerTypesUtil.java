package de.mhu.com.morse.utils;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.btc.Btc;
import de.mhu.com.morse.btc.ObjectBtc;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.pack.mc.McObjectBtc;

public class ServerTypesUtil {

	private static AL log = new AL( ServerTypesUtil.class );
	
	public static Btc createBtc( IConnection con, IType type ) {
		IType t = type;
		
		if ( "m_function".equals( t.getName() ) )
			return new ObjectBtc();
		
		while ( t != null ) {
			try {
				Btc btc = (Btc)con.getServer().loadFunction( con , "btc." + t.getName() );
				if ( btc != null )
					return  btc;
			} catch (Throwable e) {
				if ( log.t1() )
					log.error( e.toString() );
			}
			t = t.getSuperType();
		}
		return new ObjectBtc();
	}
	
}
