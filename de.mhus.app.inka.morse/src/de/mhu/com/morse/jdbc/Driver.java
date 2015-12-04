package de.mhu.com.morse.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import de.mhu.com.morse.utils.Version;

public class Driver implements java.sql.Driver {

	public Driver()
    throws SQLException
{
}

static 
{
    try
    {
        DriverManager.registerDriver(new Driver());
    }
    catch(SQLException E)
    {
        throw new RuntimeException("Can't register driver!");
    }
}
	public boolean acceptsURL(String url) throws SQLException {
		return parseUrl( url ) != null;
	}
	
	private Properties parseUrl( String url ) {
		if ( url == null )
			return null;
		if ( ! url.toLowerCase().startsWith( ( "jdbc:morse:" ) ) )
			return null;
		
		Properties prop = new Properties();
		String[] parts = url.split( ":" );
		if ( parts.length < 3 )
			return null;
		if ( parts[2].startsWith( "@" ) ) {
			prop.put( "host"   , parts[2].substring( 1 ) );
			if ( parts.length > 3 ) prop.put( "port"   , parts[3] );
			if ( parts.length > 4 ) prop.put( "service", parts[4] );
			if ( parts.length > 5 ) prop.put( "user", parts[5] );
		} else {
			for ( int i = 2; i < parts.length; i++ ) {
				int pos = parts[i].indexOf('=');
				if ( pos < 1 )
					return null;
				prop.put( parts[i].substring( 0, pos ).toLowerCase(), parts[i].substring( i+1 ) );
			}
		}
		
		if ( !prop.containsKey( "host") )
			return null;
		if ( !prop.containsKey( "port") )
			prop.put( "port", "6666" );
		else
			prop.put( "port", String.valueOf( Integer.parseInt( prop.getProperty("port" ) ) ) );
		
		return prop;
	}

	public Connection connect(String url, Properties info) throws SQLException {
		Properties prop = parseUrl( url );
		if ( prop == null )
			throw new SQLException( "" );
		return null;
	}

	public int getMajorVersion() {
		return Version.MAJOR_VERSION;
	}

	public int getMinorVersion() {
		return Version.MINOR_VERSION;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return null;
	}

	public boolean jdbcCompliant() {
		return false;
	}

}
