/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.*;

import java.util.List;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.llvm.tms9900.BackEnd;
import org.junit.Test;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.HLInstruction;

/**
 * @author ejs
 *
 */
public class Test9900InstrGen extends BaseParserTest {

	protected BackEnd doCodeGen(String text) throws Exception {
		LLModule mod = getModule(text);
		BackEnd gen = new BackEnd(typeEngine, v9t9Target);
		for (LLBaseDirective dir: mod.getDirectives()) {
			gen.generateDirective(dir);
		}
		return gen;
	}
	
	@Test
	public void testEmpty() throws Exception {
		BackEnd cg = doCodeGen("");
		List<Routine> routines = cg.getRoutines();
		assertEquals(0, routines.size());
	}
	
	@Test
	public void testSimple() throws Exception {
		BackEnd cg = doCodeGen("foo = code() {} ;\n");
		List<Routine> routines = cg.getRoutines();
		assertEquals(1, routines.size());
		Routine rout = routines.get(0);
		assertEquals("foo.void$_", rout.getName().getName());
		List<Block> blocks = rout.getBlocks();
		assertEquals(1, blocks.size());
		
		Block block;
		block = blocks.get(0);
		assertNotNull(block.getLabel());
		System.out.println(block);
		
		assertEquals(1, block.getInstrs().size());
		HLInstruction inst = block.getFirst();
		assertNotNull(inst);
		assertEquals(InstructionTable.Ib, inst.getInst());
	}
}
