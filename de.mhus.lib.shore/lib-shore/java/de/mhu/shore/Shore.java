/*
 *  mhu-shore JSP creation Framework
 *    shore help you to create JSP files using MVC (Model-View-Controll) design.
 *    mhu-shore is a ant task and generate JSP files from - nearly - simple
 *    HTML files. A special Servlet or active server component is not needed.
 * 
 *  Copyright (C) 2003  Mike Hummel
 *  
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *  
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *  
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  WWW: http://code.mikehummel.de/
 *  E-mail: code@mikehummel.de
 */

package de.mhu.shore;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.mhu.lib.MhuCast;
import de.mhu.lib.MhuFile;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Shore {

	protected final static String IDENT = "@shore.";
	
	protected static String    valueStart    = "{%";
	protected static String    valueStop     = "%}";
	
	protected static String codeStart = "<%=";
	protected static String codeStop  = "%>";
	protected static String stringAdd = "+";
	
	private Hashtable   plugins    = new Hashtable();
	private Vector      actions    = new Vector();
	
	private File        sourceDir  = null;
	private String      sourceName = null;
	private Hashtable   output     = new Hashtable();
	private File        configDir  = null;
	
	private String      content    = null;
	private String      orgContent = null;

	private Properties  parameter  = null;

	private int         uniqueId   = 0;
	
	private Hashtable	modules	   = new Hashtable();

	public Shore( File _configDir ) {
		configDir = _configDir;

		// Load plugin.properties
		Properties pl = new Properties();
		try {
			FileInputStream fis = new FileInputStream( new File( configDir, "plugins.properties" ) ); 
			pl.load( fis );
			fis.close();
		} catch ( Exception e) {
			System.err.println( "Plugin-config not found: " + e );
		} finally {		
			for ( Enumeration e = pl.keys(); e.hasMoreElements();) {
				String key = (String)e.nextElement();
				plugins.put( key, pl.getProperty( key ) );
			}
		}
		
		// load config properties
		parameter = new Properties();
		try {
			FileInputStream fis = new FileInputStream( new File( configDir, "config.properties" ) ); 
			parameter.load( fis );
			fis.close();
		} catch ( Exception e) {
		}
		
		// Set global Properties
		codeStart = parameter.getProperty( "code.start", codeStart );
		codeStop  = parameter.getProperty( "code.stop" , codeStop  );
		
		valueStart = parameter.getProperty( "value.start", valueStart );
		valueStop  = parameter.getProperty( "value.stop" , valueStop  );
		
		stringAdd  = parameter.getProperty( "operator.string.add" , stringAdd  );
	}
	
	public synchronized int getUniqueId() {
		return ++uniqueId; 
	}
	
	public void clean() {
		output.clear();
		actions.clear();
		modules.clear();
	}
	
	public boolean parse( File _file ) throws ParseException {
		try {
			File   path = _file.getParentFile();
			String name = _file.getName();
			
			FileInputStream fis = new FileInputStream( _file );
			return parse( fis, path, name );
		} catch ( IOException e ) {
			throw new ParseException( "Open File: " + _file + ": " + e );
		}
	}

	public boolean parse( InputStream _source, File _dir, String _name ) throws ParseException {		
		
		
		if ( ! _dir.isDirectory() )
			_dir = _dir.getParentFile();
			
		String ext = getParameter( "extension" );
		if ( ext != null ) {
			
			int pos = _name.lastIndexOf( '.' );
			if ( pos < 0 )
				_name = _name + '.' + ext;
			else
				_name = _name.substring( 0, pos ) + '.' + ext;

		}
			
		// Hier wird ein richtiger parser benötigt .....
		
		sourceDir = _dir;
		sourceName = _name; 

		// Message
		
		// System.out.println( "---" );		
		System.out.println( sourceDir.toString() + File.separator + sourceName );
		
		// Read File
		try {

			byte[] buffer = new byte[ _source.available() ];
			_source.read( buffer );
			orgContent = new String( buffer );
			content    = orgContent;

		} catch ( Exception e ) {
			throw new ParseException( "Read File: " + e );		
		}
		
		// plugins forced ?
		String forced = getParameter( "plugin.forced" );
		if ( forced != null )
			createAction( forced.trim(), new Hashtable() );
		
		// Find Header
		boolean out = parseHeader();
		
		if ( forced != null ) return true;
		return out;
		
	}
	
	public void execute()
				throws Exception {
					
		// save original, if needed
		if ( MhuCast.toboolean( getParameter( "backup" ), false ) &&
			 actions.size() != 0 ) {
		
			String ext = getParameter( "backup.extension" );
			if ( ext == null ) ext = "bak";
			
			output.put( sourceName + "." + ext, orgContent );

		}
				
		for ( int i = 0; i < actions.size(); i++ )
			((ActionContainer)actions.elementAt( i )).execute();
		// original file, with changes from plugins
		output.put( sourceName, content );			


	}
	
	public void write( File _dir ) throws IOException {
		
		if ( ! _dir.isDirectory() )
			_dir = _dir.getParentFile();

		for ( Enumeration e = output.keys(); e.hasMoreElements();) {
			String name = (String)e.nextElement();
			new MhuFile( _dir, name ).write( (String)output.get(name) );
		}
	}	
	
	private boolean parseHeader() {
		
		// find documentation
		
		int offset   = 0;
		int posStart = -1;
		int posEnd   = -1;  
		while ( ( posStart = content.indexOf( "<!--", offset ) ) > -1 ) {
			if ( ( posEnd = content.indexOf( "-->", posStart ) ) > -1 ) {
				
				String rem = content.substring( posStart + 3, posEnd );
				
				// find header
				if ( rem.indexOf( IDENT ) > -1 ) { // fast check
					
					// Parse lines
					String[]  lines = rem.split( "\n" );
					String    name  = null;
					Hashtable param = null;
					
					for ( int i = 0; i < lines.length; i++ ) {
						
						String line = lines[i].trim();
						if ( line.startsWith( IDENT ) ) {
							// found start tag
							if ( name != null && param != null )
								createAction( name, param );
							name = line.substring( IDENT.length() );
							param = new Hashtable();
						} else 
						if ( param != null ){
							// Add Parameter
							int p = -1;
							if ( ( p = line.indexOf( "=") ) > 0 ) {
								String k = line.substring( 0, p );
								String v = line.substring( p + 1 );
								if ( k != null && v != null )
									param.put( k.trim(), v.trim() );
							}
						}
						
					}
					
					if ( name != null )
						createAction( name, param );
					
					if ( actions.size() != 0 ) {
						// found min one valid action parameter
						// remove header from jsp
						content = content.substring( 0, posStart ) + content.substring( posEnd + 3 );
						return true;
					}
					
				}
				
				offset = posEnd + 3;
				
			} else
				offset = posStart + 3;
			
		}
	
		// header not found
		return false;	
	}
	
	private void createAction( String _name, Hashtable _param ) {
		
		// Verify
		if ( plugins.get( _name ) == null ) {
			System.err.println( "Shore: plugin not found: " + _name );
			return;
		}
		
		actions.add( new ActionContainer( _name, _param ) );
		
	}
	
	protected Hashtable getOutput() {
		return output;
	}
	
	protected String getContent() {
		return content;
	}

	protected File getConfigDir() {
		return configDir;
	}

	protected String getSourceName() {
		return sourceName;
	}

	protected File getSourceDir() {
		return sourceDir;
	}
	
	public String getParameter( String _name ) {
		return parameter.getProperty( _name );
	}
	
	public void setModuleInfo( String _key, String _value ) {
		modules.put( _key, _value );
	}
	
	public String getModuleInfo( String _key ) {
		return (String)modules.get( _key );
	}

	class ActionContainer {
		
		String    name  = null;
		Hashtable param = null;
		
		public ActionContainer( String _name, Hashtable _param ) {
			name  = _name;
			param = _param;
		}
		
		public Plugin getPlugin()
					throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			
			return (Plugin)Class.forName( (String)plugins.get( name ) ).newInstance();

		}
		
		public void execute()
					throws Exception {
						
			PluginHelper helper = new PluginHelper( Shore.this, name, param );
			getPlugin().execute( helper );
			content = helper.getContent();
			
		}
	}
}
