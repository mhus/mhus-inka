package de.mhus.dboss.core;

import java.util.Iterator;

public interface IResult extends Iterable<IObject> {

	int size();
	
	void setCurser(int pos);
	int getCurser();
	
	@Override
	Iterator<IObject> iterator();
	
	void close();
	
}
