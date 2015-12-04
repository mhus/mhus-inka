package de.mhu.com.morse.cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class CacheEntry {

	public abstract void load( ObjectInputStream stream ) throws IOException;
	public abstract void save( ObjectOutputStream stream ) throws IOException;
	
}
