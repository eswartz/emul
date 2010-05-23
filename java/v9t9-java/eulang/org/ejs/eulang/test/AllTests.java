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
		suite.addTest(new JUnit4TestAdapter(TestGenerics.class));
		suite.addTest(new JUnit4TestAdapter(TestSimplify.class));
		suite.addTest(new JUnit4TestAdapter(TestMacroCall.class));
		suite.addTest(new JUnit4TestAdapter(TestLLVMGenerator.class));
		suite.addTest(new JUnit4TestAdapter(TestLoopGenerator.class));
		suite.addTest(new JUnit4TestAdapter(TestScopes.class));
		suite.addTest(new JUnit4TestAdapter(TestTypes.class));
		suite.addTest(new JUnit4TestAdapter(Test9900Locals.class));
		suite.addTest(new JUnit4TestAdapter(Test9900InstrGen.class));
		suite.addTest(new JUnit4TestAdapter(Test9900InstrSelection.class));
		suite.addTest(new JUnit4TestAdapter(TestStockFlowGraph.class));
		suite.addTest(new JUnit4TestAdapter(TestFlowGraphPerf.class));
		suite.addTest(new JUnit4TestAdapter(Test9900Optimizer.class));
		//$JUnit-END$
		return suite;
	}

}
