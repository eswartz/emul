/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.*;

import org.ejs.eulang.llvm.tms9900.AsmInstruction;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.asm.CompositePieceOperand;
import org.ejs.eulang.llvm.tms9900.asm.CompareOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolLabelOperand;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.junit.Test;

import v9t9.engine.cpu.InstructionTable;
import static v9t9.engine.cpu.InstructionTable.*;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;

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

