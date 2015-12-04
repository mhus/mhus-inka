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

import org.javaby.jbyte.Template;

import de.mhu.lib.MhuString;
import de.mhu.lib.Rfc1738;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ShoreUtil {

	public static void addTemplateParameter( Template _t, String _key, String _value ) {
		
		_t.set( _key, _value );
		
		_t.set( _key+"_p", text2web( _value, false ) );
		_t.set( _key+"_pp", text2web( text2web( _value, false ), false ));
		_t.set( _key+"_w", text2web( _value, true ));
		_t.set( _key+"_c", toCodeParameter( _value ) );
		_t.set( _key+"_url", Rfc1738.encode( _value ) );
		_t.set( _key+"_pw", text2web( text2web( _value, true ), false ));
		
	}

	public static void addTemplateParameter( Hashtable _t, String _key, String _value ) {
		
		_t.put( _key, _value );
		
		_t.put( _key+"_p", text2web( _value, false ) );
		_t.put( _key+"_pp", text2web( text2web( _value, false ), false ));
		_t.put( _key+"_w", text2web( _value, true ));
		_t.put( _key+"_c", toCodeParameter( _value ) );
		_t.put( _key+"_pw", text2web( text2web( _value, true ), false ));
		
	}

	public static String createHtmlTag( String _name, Hashtable _parameters ) {
		StringBuffer newTag = new StringBuffer();
		newTag.append( '<' );
		newTag.append( _name );
		for ( Enumeration e = _parameters.keys(); e.hasMoreElements();) {
			String key = (String)e.nextElement();
			newTag.append( ' ' );
			newTag.append( key );
			newTag.append( "=\"" );
			String value = (String)_parameters.get( key );
			// dont encode jsp
			if ( value.startsWith( "<%" ) )
				newTag.append( value );
			else
				newTag.append( ShoreUtil.text2web( value , false ) );
			newTag.append( '"' );
		}
		newTag.append( '>' );
		
		return newTag.toString();
	}
	
	/**
	 * Transform a String to jsp code. If the string starts with "&lt;%=" and ends with "%&gt;"
	 * it use the included code. Otherwise it enclose the string with quotes.
	 * <p>
	 * e.g.<br>
	 * "Hello World" = "\"Hello World\""
	 * &lt;%=request.getParameter( "Hello" )%&gt; = request.getParameter( "Hello" )
	 * 
	 * @param _in Code/String
	 * @return Formed string to insert in a jsp code
	 */
	public static String toCodeParameter( String _in ) {
		
		if ( _in == null ) return null;		
		
		
		String[] in = MhuString.split( _in, Shore.codeStart );
		// nix gefunden?
		
		StringBuffer sb = new StringBuffer();
		sb.append( "\"" + text2web( in[0], false ) + "\"" );
		
		for ( int i = 1; i < in.length; i++ ) {
			
			String[] parts = MhuString.split( in[i], Shore.codeStop );
			if ( parts.length == 1 ) // error? end not found
				parts = new String[] { "\"\"", parts[0] };
			
			if ( i != 0 )
				sb.append( Shore.stringAdd );
				
			sb.append( parts[0] );
			if ( ! parts[1].equals(""))
				sb.append( Shore.stringAdd + "\"" + text2web( parts[1], false ) + "\"" );
			
		}
		
		return sb.toString();  
		
	}
		
	public static String escape( String _in ) {
		
		while (true) {
			
			boolean ok  = true;
			boolean esc = false;
			for ( int i = 0; i < _in.length(); i++ ) {
				if ( esc ) {
					if ( _in.charAt(i) != '"' && _in.charAt(i) != '\\' )
						ok = false;
					esc = false;
				} else {
					if ( _in.charAt(i) == '\\' )
						esc = true;
					else
					if ( _in.charAt(i) == '"' )
						ok = false;
				}
			}
			
			if ( ok ) return _in;
			
			_in = text2web( _in, false );
				
		}
				
	}
		
	/**
	 * Transform the String in a Parameter setter/getter form. this means the first
	 * character will be upper case, all other in lower case.
	 * 
	 * @param _p Name of the parameter
	 * @return Parameter in getter/setter form
	 */
	public static String toParameterName( String _p ) {
		_p = _p.trim();
		if ( _p.length() > 1 )
			_p = _p.substring( 0, 1 ).toUpperCase() + _p.substring( 1 ).toLowerCase();
		else
			_p = _p.toUpperCase();
		return _p;
	}

	/**
	 * Transform web text to readable text.
	 * 
	 * @param _text Web encoded text
	 * @param _jokers true: text is encoded with html special jokers, false: text is encoded with backslashes.
	 * @return Readable text
	 * @see text2web
	 */
	public static String web2text( String _text, boolean _jokers ) {
		
		StringBuffer buffer = new StringBuffer();
		
		try {
		
			for ( int i = 0; i < _text.length(); i++ ) {
				char c = _text.charAt( i );
				switch ( c ) {
					case '\\':
					break;
					case '&':
						if ( _jokers ) {
							int p = findNextChar( _text, i, ';');
							String joker = _text.substring( i+1, p );
							buffer.append( decodeWebJoker( joker ) );
						} else
							buffer.append( c );
					break;
					default:
						buffer.append( c );
					break;
				}
			}
		
		} catch ( Exception e ) {}
				
		return buffer.toString();
	}

	/**
	 * Transform the String in web form. If _jokers is true, it includes
	 * jokers for html special characters, otherwise it escapes quotes and backslashes.
	 *  
	 * @param _text Source text
	 * @param _jokers True: transform to html text, false: include in a html parameter 
	 * @return Transformet text
	 */
	public static String text2web( String _text, boolean _jokers ) {
		
		StringBuffer buffer = new StringBuffer();
		
		try {
		
			for ( int i = 0; i < _text.length(); i++ ) {
				char c = _text.charAt( i );
				if ( _jokers ) { 
					switch ( c ) {
						case '>': buffer.append( "&gt;" ); break;
						case '<': buffer.append( "&lt;" ); break;
						case '&': buffer.append( "&amp;" ); break;
						case '"': buffer.append( "&quot;" ); break;
						case 'ä': buffer.append( "&auml;" ); break;
						case 'Ä': buffer.append( "&Auml;" ); break;
						case 'ö': buffer.append( "&ouml;" ); break;
						case 'Ö': buffer.append( "&Ouml;" ); break;
						case 'ü': buffer.append( "&uuml;" ); break;
						case 'Ü': buffer.append( "&Uuml;" ); break;
						case 'ß': buffer.append( "&szlig;" ); break;
						default: buffer.append( c );
					}
				} else {
					switch ( c ) {
						case '"':  buffer.append( "\\\"" ); break;
						case '\\': buffer.append( "\\\\" ); break;
						//case '\'': buffer.append( "\\'" ); break;
						default: buffer.append( c );
					}
				}	
			}
		
		} catch ( Exception e ) {}
				
		return buffer.toString();
		 
	}
	
	/**
	 * Return the character for a web joker.
	 * @param joker Web joker
	 * @return Character for the joker. Is the joker is unknown, return is "?".
	 */
	public static char decodeWebJoker(String joker) {

		// dezimal encoded
		if ( joker.length() > 1 && joker.charAt( 0 ) == '#' )  {
		
			int c = 0;
			for ( int i = 2; i < joker.length(); i++ )
				c = c * 10 + (int)joker.charAt( i );
			return (char)c;	
					
		}
		
		// text encoded
		if ( joker.equals( "amp") ) return '&';
		else
		if ( joker.equals( "quot") ) return '"';
		else
		if ( joker.equals( "auml") ) return 'ä';
		else
		if ( joker.equals( "Auml") ) return 'Ä';
		else
		if ( joker.equals( "uuml") ) return 'ü';
		else
		if ( joker.equals( "Uuml") ) return 'Ü';
		else
		if ( joker.equals( "ouml") ) return 'ö';
		else
		if ( joker.equals( "Ouml") ) return 'Ö';
		else
		if ( joker.equals( "szlig") ) return 'ß';
		else
		if ( joker.equals( "gt") ) return '>';
		else
		if ( joker.equals( "lt") ) return '<';
		else
		if ( joker.equals( "euro") ) return '€';

		// return error ? 
		return '?';
	}

	/**
	 * Find next character in web code. This function is speciall to find quotes.
	 * It ignores escaped quotes.
	 * @param _text String to search into.
	 * @param _start First character
	 * @param _c e.g. quot, double quote
	 * @return Position of the character or -1
	 */
	public static int findNextChar( String _text, int _start, char _c ) {
		
		for ( int i = _start; i < _text.length(); i++ )
			if ( _text.charAt( i ) == _c && _text.charAt( i-1 ) != '\\' )
				return i;
		return -1;
	}

    /**
     * Find end of word in a html text.
     * @param _text String to search into
     * @param _start First character
     * @return Position of character after word or -1.
     */
	public static int findEnd( String _text, int _start ) {
		
		for ( int i = _start; i < _text.length(); i++ )
			switch ( _text.charAt( i ) ) {
				case ' ':
				case '>':
				case '\n':
				case '\t':
				case '\r':
				case '=':
					return i;
			}
		return -1;
	}

}
