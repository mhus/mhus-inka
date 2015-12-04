package de.mhu.com.morse.channel.functions;

import java.util.Iterator;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IQueryFunction;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;

public class Sum implements IQueryFunction {

	double vd = 0;
	long vl = 0;
	private IAttribute type = IAttributeDefault.ATTR_OBJ_DOUBLE;
	
	public void append( double v ) {
		
		vd+=v;
		type = IAttributeDefault.ATTR_OBJ_DOUBLE;
	}

	public void append( long v ) {
		
		vl+=v;
		type = IAttributeDefault.ATTR_OBJ_LONG;
	}

	public void append( int v ) {
		
		vl+=v;
		type = IAttributeDefault.ATTR_OBJ_LONG;
	}

	public String getResult() {
		if ( type == IAttributeDefault.ATTR_OBJ_LONG )
			return String.valueOf( vl );
		else
			return String.valueOf( vd );
	}

	public IAttribute getType() {
		return type;
	}

	public void initFunction(IConnectionServer con, IAclManager aclm, UserInformation user, String[] functionInit) throws MorseException {
		// TODO Auto-generated method stub
		
	}

	public void resetFunction() {
		vl = 0;
		vd = 0;
	}

}
