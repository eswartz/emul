/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static v9t9.engine.cpu.InstructionTable.Ili;
import static v9t9.engine.cpu.InstructionTable.Imov;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.LLVMGenerator;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.tms9900.BaseHLOperand;
import org.ejs.eulang.llvm.tms9900.Block;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.ISymbolOperand;
import org.ejs.eulang.llvm.tms9900.InstrSelection;
import org.ejs.eulang.llvm.tms9900.LinkedRoutine;
import org.ejs.eulang.llvm.tms9900.Locals;
import org.ejs.eulang.llvm.tms9900.RegisterLocal;
import org.ejs.eulang.llvm.tms9900.RegisterTempOperand;
import org.ejs.eulang.llvm.tms9900.RenumberInstructionsVisitor;
import org.ejs.eulang.llvm.tms9900.StackLocalOperand;
import org.ejs.eulang.llvm.tms9900.SymbolLabelOperand;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.ModuleScope;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.HLInstruction;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;

/**
 * @author ejs
 *
 */
public class Test9900InstrSelection extends BaseParserTest {

	private Locals locals;
	private ArrayList<HLInstruction> instrs;
	private ArrayList<Block> blocks;
	private Block currentBlock;

	protected void doIsel(String text) throws Exception {
		LLModule mod = getModule(text);
		for (LLBaseDirective dir : mod.getDirectives()) {
			if (dir instanceof LLDefineDirective) {
				LLDefineDirective def = (LLDefineDirective) dir;
				
				def.accept(new RenumberInstructionsVisitor());
		
				instrs = new ArrayList<HLInstruction>();
				blocks = new ArrayList<Block>();
				LinkedRoutine routine = new LinkedRoutine(def);
				locals = routine.getLocals();
				locals.buildLocalTable();
				currentBlock = null; 

				InstrSelection isel = new InstrSelection(mod, routine) {
					
					@Override
					protected RegisterLocal newTempRegister(LLInstr instr, LLType type) {
						ILocal local = locals.allocateTemp(type);
						if (!(local instanceof RegisterLocal))
							throw new IllegalStateException("cannot force " + type + " into a register");
						RegisterLocal regLocal = (RegisterLocal) local;
						return regLocal;
					}
					
					@Override
					protected void emit(HLInstruction instr) {
						System.out.println("\t"+ instr);
						instrs.add(instr);
						currentBlock.addInst(instr);
					}
					
					/* (non-Javadoc)
					 * @see org.ejs.eulang.llvm.tms9900.InstrSelection#newBlock(org.ejs.eulang.llvm.tms9900.Block)
					 */
					@Override
					protected void newBlock(Block block) {
						System.out.println(block.getLabel());
						currentBlock = block;
						blocks.add(block);
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
	public void testSetGlobal1() throws Exception {
		dumpLLVMGen = true;
		doIsel("bat := Byte;\n"+
				"foo = code(x,y:Int => nil) { bat = 10; };\n");
		
		HLInstruction inst;
		int idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 0xA00);
		
		idx = findInstrWithSymbol(instrs, "bat");
		inst = instrs.get(idx);
		
		matchInstr(inst, "MOVB", RegisterTempOperand.class, AddrOperand.class, "bat"); 
		ISymbol sym = getOperandSymbol(inst.getOp2());
		assertTrue(sym+"", sym.getScope() instanceof ModuleScope);
		
	}
	@SuppressWarnings("unchecked")
	protected void matchInstr(HLInstruction instr, String name, Object... stuff) {
		assertEquals(instr+"", name.toLowerCase(), InstructionTable.getInstName(instr.getInst()).toLowerCase());
		int opidx = 1;
		for( int i = 0; i < stuff.length; ) {
			AssemblerOperand op = instr.getOp(opidx++);
			if (stuff[i] instanceof Class) {
				assertTrue(instr+":"+i, ((Class)stuff[i]).isInstance(op));
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
					
					boolean eq = true;
					if (string.startsWith("~")) {
						string = string.substring(1);
						eq = false;
					}
					
					ISymbol sym = getOperandSymbol(op);
					if (sym != null) {
						if (eq)
							assertSameSymbol(instr, sym, string);
						else
							assertNotSameSymbol(instr, sym, string);
					} else {
						if (eq)
							assertEquals(instr+":"+op, string, op.toString());
						else
							if (string.equals(op.toString()))
								fail(instr+":"+op);
					}
						
				}
				else if (stuff[i] instanceof Integer) {
					Integer num = (Integer) stuff[i];
					i++;
					if (op instanceof RegisterOperand)
						assertTrue(instr+":"+op, ((RegisterOperand) op).isReg(num));
					else if (op instanceof RegisterTempOperand)
						assertEquals(instr+":"+op, num, (Integer)((RegisterTempOperand) op).getLocal().getVr());
					else if (op instanceof NumberOperand)
						assertEquals(instr+":"+op, num, (Integer)((NumberOperand) op).getValue());
					else if (op instanceof ConstPoolRefOperand)
						assertEquals(instr+":"+op, num, (Integer)((ConstPoolRefOperand) op).getValue());
					else 
						assertEquals(instr+":"+op, num, op);
				}
				else if (stuff[i] instanceof Boolean) {
					if (op instanceof RegisterTempOperand)
						assertEquals(instr+":"+op, stuff[i], ((RegisterTempOperand) op).isHighReg());
					else
						fail("expected register temp");
					i++;
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

	
	protected ISymbol getOperandSymbol(AssemblerOperand op) {
		if (op instanceof AddrOperand)
			op = ((AddrOperand) op).getAddr();
		if (op instanceof RegisterOperand)
			op = ((RegisterOperand) op).getReg();
		
		if (op instanceof ISymbolOperand)
			return ((ISymbolOperand) op).getSymbol();
		return null;
	}
	protected void assertSameSymbol(HLInstruction instr,
			ISymbol sym, String string) {
		assertTrue(instr+":"+sym, symbolMatches(sym, string)
				 );
	}
	protected void assertNotSameSymbol(HLInstruction instr,
			ISymbol sym, String string) {
		assertFalse(instr+":"+sym, symbolMatches(sym, string)
		);
	}

	protected boolean symbolMatches(ISymbol sym, String string) {
		return sym.getUniqueName().equals(string)
				 || sym.getName().equals(string)
				 || sym.getUniqueName().startsWith("%" + string)
				 || sym.getUniqueName().startsWith("@" + string)
				 || sym.getUniqueName().startsWith(string + ".")
				 || sym.getUniqueName().contains("." + string + ".");
	}
	
	protected int findInstrWithSymbol(List<HLInstruction> instrs, String string, int idx) {
		for (int i = idx + 1; i < instrs.size(); i++) {
			HLInstruction instr = instrs.get(i);	
			for (AssemblerOperand op : instr.getOps()) {
				ISymbol sym = getOperandSymbol(op);
				if (sym != null && symbolMatches(sym, string))
					return i;
			}
		}
		return -1;
	}

	protected int findInstrWithSymbol(List<HLInstruction> instrs, String string) {
		return findInstrWithSymbol(instrs, string, -1);
	}

	protected int findInstrWithInst(List<HLInstruction> instrs, String string) {
		return findInstrWithInst(instrs, string, -1);
	}

	protected int findInstrWithInst(List<HLInstruction> instrs, String string, int from) {
		for (int i = from + 1; i < instrs.size(); i++) {
			HLInstruction instr = instrs.get(i);
			if (InstructionTable.getInstName(instr.getInst()).equalsIgnoreCase(string))
				return i;
		}
		return -1;
	}
	@Test
	public void testConsts1() throws Exception {
		doIsel("foo = code( ) { x:=123; x=-3849 };\n");
		
		int idx = findInstrWithInst(instrs, "LI");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 123); 
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, -3849); 
	}
	@Test
	public void testConstsByte1() throws Exception {
		dumpTreeize = true;
		doIsel("foo = code( ) { x:Byte=123; x=-112 };\n");
		
		int idx = findInstrWithInst(instrs, "LI");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 123*256); 
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, (-112*256) & 0xff00); 
	}
	@Test
	public void testAdd() throws Exception {
		doIsel("foo = code(x,y:Int ) { z:=x+y; x; y; };\n");
		
		// X is used again, and both come in in regs
		int idx = findInstrWithInst(instrs, "A");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "A", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x"); 
	}
	@Test
	public void testAddConst1() throws Exception {
		doIsel("foo = code(x,y:Int ) { (x+1)+(x+2)+(y-1)+(y-2) };\n");
		
		int idx;
		HLInstruction inst;
		idx = findInstrWithInst(instrs, "INC");
		inst = instrs.get(idx);
		idx = findInstrWithInst(instrs, "INCT");
		inst = instrs.get(idx);
		idx = findInstrWithInst(instrs, "DEC");
		inst = instrs.get(idx);
		idx = findInstrWithInst(instrs, "DECT");
		inst = instrs.get(idx);
	}
	@Test
	public void testAddConst2() throws Exception {
		doIsel("foo = code(x,y:Int ) { (x-(-1))+(x-(-2)) };\n");
		
		int idx;
		HLInstruction inst;
		idx = findInstrWithInst(instrs, "INC");
		inst = instrs.get(idx);
		idx = findInstrWithInst(instrs, "INCT");
		inst = instrs.get(idx);
	}

	@Test
	public void testAddAndRet1() throws Exception {
		doIsel("foo = code(x,y:Int ) { x+y };\n");
		
		int idx = findInstrWithInst(instrs, "A");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "A", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x"); 
	}
	@Test
	public void testSubAndRet1() throws Exception {
		doIsel("foo = code(x,y:Int ) { x-y };\n");
		
		int idx = findInstrWithInst(instrs, "S");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "S", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x"); 
	}
	@Test
	public void testSubAndRet2() throws Exception {
		doIsel("foo = code(x,y:Int ) { y-x };\n");
		
		int idx = findInstrWithInst(instrs, "S");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "S", RegisterTempOperand.class, "x", RegisterTempOperand.class, "~y"); 
	}
	@Test
	public void testSubRev1() throws Exception {
		doIsel("foo = code(x:Int ) { 100-x };\n");
		
		int idx = findInstrWithInst(instrs, "LI");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 100);
		AssemblerOperand temp = inst.getOp1();
		idx = findInstrWithInst(instrs, "S");
		inst = instrs.get(idx);
		matchInstr(inst, "S", RegisterTempOperand.class, "x", temp);
	}
	@Test
	public void testSubRev2() throws Exception {
		doIsel("x : Byte; foo = code(y:Int ) { x-y };\n");
		
		int idx = findInstrWithInst(instrs, "MOVB");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOVB", AddrOperand.class, "x", RegisterTempOperand.class);
		AssemblerOperand temp = inst.getOp2();
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", temp, NumberOperand.class, 8);

		idx = findInstrWithInst(instrs, "S");
		inst = instrs.get(idx);
		matchInstr(inst, "S", RegisterTempOperand.class, "y", temp);
	}
	@Test
	public void testSubRevAss1() throws Exception {
		dumpLLVMGen =true;
		doIsel("x : Byte; foo = code(y:Int ) { x-=y };\n");
		
		/*
		SLA R0, 8
		SB R0, @X
		 */
		int idx = findInstrWithInst(instrs, "SLA");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~y", NumberOperand.class, 8);
		
		idx = findInstrWithInst(instrs, "SB");
		inst = instrs.get(idx);
		matchInstr(inst, "SB", RegisterTempOperand.class, "~y", AddrOperand.class, "x");
	}
	@Test
	public void testSubImm1() throws Exception {
		doIsel("foo = code(x:Int ) { x-1923 };\n");
		
		int idx = findInstrWithInst(instrs, "AI");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "AI", RegisterTempOperand.class, "~x", NumberOperand.class, -1923); 
	}
	@Test
	public void testTrunc16_to_8_1_Local() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x,y:Int ) { z : Byte = x+y };\n");
		
		int idx = findInstrWithInst(instrs, "SLA");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, NumberOperand.class, 8);
	}
	
	@Test
	public void testTrunc16_to_8_1_Mem_to_Temp() throws Exception {
		dumpLLVMGen = true;
		doIsel("x := 11; foo = code( ) { Byte(x) };\n");
		
		int idx = findInstrWithInst(instrs, "SLA");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, NumberOperand.class, 8);
	}
	@Test
	public void testTrunc16_to_8_1_Mem_to_Mem() throws Exception {
		dumpLLVMGen = true;
		doIsel("x := 11; foo = code( ) { x = Byte(x) };\n");
		
		int idx;
		HLInstruction inst;
		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOperand.class, "x", RegisterTempOperand.class);
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, NumberOperand.class, 8);
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, NumberOperand.class, 8);
	}
	@Test
	public void testTrunc16_to_8_1_Imm_to_Mem() throws Exception {
		dumpLLVMGen = true;
		doIsel("x : Byte; foo = code( => Int ) { x = 0x1234; };\n");
		
		int idx;
		HLInstruction inst;
		
		// downcast on the immed
		idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 0x3400);

		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, AddrOperand.class, "x");
		
		// upcast again
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, NumberOperand.class, 8);
	}
	@Test
	public void testExt8_to_16_1_Mem_to_Mem() throws Exception {
		dumpLLVMGen = true;
		doIsel("x : Byte = 11; foo = code( ) { x = Int(x) };\n");
		
		int idx;
		HLInstruction inst;
		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", AddrOperand.class, "x", RegisterTempOperand.class);
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, NumberOperand.class, 8);
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, NumberOperand.class, 8);
	}
	@Test
	public void testShiftLeftConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x<<1) + (x<<0) + (x<<4) + (x<<16) };\n");
		
		int idx = findInstrWithInst(instrs, "SLA");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~x", NumberOperand.class, 1);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~x", NumberOperand.class, 4);
		
		// <<16 ==> 0
		int idx0;
		idx0 = findInstrWithInst(instrs, "CLR", idx);
		assertTrue(idx0 >= 0);
		
	}
	@Test
	public void testShiftLeftVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x, y:Int ) { x<<y };\n");
		
		int idx;
		idx = findInstrWithSymbol(instrs, "y");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, "y", RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SLA");
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~x", RegisterTempOperand.class, 0);
	}
	
	@Test
	public void testShiftRightConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x>>1) + (x>>0) + (x>>4) + (x>>16) };\n");
		
		int idx = findInstrWithInst(instrs, "SRA");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, "~x", NumberOperand.class, 1);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, "~x", NumberOperand.class, 4);

		// >>16 = by R0 with 0
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, "~x", RegisterTempOperand.class, 0);
		
	}
	@Test
	public void testShiftRightVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Int ) { (x>>y) };\n");
		
		int idx;
		idx = findInstrWithSymbol(instrs, "y");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, "y", RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRA");
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, "~x", RegisterTempOperand.class, 0);
		
	}
	
	@Test
	public void testUShiftRightConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x+>>1) + (x+>>0) + (x+>>4) + (x+>>16) };\n");
		
		int idx = findInstrWithInst(instrs, "SRL");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegisterTempOperand.class, "~x", NumberOperand.class, 1);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SRL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegisterTempOperand.class, "~x", NumberOperand.class, 4);

		// +>>16 = 0
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class);
		
	}
	@Test
	public void testUShiftRightVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Int ) { (x+>>y) };\n");
		
		int idx;
		idx = findInstrWithSymbol(instrs, "y");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, "y", RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRL");
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegisterTempOperand.class, "~x", RegisterTempOperand.class, 0);
		
	}
	
	@Test
	public void testCShiftRightConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x>>|1) + (x>>|0) + (x>>|4) + (x>>|16) };\n");
		
		int idx = findInstrWithInst(instrs, "SRC");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegisterTempOperand.class, "~x", NumberOperand.class, 1);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SRC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegisterTempOperand.class, "~x", NumberOperand.class, 4);

		// +>>16 = no-op
		idx = findInstrWithInst(instrs, "SRC", idx);
		assertEquals(-1, idx);
		
	}
	@Test
	public void testCShiftRightVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Int ) { (x>>|y) };\n");
		
		int idx;
		idx = findInstrWithSymbol(instrs, "y");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, "y", RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRC");
		inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegisterTempOperand.class, "~x", RegisterTempOperand.class, 0);
		
	}

	@Test
	public void testCShiftRight8Bit() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte ) { (x>>|1) + (x>>|y) };\n");
		
		int idx;
		HLInstruction inst;
		idx = findInstrWithInst(instrs, "SWPB");
		inst = instrs.get(idx);
		matchInstr(inst, "SWPB", RegisterTempOperand.class, "~x");
		HLInstruction copy = inst;
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, copy.getOp1());
	}
	
	
	@Test
	public void testCShiftLeftConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x<<|1) + (x<<|0) + (x<<|4) + (x<<|16) };\n");
		
		int idx = findInstrWithInst(instrs, "SRC");
		HLInstruction inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegisterTempOperand.class, "~x", NumberOperand.class, 15);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SRC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegisterTempOperand.class, "~x", NumberOperand.class, 12);

		// +>>16 = no-op
		idx = findInstrWithInst(instrs, "SRC", idx);
		assertEquals(-1, idx);
		
	}
	@Test
	public void testCShiftLeftVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Int ) { (x<<|y) };\n");
		
		int idx;
		HLInstruction inst;
		idx = findInstrWithSymbol(instrs, "y");
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, "y", RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "NEG");
		inst = instrs.get(idx);
		matchInstr(inst, "NEG", RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRC");
		inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegisterTempOperand.class, "~x", RegisterTempOperand.class, 0);
		
	}

	@Test
	public void testCShiftLeft8Bit() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte ) { (x<<|1) + (x<<|y) };\n");
		
		int idx;
		HLInstruction inst;
		idx = findInstrWithInst(instrs, "SWPB");
		inst = instrs.get(idx);
		matchInstr(inst, "SWPB", RegisterTempOperand.class, "~x");
		HLInstruction copy = inst;
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, copy.getOp1());
	}

	@Test
	public void testLogicalOps() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x, y:Int ) { (x|15) + (x|y) + (x&4111) + (x&y) + (x~9) + (x~y) };\n");
		
		int idx = -1;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "ORI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ORI", RegisterTempOperand.class, "~x", NumberOperand.class, 15);
		
		idx = findInstrWithInst(instrs, "SOC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOC", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x");
		
		idx = findInstrWithInst(instrs, "ANDI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ANDI", RegisterTempOperand.class, "~x", NumberOperand.class, 4111);
		
		idx = findInstrWithInst(instrs, "SZC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SZC", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x");

		// XOR more complex
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 9);
		HLInstruction loadinst = inst;
		
		idx = findInstrWithInst(instrs, "XOR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "XOR", loadinst.getOp(1), RegisterTempOperand.class, "~x");
		
		idx = findInstrWithInst(instrs, "XOR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "XOR", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x");
		
	}
	@Test
	public void testLogicalOpsByte() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x, y:Byte ) { (x|15) + (x|y) + (x&41) + (x&y) + (x~9) + (x~y) };\n");
		
		int idx = -1;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "ORI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ORI", RegisterTempOperand.class, "~x", NumberOperand.class, 15*256);
		
		idx = findInstrWithInst(instrs, "SOCB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOCB", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x");
		
		idx = findInstrWithInst(instrs, "ANDI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ANDI", RegisterTempOperand.class, "~x", NumberOperand.class, (41*256) & 0xff00);
		
		idx = findInstrWithInst(instrs, "SZCB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SZCB", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x");

		// XOR more complex
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 9*256);
		HLInstruction loadinst = inst;
		
		idx = findInstrWithInst(instrs, "XOR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "XOR", loadinst.getOp(1), RegisterTempOperand.class, "~x");
		
		idx = findInstrWithInst(instrs, "XOR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "XOR", RegisterTempOperand.class, "y", RegisterTempOperand.class, "~x");
		
	}
	
	@Test
	public void testComparisonOpsInExpr() throws Exception {
		dumpLLVMGen =true;
		
		// this generates boolean comparisons and stores them for logical manipulation;
		doIsel("foo = code(x, y : Int) { (x<y) | (x==9) };\n");
		
		int idx = -1;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "C", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "C", RegisterTempOperand.class, "x", RegisterTempOperand.class, "y");
		
		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, InstrSelection.CMP_SLT, RegisterTempOperand.class, "~y");
		HLInstruction set1 = inst;
		
		idx = findInstrWithInst(instrs, "CI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CI", RegisterTempOperand.class, "x", NumberOperand.class, 9);

		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, InstrSelection.CMP_EQ, RegisterTempOperand.class, "~x");
		HLInstruction set2 = inst;

		idx = findInstrWithInst(instrs, "SOCB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOCB", set2.getOp2(), set1.getOp2());
	}
	@Test
	public void testComparisonOpsInExprByte() throws Exception {
		dumpLLVMGen =true;
		
		// this generates boolean comparisons and stores them for logical manipulation;
		doIsel("foo = code(x, y : Byte) { (x<y) | (x==9) };\n");
		
		int idx = -1;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "CB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CB", RegisterTempOperand.class, "x", RegisterTempOperand.class, "y");
		
		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, InstrSelection.CMP_SLT, RegisterTempOperand.class, "~y");
		HLInstruction set1 = inst;
		
		// don't use CI with bytes, since the low byte is unknown
		idx = findInstrWithInst(instrs, "CB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CB", RegisterTempOperand.class, "x", ConstPoolRefOperand.class, 9);

		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, InstrSelection.CMP_EQ, RegisterTempOperand.class, "~x");
		HLInstruction set2 = inst;

		idx = findInstrWithInst(instrs, "SOCB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOCB", set2.getOp2(), set1.getOp2());
	}
	@Test
	public void testComparisonOpsInJmp() throws Exception {
		dumpLLVMGen =true;
		
		// this generates boolean comparisons and jumps on them
		doIsel("foo = code(x, y : Int) { (x<y) or (x==9) };\n");
		
		int idx = -1;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "C", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "C", RegisterTempOperand.class, "x", RegisterTempOperand.class, "y");
		
		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, InstrSelection.CMP_SLT, RegisterTempOperand.class, "~y");
		HLInstruction set1 = inst;

		idx = findInstrWithInst(instrs, "JCC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "JCC", set1.getOp2(), SymbolLabelOperand.class, SymbolLabelOperand.class);

		idx = findInstrWithInst(instrs, "CI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CI", RegisterTempOperand.class, "x", NumberOperand.class, 9);

		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, InstrSelection.CMP_EQ, RegisterTempOperand.class, "~x");
		HLInstruction set2 = inst;

		idx = findInstrWithInst(instrs, "JMP", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "JMP", SymbolLabelOperand.class);
	}
	
	@Test
	public void testComparisonOps() throws Exception {
		dumpLLVMGen =true;
		
		// this generates boolean comparisons and jumps on them
		doIsel("foo = code(x, y : Int) { (x<y) or (x>=y) or (x+<y) == (x+>=y) or (x+>y) != (x+<=y) };\n");
	}
	
	@Test
	public void testMulPow2() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x*0) + (x*1) + (x*4) + (x*128) + (x*32768) };\n");
		
		int idx;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "CLR");
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class, "~x");
		
		// ignore * 1
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~x", NumberOperand.class, 2); // * 4
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~x", NumberOperand.class, 7);	// * 128
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~x", NumberOperand.class, 15);	// * 32768
		
	}


	@Test
	public void testMulBytePow2() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Byte ) { (x*0) + (x*1) + (x*4) + (x*64) + (x*32768) };\n");
		
		int idx;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "CLR");
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class, "~x");
		
		// ignore * 1
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~x", NumberOperand.class, 2); // * 4
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegisterTempOperand.class, "~x", NumberOperand.class, 6);	// * 64
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class);	// * 32768
		
	}
	

	@Test
	public void testMul1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x*123) + (x*-999) };\n");
		
		int idx;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "MOV");
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, "x", RegisterTempOperand.class, true);
		HLInstruction xval = inst;
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 123);
		HLInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "MPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MPY", val.getOp1(), xval.getOp2());
		
	}

	@Test
	public void testMulByte1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte) { (x*y) + (y*x) };\n");
		
		int idx;
		HLInstruction inst;

		idx = findInstrWithInst(instrs, "MOVB");
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, "x", RegisterTempOperand.class, true);
		HLInstruction xval = inst;
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, "y", RegisterTempOperand.class, false);
		HLInstruction yval = inst;
		
		idx = findInstrWithInst(instrs, "MPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MPY", yval.getOp2(), xval.getOp2());
		
	}
	

	@Test
	public void testDiv2() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x/1) + (x/4) + (x/128) + (x/32768) };\n");
		
		int idx;
		HLInstruction inst;
		
		// ignore / 1
		
		idx = findInstrWithInst(instrs, "SRA");
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, "~x", NumberOperand.class, 2); // * 4
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, "~x", NumberOperand.class, 7);	// * 128
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegisterTempOperand.class, "~x", NumberOperand.class, 15);	// * 32768
		
	}

	@Test
	public void testUDiv2() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x+/1) + (x+/4) + (x+/128) + (x+/32768) };\n");
		
		int idx;
		HLInstruction inst;
		
		// ignore / 1
		
		idx = findInstrWithInst(instrs, "SRL");
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegisterTempOperand.class, "~x", NumberOperand.class, 2); // * 4
		
		idx = findInstrWithInst(instrs, "SRL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegisterTempOperand.class, "~x", NumberOperand.class, 7);	// * 128
		
		idx = findInstrWithInst(instrs, "SRL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegisterTempOperand.class, "~x", NumberOperand.class, 15);	// * 32768
		
	}
	

	@Test
	public void testDiv1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x/123) + (x+/999) };\n");
		
		int idx;
		HLInstruction inst;

		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, RegisterTempOperand.class, 0);	// arg 0
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, 1, NumberOperand.class, 123);	// arg 1
		HLInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.sdiv");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterOperand.class, 0, RegisterTempOperand.class);			// save result
		HLInstruction div1 = inst;
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 999);
		val = inst;
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class, true);	// high word
		
		
		idx = findInstrWithInst(instrs, "DIV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DIV", val.getOp1(), RegisterTempOperand.class, true);
		HLInstruction div2 = inst;
		
		idx = findInstrWithInst(instrs, "A", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", div2.getOp3(), div1.getOp2());	// low words are result
		

	}
	
	@Test
	public void testRemMod1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x\\123) + (x+\\555) + (x%999) };\n");
		
		int idx;
		HLInstruction inst;


		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, 1, NumberOperand.class, 123);
		HLInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.srem");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterOperand.class, 0, RegisterTempOperand.class);	// save result
		HLInstruction div1 = inst;
		
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, "x", RegisterTempOperand.class, false);	// low word
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 555);
		val = inst;
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class, true);	// high word
		
		
		idx = findInstrWithInst(instrs, "DIV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DIV", val.getOp1(), RegisterTempOperand.class, true, RegisterTempOperand.class, false);
		HLInstruction div2 = inst;
		
		idx = findInstrWithInst(instrs, "A", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", div2.getOp2(), RegisterTempOperand.class);	// use result
		AssemblerOperand add1 = inst.getOp2();
		
		idx = findInstrWithSymbol(instrs, "x", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterTempOperand.class, RegisterTempOperand.class);
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, 1, NumberOperand.class, 999);
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.modulo");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterOperand.class, 0, RegisterTempOperand.class);	// save result
		HLInstruction div3 = inst;
		
		idx = findInstrWithInst(instrs, "A", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", div3.getOp2(), add1);	// high words are result
		
		

	}


	@Test
	public void testDivByte1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte ) { (x/123) + (x+/y) };\n");
		
		int idx;
		HLInstruction inst;

		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, RegisterTempOperand.class, 0);	// arg 0
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, 1, NumberOperand.class, 123 * 256);	// arg 1
		HLInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.sdiv");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterOperand.class, 0, RegisterTempOperand.class);	// save
		HLInstruction div1 = inst;
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, "x", RegisterTempOperand.class, false);	// low word
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class, true);	// high word
		
		
		idx = findInstrWithInst(instrs, "DIV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DIV", RegisterTempOperand.class, "y", RegisterTempOperand.class, true);
		HLInstruction div2 = inst;
		
		idx = findInstrWithInst(instrs, "AB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", div2.getOp3(), div1.getOp2());	// low words are result
		

	}
	
	@Test
	public void testModByte1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte) { (x\\123) + (x+\\55) + (x%y) };\n");
		
		int idx;
		HLInstruction inst;


		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, 1, NumberOperand.class, 123 * 256);
		HLInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.srem");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterOperand.class, 0, RegisterTempOperand.class);	// save result
		HLInstruction div1 = inst;
		
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, "x", RegisterTempOperand.class, false);	// low word
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegisterTempOperand.class, NumberOperand.class, 55 * 256);
		val = inst;
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegisterTempOperand.class, true);	// high word
		
		
		idx = findInstrWithInst(instrs, "DIV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DIV", val.getOp1(), RegisterTempOperand.class, true, RegisterTempOperand.class, false);
		HLInstruction div2 = inst;
		
		idx = findInstrWithInst(instrs, "AB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", div2.getOp2(), RegisterTempOperand.class);	// use result
		AssemblerOperand add1 = inst.getOp2();
		
		idx = findInstrWithSymbol(instrs, "x", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, RegisterTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterTempOperand.class, "y", RegisterTempOperand.class, 1);
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.modulo");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterOperand.class, 0, RegisterTempOperand.class);	// save result
		HLInstruction div3 = inst;
		
		idx = findInstrWithInst(instrs, "AB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", div3.getOp2(), add1);	// high words are result
		
		
		

	}
	
	@Test
    public void testRepeatLoopBreak3() throws Exception {
    	dumpLLVMGen = true;
    	doIsel("testRepeatLoopBreak = code (x) {\n" +
    			"   s := 0;\n"+
    			"   b := 1;\n"+
    			"	repeat x do { s, b += b;\n" +
    			"  		if s > 100 then break s else s }\n"+
    			"};\n");

		int idx;
		HLInstruction inst;

		// make sure we compare against 0 with "C r,r" not "CI r,0"
		idx = findInstrWithLabel("loopEnter");
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AssemblerOperand.class, "counter", RegisterTempOperand.class);
		HLInstruction mov = inst;
		idx = findInstrWithInst(instrs, "C", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "C", mov.getOp2(), mov.getOp2());
		idx = findInstrWithInst(instrs, "DEC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DEC", AssemblerOperand.class, "counter");
		

    }

	private int findInstrWithLabel(String string) {
		for (Block block : blocks) 
			if (block.getLabel().getName().startsWith(string))
				return instrs.indexOf(block.getFirst());
		return -1;
	}
}
