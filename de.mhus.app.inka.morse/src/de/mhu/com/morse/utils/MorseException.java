package de.mhu.com.morse.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

import de.mhu.lib.log.AL;
import de.mhu.lib.plugin.AfPluginException;

public class MorseException extends AfPluginException {

	private static AL log = new AL( MorseException.class );
	
	public static final long QUERY_EMPTY = 1;
	public static final long QUERY_UNSUPPORTED = 2;
	public static final long TYPE_NOT_FOUND = 3;
	public static final long TABLE_NOT_FOUND = 4;
	public static final long NO_ATTRIBUTES = 5;
	public static final long UNKNOWN_ATTRIBUTE = 6;
	public static final long CANT_CONNECT = 7;
	public static final long UNKNOWN_TYPE = 8;
	public static final long UNKNOWN_SYMBOL = 9;
	public static final long PARSE_ERROR = 10;
	public static final long NO_CORRESPONDING_QUOT = 11;
	public static final long ATTR_TYPE_UNKNOWN = 12;
	public static final long SHORT_NOT_FOR_SUB_TABLES = 13;
	public static final long TO_MUTCH_ATTRIBUTES = 14;
	public static final long TO_MUTCH_TABLES = 15;
	public static final long NO_BRACKED_TO_CLOSE = 16;
	public static final long UNKNOWN_SUB_INDEX = 17;
	public static final long UPDATE_SET_NOT_POSSIBLE = 18;
	public static final long ATTR_AMBIGIOUS = 19;
	public static final long ATTR_NOT_A_TABLE = 21;
	public static final long ATTR_NOT_FOUND = 22;
	public static final long ATTR_IS_A_TABLE = 23;
	public static final long WRONG_ATTR_COUNT = 24;
	public static final long WRONG_VALUE_FORMAT = 25;
	public static final long ATTR_CAN_T_SET = 26; 
	public static final long ATTR_VALUE_NOT_VALIDE = 27;
		
	public static final long INVALID_OBJECT_ID = 100;
	public static final long ACCESS_DENIED_READ = 101;
	public static final long CLIENT_TIMEOUT = 102;
	public static final long CLIENT_UNKNOWN_RC_CODE = 103;
	public static final long ACCESS_DENIED_WRITE = 104;
	public static final long ACCESS_DENIED_CREATE = 105;
	public static final long ERROR = 106;
	public static final long NOT_SUPPORTED = 107;
	public static final long ACO_UNKNOWN = 108;
	public static final long TX_QUEUE_END = 109;
	public static final long OUT_OF_BOUND = 110;
	public static final long NO_ROW_CREATED = 111;
	public static final long TYPE_ALREADY_SET = 112;
	public static final long INDEX_NOT_ALLOWED = 113;
	public static final long INDEX_NOT_SET = 114;
	public static final long ACCESS_DENIED = 115;
	public static final long ACCESS_DENIED_DELETE = 116;
	public static final long OBJECT_NOT_FOUND = 117;
	public static final long FUNCTION_NOT_FOUND = 118;
	public static final long JAR_NOT_FOUND = 119;
	public static final long AT_NOT_COMPATIBLE = 120;
	public static final long INVALID_MORSE_BASE_ID = 121;
	public static final long ALREADY_CLOSED = 122;
	public static final long FUNCTION_NO_METHOD = 123;
	public static final long ACCESS_DENIED_EXEC = 124;
	public static final long LOCK_NOT_FOUND = 125;
	public static final long RX_RETURN_CODE = 126;
	public static final long RX_ERROR = 127;
	public static final long UNKNOWN_USER = 128;
	public static final long NOT_OWNER = 129;
	public static final long CANT_LOCK = 130;
	public static final long OBJECT_IS_LOCKED = 131;
	public static final long USAGE = 132;
	public static final long TO_MUTCH_TABLE_HINTS = 133;
	public static final long PARENT_FOLDER_NOT_FOUND = 134;
	public static final long PARENT_NOT_A_FOLDER = 135;
	public static final long CANT_CREATE_OBJECT = 136;
	public static final long INVALID_TRANSACTION = 137;
	public static final long TRANSACTION_NOT_RUNNING = 138;
	public static final long FUNCTION_NOT_COMPATIBLE = 139;
	public static final long CANT_OVERWRITE_TYPE = 140;
	public static final long IDX_NOT_FOUND = 141;
	public static final long UNKNOWN_INDEX = 142;

	private static Hashtable<Long, String> MSG_NAMES_RAW = new Hashtable<Long, String>();
	
	static {
		Field[] fields = MorseException.class.getDeclaredFields();
		for ( int i = 0; i< fields.length; i++ ) {
			if ( Modifier.isStatic( fields[ i ].getModifiers() ) && fields[ i ].getType() == long.class ) {
				try {
					MSG_NAMES_RAW.put( fields[ i ].getLong( null ), fields[ i ].getName() );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error( e );
				}
			}
			
		}
	}
	
	public static String getMsgNameRaw( long code ) {
		return MSG_NAMES_RAW.get( code );
	}
	
	public String getMsg( long code ) {
		return getMsgNameRaw( code );
	}
	
	public MorseException(long id, String message, Throwable cause) {
		super(id, message, cause);
		// TODO Auto-generated constructor stub
	}

	public MorseException(long id, String message) {
		super(id, message);
		// TODO Auto-generated constructor stub
	}

	public MorseException(long id, String[] attr, Throwable cause) {
		super(id, attr, cause);
		// TODO Auto-generated constructor stub
	}

	public MorseException(long id, String[] attr) {
		super(id, attr);
		// TODO Auto-generated constructor stub
	}

	public MorseException(long id, Throwable cause) {
		super(id, cause);
		// TODO Auto-generated constructor stub
	}

	public MorseException(long id) {
		super(id);
		// TODO Auto-generated constructor stub
	}

}
