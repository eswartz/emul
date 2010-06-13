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
public class Test9900Optimizer extends BaseInstrTest {

	protected boolean doOpt(String string) throws Exception {
		routine = doIsel(string);
		routine.setupForOptimization();
	
		return doOpt(routine);
	}
	protected boolean doOpt(Routine routine) {
		return runPeepholePhase(routine);
		
	}
	
	@Test
	public void testDefOnly() throws Exception {
		doIsel("foo = code( => nil) { x := 1 };\n");
		routine.setupForOptimization();
		
		ILocal local;
		
		local = getLocal("x");
		assertEquals(0, local.getUses().cardinality());
		assertEquals(1, local.getDefs().cardinality());
		assertTrue(local.isExprTemp());
		assertTrue(local.isSingleBlock());
	}
	@Test
	public void testDefUse1() throws Exception {
		dumpIsel = true;
		doIsel("foo = code() { x := 1 };\n");
		routine.setupForOptimization();
		
		ILocal local;
		local = getLocal("x");
		
		assertEquals(1, local.getUses().cardinality());
		assertEquals(1, local.getDefs().cardinality());
		assertTrue(local.isExprTemp());
		assertTrue(local.isSingleBlock());
	}
	@Test
	public void testDefUse2() throws Exception {
		dumpIsel = true;
		doIsel("foo = code() { x := 1; x += 11; };\n");
		routine.setupForOptimization();
		
		ILocal local;
		local = getLocal("x");
		
		assertEquals(2, local.getUses().cardinality());
		assertEquals(2, local.getDefs().cardinality());
		assertFalse(local.isExprTemp());
		assertTrue(local.isSingleBlock());
	}
	@Test
	public void testDefUse3() throws Exception {
		dumpIsel = true;
		doIsel("foo = code(foo:Int[10]) { foo[0] + foo[1] + foo[2];  };\n");
		routine.setupForOptimization();
		
		int idx = -1;
		while (true) {
			idx = findInstrWithInst(instrs, "LEA", idx);
			if (idx == -1)
				break;
			AsmInstruction inst = instrs.get(idx);
			assertEquals(1, inst.getSources().length);
			assertEquals(1, inst.getTargets().length);
			
			ILocal target = stackFrame.getLocal(inst.getTargets()[0]);
			assertEquals(1, target.getUses().cardinality());
		}			
	}

	@Test
	public void testPeephole1() throws Exception {
		dumpIsel = true;
		doOpt("foo = code() { x := 1; x += 11; };\n");

		// should boil down to "li r0, 12"
		assertEquals(1, stackFrame.getAllLocals().length);
		
		ILocal local;
		local = getLocal("x");
		assertNull(local);
		
		AsmInstruction inst = instrs.get(findInstrWithInst(instrs, "LI"));
		matchInstr(inst, "LI", RegTempOperand.class, 0, NumberOperand.class, 12);
	}
	@Test
	public void testPeephole1b() throws Exception {
		dumpIsel = true;
		boolean anyChanges = doOpt("foo = code() { x:=1; y:=x; z:=y; a:=z; b:=a; c:=b; c;  };\n");

		assertTrue(anyChanges);
		
		// only the R0 return temp should be left
		assertEquals(1, stackFrame.getAllLocals().length);
		

		AsmInstruction inst = instrs.get(findInstrWithInst(instrs, "LI"));
		matchInstr(inst, "LI", RegTempOperand.class, 0, NumberOperand.class, 1);
	}
	
	/**
	 * Remove a lot of needless address calculation and memory->register moves here.
	 * @throws Exception
	 */
	@Test
	public void testPeephole2() throws Exception {
		dumpIsel = true;
		boolean anyChanges = doOpt("foo = code(foo:Int[10]) { foo[0] + foo[1] + foo[2];  };\n");

		assertTrue(anyChanges);
		
		int idx = findInstrWithInst(instrs, "LEA", -1);
		assertEquals(-1, idx);
		
		AsmInstruction inst;
		idx = findInstrWithSymbol(instrs, "foo", -1);
		inst = instrs.get(idx);
		
		// establish temp
		matchInstr(inst, "MOV", AddrOperand.class, "foo", RegTempOperand.class);
		AssemblerOperand val1 = inst.getOp2();
		
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", CompositePieceOperand.class, "foo", 2, val1);
		
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", CompositePieceOperand.class, "foo", 4, val1);

		assertEquals(-1, findInstrWithSymbol(instrs, "foo", idx));
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", val1, RegTempOperand.class, 0);
		
	}
	
