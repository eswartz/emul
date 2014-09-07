/*
  TestAssembler.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tests.asm9900;

import java.io.IOException;

import v9t9.common.asm.IOperand;
import v9t9.tests.inst9900.BaseTest9900;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.AssemblerOperandParserStage;
import v9t9.tools.asm.AssemblerTokenizer;
import v9t9.tools.asm.IOperandParserStage;
import v9t9.tools.asm.OperandParser;
import v9t9.tools.asm.ParseException;
import v9t9.tools.asm.inst9900.Assembler9900;
import v9t9.tools.asm.inst9900.AssemblerOperandParserStage9900;
import v9t9.tools.asm.inst9900.MachineOperandFactory9900;
import v9t9.tools.asm.inst9900.MachineOperandParserStage9900;
import v9t9.tools.asm.inst9900.StandardInstructionParserStage9900;
import v9t9.tools.asm.operand.hl.BinaryOperand;
import v9t9.tools.asm.operand.hl.NumberOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

public class TestAssembler extends BaseTest9900 {

	MachineOperandParserStage9900 opStage = new MachineOperandParserStage9900();
	MachineOperandFactory9900 opFactory = new MachineOperandFactory9900();
	
	Assembler9900 assembler = new Assembler9900();
	{
		assembler.setProcessor(Assembler.PROC_9900);
	}
	OperandParser opParser = new OperandParser();
	{
		opParser.appendStage(new AssemblerOperandParserStage9900(assembler));
	}
	StandardInstructionParserStage9900 asmInstStage = new StandardInstructionParserStage9900(opParser);
	
	protected LLOperand operand(String string) throws Exception {
		return (LLOperand) parseOperand(opStage, string);
	}

	public void testTokenizer() throws Exception {
		AssemblerTokenizer tz = new AssemblerTokenizer("*R15+");
		assertEquals('*', tz.nextToken());
		assertEquals(AssemblerTokenizer.ID, tz.nextToken());
		assertEquals("R15", tz.getId());
		assertEquals('+', tz.nextToken());
		assertEquals(AssemblerTokenizer.EOF, tz.nextToken());
		
		tz = new AssemblerTokenizer("@ FOO +\t66 ( 4 )\t");
		assertEquals('@', tz.nextToken());
		assertEquals(AssemblerTokenizer.ID, tz.nextToken());
		assertEquals("FOO", tz.getId());
		assertEquals('+', tz.nextToken());
		assertEquals(AssemblerTokenizer.NUMBER, tz.nextToken());
		assertEquals(66, tz.getNumber());
		assertEquals('(', tz.nextToken());
		assertEquals(AssemblerTokenizer.NUMBER, tz.nextToken());
		assertEquals(4, tz.getNumber());
		assertEquals(')', tz.nextToken());
		assertEquals(AssemblerTokenizer.EOF, tz.nextToken());
		
		tz = new AssemblerTokenizer("'6'");
		assertEquals(AssemblerTokenizer.CHAR, tz.nextToken());
		assertEquals("6", tz.getString());
		assertEquals(AssemblerTokenizer.EOF, tz.nextToken());

		tz = new AssemblerTokenizer("\"Major General 666\"");
		assertEquals(AssemblerTokenizer.STRING, tz.nextToken());
		assertEquals("Major General 666", tz.getString());
		assertEquals(AssemblerTokenizer.EOF, tz.nextToken());

	}
	
	public void testAsmOpParserNumbers() throws Exception {
		testAsmOp("5", new NumberOperand(5));
		testAsmOp(">12F", new NumberOperand(0x12f));
		testAsmOp("123", new NumberOperand(123));
		
		testBadAsmOp("=");
		testBadAsmOp("0]");
		testBadAsmOp("123;");
		testBadAsmOp("0x88");
	}


	public void testAsmOpParserExprs() throws Exception {
		testAsmOp("1+5", new BinaryOperand('+', new NumberOperand(1), new NumberOperand(5)));
		testAsmOp("3-2-1", 
				new BinaryOperand('-', new BinaryOperand('-', new NumberOperand(3), new NumberOperand(2)), new NumberOperand(1)));
		testAsmOp("3-4*8", 
				new BinaryOperand('-', 
						new NumberOperand(3),
						new BinaryOperand('*', new NumberOperand(4), new NumberOperand(8))));
		testAsmOp("3+1 >= 4-2", 
				new BinaryOperand('≥', 
						new BinaryOperand('+', new NumberOperand(3), new NumberOperand(1)),
						new BinaryOperand('-', new NumberOperand(4), new NumberOperand(2))));
	}

	private IOperand parseOperand(IOperandParserStage opStage, String string) throws ParseException {
		AssemblerTokenizer tokenizer = new AssemblerTokenizer(string);
		IOperand op = opStage.parse(tokenizer);
		int t = tokenizer.nextToken();
		if (t != AssemblerTokenizer.EOF)
			throw new ParseException("Unterminated operand: " + tokenizer.currentToken());
		return op;
	}
	
	private void testBadAsmOp(String string) {
		try {
			AssemblerOperandParserStage opStage = new AssemblerOperandParserStage9900(assembler);
			IOperand op = parseOperand(opStage, string);
			fail("Expected error, got " + op);
		} catch (ParseException e) {
			
		}
	}

	private void testAsmOp(String string, IOperand expOp) throws ParseException, IOException {
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStage9900(assembler);
		System.out.println("AsmOp: " + string);
		IOperand op = parseOperand(opStage, string);
		assertEquals(expOp, op);
	}
}
