package de.mhus.dboss.core.impl;

import java.util.Hashtable;

import de.mhus.dboss.core.DBossException;
import de.mhus.dboss.core.IType;
import de.mhus.lib.MEventHandler;

public class XTypeHandler extends MMap<String,IType> {

	public void put(IType type) throws DBossException {
		synchronized (this) {
			if (containsKey(type.getName()))
				throw new DBossException("type already defined: " + type.getName());
			put(type.getName(), type);
		}
	}
	
}
