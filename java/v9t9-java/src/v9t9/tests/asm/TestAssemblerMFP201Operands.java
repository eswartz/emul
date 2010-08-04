package v9t9.tests.asm;

import java.io.IOException;

import v9t9.engine.cpu.Operand;
import v9t9.tests.BaseTest;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.AssemblerOperandParserStage;
import v9t9.tools.asm.assembler.AssemblerOperandParserStageMFP201;
import v9t9.tools.asm.assembler.AssemblerTokenizer;
import v9t9.tools.asm.assembler.IOperandParserStage;
import v9t9.tools.asm.assembler.MachineOperandFactoryMFP201;
import v9t9.tools.asm.assembler.MachineOperandParserStageMFP201;
import v9t9.tools.asm.assembler.OperandParser;
import v9t9.tools.asm.assembler.ParseException;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.Symbol;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegDecOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.ScaledRegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.SymbolOperand;
import v9t9.tools.asm.assembler.operand.hl.UnaryOperand;
import v9t9.tools.asm.assembler.operand.ll.LLAddrOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegDecOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.assembler.operand.ll.LLScaledRegOffsOperand;

public class TestAssemblerMFP201Operands extends BaseTest {

	MachineOperandFactoryMFP201 opFactory = new MachineOperandFactoryMFP201();
	
	Assembler assembler = new Assembler();
	{
		assembler.setProcessor(Assembler.PROC_MFP201);
	}
	OperandParser opParser = new OperandParser();
	{
		opParser.appendStage(new AssemblerOperandParserStageMFP201(assembler));
	}
	MachineOperandParserStageMFP201 opStage = new MachineOperandParserStageMFP201();
	//StandardInstructionParserStageMFP201 asmInstStage = new StandardInstructionParserStageMFP201(opParser);
	
	protected LLOperand operand(String string) throws Exception {
		return (LLOperand) parseOperand(opStage, string);
	}

	public void testAsmOpParserNumbers() throws Exception {
		testAsmOp("#5", new NumberOperand(5));
		testAsmOp("5", new NumberOperand(5));
		testAsmOp("#>12F", new NumberOperand(0x12f));
		testAsmOp(">12F", new NumberOperand(0x12f));
		testAsmOp("#123", new NumberOperand(123));
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
		testAsmOp("R5", new RegisterOperand(new NumberOperand(5)));
		testAsmOp("R15", new RegisterOperand(new NumberOperand(15)));
		testAsmOp("SR", new RegisterOperand(new NumberOperand(15)));
		testAsmOp("PC", new RegisterOperand(new NumberOperand(14)));
		testAsmOp("SP", new RegisterOperand(new NumberOperand(13)));
		
		testAsmOp("@FOO", new AddrOperand(new SymbolOperand(createSymbol("FOO"))));
	}

	public void testAsmOpRegisters() throws Exception {
		testAsmOp("*R5", new RegIndOperand(new RegisterOperand(new NumberOperand(5))));
		testAsmOp("*R0+", new RegIncOperand(new RegisterOperand(new NumberOperand(0))));
		testAsmOp("*0+", new RegIncOperand(new NumberOperand(0)));
		testAsmOp("*PC-", new RegDecOperand(new RegisterOperand(new NumberOperand(14))));
		testAsmOp("*>f", new RegIndOperand(new NumberOperand(15)));
		testAsmOp("*#1", new RegIndOperand(new NumberOperand(1)));
		testAsmOp("* R5 +", new RegIncOperand(new RegisterOperand(new NumberOperand(5))));
		
		testBadAsmOp("@0+");
		testBadAsmOp("*+");
		testBadAsmOp("*++");
	}

