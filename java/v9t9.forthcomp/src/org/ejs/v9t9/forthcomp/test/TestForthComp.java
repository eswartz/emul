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
		targCtx = new F99TargetContext(1024);
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
		
		comp.parseString("Variable t");
		IWord var = targCtx.find("T");
		assertNotNull(var);

		var.execute(hostCtx, targCtx);
		assertEquals(-1, hostCtx.popData());
		
		assertEquals(((ITargetWord)var).getEntry().getContentAddr(), targCtx.resolveAddr(-1));
		
		assertTrue(startDP > targCtx.readCell(startDP));
		assertEquals((byte)0x81, targCtx.readChar(startDP + 2));
		assertEquals('t', targCtx.readChar(startDP + 3));
		
	}
	@Test
	public void testVariable2() throws Exception {
		comp.parseString("Variable t Variable ud");
		
		ITargetWord tvar = (ITargetWord) targCtx.find("T");
		tvar.execute(hostCtx, targCtx);
		ITargetWord uvar = (ITargetWord) targCtx.find("Ud");
		uvar.execute(hostCtx, targCtx);
		
		assertEquals(-2, hostCtx.popData());
		assertEquals(-1, hostCtx.popData());
		
		assertEquals(tvar.getEntry().getContentAddr(), targCtx.resolveAddr(-1));
		assertEquals(uvar.getEntry().getContentAddr(), targCtx.resolveAddr(-2));
		
		assertEquals(0, tvar.getEntry().getAddr() & 1);
		assertEquals(0, tvar.getEntry().getContentAddr() & 1);
		assertEquals(0, uvar.getEntry().getAddr() & 1);
		assertEquals(0, uvar.getEntry().getContentAddr() & 1);
	}
	
	@Test
	public void testVariableLoadStore() throws Exception {
		targCtx.clearDict();
		comp.parseString("Variable t Variable u 123 t ! t @ u !  u @");
		
		IWord uvar = targCtx.find("U");
		assertEquals(123, targCtx.readCell(((ITargetWord)uvar).getEntry().getContentAddr()));
		
		assertEquals(123, hostCtx.popData());
	}
	@Test
	public void testColon1() throws Exception {

		ITargetWord semiS = (ITargetWord) targCtx.find(";S");
		assertNotNull(semiS);

		comp.parseString(": foo ;");
		TargetColonWord foo = (TargetColonWord) targCtx.find("foo");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertTrue(word+"", word == (InstF99.Iexit << 9));
		
		assertEquals(dp + targCtx.getCellSize(), targCtx.getDP());
		
		
	}
	@Test
	public void testColon2() throws Exception {
		ITargetWord semiS = (ITargetWord) targCtx.find(";S");
		assertNotNull(semiS);

		comp.parseString(": foo ; : b ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.find("foo");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertTrue(word+"", word == (InstF99.Iexit << 9));
		
		// b should be aligned after foo
		
		TargetColonWord bword = (TargetColonWord) targCtx.find("b");
		assertEquals(dp + targCtx.getCellSize(), bword.getEntry().getAddr());
		
		dp = bword.getEntry().getContentAddr();
		word = targCtx.readAddr(dp);
		assertTrue(word+"", word == (InstF99.Iexit << 9));
		
		assertEquals(dp + targCtx.getCellSize(), targCtx.getDP());
		
		
	}
	
	@Test
	public void testPrimPacking1() throws Exception {
		comp.parseString(": foo @ ! 0 dup ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.find("foo");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, InstF99.Ifetch, InstF99.Istore, InstF99.Izero);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, InstF99.Idup, 0, InstF99.Iexit);
		
		assertEquals(dp + 4, targCtx.getDP());
	}
	
	private void assertOpword(int word, int a, int b, int c) {
		if ( a < -32 || a >= 64)
			fail("bad test: " + a);
		if ( b < -4 || b >= 8)
			fail("bad test: " + b);
		if ( c < -32 || c >= 64)
			fail("bad test: " + c);
		int desired = ((a & 0x3f) << 9) | ((b & 0x7) << 6) | (c & 0x3f);
		if (word != desired) {
			int ra = (word >> 9) & 0x3f;
			int rb = (word >> 6) & 0x7;
			int rc = (word >> 0) & 0x3f;
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
		comp.parseString(": eq 31 3 = ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.find("eq");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, 0, InstF99.IfieldLit, 31);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, InstF99.IfieldLit, 3, InstF99.Iequ);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, InstF99.Iexit, 0, 0);
		
		assertEquals(dp + 6, targCtx.getDP());
	}

	@Test
	public void testLiterals2() throws Exception {
		comp.parseString(": eq 31 456 = ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.find("eq");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, 0, InstF99.IfieldLit, 31);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, InstF99.Ilit, 0, InstF99.Iequ);
		word = targCtx.readAddr(dp + 4);
		assertEquals(456, word);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, InstF99.Iexit, 0, 0);
		
		assertEquals(dp + 8, targCtx.getDP());
	}

	@Test
	public void testLiterals3() throws Exception {
		comp.parseString(": eq -3 5 3 ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.find("eq");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, InstF99.IfieldLit, -3, InstF99.IfieldLit);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, 5, InstF99.IfieldLit, 3);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, InstF99.Iexit, 0, 0);
		
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
	/**
	 * @param name
	 * @throws AbortException 
	 * @throws IOException 
	 */
	private void interpret(String name) throws AbortException {
		targCtx.exportState(hostCtx, f99Machine, BASE_SP, BASE_RP);

		dumpMemory(System.out, startDP, targCtx.getDP(), f99Machine.getConsole());
		
		ITargetWord word = (ITargetWord) targCtx.find(name);
		if (word == null)
			throw new AbortException("no such word: " + name);
		
		int pc = word.getEntry().getContentAddr();
		
		cpu.rpush((short) 0);
		cpu.setPC((short) pc);
		while (cpu.getPC() != 0)
			interp.execute();
		
		targCtx.importState(hostCtx, f99Machine, BASE_SP, BASE_RP);
	}
	
	@Test
	public void testLiterals4() throws Exception {
		comp.parseString(": eq 122 @ 456 ! 789 dup ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.find("eq");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, InstF99.Ilit, InstF99.Ifetch, InstF99.Ilit);
		word = targCtx.readAddr(dp + 2);
		assertEquals(122, word);
		word = targCtx.readAddr(dp + 4);
		assertEquals(456, word);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, InstF99.Istore, 0, InstF99.Ilit);
		word = targCtx.readAddr(dp + 8);
		assertEquals(789, word);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, InstF99.Idup, 0, InstF99.Iexit);
		
		assertEquals(dp + 12, targCtx.getDP());
	}
	
	@Test
	public void testLiterals4Ex() throws Exception {
		comp.parseString(": eq 1020 456 ! 789 dup ;");
		
		targCtx.writeCell(1020, 1000);
		
		interpret("eq");
		
		assertEquals(1020, targCtx.readCell(456));
		assertEquals(789, hostCtx.popData());
	}
	

	@Test
	public void testIfBranch0() throws Exception {
		comp.parseString(": true if -1 else 0 then ;");

		TargetColonWord foo = (TargetColonWord) targCtx.find("true");
		assertNotNull(foo);
		
		dumpDict();
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, InstF99.I0branch, 0, InstF99.InegOne);
		word = targCtx.readAddr(dp + 2);
		assertEquals(4, word);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, InstF99.Ibranch, 0, 0);	// must break (FOR NOW)
		word = targCtx.readAddr(dp + 6);
		assertEquals(2, word);
		word = targCtx.readAddr(dp + 8);
		assertOpword(word, InstF99.Izero, 0, 0);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, InstF99.Iexit, 0, 0);	// must break (FOR NOW)
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
		
		TargetColonWord foo = (TargetColonWord) targCtx.find("sgn");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);

		assertOpword(word, InstF99.Idup, 0, InstF99.I0lt);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, InstF99.I0branch, 0, InstF99.Idrop);
		word = targCtx.readAddr(dp + 4);
		assertEquals(4, word);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, InstF99.InegOne, 0, InstF99.Ibranch);
		word = targCtx.readAddr(dp + 8);
		assertEquals(10, word);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, InstF99.I0equ, InstF99.I0branch, InstF99.Izero);
		word = targCtx.readAddr(dp + 12);
		assertEquals(4, word);
		word = targCtx.readAddr(dp + 14);
		assertOpword(word, InstF99.Ibranch, 0, 0);
		word = targCtx.readAddr(dp + 16);
		assertEquals(2, word);
		word = targCtx.readAddr(dp + 18);
		assertOpword(word, InstF99.Ione, 0, 0);
		word = targCtx.readAddr(dp + 20);
		assertOpword(word, InstF99.Iexit, 0, 0);

		/*
		assertOpword(word, InstF99.Idup, InstF99.I0lt, InstF99.I0fieldBranch);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, 8, 0, InstF99.IfieldBranch);
		word = targCtx.readAddr(dp + 4);
		assertOpword(word, 12, InstF99.Idrop, InstF99.InegOne);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, InstF99.IfieldBranch, 4, InstF99.I0equ);
		word = targCtx.readAddr(dp + 8);
		assertOpword(word, InstF99.I0fieldBranch, 4, InstF99.Izero);
		word = targCtx.readAddr(dp + 10);
		assertOpword(word, InstF99.IfieldBranch, 1, InstF99.Ione);
		word = targCtx.readAddr(dp + 12);
		assertOpword(word, InstF99.Iexit, 0, 0);
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
		
		TargetColonWord sub = (TargetColonWord) targCtx.find("sub");
		assertNotNull(sub);
		
		TargetColonWord outer = (TargetColonWord) targCtx.find("outer");
		assertNotNull(outer);
		
		int dp = outer.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);
		assertOpword(word, InstF99.Ilit, 0, 0);
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
		comp.parseString(": * um* drop ;\n" +
				//": */ ( n1 n2 n3 -- n4 ) >r um* r> um/mod  swap drop ;\n" +
				": */mod ( n1 n2 n3 -- rem quot ) >r um* r> um/mod ;\n" +
				": */ ( n1 n2 n3 -- n4 ) */mod swap drop ;\n" +
				": percent ( val p -- prod ) 100 */ ;\n" +
				": outer 500 25 percent ;");
		
		interpret("outer");
		assertEquals(125, hostCtx.popData());
	}
	

	@Test
	public void testDoLoop() throws Exception {
		comp.parseString(": stack 0 do i loop ;");

		dumpDict();
		
		TargetColonWord foo = (TargetColonWord) targCtx.find("stack");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		int word = targCtx.readAddr(dp);

		assertOpword(word, InstF99.Izero, 0, InstF99.Ido);
		word = targCtx.readAddr(dp + 2);
		assertOpword(word, InstF99.IatR, 0, InstF99.Iloop);
		word = targCtx.readAddr(dp + 4);
		assertEquals(-4 & 0xffff, word);
		word = targCtx.readAddr(dp + 6);
		assertOpword(word, InstF99.Iunloop, 0, InstF99.Iexit);

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
	
}
