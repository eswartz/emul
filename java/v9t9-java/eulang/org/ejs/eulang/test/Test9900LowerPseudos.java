/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.*;

import org.ejs.eulang.llvm.tms9900.AsmInstruction;
import org.ejs.eulang.llvm.tms9900.ICodeVisitor;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.LowerPseudoInstructions;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.RoutineDumper;
import org.ejs.eulang.llvm.tms9900.asm.CompositePieceOperand;
import org.ejs.eulang.llvm.tms9900.asm.CompareOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.junit.Test;

import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
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
    	ILocal loc1 = locals.getLocal(getOperandSymbol(inst.getOp1()));
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

}

