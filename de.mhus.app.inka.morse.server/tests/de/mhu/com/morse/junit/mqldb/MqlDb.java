package de.mhu.com.morse.junit.mqldb;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MqlDb {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for de.mhu.com.morse.junit.mqldb");
		//$JUnit-BEGIN$
		suite.addTestSuite(SimpleSelect.class);
		suite.addTestSuite(SimpleUpdate.class);
		//$JUnit-END$
		return suite;
	}

}
