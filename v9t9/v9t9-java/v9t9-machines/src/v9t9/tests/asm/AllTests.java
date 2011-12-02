/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 15, 2004
 *
 */
package v9t9.tests.asm;

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
		/*
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		ListIterator<Integer> listIter = list.listIterator();
		listIter.next();
		listIter.next();
		listIter.add(-1);
		listIter.add(-2);
		listIter.add(-3);
		
		for (Integer i : list)
			System.out.println(i);
		*/
		
		TestSuite suite = new TestSuite("Test for assembler");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestAssembler.class);
		suite.addTestSuite(TestAssembler9900.class);
		//suite.addTestSuite(TestAssemblerMFP201Operands.class);
		//suite.addTestSuite(TestAssemblerMFP201Insts.class);
		//suite.addTestSuite(TestDisassemblerMFP201.class);
		suite.addTestSuite(TestAssemblerMacros.class);
		suite.addTestSuite(TestAssemblerOptimizer.class);
		suite.addTestSuite(TestAssemblerJumpRanges.class);
		suite.addTestSuite(MachineOperandParserTest9900.class);
		suite.addTestSuite(InstructionTest9900.class);
		suite.addTestSuite(TestAssemblerConstPool.class);
		//$JUnit-END$
		return suite;
	}
}