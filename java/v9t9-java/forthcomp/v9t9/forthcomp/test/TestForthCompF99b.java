/**
 * 
 */
package v9t9.forthcomp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static v9t9.engine.cpu.InstF99b.I0branchX;
import static v9t9.engine.cpu.InstF99b.I0equ;
import static v9t9.engine.cpu.InstF99b.IatR;
import static v9t9.engine.cpu.InstF99b.IbranchB;
import static v9t9.engine.cpu.InstF99b.IbranchX;
import static v9t9.engine.cpu.InstF99b.Icmp;
import static v9t9.engine.cpu.InstF99b.Idrop;
import static v9t9.engine.cpu.InstF99b.Idup;
import static v9t9.engine.cpu.InstF99b.Iequ;
import static v9t9.engine.cpu.InstF99b.Iexit;
import static v9t9.engine.cpu.InstF99b.IlitB;
import static v9t9.engine.cpu.InstF99b.IlitB_d;
import static v9t9.engine.cpu.InstF99b.IlitD_d;
import static v9t9.engine.cpu.InstF99b.IlitW;
import static v9t9.engine.cpu.InstF99b.IlitX;
import static v9t9.engine.cpu.InstF99b.Iload;
import static v9t9.engine.cpu.InstF99b.IloopUp;
import static v9t9.engine.cpu.InstF99b.Irdrop_d;
import static v9t9.engine.cpu.InstF99b.Istore;
import static v9t9.engine.cpu.InstF99b.Isub;
import static v9t9.engine.cpu.InstF99b.ItoR_d;

import org.ejs.coffee.core.utils.Pair;
import org.junit.Test;

import v9t9.engine.cpu.InstF99b;
import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.ITargetWord;
import v9t9.forthcomp.IWord;
import v9t9.forthcomp.words.TargetColonWord;

public class TestForthCompF99b extends BaseF99bTest {
	@Test
	public void testLiteral() throws Exception {
		parseString("123");
		assertEquals(123, hostCtx.popData());
	}
	@Test
	public void testLiteral2() throws Exception {
		parseString("123 456");
		assertEquals(456, hostCtx.popData());
		assertEquals(123, hostCtx.popData());
	}
	
	@Test
	public void testVariable1() throws Exception {
		targCtx.clearDict();
		
		startDP = targCtx.getDP();
		
		parseString("Variable t");
		dumpDict();
		ITargetWord var = (ITargetWord) targCtx.require("T");

		var.getExecutionSemantics().execute(hostCtx, targCtx);
		assertEquals(var.getEntry().getParamAddr(), hostCtx.popData());
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
		
		parseString("Variable t Variable ud");
		
		ITargetWord tvar = (ITargetWord) targCtx.require("T");
		tvar.getExecutionSemantics().execute(hostCtx, targCtx);
		ITargetWord uvar = (ITargetWord) targCtx.require("Ud");
		uvar.getExecutionSemantics().execute(hostCtx, targCtx);
		
		int ud = hostCtx.popData();
		int t = hostCtx.popData();
		assertEquals(tvar.getEntry().getParamAddr(), t);
		assertEquals(uvar.getEntry().getParamAddr(), ud);
		
		assertEquals(0, tvar.getEntry().getAddr() & 1);
		assertEquals(0, tvar.getEntry().getContentAddr() & 1);
		assertEquals(0, uvar.getEntry().getAddr() & 1);
		assertEquals(0, uvar.getEntry().getContentAddr() & 1);
	}
	
	@Test
	public void testCompileTimeVariableLoadStore() throws Exception {
		targCtx.clearDict();
		parseString("Variable t Variable u 123 t ! t @ u !  u @");
		
		IWord uvar = targCtx.require("U");
		assertEquals(123, targCtx.readCell(((ITargetWord)uvar).getEntry().getParamAddr()));
		
		assertEquals(123, hostCtx.popData());
	}
	@Test
	public void testColon1() throws Exception {

		ITargetWord semiS = (ITargetWord) targCtx.require(";S");
		assertNotNull(semiS);

		parseString(": foo ;");
		TargetColonWord foo = (TargetColonWord) targCtx.require("foo");
		assertNotNull(foo);
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, Iexit);
		
