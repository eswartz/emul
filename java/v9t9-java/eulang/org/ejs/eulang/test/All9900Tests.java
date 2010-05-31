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
public class All9900Tests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.ejs.eulang.test");
		//$JUnit-BEGIN$
		suite.addTest(new JUnit4TestAdapter(Test9900Locals.class));
		suite.addTest(new JUnit4TestAdapter(Test9900InstrGen.class));
		suite.addTest(new JUnit4TestAdapter(Test9900InstrSelection.class));
		suite.addTest(new JUnit4TestAdapter(Test9900Data.class));
		suite.addTest(new JUnit4TestAdapter(TestStockFlowGraph.class));
		suite.addTest(new JUnit4TestAdapter(Test9900Optimizer.class));
		suite.addTest(new JUnit4TestAdapter(Test9900LowerPseudos.class));
		suite.addTest(Test9900Simulation.suite());
		//$JUnit-END$
		return suite;
	}

}
