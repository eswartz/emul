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
import org.ejs.eulang.llvm.tms9900.Locals;
import org.ejs.eulang.llvm.tms9900.PeepholeAndLocalCoalesce;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.RoutineDumper;
import org.ejs.eulang.llvm.tms9900.asm.AddrOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.junit.Test;

import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;

/**
 * @author ejs
 *
 */
public class Test9900Optimizer extends BaseInstrTest {

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
		System.out.println("\n*** Initial:\n");
		routine.accept(new RoutineDumper());
		
		PeepholeAndLocalCoalesce peepholeAndLocalCoalesce = new PeepholeAndLocalCoalesce();
		boolean anyChanges = false;
		do {
			routine.accept(peepholeAndLocalCoalesce);
			if (peepholeAndLocalCoalesce.isChanged()) {
				System.out.println("\n*** After pass:\n");
				routine.accept(new RoutineDumper());
				anyChanges = true;
				routine.setupForOptimization();
			}
		} while (peepholeAndLocalCoalesce.isChanged());
		
		assertNoUndefinedLocals(routine.getLocals());

		if (!anyChanges)
			System.out.println("\n*** No changes");
		else {
			System.out.println("\n*** Final:\n");
			routine.accept(new RoutineDumper());
		}
		
		instrs.clear();
		for (Block block : routine.getBlocks())
			instrs.addAll(block.getInstrs());
		
		return anyChanges;
		
	}
	
	/**
	 * @param routine
	 * @param string
	 * @return
	 */
	protected Block getBlock(Routine routine, String string) {
		for (Block block : routine.getBlocks()) {
			if (block.getLabel().getName().equals(string))
				return block;
		}
		string += ".";
		for (Block block : routine.getBlocks()) {
			if (block.getLabel().getName().startsWith(string))
				return block;
		}
		return null;
	}
	/**
	 * @param string
	 * @return
	 */
	protected ILocal getLocal(String name) {
		for (ILocal local : locals.getAllLocals()) {
			if (local.getName().getName().equals(name))
				return local;
		}
		name = "." + name + ".";
		for (ILocal local : locals.getAllLocals()) {
			if (local.getName().getUniqueName().contains(name))
				return local;
		}
		return null;
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
			
			ILocal target = locals.getLocal(inst.getTargets()[0]);
			assertEquals(1, target.getUses().cardinality());
		}			
	}

	/**
	 * @param locals
	 */
	protected void assertNoUndefinedLocals(Locals locals) {
		for (ILocal local : locals.getAllLocals()) {
			if (local.getDefs().isEmpty() && local.getUses().isEmpty()) {
				fail(local+" still present");
			}
		}
		
	}
	@Test
	public void testPeephole1() throws Exception {
		dumpIsel = true;
		doOpt("foo = code() { x := 1; x += 11; };\n");

		// should boil down to "li r0, 12"
		assertEquals(1, locals.getAllLocals().length);
		
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
		assertEquals(1, locals.getAllLocals().length);
		

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
		matchInstr(inst, "A", AddrOffsOperand.class, "foo", 2, val1);
		
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", AddrOffsOperand.class, "foo", 4, val1);

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
		matchInstr(inst, "MOV", AddrOffsOperand.class, "foo", 2, RegTempOperand.class, 0);
		AssemblerOperand val2 = inst.getOp2();
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", val1, val2);
		
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOC", AddrOffsOperand.class, "foo", 4, val1);
		
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
		matchInstr(inst, "MOV", AddrOffsOperand.class, "foo", 8, RegTempOperand.class);
		AssemblerOperand val1 = inst.getOp2();
		
		// this must go in R0, so another move -- but we have a value to use
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", val1, RegTempOperand.class, 0);
		AssemblerOperand val2 = inst.getOp2();
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", val1, val2);

		// we've trashed the vr holding the stack var, so one more read
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOC", AddrOffsOperand.class, "foo", 8, val1);
		
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
		matchInstr(inst, "MOV", RegTempOperand.class, RegIndOperand.class);
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

		// be sure we read and write the value to memory every time
		idx = findInstrWithInst(body.getInstrs(), "MOV");
		inst = body.getInstrs().get(idx);
		matchInstr(inst, "MOV", AddrOffsOperand.class, "foo", 8, RegTempOperand.class);
		AssemblerOperand mem = inst.getOp1();
		AssemblerOperand v = inst.getOp2();
		
		// move shift into R0
		idx = findInstrWithInst(body.getInstrs(), "MOV", idx);
		inst = body.getInstrs().get(idx);
		matchInstr(inst, "MOV", RegIndOperand.class, "foo", RegTempOperand.class, 0);
		
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
		
		// Don't read and write same memory  (loopValue = ...).  
		// And ensure we can cleanly put loop value into return.
		idx = findInstrWithInst(exit.getInstrs(), "MOV");
		inst = exit.getInstrs().get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "loopValue", RegTempOperand.class, 0);
		
	}
}

