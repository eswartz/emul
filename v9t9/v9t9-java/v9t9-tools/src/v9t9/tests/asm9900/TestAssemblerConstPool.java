/*
  TestAssemblerConstPool.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tests.asm9900;

import java.util.List;

import v9t9.common.asm.IInstruction;
import v9t9.tests.inst9900.BaseTest9900;
import v9t9.tools.asm.ContentEntry;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.transform.ConstPool;

public class TestAssemblerConstPool extends BaseTest9900 {
	protected int getTableByte(ConstPool pool, int val) {
		AssemblerOperand op = pool.allocateByte(val);
		return pool.getTableOffset(op);
	}
	protected int getTableWord(ConstPool pool, int val) {
		AssemblerOperand op = pool.allocateWord(val);
		return pool.getTableOffset(op);
	}
	
	public void testConstTable1() throws Exception {
		ConstPool pool = stdAssembler.getConstPool();
		pool.clear();
		
		int op1 = getTableByte(pool, 0);
		int op2 = getTableByte(pool, 0);
		assertEquals(op1, op2);
		op2 = getTableByte(pool, 1);
		assertTrue(op1 != op2);
		int op3 = getTableByte(pool, 1);
		assertEquals(op2, op3);
		int op4 = getTableByte(pool, 0);
		assertEquals(op4, op1);
		// force odd
		int two = getTableByte(pool, 0x2);

		// get a word and make sure it's at an odd offset
		int op5 = getTableWord(pool, 0x1234);
		assertEquals(0, op5 & 1);
		assertTrue(op1 != op5);
		assertTrue(op2 != op5);
		assertTrue(op3 != op5);
		assertTrue(op4 != op5);
		
		int op6 = getTableWord(pool, 0x1234);
		assertEquals(op5, op6);
		
		// make sure the word can be picked for bytes
		int op7 = getTableByte(pool, 0x12);
		assertEquals(op5, op7);
		int op8 = getTableByte(pool, 0x34);
		assertEquals(op8, op7+1);
		
		// check that if a byte is allocated, then a word (which forces even)
		// that then a word access of byte*256 is reused
		int op9 = getTableWord(pool, 0x0200);
		assertEquals(two, op9);
		
		byte[] bytes = pool.getBytes();
		assertEquals(op8 + 1, bytes.length);
		assertEquals(0, bytes[0]);
		assertEquals(1, bytes[1]);
		assertEquals(2, bytes[2]);
		assertEquals(0, bytes[3]);
		assertEquals(0x12, bytes[4]);
		assertEquals(0x34, bytes[5]);
	}
	
	public void testAssemblerConstTable1() throws Exception {
		String text =
			" aorg >100\n"+
			" cb R0, #'.'\n"+	//100
			" movb #'.',R1\n"+	//104
			" coc #>2000,R4\n"+  //108
			" a #'.', R0\n"+     //10C
			" sb #>20,@>8300\n"+//110
			" mov #('.'*256),R1\n"+  //116
			" soc #>2000,@>8320\n"+//11A
			//120
			"";
		
		// 120: >2e00
		// 122: >2000
		// 124: >002e
		//
		testFileContent(text,
				new byte[] { 0x2e, 0x00, 0x20, 0x00, 0x00, 0x2e },
				
				0x100,
				"cb R0, @>120",	//100
				"movb @>120,R1",//104
				"coc @>122,R4",//108
				"a @>124,R0",//11c
				"sb @>122,@>8300",//120
				"mov @>120,r1",//128
				"soc @>122,@>8320"//12c
				);
		
	}
	
	public void testAssemblerInstTable() throws Exception {
		String text =
			" aorg >100\n"+
			" li r1, >1234\n"+  //100
			" cb R0, #>12\n"+	//104
			" movb #>01,R1\n"+	//108
			" coc #>0201,R4\n"+  //10C
			" a #'.', R0\n"+     //110
			" sb #>20,@>8300\n"+//114
			" soc #>2000,@>8320\n"+//11A
			//120
			"";
		
		// 120: >002e
		// 122: >2000
		//
		testFileContent(text,
				new byte[] { 0x00, 0x2e, 0x20, 0x00,  },
				
				0x100,
				"li r1, >1234",	//100
				"cb R0, @>102",//104
				"movb @>101,R1",//108
				"coc @>100,R4",//10C
				"a @>120,R0",//110
				"sb @>10D,@>8300",//114	// from COC
				"soc @>122,@>8320"//11A
				);
		
	}
	
	public void testAssemblerInstTable2() throws Exception {
		// do not rely on RAM
		String text =
			" aorg >2000\n"+
			" li r1, >1234\n"+  //2000
			" aorg >100\n"+
			" cb R0, #>12\n"+	//100
			" movb #>34,R1\n"+	//104
			//108
			"";
		
		// 108: >1234
		//
		testFileContent(text,
				new byte[] { 0x12, 0x34,  },
				
				0x2000,
				"li r1, >1234",	//2000
				0x100,
				"cb R0, @>108",//104
				"movb @>109,R1" //108
				);
		
	}
	
	public void testAssemblerInstTable3() throws Exception {
		// allow overriding the const pool location
		String text =
			" aorg >100\n"+
			" li r1, >1234\n"+  //100
			" cb R0, #>22\n"+	//104
			" movb #>33,R1\n"+	//108
			//10c
			" consttable\n"+
			" aorg >400\n"+
			"";
		
		// 400: >1234
		//
		testFileContent(text,
				new byte[] { 0x22, 0x33,  },
				
				0x100,
				"li r1, >1234",	//100
				"cb R0, @>10c",//104
				"movb @>10d,R1" //108
				);
		
	}
	
	private void testFileContent(String text, byte[] consts, Object... pcOrInst) throws Exception {
		String caller = new Exception().fillInStackTrace().getStackTrace()[1].getMethodName();
		stdAssembler.pushContentEntry(new ContentEntry(caller + ".asm", text));
		List<IInstruction> asminsts = stdAssembler.parse();
		List<IInstruction> realinsts = stdAssembler.resolve(asminsts);
		realinsts = stdAssembler.optimize(realinsts);
		realinsts = stdAssembler.fixupJumps(realinsts);

		byte[] constBytes = stdAssembler.getConstPool().getBytes();
		assertEquals("table size", consts.length, constBytes.length);
		for (int x = 0; x < constBytes.length; x++)
			assertEquals("#"+x, consts[x], constBytes[x]);
		testGeneratedContent(stdAssembler, realinsts, pcOrInst);
	}


}
