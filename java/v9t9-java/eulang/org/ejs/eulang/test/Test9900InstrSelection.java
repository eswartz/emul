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
import org.ejs.eulang.llvm.tms9900.StackLocal;
import org.ejs.eulang.llvm.tms9900.StackLocalOperand;
import org.ejs.eulang.llvm.tms9900.SymbolOperand;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.junit.Test;
import org.omg.CORBA.LocalObject;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;

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

	@SuppressWarnings("unchecked")
	protected void matchInstr(HLInstruction instr, String name, Object... stuff) {
		assertEquals(instr+"", name.toLowerCase(), InstructionTable.getInstName(instr.getInst()).toLowerCase());
		int opidx = 1;
		for( int i = 0; i < stuff.length; ) {
			AssemblerOperand op = instr.getOp(opidx++);
			if (stuff[i] instanceof Class) {
				assertEquals(instr+":"+i, stuff[i], op.getClass());
				i++;
				if (i >= stuff.length)
					break;
				if (stuff[i] instanceof String) {
					String string = (String) stuff[i];
					i++;
					if (op instanceof AddrOperand)
						op = ((AddrOperand) op).getAddr();
					if (op instanceof RegisterOperand)
						op = ((RegisterOperand) op).getReg();
					
					if (op instanceof SymbolOperand)
						assertSameSymbol(instr, ((SymbolOperand) op).getSymbol(), string);
					else if (op instanceof RegisterTempOperand)
						assertSameSymbol(instr, ((RegisterTempOperand) op).getLocal().getName(), string);
					else if (op instanceof StackLocalOperand)
						assertSameSymbol(instr, ((StackLocalOperand) op).getLocal().getName(), string);
					else
						assertEquals(instr+":"+op, string, op.toString());
						
				}
				else if (stuff[i] instanceof Integer) {
					Integer num = (Integer) stuff[i];
					i++;
					if (op instanceof RegisterOperand)
						assertTrue(instr+":"+op, ((RegisterOperand) op).isReg(num));
					else if (op instanceof RegisterTempOperand)
						assertEquals(instr+":"+op, num, (Integer)((RegisterTempOperand) op).getLocal().getVr());
					else 
						assertEquals(instr+":"+op, num, op);
				}
			}
			else if (stuff[i] instanceof AssemblerOperand) {
				assertEquals(instr+":"+op, stuff[i], op);
				i++;
			}
			else
				fail("unknown handling " + stuff[i]);
		}
	}

	protected void assertSameSymbol(HLInstruction instr,
			ISymbol sym, String string) {
		assertTrue(instr+":"+sym, sym.getUniqueName().equals(string)
				 || sym.getName().equals(string)
				 || sym.getUniqueName().startsWith("%" + string)
				 || sym.getUniqueName().contains("." + string + ".")
				 );
	}

	@Test
	public void testAddAndRet1() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x,y:Int ) { x+y };\n");
		assertEquals(6, instrs.size());
		
		HLInstruction inst;
		inst = instrs.get(0);
		matchInstr(inst, "MOV", RegisterTempOperand.class, 0, AddrOperand.class, "x"); 
		inst = instrs.get(1);
		matchInstr(inst, "MOV", RegisterTempOperand.class, 1, AddrOperand.class, "y"); 
		inst = instrs.get(2);
		AssemblerOperand res = inst.getOp2();
		matchInstr(inst, "MOV", AddrOperand.class, "x", RegisterTempOperand.class); 
		inst = instrs.get(3);
		matchInstr(inst, "A", AddrOperand.class, "y", res); 
		
		HLInstruction inst3 = instrs.get(4);
		matchInstr(inst3, "MOV", res, RegisterTempOperand.class, 0); 

		inst = instrs.get(5);
		assertEquals("B *R11", inst.toString());
	}
}
