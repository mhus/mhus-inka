package de.mhu.com.morse.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.mhu.lib.AMath;
import de.mhu.lib.ASql;
import de.mhu.lib.log.AL;
import de.mhu.com.morse.cache.ContentCache;
import de.mhu.com.morse.cache.MemoryCache;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.obj.IFunctionConfig;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;

public class ObjectUtil {

	private static AL log = new AL( ObjectUtil.class );
	
	/**
	 * Returns true if the id is a valide Morse-ID.
	 * NULL is not valide.
	 * 
	 * @param id A morse ID
	 * @return true if valide
	 */
	public static boolean validateId( String id ) {
		if ( id == null || id.length() != 32 ) return false;
		for ( int i = 0; i< 32; i++ ) {
			char c = id.charAt( i );
			if ( ! ( c >= 'a' && c <= 'z' ) && ! ( c >= '0' && c <= '9' ) && c != '_' )
				return false;
		}
		return true;
 	}

	/**
	 * Throws a MorseException if the id is not valide.
	 * 
	 * @param id A Morse-ID
	 * @throws MorseException Will be thrown if the id is not valide.
	 */
	public static void assetId(String id) throws MorseException {
		if ( ! validateId( id ) ) throw new MorseException( MorseException.INVALID_OBJECT_ID, id );
	}

	/**
	 * Validate a ACL Name. If the name is not valide, NULL or to long it returns
	 * false.
	 * @param id A Morse-ID
	 * @return true if the ACL-NAME is valide.
	 */
	public static boolean validateAcl( String id ) {
		if ( id == null || id.length() > 64 ) return false;
		for ( int i = 0; i < id.length(); i++ ) {
			char c = id.charAt( i );
			if ( ! ( c >= 'a' && c <= 'z' ) && ! ( c >= '0' && c <= '9' ) && c != '_' )
				return false;
		}
		return true;
 	}
	
	/**
	 * Creates a byte-representation of the Morse-ID ( 24 Byte).
	 * 
	 * @param id
	 * @return byte-Array with 24 Elements
	 * @throws MorseException
	 */
	public static byte[] idToByte( String id ) throws MorseException {
		byte[] out = new byte[24];
		for ( int i = 0; i < 32; i++ ) {
			char c = id.charAt( i );
			if ( c == '_' ) {
				
			} else
			if ( c >= '0' && c <= '9' ) {
				byte b = (byte)( c - '0' + 1);
				for ( int j = 0; j < 6; j++ ) {
					int a = i * 6 + j;
					out[ a / 8 ] = AMath.setBit( out[ a / 8 ], a % 8, AMath.getBit( b, j ) );
				}
			}
			else
			if ( c >= 'a' && c <= 'z' ) {
				byte b = (byte)( c - 'a' + 12);
				for ( int j = 0; j < 6; j++ ) {
					int a = i * 6 + j;
					out[ a / 8 ] = AMath.setBit( out[ a / 8 ], a % 8, AMath.getBit( b, j ) );
				}
			}
			else
				throw new MorseException( MorseException.INVALID_OBJECT_ID, id );
		}
		
		return out;
	}
	
	/**
	 * Byte-Array to Morse-ID converter.
	 * 
	 * @param in Byte-Array with min 24 elements.
	 * @return
	 */
	public static String byteToId( byte[] in ) {
		char[] out = new char[ 32 ];
		for ( int i = 0; i < 32; i++ ) {
			byte b = 0;
			for ( int j = 0; j < 6; j++ ) {
				int a = i * 6 + j;
				b = AMath.setBit( b, j, AMath.getBit( in[ a / 8 ], a % 8 ) );
			}
			if ( b == 0 ) {
				out[ i ] = '_';
			} else
			if ( b >= 1 && b <= 11 ) {
				out[ i ] = (char)('0' + b - 1);
			} else
				out[ i ] = (char)('a' + b - 12 );
		}
		return new String( out );
	}
	
	/*
	public static void main( String[] args ) throws MorseException {
		System.out.println( byteToId( idToByte( "0123456789_abcdefghijklmnopqrstuvwxyz" ) ) );
	}
	*/
	
	/**
	 * Create a unique Map from a table. The function will also close the table.
	 */
	public static Map<String,String> tableToMap(ITableRead table, String keyCol, String valueCol ) throws MorseException {
		table.reset();
		Hashtable<String,String> out = new Hashtable<String,String>();
		while ( table.next() )
			out.put( table.getString( keyCol ), table.getString( valueCol ) );
		table.close();
		return out;
	}

	/**
	 * Validate the three letter Morse-Base ID.
	 * 
	 * @param id
	 * @throws MorseException
	 */
	public static void assetMBaseId(String id) throws MorseException {
		if ( id == null || id.length() != 3 ) throw new MorseException( MorseException.INVALID_MORSE_BASE_ID, id );
		for ( int i = 0; i< 3; i++ ) {
			char c = id.charAt( i );
			if ( ! ( c >= 'a' && c <= 'z' ) && ! ( c >= '0' && c <= '9' ) && c != '_' )
				throw new MorseException( MorseException.INVALID_OBJECT_ID, id );
		}
		if ( id.charAt(0) == '_' )
			throw new MorseException( MorseException.INVALID_OBJECT_ID, id );
	}

	/**
	 * Create a list from a table. The fields will be stored in one line for each row.
	 * The function will also close the table. The function will always return LinkedList,
	 * maybe a empty list but never NULL.
	 */
	public static LinkedList<Object[]> tableToList(ITableRead table, String[] fields) throws MorseException {
		LinkedList<Object[]> out = new LinkedList<Object[]>();
		table.reset();
		while ( table.next() ) {
			Object[] val = new Object[ fields.length ];
			for ( int i = 0; i < fields.length; i++ )
				val[ i ] = table.getObject( fields[ i ] );
			out.add( val );
		}
		table.close();
		return out;
	}

	public static String toString(Object value) {
		return value.toString();
	}

	public static long toLong(Object value) {
		if ( value == null ) return 0;
		if ( value instanceof Long ) return ((Long)value).longValue();
		if ( value instanceof Integer ) return ((Integer)value).intValue();
		return Long.parseLong( value.toString() );
	}
	
}
