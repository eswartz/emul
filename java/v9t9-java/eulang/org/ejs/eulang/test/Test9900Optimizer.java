/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.tms9900.AsmInstruction;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.FlowGraphVisitor;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.LinkedRoutine;
import org.ejs.eulang.llvm.tms9900.Locals;
import org.ejs.eulang.llvm.tms9900.LocalLifetimeVisitor;
import org.ejs.eulang.llvm.tms9900.PeepholeAndLocalCoalesce;
import org.ejs.eulang.llvm.tms9900.RenumberVisitor;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.RoutineDumper;
import org.ejs.eulang.llvm.tms9900.Block.Edge;
import org.ejs.eulang.llvm.tms9900.asm.Label;
import org.ejs.eulang.symbols.GlobalScope;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

/**
 * @author ejs
 *
 */
public class Test9900Optimizer extends BaseInstrTest {

	/**
	 * @param string
	 * @throws Exception 
	 */
	private void doOpt(String string) throws Exception {
		doIsel(string);
		routine.setupForOptimization();
		PeepholeAndLocalCoalesce peepholeAndLocalCoalesce = new PeepholeAndLocalCoalesce();
		routine.accept(peepholeAndLocalCoalesce);
		if (peepholeAndLocalCoalesce.isChanged()) {
			System.out.println("After optimization:");
			routine.accept(new RoutineDumper());
			routine.setupForOptimization();
		}
		System.out.println("After resetting:");
		routine.accept(new RoutineDumper());
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
		assertEquals(0, local.getInstUses().size());
		assertNotNull(local.getInit());
		assertNotNull(local.getInit().first);
		assertNotNull(local.getInit().second);
	}
	@Test
	public void testDefUse1() throws Exception {
		dumpIsel = true;
		doIsel("foo = code() { x := 1 };\n");
		routine.setupForOptimization();
		
		ILocal local;
		local = getLocal("x");
		assertNotNull(local.getInit());
		assertNotNull(local.getInit().first);
		assertNotNull(local.getInit().second);
		
		Map<Block, List<AsmInstruction>> uses = local.getInstUses();
		assertEquals(1, uses.size());
		List<AsmInstruction> list = uses.get(local.getInit().first);
		assertNotNull(list);
		assertEquals(1, list.size());
	}
	@Test
	public void testDefUse2() throws Exception {
		dumpIsel = true;
		doIsel("foo = code() { x := 1; x += 11; };\n");
		routine.setupForOptimization();
		
		ILocal local;
		local = getLocal("x");
		assertNotNull(local.getInit());
		assertNotNull(local.getInit().first);
		assertNotNull(local.getInit().second);
		
		Map<Block, List<AsmInstruction>> uses = local.getInstUses();
		assertEquals(1, uses.size());
		List<AsmInstruction> list = uses.get(local.getInit().first);
		assertNotNull(list);
		assertEquals(2, list.size());
	}
	
	@Test
	public void testPeephole1() throws Exception {
		dumpIsel = true;
		doOpt("foo = code() { x := 1; x += 11; };\n");

		routine.accept(new PeepholeAndLocalCoalesce());
		
		ILocal local;
		local = getLocal("x");
		assertNotNull(local.getInit());
		assertNotNull(local.getInit().first);
		assertNotNull(local.getInit().second);
		
		Map<Block, List<AsmInstruction>> uses = local.getInstUses();
		assertEquals(1, uses.size());
		List<AsmInstruction> list = uses.get(local.getInit().first);
		assertNotNull(list);
		assertEquals(3, list.size());
	}
}

