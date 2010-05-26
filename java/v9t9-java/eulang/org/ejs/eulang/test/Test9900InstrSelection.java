/**
 * 
 */
package org.ejs.eulang.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;

import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLBranchInstr;
import org.ejs.eulang.llvm.instrs.LLRetInstr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.llvm.ops.LLSymbolOp;
import org.ejs.eulang.llvm.tms9900.AsmInstruction;
import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.InstrSelection;
import org.ejs.eulang.llvm.tms9900.StackLocal;
import org.ejs.eulang.llvm.tms9900.asm.AddrOffsOperand;
import org.ejs.eulang.llvm.tms9900.asm.CompareOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.StackLocalOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolLabelOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.ModuleScope;
import org.ejs.eulang.types.LLTupleType;
import org.ejs.eulang.types.LLType;
import org.junit.Test;

import v9t9.engine.cpu.InstructionTable;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;

/**
 * @author ejs
 *
 */
public class Test9900InstrSelection extends BaseInstrTest {
	@Test
	public void testEmpty() throws Exception {
		// no multi-ret
		
		LLModule mod = getModule("");
		LLDefineDirective def = createDefine(mod, "test",
				typeEngine.INT,
				new LLType[0]);
		LLBlock block;
		block = def.addBlock(def.getScope().addTemporary("entry"));
		block.instrs().add(new LLRetInstr(typeEngine.VOID)); 
				
		doIsel(mod, def);
		assertFalse(def.flags().contains(LLDefineDirective.MULTI_RET));
		
		assertEquals(1, blocks.size());
		
		assertEquals(3, instrs.size());
		AsmInstruction inst = instrs.get(0);
		assertEquals(InstrSelection.Pprolog, inst.getInst());
		inst = instrs.get(1);
		assertEquals(InstrSelection.Pepilog, inst.getInst());
		inst = instrs.get(2);
		matchInstr(inst, "B", RegIndOperand.class, 11);
	}
	@Test
	public void testMultiRet() throws Exception {
		// should get multi-ret
		
		LLModule mod = getModule("");
		LLDefineDirective def = createDefine(mod, "test",
				typeEngine.INT,
				new LLType[0]);
		LLBlock block;
		block = def.addBlock(def.getScope().addTemporary("entry"));
		ISymbol trueSym = def.getScope().addTemporary("true");
		trueSym.setType(typeEngine.LABEL);
		ISymbol falseSym = def.getScope().addTemporary("false");
		falseSym.setType(typeEngine.LABEL);
		block.instrs().add(new LLBranchInstr(typeEngine.BOOL, 
				new LLConstOp(typeEngine.INT, 1), 
				new LLSymbolOp(trueSym, typeEngine.LABEL),
				new LLSymbolOp(falseSym, typeEngine.LABEL)));
		
		block = def.addBlock(trueSym);
		block.instrs().add(new LLRetInstr(typeEngine.INT, 
				new LLConstOp(typeEngine.INT, 10)));
		block = def.addBlock(falseSym);
		block.instrs().add(new LLRetInstr(typeEngine.INT, 
				new LLConstOp(typeEngine.INT, -10)));
				
		doIsel(mod, def);
		
		assertTrue(def.flags().contains(LLDefineDirective.MULTI_RET));
		
		assertEquals(4, blocks.size());
	}
	
