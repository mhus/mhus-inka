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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MhuCast {

	public static boolean toboolean( String _in, boolean _default ) {
		
		if ( _in == null ) return _default;
		_in = _in.toLowerCase().trim();
		
		if ( 	_in.equals( "true") ||
				_in.equals( "yes" ) ||
				_in.equals( "1"   )) return true;

		if ( 	_in.equals( "false") ||
				_in.equals( "no"   ) ||
				_in.equals( "0"    ) ||
				_in.equals( "-1"   )) return false;
	
		return _default;
		 
	}
	
	public static boolean toboolean( int _in, boolean _default ) {
		if ( _in == 0 )
			return false;
		if ( _in == 1 )
			return true;
		return _default;
	}

	public static double todouble( String _in ) {
		return toint( _in, false );
	}
    
	public static double todouble( String _in, boolean _exception ) {
		if ( _exception ) {
			return Double.valueOf( _in ).doubleValue();
		} else {
			try {
				return Double.valueOf( _in ).doubleValue();
			} catch ( NumberFormatException e ) {
				return 0;
			}
		}
	}

	public static int toint( boolean _in ) {
		if ( _in )
			return 1;
		else
			return 0;
	}
    
	public static int toint( Integer _in ) {
		if ( _in == null ) return -1;
		return _in.intValue();
	}

	public static int toint( String _in ) {
		return toint( _in, false );
	}
    
	public static int toint( String _in, int _def ) {
		try {
			return toint( _in, true );
		} catch( Exception e ) {
			return _def;
		}
        
	}
    
	public static int toint( String _in, boolean _exception ) {
		if ( _exception ) {
			return Integer.parseInt( _in );
		} else {
			try {
				return Integer.parseInt( _in );
			} catch ( NumberFormatException e ) {
				return 0;
			}
		}
	}

	public static long tolong( String _in ) {
		return tolong( _in, false );
	}
    
	public static long tolong( String _in, boolean _exception ) {
		if ( _exception ) {
			return Long.parseLong( _in );
		} else {
			try {
				return Long.parseLong( _in );
			} catch ( NumberFormatException e ) {
				return 0;
			}
		}
	}

	public static String toString( double _in ) {        
		return String.valueOf( _in );
	}

	public static String toString( boolean _in ) {        
		if ( _in )
			return "true";
		else
			return "false";
	}

	public static String toString( int _in ) {        
		return String.valueOf( _in );
	}
    
	public static String toString( long _in ) {
		return String.valueOf( _in );
	}
    
	/**
	 * The String can be a standart URL, if no type (e.g. http:) is set,
	 * the function interprate the string as file path. is the type "classpath"
	 * is given, the URL points to a file in the classpath.
	 **/
    
	public static URL toUrl( String _in ) {
      
		if ( _in == null ) return null;
        
		try {
			if ( MhuToolbox.isIndex( _in, ':' ) ) {
				if ( MhuToolbox.beforeIndex( _in, ':' ).toLowerCase().equals( "classpath" ) )
					return _in.getClass().getResource( MhuToolbox.afterIndex( _in, ':' ) );

			}
/*
			if ( ! ATool.isIndex( _in, ':' ) ) {
				return new URL( "file:" + _in );
			}
*/          

			return new URL( _in );
		} catch( MalformedURLException e ) {
            
			// Is a Filename ?
			try {
				return new File( _in ).toURL();
			} catch ( Exception e2 ) {
				e2.printStackTrace();
			}
            
		} catch( Exception e ) {
			e.printStackTrace();
		}
        
		return null;
	}
    
	public static URI toUri( String _in ) {
		URL url = toUrl( _in );
		if ( _in == null ) return null;
        
		try {
			return new URI( url.toString() );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return null;
	}
/*
	public static String toString( Calendar _in ) {
		return "";
	}
*/  

	/**
	 * Cast a Image to a Buffered Image. The new image is a BufferedImage.TYPE_3BYTE_BGR type.
	 * Perhaps some informations of the origin image will be lost.
	 */
    
	public static BufferedImage toBufferedImage( Image _in ) {
        
		BufferedImage out = new BufferedImage( _in.getWidth( null ), _in.getHeight( null ), BufferedImage.TYPE_3BYTE_BGR );
		Graphics g = out.getGraphics();
		g.drawImage( _in, 0, 0, null );
		_in.flush();
        
		return out;
	}
	
}
