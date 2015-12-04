package de.mhu.com.morse.eecm;

import java.net.MalformedURLException;

import de.mhu.lib.eecm.model.EcmManager;
import de.mhu.lib.eecm.model.IEcmConnection;
import de.mhu.lib.eecm.model.IEcmDriver;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.client.AuthPassword;

public class MorseDriver implements IEcmDriver {
	
	private static AL log = new AL(MorseDriver.class);

	static {
		EcmManager.registerDriver( new MorseDriver() );
	}
	
	private MorseDriver() {
		
	}
	
	public IEcmConnection connect(String url, String user, String pass)
			throws Exception {
		
		if ( !url.startsWith( "morse://" ) )
			throw new MalformedURLException( url );

		MorseConnection con = new MorseConnection();
		
		con.setLoginName( null );
		con.setPort( 6666 );
		con.setHost( "localhost" );
		con.setAuthHandler( null );
		
		String[] parts = url.split( "/", 4 );
		int pos = parts[2].indexOf( '@' );
		if ( pos >= 0 ) {
			String usr = parts[2].substring( 0, pos );
			parts[2] = parts[2].substring( pos+1 );
			pos = usr.indexOf( ':' );
			if ( pos >= 0 ) {
				con.setAuthHandler( new AuthPassword( usr.substring( pos+1 ) ) );
				usr = usr.substring( 0, pos );
			}
			con.setLoginName( usr );
		}
		pos = parts[2].indexOf( ':' );
		if ( pos >= 0 ) {
			con.setPort( Integer.parseInt( parts[2].substring( pos+1 ) ) );
			parts[2] = parts[2].substring( 0, pos );
		}
		con.setHost( parts[2] );
		
		pos = parts[3].indexOf( '?' );
		if ( pos >= 0 ) {
			parts[3] = parts[3].substring( 0, pos );
			// handle parameters
		}
		con.setService( parts[3] );

		if ( user != null && user.length() > 0 )
			con.setLoginName( user );
		
		if ( pass != null && pass.length() > 0 )
			con.setAuthHandler( new AuthPassword( pass ) );
		
		con.connect();
		
		return con;
	}

}
