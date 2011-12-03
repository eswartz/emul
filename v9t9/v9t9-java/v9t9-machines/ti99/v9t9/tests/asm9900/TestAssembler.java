package v9t9.tests.asm9900;

import java.io.IOException;

import v9t9.engine.cpu.Operand;
import v9t9.tests.inst9900.BaseTest9900;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.AssemblerOperandParserStage;
import v9t9.tools.asm.assembler.AssemblerTokenizer;
import v9t9.tools.asm.assembler.IOperandParserStage;
import v9t9.tools.asm.assembler.OperandParser;
import v9t9.tools.asm.assembler.ParseException;
import v9t9.tools.asm.assembler.inst9900.Assembler9900;
import v9t9.tools.asm.assembler.inst9900.AssemblerOperandParserStage9900;
import v9t9.tools.asm.assembler.inst9900.MachineOperandFactory9900;
import v9t9.tools.asm.assembler.inst9900.MachineOperandParserStage9900;
import v9t9.tools.asm.assembler.inst9900.StandardInstructionParserStage9900;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

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


	private Operand parseOperand(IOperandParserStage opStage, String string) throws ParseException {
		AssemblerTokenizer tokenizer = new AssemblerTokenizer(string);
		Operand op = opStage.parse(tokenizer);
		int t = tokenizer.nextToken();
		if (t != AssemblerTokenizer.EOF)
			throw new ParseException("Unterminated operand: " + tokenizer.currentToken());
		return op;
	}
	
	private void testBadAsmOp(String string) {
		try {
			AssemblerOperandParserStage opStage = new AssemblerOperandParserStage9900(assembler);
			Operand op = parseOperand(opStage, string);
			fail("Expected error, got " + op);
		} catch (ParseException e) {
			
		}
	}

	private void testAsmOp(String string, Operand expOp) throws ParseException, IOException {
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStage9900(assembler);
		System.out.println("AsmOp: " + string);
		Operand op = parseOperand(opStage, string);
		assertEquals(expOp, op);
	}
}
