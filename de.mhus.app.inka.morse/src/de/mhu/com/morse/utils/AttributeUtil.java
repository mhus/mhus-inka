package de.mhu.com.morse.utils;

import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;

public class AttributeUtil {

	public static boolean needQuots( int type ) {
		return type == IAttribute.AT_STRING || type == IAttribute.AT_ID || type == IAttribute.AT_ACL || type == IAttribute.AT_DATE; 
	}

	/**
	 * Validate the value if it is a value. if not its a attribute name. A value
	 * starts and ends with quotes (a string) or is a number. All other
	 * must be something different.
	 * 
	 * @param in
	 * @return true if it is a value.
	 */
	public static boolean isValue( String in) {
		
		// is a string ?
		
		if ( in.length() > 1 && in.charAt( 0 ) == '\'' && in.charAt( in.length() - 1 ) == '\'' ) {
			for ( int i = 1; i < in.length()-1; i++ )
				if ( in.charAt( i ) == '\'' && ( in.charAt( i + 1 ) != '\'' ) )
					return false;
			return true;
		}
		
		// is a number ?
		/*
		for ( int i = 0; i < in.length(); i++ ) {
			char c = in.charAt( i );
			if ( ! ( ( c >= '0' && c <= '9' ) || c == '.' || ( c == '-' || c == '+' || c != 'e' ) && i == 0 ) )
				return false;
		}
		*/
		try {
			Double.parseDouble( in );
		} catch ( NumberFormatException nfe ) {
			return false;
		}
		return true;
	}
	
	/**
	 * Extract the real value out of the string. Specially removes quotes.
	 * 
	 * TODO Add check for types ...
	 * 
	 * @param attr IAttribute type of the value or null if unknown. In this case
	 * the transformation is maybe not valide.
	 * @param in The value
	 * @return The extracted value e.g. to check with ACO.validate
	 */
	public static String valueExtract( IAttribute attr, String in ) {
		if ( in.length() > 1 && in.charAt( 0 ) == '\'' && in.charAt( in.length() - 1 ) == '\'' ) {
			String out = in.substring( 1, in.length() - 1 );
			if ( out.indexOf( '\'') >= 0 ) {
				out = out.replaceAll( "''", "'" );
			}
			return out;
		}
		return in;
	}
	
	/**
	 * Check if the name (in) is a valide attribute name. The check will only validate
	 * the characters.
	 * 
	 * @param in The name
	 * @param pathAllowed If a canonical path is allowed e.g. m_object.m_id
	 * @return true if the name is valide.
	 */
	public static boolean isAttrName( String in, boolean pathAllowed ) {
		
		boolean startOfName = true;
		for ( int i = 0; i < in.length(); i++ ) {
			char c = in.charAt( i );
			if ( ! ( ( c >= '0' && c <= '9' && !startOfName ) || ( c >='a' && c <='z' ) || ( pathAllowed && !startOfName && c == '.' ) || c == '_' ) )
				return false;
			
			startOfName = false;
			if ( c == '.' )
				startOfName = true;
			
		}
		if ( startOfName ) return false;
		
		return true;
	}
	
	public static void appendToMsg( ITableRead res, int index, IMessage msg ) throws MorseException {
		IAttribute attr = res.getAttribute( index );
		switch ( attr.getType() ) {
		case IAttribute.AT_ID:
		case IAttribute.AT_ACL:
		case IAttribute.AT_STRING:
		case IAttribute.AT_DATE:
			msg.append( res.getString( index ) );
			break;
		case IAttribute.AT_BOOLEAN:
		case IAttribute.AT_INT:
			msg.append( res.getInteger( index ) );
			break;
		case IAttribute.AT_LONG:
			msg.append( res.getLong( index ) );
			break;
		case IAttribute.AT_DOUBLE:
			msg.append( res.getDouble( index ) );
			break;
		}
	}
	
	public static String readFromMsg( IMessage msg, IAttribute attr ) {
		switch ( attr.getType() ) {
		case IAttribute.AT_ID:
		case IAttribute.AT_ACL:
		case IAttribute.AT_STRING:
		case IAttribute.AT_DATE:
			return msg.shiftString();
		case IAttribute.AT_BOOLEAN:
		case IAttribute.AT_INT:
			return String.valueOf( msg.shiftInteger() );
		case IAttribute.AT_LONG:
			return String.valueOf( msg.shiftLong() );
		case IAttribute.AT_DOUBLE:
			return String.valueOf( msg.shiftDouble() );
		}
		return msg.shiftString();
	}
}
