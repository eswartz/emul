/**
 * 
 */
package org.ejs.v9t9.forthcomp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.v9t9.forthcomp.AbortException;
import org.ejs.v9t9.forthcomp.F99TargetContext;
import org.ejs.v9t9.forthcomp.ForthComp;
import org.ejs.v9t9.forthcomp.HostContext;
import org.ejs.v9t9.forthcomp.ITargetWord;
import org.ejs.v9t9.forthcomp.IWord;
import org.ejs.v9t9.forthcomp.TargetColonWord;
import org.junit.Before;
import org.junit.Test;

import v9t9.emulator.hardware.F99Machine;
import v9t9.emulator.hardware.F99MachineModel;
import v9t9.emulator.runtime.cpu.CpuF99;
import v9t9.emulator.runtime.cpu.DumpFullReporterF99;
import v9t9.emulator.runtime.interpreter.InterpreterF99;
import v9t9.engine.cpu.InstF99;
import static v9t9.engine.cpu.InstF99.*;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public class TestForthComp {

	/**
	 * 
	 */
	private static final int BASE_RP = 0xff00;
	/**
	 * 
	 */
	private static final int BASE_SP = 0xf800;
	private F99TargetContext targCtx;
	ForthComp comp;
	HostContext hostCtx;
	private int startDP;
	
	static F99MachineModel f99MachineModel;
	static F99Machine f99Machine;
	private static InterpreterF99 interp;
	private static CpuF99 cpu;

	/**
	 * 
	 */
	public TestForthComp() {
		if (cpu == null) {
			f99MachineModel = new F99MachineModel();
			f99Machine = (F99Machine) f99MachineModel.createMachine();
			interp = new InterpreterF99(f99Machine);
			cpu = (CpuF99) f99Machine.getCpu();
		
			DumpFullReporterF99 dump = new DumpFullReporterF99(cpu,  new PrintWriter(System.out));
			f99Machine.getExecutor().addInstructionListener(dump);
		}

	}
	@Before
	public void setup() {
		targCtx = new F99TargetContext(4096);
		targCtx.setDP(0x400);
		comp = new ForthComp(targCtx);
		hostCtx = comp.getHostContext();
		
		for (int i = 0; i <65536; i+= 2)
			cpu.getConsole().writeWord(i, (short) 0);
		
		targCtx.defineBuiltins();
		startDP = targCtx.getDP();
	}
	@Test
	public void testLiteral() throws Exception {
		comp.parseString("123");
		assertEquals(123, hostCtx.popData());
	}
	@Test
	public void testLiteral2() throws Exception {
		comp.parseString("123 456");
		assertEquals(456, hostCtx.popData());
		assertEquals(123, hostCtx.popData());
	}
	
	@Test
	public void testVariable1() throws Exception {
		targCtx.clearDict();
		
		startDP = targCtx.getDP();
		
		comp.parseString("Variable t");
		IWord var = targCtx.require("T");

		var.execute(hostCtx, targCtx);
		assertEquals(startDP + 4, hostCtx.popData());
		//assertEquals(-1, hostCtx.popData());
		//assertEquals(((ITargetWord)var).getEntry().getContentAddr(), targCtx.resolveAddr(-1));
		
		assertTrue(startDP == 0 || startDP > targCtx.readCell(startDP));
		assertEquals((byte)0x81, targCtx.readChar(startDP + 2));
		assertEquals('t', targCtx.readChar(startDP + 3));
		
	}
	@Test
	public void testVariable2() throws Exception {
		targCtx.clearDict();
		
		startDP = targCtx.getDP();
		
		comp.parseString("Variable t Variable ud");
		
		ITargetWord tvar = (ITargetWord) targCtx.require("T");
		tvar.execute(hostCtx, targCtx);
		ITargetWord uvar = (ITargetWord) targCtx.require("Ud");
		uvar.execute(hostCtx, targCtx);
		
		//assertEquals(-2, hostCtx.popData());
		//assertEquals(-1, hostCtx.popData());
		//assertEquals(tvar.getEntry().getContentAddr(), targCtx.resolveAddr(-1));
		//assertEquals(uvar.getEntry().getContentAddr(), targCtx.resolveAddr(-2));
		
		assertEquals(0, tvar.getEntry().getAddr() & 1);
		assertEquals(0, tvar.getEntry().getContentAddr() & 1);
		assertEquals(0, uvar.getEntry().getAddr() & 1);
		assertEquals(0, uvar.getEntry().getContentAddr() & 1);
	}
	
	@Test
	public void testCompileTimeVariableLoadStore() throws Exception {
		targCtx.clearDict();
		comp.parseString("Variable t Variable u 123 t ! t @ u !  u @");
		
		IWord uvar = targCtx.require("U");
		assertEquals(123, targCtx.readCell(((ITargetWord)uvar).getEntry().getParamAddr()));
		
		assertEquals(123, hostCtx.popData());
	}
	@Test
	public void testColon1() throws Exception {

		ITargetWord semiS = (ITargetWord) targCtx.require(";S");
		assertNotNull(semiS);

		comp.parseString(": foo ;");
		TargetColonWord foo = (TargetColonWord) targCtx.require("foo");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertTrue(word+"", word == (Iexit << 10));
		
		assertEquals(dp + targCtx.getCellSize(), targCtx.getDP());
		
		
	}
	@Test
	public void testColon2() throws Exception {
		comp.parseString(": foo ; : b ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("foo");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertTrue(word+"", word == (Iexit << 10));
		
		// b should be aligned after foo
		
		TargetColonWord bword = (TargetColonWord) targCtx.require("b");
		assertEquals(dp + targCtx.getCellSize(), bword.getEntry().getAddr());
		
		dp = bword.getEntry().getContentAddr();
		word = targCtx.readAddr(dp);
		assertTrue(word+"", word == (Iexit << 10));
		
		assertEquals(dp + targCtx.getCellSize(), targCtx.getDP());
		
		
	}
	
	@Test
	public void testPrimPacking1() throws Exception {
		comp.parseString(": foo @ ! 0 dup ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("foo");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, Iload, Istore, IfieldLit);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, 0, Idup, Iexit);
		
		assertEquals(dp + 4, targCtx.getDP());
	}
	
	private void assertOpword(int word, int a, int b, int c) {
		if ( a < -16 || a >= 32)
			fail("bad test: " + a);
		if ( b < -16 || b >= 32)
			fail("bad test: " + b);
		if ( c < -16 || c >= 32)
			fail("bad test: " + c);
		int desired = ((a & 0x1f) << 10) | ((b & 0x1f) << 5) | (c & 0x1f);
		if (word != desired) {
			int ra = (word >> 10) & 0x1f;
			int rb = (word >> 5) & 0x1f;
			int rc = (word >> 0) & 0x1f;
			fail("expected " + HexUtils.toHex4(desired)+"; got " + HexUtils.toHex4(word) + " (" +ra+"|"+rb+"|"+rc+")");
		}
	}
	
	private void dumpMemory(PrintStream out, int from, int to, MemoryDomain domain) {
		System.out.println("raw memory:");
		int perLine = 8;
		int lines = ((to - from) / 2 + perLine - 1) / perLine;
		int addr = from;
		for (int i = 0; i < lines; i++) {
			out.print(HexUtils.toHex4(addr) + ": ");
			for (int j = 0; j < perLine && addr < to; j++) {
				out.print(HexUtils.toHex4(domain.readWord(addr)) + " ");
				addr += 2;
			}
			out.println();
		}
	}

	private void dumpDict() {
		System.out.println("dictionary cells:");
		targCtx.dumpDict(System.out, startDP, targCtx.getDP());
	}
	
	@Test
	public void testLiterals1() throws Exception {
		comp.parseString(": eq 15 3 - 0= ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, IfieldLit, 15, IfieldLit);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, 3, Ibinop, OP_SUB);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, I0equ, Iexit, 0);
		
		assertEquals(dp + 6, targCtx.getDP());
	}

	@Test
	public void testLiterals2() throws Exception {
		comp.parseString(": eq 15 456 = ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, IfieldLit, 15, Ilit);
		word = targCtx.readAddr(dp + 2);
		assertEquals(456, word);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, Iequ, Iexit, 0);
		
		assertEquals(dp + 6, targCtx.getDP());
	}

	@Test
	public void testLiterals3() throws Exception {
		comp.parseString(": eq -3 5 4 ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, IfieldLit, -3, IfieldLit);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, 5, IfieldLit, 4);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, Iexit, 0, 0);
		
		assertEquals(dp + 6, targCtx.getDP());
	}
	

	@Test
	public void testLiterals3Ex() throws Exception {
		comp.parseString(": eq -3 5 3 ;");

		interpret("eq");
		
		assertEquals(3, hostCtx.popData());
		assertEquals(5, hostCtx.popData());
		assertEquals(-3, hostCtx.popData());
	}
	

	@Test
	public void testLiterals6() throws Exception {
		comp.parseString(": eq 11. $ffff.aaaa ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, Iext, IfieldLit_d - _Iext, 11); 
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, Iext, Ilit_d - _Iext, Iexit);
		word = targCtx.readAddr(dp + 4);
		assertEquals(0xaaaa, word);
		word = targCtx.readAddr(dp + 6);
		assertEquals(0xffff, word);
	}
	/**
	 * @param name
	 * @throws AbortException 
	 * @throws IOException 
	 */
	private void interpret(String name) throws AbortException {
		targCtx.exportState(hostCtx, f99Machine, BASE_SP, BASE_RP);

		dumpCompiledMemory();
		
		ITargetWord word = (ITargetWord) targCtx.require(name);
		
		int pc = word.getEntry().getContentAddr();
		
		cpu.rpush((short) 0);
		cpu.setPC((short) pc);
		interp.setShowSymbol();
		while (cpu.getPC() != 0)
			interp.execute();

		assertTrue(cpu.getState().getSP() <= cpu.getState().getBaseSP());
		assertTrue(cpu.getState().getRP() <= cpu.getState().getBaseRP());
		targCtx.importState(hostCtx, f99Machine, BASE_SP, BASE_RP);
		
	}
	private void dumpCompiledMemory() {
		dumpMemory(System.out, startDP, targCtx.getDP(), f99Machine.getConsole());
	}
	
	@Test
	public void testLiterals4() throws Exception {
		comp.parseString(": eq 122 @ 456 ! 789 dup ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, Ilit, Iload, Ilit);
		word = targCtx.readAddr(dp + 2);
		assertEquals(122, word);
		word = targCtx.readAddr(dp + 4);
		assertEquals(456, word);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, Istore, Ilit, Idup);
		word = targCtx.readAddr(dp + 8);
		assertEquals(789, word);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, Iexit, 0, 0);
		
		assertEquals(dp + 12, targCtx.getDP());
	}
	
	@Test
	public void testLiterals4Ex() throws Exception {
		comp.parseString(": eq 1020 1456 ! 789 dup ;");
		
		targCtx.writeCell(1020, 1000);
		
		interpret("eq");
		
		assertEquals(1020, targCtx.readCell(1456));
		assertEquals(789, hostCtx.popData());
	}

	@Test
	public void testLiterals5Ex() throws Exception {
		comp.parseString(": num $1234.5678 ;");
		
		interpret("num");
		
		assertEquals(0x1234, hostCtx.popData());
		assertEquals(0x5678, hostCtx.popData());
	}

	@Test
	public void testIfBranch0() throws Exception {
		comp.parseString(": true if -1 else 0 then ;");

		TargetColonWord foo = (TargetColonWord) targCtx.require("true");
		
		dumpDict();
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, I0branch, IfieldLit, -1);
		word = targCtx.readAddr(dp + 2);
		assertEquals(8, word);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, Iext, Ibranch - _Iext, 0);	// must break (FOR NOW)
		word = targCtx.readAddr(dp + 6);
		assertEquals(6, word);
		word = targCtx.readAddr(dp + 8);
		assertOpword(word, IfieldLit, 0, 0);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, Iexit, 0, 0);	// must break (FOR NOW)
	}

	@Test
	public void testIfBranch0Ex() throws Exception {
		comp.parseString(": true if -1 else 0 then ;");

		hostCtx.pushData(5);
		interpret("true");
		assertEquals(-1, hostCtx.popData());
		
		hostCtx.pushData(0);
		interpret("true");
		assertEquals(0, hostCtx.popData());
	}
	
	@Test
	public void testIfBranch() throws Exception {
		comp.parseString(": sgn dup 0< if drop -1 else 0= if 0 else 1 then then ;");

		dumpDict();
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("sgn");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);

		assertOpword(word, Idup, I0cmp, InstF99.CMP_LT);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, I0branch, Idrop, IfieldLit);
		word = targCtx.readAddr(dp + 4);
		assertEquals(8, word);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, -1, Iext, Ibranch - _Iext);
		word = targCtx.readAddr(dp + 8);
		assertEquals(14, word);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, I0equ, I0branch, IfieldLit);
		word = targCtx.readAddr(dp + 12);
		assertEquals(8, word);
		word = targCtx.readAddr(dp + 14);
		assertOpword(word, 0, Iext, Ibranch - _Iext);
		word = targCtx.readAddr(dp + 16);
		assertEquals(6, word);
		word = targCtx.readAddr(dp + 18);
		assertOpword(word, IfieldLit, 1, 0);
		word = targCtx.readAddr(dp + 20);
		assertOpword(word, Iexit, 0, 0);

		/*
		assertOpword(word, Idup, I0lt, I0fieldBranch);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, 8, 0, IfieldBranch);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, 12, Idrop, InegOne);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, IfieldBranch, 4, I0equ);
		word = targCtx.readAddr(dp + 8);
		assertOpword(word, I0fieldBranch, 4, Izero);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, IfieldBranch, 1, Ione);
		word = targCtx.readAddr(dp + 12);
		assertOpword(word, Iexit, 0, 0);
		*/

	}

	@Test
	public void testIfBranchEx() throws Exception {
		comp.parseString(": sgn dup 0< if drop -1 else 0= if 0 else 1 then then ;");

		hostCtx.pushData(5);
		interpret("sgn");
		assertEquals(1, hostCtx.popData());
		
		hostCtx.pushData(-102);
		interpret("sgn");
		assertEquals(-1, hostCtx.popData());
		
		hostCtx.pushData(0);
		interpret("sgn");
		assertEquals(0, hostCtx.popData());

	}
	
	@Test
	public void testColonCall() throws Exception {
		comp.parseString(": sub negate 10 + ; : outer 100 sub -50 sub + ;");

		dumpDict();
		
		TargetColonWord sub = (TargetColonWord) targCtx.require("sub");
		
		TargetColonWord outer = (TargetColonWord) targCtx.require("outer");
		
		int dp = outer.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, Ilit, 0, 0);
		word = targCtx.readAddr(dp + 2);
		assertEquals(100, word);
		word = targCtx.readAddr(dp + 4);
		assertEquals(sub.getEntry().getContentAddr(), targCtx.findReloc(dp + 4));
	}
	

	@Test
	public void testColonCallEx() throws Exception {
		comp.parseString(": sub negate 10 + ; : outer 100 sub -50 sub + ;");
		
		interpret("outer");
		assertEquals(-30, hostCtx.popData());
	}
	

	@Test
	public void testMultiplyDivide() throws Exception {
		comp.parseString(
				//": */ ( n1 n2 n3 -- n4 ) >r um* r> um/mod  swap drop ;\n" +
				": */mod ( n1 n2 n3 -- rem quot ) >r um* r> um/mod ;\n" +
				": */ ( n1 n2 n3 -- n4 ) */mod swap drop ;\n" +
				": percent ( val p -- prod ) 100 */ ;\n" +
				": outer 500 25 percent ;");
		
		interpret("outer");
		assertEquals(125, hostCtx.popData());
	}
	

	@Test
	public void testDoubleMath1Ex() throws Exception {
		comp.parseString(
				": outer $8888 $ffff um* $8887.7778 d= ;");
		
		interpret("outer");
		assertEquals(-1, hostCtx.popData());
	}
	

	@Test
	public void testDoubleMath2Ex() throws Exception {
		comp.parseString(
				": outer $8887.7778 2>r  $8887 r> = $7778 r> = + ;");
		
		interpret("outer");
		assertEquals(-2, hostCtx.popData());
	}
	@Test
	public void testShifts1Ex() throws Exception {
		comp.parseString(
				": outer $8887 12 urshift  $8887 8 rshift $ffff 15 lshift ;");
		
		interpret("outer");
		assertEquals((short)0x8000, hostCtx.popData());
		assertEquals((short)0xff88, hostCtx.popData());
		assertEquals((short)0x0008, hostCtx.popData());
	}
	@Test
	public void testShifts2Ex() throws Exception {
		comp.parseString(
				": outer $8887 12U urshift  $8887 8U rshift $ffff 15U lshift ;");
		
		interpret("outer");
		assertEquals((short)0x8000, hostCtx.popData());
		assertEquals((short)0xff88, hostCtx.popData());
		assertEquals((short)0x0008, hostCtx.popData());
	}
	@Test
	public void testDoublShifts1Ex() throws Exception {
		comp.parseString(
				": outer $8887.7778 16. durshift  $8887.7778 8. drshift ;");
		
		interpret("outer");
		assertEquals((short)0xff88, hostCtx.popData());
		assertEquals((short)0x8777, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals((short)0x8887, hostCtx.popData());
	}
	@Test
	public void testDoublShifts2Ex() throws Exception {
		comp.parseString(
				": outer $8887.7778 16.U durshift  $8887.7778 8.U drshift ;");
		
		interpret("outer");
		assertEquals((short)0xff88, hostCtx.popData());
		assertEquals((short)0x8777, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals((short)0x8887, hostCtx.popData());
	}
	@Test
	public void testDoLoop() throws Exception {
		comp.parseString(": stack 0 do i loop ;");

		dumpDict();
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("stack");
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);

		assertOpword(word, IfieldLit, 0, Iext);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, ItoR_d - _Iext, 0, 0);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word,  Iext, Ii - _Iext , Iloop);
		word = targCtx.readAddr(dp + 6);
		assertEquals(0 & 0xffff, word);
		word = targCtx.readAddr(dp + 8);
		assertOpword(word,  Irdrop, Irdrop, Iexit);

	}

	@Test
	public void testDoLoopEx() throws Exception {
		comp.parseString(": stack 0 do i loop ;");

		hostCtx.pushData(5);
		interpret("stack");
		assertEquals(4, hostCtx.popData());
		assertEquals(3, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(1, hostCtx.popData());
		assertEquals(0, hostCtx.popData());

	}
	
	@Test
	public void testQDoLoop() throws Exception {
		comp.parseString(": stack 0 ?do i loop ;");

		dumpDict();
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("stack");
		
		//	2dup	copy vals
		//	2>r		place init/lim on rstack
		//	=		compare init/lim
		//	0branch	if not equal, go into loop
		//	xx
		//	branch	go to leave
		//	yy
		//	i
		//	loop
		//	-zz
		//	<< leave point >>
		//	rdrop
		//	rdrop
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);

		assertOpword(word, IfieldLit, 0, Iext);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, Idup_d - _Iext, Iext, ItoR_d - _Iext);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, Ibinop, OP_SUB, I0branch);	// [if] 0branch to skip loop
		word = targCtx.readAddr(dp + 6);
		assertEquals(8 & 0xffff, word);	// past loop
		word = targCtx.readAddr(dp + 8);		// body
		assertOpword(word, Iext, Ii - _Iext , Iloop);
		word = targCtx.readAddr(dp + 10);
		assertEquals(0 & 0xffff, word);
		word = targCtx.readAddr(dp + 12);
		assertOpword(word,  Irdrop, Irdrop, Iexit);

	}

	@Test
	public void testDoQLoopEx() throws Exception {
		comp.parseString(": stack 3 ?do i loop ;");

		hostCtx.pushData(5);
		interpret("stack");
		assertEquals(4, hostCtx.popData());
		assertEquals(3, hostCtx.popData());

		hostCtx.pushData(-10);	// sentinel
		hostCtx.pushData(3);
		interpret("stack");
		assertEquals(-10, hostCtx.popData());

	}

	@Test
	public void testDoLoopLeaveEx() throws Exception {
		comp.parseString(": stack 10 3 do i 5 = if leave then i loop ;");

		interpret("stack");
		assertEquals(4, hostCtx.popData());
		assertEquals(3, hostCtx.popData());

	}

	@Test
	public void testDoPlusLoopEx() throws Exception {
		comp.parseString(": stack 0 do i 3 +loop ;");

		hostCtx.pushData(10);
		interpret("stack");
		assertEquals(9, hostCtx.popData());
		assertEquals(6, hostCtx.popData());
		assertEquals(3, hostCtx.popData());
		assertEquals(0, hostCtx.popData());

		hostCtx.pushData(9);
		interpret("stack");
		assertEquals(6, hostCtx.popData());
		assertEquals(3, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
	}

	@Test
	public void testDoPlusLoopEx2() throws Exception {
		comp.parseString(": stack 0 do i 16384 u+loop ;");

		hostCtx.pushData(0);
		interpret("stack");
		assertEquals((short)49152, hostCtx.popData());
		assertEquals((short)32768, hostCtx.popData());
		assertEquals(16384, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
	}

	@Test
	public void testDoPlusLoopEx3() throws Exception {
		comp.parseString(": stack 10 -10 do i 4 +loop ;");

		interpret("stack");
		assertEquals(6, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(-2, hostCtx.popData());
		assertEquals(-6, hostCtx.popData());
		assertEquals(-10, hostCtx.popData());
	}

	@Test
	public void testStackAccessorsEx() throws Exception {
		comp.parseString(
			": depth (context>) [ 1 field, ] (context>) [ 0 field, ] - 2/ 1- ;\n"+
			": rdepth (context>) [ 3 field, ] (context>) [ 2 field, ] - 2/ 1- ; \n" +	
			": stack 1 2 4 5 >r depth rdepth rdrop ;");

		interpret("stack");
		assertEquals(2, hostCtx.popData());
		assertEquals(3, hostCtx.popData());

	}
	@Test
	public void testLogOpsEx1() throws Exception {
		comp.parseString(
				": tst 1 2 < 1 2 >  1 -2 < 1 -2 >  6 6 >= -8 6 <= ;\n" +
				": utst 1 2 u< 1 2 u>  1 -2 u< 1 -2 u>  6 6 u>= -8 6 u<= ;"
				
		);
		
		interpret("tst");
		assertEquals(-1, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		
		interpret("utst");
		assertEquals(0, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		
	}
	@Test
	public void testLogOpsEx2() throws Exception {
		comp.parseString(
				": tst 1. 2. d< 1. 2. d>  1. -2. d< 1. -2. d>  6. 6. d>= -8. 6. d<= ;\n" +
				": utst 1. 2. du< 1. 2. du>  1. -2. du< 1. -2. du>  6. 6. du>= -8. 6. du<= ;"
				
		);
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("tst");
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);

		assertOpword(word, Iext, IfieldLit_d - _Iext, 1);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, Iext, IfieldLit_d - _Iext, 2);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, Iext, Icmp_d - _Iext, CMP_LT);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, Iext, IfieldLit_d - _Iext, 1);
		word = targCtx.readAddr(dp + 8);
		assertOpword(word, Iext, IfieldLit_d - _Iext, 2);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, Iext, Icmp_d - _Iext, CMP_GT);
		word = targCtx.readAddr(dp + 12);
		assertOpword(word, Iext, IfieldLit_d - _Iext, 1);
		word = targCtx.readAddr(dp + 14);
		assertOpword(word, Iext, IfieldLit_d - _Iext, -2);
		word = targCtx.readAddr(dp + 16);
		assertOpword(word, Iext, Icmp_d - _Iext, CMP_LT);
		
		interpret("tst");
		assertEquals(-1, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		
		interpret("utst");
		assertEquals(0, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		
	}

	@Test
	public void testMemOps1Ex() throws Exception {
		comp.parseString(
				"Variable x\n" +
				"Variable y\n" +
				": tst 55 y !  x +!  x @ y +! ;"
				
		);

		IWord x = targCtx.require("X"); 
		IWord y = targCtx.require("Y");
		
		dumpDict();
		
		int xaddr = ((ITargetWord)x).getEntry().getParamAddr();
		targCtx.writeCell(xaddr, 100);
		int yaddr = ((ITargetWord)y).getEntry().getParamAddr();
		targCtx.writeCell(yaddr, 200);
		
		hostCtx.pushData(-10);
		interpret("tst");
		
		dumpCompiledMemory();
		
		assertEquals(90, targCtx.readCell(xaddr));
		assertEquals(145, targCtx.readCell(yaddr));
	}

	@Test
	public void testMemOps2Ex() throws Exception {
		comp.parseString(
				"Create str\n" +
				"3 c, 65 c, 72 c, 90 c,\n" +
				": [] ( addr idx -- val addr ) over + c@ swap ; \n"+
				": tst str 0 [] 1 [] 2 [] 3 [] drop ;"
				
		);
		
		dumpDict();
		
		interpret("tst");
		
		assertEquals(90, hostCtx.popData());
		assertEquals(72, hostCtx.popData());
		assertEquals(65, hostCtx.popData());
		assertEquals(3, hostCtx.popData());
	}
	
	@Test
	public void testBeginWhileRepeat() throws Exception {
		comp.parseString(
				"create  TextModeRegs\n" + 
				"    $8000 , $81B0 , $8200 , $8400 , 0 , \n"+
				"create Copy 50 allot\n"+
				"variable copyidx\n"+
				": >copy Copy copyidx @ + ! 2 copyidx +! ;\n"+
				": readList\n"+
				" TextModeRegs\n"+
				"begin \n" + 
				"        dup @ dup\n" + 
				"    while\n" + 
				"        >copy \n" + 
				"        2+\n" + 
				"    repeat   \n" + 
				"    drop ;");

		dumpDict();
		
		interpret("readList");
		
		IWord copy = targCtx.require("Copy"); 
		IWord copyIdx = targCtx.require("CopyIdx"); 
		
		int copyIdxAddr = ((ITargetWord)copyIdx).getEntry().getParamAddr();
		assertEquals((short)8, targCtx.readCell(copyIdxAddr));
		
		int copyAddr = ((ITargetWord)copy).getEntry().getParamAddr();
		
		assertEquals((short)0x8400, targCtx.readCell(copyAddr + 6));
		assertEquals((short)0x8200, targCtx.readCell(copyAddr + 4));
		assertEquals((short)0x81b0, targCtx.readCell(copyAddr + 2));
		assertEquals((short)0x8000, targCtx.readCell(copyAddr));
	}
}
