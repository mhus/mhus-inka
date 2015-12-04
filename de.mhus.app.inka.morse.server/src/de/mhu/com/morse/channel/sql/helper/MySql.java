package de.mhu.com.morse.channel.sql.helper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.utils.AttributeUtil;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.dtb.StatementPool;

public class MySql implements SqlHelper {
 
	private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	private String schema;
	private StatementPool pool;

	public void sqlInit() throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
	}

	public String getAlterTablePrefixSql() {
		return "ALTER TABLE";
	}
	
	public String getColumnName(String attrName) {
		return attrName;
	}
	
	public String getColumnDefinition( IAttribute attr, boolean primaryKey ) throws MorseException {
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

		if ( primaryKey || attr.isNotNull() )
			create.append( " NOT NULL" );
		String def = attr.getDefaultValue();
		
		if ( def != null && def.length() != 0 ) {
			create.append( " DEFAULT " );
			if ( AttributeUtil.needQuots( attr.getType() ) )
				create.append( '\'' );
			create.append( def );
			if ( AttributeUtil.needQuots( attr.getType() ) )
				create.append( '\'' );
		}		
		
		return create.toString();
	}

	public String getCreateTablePrefixSql() {
		return "CREATE TABLE";
	}

	public String getCreateViewPrefixSql() {
		return "CREATE OR REPLACE VIEW";
	}

	public String getDropTablePrefixSql() {
		return "DROP TABLE";
	}

	public String getDropViewPrefixSql() {
		return "DROP VIEW";
	}

	public String getCreateIndexSql( int index, String name, String table, String[] attrs ) {
	
		if ( index == IAttribute.INDEX_NONE ) 
			return null;
	
		// CREATE INDEX name ON table (attr1,attr2...)
		StringBuffer sb = new StringBuffer().append( "CREATE" );
		if ( index == IAttribute.INDEX_UNIQUE )
			sb.append( " UNIQUE" );
		sb.append( " INDEX " + name + " ON " + table + " (" );
		
		for ( int i = 0; i < attrs.length; i++ ) {
			if ( i != 0 ) sb.append( ',' );
			sb.append( getColumnName( attrs[i] ) );
		}
		sb.append( ')' );
		
		return sb.toString();
		
	}

	public String getCreateTablePrimaryKeySql(String[] names) {
		
		StringBuffer sb = new StringBuffer().append( "PRIMARY KEY ( " );
		for ( int i = 0; i < names.length; i++ ) {
			if ( i != 0 ) sb.append( ',' );
			sb.append( getColumnName( names[i] ) );
		}
		sb.append( ')' );
		
		return sb.toString(); 
	}

	public String getCreateTableSuffixSql() {
		return "ENGINE = InnoDB";
	}

	public String toValidDate(Date date) {
		synchronized ( dateFormat ) {
			return dateFormat.format( date );
		}
	}

	public String getLockSql(String id, String table) {
		return "LOCK TABLE " + table; 
	}

	public String getUnlockSql(String id, String table) {		
		return "UNLOCK TABLE " + table;
	}

	public String getCreateTmpIndexSql(int index, String table, String attr) {
		return "CREATE INDEX " + table + attr  +" ON " + table + " ( " + attr + ')';
	}

	public String getCreateTmpTablePrefixSql() {
		return "CREATE TABLE";
	}

	public String getCreateTmpTableSuffixSql() {
		return "";
	}

	public String getDropTmpTableSql(String name) {
		return "DROP TABLE " + name;
	}

	public String getTableAs() {
		return " AS ";
	}

	public String getAttrName(String columnName) {
		return columnName;
	}
	
	public ResultSet getTableColumns( String name ) throws SQLException {
		return pool.getDb().getMetaData().getColumns( null, schema, name, "%" );
	}
	
	public void setInternalConnection(StatementPool pPool, String pSchema ) {
		pool = pPool;
		schema = pSchema;
	}
	
	public boolean existsTable( String name ) throws SQLException {
		//	check for table
		boolean exists = false;
		ResultSet res = pool.getDb().getMetaData().getTables( null, schema, name, new String[] { "TABLE" } );
		if ( res.next() ) {
			exists = true;
		}
		res.close();
		return exists;
	}
}
