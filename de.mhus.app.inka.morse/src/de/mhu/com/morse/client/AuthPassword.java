package de.mhu.com.morse.client;

import de.mhu.lib.APassword;

public class AuthPassword implements IAuthHandler {

	private byte[] pass;

	public AuthPassword( String pPass ) throws Exception {
		pass = encrypt( APassword.decode( pPass ) );
	}
	
	public byte[] question(byte[] question) {
		return pass;
	}

	public static byte[] encrypt(String x)   throws Exception
	  {
	     java.security.MessageDigest d =null;
	     d = java.security.MessageDigest.getInstance("SHA-1");
	     d.reset();
	     d.update(x.getBytes());
	     return  d.digest();
	  }

}
