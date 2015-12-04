package de.mhu.com.morse.channel.init;

import java.io.File;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.config.Config;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.utils.Properties;

public class ClasspathLoader implements ILoader {

	private static Config config = ConfigManager.getConfig( "server" );
	private String path;

	public void fill(String type, String id, Properties value)
			throws MorseException {
		try {
			value.load( this.getClass().getClassLoader().getResourceAsStream( path + "/" + type + "/" + id + ".txt" ) );
		} catch ( Throwable t ) {
			throw new MorseException( MorseException.ERROR, t );
		}
	}

	public LinkedList<String> getIds(String type) throws MorseException {
		Scanner scanner = new Scanner( this.getClass().getClassLoader().getResourceAsStream( path + "/" + type + "/index" ) );
		LinkedList<String> out = new LinkedList<String>();
		try {
			while ( true ) {
				String next = scanner.next();
				if ( next != null && next.trim().length() != 0 && next.endsWith( ".txt" ) )
					out.add( next.substring( 0, 32 ) );
			}
		} catch ( NoSuchElementException se ) {}
		return out;
	}

	public LinkedList<String> getTypes() throws MorseException {
		Scanner scanner = new Scanner( this.getClass().getClassLoader().getResourceAsStream( path + "/index" ) );
		LinkedList<String> out = new LinkedList<String>();
		try {
			while ( true ) {
				String next = scanner.next();
				if ( next != null && next.trim().length() != 0 )
					out.add( next );
			}
		} catch ( NoSuchElementException se ) {}
		return out;
	}

	public void init(String schema) throws MorseException {
		path = config.getProperty( "init.schema." + schema + ".path" );
	}

}
