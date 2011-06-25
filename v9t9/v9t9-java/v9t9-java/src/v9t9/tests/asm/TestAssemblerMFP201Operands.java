package v9t9.tests.asm;

import java.io.IOException;

import v9t9.engine.cpu.MachineOperandMFP201;
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
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.BinaryOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.PcRelativeOperand;
import v9t9.tools.asm.assembler.operand.hl.RegDecOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.ScaledRegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.SymbolOperand;
import v9t9.tools.asm.assembler.operand.hl.UnaryOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegDecOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;
import v9t9.tools.asm.assembler.operand.ll.LLScaledRegOffsOperand;

public class TestAssemblerMFP201Operands extends BaseTest {

	MachineOperandFactoryMFP201 mopFactory = new MachineOperandFactoryMFP201();
	
	Assembler assembler = new Assembler();
	{
		assembler.setProcessor(Assembler.PROC_MFP201);
	}
	MachineOperandParserStageMFP201 mopStage = new MachineOperandParserStageMFP201();
	AssemblerOperandParserStageMFP201 aopStage = new AssemblerOperandParserStageMFP201(assembler);
	OperandParser opParser = new OperandParser();
	{
		opParser.appendStage(aopStage);
	}
	//StandardInstructionParserStageMFP201 asmInstStage = new StandardInstructionParserStageMFP201(opParser);
	
	protected LLOperand operand(String string) throws Exception {
		return (LLOperand) parseOperand(mopStage, string);
	}

	public void testAsmOpParserNumbers() throws Exception {
		testAsmOp("5", new NumberOperand(5), new LLImmedOperand(5));
		testAsmOp("#5", new NumberOperand(5), new LLImmedOperand(5));
		testAsmOp(">12F", new NumberOperand(0x12f) , new LLImmedOperand(0x12f));
		testAsmOp("#>12F", new NumberOperand(0x12f) , new LLImmedOperand(0x12f));
		testAsmOp("123", new NumberOperand(123), new LLImmedOperand(123));
		
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
		testAsmOp("R5", new RegisterOperand(new NumberOperand(5)), new LLRegisterOperand(5));
		testAsmOp("R15", new RegisterOperand(new NumberOperand(15)), new LLRegisterOperand(15));
		testAsmOp("SR", new RegisterOperand(new NumberOperand(15)), new LLRegisterOperand(15));
		testAsmOp("PC", new RegisterOperand(new NumberOperand(14)), new LLRegisterOperand(14));
		testAsmOp("SP", new RegisterOperand(new NumberOperand(13)), new LLRegisterOperand(13));
		
		testAsmOp("@FOO", new RegOffsOperand(
				new SymbolOperand(createSymbol("FOO")),
				new NumberOperand(MachineOperandMFP201.PC)));

		testAsmOp("&FOO", new RegOffsOperand(
				new SymbolOperand(createSymbol("FOO")),
				new NumberOperand(MachineOperandMFP201.SR)));
	}

	public void testAsmOpRegisters() throws Exception {
		testAsmOp("*R5", new RegIndOperand(new RegisterOperand(new NumberOperand(5))),
				new LLRegIndOperand(5));
		testAsmOp("*R0+", new RegIncOperand(new RegisterOperand(new NumberOperand(0))),
				new LLRegIncOperand(0));
		testAsmOp("*0+", new RegIncOperand(new NumberOperand(0)),
				new LLRegIncOperand(0));
		testAsmOp("*PC-", new RegDecOperand(new RegisterOperand(new NumberOperand(14))),
				new LLRegDecOperand(14));
		testAsmOp("*>f", new RegIndOperand(new NumberOperand(15)));
		testAsmOp("*1-", new RegDecOperand(new NumberOperand(1)));
		testAsmOp("* R5 +", new RegIncOperand(new RegisterOperand(new NumberOperand(5))),
				new LLRegIncOperand(5));
		
		testBadAsmOp("@0+");
		testBadAsmOp("*+");
		testBadAsmOp("*++");
	}

