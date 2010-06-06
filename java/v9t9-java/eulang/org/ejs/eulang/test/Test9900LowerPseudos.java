/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.*;

import org.ejs.eulang.llvm.tms9900.AsmInstruction;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.RoutineDumper;
import org.ejs.eulang.llvm.tms9900.asm.CompositePieceOperand;
import org.ejs.eulang.llvm.tms9900.asm.CompareOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.junit.Test;

import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;

/**
 * @author ejs
 *
 */
public class Test9900LowerPseudos extends BaseInstrTest {

	protected boolean doOpt(String string) throws Exception {
		routine = doIsel(string);
		routine.setupForOptimization();
	
		return doOpt(routine);
	}
	protected boolean doOpt(Routine routine) {
		
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
    	matchInstr(inst, "MOV", RegTempOperand.class, "x", CompositePieceOperand.class);
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegTempOperand.class, "y", CompositePieceOperand.class, 2);
    	
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegTempOperand.class, "y", CompositePieceOperand.class);
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegTempOperand.class, "x", CompositePieceOperand.class, 2);
    	
    	// be sure we don't incorrectly optimize 
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, CompositePieceOperand.class, ".callerRet", 0);
    	idx = findInstrWithSymbol(instrs, "cond", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, 2, CompositePieceOperand.class, ".callerRet", 2);

    	// make sure we have piecewise access that doesn't kill on each write
    	ILocal local = stackFrame.getLocal(getOperandSymbol(inst.getOp1()));
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
    	
    	// the copies are expanded as a loop
    	
    	idx = findInstrWithSymbol(instrs, "x", idx);
    	inst = instrs.get(idx);
    	idx = findInstrWithSymbol(instrs, "y", idx);
    	inst = instrs.get(idx);
    	idx = findInstrWithInst(instrs, "LI", idx);
    	inst = instrs.get(idx);
    	
    	matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 20);
    	
    	idx = findInstrWithInst(instrs, "JMP", idx);
    	inst = instrs.get(idx);
    	idx = findInstrWithInst(instrs, "MOV", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegIncOperand.class, RegIncOperand.class);
    	
    	// make sure *R+ is properly recorded
    	ILocal loc1 = stackFrame.getLocal(getOperandSymbol(inst.getOp1()));
    	assertTrue(loc1.getUses().get(inst.getNumber()));
    	assertTrue(loc1.getDefs().get(inst.getNumber()));
    	
    	idx = findInstrWithInst(instrs, "DECT", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "DECT", RegTempOperand.class);
    	idx = findInstrWithInst(instrs, "JCC", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "JCC", CompareOperand.class, CompareOperand.CMP_SGT);

	}


	@Test
	public void testTupleCopy() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
    			"Tuple = data { a, b : Byte; c: Int; };\n"+
    			"testTupleCopy = code(x, y, z : Int) {\n"+
    			"  t : Tuple = [ .a=x, .b=y, .c=10 ];\n"+
    			"  val := 10;\n"+
    			"  t.b += val;\n"+
    			"  t;\n"+
    			"};\n"+
    	"");
    	
		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;

    	// don't substitute byte-valued register here (the temps are bytes, not ints) 
    	idx = findInstrWithSymbol(instrs, ".callerRet", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, "t", 0, CompositePieceOperand.class, ".callerRet", 0);
    	
    	idx = findInstrWithSymbol(instrs, ".callerRet", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, "t", 2, CompositePieceOperand.class, ".callerRet", 2);

	}

	@Test
	public void testTupleCopy2() throws Exception {
		// test with globals
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
    			"Tuple = data { a, b : Byte; c: Int; };\n"+
    			"t : Tuple;\n"+
    			"testTupleCopy = code(x, y, z : Int) {\n"+
    			"  t.a=x; t.b=y; t.c=10;\n"+
    			"  val := 10;\n"+
    			"  t.b += val;\n"+
    			"  t;\n"+
    			"};\n"+
    	"");
    	
		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;

    	// don't substitute byte-valued register here (the temps are bytes, not ints) 
    	idx = findInstrWithSymbol(instrs, ".callerRet", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, "t", 0, CompositePieceOperand.class, ".callerRet", 0);
    	
    	idx = findInstrWithSymbol(instrs, ".callerRet", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, "t", 2, CompositePieceOperand.class, ".callerRet", 2);

	}

	@Test
	public void testTupleCopy3() throws Exception {
		// test with pointer
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
    			"Tuple = data { a, b : Byte; c: Int; };\n"+
    			"testTupleCopy = code(x, y, z : Int; t:Tuple^) {\n"+
    			"  t.a=x; t.b=y; t.c=10;\n"+
    			"  val := 10;\n"+
    			"  t.b += val;\n"+
    			"  t^;\n"+
    			"};\n"+
    	"");
    	
		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;

    	// don't substitute byte-valued register here (the temps are bytes, not ints) 
    	idx = findInstrWithSymbol(instrs, ".callerRet", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, "t", 0, CompositePieceOperand.class, ".callerRet", 0);
    	
    	idx = findInstrWithSymbol(instrs, ".callerRet", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, "t", 2, CompositePieceOperand.class, ".callerRet", 2);

	}


	@Test
	public void testDataCopy1() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
    			"Tuple = data { x,y,z,a : Byte; };\n"+
    			"testDataCopy1 = code(p,q,r,s:Byte) {\n"+
    			"  y : Tuple = [p,2,3,4];\n"+
    			"};\n"+
    	"");
    	
		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;
    	
    	// be sure we actually load AND store the stuff
    	idx = findInstrWithInst(instrs, "LI", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 0x0200);
    	
    	idx = findInstrWithInst(instrs, "MOVB", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOVB", RegTempOperand.class, CompositePieceOperand.class, "y", 1);
    	
    	idx = findInstrWithInst(instrs, "LI", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 0x0300);
    	
    	idx = findInstrWithInst(instrs, "MOVB", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOVB", RegTempOperand.class, CompositePieceOperand.class, "y", 2);
    	
    	idx = findInstrWithInst(instrs, "LI", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 0x0400);
    	
    	idx = findInstrWithInst(instrs, "MOVB", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOVB", RegTempOperand.class, CompositePieceOperand.class, "y", 3);
    	
    	idx = findInstrWithSymbol(instrs, ".callerRet", idx);
    	

	}

	
	@Test
	public void testStringCopy1() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
    			"testTupleCopy = code() {\n"+
    			"  y := \"Hello\";\n"+
    			"};\n"+
    	"");
    	
		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;
    	
    	// string is const so use loop to copy
    	idx = findInstrWithInst(instrs, "LI", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 0x0007);
    	
    	idx = findInstrWithInst(instrs, "JMP", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "JMP");
    	
    	idx = findInstrWithInst(instrs, "MOV", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", RegIncOperand.class, RegIncOperand.class);
    	
    	idx = findInstrWithInst(instrs, "DECT", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "DECT", RegTempOperand.class);
    	
    	idx = findInstrWithInst(instrs, "JCC", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "JCC", CompareOperand.class, CompareOperand.CMP_SGT);
    	
    	// now, we need one last copy
    	idx = findInstrWithInst(instrs, "MOVB", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOVB", RegIndOperand.class, RegIndOperand.class);
    	
    	

	}

	@Test
	public void testClearOdd1() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
    			"testClearOdd1 = code() {\n"+
    			"  y : Byte[7] = [];\n"+
    			"};\n"+
    	"");
    	
		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;
    	
    	// clear items
    	idx = findInstrWithInst(instrs, "CLR", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "CLR", CompositePieceOperand.class, "y", 0);
    	idx = findInstrWithInst(instrs, "CLR", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "CLR", CompositePieceOperand.class, "y", 2);
    	idx = findInstrWithInst(instrs, "CLR", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "CLR", CompositePieceOperand.class, "y", 4);
    	idx = findInstrWithInst(instrs, "SB", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "SB", CompositePieceOperand.class, "y", 6, CompositePieceOperand.class, "y", 6);

	}


}

