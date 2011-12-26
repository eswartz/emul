package v9t9.machine.ti99.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ejs
 */
public class AllTests {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AllTests.suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for v9t9");
		//$JUnit-BEGIN$
		suite.addTestSuite(DiskMemoryEntryTest.class);
		suite.addTestSuite(MemoryTest.class);
		suite.addTestSuite(MemoryEntryTest.class);
		//$JUnit-END$
		return suite;
	}
}