	@Test
	public void testRetInt() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code() { 1 };\n");
		int idx;
		AsmInstruction inst;
		idx = findInstrWithInst(instrs, "B");
		inst = instrs.get(idx);
		matchInstr(inst, "B", RegIndOperand.class, 11);
	}


	@Test
	public void testInitLocal1() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code( => nil) { x := 1 };\n");
		
		int idx;
		AsmInstruction inst;
		idx = findInstrWithInst(instrs, "LI");
		
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, "x", NumberOperand.class, 1);
		
		idx = findInstrWithInst(instrs, "B");
		inst = instrs.get(idx);
		matchInstr(inst, "B", RegIndOperand.class, 11);
	}
	

	@Test
	public void testInitLocalAndRet1() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code( ) { x := 1 };\n");
		
		int idx;
		AsmInstruction inst;
		idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, "x", NumberOperand.class, 1);
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		AsmInstruction inst2 = instrs.get(idx);
		matchInstr(inst2, "MOV", inst.getOp1(), RegTempOperand.class, 0);

		idx = findInstrWithInst(instrs, "B", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "B", RegIndOperand.class, 11);
	}


	@Test
	public void testPtrDeref1() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x:Int^ => Int) { x^ };\n");
		
		int idx;
		AsmInstruction inst;
		
		idx = findInstrWithInst(instrs, "MOV");
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "x", RegTempOperand.class);
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegIndOperand.class, RegTempOperand.class);
		
		idx = findInstrWithInst(instrs, "B", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "B", RegIndOperand.class, 11);
	}

	@Test
	public void testPtrDeref1b() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x:Int^; y:Int) { x^=y };\n");
		
		int idx;
		AsmInstruction inst;
		
		idx = findInstrWithInst(instrs, "MOV", 2);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegIndOperand.class, "x");
		
		// TODO: this also re-reads contents from X^ before returning... blah!
		
		idx = findInstrWithInst(instrs, "B", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "B", RegIndOperand.class, 11);
	}
	

	@Test
	public void testPtrDeref2() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x:Int[10]; y:Int^) { x[5]=y^ };\n");
		
		int idx;
		AsmInstruction inst;
		
		idx = findInstrWithInst(instrs, "COPY");
		assertTrue(idx < 0);
		
		idx = findInstrWithInst(instrs, "MOV", 1);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegIndOperand.class, "y", RegTempOperand.class);
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, AddrOffsOperand.class, "%reg", 10);
		
		// TODO: this also re-reads contents from X^ before returning... blah!
		
		idx = findInstrWithInst(instrs, "B", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "B", RegIndOperand.class, 11);
	}

	@Test
	public void testPtrDeref3() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x:Int[10][4]; y:Int[4]^) { x[4]=y^ };\n");
		
		int idx;
		AsmInstruction inst;
		
		idx = findInstrWithInst(instrs, "COPY", 1);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", RegIndOperand.class, "y", AddrOperand.class);
		
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOperand.class, AddrOffsOperand.class, "%reg", 32);
		
		// TODO: this also re-reads contents from X^ before returning... blah!
		
		idx = findInstrWithInst(instrs, "B", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "B", RegIndOperand.class, 11);
	}
	@Test
	public void testPtrDeref4() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x:Int[10]^) { (x-1)[2] };\n");
		
		int idx;
		AsmInstruction inst;
		
		idx = findInstrWithInst(instrs, "LEA", 1);
		inst = instrs.get(idx);
		matchInstr(inst, "LEA", AddrOffsOperand.class, "x", -2, RegTempOperand.class);
		AsmInstruction inst0 = inst;
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOffsOperand.class, ((RegTempOperand) inst0.getOp2()).getSymbol().getName(), 4, RegTempOperand.class);
		
	}
	@Test
	public void testSetGlobal1() throws Exception {
		dumpLLVMGen = true;
		doIsel("bat := Byte;\n"+
				"foo = code(x,y:Int => nil) { bat = 10; };\n");
		
		AsmInstruction inst;
		int idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 0xA00);
		
		idx = findInstrWithSymbol(instrs, "bat");
		inst = instrs.get(idx);
		
		matchInstr(inst, "MOVB", RegTempOperand.class, AddrOperand.class, "bat"); 
		ISymbol sym = getOperandSymbol(inst.getOp2());
		assertTrue(sym+"", sym.getScope() instanceof ModuleScope);
		
		ISymbol[] targets = inst.getTargets();
		assertEquals(1, targets.length);
		assertEquals(sym, targets[0]);
		
	}
	@Test
	public void testConsts1() throws Exception {
		doIsel("foo = code( ) { x:=123; x=-3849 };\n");
		
		int idx = findInstrWithInst(instrs, "LI");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 123); 
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, -3849); 
	}
	@Test
	public void testConstsByte1() throws Exception {
		doIsel("foo = code( ) { x:Byte=123; x=-112 };\n");
		
		int idx = findInstrWithInst(instrs, "LI");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 123*256); 
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, (-112*256) & 0xff00); 
	}
	@Test
	public void testAdd() throws Exception {
		doIsel("foo = code(x,y:Int ) { z:=x+y; x+ y; };\n");
		
		// X is used again, and both come in in regs
		int idx = findInstrWithInst(instrs, "A");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "A", RegTempOperand.class, "y", RegTempOperand.class, "~x"); 
	}
	@Test
	public void testAddConst1() throws Exception {
		doIsel("foo = code(x,y:Int ) { (x+1)+(x+2)+(y-1)+(y-2) };\n");
		
		AsmInstruction inst;
		int idx;
		idx = findInstrWithInst(instrs, "INC");
		instrs.get(idx);
		idx = findInstrWithInst(instrs, "INCT");
		instrs.get(idx);
		idx = findInstrWithInst(instrs, "DEC");
		inst = instrs.get(idx);
		assertEquals(1, inst.getSources().length);
		assertEquals(1, inst.getTargets().length);
		assertEquals(inst.getSources()[0], inst.getTargets()[0]);
		idx = findInstrWithInst(instrs, "DECT");
		instrs.get(idx);
	}
	@Test
	public void testAddConst2() throws Exception {
		doIsel("foo = code(x,y:Int ) { (x-(-1))+(x-(-2)) };\n");
		
		int idx;
		idx = findInstrWithInst(instrs, "INC");
		instrs.get(idx);
		idx = findInstrWithInst(instrs, "INCT");
		instrs.get(idx);
	}

	@Test
	public void testAddAndRet1() throws Exception {
		doIsel("foo = code(x,y:Int ) { x+y };\n");
		
		int idx = findInstrWithInst(instrs, "A");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "A", RegTempOperand.class, "y", RegTempOperand.class, "~x"); 
	}
	@Test
	public void testSubAndRet1() throws Exception {
		doIsel("foo = code(x,y:Int ) { x-y };\n");
		
		int idx = findInstrWithInst(instrs, "S");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "S", RegTempOperand.class, "y", RegTempOperand.class, "~x"); 
	}
	@Test
	public void testSubAndRet2() throws Exception {
		doIsel("foo = code(x,y:Int ) { y-x };\n");
		
		int idx = findInstrWithInst(instrs, "S");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "S", RegTempOperand.class, "x", RegTempOperand.class, "~y"); 
	}
	@Test
	public void testSubRev1() throws Exception {
		doIsel("foo = code(x:Int ) { 100-x };\n");
		
		int idx = findInstrWithInst(instrs, "LI");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 100);
		AssemblerOperand temp = inst.getOp1();
		idx = findInstrWithInst(instrs, "S");
		inst = instrs.get(idx);
		matchInstr(inst, "S", RegTempOperand.class, "x", temp);
	}
	@Test
	public void testSubRev2() throws Exception {
		dumpLLVMGen = true;
		doIsel("x : Byte; foo = code(y:Int ) { x-y };\n");
		
		int idx = findInstrWithInst(instrs, "MOVB");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOVB", AddrOperand.class, "x", RegTempOperand.class);
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, NumberOperand.class, 8);

		idx = findInstrWithInst(instrs, "S");
		inst = instrs.get(idx);
		matchInstr(inst, "S", RegTempOperand.class, "y", RegTempOperand.class);
	}
	@Test
	public void testSubRevAss1() throws Exception {
		dumpLLVMGen =true;
		doIsel("x : Byte; foo = code(y:Int ) { x-=y };\n");
		
		/*
		This is optimal:
		
		SLA R0, 8
		SB R0, @X
		
		But for SSA, we need to read memory -> temp, change, then temp -> memory
		 */
		int idx = findInstrWithInst(instrs, "SLA");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, "~y", NumberOperand.class, 8);
		
		idx = findInstrWithInst(instrs, "SB");
		inst = instrs.get(idx);
		matchInstr(inst, "SB", RegTempOperand.class, "~y", RegTempOperand.class);
	}
	@Test
	public void testSubImm1() throws Exception {
		doIsel("foo = code(x:Int ) { x-1923 };\n");
		
		int idx = findInstrWithInst(instrs, "AI");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "AI", RegTempOperand.class, "~x", NumberOperand.class, -1923); 
	}
	@Test
	public void testTrunc16_to_8_1_Local() throws Exception {
		dumpLLVMGen = true;
		doIsel("foo = code(x,y:Int ) { z : Byte = x+y };\n");
		
		int idx = findInstrWithInst(instrs, "SLA");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, NumberOperand.class, 8);
	}
	
	@Test
	public void testTrunc16_to_8_1_Mem_to_Temp() throws Exception {
		dumpLLVMGen = true;
		doIsel("x := 11; foo = code( ) { Byte(x) };\n");
		
		int idx = findInstrWithInst(instrs, "SLA");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, NumberOperand.class, 8);
	}
	@Test
	public void testTrunc16_to_8_1_Mem_to_Mem() throws Exception {
		dumpLLVMGen = true;
		doIsel("x := 11; foo = code( ) { x = Byte(x) };\n");
		
		int idx;
		AsmInstruction inst;
		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOperand.class, "x", RegTempOperand.class);
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, NumberOperand.class, 8);
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, NumberOperand.class, 8);
	}
	@Test
	public void testTrunc16_to_8_1_Imm_to_Mem() throws Exception {
		dumpLLVMGen = true;
		doIsel("x : Byte; foo = code( => Int ) { x = 0x1234; };\n");
		
		int idx;
		AsmInstruction inst;
		
		// downcast on the immed
		idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 0x3400);

		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, AddrOperand.class, "x");
		
		// upcast again
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, NumberOperand.class, 8);
	}
	@Test
	public void testExt8_to_16_1_Mem_to_Mem() throws Exception {
		dumpLLVMGen = true;
		doIsel("x : Byte = 11; foo = code( ) { x = Int(x) };\n");
		
		int idx;
		AsmInstruction inst;
		idx = findInstrWithSymbol(instrs, "x");
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", AddrOperand.class, "x", RegTempOperand.class);
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, NumberOperand.class, 8);
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, NumberOperand.class, 8);
	}
	@Test
	public void testShiftLeftConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x<<1) + (x<<0) + (x<<4) + (x<<16) };\n");
		
		int idx = findInstrWithInst(instrs, "SLA");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", NumberOperand.class, 1);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", NumberOperand.class, 4);
		
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
		idx = findInstrWithSymbol(instrs, "y", 2);
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SLA");
		inst = instrs.get(idx);
		// don't change 'x'
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", RegTempOperand.class, 0);
	}
	@Test
	public void testShiftLeftEqVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x, y:Int ) { x<<=y };\n");
		
		int idx;
		idx = findInstrWithSymbol(instrs, "y", 2);
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SLA");
		inst = instrs.get(idx);
		// don't reuse 'x'; we store later
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", RegTempOperand.class, 0);
	}
	@Test
	public void testShiftLeftVarLoop() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x, y:Int ) { repeat 100 do x<<y };\n");
		
		int idx;
		idx = findInstrWithLabel("loopEnter");
		idx = findInstrWithSymbol(instrs, "y", idx);
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SLA");
		inst = instrs.get(idx);
		// do not reuse 'x': not last use
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", RegTempOperand.class, 0);
	}
	@Test
	public void testShiftRightConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x>>1) + (x>>0) + (x>>4) + (x>>16) };\n");
		
		int idx = findInstrWithInst(instrs, "SRA");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, "~x", NumberOperand.class, 1);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, "~x", NumberOperand.class, 4);

		// >>16 = by R0 with 0
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, "~x", RegTempOperand.class, 0);
		
	}
	@Test
	public void testShiftRightVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Int ) { (x>>y) };\n");
		
		int idx;
		idx = findInstrWithSymbol(instrs, "y", 2);
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRA");
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, "~x", RegTempOperand.class, 0);
		
	}
	
	@Test
	public void testUShiftRightConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x+>>1) + (x+>>0) + (x+>>4) + (x+>>16) };\n");
		
		int idx = findInstrWithInst(instrs, "SRL");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegTempOperand.class, "~x", NumberOperand.class, 1);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SRL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegTempOperand.class, "~x", NumberOperand.class, 4);

		// +>>16 = 0
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class);
		
	}
	@Test
	public void testUShiftRightVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Int ) { (x+>>y) };\n");
		
		int idx;
		idx = findInstrWithSymbol(instrs, "y", 2);
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRL");
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegTempOperand.class, "~x", RegTempOperand.class, 0);
		
	}
	
	@Test
	public void testCShiftRightConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x>>|1) + (x>>|0) + (x>>|4) + (x>>|16) };\n");
		
		int idx = findInstrWithInst(instrs, "SRC");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegTempOperand.class, "~x", NumberOperand.class, 1);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SRC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegTempOperand.class, "~x", NumberOperand.class, 4);

		// +>>16 = no-op
		idx = findInstrWithInst(instrs, "SRC", idx);
		assertEquals(-1, idx);
		
	}
	@Test
	public void testCShiftRightVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Int ) { (x>>|y) };\n");
		
		int idx;
		idx = findInstrWithSymbol(instrs, "y", 2);
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRC");
		inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegTempOperand.class, "~x", RegTempOperand.class, 0);
		
	}

	@Test
	public void testCShiftRight8Bit() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte ) { (x>>|1) + (x>>|y) };\n");
		
		int idx;
		AsmInstruction inst;
		idx = findInstrWithInst(instrs, "SWPB");
		inst = instrs.get(idx);
		matchInstr(inst, "SWPB", RegTempOperand.class, "~x");
		AsmInstruction copy = inst;
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, copy.getOp1());
	}
	
	
	@Test
	public void testCShiftLeftConst() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x<<|1) + (x<<|0) + (x<<|4) + (x<<|16) };\n");
		
		int idx = findInstrWithInst(instrs, "SRC");
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegTempOperand.class, "~x", NumberOperand.class, 15);
		
		// ignore shift by zero
		
		idx = findInstrWithInst(instrs, "SRC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegTempOperand.class, "~x", NumberOperand.class, 12);

		// +>>16 = no-op
		idx = findInstrWithInst(instrs, "SRC", idx);
		assertEquals(-1, idx);
		
	}
	@Test
	public void testCShiftLeftVar() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Int ) { (x<<|y) };\n");
		
		int idx;
		AsmInstruction inst;
		idx = findInstrWithSymbol(instrs, "y", 2);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "NEG");
		inst = instrs.get(idx);
		matchInstr(inst, "NEG", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SRC");
		inst = instrs.get(idx);
		matchInstr(inst, "SRC", RegTempOperand.class, "~x", RegTempOperand.class, 0);
		
	}

	@Test
	public void testCShiftLeft8Bit() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte ) { (x<<|1) + (x<<|y) };\n");
		
		int idx;
		AsmInstruction inst;
		idx = findInstrWithInst(instrs, "SWPB");
		inst = instrs.get(idx);
		matchInstr(inst, "SWPB", RegTempOperand.class, "~x");
		AsmInstruction copy = inst;
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, copy.getOp1());
	}

	@Test
	public void testLogicalOps() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x, y:Int ) { (x|15) + (x|y) + (x&4111) + (x&y) + (x~9) + (x~y) };\n");
		
		int idx = -1;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "ORI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ORI", RegTempOperand.class, "~x", NumberOperand.class, 15);
		
		idx = findInstrWithInst(instrs, "SOC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOC", RegTempOperand.class, "y", RegTempOperand.class, "~x");
		
		idx = findInstrWithInst(instrs, "ANDI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ANDI", RegTempOperand.class, "~x", NumberOperand.class, 4111);
		
		idx = findInstrWithInst(instrs, "SZC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SZC", RegTempOperand.class, "y", RegTempOperand.class, "~x");

		// XOR more complex
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 9);
		AsmInstruction loadinst = inst;
		
		idx = findInstrWithInst(instrs, "XOR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "XOR", loadinst.getOp(1), RegTempOperand.class, "~x");
		
		idx = findInstrWithInst(instrs, "XOR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "XOR", RegTempOperand.class, "y", RegTempOperand.class, "~x");
		
	}
	@Test
	public void testLogicalOpsByte() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x, y:Byte ) { (x|15) + (x|y) + (x&41) + (x&y) + (x~9) + (x~y) };\n");
		
		int idx = -1;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "ORI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ORI", RegTempOperand.class, "~x", NumberOperand.class, 15*256);
		
		idx = findInstrWithInst(instrs, "SOCB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOCB", RegTempOperand.class, "y", RegTempOperand.class, "~x");
		
		idx = findInstrWithInst(instrs, "ANDI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ANDI", RegTempOperand.class, "~x", NumberOperand.class, (41*256) & 0xff00);
		
		idx = findInstrWithInst(instrs, "SZCB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SZCB", RegTempOperand.class, "y", RegTempOperand.class, "~x");

		// XOR more complex
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 9*256);
		AsmInstruction loadinst = inst;
		
		idx = findInstrWithInst(instrs, "XOR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "XOR", loadinst.getOp(1), RegTempOperand.class, "~x");
		
		idx = findInstrWithInst(instrs, "XOR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "XOR", RegTempOperand.class, "y", RegTempOperand.class, "~x");
		
	}
	
	@Test
	public void testComparisonOpsInExpr() throws Exception {
		dumpLLVMGen =true;
		
		// this generates boolean comparisons and stores them for logical manipulation;
		doIsel("foo = code(x, y : Int) { (x<y) | (x==9) };\n");
		
		int idx = -1;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "C", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "C", RegTempOperand.class, "x", RegTempOperand.class, "y");
		
		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, CompareOperand.CMP_SLT, RegTempOperand.class, "~y");
		
		idx = findInstrWithInst(instrs, "CI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CI", RegTempOperand.class, "x", NumberOperand.class, 9);

		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, CompareOperand.CMP_EQ, RegTempOperand.class, "~x");
		AsmInstruction set2 = inst;

		idx = findInstrWithInst(instrs, "SOCB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOCB", set2.getOp2(), RegTempOperand.class);
	}
	@Test
	public void testComparisonOpsInExprByte() throws Exception {
		dumpLLVMGen =true;
		
		// this generates boolean comparisons and stores them for logical manipulation;
		doIsel("foo = code(x, y : Byte) { (x<y) | (x==9) };\n");
		
		int idx = -1;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "CB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CB", RegTempOperand.class, "x", RegTempOperand.class, "y");
		
		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, CompareOperand.CMP_SLT, RegTempOperand.class, "~y");
		
		// don't use CI with bytes, since the low byte is unknown
		idx = findInstrWithInst(instrs, "CB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CB", RegTempOperand.class, "x", ConstPoolRefOperand.class, 9);

		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, CompareOperand.CMP_EQ, RegTempOperand.class, "~x");
		AsmInstruction set2 = inst;

		idx = findInstrWithInst(instrs, "SOCB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SOCB", set2.getOp2(), RegTempOperand.class);
	}
	@Test
	public void testComparisonOpsInJmp() throws Exception {
		dumpLLVMGen =true;
		
		// this generates boolean comparisons and jumps on them
		doIsel("foo = code(x, y : Int) { (x<y) or (x==9) };\n");
		
		int idx = -1;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "C", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "C", RegTempOperand.class, "x", RegTempOperand.class, "y");
		
		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, CompareOperand.CMP_SLT, RegTempOperand.class, "~y");
		AsmInstruction set1 = inst;

		idx = findInstrWithInst(instrs, "JCC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "JCC", set1.getOp2(), SymbolLabelOperand.class, SymbolLabelOperand.class);

		idx = findInstrWithInst(instrs, "CI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CI", RegTempOperand.class, "x", NumberOperand.class, 9);

		idx = findInstrWithInst(instrs, "ISET", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "ISET", NumberOperand.class, CompareOperand.CMP_EQ, RegTempOperand.class, "~x");

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
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "CLR");
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class, "~x");
		
		// ignore * 1
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", NumberOperand.class, 2); // * 4
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", NumberOperand.class, 7);	// * 128
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", NumberOperand.class, 15);	// * 32768
		
	}


	@Test
	public void testMulBytePow2() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Byte ) { (x*0) + (x*1) + (x*4) + (x*64) + (x*32768) };\n");
		
		int idx;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "CLR");
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class, "~x");
		
		// ignore * 1
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", NumberOperand.class, 2); // * 4
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", NumberOperand.class, 6);	// * 64
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class);	// * 32768
		
	}
	

	@Test
	public void testMul1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x*123) + (x*-999) };\n");
		
		int idx;
		AsmInstruction inst;

		idx = findInstrWithSymbol(instrs, "x", 1);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "x", RegTempOperand.class, true);
		AsmInstruction xval = inst;
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 123);
		AsmInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "MPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MPY", val.getOp1(), xval.getOp2(), RegTempOperand.class, false);
		
		// low part is result
		idx = findInstrWithInst(instrs, "A", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", RegTempOperand.class, false, RegTempOperand.class);
		
	}

	@Test
	public void testMulByte1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte) { (x*y) + (y*x) };\n");
		
		int idx;
		AsmInstruction inst;

		idx = findInstrWithSymbol(instrs, "x", 2);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, "x", RegTempOperand.class, true);
		AsmInstruction xval = inst;
		
		idx = findInstrWithInst(instrs, "MPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MPY", RegTempOperand.class, "y", xval.getOp2(), RegTempOperand.class, false);
		AsmInstruction mpy = inst;
		
		ISymbol[] srcs = mpy.getSources();
		assertEquals(2, srcs.length);
		assertEqualSymbolIn(mpy.getOp1(), srcs[0]);
		assertEqualSymbolIn(mpy.getOp2(), srcs[1]);
		ISymbol[] dsts = mpy.getTargets();
		assertEquals(1, dsts.length);		// two regs, but one symbol
		assertEqualSymbolIn(mpy.getOp2(), dsts[0]);

		
		// low part is result
		idx = findInstrWithInst(instrs, "AB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", RegTempOperand.class, false, RegTempOperand.class);
	}
	

	/**
	 * @param op3
	 * @param iSymbol
	 */
	private void assertEqualSymbolIn(AssemblerOperand op, ISymbol symbol) {
		ISymbol asmSymbol = getOperandSymbol(op);
		assertEquals(asmSymbol, symbol);
	}

	@Test
	public void testDiv2() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x/1) + (x/4) + (x/128) + (x/32768) };\n");
		
		int idx;
		AsmInstruction inst;
		
		// ignore / 1
		
		idx = findInstrWithInst(instrs, "SRA");
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, "~x", NumberOperand.class, 2); // * 4
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, "~x", NumberOperand.class, 7);	// * 128
		
		idx = findInstrWithInst(instrs, "SRA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRA", RegTempOperand.class, "~x", NumberOperand.class, 15);	// * 32768
		
	}

	@Test
	public void testUDiv2() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x+/1) + (x+/4) + (x+/128) + (x+/32768) };\n");
		
		int idx;
		AsmInstruction inst;
		
		// ignore / 1
		
		idx = findInstrWithInst(instrs, "SRL");
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegTempOperand.class, "~x", NumberOperand.class, 2); // * 4
		
		idx = findInstrWithInst(instrs, "SRL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegTempOperand.class, "~x", NumberOperand.class, 7);	// * 128
		
		idx = findInstrWithInst(instrs, "SRL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SRL", RegTempOperand.class, "~x", NumberOperand.class, 15);	// * 32768
		
	}
	

	@Test
	public void testDiv1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x/123) + (x+/999) };\n");
		
		int idx;
		AsmInstruction inst;

		idx = findInstrWithSymbol(instrs, "x", 2);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, RegTempOperand.class, 0);	// arg 0
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 1, NumberOperand.class, 123);	// arg 1
		AsmInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.sdiv");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterOperand.class, 0, RegTempOperand.class);			// save result
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 999);
		val = inst;
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class, true);	// high word
		
		
		idx = findInstrWithInst(instrs, "DIV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DIV", val.getOp1(), RegTempOperand.class, true);
		AsmInstruction div2 = inst;
		
		ISymbol[] srcs = div2.getSources();
		assertEquals(2, srcs.length);
		assertEqualSymbolIn(div2.getOp1(), srcs[0]);
		assertEqualSymbolIn(div2.getOp2(), srcs[1]);
		ISymbol[] dsts = div2.getTargets();
		assertEquals(1, dsts.length);
		assertEqualSymbolIn(div2.getOp2(), dsts[0]);	// two regs, but one symbol
		
		idx = findInstrWithInst(instrs, "A", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", div2.getOp3(), RegTempOperand.class);	// low words are result
		

	}
	
	@Test
	public void testRemMod1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x:Int ) { (x\\123) + (x+\\555) + (x%999) };\n");
		
		int idx;
		AsmInstruction inst;


		idx = findInstrWithSymbol(instrs, "x", 2);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 1, NumberOperand.class, 123);
		AsmInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.srem");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterOperand.class, 0, RegTempOperand.class);	// save result
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "x", RegTempOperand.class, false);	// low word
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 555);
		val = inst;
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class, true);	// high word
		
		
		idx = findInstrWithInst(instrs, "DIV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DIV", val.getOp1(), RegTempOperand.class, true, RegTempOperand.class, false);
		AsmInstruction div2 = inst;
		
		idx = findInstrWithInst(instrs, "A", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", div2.getOp2(), RegTempOperand.class);	// use result
		
		idx = findInstrWithSymbol(instrs, "x", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, RegTempOperand.class);
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 1, NumberOperand.class, 999);
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.modulo");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegisterOperand.class, 0, RegTempOperand.class);	// save result
		AsmInstruction div3 = inst;
		
		idx = findInstrWithInst(instrs, "A", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "A", div3.getOp2(), RegTempOperand.class);	// high words are result
		
		

	}


	@Test
	public void testDivByte1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte ) { (x/123) + (x+/y) };\n");
		
		int idx;
		AsmInstruction inst;

		idx = findInstrWithSymbol(instrs, "x", 2);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, RegTempOperand.class, 0);	// arg 0
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 1, NumberOperand.class, 123 * 256);	// arg 1
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.sdiv");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterOperand.class, 0, RegTempOperand.class);	// save
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, "x", RegTempOperand.class, false);	// low word
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class, true);	// high word
		
		
		idx = findInstrWithInst(instrs, "DIV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DIV", RegTempOperand.class, "y", RegTempOperand.class, true);
		AsmInstruction div2 = inst;
		
		idx = findInstrWithInst(instrs, "AB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", div2.getOp3(), RegTempOperand.class);	// low words are result
		

	}
	
	@Test
	public void testModByte1() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x,y:Byte) { (x\\123) + (x+\\55) + (x%y) };\n");
		
		int idx;
		AsmInstruction inst;


		idx = findInstrWithSymbol(instrs, "x", 2);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 1, NumberOperand.class, 123 * 256);
		AsmInstruction val = inst;
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.srem");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterOperand.class, 0, RegTempOperand.class);	// save result
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, "x", RegTempOperand.class, false);	// low word
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 55 * 256);
		val = inst;
		
		idx = findInstrWithInst(instrs, "CLR", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "CLR", RegTempOperand.class, true);	// high word
		
		
		idx = findInstrWithInst(instrs, "DIV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DIV", val.getOp1(), RegTempOperand.class, true, RegTempOperand.class, false);
		AsmInstruction div2 = inst;
		
		idx = findInstrWithInst(instrs, "AB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", div2.getOp2(), RegTempOperand.class);	// use result
		
		idx = findInstrWithSymbol(instrs, "x", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegTempOperand.class, "y", RegTempOperand.class, 1);
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "intrinsic.modulo");		// intrinsic
		
		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", RegisterOperand.class, 0, RegTempOperand.class);	// save result
		AsmInstruction div3 = inst;
		
		idx = findInstrWithInst(instrs, "AB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "AB", div3.getOp2(), RegTempOperand.class);	// high words are result
		
		
		

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
		AsmInstruction inst;

		// make sure we compare against 0 with "C r,r" not "CI r,0"
		idx = findInstrWithLabel("loopEnter");
		idx = findInstrWithInst(instrs, "C", idx-1);
		inst = instrs.get(idx);
		matchInstr(inst, "C", RegTempOperand.class, "counter", RegTempOperand.class, "counter");
		idx = findInstrWithInst(instrs, "DEC", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "DEC", RegTempOperand.class, "~counter");
		

    }

	@Test
	public void testCalls1() throws Exception {
		dumpLLVMGen = true;
    	doIsel("forward util;\n"+
    			"testCalls1 = code (=>nil) {\n" +
    			"   util();\n" +
    			"};\n"+
    			"util = code () { };\n");

		int idx;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "AI");
		if (idx >= 0)
			fail("need no stack");
		
		idx = findInstrWithInst(instrs, "LEA", idx);
		if (idx >= 0)
			fail("need no stack temp");
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "util");
		
		ISymbol[] srcs = inst.getSources();
		assertEquals(1, srcs.length);
		assertEqualSymbolIn(inst.getOp1(), srcs[0]);
		ISymbol[] dsts = inst.getTargets();
		assertEquals(0, dsts.length);

	}
	

	@Test
	public void testCallsBigRet1() throws Exception {
		dumpLLVMGen = true;
    	doIsel("forward util;\n"+
    			"testCallsBigRet1 = code (a,b,c,d:Float=>nil) {\n" +
    			"   util(a,b,c,d);\n" +
    			"};\n"+
    			"util = code (a,b,c,d : Float => Float) { 0. };\n");

		int idx;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "AI");
		inst = instrs.get(idx);
		matchInstr(inst, "AI", RegisterOperand.class, 10, NumberOperand.class, -16);
		
		// caller stack op in R0
		idx = findInstrWithInst(instrs, "LEA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LEA", AddrOperand.class, ".callerRet", RegTempOperand.class, 0);
		assertEquals(typeEngine.FLOAT, ((StackLocalOperand)((AddrOperand) inst.getOp1()).getAddr()).getLocal().getType());
		AssemblerOperand retStack = ((AddrOperand) inst.getOp1()).getAddr();
		
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOperand.class, "a", RegOffsOperand.class, 10, 16);
		
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOperand.class, "b", RegOffsOperand.class, 10, 12);
		
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOperand.class, "c", RegOffsOperand.class, 10, 8);
		
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOperand.class, "d", RegOffsOperand.class, 10, 4);
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", AddrOperand.class, "util");
		
		ISymbol[] srcs = inst.getSources();
		assertEquals(1, srcs.length);
		assertEqualSymbolIn(inst.getOp1(), srcs[0]);
		ISymbol[] dsts = inst.getTargets();
		assertEquals(1, dsts.length);
		assertEqualSymbolIn(retStack, dsts[0]);

	}
	

	@Test
	public void testCallsBigRet2() throws Exception {
		dumpLLVMGen = true;
    	doIsel(
    			"testCallsBigRet2 = code (x:Float=>Float) {\n" +
    			" x;\n" +
    			"};\n"+
    			"");
		int idx;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "COPY");
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOperand.class, "x", RegIndOperand.class, 0);
	}
	

    @Test
    public void testSelfRef3() throws Exception {
    	dumpLLVMGen = true;
    	doIsel(
    			"Class = data {\n"+
    			"  draw:code(this:Class; count:Int => nil);\n"+
    			"};\n"+
    			"forward doDraw;\n"+
    			"testSelfRef3 = code() {\n"+
    			"  inst : Class;\n"+
    			"  inst.draw = doDraw;\n"+
    			"  inst.draw(inst, 5);\n"+
    			"};\n"+
    			"doDraw = code(this:Class; count:Int => nil) { count*count };\n"+
    	"");
    	int idx;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, SymbolOperand.class, "doDraw");
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, AddrOperand.class, "inst");
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOperand.class, "inst", RegTempOperand.class);
		AsmInstruction getFuncInst = inst;
		
		
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOperand.class, "inst", RegOffsOperand.class, 10, 2);
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 5);
		AsmInstruction liInst = inst;
		
		
		idx = findInstrWithInst(instrs, "BL");
		inst = instrs.get(idx);
		matchInstr(inst, "BL", RegIndOperand.class);
		
		ISymbol[] srcs = inst.getSources();
		assertEquals(2, srcs.length);
		assertEqualSymbolIn(liInst.getOp1(), srcs[0]);
		assertEqualSymbolIn(getFuncInst.getOp2(), srcs[1]);
		ISymbol[] dsts = inst.getTargets();
		assertEquals(0, dsts.length);

    }
    
    @Test
    public void testPtrRef4() throws Exception {
    	dumpLLVMGen = true;
    	doIsel(
    			"Class = data {\n"+
    			"  draw:code(this:Class^; count:Int => nil);\n"+
    			"};\n"+
    			"forward doDraw;\n"+
    			"testSelfRef3 = code() {\n"+
    			"  inst : Class^;\n"+
    			"  inst.draw = doDraw;\n"+
    			"  inst.draw(inst, 5);\n"+
    			"};\n"+
    			"doDraw = code(this:Class^; count:Int => nil) { count*count };\n"+
    	"");
    	int idx;
		AsmInstruction inst;


		idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, SymbolOperand.class, "doDraw");
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, RegIndOperand.class, "inst");
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegIndOperand.class, "inst", RegTempOperand.class);
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "LI", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, 1, NumberOperand.class, 5);
		
		
		
		idx = findInstrWithInst(instrs, "BL", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "BL", RegIndOperand.class);
    }

    @Test
    public void testPtrRef5() throws Exception {
    	dumpLLVMGen = true;
    	dumpIsel = true;
    	doIsel(
    			"List = [T] data {\n"+
    			"  next:List^; node:T;\n"+
    			"};\n"+
    			"testPtrRef5 = code() {\n"+
    			"  loc : List<Int>;" +
    			"  loc.node = 100;\n"+
    			"  inst : List<Int>^ = &loc;\n"+
    			"  inst.next = inst;\n"+
    			"  inst.next.next.next.node;\n"+
    			"};\n"+
    	"");
    	int idx;
		AsmInstruction inst;


		idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class, NumberOperand.class, 100);
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, AddrOffsOperand.class, "%reg", 2);
		
		// addr goes here
		idx = findInstrWithInst(instrs, "LEA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LEA", AddrOperand.class, "loc", RegTempOperand.class, "inst");
		
		// set the 'next' field
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "inst", RegIndOperand.class);
		
		// ptr derefs
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegIndOperand.class, RegTempOperand.class);
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegIndOperand.class, RegTempOperand.class);
		
		// copy
		//idx = findInstrWithInst(instrs, "MOV", idx);
		//inst = instrs.get(idx);
		//matchInstr(inst, "MOV", RegTempOperand.class, RegTempOperand.class);
		
		// hard to find the next instr since it depends on a temp
		while (idx < instrs.size()) {
			inst = instrs.get(idx);
			if (inst.getInst() == InstructionTable.Imov) {
				if (inst.getOp1() instanceof AddrOffsOperand) {
					assertEquals(2, ((NumberOperand)((AddrOffsOperand) inst.getOp1()).getOffset()).getValue());
					idx = -1;
					break;
				}
			}
			idx++;
		}
		assertEquals(-1, idx);
    }


    @Test
    public void testPtrCalc6() throws Exception {
    	doIsel(
    			"forward Complex;\n"+
    			"Inner = data {\n"+
    			"  d1,d2:Float;\n"+
    			"  p : Complex^;\n"+
    			"};\n"+
    			"Complex = data {\n"+
    			"  a,b,c:Byte;\n"+
    			"  d : Inner;\n"+
    			" };\n"+
    			"testPtrCalc6 = code() {\n"+
    			"  c : Complex;\n" +
    			"  c.d.p.d.d2;\n"+
    			"};\n"+
    	"");
    	int idx;
		AsmInstruction inst;


		// get addr of c to Complex*
		idx = findInstrWithInst(instrs, "LEA");
		inst = instrs.get(idx);
		matchInstr(inst, "LEA", AddrOperand.class, "c", RegTempOperand.class);
		
		// get 'd' offset inside, to Inner*
		idx = findInstrWithInst(instrs, "LEA", idx);
		AsmInstruction inst2 = instrs.get(idx);
		matchInstr(inst2, "LEA", AddrOffsOperand.class, inst.getOp2(), 4, RegTempOperand.class);
		
		// then, deref 'p' to Complex* 
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOffsOperand.class, inst2.getOp2(), RegTempOperand.class);
		
		// get 'd' offset inside, to Inner*
		idx = findInstrWithInst(instrs, "LEA", idx);
		inst2 = instrs.get(idx);
		matchInstr(inst2, "LEA", AddrOffsOperand.class, inst.getOp2(), RegTempOperand.class);
		
		// read 'd2'
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOffsOperand.class, inst2.getOp2(), 4, AddrOperand.class);
    }

    @Test
    public void testRetAddr1() throws Exception {
    	dumpLLVMGen = true;
    	doIsel(
    			"List = [T] data {\n"+
    			"  next:List^; node:T;\n"+
    			"};\n"+
    			"testPtrRef5 = code() {\n"+
    			"  loc : List<Int>;" +
    			" &loc;\n"+
    			"};\n"+
    	"");
    	int idx;
		AsmInstruction inst;


		idx = findInstrWithInst(instrs, "LEA");
		inst = instrs.get(idx);
		matchInstr(inst, "LEA", AddrOperand.class, "loc", RegTempOperand.class, 0);
		
    }

    @Test
    public void testRetAddr2() throws Exception {
    	dumpLLVMGen = true;
    	doIsel(
    			"x : Byte;\n"+
    			"testRetAddr2 = code() {\n"+
    			" y : Byte^;\n"+
    			" y = &x;\n"+
    			"};\n"+
    	"");
    	int idx;
		AsmInstruction inst;

		idx = findInstrWithInst(instrs, "LI");
		inst = instrs.get(idx);
		matchInstr(inst, "LI", RegTempOperand.class,  SymbolOperand.class, "x");
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class,  RegTempOperand.class, "y");
		
		idx = findInstrWithInst(instrs, "MOV", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y",  RegTempOperand.class, 0);
		
    }
    
    @Test
    public void testTuples1() throws Exception {
    	dumpIsel = true;
    	doIsel(
    			"makeTuple = code(x:Int;y) { (x,y*x,66) };\n"+
    	"");
    	boolean found = false;
		for (AsmInstruction inst : instrs) {
			if (inst.getInst() == InstrSelection.Pcopy) {
				if (found) fail("too many copies");
				found = true;
				assertTrue(inst.getOp1() instanceof TupleTempOperand);
				AssemblerOperand[] cs = ((TupleTempOperand)inst.getOp1()).getComponents();
				assertEquals(3, cs.length);
				assertNotNull(cs[0]);
				assertNotNull(cs[1]);
				assertNotNull(cs[2]);
				assertTrue(inst.getOp2() instanceof RegIndOperand);
				assertTrue(((RegIndOperand)inst.getOp2()).isReg(0));
				break;
			}
		}

    }
    @Test
    public void testTuples2() throws Exception {
    	dumpLLVMGen = true;
    	doIsel(
    			"forward makeTuple;\n"+
    			"useTuple = code(i:Int) { (x,y) := makeTuple(19, i); x+y; };\n"+
    			"makeTuple = code(x,y:Int) { (x,y) };\n"+
    	"");
    	int idx;
		AsmInstruction inst;

		// caller stack op in R0
		idx = findInstrWithInst(instrs, "LEA", -1);
		inst = instrs.get(idx);
		matchInstr(inst, "LEA", AddrOperand.class, ".callerRet", RegTempOperand.class, 0);
		AddrOperand addrOperand = (AddrOperand) inst.getOp1();
		StackLocal local = ((StackLocalOperand)addrOperand.getAddr()).getLocal();
		assertTrue(local.getType() instanceof LLTupleType);

		// extract instrs copy pieces from the operand
		idx = findInstrWithInst(instrs, "BL", idx);
		
		idx = findInstrWithSymbol(instrs, ".callerRet", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOffsOperand.class, ".callerRet", 0, RegTempOperand.class);
		
		idx = findInstrWithSymbol(instrs, ".callerRet", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOV", AddrOffsOperand.class, ".callerRet", 2, RegTempOperand.class);
		
    }

    @Test
    public void testDataFields() throws Exception {
    	dumpIsel = true;
    	doIsel(
    			"Class = data { x,b:Byte; y:Float; };\n"+
    			"useClass = code(i:Int) { c : Class; x := c.x + c.b; y := c.y;  };\n"+
    	"");
    	int idx;
		AsmInstruction inst;
		

		// c.x fetched directly
		idx = findInstrWithInst(instrs, "MOVB", -1);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", AddrOperand.class, "c", RegTempOperand.class);

		// get an address for c.y
		idx = findInstrWithInst(instrs, "LEA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LEA", AddrOperand.class, "c", RegTempOperand.class);

		idx = findInstrWithInst(instrs, "MOVB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "MOVB", AddrOffsOperand.class, "%reg", 1, RegTempOperand.class);
		
		// skip...
		
		// get the c.y
		idx = findInstrWithInst(instrs, "LEA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "LEA", AddrOperand.class, "c", RegTempOperand.class);
		
		// copy float out
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOffsOperand.class, "%reg", 2, AddrOperand.class);

		// and, sadly, copy again
		idx = findInstrWithInst(instrs, "COPY", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "COPY", AddrOperand.class, AddrOperand.class, "y");
		
    }
    
	@Test
	public void testReturnMulti() throws Exception {
		dumpLLVMGen =true;
		doIsel("foo = code(x, y:Int ) { if x < y then -1 elif x == y then 0 else { repeat 100 do x<<y } };\n");
		
		int idx;
		idx = findInstrWithLabel("loopEnter");
		idx = findInstrWithSymbol(instrs, "y", idx);
		AsmInstruction inst = instrs.get(idx);
		matchInstr(inst, "MOV", RegTempOperand.class, "y", RegTempOperand.class, 0);
		
		idx = findInstrWithInst(instrs, "SLA");
		inst = instrs.get(idx);
		// do not reuse 'x': not last use
		matchInstr(inst, "SLA", RegTempOperand.class, "~x", RegTempOperand.class, 0);
	}
	
    @Test
    public void testDataInit1() throws Exception {
    	dumpLLVMGen = true;
    	dumpIsel = true;
    	doIsel(
    			"Tuple = data {\n"+
    			"   x:Byte; f:Bool; y,z:Byte; };\n"+
    			"testDataInit1 = code() {\n"+
    			"  foo:Tuple = [ 3, 1, .z=0x10, .y=0x20 ];\n"+
    			"   if foo.f then foo.x else foo.y<<foo.z;\n" +
    			"};\n"+
    	"");
    	int idx = -1;
    	AsmInstruction inst;
    	
		idx = findInstrWithInst(instrs, "SWPB", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SWPB", RegTempOperand.class, 0);
		AssemblerOperand shift = inst.getOp1();
		
		idx = findInstrWithInst(instrs, "SLA", idx);
		inst = instrs.get(idx);
		matchInstr(inst, "SLA", RegTempOperand.class, shift);
    }

    @Test
    public void testDataInitVar1() throws Exception {
    	doIsel(
    			"testDataInit2 = code() {\n"+
    			"  val := 10;\n"+
    			"  foo:Int[10] = [ [5] = val, [1] = 11 ];\n"+
    			"};\n"+
    	"");
    }

	@Test
    public void testDataInit4() throws Exception {
		doIsel(
    			"testDataInit4 = code() {\n"+
    			"  foo:Byte[][3] = [ [ 1, 2, 3], [4, 5, 6], [7, 8, 9]];\n"+
    			"};\n"+
    	"");
    }

	@Test
	public void testDataLocalUse1() throws Exception {
		dumpIsel = true;
	   	doIsel(
	   			"Tuple = data {\n"+
	   			"   x:Byte; f:Bool; };\n"+
	   			"testDataInit1 = code() {\n"+
	   			"  foo:Tuple;\n"+
	   			"  foo.x = 3; foo.f = 1; ;\n"+
	   			"  if foo.f then foo.x else 123;\n" +
	   			"};\n"+
	   	"");
	   	
	   	for (AsmInstruction inst : instrs) {
    		if (inst.getInst() == InstructionTable.Ili) {
    			if (((NumberOperand)inst.getOp2()).getValue() == 3)
    				fail(inst+": expected >0300");
    			if (((NumberOperand)inst.getOp2()).getValue() == 1)
    				fail(inst+": expected >0100");
    			if (((NumberOperand)inst.getOp2()).getValue() == 0x20)
    				fail(inst+": expected >2000");
    			if (((NumberOperand)inst.getOp2()).getValue() == 0x10)
    				fail(inst+": expected >1000");
    		}
    	}
	}
    
}
