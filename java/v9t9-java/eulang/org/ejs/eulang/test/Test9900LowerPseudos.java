/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.*;

import java.util.BitSet;

import org.ejs.eulang.llvm.tms9900.AsmInstruction;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.ICodeVisitor;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.LowerPseudoInstructions;
import org.ejs.eulang.llvm.tms9900.PeepholeAndLocalCoalesce;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.RoutineDumper;
import org.ejs.eulang.llvm.tms9900.asm.LocalOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.StackLocalOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.CompareOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolLabelOperand;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.junit.Test;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;

/**
 * @author ejs
 *
 */
public class Test9900LowerPseudos extends BaseInstrTest {

	/**
	 * @param string
	 * @throws Exception 
	 */
	private boolean doOpt(String string) throws Exception {
		routine = doIsel(string);
		routine.setupForOptimization();
	
		return doOpt(routine);
	}
	private boolean doOpt(Routine routine) {
		
		boolean anyLowered = false;
		do {
			runPeepholePhase(routine);
			boolean lowered = runLowerPseudoPhase(routine);
			anyLowered |= lowered;
			if (!lowered)
				break;
		} while (true);
		
		if (!anyLowered)
			System.out.println("\n*** No changes");
		else {
			System.out.println("\n*** Done:\n");
			routine.accept(new RoutineDumper());
		}
		
		
		return anyLowered;
	}
	
	protected boolean runLowerPseudoPhase(Routine routine) {
		System.out.println("\n*** Before lowering:\n");
		routine.accept(new RoutineDumper());
		
		LowerPseudoInstructions lower = new LowerPseudoInstructions();
		boolean anyChanges = false;
		do {
			try {
				routine.accept(lower);
			} catch (ICodeVisitor.Terminate e) {
				
			}
			if (lower.isChanged()) {
				System.out.println("\n*** After lowering pass:\n");
				routine.accept(new RoutineDumper());
				anyChanges = true;
				
				routine.setupForOptimization();
			}
		} while (lower.isChanged());
		

		if (!anyChanges)
			System.out.println("\n*** No changes");
		else {
			System.out.println("\n*** Done lowering:\n");
			routine.accept(new RoutineDumper());
		}
		
		validateInstrsAndResync(routine);
		return anyChanges;
		
	}
	

	@Test
	public void testTuplesSwap() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
		boolean changed = doOpt("swap = code (x,y:Int;b ) { if b then (x,y) else (y,x) };\n");

		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;

    	// ensure jump is status-based
    	idx = findInstrWithInst(instrs, "CB", idx);
    	assertTrue(idx != -1);

    	idx = findInstrWithInst(instrs, "JCC", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "JCC", CompareOperand.class, CompareOperand.CMP_NE);
    	assertEquals(3, inst.getSources().length);
    	assertSameSymbol(inst.getSources()[0], ".status");

    	assertEquals(-1, findInstrWithInst(instrs, "COPY", -1));
    	
    	// the copies are expanded as MOVs
    	
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegTempOperand.class, "x", StackLocalOffsOperand.class);
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegTempOperand.class, "y", StackLocalOffsOperand.class, 2);
    	
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegTempOperand.class, "y", StackLocalOffsOperand.class);
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegTempOperand.class, "x", StackLocalOffsOperand.class, 2);
    	
    	// be sure we don't incorrectly optimize 
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", StackLocalOffsOperand.class, RegIndOperand.class, 0);
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", StackLocalOffsOperand.class, 2, RegOffsOperand.class, 0, 2);

    	// make sure we have piecewise access that doesn't kill on each write
    	ILocal local = locals.getLocal(getOperandSymbol(inst.getOp1()));
    	assertLocalIsNeverKilled(local);
    	assertEquals(4, local.getDefs().cardinality());
    	assertEquals(6, local.getUses().cardinality());

	}
	

	@Test
	public void testCopyExpand() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
		boolean changed = doOpt("swap = code (x:Int[10]; y:Int[10]^) { y^=x; };\n");

		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;

    	assertEquals(-1, findInstrWithInst(instrs, "COPY", -1));
    	
    	// the copies are expanded as MOVs
    	
    	idx = findInstrWithSymbol(instrs, "x", idx);
    	inst = instrs.get(idx);
    	

	}

}