	public void testAsmOpAddrs() throws Exception {
		testAsmOp(" &0", new RegOffsOperand(new NumberOperand(0),
				new NumberOperand(MachineOperandMFP201.SR)),
				new LLRegOffsOperand(null, MachineOperandMFP201.SR, 0));
		
		testAsmOp("&(0)", new RegOffsOperand(new NumberOperand(0),
				new NumberOperand(MachineOperandMFP201.SR)));
		testAsmOp("&>FfFf", new RegOffsOperand(new NumberOperand(65535),
				new NumberOperand(MachineOperandMFP201.SR)),
				new LLRegOffsOperand(null, MachineOperandMFP201.SR, -1));
		testAsmOp("&-4", new RegOffsOperand(new UnaryOperand('-', new NumberOperand(4)),
				new NumberOperand(MachineOperandMFP201.SR)),
				new LLRegOffsOperand(null, MachineOperandMFP201.SR, -4));
		testAsmOp("@66(R0)", new RegOffsOperand(new NumberOperand(66), 
				new RegisterOperand(new NumberOperand(0))),
				new LLRegOffsOperand(null, 0, 66));
		testAsmOp("@12(0)", new RegOffsOperand(new NumberOperand(12), 
				new NumberOperand(0)),
				new LLRegOffsOperand(null, 0, 12));
		testAsmOp("\t@>f(PC)", new RegOffsOperand(new NumberOperand(15),
				new RegisterOperand(new NumberOperand(14))),
				new LLRegOffsOperand(null, 14, 15));
		testAsmOp("@8(R15)", new RegOffsOperand(new NumberOperand(8),
				new RegisterOperand(new NumberOperand(15))),
				new LLRegOffsOperand(null, 15, 8));
		
		assertEquals("@>0000(R15)", new LLRegOffsOperand(null, 15, 0).toString());
		assertEquals("&>0000", MachineOperandMFP201.createGeneralOperand(
				MachineOperandMFP201.OP_OFFS, 15, 0).toString());
		assertEquals("@>1234(R14)", new LLRegOffsOperand(null, 14, 0x1234).toString());
		assertEquals("@>1234(PC)", MachineOperandMFP201.createGeneralOperand(
				MachineOperandMFP201.OP_OFFS, 14, 0x1234).toString());
		

		// absolute addresses require '&'
		testBadAsmOp(" @0");
		testBadAsmOp("@(0)");
		testBadAsmOp("@>FfFf");
		testBadAsmOp("@-4");
		
		// no # inside operands
		testBadAsmOp("&#-4");
		testBadAsmOp("@#66(R0)");

	}
	public void testAsmOpSfoAddrs() throws Exception {
		testAsmOp("@10(SP+R0)", new ScaledRegOffsOperand(
				new NumberOperand(10),
				new RegisterOperand(new NumberOperand(13)),
				new RegisterOperand(new NumberOperand(0)),
				new NumberOperand(1)
			),
			new LLScaledRegOffsOperand(null, 10, 13, 0, 1)
		);
		testAsmOp("@-10(R5*4)", new ScaledRegOffsOperand(
				new UnaryOperand('-', new NumberOperand(10)),
				null,
				new RegisterOperand(new NumberOperand(5)),
				new NumberOperand(4)
			),
			new LLScaledRegOffsOperand(null, -10, MachineOperandMFP201.SR, 5, 4)
		);
		testAsmOp("@123(SP+R4*128)", new ScaledRegOffsOperand(
				new NumberOperand(123),
				new RegisterOperand(new NumberOperand(13)),
				new RegisterOperand(new NumberOperand(4)),
				new NumberOperand(128)
			),
			new LLScaledRegOffsOperand(null, 123, 13, 4, 128)
		);
		testAsmOp("@123(R4*128+SP)", new ScaledRegOffsOperand(
				new NumberOperand(123),
				new RegisterOperand(new NumberOperand(13)),
				new RegisterOperand(new NumberOperand(4)),
				new NumberOperand(128)
			),
			new LLScaledRegOffsOperand(null, 123, 13, 4, 128)
		);
		
		testBadAsmOp("@123(R4*#128+SP)");
	}
	
	
	public void testAsmSymbols() throws Exception {
		AssemblerOperandParserStage opStage = new AssemblerOperandParserStageMFP201(assembler);

		Operand op = parseOperand(opStage, "@BUFFER(R5)");
		Symbol sym = assembler.getSymbolTable().findSymbol("BUFFER");
		assertNotNull(sym);
		assertEquals(new RegOffsOperand(new SymbolOperand(sym),
				new RegisterOperand(new NumberOperand(5))), op);
		
	}

	public void testAsmOpJumps() throws Exception {
		testAsmOp("$", new PcRelativeOperand(),
				new LLPCRelativeOperand(null, 0));
		
		testAsmOp("$+10", new BinaryOperand('+', new PcRelativeOperand(), new NumberOperand(10)),
				new LLPCRelativeOperand(null, 10));
	}
	

	private void testBadAsmOp(String string) {
		try {
			Operand op = parseOperand(aopStage, string);
			fail("asm: Expected error, got " + op);
		} catch (ParseException e) {
			
		}
		try {
			Operand op = parseOperand(mopStage, string);
			if (op != null)
				fail("machine: Expected error, got " + op);
		} catch (ParseException e) {
			
		}
	}

