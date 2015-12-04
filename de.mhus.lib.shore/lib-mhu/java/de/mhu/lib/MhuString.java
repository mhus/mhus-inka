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
public class MhuString {
	
	public static boolean isEmpty( String _in ) {
		return ( _in == null || _in.equals( "" ));
	}
	
	public static String replaceAll( String _src, String _pattern, String _replacement ) {
		StringBuffer sb = new StringBuffer( _src );
		replaceAll( sb, _pattern, _replacement );
		return sb.toString();
	}
		
	public static void replaceAll( StringBuffer _src, String _pattern, String _replacement ) {
		
		int offset = 0;
		int len    = _pattern.length();
		int diff   = _replacement.length() - len;
		
		while ( ( offset = _src.indexOf( _pattern, offset )) >= 0 ) {
			_src.replace( offset, offset+len, _replacement );
			offset+= diff;
		}
		
	}

	public static String[] split( String _in, String _pattern ) {
		
		if ( _in == null ) return new String[0];
		
		int nr  = 0;
		int offset = 0;
		while ( ( offset = _in.indexOf( _pattern, offset )) != -1 ) {
			nr++;
			offset+=_pattern.length();
		}
		
		String[] out = new String[ nr + 1 ];
		
		nr  = 0;
		offset = 0;
		int oldOffset = 0;
		while ( ( offset = _in.indexOf( _pattern, offset )) != -1 ) {
			out[nr] = _in.substring( oldOffset, offset );
			nr++;
			offset+=_pattern.length();
			oldOffset = offset;
		}
		out[ nr ] = _in.substring( oldOffset );
		
		return out;
		
	}

}
