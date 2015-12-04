package de.mhus.dboss.core.impl.ql;

public interface IQueryDefinition {

	public String getQueryDefinition( String in );
	public int getConstantId( String name );
	
}
