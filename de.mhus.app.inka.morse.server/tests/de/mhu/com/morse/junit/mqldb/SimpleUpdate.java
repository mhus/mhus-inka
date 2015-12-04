package de.mhu.com.morse.junit.mqldb;

import junit.framework.TestCase;
import de.mhu.com.morse.client.AuthPassword;
import de.mhu.com.morse.client.MConnection;
import de.mhu.com.morse.client.MConnectionTcp;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;


public class SimpleUpdate extends TestCase {

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
				"update m_type set attribute APPEND ( name ) VALUES ( 'murks') WHERE name='mc_document' @sys" ).execute();
		assertTrue( "Error Code", res.getErrorCode() == 0 );
		assertTrue( "Results", res.getReturnCode() == 1 );
		res.close();
	}
	
}