	/** Test a symbolic operand, which is a superset of machine operands */
	/** Test a machine operand with machine and assembler parser */
	private void testAsmOp(String string, Operand expOp, Operand expMop) throws ParseException, IOException {
		testAsmOp(string, expOp);
		testMachineOp(string, expMop);
	}
	private void testMachineOp(String string, Operand expMop) throws ParseException, IOException {
		System.out.println("MachineOp: " + string);
		Operand op = parseOperand(mopStage, string);
		assertEquals("machine", expMop, op);
		op = parseOperand(mopStage, string.toLowerCase());
		assertEquals("machine lwc", expMop, op);
	}
	private void testAsmOp(String string, Operand expOp) throws ParseException, IOException {
		System.out.println("AsmOp: " + string);
		Operand op = parseOperand(aopStage, string);
		assertEquals("asm", expOp, op);
		op = parseOperand(aopStage, string.toLowerCase());
		assertEquals("asm lwc", expOp, op);
		
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
		testResAsmOp("*R5", new LLRegIndOperand(5));
		testResAsmOp("*R0+", new LLRegIncOperand(0));
		testResAsmOp("*0+", new LLRegIncOperand(0));
		testResAsmOp("*PC-", new LLRegDecOperand(14));
		testResAsmOp("*>f", new LLRegIndOperand(15));
		testResAsmOp("*1-", new LLRegDecOperand(1));
		testResAsmOp("* R5 +", new LLRegIncOperand(5));
		
		testBadResAsmOp("@0+");
		testBadResAsmOp("*+");
		testBadResAsmOp("*++");
	}
	
	public void testResolvedAsmOpAddrs() throws Exception {
		testResAsmOp(" &0", new LLRegOffsOperand(null, 15, 0));
		testResAsmOp("&(0)", new LLRegOffsOperand(null, 15, 0));
		testResAsmOp("&>FfFf", new LLRegOffsOperand(null, 15, 65535));
		testResAsmOp("&-4", new LLRegOffsOperand(null, 15, -4));
		testResAsmOp("@66(R0)", new LLRegOffsOperand(null, 0, 66)); 
		testResAsmOp("@12(0)", new LLRegOffsOperand(null, 0, 12));
		testResAsmOp("\t@>f(PC)", new LLRegOffsOperand(null, 14, 15));
		testResAsmOp("@8(R15)", new LLRegOffsOperand(null, 15, 8));
		
		testBadResAsmOp("@0");
		
	}
	
	public void testResolvedAsmOpSfoAddrs() throws Exception {
		testResAsmOp("@10(SP+R0)", new LLScaledRegOffsOperand(
				null, 10, 13, 0, 1));
		testResAsmOp("@-10(R5*4)", new LLScaledRegOffsOperand(
				null, -10, 15, 5, 4));
		testResAsmOp("@123(SP+R4*128)", new LLScaledRegOffsOperand(
				null, 123, 13, 4, 128));
		testResAsmOp("@123(R4*128+SP)", new LLScaledRegOffsOperand(
				null, 123, 13, 4, 128));
		
		testBadResAsmOp("@-10(4*R5)");
		testBadResAsmOp("@10(SP+R4*100)");
		testBadResAsmOp("@10(SP+R4*256)");
		testBadResAsmOp("@10(R5*128+R4*256)");
	}
	
	protected MachineOperandMFP201 getMachineOperand(String string) throws Exception {
		return ((MachineOperandMFP201) operand(string).createMachineOperand(mopFactory));
	}

	public void testMachineOpImmeds() throws Exception {
		MachineOperandMFP201 mop;
		mop = getMachineOperand("456");
		assertEquals(MachineOperandMFP201.createImmediate(456),
				mop);
		mop = getMachineOperand(">123");
		assertEquals(MachineOperandMFP201.createImmediate(0x123),
				mop);

	}
	public void testMachineOpRegs() throws Exception {
		MachineOperandMFP201 mop;
		mop = getMachineOperand("R5");
		assertEquals(MachineOperandMFP201.createRegisterOperand(5), mop);
		mop = getMachineOperand("*PC");
		assertEquals(MachineOperandMFP201.createGeneralOperand(
				MachineOperandMFP201.OP_IND, 14), mop);
		mop = getMachineOperand("*SP+");
		assertEquals(MachineOperandMFP201.createGeneralOperand(
				MachineOperandMFP201.OP_INC, 13), mop);
		mop = getMachineOperand("*R0-");
		assertEquals(MachineOperandMFP201.createGeneralOperand(
				MachineOperandMFP201.OP_DEC, 0), mop);
		mop = getMachineOperand("SR");
		assertEquals(MachineOperandMFP201.createGeneralOperand(
				MachineOperandMFP201.OP_REG, 15), mop);
	}
	public void testMachineOpRegOffs() throws Exception {
		MachineOperandMFP201 mop;
		mop = getMachineOperand("&11");
		assertEquals(MachineOperandMFP201.createGeneralOperand(
				MachineOperandMFP201.OP_OFFS, MachineOperandMFP201.SR, 11), mop);
	}
}
