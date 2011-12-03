package v9t9.tests.inst9900;

import v9t9.tests.inst9900.StatusTest9900;
import v9t9.tests.inst9900.TestBlocks9900;
import v9t9.tests.inst9900.TestTopDown1_9900;
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
		TestSuite suite = new TestSuite("Test for 9900");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestBlocks9900.class);
		suite.addTestSuite(StatusTest9900.class);
		suite.addTestSuite(TestTopDown1_9900.class);
		//$JUnit-END$
		return suite;
	}
}