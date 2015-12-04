package de.mhu.com.morse.mql;

import de.mhu.com.morse.utils.MorseException;

public interface IQuery {
	
	public long executeUpdate(String sql ) throws MorseException;
	public IQueryResult executeQuery(String sql) throws MorseException;

}
