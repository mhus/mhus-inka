package de.mhu.com.morse.mql;

public interface ICompiledQuery {

	public void dump();

	public int size();

	public int getInteger(int i);

	public String getString(int off);

	public boolean isFeature( String in );

	public String[] getFeatures();
	
}
