/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static v9t9.engine.cpu.InstructionTable.Ili;
import static v9t9.engine.cpu.InstructionTable.Imov;

import java.util.ArrayList;

import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.InstrSelection;
import org.ejs.eulang.llvm.tms9900.LinkedRoutine;
import org.ejs.eulang.llvm.tms9900.Locals;
import org.ejs.eulang.llvm.tms9900.RegisterLocal;
import org.ejs.eulang.llvm.tms9900.RegisterTempOperand;
import org.ejs.eulang.llvm.tms9900.StackLocalOperand;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

/**
 * @author ejs
 *
 */
public class Test9900InstrSelection extends BaseParserTest {

	private Locals locals;
	private ArrayList<HLInstruction> instrs;

	protected void doIsel(String text) throws Exception {
		LLModule mod = getModule(text);
		for (LLBaseDirective dir : mod.getDirectives()) {
			if (dir instanceof LLDefineDirective) {
				LLDefineDirective def = (LLDefineDirective) dir;
		
				instrs = new ArrayList<HLInstruction>();
				LinkedRoutine routine = new LinkedRoutine(def);
				locals = routine.getLocals();
				locals.buildLocalTable();

				InstrSelection isel = new InstrSelection(routine) {
					
					@Override
					protected RegisterLocal newRegister(LLType type) {
						ILocal local = locals.allocateTemp(type);
						if (!(local instanceof RegisterLocal))
							throw new IllegalStateException("cannot force " + type + " into a register");
						RegisterLocal regLocal = (RegisterLocal) local;
						return regLocal;
					}
					
					@Override
					protected void emit(HLInstruction instr) {
						System.out.println(instr);
						instrs.add(instr);
					}
				};
				
				def.accept(isel);
				return;
			}
		}
		fail("no code generated:\n" + mod);
	}
	
	@Test
	public void testEmpty() throws Exception {
		doIsel("foo = code() { };\n");
		assertEquals(1, instrs.size());
		HLInstruction inst = instrs.get(0);
		assertEquals("B *R11", inst.toString());
	}
	

	@Test
	public void testRetInt() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code() { 1 };\n");
		assertEquals(2, instrs.size());
		HLInstruction inst;
		inst = instrs.get(0);
		assertEquals(Ili, inst.getInst());
		assertTrue(inst.getOp1().isRegister());
		assertTrue(inst.getOp2() instanceof NumberOperand);
		inst = instrs.get(1);
		assertEquals("B *R11", inst.toString());
	}


	@Test
	public void testInitLocal1() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code( => nil) { x := 1 };\n");
		assertEquals(3, instrs.size());
		HLInstruction inst;
		inst = instrs.get(0);
		assertEquals(Ili, inst.getInst());
		assertTrue(inst.getOp1().isRegister());
		assertTrue(inst.getOp2() instanceof NumberOperand);
		
		HLInstruction inst2 = instrs.get(1);
		assertEquals(Imov, inst2.getInst());
		assertEquals(inst.getOp1(), inst2.getOp1());
		assertTrue(inst2.getOp2() instanceof AddrOperand);
		assertTrue(((AddrOperand)inst2.getOp2()).getAddr() instanceof StackLocalOperand);
		
		inst = instrs.get(2);
		assertEquals("B *R11", inst.toString());
	}
	

	@Test
	public void testInitLocalAndRet1() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code( ) { x := 1 };\n");
		assertEquals(4, instrs.size());
		
		HLInstruction inst;
		inst = instrs.get(0);
		assertEquals(Ili, inst.getInst());
		assertTrue(inst.getOp1().isRegister());
		assertTrue(inst.getOp2() instanceof NumberOperand);
		
		HLInstruction inst2 = instrs.get(1);
		assertEquals(Imov, inst2.getInst());
		assertEquals(inst.getOp1(), inst2.getOp1());
		assertTrue(inst2.getOp2() instanceof AddrOperand);
		assertTrue(((AddrOperand)inst2.getOp2()).getAddr() instanceof StackLocalOperand);

		HLInstruction inst3 = instrs.get(2);
		assertEquals(Imov, inst3.getInst());
		assertEquals(inst3.getOp1(), inst2.getOp2());
		assertTrue(inst3.getOp2().isRegister());
		assertTrue(((RegisterTempOperand)inst3.getOp2()).getLocal().getVr() == 0);

		inst = instrs.get(3);
		assertEquals("B *R11", inst.toString());
	}
	

	@Test
	public void testAddAndRet1() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x,y:Int ) { x+y };\n");
		assertEquals(6, instrs.size());
		
		HLInstruction inst;
		inst = instrs.get(0);
		assertTrue(inst.toString(), inst.toString().matches("MOV R0.*,.*\\.x\\..*"));
		inst = instrs.get(1);
		assertTrue(inst.toString(), inst.toString().matches("MOV R1.*,.*\\.y\\..*"));
		inst = instrs.get(2);
		AssemblerOperand res = inst.getOp2();
		assertTrue(inst.toString(), inst.toString().matches("MOV .*\\.x\\..*,R.*"));
		inst = instrs.get(3);
		assertTrue(inst.toString(), inst.toString().matches("A .*\\.y\\..*,R.*"));
		assertEquals(res, inst.getOp2());
		
		HLInstruction inst3 = instrs.get(4);
		assertEquals(Imov, inst3.getInst());
		assertEquals(res, inst3.getOp1());
		assertTrue(inst3.getOp2().isRegister());
		assertTrue(((RegisterTempOperand)inst3.getOp2()).getLocal().getVr() == 0);

		inst = instrs.get(5);
		assertEquals("B *R11", inst.toString());
	}
}
