/*
 * Created on 07.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.mhu.test;

import de.mhu.shore.ifc.Page;
import de.mhu.shore.ifc.PageResult;
import junit.framework.TestCase;

/**
 * @author hummel
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PageTest extends TestCase {

	/**
	 * Constructor for PageTest.
	 * @param arg0
	 */
	public PageTest(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PageTest.class);
	}

	/*
	 * Test for Object get(Object)
	 */
	public void testGetObject() {
		
		Page   p = new XYPage();
		Object o = p.get( "Xy" );

		assertEquals( o, "ok" );
	}

	public class XYPage extends Page {

		/* (non-Javadoc)
		 * @see de.mhu.shore.ifc.Page#init()
		 */
		public PageResult init() {
			return new PageResult( PageResult.READY );
		}
		
		public Object getXy() {
			return "ok";
		}
		
	}
}
