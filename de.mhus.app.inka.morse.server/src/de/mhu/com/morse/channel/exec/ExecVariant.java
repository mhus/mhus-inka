package de.mhu.com.morse.channel.exec;

import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;

public class ExecVariant {

	private String name;
	private IAttribute type;
	private Object value;
	
	public ExecVariant( String pName, Object pValue ) {
		this( pName, getTypeFor( pValue ), pValue );
	}
	
	public ExecVariant( String pName, IAttribute pType, Object pValue ) {
		value = pValue;
		name  = pName;
		type = pType;
	}
	
	public String getName() {
		return name;
	}
	
	private IAttribute getType() {
		return type;
	}
	

	private String getStringValue() {
		if ( value == null ) return null;
		return value.toString();
	}

	public static IAttribute getTypeFor(Object object) {
		if ( object == null )
			return IAttributeDefault.ATTR_OBJ_STRING;
		if ( object instanceof ExecVariant )
			return ((ExecVariant)object).getType();
		if ( object instanceof Long )
			return IAttributeDefault.ATTR_OBJ_LONG;
		if ( object instanceof Integer )
			return IAttributeDefault.ATTR_OBJ_INT;
		if ( object instanceof String )
			return IAttributeDefault.ATTR_OBJ_STRING;
		if ( object instanceof Double )
			return IAttributeDefault.ATTR_OBJ_DOUBLE;
		if ( object instanceof Boolean )
			return IAttributeDefault.ATTR_OBJ_BOOLEAN;
		
		return IAttributeDefault.ATTR_OBJ_STRING;
	}

	public static String getNameFor(int i, Object object) {
		if ( object == null )
			return String.valueOf( i );
		if ( object instanceof ExecVariant )
			return ((ExecVariant)object).getName();
		return String.valueOf( i );
	}

	public static String getStringValueFor(Object object) {
		if ( object == null )
			return null;
		if ( object instanceof ExecVariant )
			return ((ExecVariant)object).getStringValue();
		return object.toString();
	}
	
}
