package de.mhu.com.morse.junit.mqldb;

import org.junit.Test;

import de.mhu.com.morse.client.AuthPassword;
import de.mhu.com.morse.client.MConnection;
import de.mhu.com.morse.client.MConnectionTcp;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.utils.MorseException;

import junit.framework.TestCase;

public class SimpleSelect extends TestCase {

	private MConnection con;

	public void setUp() throws Exception {
		con = new MConnectionTcp( "localhost", 6666 );
		con.setAuth( "root", new AuthPassword( "nein" ) );
		con.setService( "service" );
		// con.setEntranceAddress( "localhost", 6666 );
	}
	
	public void tearDown() {
		con.close();
	}
	
	public void testSimpleAll() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"SELECT * FROM m_type @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results", res.next() );
		res.close();
	}
	
	public void testSimpleAllAndTables() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"SELECT ** FROM m_type @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results", res.next() );
		ITableRead table = res.getTable( "attribute" );
		assertTrue( "Found Table", table.next() );
		table.close();
		res.close();
	}

	public void testQuerySubTableAsAttribute() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"select ** from m_type WHERE attribute = ( name = 'name' ) @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results", res.next() );
		res.close();
	}

	public void testQuerySubTable() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"select * from m_type.attribute WHERE name='name' @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results", res.next() );
		res.close();
	}
	
	public void testQueryJoinSubTable() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"select * from m_type.attribute AS a,m_type AS t WHERE a.name='name' AND a.m_id=t.m_id @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results", res.next() );
		res.close();
	}
	
	public void testFunctionCount() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"select count(*) from m_type @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results1", res.next() );
		assertTrue( "Results2", res.next() );
		res.close();
	}
	
	public void testFindCount() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"select m_id FIND count(*) from m_type @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results1", res.next() );
		assertFalse( "Results2", res.next() );
		res.close();
	}
	
	public void testQueryInValues() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"SELECT * FROM m_type WHERE super_type IN ('mc_object','mc_document')  @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results", res.next() );
		res.close();
	}
	
	public void testQueryInSelect() throws MorseException {
		IQueryResult res = new Query( con.getDefaultConnection(), 
				"SELECT * FROM m_type WHERE super_type IN (SELECT name FROM m_type WHERE access_acl='everyone_all')  @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results", res.next() );
		res.close();
	}
	
}
