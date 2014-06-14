/*
  TestForthCompBootstrapF99b.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import v9t9.machine.f99b.asm.InstF99b;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class TestForthCompBootstrapF99b extends BaseF99bTest {
	@Test
	public void testInvokeSimple1() throws Exception {
		
		parseString(
			": num 1 ;\n"+
			"num");
		
		assertEquals(1, hostCtx.popData()); 
	}
	@Test
	public void testInvokeSimple2() throws Exception {
		
		hostCtx.pushData(1); 
		parseString(
				": num 2 + ;\n"+
		"num");
		
		assertEquals(3, hostCtx.popData()); 
	}
	@Test
	public void testInvokeCalls1() throws Exception {
		
		hostCtx.pushData(1); 
		parseString(
				": inner 10 * ;\n"+
				": num inner 2 +  ;\n"+
		"num");
		
		assertEquals(12, hostCtx.popData()); 
	}
	@Test
	public void testVariables1() throws Exception {
		
		hostCtx.pushData(1); 
		parseString(
				"Variable x\n"+
				"10 x !\n"+
				": inner x @ * ;\n"+
				": num inner 2 +  ;\n"+
		"num");
		
		assertEquals(12, hostCtx.popData()); 
	}
	@Test
	public void testValues1() throws Exception {
		
		hostCtx.pushData(1); 
		parseString(
				"0 Value x\n"+
				": inner x * ;\n"+
				": num inner 2 +  ;\n"+
				"10 to x\n"+
		"num");
		
		assertEquals(12, hostCtx.popData()); 
	}
	
	@Test
	public void testConditionsIf() throws Exception {
		
		hostCtx.pushData(1); 
		parseString(
				": num dup 0 < if negate else 5 * then ;\n"+
		"-11 num");
		
		assertEquals(11, hostCtx.popData()); 
		assertEquals(1, hostCtx.popData());
		
		
		parseString("10 num");
		
		assertEquals(50, hostCtx.popData()); 

	}
	

	@Test
	public void testDictChange() throws Exception {
		
		parseString(
				stockDictDefs +
				": num dup , $f and c,  ;\n"+
				"here\n"+
		"31 num  13 num\n"+
				"here\n");
		
		int outHere = hostCtx.popData();
		assertEquals(outHere, targCtx.getDP());
		int origHere = hostCtx.popData();
		assertEquals(2 * (targCtx.getCellSize() + 1), outHere - origHere);
		
		dumpDict();
		
		assertEquals(31, targCtx.readCell(origHere));
		assertEquals(15, targCtx.readChar(origHere + 2));
		assertEquals(13, targCtx.readCell(origHere + 3));
		assertEquals(13, targCtx.readChar(origHere + 5));
	}


	@Test
	public void testImmed1() throws Exception {
		
		parseString(
				"Variable foo\n"+
				": doit 123 Foo ! ; immediate\n"+
				": push 11 Foo ! doit  ;\n");
		
		ITargetWord var = targCtx.require("Foo");
		assertEquals(123, targCtx.readCell(var.getEntry().getParamAddr())); 
		
		parseString("push");
		
		assertEquals(11, targCtx.readCell(var.getEntry().getParamAddr()));
	}

	@Test
	public void testImmed2() throws Exception {
		
		parseString(
				"Variable foo\n"+
				": doit 123 Foo ! ; immediate\n"+
				": push 11 Foo ! postpone doit ;\n");
		
		dumpDict();
		
		ITargetWord var = targCtx.require("Foo");
		assertEquals(0, targCtx.readCell(var.getEntry().getParamAddr())); 
		
		parseString("push");
		
		assertEquals(123, targCtx.readCell(var.getEntry().getParamAddr()));
	}
	@Test
	public void testDictCompileLiteral() throws Exception {
		
		// TRICKY!  We provide a way to both interpret (colon-def-wise) and
		// show a mirror high-level variant, to ensure we match the behavior
		parseString(
				stockDictDefs +
				compileLiteral +
				compileMeta +
				": literal ( n -- ) dup -8 >= over 8 < and  if\n" +
				" 	$f and $20 or c,  else\n" +
				"dup -128 >= over 128 < and  if\n" +
				" 	$78 c, c,\n" +
				"else\n" +
				"	$79 c, ,\n" +
				"then then\n"+
				"; immediate host( 1 ) literal \n"+
				": pushNum postpone literal  ; immediate\n"+	
				": push [ 10 ] pushNum [ -3 ] pushNum [ $1234 ] pushNum ;\n"+
				"push\n");
		
		dumpDict();
		assertEquals(0x1234, hostCtx.popData());
		assertEquals(-3, hostCtx.popData());
		assertEquals(10, hostCtx.popData());

		int dp = (targCtx.require("push")).getEntry().getContentAddr();
		assertEquals(InstF99b.IlitB, targCtx.readChar(dp++));
		assertEquals(10, targCtx.readChar(dp++));
		assertEquals(InstF99b.IlitX | (-3 & 0xf), targCtx.readChar(dp++));
		assertEquals(InstF99b.IlitW, targCtx.readChar(dp++));
		assertEquals(0x12, targCtx.readChar(dp++));
		assertEquals(0x34, targCtx.readChar(dp++));
		assertEquals(InstF99b.Iexit, targCtx.readChar(dp++));

	}
	

	@Test
	public void testDictCompileWord() throws Exception {
		
		hostCtx.pushData(99);
		
		// Provide enough target defs to convince the compiler
		// to allow emulating.
		// Of course, /interpreting/ make-adder: would lead to BIG TROUBLE!
		parseString(
				stockDictDefs +
				compileLiteral +
				compileMeta +
				": make-adder: ( n -- ) :  postpone literal  postpone +  0  postpone ; drop ;\n "+
				"here swap\n"+
				"100 make-adder: 100+\n"+
				"100+");
		
		dumpDict();
		assertEquals(199, hostCtx.popData());
		
		int dphere = hostCtx.popData();

		DictEntry newEntry = (targCtx.require("100+")).getEntry();
		int dp = newEntry.getContentAddr();

		assertEquals(dp+"|"+dphere, dp, dphere + newEntry.getHeaderSize());
		
		assertEquals(InstF99b.IlitB, targCtx.readChar(dp++));
		assertEquals(100, targCtx.readChar(dp++));
		assertEquals(InstF99b.Iadd, targCtx.readChar(dp++));
		assertEquals(InstF99b.Iexit, targCtx.readChar(dp++));

	}
	
	@Test
	public void testDictCreateDoes() throws Exception {
		
		hostCtx.pushData(99);
		
		// Provide enough target defs to convince the compiler
		// to allow emulating.
		// Of course, /interpreting/ make-adder: would lead to BIG TROUBLE!
		parseString(
				stockDictDefs +
				compileLiteral +
				compileMeta+
				": xt! ( xt addr -- ) $7B ( BRANCHW ) over c! 1+ ! ; \n"+	// BOGUS
				//": ] true state ! ; immediate host( 0 ) ] \n"+
				": (does>) r> latest xt!  ;  \n"+
				": does> postpone (does>) postpone rdrop ; immediate target-only\n"+
				": make-adder: ( n -- ) create , does> @ + ;\n "+
				"here swap\n"+
				"100 make-adder: 100+\n");
		
		dumpDict();
		exportBinary();
		
		parseString("100+");
		assertEquals(199, hostCtx.popData());


		// make sure CREATE , DOES> has expected code in it
		DictEntry creatorEntry = (targCtx.require("make-adder:")).getEntry();
		int dp = creatorEntry.getContentAddr();

		// PUNT
		assertTrue((targCtx.readCell(dp) & 0xffff) >= 0x8000); dp+=2;
		assertTrue((targCtx.readCell(dp) & 0xffff) >= 0x8000); dp+=2;
		
		// @ + bit
		int doesDp = dp;
		assertTrue(""+dp, (dp & 1) == 0);	// calls, so must align
		assertEquals(InstF99b.IRfrom, targCtx.readChar(dp++));	// get address
		assertEquals(InstF99b.Iload, targCtx.readChar(dp++));	// user-specified code
		assertEquals(InstF99b.Iadd, targCtx.readChar(dp++));
		assertEquals(InstF99b.Iexit, targCtx.readChar(dp++));
		
		/// now make sure the created word is sensible
		
		int dphere = hostCtx.popData();
		
		DictEntry newEntry = (targCtx.require("100+")).getEntry();
		dp = newEntry.getContentAddr();

		assertEquals(dp+"|"+dphere, ((dphere + newEntry.getHeaderSize()+1)&~1), dp);
		
		/*
		// first, normal DOVAR (PC@ 2+)
		assertEquals(InstF99b.IcontextFrom, targCtx.readChar(dp++));
		assertEquals(InstF99b.CTX_PC, targCtx.readChar(dp++));
		assertEquals(InstF99b.I2plus, targCtx.readChar(dp++));
		// then, instead of exit, go to a jump
		assertEquals(InstF99b.IbranchX | 2, targCtx.readChar(dp++));
		// then the data
		assertEquals(100, targCtx.readCell(dp)); dp+=2;
		// and the DOES> redirect
		assertEquals(InstF99b.IbranchW, targCtx.readChar(dp++));
		assertEquals(doesDp, targCtx.findReloc(dp)); dp+=2;
		*/

		// the DOES> redirect
		assertEquals(doesDp, targCtx.findReloc(dp)); dp+=2;
		// then the data
		assertEquals(100, targCtx.readCell(dp)); dp+=2;
		// should be nothing weird after this
		assertEquals(targCtx.getDP(), dp);
	}
	
	@Test
	public void testStringLits() throws Exception {
		
		startDP = 0x400;
		parseString(
				stockDictDefs+
				compileLiteral+
				compileMeta+
				": s\" postpone (s\") ; immediate target-only\n"+
				": lala s\" hi there\" ;\n"
		);		
		dumpDict();
		exportBinary();
		
		parseString("lala");
		
		DictEntry entry = (targCtx.require("lala")).getEntry();
		int dp = entry.getContentAddr();
		assertEquals(((ITargetWord)targCtx.require("(S\")")).getEntry().getContentAddr(), targCtx.findReloc(dp)); 
		dp+=2;
		/*
		assertEquals(InstF99b.IcontextFrom, targCtx.readChar(dp++));
		assertEquals(InstF99b.CTX_PC, targCtx.readChar(dp++));
		assertEquals(InstF99b.IlitX | 5, targCtx.readChar(dp++));
		assertEquals(InstF99b.Iadd, targCtx.readChar(dp++));
		assertEquals(InstF99b.Idup, targCtx.readChar(dp++));
		assertEquals(InstF99b.I1plus, targCtx.readChar(dp++));
		assertEquals(InstF99b.Iswap, targCtx.readChar(dp++));
		assertEquals(InstF99b.Icload, targCtx.readChar(dp++));
		*/
		
		assertEquals(8, targCtx.readChar(dp++));
		assertEquals('h', targCtx.readChar(dp++));
		assertEquals('i', targCtx.readChar(dp++));
		assertEquals(' ', targCtx.readChar(dp++));
		assertEquals('t', targCtx.readChar(dp++));
		assertEquals('h', targCtx.readChar(dp++));
		assertEquals('e', targCtx.readChar(dp++));
		assertEquals('r', targCtx.readChar(dp++));
		assertEquals('e', targCtx.readChar(dp++));
		
		assertEquals(InstF99b.Iexit, targCtx.readChar(dp++));
	}
}
