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

import java.util.*;

/**
 * The class encodes/decodes parameter files in rfc1738 standard.
 * @author  jesus
 */
public class Rfc1738 {
    
    private TreeMap parameter = null;
    
    /** Creates a new instance of Rfc1738 */    
    public Rfc1738() {
        parameter = new TreeMap();
    }
    
    public Rfc1738( Map _h ) {
        parameter = new TreeMap( _h );
    }

    public Rfc1738( String _in ) {
        parameter = explode( _in );
    }
    
    public TreeMap getSource() {
        return parameter;
    }
    
    public void set( String _key, String _value ) {
        
        if ( _key == null ) return;
        
        if ( _value == null ) {
            remove( _key );
            return;
        }
        
        parameter.put( _key, _value );
        
    }
        
    public String get( String _key ) {
        
        if ( _key == null ) return null;
        return (String)parameter.get( _key );
        
    }
    
    public String getString( String _key, String _def ) {
        
        return get( _key, _def );
        
    }

    public String getString( String _key ) {
        return get( _key );
    }
    
    public String get( String _key, String _def ) {
        String v = get( _key );
        if ( v == null ) return _def;
        return v;
    }
    
    public void set( String _key, int _value ) {
        set( _key, MhuCast.toString( _value ) );
    }
        
    public int getInt( String _key, int _def ) {
        String v = get( _key );
        if ( v == null ) return _def;
        return MhuCast.toint( v );        
    }

    public int getInt( String _key ) {
        return getInt( _key, -1 );
    }
    
    public void remove( String _key ) {
        
        if ( _key == null ) return;
    
        parameter.remove( _key );
        
    }
    
    public boolean exist( String _key ) {
    
        if ( _key == null ) return false;
    
        if ( parameter.get( _key ) == null )
            return false;
        
        return true;
        
    }
    
    public String toString() {
        return implode( parameter );
    }
    
    
    
    
    
    public static String decode( String _in ) {

        if ( _in == null ) return "";
        
        StringBuffer sb = new StringBuffer();
        
        int mode   = 0;
        int buffer = 0;
        for ( int i = 0; i < _in.length(); i++ ) {
            
            char c = _in.charAt( i );
            
            if ( mode == 0 ) {
                
                if ( c == '%' ) {
                    mode   = 1;
                    buffer = 0;
                } else if ( c == '+' )
                    sb.append( ' ' );
                else
                    sb.append( c );
                
            } else if ( mode == 1 ) {
                
                if ( c >= '0' && c <= '9' )
                    buffer = c - '0';
                else if ( c >= 'A' && c <= 'F' )
                    buffer = c + 10 - 'A';
                else if ( c >= 'a' && c <= 'f' )
                    buffer = c + 10 - 'a';
                
                mode = 2;
            } else if ( mode == 2 ) {
                
                buffer = buffer * 16;
                
                if ( c >= '0' && c <= '9' )
                    buffer+= c - '0';
                else if ( c >= 'A' && c <= 'F' )
                    buffer+= c + 10 - 'A';
                else if ( c >= 'a' && c <= 'f' )
                    buffer+= c + 10 - 'a';
                
                sb.append( (char)buffer );
                mode = 0;
            }
            
        }
        
        return sb.toString();
    }
    
    public static String encode( String _in ) {
     
        if ( _in == null ) return "";
        
        StringBuffer sb = new StringBuffer();
        
        int  mode   = 0;
        int buffer = 0;
        for ( int i = 0; i < _in.length(); i++ ) {
            
            char c = _in.charAt( i );
            
            if ( c == '%' || c == '&' || c == '=' || c == '+' ||
                    c == '\n' || c == '\r' || c == '?' ) {
                        
                sb.append( '%' );
                
                int cc = c;
                buffer = cc / 16;
                if ( buffer < 10 )
                    sb.append( (char)( (int)'0' + buffer) );
                else
                    sb.append( (char)( (int)'A' - 10 + buffer) );
                        
                buffer = cc % 16;
                if ( buffer < 10 )
                    sb.append( (char)( (int)'0' + buffer) );
                else
                    sb.append( (char)( (int)'A' - 10 + buffer) );
                
            } else if ( c == ' ' )
                sb.append( '+' );
            else
                sb.append( c );
        }
        
        return sb.toString();
    }
    
    public static TreeMap explode( String _in ) {
        
        if ( _in == null ) return new TreeMap();
        
        TreeMap out = new TreeMap();
        
        String[] obj = _in.split( "&" );
        
        for ( int i = 0; i < obj.length ; i++ ) {
         
            String[] kv = obj[ i ].split( "=" );
            
            
            if ( kv.length == 2 ) {
                if ( kv[ 1 ] == null ) kv[ 1 ] = "";
                out.put( decode( kv[ 0 ] ), decode( kv[ 1 ] ) );
            } else
            if ( kv.length == 1 )
                out.put( decode( kv[ 0 ] ), "" );
            
        }
        
        return out;
    }
    
    public static String implode( TreeMap _in ) {
        
        if ( _in == null ) return "";
        
        StringBuffer sb = new StringBuffer();
        
        boolean first = true;
        
        for ( Iterator e = _in.keySet().iterator(); e.hasNext(); ) {
         
            Object key   = e.next();
            Object value = _in.get( key );
            
            if ( value != null ) {
                if ( ! first ) sb.append( '&' );
                sb.append( encode( key.toString() ) );
                sb.append( '=' );
                sb.append( encode( value.toString() ) );
                first = false;
            }
            
        }
        
        return sb.toString();
    }
    
}
