package de.mhus.dboss.core;

public interface ILoader {

	Object newInstance(String name,Object ... objects) throws DBossException;

}
