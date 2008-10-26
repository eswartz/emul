package v9t9.tests;

import java.io.IOException;
import java.util.Random;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.AssemblerOperandParserStage;
import v9t9.tools.asm.AssemblerOptions;
import v9t9.tools.asm.AssemblerTokenizer;
import v9t9.tools.asm.IInstructionParserStage;
import v9t9.tools.asm.IOperandParserStage;
import v9t9.tools.asm.MachineOperandParserStage;
import v9t9.tools.asm.NumberOperand;
import v9t9.tools.asm.OperandParser;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.StandardInstructionParserStage;
import v9t9.tools.asm.Symbol;
import v9t9.tools.asm.operand.AddrOperand;
import v9t9.tools.asm.operand.RegIncOperand;
import v9t9.tools.asm.operand.RegIndOperand;
import v9t9.tools.asm.operand.RegOffsOperand;
import v9t9.tools.asm.operand.SymbolOperand;
import v9t9.tools.asm.operand.UnaryOperand;
import v9t9.tools.llinst.ParseException;
import v9t9.utils.Utils;

public class TestAssembler extends BaseTest {

	MachineOperandParserStage opStage = new MachineOperandParserStage();
	StandardInstructionParserStage stdInstStage = new StandardInstructionParserStage();
	
	Assembler assembler = new Assembler(new AssemblerOptions());
	OperandParser opParser = new OperandParser();
	{
		opParser.appendStage(new AssemblerOperandParserStage(assembler));
	}
	StandardInstructionParserStage asmInstStage = new StandardInstructionParserStage(opParser);
	
	protected MachineOperand operand(String string) throws Exception {
		return (MachineOperand) parseOperand(opStage, string);
	}

	public void testOperands() throws Exception {
		assertEquals(2, operand("R2").getBits());
		try {
			operand("R16").getBits();
			fail();
		} catch (IllegalArgumentException e) {
		}
		assertEquals(0x12, operand("*R2").getBits());
		try {
			operand("*R16").getBits();
			fail();
		} catch (IllegalArgumentException e) {
		}
		assertEquals(0x38, operand("*R8+").getBits());
		try {
			operand("*R16+").getBits();
			fail();
		} catch (IllegalArgumentException e) {
		}
		assertEquals(0x2f, operand("@>2(R15)").getBits());
		try {
			operand("@2(R16)").getBits();
			fail();
		} catch (IllegalArgumentException e) {
		}
		// immeds
		assertEquals(0x0, operand(">b").getBits());
		// immeds
		MachineOperand op = new MachineOperand(MachineOperand.OP_CNT);
		op.val = op.immed = 0xb;
		assertEquals(0xb, op.getBits());
	}

	public void testParse() throws Exception {
		CPU.writeWord(0, (short) 0x100);
		Instruction minst = new Instruction(CPU.readWord(0), 0, CPU);
		System.out.println(minst);
		assertEquals(0x100, minst.opcode);
		assertEquals(2, minst.size);
		
	}
	public void testEncode1() throws Exception {
		_testEncode(stdInstStage);

	}

	private void _testEncode(IInstructionParserStage instStage) throws ParseException, ResolveException {
		assertInst(instStage, "MOV R1,R2");
		assertInst(instStage, "MOV R1,*R2");
		assertInst(instStage, "MOV @4(R1),R15");
		assertInst(instStage, "MOV @4(R1),*R15+");
		assertInst(instStage, "LI 6,>777");
		assertInst(instStage, "CI R15,0");
		assertInst(instStage, "STWP R8");
		assertInst(instStage, "LWPI >84e0");
		assertInst(instStage, "LIMI 2");
		assertInst(instStage, "IDLE");
		assertInst(instStage, "LREX");
		assertInst(instStage, "BLWP @>0");
		assertInst(instStage, "BLWP @>0420");
		assertInst(instStage, "BLWP @>0420(R6)");
		assertInst(instStage, "BLWP *R5");
		assertInst(instStage, "BLWP 5");
		assertInst(instStage, "X R6");
		assertInst(instStage, "SRA R5,8");
		assertInst(instStage, "SRA R5,R0");
		assertInst(instStage, "JMP 4");
		assertInst(instStage, "JMP $");
		assertInst(instStage, "JNC $+4");
		assertInst(instStage, "JHE $->54");
		assertInst(instStage, "SBO >1");
		assertInst(instStage, "SBZ >2");
		assertInst(instStage, "COC *R5+,9");
		assertInst(instStage, "XOR @>6,R15");
		
		assertInst(instStage, "LDCR @>6,14");
		assertInst(instStage, "STCR R5,0");
		assertInst(instStage, "MPY @>44ff,R11");
		assertInst(instStage, "SZC @>6,R15");
		assertInst(instStage, "MOVB *R6+,*R1+");
		assertInst(instStage, "A 3,3");

		assertInst(instStage, "DATA >123");
		assertInst(instStage, "SBO >99");
		
		assertBadInst(instStage, "ABS >4ff");
		assertBadInst(instStage, "ABS >4,4");
		assertBadInst(instStage, "BLWP");
		assertBadInst(instStage, "MOV");
		assertBadInst(instStage, "SRL 6,88");
		assertBadInst(instStage, "SRL @7,3");
		assertBadInst(instStage, "JMP *R12");
		assertBadInst(instStage, "STCR R5,*R3");
		assertBadInst(instStage, "MPY @>44ff,@>3");
		assertBadInst(instStage, "A 0");
	}
	
