package de.mhu.com.morse.channel.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import de.mhu.com.morse.channel.sql.SqlDriver;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPluginException;

public class HSqlDriver extends SqlDriver {

	@Override
	protected void apDisable() throws AfPluginException {
		super.apDisable();
	}

	@Override
	protected void apInit() throws Exception {
		Class.forName("org.hsqldb.jdbcDriver").newInstance();
		super.apInit();
	}

	@Override
	protected String getAlterTablePrefixSql() {
		return "ALTER TABLE";
	}

	@Override
	protected boolean existsTable( String name ) throws SQLException {
//		check for table
		boolean exists = false;
		ResultSet res = internatConnection.getPool().getDb().getMetaData().getTables( null, "PUBLIC", name.toUpperCase(), new String[] { "TABLE" } );
		if ( res.next() ) {
			exists = true;
		}
		res.close();
		return exists;
	}
	
	@Override
	protected ResultSet getTableColumns( String name ) throws SQLException {
		return internatConnection.getPool().getDb().getMetaData().getColumns( null, "PUBLIC", name.toUpperCase(), "%" );
	}
	
	// SELECT * FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE 1=1 AND TABLE_CAT = 'PUBLIC' AND TABLE_SCHEM LIKE '%' AND TABLE_NAME LIKE 't_m_object' AND TABLE_TYPE IN ('TABLE')
	@Override
	protected String getColumnDefinition( IAttribute attr, boolean primaryKey ) throws MorseException {
		StringBuffer create = new StringBuffer();
		if ( attr.getType() == IAttribute.AT_INT  ) {
			create.append( "INT" );
		} else
		if ( attr.getType() == IAttribute.AT_LONG ) {
			create.append( "INT" );
		} else
		if ( attr.getType() == IAttribute.AT_STRING ) {
			create.append( "VARCHAR(" ).append( attr.getSize() ).append( ')' );
		} else
		if ( attr.getType() == IAttribute.AT_STRING_RAW ) {
			create.append( "VARCHAR(" ).append( attr.getSize() ).append( ')' );
		} else
		if ( attr.getType() == IAttribute.AT_DATE ) {
			create.append( "DATETIME" );
		} else
		if ( attr.getType() == IAttribute.AT_ID ) {
			create.append( "CHAR( 32 )" );
		} else
		if ( attr.getType() == IAttribute.AT_ACL ) {
			create.append( "CHAR(64)" );
		} else
		if ( attr.getType() == IAttribute.AT_BOOLEAN ) {
			create.append( "INT" );
		} else
			if ( attr.getType() == IAttribute.AT_DOUBLE ) {
				create.append( "DOUBLE" );                          // check !!!!
		} else
			throw new MorseException( MorseException.ATTR_TYPE_UNKNOWN, String.valueOf( attr.getType() ) );

		return create.toString();
	}

	@Override
	protected String getCreateViewPrefixSql() {
		return "CREATE VIEW";
	}

	@Override
	protected String getDropTablePrefixSql() {
		return "DROP TABLE";
	}
	
	@Override
	protected String getDropViewPrefixSql() {
		return "DROP VIEW";
	}

	@Override
	protected String getCreateIndexSql(int index, IType type, IAttribute subTable, IAttribute attr) {
		if ( index == IAttribute.INDEX_NONE ) 
			return null;
		
		if ( index == IAttribute.INDEX_UNIQUE )
			return 	"CREATE UNIQUE INDEX " 
					+ ( subTable != null ? "r_" : "t_" ) 
					+ type.getName() 
					+ ( subTable != null ? "_" + subTable.getName() : "" )
					+ "_" + attr.getName() + " ON " 
					+ ( subTable != null ? "r_" : "t_" ) 
					+ type.getName() 
					+ ( subTable != null ? "_" + subTable.getName() : "" )
					+ "("  + ( subTable != null ? getColumnName( IAttribute.M_ID ) + "," : "" ) + getColumnName( attr.getName() ) + ")"
					;
			
		return 	  	"CREATE INDEX " 
					+ ( subTable != null ? "r_" : "t_" ) 
					+ type.getName() 
					+ ( subTable != null ? "_" + subTable.getName() : "" )
					+ "_" + attr.getName() + " ON " 
					+ ( subTable != null ? "r_" : "t_" ) 
					+ type.getName() 
					+ ( subTable != null ? "_" + subTable.getName() : "" )
					+ "("  + ( subTable != null ? getColumnName( IAttribute.M_ID ) + "," : "" ) + getColumnName( attr.getName() ) + ")"
					;
	}

	@Override
	protected String getCreateTablePrefixSql(IType type, IAttribute attr) {
		return "CREATE TABLE";
	}

	@Override
	protected String getCreateTablePrimaryKeySql(IType type, String[] names) {
		if ( names.length == 1 )
			return "PRIMARY KEY ( " + names[0] + ")";
		
		return "PRIMARY KEY ( " + names[0] + "," + names[1] + ")"; 
	}

	@Override
	protected String getCreateTableSuffixSql(IType type, IAttribute attr) {
		return "";
	}

	@Override
	public String toValidDate(Date date) {
		return null;
	}

	@Override
	public String getLockSql(String id, String table) {
		return "LOCK TABLE " + table; 
	}

	@Override
	public String getUnlockSql(String id, String table) {		
		return "UNLOCK TABLE " + table;
	}

	/*
	@Override
	public String getColumnName(String attrName) {
		return attrName + '_';
	}
	
	@Override
	public String getAttrName(String columnName) {
		return columnName.substring( 0,  columnName.length() - 1 );
	}
	*/
	
	@Override
	protected String getCreateTmpIndexSql(int index, String table, String attr) {
		return "CREATE INDEX " + table + attr  +" ON " + table + " ( " + attr + ')';
	}

	@Override
	protected String getCreateTmpTablePrefixSql() {
		return "CREATE TABLE";
	}

	@Override
	protected String getCreateTmpTableSuffixSql() {
		return "";
	}

	@Override
	protected String getDropTmpTableSql(String name) {
		return "DROP TABLE " + name;
	}
	
}