	/**
	 * Remove a lot of needless address calculation and memory->register moves here.
	 * @throws Exception
	 */
	@Test
	public void testPeephole2b() throws Exception {
		dumpIsel = true;
		boolean anyChanges = doOpt("foo = code(foo:Int[10]) { foo[0] >> foo[1] | foo[2];  };\n");

		assertTrue(anyChanges);
		
		int idx = findInstrWithInst(instrs, "LEA", -1);
		assertEquals(-1, idx);
		
		AsmInstruction inst;
		idx = findInstrWithSymbol(instrs, "foo", -1);
		inst = instrs.get(idx);
		
		// establish temp
		matchInstr(inst, "MOV", AddrOperand.class, "foo", RegTempOperand.class);
		AssemblerOperand val1 = inst.getOp2();
		
		// this must go in R0, so another move
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", CompositePieceOperand.class, "foo", 2, RegTempOperand.class, 0);
		AssemblerOperand val2 = inst.getOp2();
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", val1, val2);
		
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOC", CompositePieceOperand.class, "foo", 4, val1);
		
		assertEquals(-1, findInstrWithSymbol(instrs, "foo", idx));
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", val1, RegTempOperand.class, 0);
		
	}
	
	/**
	 * Ensure only one memory read: the value is available in temps and needn't be read again
	 * @throws Exception
	 */
	@Test
	public void testPeephole3() throws Exception {
		dumpIsel = true;
		boolean anyChanges = doOpt("foo = code(foo:Int[10]) { foo[4] >> foo[4] | foo[4];  };\n");

		assertTrue(anyChanges);
		
		int idx = findInstrWithInst(instrs, "LEA", -1);
		assertEquals(-1, idx);
		
		AsmInstruction inst;
		idx = findInstrWithSymbol(instrs, "foo", -1);
		inst = instrs.get(idx);
		
		// establish temp, reading once from memory
		matchInstr(inst, "MOV", AddrOperand.class, "foo", 8, RegTempOperand.class);
		AssemblerOperand val1 = inst.getOp2();
		
		// this must go in R0, so another move -- but we have a value to use
		assertEquals(-1, findInstrWithSymbol(instrs, "foo", idx));
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", val1, RegTempOperand.class, 0);
		AssemblerOperand val2 = inst.getOp2();
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", val1, val2);
		
		idx = findInstrWithInst(instrs, "SOC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOC", val2, val1);
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", val1, RegTempOperand.class, 0);
		
	}
	

	/**
	 * Validate pointer ops, and another case where we want to avoid memory reads. 
	 * @throws Exception
	 */
	@Test
	public void testPeephole4() throws Exception {
		dumpIsel = true;
		boolean anyChanges = doOpt("foo = code(foo:Int[10]^) { foo[4] >> foo[4] | foo[4];  };\n");

		assertTrue(anyChanges);
		
		int idx = findInstrWithInst(instrs, "LEA", -1);
		assertEquals(-1, idx);
		
		AsmInstruction inst;
		idx = findInstrWithSymbol(instrs, "foo", -1);
		inst = instrs.get(idx);
		
		// establish foo (ptr)
		matchInstr(inst, "MOV", RegTempOperand.class, "foo", RegTempOperand.class);
		
		// get foo[4]
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", CompositePieceOperand.class, "foo", 8, RegTempOperand.class);
		AssemblerOperand val1 = inst.getOp2();
		
		// this must go in R0, so another move -- but we have a value to use
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", val1, RegTempOperand.class, 0);
		AssemblerOperand val2 = inst.getOp2();
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", val1, val2);

		// R0 holds the foo[4] still
		idx = findInstrWithInst(instrs, "SOC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOC", val2, val1);
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", val1, RegTempOperand.class, 0);
		
	}
	