	public void testEncodedInsts() throws Exception {
		assertInstWords(new short[] { 0x135B });
		assertInstWords(new short[] { 0x111 });
		assertInstWords(new short[] { 0x1f50 });
		assertInstWords(new short[] { 0x0257, (short) 0xECA2 });
		assertInstWords(new short[] { 0x653 });
	}
	public void testRandomInst() throws Exception {
		int cnt = 100;
		Random random = new Random(System.currentTimeMillis());
		while (cnt-- > 0) {
			assertInstWords(new short[] {
					(short)random.nextInt(65536),
					(short)random.nextInt(65536),
					(short)random.nextInt(65536),
			});
		}
	}

	private void assertInstWords(short[] iwords) throws ParseException {
		for (int i = 0; i < iwords.length; i++) {
			CPU.flatWriteWord(i*2, iwords[i]);
		}
		
		Instruction minst = new Instruction(CPU.readWord(0), 0, CPU);
		InstructionTable.coerceOperandTypes(minst);
		System.out.println(minst + " = " + Utils.toHex4(minst.opcode));
		Instruction[] insts = stdInstStage.parse(minst.toString());
		
		if (insts == null || insts.length != 1)
			fail("failed to parse inst " + minst + " : " + insts);
		
		short[] words = InstructionTable.encode(insts[0]);
		assertEquals(minst.toString(), minst.size, words.length*2);
		
		for (int i = 0; i < minst.size; i += 2) {
			short exp = CPU.readWord(i);
			if (i == 0)
				exp = (short) InstructionTable.coerceInstructionOpcode(minst.inst, exp);
			assertEquals(minst.toString() + "@" + minst.size, exp, words[i/2]);
		}
	}

	private void assertBadInst(IInstructionParserStage instStage, String string) {
		try {
			assertInst(instStage, string);
			fail();
		} catch (IllegalArgumentException e) {
			
		} catch (ParseException e) {
			
		} catch (ResolveException e) {
		} catch (Error e) {
			throw e;
		}
	}
	
