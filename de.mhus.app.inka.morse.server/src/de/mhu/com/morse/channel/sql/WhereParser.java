package de.mhu.com.morse.channel.sql;

import java.util.LinkedList;

import de.mhu.lib.ACast;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.CMql;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IQueryFunction;
import de.mhu.com.morse.mql.ICompiledQuery;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;

public class WhereParser {

	private int off;
	private int brackedCount;
	private ICompiledQuery code;
	private IConnectionServer con;
	private UserInformation user;
	private IAclManager aclm;
	private IWhereListener listener;


	public int parse( IConnectionServer pCon, IAclManager pAclm, UserInformation pUser, ICompiledQuery pCode, int pOff, IWhereListener pListener ) throws MorseException {
		
		con = pCon;
		user = pUser;
		aclm = pAclm;
		off = pOff;
		code = pCode;
		listener = pListener;
		brackedCount = 0;
		
		while ( off < code.size() ) {
			
			int c = code.getInteger( off );
			
			switch ( c ) {
			case CMql.OPEN:
				listener.brackedOpen();
				off++;
				brackedCount++;
				break;
			case CMql.CLOSE:
				listener.brackedClose();
				off++;
				brackedCount--;
				if ( brackedCount < 0 )
					return off; // maybe its a subclause, so this is not my bracked
				break;
			case CMql.AND:
				listener.operatorAnd();
				off++;
				break;
			case CMql.OR:
				listener.operatorOr();
				off++;
				break;
			case CMql.NOT:
				listener.operatorNot();
				off++;
				break;
			case CMql.NaN:
				parseCompare();
				break;
			default:
				return off;
			}
			
		}
		
		return off;
	}
	
	
	private void parseCompare() throws MorseException {
		
		String left = parseWAttr( null ).toString();
		
		if ( code.getInteger( off ) == CMql.EQ && code.getInteger( off + 1 ) == CMql.OPEN ) {
			// table in select
			off = listener.appendTableSelect( left, off );
			
		} else {
			String right = null;
			switch ( code.getInteger( off ) ) {
			case CMql.EQ:
				off++;
				right = ObjectUtil.toString( parseWAttr( left ) );
				listener.compareEQ( left, right );
				break;
			case CMql.NOT:
				off++;
				right = ObjectUtil.toString( parseWAttr( left ) );
				listener.compareNOTEQ( left, right );
				break;
			case CMql.GT:
				off++;
				right = ObjectUtil.toString( parseWAttr( left ) );
				listener.compareGT( left, right );
				break;
			case CMql.GTEQ:
				off++;
				right = ObjectUtil.toString( parseWAttr( left ) );
				listener.compareGTEQ( left, right );
				break;
			case CMql.LT:
				off++;
				right = ObjectUtil.toString( parseWAttr( left ) );
				listener.compareLT( left, right );
				break;
			case CMql.LTEQ:
				off++;
				right = ObjectUtil.toString( parseWAttr( left ) );
				listener.compareLTEQ( left, right );
				break;
			case CMql.LIKE:
				off++;
				right = ObjectUtil.toString( parseWAttr( left ) );
				listener.compareLIKE( left, right );
				break;
			case CMql.IN:
				off++;
				if ( code.getInteger( off ) == CMql.OPEN ) {
					off++;
					parseInSelect( left );
				} else {
					parseInFunction( left );
				}
				break;
			default:
				throw new MorseException( MorseException.UNKNOWN_SYMBOL, new String[] { String.valueOf( code.getInteger( off ) ), code.getString( off ) } );
			}
		}
	}

	private void parseInFunction(String left) throws MorseException {
		String name = code.getString( off );
		off++;
		off++; // (
		LinkedList<Object> functionAttrs = new LinkedList<Object>();
		while ( code.getInteger( off ) != CMql.CLOSE ) {
			functionAttrs.add( parseWAttr( null ) );
			if ( code.getInteger( off ) == CMql.COMMA )
				off++;
		}
		off++; // )
		LinkedList<String> functionInit = new LinkedList<String>();
		if ( off < code.size() && code.getInteger( off ) == CMql.INIT ) {
			off++; // INIT
			off++; // (
			while ( code.getInteger( off ) != CMql.CLOSE ) {
				functionInit.add( code.getString( off ) );
				off++;
				if ( code.getInteger( off ) == CMql.COMMA )
					off++;
			}
		}
		off++; // )
		IQueryFunction function = (IQueryFunction)con.getServer().loadFunction( con, "query." + name.toLowerCase() );
		function.initFunction( con, aclm, user, (String[])functionInit.toArray( new String[ functionInit.size() ] ) );
		listener.appendInFunction( left, function, functionAttrs);
	}