	public void testAsmOpAddrs() throws Exception {
		testAsmOp(" @0", new AddrOperand(new NumberOperand(0)));
		testAsmOp("@(0)", new AddrOperand(new NumberOperand(0)));
		testAsmOp("@>FfFf", new AddrOperand(new NumberOperand(65535)));
		testAsmOp("@-4", new AddrOperand(new UnaryOperand('-', new NumberOperand(4))));
		testAsmOp("@66(R0)", new RegOffsOperand(new NumberOperand(66), 
				new RegisterOperand(new NumberOperand(0))));
		testAsmOp("@12(0)", new RegOffsOperand(new NumberOperand(12), 
				new NumberOperand(0)));
		testAsmOp("\t@>f(PC)", new RegOffsOperand(new NumberOperand(15),
				new RegisterOperand(new NumberOperand(14))));
		testAsmOp("@8(R15)", new RegOffsOperand(new NumberOperand(8),
				new RegisterOperand(new NumberOperand(15))));
		
	}
	public void testAsmOpSfoAddrs() throws Exception {
		testAsmOp("@10(SP+R0)", new ScaledRegOffsOperand(
				new NumberOperand(10),
				new RegisterOperand(new NumberOperand(13)),
				new RegisterOperand(new NumberOperand(0)),
				new NumberOperand(1)
		));
		testAsmOp("@-10(R5*4)", new ScaledRegOffsOperand(
				new UnaryOperand('-', new NumberOperand(10)),
				null,
				new RegisterOperand(new NumberOperand(5)),
				new NumberOperand(4)
		));
		testAsmOp("@-10(4*R5)", new ScaledRegOffsOperand(
				new UnaryOperand('-', new NumberOperand(10)),
				null,
				new RegisterOperand(new NumberOperand(5)),
				new NumberOperand(4)
		));
		testAsmOp("@123(SP+R4*128)", new ScaledRegOffsOperand(
				new NumberOperand(123),
				new RegisterOperand(new NumberOperand(13)),
				new RegisterOperand(new NumberOperand(4)),
				new NumberOperand(128)
		));
		testAsmOp("@123(R4*128+SP)", new ScaledRegOffsOperand(
				new NumberOperand(123),
				new RegisterOperand(new NumberOperand(13)),
				new RegisterOperand(new NumberOperand(4)),
				new NumberOperand(128)
		));
		
		// TODO: these are okay here, but not once resolved
		/*
		testBadAsmOp("@10(SP+R4*100)");
		testBadAsmOp("@10(SP+R4*256)");
		testBadAsmOp("@10(R5*128+R4*256)");
		*/
	}
	
	
	public void testAsmSymbols() throws Exception {
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStageMFP201(assembler);

		Operand op = parseOperand(opStage, "@BUFFER(R5)");
		Symbol sym = assembler.getSymbolTable().findSymbol("BUFFER");
		assertNotNull(sym);
		assertEquals(new RegOffsOperand(new SymbolOperand(sym),
				new RegisterOperand(new NumberOperand(5))), op);
		
	}


	private void testBadAsmOp(String string) {
		try {
			AssemblerOperandParserStage opStage = new AssemblerOperandParserStageMFP201(assembler);
			Operand op = parseOperand(opStage, string);
			fail("Expected error, got " + op);
		} catch (ParseException e) {
			
		}
	}

	private void testAsmOp(String string, Operand expOp) throws ParseException, IOException {
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStageMFP201(assembler);
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
	
	private void testResAsmOp(String string, Operand expOp) throws ParseException, IOException, ResolveException {
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStageMFP201(assembler);
		System.out.println("AsmOp: " + string);
		Operand op = parseOperand(opStage, string);
		op = ((AssemblerOperand) op).resolve(assembler, null);
		assertEquals(expOp, op);
	}

	private void testBadResAsmOp(String string) {
		try {
			AssemblerOperandParserStage opStage = new AssemblerOperandParserStageMFP201(assembler);
			Operand op = parseOperand(opStage, string);
			op = ((AssemblerOperand) op).resolve(assembler, null);
			fail("Expected error, got " + op);
		} catch (ParseException e) {
			
		} catch (ResolveException e) {
			
		}
	}

	public void testResolvedAsmOpRegisters() throws Exception {
		testResAsmOp("*R5", new LLRegOffsOperand(5));
		testResAsmOp("*R0+", new LLRegIncOperand(0));
		testResAsmOp("*0+", new LLRegIncOperand(0));
		testResAsmOp("*PC-", new LLRegDecOperand(14));
		testResAsmOp("*>f", new LLRegOffsOperand(15));
		testResAsmOp("*#1", new LLRegOffsOperand(1));
		testResAsmOp("* R5 +", new LLRegIncOperand(5));
		
		testBadResAsmOp("@0+");
		testBadResAsmOp("*+");
		testBadResAsmOp("*++");
	}
	
	public void testResolvedAsmOpAddrs() throws Exception {
		testResAsmOp(" @0", new LLAddrOperand(null, 0));
		testResAsmOp("@(0)", new LLAddrOperand(null, 0));
		testResAsmOp("@>FfFf", new LLAddrOperand(null, 65535));
		testResAsmOp("@-4", new LLAddrOperand(null, -4));
		testResAsmOp("@66(R0)", new LLRegOffsOperand(null, 0, 66)); 
		testResAsmOp("@12(0)", new LLRegOffsOperand(null, 0, 12));
		testResAsmOp("\t@>f(PC)", new LLRegOffsOperand(null, 14, 15));
		testResAsmOp("@8(R15)", new LLRegOffsOperand(null, 15, 8));
		
	}
	
	public void testResolvedAsmOpSfoAddrs() throws Exception {
		testResAsmOp("@10(SP+R0)", new LLScaledRegOffsOperand(
				null, 10, 13, 0, 0));
		testResAsmOp("@-10(R5*4)", new LLScaledRegOffsOperand(
				null, -10, 15, 5, 4));
		testResAsmOp("@-10(4*R5)", new LLScaledRegOffsOperand(
				null, -10, 15, 5, 4));
		testResAsmOp("@123(SP+R4*128)", new LLScaledRegOffsOperand(
				null, 123, 13, 4, 128));
		testResAsmOp("@123(R4*128+SP)", new LLScaledRegOffsOperand(
				null, 123, 13, 4, 128));
		
		testBadResAsmOp("@10(SP+R4*100)");
		testBadResAsmOp("@10(SP+R4*256)");
		testBadResAsmOp("@10(R5*128+R4*256)");
	}
	
}
