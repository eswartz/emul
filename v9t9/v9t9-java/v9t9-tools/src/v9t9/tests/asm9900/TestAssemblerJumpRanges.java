/*
  TestAssemblerJumpRanges.java

  (c) 2008-2013 Edward Swartz

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

public class TestAssemblerJumpRanges extends BaseTest9900 {

	public void testAssemblerJumpRanges0() throws Exception {
		String text =
			" aorg >100\n"+
			" li R0, 44\n"+
			" jne $+2\n"+
			" jmp $1+\n"+
			"$2:\n"+
			" rt\n"+
			" aorg >400\n"+
			"$1: mov r5,r5\n"+
			" jne $2-\n"+
			"foo: \n"+
			" ai r7, -2\n"+
			" jgt ($3+)\n"+
			" jmp ($2-)\n"+
			" aorg >ff00\n"+
			"$3: rtwp\n"+
			"";
		
		testFileContent(text,
				0x100,
				"li r0,44", //100
				" jne $+2\n",//104
				"b @>400", //106
				"b *11", //10A
				0x400,
				"mov r5,r5", //400
				"jeq >408", //402
				"b @>10a", //404
				"dect r7", //408
				"jlt >412", //40a
				"jeq >412", //40c
				"b @>ff00", //40e
				"b @>10a",
				0xff00,
				"rtwp"
				);
		
	}
	
	public void testAssemblerJumpRanges1() throws Exception {
		String text =
			" aorg >100\n"+
			" li R0, 44\n"+
			" jne $+2\n"+
			" jmp $1+\n"+
			"$2:\n"+
			" rt\n"+
			" aorg >400\n"+
			"$1: mov r5,r5\n"+
			" jne $2-\n"+
			"foo: \n"+
			" ai r7, -2\n"+
			" jgt ($3+) - 2\n"+
			" jmp ($2-) + 10\n"+
			" aorg >ff00\n"+
			"$3: rtwp\n"+
			"";
		
		testFileContent(text,
				0x100,
				"li r0,44", //100
				" jne $+2\n",//104
				"b @>400", //106
				"b *11", //10A
				0x400,
				"mov r5,r5", //400
				"jeq >408", //402
				"b @>10a", //404
				"dect r7", //408
				"jlt >412", //40a
				"jeq >412", //40c
				"b @>fefe", //40e
				"b @>114",
				0xff00,
				"rtwp"
				);
		
	}
	
	public void testAssemblerJumpRanges2() throws Exception {
		String text =
			" aorg >100\n"+
			" li R0, 44\n"+
			" jop $1+\n"+	// can't invert (yet)
			"$2:\n"+
			" rt\n"+
			" aorg >400\n"+
			"$1: mov r5,r5\n"+
			"";
		
		try {
			testFileContent(text,
					0x100,
					"li r0,44",
					"jop >400",
					"b @>400",
					0x400,
					"mov r5,r5" //400
					);
			fail("should have exception");
		} catch (Error e) {
			
		}
		
	}
	
	private void testFileContent(String text, Object... pcOrInst) throws Exception {
		String caller = new Exception().fillInStackTrace().getStackTrace()[1].getMethodName();
		stdAssembler.pushContentEntry(new ContentEntry(caller + ".asm", text));
		List<IInstruction> asminsts = stdAssembler.parse();
		List<IInstruction> realinsts = stdAssembler.resolve(asminsts);
		realinsts = stdAssembler.optimize(realinsts);
		realinsts = stdAssembler.fixupJumps(realinsts);

		testGeneratedContent(stdAssembler, realinsts, pcOrInst);
	}


}
