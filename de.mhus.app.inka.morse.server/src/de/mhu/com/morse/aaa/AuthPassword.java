package de.mhu.com.morse.aaa;

import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import de.mhu.lib.log.AL;
import de.mhu.com.morse.channel.IConnection;

public class AuthPassword implements IAuth {

	private static AL log = new AL( AuthPassword.class );
	
	private byte[] pass;
	private boolean allowed = false;

	public byte[] getQuestion() {
		return "password:".getBytes();
	}

	public boolean isAllow() {
		return allowed;
	}

	public void setAnswer(byte[] string) {
		allowed = true;
		if ( pass != null && string != null && pass.length == string.length ) {
			for ( int i = 0; i < pass.length; i++ )
				if ( pass[i] != string[i] ) {
					allowed = false;
					break;
				}
		} else
			allowed = false;
	}

	public void init( IConnection con, Map<String, String> attr) {
		pass = encrypt( attr.get( "password" ) );
	}
	
	public static byte[] encrypt(String x)
	  {
		if ( x == null ) return null;
	     java.security.MessageDigest d =null;
	     try {
			d = java.security.MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			log.error( e );
			return null;
		}
	     d.reset();
	     d.update(x.getBytes());
	     return  d.digest();
	  }

}
