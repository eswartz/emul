package v9t9.tests.asm;

import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.tests.BaseTest;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ContentEntry;
import v9t9.tools.asm.transform.ConstPool;

public class TestAssemblerConstPool extends BaseTest {

	Assembler assembler = new Assembler();
	
	public void testConstTable1() throws Exception {
		ConstPool pool = assembler.getConstPool();
		pool.clear();
		
		int op1 = pool.allocateByte(0);
		int op2 = pool.allocateByte(0);
		assertEquals(op1, op2);
		op2 = pool.allocateByte(1);
		assertTrue(op1 != op2);
		int op3 = pool.allocateByte(1);
		assertEquals(op2, op3);
		int op4 = pool.allocateByte(0);
		assertEquals(op4, op1);
		// force odd
		int two = pool.allocateByte(0x2);

		// get a word and make sure it's at an odd offset
		int op5 = pool.allocateWord(0x1234);
		assertEquals(0, op5 & 1);
		assertTrue(op1 != op5);
		assertTrue(op2 != op5);
		assertTrue(op3 != op5);
		assertTrue(op4 != op5);
		
		int op6 = pool.allocateWord(0x1234);
		assertEquals(op5, op6);
		
		// make sure the word can be picked for bytes
		int op7 = pool.allocateByte(0x12);
		assertEquals(op5, op7);
		int op8 = pool.allocateByte(0x34);
		assertEquals(op8, op7+1);
		
		// check that if a byte is allocated, then a word (which forces even)
		// that then a word access of byte*256 is reused
		int op9 = pool.allocateWord(0x0200);
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
	
	
	private void testFileContent(String text, byte[] consts, Object... pcOrInst) throws Exception {
		String caller = new Exception().fillInStackTrace().getStackTrace()[1].getMethodName();
		assembler.pushContentEntry(new ContentEntry(caller + ".asm", text));
		List<IInstruction> asminsts = assembler.parse();
		List<IInstruction> realinsts = assembler.resolve(asminsts);
		assembler.optimize(realinsts);
		realinsts = assembler.fixupJumps(realinsts);

		testGeneratedContent(assembler, realinsts, pcOrInst);
		byte[] constBytes = assembler.getConstPool().getBytes();
		assertEquals("table size", consts.length, constBytes.length);
		for (int x = 0; x < constBytes.length; x++)
			assertEquals("#"+x, consts[x], constBytes[x]);
	}


}
