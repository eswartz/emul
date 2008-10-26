/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.tests;

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
		suite.addTestSuite(MemoryTest.class);
		suite.addTestSuite(InstructionTest.class);
		suite.addTestSuite(TestSpriteCanvas.class);
		suite.addTestSuite(TestAssembler.class);
		suite.addTestSuite(SettingTest.class);
		suite.addTestSuite(TestBlocks.class);
		suite.addTestSuite(StatusTest.class);
		suite.addTestSuite(DiskMemoryEntryTest.class);
		suite.addTestSuite(TestTopDown1.class);
		suite.addTestSuite(MemoryEntryTest.class);
		suite.addTestSuite(TestMemoryRanges.class);
		suite.addTestSuite(MachineOperandParserTest.class);
		//$JUnit-END$
		return suite;
	}
}