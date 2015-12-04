package de.mhu.com.morse.channel.sql.helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import de.mhu.com.morse.channel.sql.SqlChannel;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.dtb.StatementPool;

public interface SqlHelper {


	public String getDropViewPrefixSql();
	public String getCreateViewPrefixSql();
	
	public String getDropTablePrefixSql();
	public String getCreateTablePrefixSql();
	public String getCreateTableSuffixSql();
	public String getCreateTablePrimaryKeySql(String[] names);

	public String getAlterTablePrefixSql();
	
	public String getCreateIndexSql( int index, String name, String table, String[] attrs );
	
	public String getColumnDefinition( IAttribute attr, boolean primaryKey ) throws MorseException;
	
	public String toValidDate(Date date);

	public String getLockSql( String id, String table );
	public String getUnlockSql( String id, String table );

	public String getCreateTmpTablePrefixSql();
	public String getCreateTmpTableSuffixSql();
	public String getDropTmpTableSql( String name );
	public String getCreateTmpIndexSql( int index, String table, String attr );
	public void sqlInit() throws Exception;
	public String getTableAs();
	public String getAttrName(String columnName);
	public String getColumnName(String attrName);
	public void setInternalConnection(StatementPool pPool, String schema );
	public ResultSet getTableColumns(String name) throws SQLException;
	public boolean existsTable( String name ) throws SQLException;
	
}
