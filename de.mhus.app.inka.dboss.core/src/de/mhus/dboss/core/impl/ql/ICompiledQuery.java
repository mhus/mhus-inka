package de.mhus.dboss.core.impl.ql;

public interface ICompiledQuery {

	public void dump();

	public int size();

	public int getInteger(int i);

	public String getString(int off);

	public boolean isFeature( String in );

	public String[] getFeatures();
	
}
