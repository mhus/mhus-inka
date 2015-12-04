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

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PluginHelper {

	private static final int KEY   = 1;
	private static final int VALUE = 2;

	private String    content      = null;
	private Hashtable param        = null;
	private Hashtable output       = null;
	private Shore     shore        = null;
	
	private int       contentOffset = -1;
	private String    tagName       = null;
	private Hashtable tagParameter  = null;
	private int       tagStart      = -1;
	private int       tagEnd        = -1;
	

	public PluginHelper( Shore _shore, String _name, Hashtable _param ) {
		
		shore        = _shore;
		content      = _shore.getContent();
		param        = _param;
		output       = _shore.getOutput();

	}
	
	public void resetTag() {
		contentOffset = 0;		
	}
	
	public boolean nextTag() {
		
		if ( contentOffset < 0 ) return false;
		
		try {

			// find next tag
			int start = content.indexOf( "<", contentOffset );
			if ( start == -1 ) {
				contentOffset = -1;
				return false;
			}
			
			tagStart = start;
			
			// is tag a documentation ?, next Tag
			if ( content.charAt( start+1 ) == '!' ) {
				contentOffset = content.indexOf( "-->", start+1 );
				return nextTag();
			}
			
			// find name
			while ( content.charAt( start + 1) == ' ') start++; // remove spaces
			int end = ShoreUtil.findEnd( content, start );
			tagName = content.substring( start + 1, end ).toLowerCase();
			
			// find parameters
			int mode   = KEY;
			String key = "";
			tagParameter = new Hashtable();
			
			while ( content.charAt( end ) != '>' ) {
				
				switch ( content.charAt( end ) ) {
					case ' ':
						while ( content.charAt( end ) == ' ') end++; // remove spaces
					break;
					case '\t':
						while ( content.charAt( end ) == '\t') end++; // remove spaces
					break;
					case '\n':
						while ( content.charAt( end ) == '\n') end++; // remove spaces
					break;
					case '\r':
						while ( content.charAt( end ) == '\r') end++; // remove spaces
					break;
					case '=':
						mode = VALUE;
						end++;
					break;
					default:
						int e = -1;
						int newEnd = -1;
						if ( content.charAt( end ) == '"' ) {
							end++;
							e = ShoreUtil.findNextChar( content, end, '"' );
							newEnd = e+1;
						} else
						if ( content.charAt( end ) == '\'' ) {
							end++;
							e = ShoreUtil.findNextChar( content, end, '\'' );
							newEnd = e+1;
						} else {
							e = ShoreUtil.findEnd( content, end );
							newEnd = e;
						}
						String n = content.substring( end, e );
						if ( mode == KEY ) {
							key = n;
						} else {
							tagParameter.put( key.toLowerCase(), ShoreUtil.web2text( n, false ) );
							mode = KEY;
						}
						
						end = newEnd;
				}
				
				
				
			}
			
			tagEnd = end;
			contentOffset = end + 1;
			
		} catch ( Exception e ) {
			contentOffset = -1;
			return false;		
		}
				
		return true;
		
	}
		
	public String getTagName() {
		return tagName;
	}

	public Hashtable getTagParameter() {
		return tagParameter;
	}
	
	public void replaceTag() {
				
		replaceTag( ShoreUtil.createHtmlTag( tagName, tagParameter ) );
		
	}
	
	public void insertAfterTag( String _text ) {
		
		content = content.substring( 0, contentOffset ) + _text + content.substring( contentOffset );
		contentOffset+= _text.length(); 
	}
	
	public void replaceTag( String _text ) {
		
		content = content.substring( 0, tagStart ) + _text + content.substring( tagEnd+1 );
		contentOffset = tagStart + _text.length() + 1;
		
	}
	
	public int getTagCursor() {
		return contentOffset;
	}
	
	public void replaceText( int _start, int _end, String _text ) {
		if ( _start > _end ) return;
		int l = _text.length();
		
		// set new cursors, if needed
		if ( contentOffset > _start )
			contentOffset = contentOffset - ( _end - _start ) + l;
		if ( tagStart > _start )
			tagStart = tagStart - ( _end - _start ) + l;
		if ( tagEnd > _start )
			tagEnd = tagEnd - ( _end - _start ) + l;
		
		content = content.substring( 0, _start ) + _text + content.substring( _end + 1 );
		
	}
	
	public void replaceValues( int _start, int _stop, ReplaceListener _listener ) {
		content = replaceValues( content, _start, _stop, _listener );
	}

	public String replaceValues( String _text, int _start, int _stop, ReplaceListener _listener ) {
		// System.out.println( "REPLACE: " + Shore.valueStart + "..." + Shore.valueStop );
		if ( _listener == null ) return _text;		
		if ( _stop == -1 ) _stop = _text.length();

		boolean resize = ( _text == content ); // resize offset pointer?

		int pos = _start;
		while ( ( pos = _text.indexOf( Shore.valueStart, pos ) ) >= 0 && pos <= _stop ) {
			
			int start = pos;
			pos = pos + Shore.valueStart.length();
			
			int end = _text.indexOf( Shore.valueStop, pos );
			if ( end >= 0 ) {
				 // System.out.println( "Replace Value At: " + pos + " to " + end ); //--
				String newString = _listener.replace( _text.substring( pos, end ).trim() );
				end = end + Shore.valueStop.length();
				if ( newString == null ) {
					pos = end;
				} else {
					_text = _text.substring( 0, start ) + newString + _text.substring( end );
					pos = start + newString.length(); 
					
					if ( resize ) {
						// set new cursors, if needed
						if ( contentOffset > start )
							contentOffset = contentOffset - ( end - start ) + newString.length();
						if ( tagStart > start )
							tagStart = tagStart - ( end - start ) + newString.length();
						if ( tagEnd > start )
							tagEnd = tagEnd - ( end - start ) + newString.length();
					}
										
					// set new stop value
					_stop = _stop - ( end - start ) + newString.length();
					// System.out.println( "New Stop at " + _stop ); //--
						
				}
				
			}
			
		}
		
		return _text;
				
	}
	
	public String getContent() {
		return content;
	}
	
	public String getParameter( String _name ) {
		return (String)param.get( _name );
	}
	
	public Enumeration getParameterKeys() {
		return param.keys();
	}

	/**
	 * @return
	 */
	public String getSourceName() {
		return shore.getSourceName();
	}
	
	public File getSourceDir() {
		return shore.getSourceDir();
	}

	public String newFileName( String _extension ) {
		return getSourceName() + getUniqueId() + "." + _extension;
	}

	public String newFileName() {
		String ext = shore.getParameter( "extension" );
		if ( ext == null ) ext = "jsp";
		return newFileName( ext );
	}

	/**
	 * @param string
	 * @param string2
	 */
	public void newFile(String _name, String _content ) {
		output.put( _name, _content );		
	}

	/**
	 * @return
	 */
	public File getConfigDir() {
		return shore.getConfigDir();
	}
	
	public File getTemplateFile( String _name ) {
		return new File( getConfigDir(), "templates/" + _name );
	}
	
	public String getTemplatePath( String _name ) {
		return getTemplateFile( _name ).getAbsolutePath();
	}
 	
 	public String getShoreParameter( String _name ) {
 		return shore.getParameter( _name );
 	}
	
	public synchronized int getUniqueId() {
		return shore.getUniqueId();
	}

	public void setContent(String _c ) {
		content = _c;
	}
	
	public void setModuleInfo( String _key, String _value ) {
		shore.setModuleInfo( _key, _value );
	}
	
	public String getModuleInfo( String _key ) {
		return shore.getModuleInfo( _key );
	}
	
	
}
