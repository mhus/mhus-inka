package de.mhu.com.morse.channel.sql;

import de.mhu.com.morse.types.IAttribute;

public interface IQuerySqlFunction {

	public String appendSqlCommand( SqlDriver driver, String[] attrs);

}
