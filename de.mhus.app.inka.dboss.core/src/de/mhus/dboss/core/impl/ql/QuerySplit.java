package de.mhus.dboss.core.impl.ql;

import java.util.LinkedList;

import de.mhus.dboss.core.DBossException;

public class QuerySplit {

	public static String[] split( String in ) throws DBossException {
		
		int offset = 0;
		int mode   = 0;
		LinkedList<String> parts = new LinkedList<String>();
		
		for ( int i = 0; i < in.length(); i++ ) {
			char c = in.charAt( i );
			if ( c == ' ' || c == '\t' || c == '\n' || c == '\r' ) {
				if ( mode == 0 ) {
					offset = i+1;
				} else {
					parts.add( in.substring( offset, i ) );
					mode = 0;
					offset = i+1;
				}
			}
			else
			if ( c == '(' || c == ')' || c == ',' || c == '[' || c == ']' || c == '/' || c == '%' ) {
				if ( mode != 0 ) {
					parts.add( in.substring( offset, i ) );
				}
				parts.add( new String( new char[]{c} ) );
				offset = i+1;
				mode = 0;
			} else
			if ( ( c == '-' || c == '+' ) && mode == 0 ) {
				parts.add( new String( new char[]{c} ) );
				offset = i+1;
				mode = 0;
			} else
			if ( c == '=' || c == '<' || c == '>' || c == '!' ) {
				if ( mode == 1 )
					parts.add( in.substring( offset, i ) );
				if ( mode != 2 )
					offset = i;
				mode = 2;
			} else
			if ( c == '\'' ) {
				if ( mode != 0 )
					parts.add( in.substring( offset, i ) );
				offset = i;
				// find corresponding
				for ( i++ ; i < in.length(); i++ ) if ( in.charAt( i ) == '\'' ) break;
				if ( i >= in.length() ) throw new DBossException( "NO_CORRESPONDING_QUOT " + in );
				parts.add( in.substring( offset,i+1 ) );
				offset = i+1;
				mode = 0;
			}
			else
			if ( c == '`' ) {
				if ( mode != 0 )
					parts.add( in.substring( offset, i ) );
				offset = i;
				// find corresponding
				for ( i++; i < in.length(); i++ ) if ( in.charAt( i ) == '`' ) break;
				if ( i >= in.length() ) throw new DBossException( "NO_CORRESPONDING_QUOT " + in );
				parts.add( in.substring( offset+1,i ) );
				offset = i+1;
				mode = 0;
			}
			else
			if ( mode == 2 ) {
				parts.add( in.substring( offset, i ) );
				offset = i;
				mode = 1;
			}
			else
				mode = 1;
		}
		
		if ( mode != 0 )
			parts.add( in.substring( offset ) );
		
		
		return parts.toArray(new String[ parts.size() ] );
	}
	
	public static void main( String[] args ) throws DBossException {
		String[] p = split( "SELECT * FROM m_type" );
		for ( int i = 0; i < p.length; i++ )
			System.out.println( ": [" + p[i] + ']' );
	}
	
}
