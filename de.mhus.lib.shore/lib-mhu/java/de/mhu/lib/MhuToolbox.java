/*
 *  mhu-lib Generic Application Framework
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

package de.mhu.lib;

/**
 * @author hummel
 * 

 * 
 */
public class MhuToolbox {

	/**
	 * Return, if the char is located in the string (indexOf).
	 */
    
	public static boolean isIndex( String _s, char _c ) {
	if ( _s.indexOf( _c ) < 0 )
		return false;
	else
		return true;
	}

	/**
	 * Return the string before _c in _s. If _c is not found in
	 * _s, return is an empty string.
	 */
	public static String beforeIndex( String _s, char _c ) {
		if ( ! isIndex( _s, _c ) ) return "";
        
		return _s.substring( 0, _s.indexOf( _c ) );
	}
    
	/**
	 * Return the string after _c in _s. If _c is not found in
	 * _s, return is an empty string.
	 */
	public static String afterIndex( String _s, char _c ) {
		if ( ! isIndex( _s, _c ) ) return "";
        
		return _s.substring( _s.indexOf( _c ) + 1 );
	}

	public static void sleep( int _time ) {
		try {
			Thread.sleep( _time );
		} catch ( Exception e ) {
		}
	}

}