	private void parseInSelect( String left ) throws MorseException {
		if ( code.getInteger( off ) == CMql.SELECT ) {
			off++;
			boolean distinct = false;
			if ( code.getInteger( off ) == CMql.DISTINCT ) {
				distinct = true;
				off++;
			}
			off = listener.compareSubSelect( left, off, distinct );
		} else {
			listener.compareINBegin( left );
			while ( true ) {
				listener.compareINValue( ObjectUtil.toString( listener.transformValue( null, code.getString( off ) ) ) );
				off++;
				if ( code.getInteger( off ) == CMql.COMMA ) {
					off++;
				} else
					break;
			}
			off++;
			listener.compareINEnd();
		}
	}


	private Object parseWAttr( String attrName ) throws MorseException {
		String name = code.getString( off );
		off++;
		boolean isValue = AttributeUtil.isValue( name );
		if ( off < code.size() && code.getInteger( off ) == CMql.OPEN ) {
			// its a function
			if ( isValue )
				throw new MorseException( MorseException.NOT_SUPPORTED, name );
			
			off++; // (
			
			LinkedList<Object> functionAttrs = new LinkedList<Object>();
			while ( code.getInteger( off ) != CMql.CLOSE ) {
				functionAttrs.add( parseWAttr( null ) );
				if ( code.getInteger( off ) == CMql.COMMA )
					off++;
			}
			off++; // )
			
			LinkedList<String> functionInit = new LinkedList<String>();
			if ( off < code.size() && code.getInteger( off ) == CMql.INIT ) {
				off++; // INIT
				off++; // (
				while ( code.getInteger( off ) != CMql.CLOSE ) {
					functionInit.add( code.getString( off ) );
					off++;
					if ( code.getInteger( off ) == CMql.COMMA )
						off++;
				}
			}
			off++; // )
			
			IQueryFunction function = (IQueryFunction)con.getServer().loadFunction( con, "query." + name.toLowerCase() );
			function.initFunction( con, aclm, user, (String[])functionInit.toArray( new String[ functionInit.size() ] ) );
			
			return listener.executeFunction( function, functionAttrs );
		}
		
		if ( isValue ) {
			String type = null;
			if ( code.getInteger( off ) == CMql.AS ) {
				off++;
				type = code.getString( off );
				off++;
			}
			if ( attrName == null ) {
				attrName = toAttrType( type );
			} else 
			if ( type != null ) {
				throw new MorseException( MorseException.CANT_OVERWRITE_TYPE, name );
			}
			return listener.transformValue( attrName, name );
		} else
			return listener.transformAttribute( name );
		
	}

	private String toAttrType(String type) throws MorseException {
		
		if ( type == null ) return IAttributeDefault.ATTR_OBJ_STRING.getName();
		
		if ( "string".equals( type ) )
			return IAttributeDefault.ATTR_OBJ_STRING.getName();

		if ( "int".equals( type ) )
			return IAttributeDefault.ATTR_OBJ_INT.getName();

		if ( "long".equals( type ) )
			return IAttributeDefault.ATTR_OBJ_LONG.getName();

		if ( "double".equals( type ) )
			return IAttributeDefault.ATTR_OBJ_DOUBLE.getName();

		if ( "boolean".equals( type ) )
			return IAttributeDefault.ATTR_OBJ_BOOLEAN.getName();

		throw new MorseException( MorseException.ATTR_TYPE_UNKNOWN, type );
		
	}

	/*
	private Object toAttrObject(String value, String type) throws MorseException {
		
		if ( type == null ) return value;
		
		if ( "string".equals( type ) )
			return value;

		if ( "int".equals( type ) )
			return Integer.parseInt( value );

		if ( "long".equals( type ) )
			return Long.parseLong( value );

		if ( "double".equals( type ) )
			return Double.parseDouble( value );

		if ( "boolean".equals( type ) )
			return ACast.toboolean( value, false );

		throw new MorseException( MorseException.ATTR_TYPE_UNKNOWN, type );
		
	}
	*/
	
	public static interface IWhereListener {

		public void brackedOpen() throws MorseException;

		public void appendInFunction( String left, IQueryFunction function, LinkedList<Object> functionAttrs) throws MorseException;

		public void compareINEnd() throws MorseException;

		public void compareINValue(String string) throws MorseException;

		public void compareINBegin(String left) throws MorseException;

		public int compareSubSelect(String name, int off, boolean distinct) throws MorseException;

		public void compareLIKE(String left, String right) throws MorseException;

		public void compareLTEQ(String left, String right) throws MorseException;

		public void compareLT(String left, String right) throws MorseException;

		public void compareGTEQ(String left, String right) throws MorseException;

		public void compareGT(String left, String right) throws MorseException;

		public void compareNOTEQ(String left, String right) throws MorseException;

		public void compareEQ(String left, String right) throws MorseException;

		public int appendTableSelect(String name, int off) throws MorseException;

		public String transformAttribute(String name) throws MorseException;

		public Object transformValue( String typeName, String name ) throws MorseException;

		public Object executeFunction(IQueryFunction function, LinkedList<Object> functionAttrs) throws MorseException;

		public void operatorNot() throws MorseException;

		public void operatorOr() throws MorseException;

		public void operatorAnd() throws MorseException;

		public void brackedClose() throws MorseException;
		
	}
	
}
