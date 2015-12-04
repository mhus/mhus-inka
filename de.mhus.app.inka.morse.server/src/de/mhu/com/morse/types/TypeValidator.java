package de.mhu.com.morse.types;

import java.util.Hashtable;

import de.mhu.lib.AFile;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;

public class TypeValidator {

	private static Hashtable<String, String> restricted = new Hashtable<String, String>();
	private static Config config = ConfigManager.getConfig( "server" );
	
	static {
		String[] parts = AFile.readFile( "".getClass().getClassLoader().getResourceAsStream( config.getProperty( "resource.restricted" ) ) ).split( "\n" );
		for ( int i = 0; i < parts.length; i++ ) {
			String name = parts[i].trim().toLowerCase();
			if ( name.length() > 0 )
				restricted.put( name, name );
		}
	}
	
	public static boolean validateAttrName( String in ) {
		
		if ( in == null || in.length() == 0 || in.length() > 30 ) return false;
		char c = in.charAt( 0 );
		if ( ! (c >= 'a' && c <= 'z') )
			return false;
		for ( int i = 0; i < in.length(); i++ ) {
			c = in.charAt( i );
			if ( ! (c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' ) )
				return false;
		}
		
		return ! restricted.containsKey( in );
	}
	
	public static boolean validateTypeName( String in ) {
		
		if ( in == null || in.length() == 0 || in.length() > 20 ) return false;
		char c = in.charAt( 0 );
		if ( ! (c >= 'a' && c <= 'z') )
			return false;
		for ( int i = 0; i < in.length(); i++ ) {
			c = in.charAt( i );
			if ( ! (c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' ) )
				return false;
		}
		
		return ! restricted.containsKey( in );
	}
	
}
