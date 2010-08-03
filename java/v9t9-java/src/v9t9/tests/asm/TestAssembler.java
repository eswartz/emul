package v9t9.tests.asm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tests.BaseTest;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.AssemblerInstruction;
import v9t9.tools.asm.assembler.AssemblerOperandParserStage;
import v9t9.tools.asm.assembler.AssemblerOperandParserStage9900;
import v9t9.tools.asm.assembler.AssemblerTokenizer;
import v9t9.tools.asm.assembler.BaseAssemblerInstruction;
import v9t9.tools.asm.assembler.ContentEntry;
import v9t9.tools.asm.assembler.Equate;
import v9t9.tools.asm.assembler.IInstructionParserStage;
import v9t9.tools.asm.assembler.IOperandParserStage;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.MachineOperandFactory9900;
import v9t9.tools.asm.assembler.MachineOperandParserStage9900;
import v9t9.tools.asm.assembler.OperandParser;
import v9t9.tools.asm.assembler.ParseException;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.StandardInstructionParserStage9900;
import v9t9.tools.asm.assembler.Symbol;
import v9t9.tools.asm.assembler.directive.DefineByteDirective;
import v9t9.tools.asm.assembler.directive.DefineWordDirective;
import v9t9.tools.asm.assembler.directive.Directive;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.StringOperand;
import v9t9.tools.asm.assembler.operand.hl.SymbolOperand;
import v9t9.tools.asm.assembler.operand.hl.UnaryOperand;
import v9t9.tools.asm.assembler.operand.ll.LLCountOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

public class TestAssembler extends BaseTest {

	MachineOperandParserStage9900 opStage = new MachineOperandParserStage9900();
	
	Assembler assembler = new Assembler();
	OperandParser opParser = new OperandParser();
	{
		opParser.appendStage(new AssemblerOperandParserStage9900(assembler));
	}
	StandardInstructionParserStage9900 asmInstStage = new StandardInstructionParserStage9900(opParser);
	
	protected LLOperand operand(String string) throws Exception {
		return (LLOperand) parseOperand(opStage, string);
	}

	public void testOperands() throws Exception {
		MachineOperandFactory9900 factory = new MachineOperandFactory9900();
		assertEquals(2, operand("R2").createMachineOperand(factory).getBits());
		try {
			operand("R16").createMachineOperand(factory).getBits();
			fail();
		} catch (IllegalArgumentException e) {
		}
		assertEquals(0x12, operand("*R2").createMachineOperand(factory).getBits());
		try {
			operand("*R16").createMachineOperand(factory).getBits();
			fail();
		} catch (IllegalArgumentException e) {
		}
		assertEquals(0x38, operand("*R8+").createMachineOperand(factory).getBits());
		try {
			operand("*R16+").createMachineOperand(factory).getBits();
			fail();
		} catch (IllegalArgumentException e) {
		}
		assertEquals(0x2f, operand("@>2(R15)").createMachineOperand(factory).getBits());
		try {
			operand("@2(R16)").createMachineOperand(factory).getBits();
			fail();
		} catch (IllegalArgumentException e) {
		}
		// immeds
		assertEquals(0x0, operand(">b").createMachineOperand(factory).getBits());
		// immeds
		LLOperand op = new LLCountOperand(0xb);
		assertEquals(0xb, op.createMachineOperand(factory).getBits());
	}