		assertEquals(dp + 1, targCtx.getDP());
		
		
	}
	@Test
	public void testColon2() throws Exception {
		parseString(": foo ; : b ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("foo");
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, Iexit);
		
		// b should be aligned after foo
		
		TargetColonWord bword = (TargetColonWord) targCtx.require("b");
		assertEquals(dp + targCtx.getCellSize(), bword.getEntry().getAddr());
		
		dp = bword.getEntry().getContentAddr();
		assertOpcodes(dp, Iexit);
		
		assertEquals(dp + 1, targCtx.getDP());
		
		
	}
	
	protected void assertOpcodes(int dp, int... opcodes) {
		int start = dp;
		for (int i = 0; i < opcodes.length; i++) {
			if (opcodes[i] < 256) {
				assertEquals((dp-start)+":"+(i)+"", opcodes[i], targCtx.readChar(dp) & 0xff);
				dp++;
			}
			else {
				assertEquals((dp-start)+":"+(i)+"", (opcodes[i] >> 8) & 0xff, targCtx.readChar(dp) & 0xff);
				assertEquals((dp-start)+":"+(i+1)+"", (opcodes[i] & 0xff), targCtx.readChar(dp+1) & 0xff);
				dp += 2;
			}
		}
	}
	@Test
	public void testPrimPacking1() throws Exception {
		parseString(": foo @ ! 0 dup ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("foo");
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, Iload, Istore, IlitX, Idup, Iexit);
		
		assertEquals(dp + 5, targCtx.getDP());
	}
	
	@Test
	public void testLiterals1() throws Exception {
		parseString(": eq 7 3 - 0= ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, IlitX | 7, IlitX | 3, Isub, I0equ, Iexit);
		
		assertEquals(dp + 5, targCtx.getDP());
	}

	@Test
	public void testLiterals2() throws Exception {
		parseString(": eq 15 456 = ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, IlitB, 15, IlitW, 456, Iequ, Iexit);
		
		assertEquals(dp + 7, targCtx.getDP());
	}

	@Test
	public void testLiterals3() throws Exception {
		parseString(": eq -3 5 4 ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, IlitX | (-3& 0xf), IlitX | 5, IlitX | 4, Iexit);
		
		assertEquals(dp + 4, targCtx.getDP());
	}
	

	@Test
	public void testLiterals3Ex() throws Exception {
		parseString(": eq -3 5 3 ;");

		interpret("eq");
		
		assertEquals(3, hostCtx.popData());
		assertEquals(5, hostCtx.popData());
		assertEquals(-3, hostCtx.popData());
	}
	

	@Test
	public void testLiterals6() throws Exception {
		parseString(": eq 11. $ffff.aaaa ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, IlitB_d, 11, IlitD_d, 0xaaaa, 0xffff, Iexit); 
	}
	

	@Test
	public void testLiterals7Ex() throws Exception {
		parseString("14      constant    INT_NMI  : foo ;  : eq 0 ['] foo  INT_NMI foo ;");

		interpret("eq");
		
		assertEquals(14, hostCtx.popData());
	}

	@Test
	public void testLiterals4() throws Exception {
		parseString(": eq 122 @ 456 ! 789 dup ;");
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("eq");
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, IlitB, 122, Iload, IlitW, 456, Istore, IlitW, 789, Idup, Iexit);
		
		assertEquals(dp + 12, targCtx.getDP());
	}
	
	@Test
	public void testLiterals4Ex() throws Exception {
		parseString(": eq 1020 1456 ! 789 dup ;");
		
		targCtx.writeCell(1456, 1000);
		
		interpret("eq");
		
		assertEquals(1020, targCtx.readCell(1456));
		assertEquals(789, hostCtx.popData());
	}

	@Test
	public void testLiterals5Ex() throws Exception {
		parseString(": num $1234.5678 ;");
		
		interpret("num");
		
		assertEquals(0x1234, hostCtx.popData());
		assertEquals(0x5678, hostCtx.popData());
	}

	@Test
	public void testIfBranch0() throws Exception {
		parseString(": true if -1 else 0 then ;");

		TargetColonWord foo = (TargetColonWord) targCtx.require("true");
		
		dumpDict();
		
		int dp = foo.getEntry().getContentAddr();
		assertOpcodes(dp, I0branchX | 4, 0, IlitX | (-1&0xf), IbranchX | 2, 0, IlitX, Iexit);
	}

	@Test
	public void testIfBranch0Ex() throws Exception {
		parseString(": true if -1 else 0 then ;");

		hostCtx.pushData(5);
		interpret("true");
		assertEquals(-1, hostCtx.popData());
		
		hostCtx.pushData(0);
		interpret("true");
		assertEquals(0, hostCtx.popData());
	}
	
	@Test
	public void testIfBranch() throws Exception {
		parseString(": sgn dup 0< if drop -1 else 0= if 0 else 1 then then ;");

		dumpDict();
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("sgn");
		
		int dp = foo.getEntry().getContentAddr();

		assertOpcodes(dp, Idup, IlitX, Icmp+InstF99b.CMP_LT, I0branchX | 5, 0, 
				Idrop, IlitX|(-1&0xf), IbranchB, 7,
				I0equ, I0branchX | 4, 0, IlitX, IbranchX | 2, 0, IlitX | 1,
				Iexit);
	}

	@Test
	public void testIfBranchEx() throws Exception {
		parseString(": sgn dup 0< if drop -1 else 0= if 0 else 1 then then ;");

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
		parseString(": sub negate 10 + ; : outer 100 sub -50 sub + ;");

		dumpDict();
		
		TargetColonWord sub = (TargetColonWord) targCtx.require("sub");
		
		TargetColonWord outer = (TargetColonWord) targCtx.require("outer");
		
		int dp = outer.getEntry().getContentAddr();
		assertOpcodes(dp, IlitB, 100);
		assertEquals(sub.getEntry().getContentAddr(), targCtx.findReloc(dp + 2));
	}
	

	@Test
	public void testColonCallEx() throws Exception {
		parseString(": sub negate 10 + ; : outer 100 sub -50 sub + ;");
		
		interpret("outer");
		assertEquals(-30, hostCtx.popData());
	}
	

	@Test
	public void testMultiplyDivide() throws Exception {
		parseString(
				//": */ ( n1 n2 n3 -- n4 ) >r um* r> um/mod  swap drop ;\n" +
				": */mod ( n1 n2 n3 -- rem quot ) >r um* r> um/mod ;\n" +
				": */ ( n1 n2 n3 -- n4 ) */mod swap drop ;\n" +
				": percent ( val p -- prod ) 100 */ ;\n" +
				": outer 500 25 percent ;");
		
		dumpDict();
		interpret("outer");
		assertEquals(125, hostCtx.popData());
	}
	

	@Test
	public void testDoubleMath1Ex() throws Exception {
		parseString(
				": outer $8888 $ffff um* $8887.7778 d= ;");
		
		interpret("outer");
		assertEquals(-1, hostCtx.popData());
	}
	

	@Test
	public void testDoubleMath2Ex() throws Exception {
		parseString(
				": outer $8887.7778 2>r  $8887 r> = $7778 r> = + ;");
		
		interpret("outer");
		assertEquals(-2, hostCtx.popData());
	}
	@Test
	public void testShifts1Ex() throws Exception {
		parseString(
				": outer $8887 12 urshift  $8887 8 rshift $ffff 15 lshift ;");
		
		interpret("outer");
		assertEquals((short)0x8000, hostCtx.popData());
		assertEquals((short)0xff88, hostCtx.popData());
		assertEquals((short)0x0008, hostCtx.popData());
	}
	@Test
	public void testShifts2Ex() throws Exception {
		parseString(
				": outer $8887 12U urshift  $8887 8U rshift $ffff 15U lshift ;");
		
		interpret("outer");
		assertEquals((short)0x8000, hostCtx.popData());
		assertEquals((short)0xff88, hostCtx.popData());
		assertEquals((short)0x0008, hostCtx.popData());
	}
	@Test
	public void testDoublShifts1Ex() throws Exception {
		parseString(
				": outer $8887.7778 16. durshift  $8887.7778 8. drshift ;");
		
		interpret("outer");
		assertEquals((short)0xff88, hostCtx.popData());
		assertEquals((short)0x8777, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals((short)0x8887, hostCtx.popData());
	}
	@Test
	public void testDoublShifts2Ex() throws Exception {
		parseString(
				": outer $8887.7778 16.U durshift  $8887.7778 8.U drshift ;");
		
		interpret("outer");
		assertEquals((short)0xff88, hostCtx.popData());
		assertEquals((short)0x8777, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals((short)0x8887, hostCtx.popData());
	}
	@Test
	public void testDoLoop() throws Exception {
		parseString(": stack 0 do i loop ;");

		dumpDict();
		
		TargetColonWord foo = (TargetColonWord) targCtx.require("stack");
		
		int dp = foo.getEntry().getContentAddr();

		// back branches should just from the start of the inst
		assertOpcodes(dp, IlitX, ItoR_d, IatR, IloopUp, I0branchX | 0xe, Irdrop_d, Iexit); 
	}

	@Test
	public void testDoLoopEx() throws Exception {
		parseString(": stack 0 do i loop ;");

		hostCtx.pushData(5);
		interpret("stack");
		assertEquals(4, hostCtx.popData());
		assertEquals(3, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(1, hostCtx.popData());
		assertEquals(0, hostCtx.popData());

	}
	
	/*
	@Test
	public void testQDoLoop() throws Exception {
		parseString(": stack 0 ?do i loop ;");

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
		assertOpword(word, Iext, IatR - _Iext , Iloop);
		word = targCtx.readAddr(dp + 10);
		assertEquals(0 & 0xffff, word);
		word = targCtx.readAddr(dp + 12);
		assertOpword(word,  Irdrop, Irdrop, Iexit);

	}*/

	@Test
	public void testDoQLoopEx() throws Exception {
		parseString(": stack 3 ?do i loop ;");

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
		parseString(": stack 10 3 do i 5 = if leave then i loop ;");

		interpret("stack");
		assertEquals(4, hostCtx.popData());
		assertEquals(3, hostCtx.popData());

	}

	@Test
	public void testDoPlusLoopEx() throws Exception {
		parseString(": stack 0 do i 3 +loop ;");

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
		parseString(": stack 0 do i 16384 u+loop ;");

		hostCtx.pushData(0);
		interpret("stack");
		assertEquals((short)49152, hostCtx.popData());
		assertEquals((short)32768, hostCtx.popData());
		assertEquals(16384, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
	}

	@Test
	public void testDoPlusLoopEx3() throws Exception {
		parseString(": stack 10 -10 do i 4 +loop ;");

		interpret("stack");
		assertEquals(6, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(-2, hostCtx.popData());
		assertEquals(-6, hostCtx.popData());
		assertEquals(-10, hostCtx.popData());
	}

	@Test
	public void testStackAccessorsEx() throws Exception {
		parseString(
			": depth (context>) [ 1 field, ] (context>) [ 0 field, ] - 2/ 1- ;\n"+
			": rdepth (context>) [ 3 field, ] (context>) [ 2 field, ] - 2/ 1- ; \n" +	
			": stack 1 2 4 5 >r depth rdepth rdrop ;");

		interpret("stack");
		assertEquals(2, hostCtx.popData());
		assertEquals(3, hostCtx.popData());

	}
	@Test
	public void testLogOpsEx1() throws Exception {
		parseString(
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
		parseString(
				": tst 1. 2. d< 1. 2. d>  1. -2. d< 1. -2. d>  6. 6. d>= -8. 6. d<= ;\n" +
				": utst 1. 2. du< 1. 2. du>  1. -2. du< 1. -2. du>  6. 6. du>= -8. 6. du<= ;"
				
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
	public void testMemOps1Ex() throws Exception {
		parseString(
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
		parseString(
				"Create str\n" +
				"3 c, 65 c, 172 c, 90 c,\n" +
				": [] ( addr idx -- val addr ) over + c@ swap ; \n"+
				": tst str 0 [] 1 [] 2 [] 3 [] drop ;"
				
		);
		
		dumpDict();
		
		interpret("tst");
		
		assertEquals(90, hostCtx.popData());
		assertEquals(172, hostCtx.popData());
		assertEquals(65, hostCtx.popData());
		assertEquals(3, hostCtx.popData());
	}

	@Test
	public void testMemOps3Ex() throws Exception {
		parseString(
				"DVariable counter\n" +
				": tst 123. counter D!  1000. counter D+!  counter d@ ;"
				
		);
		
		dumpDict();
		
		interpret("tst");
		
		assertEquals(0, hostCtx.popData());
		assertEquals(1123, hostCtx.popData());
	}
	@Test
	public void testMemOps4Ex() throws Exception {
		parseString(
					//		 	low     high
				"Create counter $1234 , $5678 , \n" +
				": tst counter D@ ;"
				
		);
		
		dumpDict();
		
		interpret("tst");
		
		assertEquals(0x5678, hostCtx.popData());
		assertEquals(0x1234, hostCtx.popData());
	}
	@Test
	public void testBeginAgain() throws Exception {
		parseString(
				"variable idx\n"+
				": loopit\n"+
				"begin \n" +
				"  1 idx +! "+
				"  idx @ 23 = if exit then\n"+
				"again ;");

		dumpDict();
		
		interpret("loopit");
		
		IWord idx = targCtx.require("Idx"); 
		
		int idxAddr = ((ITargetWord)idx).getEntry().getParamAddr();
		assertEquals((short)23, targCtx.readCell(idxAddr));
	}
	

	@Test
	public void testBeginUntil() throws Exception {
		parseString(
				"variable idx\n"+
				": loopit\n"+
				"begin \n" +
				"  1 idx +! "+
				"  idx @ 23 = \n"+
				"until ;");

		dumpDict();
		
		interpret("loopit");
		
		IWord idx = targCtx.require("Idx"); 
		
		int idxAddr = ((ITargetWord)idx).getEntry().getParamAddr();
		assertEquals((short)23, targCtx.readCell(idxAddr));
	}
	@Test
	public void testBeginWhileRepeat() throws Exception {
		parseString(
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
	
	@Test
	public void testConstants() throws Exception {
		parseString(
				"12 constant Speed\n" +
				"50 constant Distance\n"+
				": foo Speed Distance * ;\n");
		
		dumpDict();
		
		interpret("foo");
		
		assertEquals(50*12, hostCtx.popData());
	}

	@Test
	public void testStacks() throws Exception {
		parseString(
				": foo rot ;\n");
		
		dumpDict();
		
		hostCtx.pushData(1); hostCtx.pushData(2); hostCtx.pushData(3);
		interpret("foo");
		assertEquals(1, hostCtx.popData()); assertEquals(3, hostCtx.popData());	assertEquals(2, hostCtx.popData());
	}
	
	@Test
	public void testForwards() throws Exception {
		parseString(
				": vfill 1 2 3 ; \n"+
				": foo cls cls cls ;\n" +
				": cls vfill drop drop drop ;\n");

		dumpDict();

		TargetColonWord foo = (TargetColonWord) targCtx.require("foo");
		
		int dp = foo.getEntry().getContentAddr();
		int word;
		word = targCtx.readAddr(dp);
		assertCall("cls", word);
		word = targCtx.readAddr(dp + 2);
		assertCall("cls", word);
		word = targCtx.readAddr(dp + 4);
		assertCall("cls", word);
		assertOpcodes(dp + 6, Iexit);
	}

	@Test
	public void testRedef() throws Exception {
		parseString(
				": vfill ; \n"+
				": foo cls ;\n" +
				": cls vfill ;\n");
		
		ITargetWord origCls = (ITargetWord) targCtx.require("cls");
		ITargetWord origVfill = (ITargetWord) targCtx.require("vfill");
		
		parseString(
				": vfill ; \n"+
				": foo2 vfill ;\n"+
				": cls drop ;\n"+
				": lala cls ;\n"+
				"");

		dumpDict();

		TargetColonWord foo = (TargetColonWord) targCtx.require("foo");
		
		int dp = foo.getEntry().getContentAddr();
		int word;
		word = targCtx.readAddr(dp);
		assertCall(origCls, word);
		
		dp = origCls.getEntry().getContentAddr();
		word = targCtx.readAddr(dp);
		assertCall(origVfill, word);
		
		dp = ((ITargetWord) targCtx.require("foo2")).getEntry().getContentAddr();
		
		word = targCtx.readAddr(dp );
		assertCall("vfill", word);
		
		dp = ((ITargetWord) targCtx.require("lala")).getEntry().getContentAddr();
		
		word = targCtx.readAddr(dp);
		assertCall("cls", word);
	}


	@Test
	public void testWriteProgram() throws Exception {
		parseString(
				": num [ 123 (literal) ] ;\n" +	// forces 16-bit
				": foo num  456 ['] num 1+ ! num ;\n");
		
		dumpDict();
		
		interpret("foo");
		
		assertEquals(456, hostCtx.popData());
		assertEquals(123, hostCtx.popData());
	}

	@Test
	public void testWriteProgram2() throws Exception {
		parseString(
				": num [ 123. (DLITERAL) ] ;\n" +
				": foo num  456 ['] num 2+ 2+ ! num ;\n");
		
		dumpDict();
		
		interpret("foo");
		
		assertEquals(456, hostCtx.popData());
		assertEquals(123, hostCtx.popData());
		assertEquals(0, hostCtx.popData());
		assertEquals(123, hostCtx.popData());
	}
	
	@Test
	public void testUserVars() throws Exception {
		parseString(
				"User a\n"+
				"User b\n"+
				": foo 10  a !  5 b !  a @ b @ ;"
		);

		dumpDict();
		
		interpret("foo");
		
		assertEquals(5, hostCtx.popData());
		assertEquals(10, hostCtx.popData());
		
	}

	@Test
	public void testLocals1() throws Exception {
		parseString(
				":: rev4 ( a b c d -- d c b a x )\n"+
				"  d c b a " +
				" a b c d + + + " + 
				";"
		);

		hostCtx.pushData(1);
		hostCtx.pushData(2);
		hostCtx.pushData(3);
		hostCtx.pushData(4);
		
		dumpDict();
		
		interpret("rev4");
		
		assertEquals(10, hostCtx.popData());
		assertEquals(1, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(3, hostCtx.popData());
		assertEquals(4, hostCtx.popData());
		
	}
	@Test
	public void testLocals2() throws Exception {
		
		parseString(
				":: strcmp ( addr1 c1 addr2 c2 -- f )\n"+
				"begin\n"+
				"  c1 0= c2 0= and not \n"+
				"while\n"+
				"  addr1  dup 1+ to addr1  c1 1- to c1  c@\n"+
				"  addr2  dup 1+ to addr2  c2 1- to c2  c@\n"+
				"  - dup if  exit  else  drop  then\n"+
				"repeat\n"+
				"c1 c2 - \\ length dictates winner\n" +
				";");
		
		doStrCmpTest();
	}
	@Test
	public void testLocals3() throws Exception {
		
		parseString(
				":: strcmp ( addr1 c1 addr2 c2 -- f )\n"+
				"begin\n"+
				"  c1 0= c2 0= and not \n"+
				"while\n"+
				"  'addr1  dup @  1 rot +!  -1 'c1 +!  c@\n"+
				"  'addr2  dup @  1 rot +!  -1 'c2 +!  c@\n"+
				"  - dup if  exit  else  drop  then\n"+
				"repeat\n"+
				"c1 c2 - \\ length dictates winner\n" +
				";");
		
		doStrCmpTest();
	}
	private void doStrCmpTest() throws AbortException {
		Pair<Integer, Integer> str1;
		Pair<Integer, Integer> str2;
		
		int cycles1 = cpu.getCurrentCycleCount();
		
		str1 = targCtx.writeLengthPrefixedString("This is first");
		targCtx.alloc(str1.second);
		str2 = targCtx.writeLengthPrefixedString("This is second");
		targCtx.alloc(str2.second);
		
		dumpDict();
		
		hostCtx.pushData(str1.first + 1);
		hostCtx.pushData(targCtx.readChar(str1.first));
		hostCtx.pushData(str2.first + 1);
		hostCtx.pushData(targCtx.readChar(str2.first));
		
		interpret("strcmp");
		
		int ret;
		ret = hostCtx.popData();
		assertEquals(ret+"",  ('f' - 's'), ret);
	

		str1 = targCtx.writeLengthPrefixedString("Yet, bigger.");
		targCtx.alloc(str1.second);
		str2 = targCtx.writeLengthPrefixedString("And smaller.");
		targCtx.alloc(str2.second);
		
		dumpDict();
		
		hostCtx.pushData(str1.first + 1);
		hostCtx.pushData(targCtx.readChar(str1.first));
		hostCtx.pushData(str2.first + 1);
		hostCtx.pushData(targCtx.readChar(str2.first));
		
		interpret("strcmp");
		
		ret = hostCtx.popData();
		assertEquals(ret+"", ('Y' - 'A'), ret);
		
		str1 = targCtx.writeLengthPrefixedString("Another plain old copy?");
		targCtx.alloc(str1.second);
		str2 = targCtx.writeLengthPrefixedString("Another plain old copy?");
		targCtx.alloc(str2.second);

		dumpDict();
		
		hostCtx.pushData(str1.first + 1);
		hostCtx.pushData(targCtx.readChar(str1.first));
		hostCtx.pushData(str2.first + 1);
		hostCtx.pushData(targCtx.readChar(str2.first));
		
		interpret("strcmp");
		
		ret = hostCtx.popData();
		assertTrue(ret+"", ret == 0);
		
		int cycles2 = cpu.getCurrentCycleCount();
		
		System.out.println("cycles: " + (cycles2 - cycles1));
	}
	
	@Test
	public void testLocalNesting() throws Exception {
		
		parseString(
				": trash 111 222 333 >r >r >r rdrop rdrop rdrop 444 ;\n" +
				":: addsub ( a b ) a b + trash drop a b - ; \n"+
				":: silly ( cnt bias )\n"+
				"cnt 0 do\n"+
				"  i bias addsub \n" +
				"loop\n"+
				";");
		
		
		hostCtx.pushData(3);
		hostCtx.pushData(10);
		
		interpret("silly");
		
		// 10, -10; 11, -9; 12, -8
		assertEquals(-8, hostCtx.popData());
		assertEquals(12, hostCtx.popData());
		assertEquals(-9, hostCtx.popData());
		assertEquals(11, hostCtx.popData());
		assertEquals(-10, hostCtx.popData());
		assertEquals(10, hostCtx.popData());
	}
	
	@Test
	public void testValues() throws Exception {

		parseString(
				"88 Value grade\n"+
				"7 to grade\n"+
				": fool\n"+
				" grade 90 + to grade ;\n");
		
		interpret("fool");

		ITargetWord var = (ITargetWord) targCtx.require("grade");
		assertEquals(97, targCtx.readCell(var.getEntry().getParamAddr())); 
	}
	
	@Test
	public void testCompiledLit() throws Exception {

		parseString(
				"Create buffer 100 allot\n"+
				": fool [ buffer 50 + LITERAL ] ;\n");
		
		interpret("fool");

		ITargetWord var = (ITargetWord) targCtx.require("buffer");
		assertEquals(var.getEntry().getParamAddr() + 50, hostCtx.popData()); 
	}
	@Test
	public void testNoExportValuesAndVars() throws Exception {
		
		parseString(
				"0 <export\n"+
				"Variable vy\n" + 
				"\n" + 
				"10 Value win-x\n" + 
		": fool 1 vy !  win-x ;\n");
		
		interpret("fool");
		
		assertEquals(10, hostCtx.popData()); 
	}
	
	@Test
	public void testDictCompileLiteral() throws Exception {
		
		hostCtx.pushData(123);
		parseString(
				stockDictDefs +
				": mklit ( n -- ) dup -8 >= over 8 < and  if\n" +
				" 	$f and $20 or c,  else\n" +
				"dup -128 >= over 128 < and  if\n" +
				" 	$78 c, c,\n" +
				"else\n" +
				"	$79 c, ,\n" +
				"then then\n"+
				"; \n"
				);
		
		
		int dp = targCtx.getDP();
		
		hostCtx.pushData(10);
		interpret("mklit");	
		hostCtx.pushData(-3);
		interpret("mklit");
		hostCtx.pushData(0x1234);
		interpret("mklit");

		dumpDict();
		
		assertEquals(0x78, targCtx.readChar(dp++));
		assertEquals(10, targCtx.readChar(dp++));
		assertEquals(0x2D, targCtx.readChar(dp++));
		assertEquals(0x79, targCtx.readChar(dp++));
		assertEquals(0x12, targCtx.readChar(dp++));
		assertEquals(0x34, targCtx.readChar(dp++));
	}
	
	@Test
	public void testBracketIfEtc1() throws Exception {
		hostCtx.pushData(123);
		parseString(
				"[if] 1 2 3\n"+
				"[then]"
				);
		assertEquals(3, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(1, hostCtx.popData());
		assertEquals(0, hostCtx.getDataStack().size());
		
		hostCtx.pushData(123);
		parseString(
				"0= [if] 1 2 3\n"+
				"[then]"
				);
		assertEquals(0, hostCtx.getDataStack().size());
		

		hostCtx.pushData(123);
		parseString(
				"[if] 1 2 3 [else] 4 5 6\n"+
				"[then]"
				);
		assertEquals(3, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(1, hostCtx.popData());
		assertEquals(0, hostCtx.getDataStack().size());
		

		hostCtx.pushData(0);
		parseString(
				"[if] 1 2 3 [else] 4 5 6\n"+
				"[then]"
				);
		assertEquals(6, hostCtx.popData());
		assertEquals(5, hostCtx.popData());
		assertEquals(4, hostCtx.popData());
		assertEquals(0, hostCtx.getDataStack().size());
	}
	
	@Test
	public void testBracketIfdefEtc1() throws Exception {
		
		hostCtx.pushData(123);
		parseString(
				"[ifundef] 0\n"+
				": 0 [ $20 c, ] ; target-only\n"+
				"[then]\n" +
				"[ifundef] 0\n"+
				": 0 bogus \n"+
				"[then]" 
				);
		
		assertEquals(123, hostCtx.popData());

		dumpDict();

		ITargetWord var = (ITargetWord) targCtx.require("0");
		int dp = var.getEntry().getContentAddr();		
		assertEquals(IlitX, targCtx.readChar(dp++));
		assertEquals(Iexit, targCtx.readChar(dp++));
	}
	

	@Test
	public void testBracketIfEtc2() throws Exception {
		hostCtx.pushData(123);
		parseString(
				"[if] 1 2 3  3 = [if] 10 + [then]\n"+
				"[then]"
				);
		assertEquals(12, hostCtx.popData());
		assertEquals(1, hostCtx.popData());
		assertEquals(0, hostCtx.getDataStack().size());
		
		hostCtx.pushData(123);
		hostCtx.pushData(0);
		parseString(
				"[if] 1 2 3  3 = " +
				"	[if] 10 + [else] 1999 [then] " +
				"[else] -23 + [then]"
				);
		assertEquals(100, hostCtx.popData());
		assertEquals(0, hostCtx.getDataStack().size());
		
		
	}

	@Test
	public void testBracketIfEtc3() throws Exception {
		hostCtx.pushData(123);
		hostCtx.pushData(1);
		parseString(
				"[if] 1 2 3  6 = " +
					"[if] 10 + [else] 1999 [then] " +
				"[else] -23 + [then]"
				);
		assertEquals(1999, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(1, hostCtx.popData());
		assertEquals(123, hostCtx.popData());
		assertEquals(0, hostCtx.getDataStack().size());
		
	}
	@Test
	public void testPick() throws Exception {
		parseString(
				"0           constant    CTX_SP     \n"+
				": pick ( n -- v )\n" + 
				"    1+ cells (context>) [ CTX_SP field, ] + @  \n" + 
				"; target-only\n"+
				"1 2 3 4   3 \n");
		interpret("pick");
		
		assertEquals(1, hostCtx.popData());
		
		assertEquals(4, hostCtx.popData());
		assertEquals(3, hostCtx.popData());
		assertEquals(2, hostCtx.popData());
		assertEquals(1, hostCtx.popData());
	}
	@Test
	public void testMaxMin() throws Exception {
		parseString(
				": NIP    ( a b -- b )\n" + 
				"    swap drop\n" + 
				";\n" + 
				"\n" + 
				": MAX\n" + 
				"    2dup >= if drop else nip then\n" + 
				";\n" + 
				": MIN\n" + 
				"    2dup <= if drop else nip then\n" + 
				";\n");
		hostCtx.pushData(100);
		hostCtx.pushData(200);
		
		interpret("max");
		
		assertEquals(200, hostCtx.popData());
		assertTrue(hostCtx.getDataStack().isEmpty());
		
		hostCtx.pushData(200);
		hostCtx.pushData(100);
		
		interpret("max");
		
		assertEquals(200, hostCtx.popData());
		assertTrue(hostCtx.getDataStack().isEmpty());

	}
}
