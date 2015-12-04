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

public class Max implements IQueryFunction {
	long cnt = 0;
	double vd = 0;
	long vl = 0;
	private IAttribute type = IAttributeDefault.ATTR_OBJ_DOUBLE;
	
	public void append( double v ) {
		
		if ( cnt == 0 )
			vd = v;
		else
			vd = Math.max( vd, v );
		cnt++;
		type = IAttributeDefault.ATTR_OBJ_DOUBLE;
	}

	public void append( long v ) {
		
		if ( cnt == 0 )
			vl = v;
		else
			vl = Math.min( vl, v );
		cnt++;
		type = IAttributeDefault.ATTR_OBJ_LONG;
	}

	public void append( int v ) {
		
		if ( cnt == 0 )
			vl = v;
		else
			vl = Math.max( vl, v );
		cnt++;
		type = IAttributeDefault.ATTR_OBJ_LONG;
	}

	public String getResult() {
		if ( cnt == 0 )
			return null;
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
		cnt = 0;
	}

}