	public void testParse() throws Exception {
		CPU.writeWord(0, (short) 0x100);
		RawInstruction minst = InstTable9900.decodeInstruction(CPU.readWord(0), 0, CPU);
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
		Random random = new Random(0x12345);
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
		
		RawInstruction minst = InstTable9900.decodeInstruction(CPU.readWord(0), 0, CPU);
		InstTable9900.coerceOperandTypes(minst);
		System.out.println(minst + " = " + HexUtils.toHex4(minst.opcode));
		
		RawInstruction stdinst = createInstruction(0, minst.toString());
		
		short[] words = InstTable9900.encode(stdinst);
		assertEquals(minst.toString(), minst.size, words.length*2);
		
		for (int i = 0; i < minst.size; i += 2) {
			short exp = CPU.readWord(i);
			if (i == 0)
				exp = (short) InstTable9900.coerceInstructionOpcode(minst.getInst(), exp);
			assertEquals(minst.toString() + "@" + minst.size, Integer.toHexString(exp), Integer.toHexString(words[i/2]));
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
		IInstruction[] insts = instStage.parse("foo", string);
		assertNotNull(insts);
		assertEquals(1, insts.length);
		
		IInstruction[] irealInsts = ((AssemblerInstruction) insts[0]).resolve(assembler, null, true);
		assertEquals(1, irealInsts.length);
		assertTrue(irealInsts[0] instanceof LLInstruction);
		
		RawInstruction realInst = assembler.getInstructionFactory().createRawInstruction(
				((LLInstruction) irealInsts[0]));
		short[] words = InstTable9900.encode(realInst);
		assertEquals(realInst.size, words.length * 2);
		
		realInst.pc = 0;
		for (int i = 0; i < words.length; i++)
			CPU.flatWriteWord(i*2, words[i]);
		
		RawInstruction minst = InstTable9900.decodeInstruction(words[0], 0, CPU);
		InstTable9900.coerceOperandTypes(minst);
		InstTable9900.coerceOperandTypes(realInst);
		
		assertEquals(realInst.toString(), minst.toString());
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

	protected Symbol createSymbol(String name) {
		return new Symbol(assembler.getSymbolTable(), name);
	}
	
	protected Symbol createSymbol(String name, int val) {
		return new Symbol(assembler.getSymbolTable(), name, val);
	}
	
	public void testAsmOpSymbols() throws Exception {
		testAsmOp("R5", new NumberOperand(5));
		testAsmOp("R15", new NumberOperand(0xf));
		
		testAsmOp("@FOO", new AddrOperand(new SymbolOperand(createSymbol("FOO"))));
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
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStage9900(assembler);

		Operand op = parseOperand(opStage, "@BUFFER(R5)");
		Symbol sym = assembler.getSymbolTable().findSymbol("BUFFER");
		assertNotNull(sym);
		assertEquals(new RegOffsOperand(new SymbolOperand(sym),
				new NumberOperand(5)), op);
		
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
				new Symbol[] { createSymbol("sym", 0x1234) });
		
		testResolve("jmp foo",
				"jmp >104",
				new Symbol[] { createSymbol("foo", 0x104) },
				0x100);
		
		testResolve("jmp foo+2",
				"jmp >106",
				new Symbol[] { createSymbol("foo", 0x104) },
				0x100);
		
		testResolve("jmp foo+foo",
				"jmp >208",
				new Symbol[] { createSymbol("foo", 0x104) },
				0x100);
	}
	
	
	/** Test resolving an instruction's operands */
	private void testResolve(String string, String stdInst, Symbol[] symbols, int pc) throws ParseException, ResolveException {
		for (Symbol symbol : symbols)
			assembler.getSymbolTable().addSymbol(symbol);
		assembler.setPc((short) pc);
		
		System.out.println("AsmInst: " + string);
		IInstruction[] insts = asmInstStage.parse("foo", string);
		assertEquals(1, insts.length);

		IInstruction[] irealInsts = ((AssemblerInstruction) insts[0]).resolve(assembler, null, true);
		assertEquals(1, irealInsts.length);
		assertTrue(irealInsts[0] instanceof LLInstruction);
		RawInstruction realinst = assembler.getInstructionFactory().createRawInstruction(
				((LLInstruction) irealInsts[0]));
		InstTable9900.coerceOperandTypes(realinst);
		
		RawInstruction stdinst = createInstruction(pc, stdInst);
		InstTable9900.coerceOperandTypes(stdinst);
		if (!stdinst.equals(realinst))
			assertEquals(stdinst, realinst);
	}
	private void testResolve(String string, String stdInst, Symbol[] symbols) throws ParseException, ResolveException {
		testResolve(string, stdInst, symbols, 0);
	}
		
	private void testResolve(String inst, String stdInst) throws ParseException, ResolveException {
		testResolve(inst, stdInst, new Symbol[0]);
	}
	private void testResolve(String inst) throws ParseException, ResolveException {
		testResolve(inst, inst);
	}
	
	public void testEncode2() throws Exception {
		_testEncode(asmInstStage);
		
		assertBadInst(asmInstStage, "data $ 1-\n");

	}
	public void testAsmInst() throws Exception {
		// not the simpler parser above
		testAsmInst("movb @>9800, 1");
		testAsmInst("li r5, 11");
		testAsmInst("movb *r5+, @foo");
	}

	private void testAsmInst(String string) throws ParseException {
		System.out.println("AsmInst: " + string);
		IInstruction[] insts = asmInstStage.parse("foo", string);
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
	
	public void testAssemblerProgResolve0() throws Exception {
		String text =
			" aorg >100\n"+
			" jmp $\n"
			;
		
		testFileContent(text,
				0x100,
				new String[] { 
				"jmp $"},
				new Symbol[] {  });
		
	}
	public void testAssemblerProgResolve1() throws Exception {
		// we allow adding constants to forward symbols
		// (by storing the offset in the LLOperand immed before the symbol is resolved)
		// but otherwise math may only be done on predefined symbols
		String text =
			" aorg >100\n"+
			" li r5, 44\n"+ //100
			"lab0: mov r5,r5\n"+ //104
			" jeq lab1 + 2\n"+ //106
			" movb r1,@buffer+16(r5)\n"+//108
			"lab1: jmp lab0\n"+ //10c
			" rt\n"+ //10e
			" jmp $\n"+ //110
			"buffer equ >8300\n"
			;
		
		testFileContent(text,
				0x100,
				new String[] { "li r5, 44",
				"mov r5, r5", 
				"jeq $+8",
				"movb r1,@>8310(r5)",
				"jmp $-8", 
				"b *11",
				"jmp $"},
				new Symbol[] { createSymbol("buffer", 0x8300),
					createSymbol("lab0", 0x104),
					createSymbol("lab1", 0x10c) });
		
	}

	public void testAssemblerProgResolve2() throws Exception {
		// we allow adding constants to forward symbols
		// (by storing the offset in the LLOperand immed before the symbol is resolved)
		// but otherwise math may only be done on predefined symbols
		String text =
			" aorg >100\n"+
			" dw 5,label\n"+
			" dw buffer+4,44\n"+
			"label:\n"+
			"size dw $->100\n"+
			"buffer equ >8300\n"+
			" dw -buffer,4+4*7+>100\n"
			;
		
		testFileContent(text,
				0x100,
				new String[] { },
				new Symbol[] { createSymbol("buffer", 0x8300),
					createSymbol("label", 0x108) });
		
	}
	public void testAssemblerProgResolve3() throws Exception {
		String text =
			" aorg >100\n"+
			" li r1,1\n"+
			" srl 0,1\n"+
			" jmp $\n"
			;
		
		testFileContent(text,
				0x100,
				new String[] {
				"li r1,1",
				"srl 0,1",
				"jmp $"},
				new Symbol[] {  });
		
	}
	public void testAssemblerProgDirectives2() throws Exception {
		String text =
			" aorg >101\n"+
			" even\n"+
			"foo: equ $\n"+
			" aorg >201\n"+
			"odd: dw 5\n"
			;
		
		testFileContent(text,
				0x202,
				new String[] { },
				new Symbol[] { createSymbol("foo", 0x102),
					createSymbol("odd", 0x201) });
		
	}
	public void testAssemblerProgDirectives2b() throws Exception {
		String text =
			" aorg >100\n"+
			" mov r1, r2\n"+
			" db 1\n"+
			"foo mov r3,r4\n"
			;
		
		testFileContent(text,
				0x100,
				new String[] { 
				"mov r1,r2",
				"db >1",
				"mov r3,r4",
				},
				new Symbol[] { createSymbol("foo", 0x104) });
		
	}
	@SuppressWarnings("unchecked")
	public void testAssemblerProgDirectives3() throws Exception {
		String text =
			" aorg >a000\n"+
			" db 1,2,3,'*'\n"+
			// a004
			" db \"hello!\",0\n"+
			// a00b, even to a00c
			"lab: mov r5,r5\n"
			;
		
		testFileContent(text,
				0xa00c,
				new String[] { 
				"mov r5,r5"
				
			},
				new Symbol[] { 
					createSymbol("lab", 0xa00c) });
		
		List<AssemblerOperand> ops = (List) Collections.singletonList(new StringOperand("hello!"));
		BaseAssemblerInstruction dbInst = new DefineByteDirective(ops);
		IInstruction[] resolve = dbInst.resolve(assembler, null, true);
		byte[] bytes = ((Directive) resolve[0]).getBytes(assembler.getInstructionFactory());
		assertEquals(6, bytes.length);
		assertEquals('h', bytes[0]);
		assertEquals('e', bytes[1]);
		assertEquals('l', bytes[2]);
		assertEquals('l', bytes[3]);
		assertEquals('o', bytes[4]);
		assertEquals('!', bytes[5]);
		
	}
	
	public void testAssemblerProgDirectives4() throws Exception {
		String text =
			" aorg >2000\n"+
			"foo1 bss >40\n"+
			"foo2 equ $\n"
			
			;
		
		testFileContent(text,
				0x2000,
				new String[] { 
				
			},
				new Symbol[] { 
					createSymbol("foo1", 0x2000), 
					createSymbol("foo2", 0x2040) 
					});
		
	}
	public void testAssemblerProgDirectives5() throws Exception {
		String text =
			" aorg >100\n"+
			"h00 equ $\n"+
			"h1 data 1\n"+
			"h01 equ $-1\n"
			
			;
		
		testFileContent(text,
				0x100,
				new String[] { 
			},
				new Symbol[] { 
					createSymbol("h00", 0x100), 
					createSymbol("h1", 0x100), 
					createSymbol("h01", 0x101) 
					});
		
	}

	public void testAssemblerProgDirectives6() throws Exception {
		Symbol bufmaskSymbol = new Equate(assembler.getSymbolTable(), "bufmask", 0x20);
		List<AssemblerOperand> ops = new ArrayList<AssemblerOperand>();
		ops.add(new UnaryOperand('-',
				new SymbolOperand(bufmaskSymbol)));
		BaseAssemblerInstruction dbInst = new DefineByteDirective(ops);
		BaseAssemblerInstruction dwInst = new DefineWordDirective(ops);
		List<IInstruction> insts = new ArrayList<IInstruction>();
		insts.add(dbInst);
		insts.add(dwInst);
		insts = assembler.resolve(insts);
		insts = assembler.resolve(insts);
		IInstruction resolve = insts.get(0);
		byte[] bytes = ((Directive) resolve).getBytes(assembler.getInstructionFactory());
		assertEquals(1, bytes.length);
		assertEquals((byte)0xe0, bytes[0]);
		
		resolve = insts.get(1);
		bytes = ((Directive) resolve).getBytes(assembler.getInstructionFactory());
		assertEquals(2, bytes.length);
		assertEquals((byte)0xff, bytes[0]);
		assertEquals((byte)0xe0, bytes[1]);

	}

	public void testAssemblerProgDirectives7() throws Exception {
		String text =
			" aorg >100\n"+
			" li r0,0\n"+
			" ai r1,0\n"+
			"h00 equ $\n"+
			""
			;
		
		
		testFileContent(text,
				0x100,
				new String[] { 
			},
				new Symbol[] { 
					createSymbol("h00", 0x102), 
					},
					true);

	}
	
	
	
	private void testFileContent(String text, int pc, String[] stdInsts, Symbol[] symbols) throws Exception {
		testFileContent(text, pc, stdInsts, symbols, false);
	}
	private void testFileContent(String text, int pc, String[] stdInsts, Symbol[] symbols, boolean optimize) throws Exception {
		assembler.pushContentEntry(new ContentEntry("main.asm", text));
		List<IInstruction> asminsts = assembler.parse();
		List<IInstruction> realinsts = assembler.resolve(asminsts);
		if (optimize) {
			realinsts = assembler.optimize(realinsts);
			realinsts = assembler.fixupJumps(realinsts);
		}
		testGeneratedContent(assembler, pc, stdInsts, symbols, realinsts);
	}
	
	public void testJumpRelative1() throws Exception {
		// we allow jumping to a set of jump targets either before or behind the PC
		String text =
			" aorg >100\n"+
			"$1 mov 5,5\n"+ //100
			" jne $2+\n"+ //102
			" dec 5\n"+ //104
			" jmp $1-\n"+ //106
			"$2: rt\n" //108
			;
		
		testFileContent(text,
				0x100,
				new String[] { 
					"mov 5,5",
					"jne >108",
					"dec 5",
					"jmp >100",
					"b *11"
				},
				new Symbol[] {  });		
	}
	
	public void testJumpRelative2() throws Exception {
		// we allow jumping to a set of jump targets either before or behind the PC
		String text =
			" aorg >100\n"+
			"$1 mov 5,5\n"+
			" jne $2+\n"+ //102
			" dec 5\n"+ //104
			" jmp $1-\n"+ //106
			"$2 mov *6+, 5\n"+ //108
			"$1 mov *6, 7\n"+ //10a
			" dec 7\n"+ //10c
			" jne $2-\n"+ //10e
			" inct 6\n"+ //110
			" jne $1-\n"+ //112
			" jgt $2+\n"+ //114
			"$2: rt\n" //116
			;
		
		testFileContent(text,
				0x100,
				new String[] { 
					"mov 5,5",
					"jne >108",
					"dec 5",
					"jmp >100",
					"mov *6+,5",
					"mov *6,7",
					"dec 7",
					"jne >108",
					"inct 6",
					"jne >10a",
					"jgt >116",
					"b *11"
				},
				new Symbol[] {  });		
	}
}
