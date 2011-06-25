/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertTrue;
import org.ejs.eulang.llvm.tms9900.Routine;

/**
 * @author ejs
 *
 */
public class Test9900Induction extends BaseInstrTest {

	protected boolean doOpt(String string) throws Exception {
		routine = doIsel(string);
		routine.setupForOptimization();
	
		return doOpt(routine);
	}
	protected boolean doOpt(Routine routine) {
		runPeepholePhase(routine);
		return runInductionPhase(routine);
		
	}
	
	//@Test
	public void testPointerInduction1() throws Exception {
		dumpIsel = true;
		boolean changed = doOpt(
				"vals: Int[32]; \n" + 
				"doSum = code() {\n" + 
				"    s := 0;\n" + 
				"    for i in 32 do s += vals[i];\n" + 
				"};"+ 
		"");
		
		assertTrue(changed);
		
		int idx = -1;
		
		
		
	}

}

