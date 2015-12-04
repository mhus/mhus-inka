package de.mhu.com.morse.utils;

import de.mhu.lib.log.AL;

public class Version {
	// private static AL log = new AL(Version.class);
	
	public static final int MAJOR_VERSION=1;
	public static final int MINOR_VERSION=1;
	public static final String VENDOR="Mike Hummel";
	public static final String LICENCE = "Non Public 2007";
	public static final String PRODUCT = "MORSE";
	
	public static final String VERSION = String.valueOf( MAJOR_VERSION ) + '.' + String.valueOf( MINOR_VERSION ); 
	public static final boolean IS_DEVELOPER = ( MINOR_VERSION % 2 == 1 );
	public static final String COPYRIGHT = PRODUCT + ' ' + VERSION + ( IS_DEVELOPER ? "(dev)" : "" ) + " (c) " + LICENCE + ' ' + VENDOR;
		
}
