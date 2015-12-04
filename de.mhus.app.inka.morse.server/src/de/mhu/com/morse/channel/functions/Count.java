package de.mhu.com.morse.channel.functions;

import java.util.Iterator;

import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.channel.IConnection;
import de.mhu.com.morse.channel.IConnectionServer;
import de.mhu.com.morse.channel.IQueryFunction;
import de.mhu.com.morse.channel.IQueryWhereFunction;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IAttributeDefault;
import de.mhu.com.morse.usr.UserInformation;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.utils.ObjectUtil;

public class Count implements IQueryFunction,IQueryWhereFunction {

	long cnt = 0;

	public String getResult() {
		return String.valueOf( cnt );
	}

	public IAttribute getType() {
		return IAttributeDefault.ATTR_OBJ_LONG;
	}
	
	public void append( String in ) {
		cnt++;
	}

	public void append() {
		cnt++;
	}

	public void initFunction(IConnectionServer con, IAclManager aclm, UserInformation user, String[] functionInit) throws MorseException {
		// TODO Auto-generated method stub
		
	}

	public void resetFunction() {
		cnt = 0;
	}

	public Iterator<String> getRepeatingResult(Object[] attr) throws MorseException {
		if ( attr.length != 3 )
			throw new MorseException( MorseException.NOT_SUPPORTED );
		final long start = ObjectUtil.toLong( attr[0] );
		final long end   = ObjectUtil.toLong( attr[1] );
		final long step  = ObjectUtil.toLong( attr[2] );
		return new Iterator<String>() {
			
			long cur = start;
			public boolean hasNext() {
				return cur <= end;
			}

			public String next() {
				cur+=step;
				return String.valueOf( cur-step );
			}

			public void remove() {
			}
			
		};
	}

	public String getSingleResult(Object[] attr) throws MorseException {
		throw new MorseException( MorseException.NOT_SUPPORTED );
	}
	
}
