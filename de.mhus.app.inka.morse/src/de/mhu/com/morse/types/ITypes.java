package de.mhu.com.morse.types;

import java.util.Iterator;

import de.mhu.lib.plugin.IAfPpi;

public interface ITypes extends IAfPpi {

	public Iterator getTypes();
	public IType get( String name );
	public IAttribute getAttributeByCanonicalName(String canonical);
	
}
