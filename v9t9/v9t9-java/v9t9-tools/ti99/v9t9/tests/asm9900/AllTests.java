/*
  AllTests.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tests.asm9900;

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