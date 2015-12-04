package de.mhu.com.morse.mql;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import de.mhu.lib.utils.Properties;



public class PropertyQueryDefinition extends Properties implements
		IQueryDefinition {

	public int getConstantId(String name) {
		// System.out.println( "ASK CONST: " + name );
		return Integer.parseInt( getProperty( name, "-1" ) );
	}

	public String getQueryDefinition(String in) {
		// System.out.println( "ASK QUERY: " + in );
		return getProperty( "" + in, "" );
	}

}
