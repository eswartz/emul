/**
 * 
 */
package org.ejs.eulang.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ejs
 *
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.ejs.eulang.test");
		//$JUnit-BEGIN$
		suite.addTest(new JUnit4TestAdapter(TestParser.class));
		suite.addTest(new JUnit4TestAdapter(TestGenerator.class));
		suite.addTest(new JUnit4TestAdapter(TestTypeInfer.class));
		suite.addTest(new JUnit4TestAdapter(TestSimplify.class));
		suite.addTest(new JUnit4TestAdapter(TestMacroCall.class));
		suite.addTest(new JUnit4TestAdapter(TestTypes.class));
		suite.addTest(new JUnit4TestAdapter(TestLLVMGenerator.class));
		suite.addTest(new JUnit4TestAdapter(TestLoopGenerator.class));
		//$JUnit-END$
		return suite;
	}

}