	private void assertInst(IInstructionParserStage instStage, String string) throws ParseException, ResolveException {
		Instruction[] insts = instStage.parse(string);
		assertNotNull(insts);
		assertEquals(1, insts.length);
		
		assembler.resolveInstruction(insts[0]);
		
		short[] words = InstructionTable.encode(insts[0]);
		assertEquals(insts[0].size, words.length * 2);
		
		insts[0].pc = 0;
		for (int i = 0; i < words.length; i++)
			CPU.flatWriteWord(i*2, words[i]);
		
		Instruction minst = new Instruction(words[0], 0, CPU);
		InstructionTable.coerceOperandTypes(minst);
		
		assertEquals(insts[0].toString(), minst.toString());
		System.out.println(insts[0]);
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

	public void testAsmOpSymbols() throws Exception {
		testAsmOp("R5", new NumberOperand(5));
		testAsmOp("R15", new NumberOperand(0xf));
		
		testAsmOp("@FOO", new AddrOperand(new SymbolOperand(new Symbol("FOO"))));
	}

	public void testAsmOpRegisters() throws Exception {
		testAsmOp("*R5", new RegIndOperand(new NumberOperand(5)));
		testAsmOp("*>f", new RegIndOperand(new NumberOperand(15)));
		testAsmOp("*0+", new RegIncOperand(new NumberOperand(0)));
		testAsmOp("* R5 +", new RegIncOperand(new NumberOperand(5)));
		
		testBadAsmOp("*+");
		testBadAsmOp("*++");
	}

	public void testAsmOpAddrs() throws Exception {
		testAsmOp(" @0", new AddrOperand(new NumberOperand(0)));
		testAsmOp("@>FfFf", new AddrOperand(new NumberOperand(65535)));
		testAsmOp("@-4", new AddrOperand(new UnaryOperand('-', new NumberOperand(4))));
		testAsmOp("@66(0)", new RegOffsOperand(new NumberOperand(66), new NumberOperand(0)));
		testAsmOp("\t@>f(>4)", new RegOffsOperand(new NumberOperand(15), new NumberOperand(4)));
		testAsmOp("@8(R15)", new RegOffsOperand(new NumberOperand(8), new NumberOperand(15)));
		
		testAsmOp("@(0)", new AddrOperand(new NumberOperand(0)));
	}
	
	public void testAsmSymbols() throws Exception {
		Assembler assembler = new Assembler(new AssemblerOptions());
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStage(assembler);

		Operand op = parseOperand(opStage, "@BUFFER(R5)");
		Symbol sym = assembler.getSymbolTable().findSymbol("BUFFER");
		assertNotNull(sym);
		assertEquals(new RegOffsOperand(new SymbolOperand(sym),
				new NumberOperand(5)), op);
		
	}


	private void testBadAsmOp(String string) {
		try {
			Assembler assembler = new Assembler(new AssemblerOptions());
			AssemblerOperandParserStage opStage = new AssemblerOperandParserStage(assembler);
			Operand op = parseOperand(opStage, string);
			fail("Expected error, got " + op);
		} catch (ParseException e) {
			
		}
	}

	private void testAsmOp(String string, Operand expOp) throws ParseException, IOException {
		Assembler assembler = new Assembler(new AssemblerOptions());
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStage(assembler);
		System.out.println("AsmOp: " + string);
		Operand op = parseOperand(opStage, string);
		assertEquals(expOp, op);
	}

	private Operand parseOperand(IOperandParserStage opStage, String string) throws ParseException {
		AssemblerTokenizer tokenizer = new AssemblerTokenizer(string);
		Operand op = opStage.parse(tokenizer);
		int t = tokenizer.nextToken();
		if (t != AssemblerTokenizer.EOF)
			throw new ParseException("Unterminated operand: " + tokenizer.currentToken());
		return op;
	}
	
	public void testAsmResolve() throws Exception {
		testResolve("mov *5+, @>0");
		testResolve("li r7, >1234");
		testResolve("mov @sym,@sym+2",
				"mov @>1234,@>1236",
				new Symbol[] { new Symbol("sym", 0x1234) });
	}
	
	/** Test resolving an instruction's operands */
	private void testResolve(String string, String stdInst, Symbol[] symbols) throws ParseException, ResolveException {
		for (Symbol symbol : symbols)
			assembler.getSymbolTable().addSymbol(symbol);
		
		System.out.println("AsmInst: " + string);
		Instruction[] insts = asmInstStage.parse(string);
		assertEquals(1, insts.length);

		assembler.resolveInstruction(insts[0]);
		
		Instruction[] stdInsts = stdInstStage.parse(stdInst);
		assertEquals(stdInsts.length, insts.length);
		for (int i = 0; i < stdInsts.length; i++)
			assertEquals(stdInsts[i], insts[i]);
	}
	
	private void testResolve(String inst, String stdInst) throws ParseException, ResolveException {
		testResolve(inst, stdInst, new Symbol[0]);
	}
	private void testResolve(String inst) throws ParseException, ResolveException {
		testResolve(inst, inst);
	}
	
	public void testEncode2() throws Exception {
		_testEncode(asmInstStage);

	}
	public void testAsmInst() throws Exception {
		// not the simpler parser above
		testAsmInst("movb @>9800, 1");
		testAsmInst("li r5, 11");
		testAsmInst("movb *r5+, @foo");
	}

	private void testAsmInst(String string) throws ParseException {
		System.out.println("AsmInst: " + string);
		Instruction[] insts = asmInstStage.parse(string);
		assertEquals(1, insts.length);
		/*
		try {
			Instruction[] stdInsts = stdInstStage.parse(string);
			assertEquals(stdInsts.length, insts.length);
			for (int i = 0; i < stdInsts.length; i++)
				assertEquals(stdInsts[i], insts[i]);
		} catch (ParseException e) {
			// ignore, not standard
		}
		*/
		
	}
	
}
