/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.tests.video;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ejs
 */
public class AllSwtVideoSpeedTests {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllSwtVideoSpeedTests.suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for SWT Video Speed");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestVideoSpeedSwt24Bit.class);
		suite.addTestSuite(TestVideoSpeedSwtPaletted.class);
		//$JUnit-END$
		return suite;
	}
}