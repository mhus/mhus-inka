package de.mhu.com.morse.mql;

import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.net.IMessage;

public class CompilledQueryMessage extends CompiledQuery {

	public CompilledQueryMessage( IMessage msg ) {
		int size = msg.shiftInteger();
		features = new String[ size ];
		for ( int i = 0; i < size; i++ )
			features[i] = msg.shiftString();
		
		size = msg.shiftInteger();
		for ( int i = 0; i < size; i++ ) {
			int type = msg.shiftInteger();
			if ( type == 1 )
				add( msg.shiftString() );
			else
				add( msg.shiftInteger() );
		}
		
	}

	public static void toMessage( ICompiledQuery code, IMessage msg ) {
		
		String[] f = code.getFeatures();
		if ( f == null )
			msg.append( 0 ); 
		else {
			msg.append( f.length );
			for ( int i = 0; i < f.length; i++ )
				msg.append( f[i] );
		}		
		msg.append( code.size() );
		for ( int i = 0; i < code.size(); i++ ) {
			int x = code.getInteger( i );
			if ( x == CMql.NaN ) {
				msg.append( 1 );
				msg.append( code.getString( i ) );
			} else {
				msg.append( 0 );
				msg.append( x );				
			}
			
		}
	}
}
