/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.ejs.eulang.llvm.tms9900.AsmInstruction;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.Locals;
import org.ejs.eulang.llvm.tms9900.PeepholeAndLocalCoalesce;
import org.ejs.eulang.llvm.tms9900.RoutineDumper;
import org.ejs.eulang.llvm.tms9900.asm.AddrOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.junit.Test;

import v9t9.tools.asm.assembler.operand.hl.AddrOperand;

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
		doIsel(string);
		routine.setupForOptimization();
		
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

		PeepholeAndLocalCoalesce peepholeAndLocalCoalesce = new PeepholeAndLocalCoalesce();
		do {
			routine.accept(peepholeAndLocalCoalesce);
		} while (peepholeAndLocalCoalesce.isChanged());
		
		routine.setupForOptimization();
		
		assertNoUndefinedLocals(routine.getLocals());
		
		ILocal local;
		local = getLocal("x");
		
		assertEquals(2, local.getUses().cardinality());
		assertEquals(1, local.getDefs().cardinality());
		assertTrue(local.isExprTemp());
		assertTrue(local.isSingleBlock());
	}
	@Test
	public void testPeephole2() throws Exception {
		dumpIsel = true;
		boolean anyChanges = doOpt("foo = code(foo:Int[10]) { foo[0] + foo[1] + foo[2];  };\n");

		assertTrue(anyChanges);
		
		assertNoUndefinedLocals(routine.getLocals());
		
		int idx = findInstrWithInst(instrs, "LEA", -1);
		assertEquals(-1, idx);
		
		AsmInstruction inst;
		idx = findInstrWithSymbol(instrs, "foo", -1);
		inst = instrs.get(idx);
		
		matchInstr(inst, "MOV", AddrOperand.class, "foo", RegTempOperand.class);
		
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOffsOperand.class, "foo", 2, RegTempOperand.class);
		
		idx = findInstrWithSymbol(instrs, "foo", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOffsOperand.class, "foo", 4, RegTempOperand.class);

		idx = findInstrWithSymbol(instrs, "foo", idx);
		assertEquals(-1, idx);
	}
}

