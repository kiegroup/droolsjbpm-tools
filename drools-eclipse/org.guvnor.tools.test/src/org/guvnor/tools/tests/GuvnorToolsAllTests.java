 
package org.guvnor.tools.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class GuvnorToolsAllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test Guvnor Tools");
		suite.addTestSuite(GuvnorJunitTests.class);
		return suite;
	}

}