	/**
	 * Validate pointer ops, #2
	 * @throws Exception
	 */
	@Test
	public void testPeephole5() throws Exception {
		dumpIsel = true;
		boolean anyChanges = doOpt("foo = code(foo:Int^) { foo^ = 6; foo^; };\n");

		assertTrue(anyChanges);
		
		AsmInstruction inst;
		int idx = findInstrWithInst(instrs, "LI", -1);
		inst = instrs.get(idx);
		
		// don't fold 6 into an LI!
		assertFalse(inst.getOp1().isMemory());
		
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, RegIndOperand.class, "foo");
		AssemblerOperand val = inst.getOp1();
		
		// don't re-read from memory
		assertEquals(-1, findInstrWithSymbol(instrs, "foo", idx));

		// also, don't re-load the value with LI -- it's easier to use the existing register
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", val, RegTempOperand.class, 0);
		
	}
	/**
	 * Make sure we don't do dumb stuff in loops 
	 * @throws Exception
	 */
	@Test
	public void testPeepholeLoops1() throws Exception {
		dumpIsel = true;
		String text = "foo = code(foo:Int[10]^) { repeat 10 do foo[4] >>= foo[0] ;  };\n";
		
		routine = doIsel(text);
		routine.setupForOptimization();

		// make sure we have sensible blocks...
		Block entry = getBlock(routine, "entry");
		Block enter = getBlock(routine, "loopEnter");
		Block body = getBlock(routine, "loopBody");
		Block exit = getBlock(routine, "loopExit");
		
		assertEquals(1, entry.succ().size());
		assertTrue(entry.succ().contains(enter));
		
		assertEquals(2, enter.succ().size());
		assertTrue(enter.succ().contains(exit));
		assertTrue(enter.succ().contains(body));
		
		assertEquals(1, body.succ().size());
		assertTrue(body.succ().contains(enter));
		
		assertEquals(0, exit.succ().size());
		
		////////
		
		boolean anyChanges = doOpt(routine);

		assertTrue(anyChanges);
		
		AsmInstruction inst;
		int idx;

		// make sure jump was converted
		idx = findInstrWithInst(enter.getInstrs(), "JCC");
		inst = enter.getInstrs().get(idx);
		matchInstr(inst, "JCC", CompareOperand.class, CompareOperand.CMP_EQ, SymbolLabelOperand.class, SymbolLabelOperand.class);
		assertSameSymbol(inst, inst.getSources()[0], ".status");
		
		// be sure we read and write the value to memory every time
		idx = findInstrWithInst(body.getInstrs(), "MOV");
		inst = body.getInstrs().get(idx);
		matchInstr(inst, "MOV", CompositePieceOperand.class, "foo", 8, RegTempOperand.class);
		AssemblerOperand mem = inst.getOp1();
		AssemblerOperand v = inst.getOp2();
		
		// move shift into R0
		idx = findInstrWithInst(body.getInstrs(), "MOV", idx);
		inst = body.getInstrs().get(idx);
		matchInstr(inst, "MOV", CompositePieceOperand.class, "foo", 0, RegTempOperand.class, 0);
		
		// do shift
		idx = findInstrWithInst(body.getInstrs(), "SRA", idx);
		inst = body.getInstrs().get(idx);
		matchInstr(inst, "SRA", v, RegTempOperand.class, 0);

		// copy back
		idx = findInstrWithInst(body.getInstrs(), "MOV", idx);
		inst = body.getInstrs().get(idx);
		matchInstr(inst, "MOV", v, mem);
		
		
		// confusion with foo[4] <<= ...
		idx = findInstrWithInst(body.getInstrs(), "SRA");
		inst = body.getInstrs().get(idx);
		assertFalse(symbolMatches(getOperandSymbol(inst.getOp1()), "foo"));
		
		// double-check DEC usage
		idx = findInstrWithInst(body.getInstrs(), "DEC");
		inst = body.getInstrs().get(idx);
		matchInstr(inst, "DEC", RegTempOperand.class, "counter");
		
		// Don't read and write same memory  (loopValue = ...).  
		// And ensure we can cleanly put loop value into return.
		idx = findInstrWithInst(exit.getInstrs(), "MOV");
		inst = exit.getInstrs().get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "loopValue", RegTempOperand.class, 0);

	}
	
	/**
	 * Can't coalesce with register pairs  
	 * @throws Exception
	 */
	@Test
	public void testPeepholeRegPairs() throws Exception {
		dumpIsel = true;
		doOpt("foo = code(x:Int;y:Int) { p := x*y; q := y*x; (p, q) };\n");

		// validation checks the hi/lo usage
		
	}
	

	@Test
	public void testDataLocalUse1() throws Exception {
		dumpIsel = true;
	   	boolean changed = doOpt(
	   			"Tuple = data {\n"+
	   			"   x:Byte; f:Bool; y,z:Byte; };\n"+
	   			"testDataInit1 = code() {\n"+
	   			"  foo:Tuple = nil;\n"+
	   			"  foo.x = 3; foo.f = 1; foo.y = 0x20; foo.z = 0x10;\n"+
	   			"  if foo.f then foo.x else foo.y<<foo.z;\n" +
	   			"};\n"+
	   	"");
	   	assertTrue(changed);
	   	
	   	for (AsmInstruction inst : instrs) {
    		if (inst.getInst() == InstructionTable.Ili) {
    			if ((((NumberOperand)inst.getOp2()).getValue() & 0xffff) == 0)
    				fail(inst+": lost value");
    		}
    	}
	   	
	   	int idx;
	   	AsmInstruction inst;
	   	
	   	// we should have substituted all values and no longer need the memory
    	idx = findInstrWithSymbol(instrs, "foo");
    	assertEquals(-1, idx);
    	
    	
		idx = findInstrWithInst(instrs, "SWPB");
		inst = instrs.get(idx);
		matchInstr(inst, "SWPB", RegTempOperand.class, 0);
		AssemblerOperand shift = inst.getOp1();
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, shift);
	 }
	

	@Test
	public void testDataNonLocalUse1() throws Exception {
		dumpIsel = true;
	   	boolean changed = doOpt(
	   			"Tuple = data {\n"+
	   			"   x:Byte; f:Bool; y,z:Byte; };\n"+
	   			"testDataInit1 = code(foo:Tuple^) {\n"+
	   			"  foo.x = 3; foo.f = 1; foo.y = 0x20; foo.z = 0x10;\n"+
	   			"  if foo.f then foo.x else foo.y<<foo.z;\n" +
	   			"};\n"+
	   	"");
	   	assertTrue(changed);
	   	
	   	for (AsmInstruction inst : instrs) {
    		if (inst.getInst() == InstructionTable.Ili) {
    			if ((((NumberOperand)inst.getOp2()).getValue() & 0xffff) == 0)
    				fail(inst+": lost value");
    		}
    	}
	   	
	   	int idx;
	   	AsmInstruction inst;
	   	
	   	// we should keep refs to the symbol
    	idx = findInstrWithSymbol(instrs, "foo");
    	assertFalse(-1 == idx);
    	
    	
		idx = findInstrWithInst(instrs, "SWPB");
		inst = instrs.get(idx);
		matchInstr(inst, "SWPB", RegTempOperand.class, 0);
		AssemblerOperand shift = inst.getOp1();
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, shift);
	 }
	
	@Test
    public void testDataInit1() throws Exception {
		dumpIsel = true;
		routine = doIsel(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Bool; y,z:Byte; };\n"+
    			"testDataInit1 = code() {\n"+
    			"  foo:Tuple = [ 3, 1, .z=0x4, .y=0x20 ];\n"+
    			"   if foo.f then foo.x else foo.y<<foo.z;\n" +
    			"};\n"+
    	"");
		
    	int idx;
    	AsmInstruction inst;

    	routine.setupForOptimization();
    	
    	// validate def/use
		idx = findInstrWithInst(instrs, "COPY");
		inst = instrs.get(idx);
		ILocal foo = stackFrame.getLocal(getOperandSymbol(inst.getOp2()));
		assertNotNull(foo);
		assertEquals(1, foo.getDefs().cardinality());
		assertFalse(foo.getUses().get(foo.getDefs().nextSetBit(0)));	// is not read where written
		assertEquals(4, foo.getUses().cardinality());	// four reads
    	
		
		boolean changed = doOpt(routine);
    	assertTrue(changed);
    	
    	// ensure we use proper byte-shifted version of constants
    	int matches = 0;
	   	for (AsmInstruction ins : instrs) {
    		if (ins.getInst() == InstructionTable.Ili) {
    			int v = ((NumberOperand)ins.getOp2()).getValue();
				if ((v & 0xffff) == 0)
    				fail(inst+": lost value");
    			if (v == 0x4)
    				fail(ins+": expected >0400");
    			if (v == 0x20)
    				fail(ins+": expected >2000");
    			if (v == 0x3)
    				fail(ins+": expected >0300");
    			if (v == 0x1)
    				fail(ins+": expected >0100");
    			if (v == 0x100 || v == 0x300 || v == 0x2000 || v == 0x400)
    				matches++;
    		}
    	}
	   	assertTrue(matches > 2);
	   	
    	// we should have substituted all values and no longer need the memory
    	idx = findInstrWithSymbol(instrs, "foo");
    	assertEquals(-1, idx);
    	
		idx = findInstrWithInst(instrs, "SWPB");
		inst = instrs.get(idx);
		matchInstr(inst, "SWPB", RegTempOperand.class, 0);
		AssemblerOperand shift = inst.getOp1();
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, shift);
    }
	
	@Test
    public void testLocalAddrRef1() throws Exception {
		dumpIsel = true;
		boolean changed = doOpt(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Bool; y,z:Byte; };\n"+
    			"testLocalAddrRef1 = code() {\n"+
    			"  foo:Tuple;\n"+
    			"  fooptr:Tuple^=&foo;\n"+
    			"  fooptr.y+fooptr.x;\n" +
    			"};\n"+
    	"");
		
    	int idx;
    	AsmInstruction inst;
		
    	assertTrue(changed);

    	// fold fooptr into the accesses
		idx = findInstrWithInst(instrs, "MOVB", -1);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", CompositePieceOperand.class, "foo", 2, RegTempOperand.class);
		
		// don't do   AB *R(Local._.foo),vr26(%6.2)
		idx = findInstrWithInst(instrs, "AB",   idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", AddrOperand.class, "foo", RegTempOperand.class);
    }
	

	@Test
    public void testLocalAddrRef2() throws Exception {
		dumpIsel = true;
		boolean changed = doOpt(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Bool; y,z:Byte; };\n"+
    			"testLocalAddrRef2 = code() {\n"+
    			"  foo:Tuple;\n"+
    			"  fooptr:Tuple^=&foo;\n"+
    			"  fooptr.y+fooptr.z;\n" +
    			"};\n"+
    	"");
		
    	int idx;
    	AsmInstruction inst;
		
    	assertTrue(changed);
		
		idx = findInstrWithInst(instrs, "MOVB", -1);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", CompositePieceOperand.class, "foo", 2, RegTempOperand.class);
		
		idx = findInstrWithInst(instrs, "AB",   idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", CompositePieceOperand.class, "foo", 3, RegTempOperand.class);
    }


    @Test
    public void testDataInitVar1() throws Exception {
    	dumpIsel = true;
    	boolean changed = doOpt(
    			"testDataInitVar1 = code() {\n"+
    			"  val := 10;\n"+
    			"  foo:Int[10] = [ [5] = val, [1] = 11, 22 ];\n"+
    			"  foo[1]+foo[4]+foo[5];" +
    			"};\n"+
    	"");
    	
    	assertTrue(changed);
    	
    	int idx;
    	AsmInstruction inst;

    	idx = findInstrWithSymbol(instrs, "foo");
    	assertEquals(-1, idx);
		idx = findInstrWithInst(instrs, "LI", -1);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 0, NumberOperand.class, 21);
    }

	@Test
    public void testDataInit4() throws Exception {
		dumpIsel = true;
		boolean changed = doOpt(
    			"testDataInit4 = code() {\n"+
    			"  foo:Byte[][3] = [ [ 1, 2, 3], [4, 5, 6], [7, 8, 9]];\n"+
    			"  foo[1][2] + foo[2][1];\n"+
    			"};\n"+
    	"");
		assertTrue(changed);
    	
    	int idx;
    	AsmInstruction inst;

		idx = findInstrWithInst(instrs, "LI", -1);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 0, NumberOperand.class, 0xe00);
    }
	
	@Test
	public void testDataInit4b() throws Exception {
		dumpIsel = true;
		boolean changed = doOpt(
				"testDataInit4 = code() {\n"+
				"  foo:Byte[][3] = [ [ 1, 2, 3], [4, 5, 6], [7, 8, 9]];\n"+
				"  foo[0][2] & foo[2][2];\n"+
				"};\n"+
		"");
		assertTrue(changed);
		
		int idx;
		AsmInstruction inst;
		
		idx = findInstrWithInst(instrs, "LI", -1);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 0, NumberOperand.class, 0x100);
	}
	
	@Test
	public void testTuples5() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
		// emitting constant tuples, tuple casting, cond list common type, etc.
		boolean changed = doOpt("testTuples5 = code (x) { (if x<10 then (1,(x+2){Byte}) else (2,1){(Byte,Byte)}){(Int,Int)}; };\n");
		assertTrue(changed);
		

    	int idx = -1;
    	AsmInstruction inst;

    	idx = findInstrWithInst(instrs, "JCC", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "JCC", CompareOperand.class, CompareOperand.CMP_SLT);

    	idx = findInstrWithInst(instrs, "COPY", idx);
    	assertTrue(idx != -1);
    	
    	idx = findInstrWithInst(instrs, "JMP", idx);
    	assertTrue(idx != -1);
    	
    	idx = findInstrWithInst(instrs, "COPY", idx);
    	assertTrue(idx != -1);
    	AssemblerOperand op = instrs.get(idx).getOp1();
    	
    	assertTrue(op instanceof TupleTempOperand);
    	AssemblerOperand top;
    	ILocal local;
    	if (!((TupleTempOperand) op).isConst()) {
	    	// the regs should have one def only (but not necc. become constants, since we'll just 
	    	// have to reconstruct regs for LI ops anyway)
			top = ((TupleTempOperand) op).get(0);
			local = stackFrame.getLocal(getOperandSymbol(top));
	    	assertNotNull(local);
	    	assertEquals(1, local.getDefs().cardinality());
	    	assertEquals(1, local.getUses().cardinality());
	    	
	    	top = ((TupleTempOperand) op).get(1);
			local = stackFrame.getLocal(getOperandSymbol(top));
	    	assertNotNull(local);
	    	assertEquals(1, local.getDefs().cardinality());
	    	assertEquals(1, local.getUses().cardinality());
    	}
    	
    	/// other branch needs a cast
    	
    	idx = findInstrWithInst(instrs, "MOVB", idx);
    	assertTrue(idx != -1);

    	idx = findInstrWithInst(instrs, "COPY", idx);
    	assertTrue(idx != -1);
    	op = instrs.get(idx).getOp1();
    	
    	assertTrue(op instanceof TupleTempOperand);
    	top = ((TupleTempOperand) op).get(0);
		local = stackFrame.getLocal(getOperandSymbol(top));
    	assertNotNull(local);
    	assertEquals(1, local.getDefs().cardinality());
    	assertEquals(1, local.getUses().cardinality());
    	
    	top = ((TupleTempOperand) op).get(1);
		local = stackFrame.getLocal(getOperandSymbol(top));
    	assertNotNull(local);
    	assertEquals(2, local.getDefs().cardinality());
    	assertEquals(2, local.getUses().cardinality());
    	

	}

	@Test
	public void testCopyExpand() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
		boolean changed = doOpt("swap = code (x:Int[10]; y:Int[10]^) { y^=x; };\n");

		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;
    	
    	// make sure there are only two copies:  one from x -> y^ and one for return

    	idx = findInstrWithInst(instrs, "COPY", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "COPY", AddrOperand.class, "x", RegIndOperand.class, "y");
    	
    	idx = findInstrWithInst(instrs, "COPY", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "COPY", RegIndOperand.class, "y", RegIndOperand.class, 0);
    	
    	assertEquals(-1, findInstrWithInst(instrs, "COPY", idx));

	}
	
	@Test
	public void testTupleCopy2() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
    			"Tuple = data { a, b : Byte; c: Int; };\n"+
    			"glob : Tuple;\n"+
    			"testTupleCopy = code(x, y, z : Int => nil) {\n"+
    			"  glob.a=x; glob.b=y; glob.c=10;\n"+
    			"  val := 10;\n"+
    			"  glob.b += val;\n"+
    			"};\n"+
    	"");
    	
		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;

    	// make sure we don't use LI/LEA for the global
    	
    	idx = findInstrWithSymbol(instrs, "glob", idx);
    	inst = instrs.get(idx);
    	assertTrue(inst+"", inst.getInst() != Ili);

    	// don't lose the ADD
    	idx = findInstrWithInst(instrs, "AI", -1);
    	if (idx == -1) {
    		idx = findInstrWithInst(instrs, "AB", -1);
    	}
    	assertTrue(idx != -1);
	}

	@Test
	public void testPeepholeNeg() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
				"x := 100;\n"+
				"negate = code(x:Int) { -x; };\n"+
				"");

		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;

    	// note: this is three insts (copy R0 -> temp, NEG temp, MOV temp -> R0)
    	// but will stay that way until we color registers later.
    	idx = findInstrWithInst(instrs, "NEG", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "NEG", RegTempOperand.class);

	}
	
	@Test
	public void testArraySum() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
				"testArraySum = code() {\n"+
				"  vals : Int[10];\n"+
				"  s := 0;\n"+
				"  for i in 10 do vals[i] = i;\n"+
				"  for i in 10 do s += vals[i];\n"+
				"};\n"+
				"");
    	assertTrue(changed);
    	
    	int idx = -1;
    	AsmInstruction inst;
    	idx = findInstrWithInst(instrs, "SLA", idx);
    	inst = instrs.get(idx);
    	idx = findInstrWithInst(instrs, "A", idx);
    	inst = instrs.get(idx);
    	
    	if (inst.getOp2() instanceof  AddrOperand)
    		fail(inst+": should not substitute addr here");
    	matchInstr(inst, "A", RegTempOperand.class, RegTempOperand.class);
    	
    	idx = findInstrWithInst(instrs, "A", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "A", RegTempOperand.class, RegTempOperand.class);
	}

	@Test
	public void testAddrCalc1() throws Exception {
		dumpLLVMGen = true;
		dumpIsel = true;
    	boolean changed = doOpt(
				"arr : Int[10,10];\n"+
				"negate = code(x:Int) { \n" +
				"	rowp:=&arr[5];\n" +
				"   colp:=&rowp[5];\n"+
				"   colp^;\n"+
				"};\n"+
				"");

		assertTrue(changed);

    	int idx = -1;
    	AsmInstruction inst;

    	idx = findInstrWithInst(instrs, "LEA", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "LEA", CompositePieceOperand.class, "arr", 5*10*2, RegTempOperand.class);

    	idx = findInstrWithInst(instrs, "MOV", idx);
    	inst = instrs.get(idx);
    	matchInstr(inst, "MOV", CompositePieceOperand.class, 5*2, RegTempOperand.class, 0);

	}
	
	@Test
	public void testArraySubAss1() throws Exception {
		dumpIsel = true;
		boolean changed = doOpt(
				"vals: Int[3,3];\n" + 
				"doSum = code(arr: Int[3,3]) {\n" + 
				"  s := 0;\n" + 
				"  for i in 3 do for j in 3 do arr[i,j] = (i+2)*(j+2);       // 4,6,8 | 6,9,12 | 8,12,16\n" + 
				"  for i in 3 do for j in 3 do vals[i][j] -= arr[i][j];\n" + 
				"};");

		assertTrue(changed);

    	int idx = -1;

    	idx = findInstrWithInst(instrs, "S");
    	assertTrue(idx != -1);
    	
	}

	@Test
	public void testSwpb() throws Exception {
		dumpIsel = true;
		boolean changed = doOpt(
				"src = code(x, y : Byte) {\n" +
				"  x <<| y;"+
				"};"+ 
		"");
		
		assertTrue(changed);
		
		int idx = -1;
		
		idx = findInstrWithInst(instrs, "SWPB");
		assertTrue(idx != -1);
	}
	@Test
	public void testPointerMath1() throws Exception {
		dumpIsel = true;
		boolean changed = doOpt(
				"vals: Int[3,3]; \n" + 
				"doSum = code() {\n" + 
				"    valp : Int^ = (&vals){Int^};\n" + 
				"    s := 0;\n" + 
				"    for i in 3 do for j in 3 do (valp+(i*3+j))^ = i*3+j+1;\n" + 
				"//    for i in 9 do (valp+i)^ = i+1;\n" + 
				"    for i in 3 do for j in 3 do s += vals[i][j];\n" + 
				"};"+ 
		"");
		
		assertTrue(changed);
		
		int idx = -1;
		
		// don't coalesce valp with a temp (correctly detect cross-block usage)
		idx = findInstrWithSymbol(instrs, "valp");
		assertTrue(idx != -1);
		
		
	}
}

