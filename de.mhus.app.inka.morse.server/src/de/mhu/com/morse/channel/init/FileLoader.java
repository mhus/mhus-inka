package de.mhu.com.morse.channel.init;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.log.AL;
import de.mhu.lib.utils.Properties;

public class FileLoader implements ILoader {

	private static Config config = ConfigManager.getConfig( "server" );
	private static Config initConfig = ConfigManager.getConfig( "init" );
	private static AL log = new AL( FileLoader.class );
	
	private String schema;
	private File path;

	public void fill(String type, String id, Properties value)
			throws MorseException {

		try {
			File f = new File( path, type + '/' + id + ".txt" );
			FileInputStream fis = new FileInputStream( f );
			value.load( fis );
			fis.close();
			
			if ( config.getProperty( "init.parsevalues", true ) ) {
				Object[] list = value.keySet().toArray();
				for ( int i = 0; i < list.length; i++ )
					value.setProperty( (String)list[i], parseValue( value.getProperty( (String)list[i]) ) );
			}
			
		} catch ( Throwable t ) {
			throw new MorseException( MorseException.ERROR, t );
		}
		
	}

	private String parseValue(String v ) {
		
		if ( v == null || v.indexOf( "$$" ) < 0 ) return v;
		
		String[] parts = v.split( "\\$\\$" );
		StringBuffer out = new StringBuffer();
		for ( int i = 0; i < parts.length; i++ ) {
			if ( i % 2 == 0 ) {
				out.append( parts[i] );
			} else {
				if ( parts[i].length() == 0 ) {
					out.append( "$$" );
				} else {
					//String v2 = System.getenv( parts[i] );
					String v2 = initConfig.getProperty( parts[i] );
					if ( v2 == null )
						v2 = System.getenv( parts[i] );
					if ( v2 == null )
						log.warn( "Value not found: " + parts[i] );
					else
						out.append( v2 );
				}
			}
		}
		return out.toString();
	}

	public LinkedList<String> getIds(String type) throws MorseException {
		
		File s = new File( path, type );
		File[] list = s.listFiles();
		LinkedList<String> out = new LinkedList<String>();
		for ( File f : list ) {
			if ( f.isFile() && f.getName().endsWith( ".txt" ) ) {
				out.add( f.getName().substring( 0, 32 ) );
			}
		}
		
		return out;
	}

	public LinkedList<String> getTypes() throws MorseException {

		File[] list = path.listFiles();
		LinkedList<String> out = new LinkedList<String>();
		for ( File f : list ) {
			if ( f.isDirectory() && !f.getName().startsWith( "." ) ) {
				out.add( f.getName() );
			}
		}
		return out;
	}

	public void init(String pSchema) throws MorseException {
		schema = pSchema;
		path = new File( config.getProperty( "init.schema." + schema + ".path" ) );
	}

}
