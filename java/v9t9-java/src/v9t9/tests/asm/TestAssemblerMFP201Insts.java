package v9t9.tests.asm;

import java.util.Arrays;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tests.BaseTest;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.AssemblerInstruction;
import v9t9.tools.asm.assembler.AssemblerOperandParserStageMFP201;
import v9t9.tools.asm.assembler.IInstructionParserStage;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.MachineOperandFactoryMFP201;
import v9t9.tools.asm.assembler.MachineOperandParserStageMFP201;
import v9t9.tools.asm.assembler.OperandParser;
import v9t9.tools.asm.assembler.ParseException;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.StandardInstructionParserStageMFP201;

public class TestAssemblerMFP201Insts extends BaseTest {

	MachineOperandParserStageMFP201 mopStage = new MachineOperandParserStageMFP201();
	MachineOperandFactoryMFP201 mopFactory = new MachineOperandFactoryMFP201();
	
	Assembler assembler = new Assembler();
	{
		assembler.setProcessor(Assembler.PROC_MFP201);
	}
	AssemblerOperandParserStageMFP201 aopStage = new AssemblerOperandParserStageMFP201(assembler);
	OperandParser aaopParser = new OperandParser();
	{
		aaopParser.appendStage(aopStage);
	}
	StandardInstructionParserStageMFP201 asmInstStage = new StandardInstructionParserStageMFP201(aaopParser);
	
	/*
	protected LLOperand operand(String string) throws Exception {
		return (LLOperand) parseOperand(aopStage, string);
	}

	protected MachineOperandMFP201 getMachineOperand(String string) throws Exception {
		return ((MachineOperandMFP201) operand(string).createMachineOperand(mopFactory));
	}
	*/

	public void testEncodeData() throws Exception {
		_testEncode("DATA 11", new byte[] { 0x00, 11 });
		_testEncode("DATA >1234", new byte[] { 0x12, 0x34 });
		_testEncode("BYTE >ff", new byte[] { (byte) 0xff });
		_testEncode("BYTE >1234", new byte[] { 0x34 });
	}
	public void testEncodeSimple1() throws Exception {
		_testEncode("BKPT", new byte[] { 0x00 });
		_testEncode("RET", new byte[] { 0x3E });	// POP PC
		_testEncode("RETI", new byte[] { 0x44, 0x3e }); // POP #2, PC
	}
	public void testEncodeSimple2() throws Exception {
		_testEncode("BR >1234", new byte[] { 0x0c, 0x02, 0x33 });
		_testEncode("BRA >1234", new byte[] { 0x0d, 0x12, 0x34 });
		_testEncode("CALL >0234", new byte[] { 0x0e, (byte) 0xF2, (byte) 0x33 });
		_testEncode("CALLA >0234", new byte[] { 0x0f, 0x02, 0x34 });
		
		_testEncode("BR $", new byte[] { 0x0c, (byte) 0xff, (byte) 0xff });
		assertBadInst("BR");
		assertBadInst("BR R15");
		assertBadInst("CALLA *R1+");
	}
	/** The immediate encodings of these should be selected */
	public void testEncodeImm1() throws Exception {
		
		// the non-.B variant tries to select the best size (word/byte)
		_testEncode("OR >100, R5", new byte[] { 0x08, 0x05, 0x01, 0x00 });
		_testEncode("OR >7f, R5", new byte[] { 0x09, 0x05, 0x7f });
		_testEncode("OR -12, R5", new byte[] { 0x09, 0x05, (byte) 0xf4 });
		
		_testEncode("AND >ff, R5", new byte[] { 0x08, 0x25, 0x00, (byte) 0xff });
		_testEncode("TST >11, R5", new byte[] { 0x09, 0x35, 0x11 });

		_testEncode("CMP >1234, PC", new byte[] { 0x08, (byte) 0xbe, 0x12, 0x34 });
		
		_testEncode("ADD? >11, R0", new byte[] { 0x09, (byte) 0x90, 0x11 });
		
		// the byte variant obeys
		_testEncode("ADD.B? #>11, R0", new byte[] { 0x09, (byte) 0x90, 0x11 });
		_testEncode("OR.B >1234, R12", new byte[] { 0x09, (byte) 0x0C, 0x34 });
		
		
		_testEncode("OR >100, R0", new byte[] { 0x08, (byte) 0x00, 0x01, 0x00 });
		_testEncode("OR.B #3, R0", new byte[] { 0x09, (byte) 0x00, 0x03 });
		_testEncode("OR? >100, R0", new byte[] { 0x08, (byte) 0x10, 0x01, 0x00 });
		_testEncode("OR.B? #3, R0", new byte[] { 0x09, (byte) 0x10, 0x03 });
		
		_testEncode("AND >100, R0", new byte[] { 0x08, (byte) 0x20, 0x01, 0x00 });
		_testEncode("AND.B #3, R0", new byte[] { 0x09, (byte) 0x20, 0x03 });
		_testEncode("TST >100, R0", new byte[] { 0x08, (byte) 0x30, 0x01, 0x00 });
		_testEncode("TST.B #3, R0", new byte[] { 0x09, (byte) 0x30, 0x03 });
		
		_testEncode("NAND >100, R0", new byte[] { 0x08, (byte) 0x40, 0x01, 0x00 });
		_testEncode("NAND.B #3, R0", new byte[] { 0x09, (byte) 0x40, 0x03 });
		_testEncode("TSTN >100, R0", new byte[] { 0x08, (byte) 0x50, 0x01, 0x00 });
		_testEncode("TSTN.B #3, R0", new byte[] { 0x09, (byte) 0x50, 0x03 });
		
		_testEncode("XOR >100, R0", new byte[] { 0x08, (byte) 0x60, 0x01, 0x00 });
		_testEncode("XOR.B #3, R0", new byte[] { 0x09, (byte) 0x60, 0x03 });
		_testEncode("XOR? >100, R0", new byte[] { 0x08, (byte) 0x70, 0x01, 0x00 });
		_testEncode("XOR.B? #3, R0", new byte[] { 0x09, (byte) 0x70, 0x03 });
		
		_testEncode("ADD >100, R0", new byte[] { 0x08, (byte) 0x80, 0x01, 0x00 });
		_testEncode("ADD.B #3, R0", new byte[] { 0x09, (byte) 0x80, 0x03 });
		_testEncode("ADD? >100, R0", new byte[] { 0x08, (byte) 0x90, 0x01, 0x00 });
		_testEncode("ADD.B? #3, R0", new byte[] { 0x09, (byte) 0x90, 0x03 });
		
		_testEncode("SUB >100, R0", new byte[] { 0x08, (byte) 0xa0, 0x01, 0x00 });
		_testEncode("SUB.B #3, R0", new byte[] { 0x09, (byte) 0xa0, 0x03 });
		_testEncode("CMP >100, R0", new byte[] { 0x08, (byte) 0xb0, 0x01, 0x00 });
		_testEncode("CMP.B #3, R0", new byte[] { 0x09, (byte) 0xb0, 0x03 });
		
		_testEncode("ADC >100, R0", new byte[] { 0x08, (byte) 0xc0, 0x01, 0x00 });
		_testEncode("ADC.B #4, R0", new byte[] { 0x09, (byte) 0xc0, 0x04 });
		_testEncode("ADC? >100, R0", new byte[] { 0x08, (byte) 0xd0, 0x01, 0x00 });
		_testEncode("ADC.B? #4, R0", new byte[] { 0x09, (byte) 0xd0, 0x04 });
		
		_testEncode("LDC >100, R0", new byte[] { 0x08, (byte) 0xe0, 0x01, 0x00 });
		_testEncode("LDC.B #0, R0", new byte[] { 0x09, (byte) 0xe0, 0x00 });
		_testEncode("LDC? >100, R0", new byte[] { 0x08, (byte) 0xf0, 0x01, 0x00 });
		_testEncode("LDC.B? #0, R0", new byte[] { 0x09, (byte) 0xf0, 0x00 });
		
		_testEncode("LDC >ff01, R1", new byte[] { 0x08, (byte) 0xe1, (byte) 0xff, 0x01 });
		_testEncode("LDC >7f, R14", new byte[] { 0x09, (byte) 0xee, 0x7f });
		
		assertBadInst("OR >10");
		assertBadInst("OR R5");
		assertBadInst("OR");
		assertBadInst("LDC?");
	}
	
	
	public void testEncode3OpWith3() throws Exception {
		
		_testEncode("OR R5, R12, R1", new byte[] { (byte) 0x85, (byte) 0xC1 });
		
		// use register for implicit constant in 2nd position
		// or select the implicit constant form if the operation is commutative
		_testEncode("OR R12, #1, R1", new byte[] { (byte) 0x8C, (byte) 0xD1 });
		_testEncode("OR #1, R12, R1", new byte[] { (byte) 0x8C, (byte) 0xD1 });
		_testEncode("AND R12, >8000, R1", new byte[] { (byte) 0x9C, (byte) 0xE1 });
		_testEncode("AND >8000, R12, R1", new byte[] { (byte) 0x9C, (byte) 0xE1 });
		_testEncode("NAND.B R12, >80, R1", new byte[] { 0x50, (byte) 0xAC, (byte) 0xE1 });
		_testEncode("NAND.B >80, R12, R1", new byte[] { 0x50, (byte) 0xAC, (byte) 0xE1 });
		_testEncode("XOR R12, >FFFF, R1", new byte[] { (byte) 0xBC, (byte) 0xF1 });
		_testEncode("XOR >FFFF, R12, R1", new byte[] { (byte) 0xBC, (byte) 0xF1 });
		_testEncode("OR.B R12, >FF, R1", new byte[] { 0x50, (byte) 0x8C, (byte) 0xF1 });
		_testEncode("OR.B >FF, R12, R1", new byte[] { 0x50, (byte) 0x8C, (byte) 0xF1 });
		
		// explicit constant in mem op position
		_testEncode("OR.B >1F, R12, R1", new byte[] { 0x5C, (byte) 0x8E, (byte) 0xC1, 0x1f });
		// ... autoselect byte form
		_testEncode("OR >1F, R12, R1", new byte[] { 0x5C, (byte) 0x8E, (byte) 0xC1, 0x1f });
		_testEncode("OR >1F80, R12, R1", new byte[] { 0x4C, (byte) 0x8E, (byte) 0xC1, 0x1f, (byte)0x80 });

		// ... but don't select byte form here, since it's accessing memory
		_testEncode("OR >1F, R12, *R1", new byte[] { 0x4E, (byte) 0x8E, (byte) 0xC1, 0x00, 0x1f });

		_testEncode("ADD SP, #2, SP", new byte[] { (byte) 0xCD, (byte) 0xED });
		_testEncode("ADD.B SP, #2, SP", new byte[] { 0x50, (byte) 0xCD, (byte) 0xED });
		_testEncode("ADD #2, SP, SP", new byte[] { (byte) 0xCD, (byte) 0xED });
		_testEncode("ADD.B #2, SP, SP", new byte[] { 0x50, (byte) 0xCD, (byte) 0xED });

		// A non-implicit immediate is not allowed in 2nd position, 
		// so this is converted to ADD ->1000, R7, R1.
		_testEncode("SUB R7, >1000, R4", new byte[] { 0x4C, (byte) 0xCE, (byte) 0x74, (byte) 0xf0, 0x00 });
		// this is also converted.  Note that the byte form is used, because it does
		// not affect the calculation (all reg ops, except for shifts, use the full reg anyway).
		_testEncode("SUB R7, 100, R4", new byte[] { 0x5C, (byte) 0xCE, (byte) 0x74, (byte) 0x9C });
		
		// no byte form here; accessing memory
		_testEncode("SUB R7, 100, *R4", new byte[] { 0x4E, (byte) 0xCE, (byte) 0x74, (byte) 0xff, (byte) 0x9C });

		// cannot convert, since this places the memory operand in the register-only position
		assertBadInst("SUB *R7, 100, R4");
		
		// swizzle operands if memory is in second position and
		// instruction is reverseable or commutative
		_testEncode("OR *R5, R12, R1", new byte[] { 0x48, (byte) 0x85, (byte) 0xC1 });
		_testEncode("OR R12, *R5, R1", new byte[] { 0x48, (byte) 0x85, (byte) 0xC1 });

		// SETO -> SUB 0/*SP*/, 1/*R13*/, R
		_testEncode("SUB 0, 1, R1", new byte[] { (byte) 0xEF, (byte) 0xD1 });
	
		// cannot reverse subtract
		assertBadInst("SUB R12, *R5, R1");
		// cannot have mem in second position
		assertBadInst("OR *R7, *R8, R4");

		// cannot have immed in dest
		assertBadInst("OR R7, R8, #10");
	}
	
	public void testEncode3OpWith2() throws Exception {
		// the first source operand becomes the dest operand
			// OR R1, R5, R1
		_testEncode("OR R5, R1", new byte[] { (byte) 0x81, (byte) 0x51 });
		_testEncode("SBB.B R5, R1", new byte[] { 0x50, (byte) 0xf1, (byte) 0x51 });
		_testEncode("AND *R5, R1", new byte[] { 0x48, (byte) 0x95, (byte) 0x11 });
		_testEncode("ADD *R5+, R1", new byte[] { 0x4C, (byte) 0xC5, (byte) 0x11 });
		_testEncode("SUB *R5, R1", new byte[] { 0x48, (byte) 0xE5, (byte) 0x11 });

		// immediates with implicit constants
		_testEncode("ADD #1, R4", new byte[] { (byte) 0xC4, (byte) 0xD4 });
		_testEncode("ADD #2, SP", new byte[] { (byte) 0xCD, (byte) 0xED });
		_testEncode("NAND #1, SR", new byte[] { (byte) 0xAF, (byte) 0xDF });
		
		// SP cannot appear in src2R as SP, so ops must be swapped
			// -> ADD SP, R4, R4
		_testEncode("ADD SP, R4", new byte[] { (byte) 0xCD, (byte) 0x44 });
		
		// status setters: no operand movement; SR is the destination
			// XOR? R5, R1, SR
		_testEncode("XOR? R5, R1", new byte[] { (byte) 0xB5, (byte) 0x1F });
			// XOR? *R5, R1, SR
		_testEncode("XOR? *R5, R1", new byte[] { 0x48, (byte) 0xB5, (byte) 0x1F });

		// note: no non-writing version of ADD/ADC since these are TST/TSTN
		assertBadInst("ADD? R5, R1");
		assertBadInst("ADC? R5, R1");
		
		_testEncode("CMP R5, R1", new byte[] { (byte) 0xe5, (byte) 0x1F });
		_testEncode("CMP *R5+, R1", new byte[] { 0x4C, (byte) 0xe5, (byte) 0x1F });

		// the opcode is for ADD/ADC here, not AND/NAND
		_testEncode("TST R5, R1", new byte[] { (byte) 0xC5, (byte) 0x1F });
		_testEncode("TSTN R5, R1", new byte[] { (byte) 0xD5, (byte) 0x1F });
		
		// pseudo
			// CLR -> XOR r,r,r
		_testEncode("XOR R1, R1", new byte[] { (byte) 0xB1, (byte) 0x11 });
			// INV -> XOR >FFFF,r,r
		_testEncode("XOR >FFFF, R1", new byte[] { (byte) 0xB1, (byte) 0xF1 });

		// If insts have destination as memory, make the middle operand the register,
		// if possible.
		
			// -> ADD *R5, R1, *R5 
		_testEncode("ADD R1, *R5", new byte[] { 0x4A, (byte) 0xC5, (byte) 0x15 });
			// -> XOR.B *R5, R1, *R5
		_testEncode("XOR.B R1, *R5", new byte[] { 0x5A, (byte) 0xB5, (byte) 0x15 });
			// SUB *SP, R0, *SP
		_testEncode("SUB R0, *SP", new byte[] { 0x4a, (byte) 0xed, (byte) 0x0d });
			// -> CMPR.B *R5+, R1
		_testEncode("CMP.B R1, *R5+", new byte[] { 0x5C, (byte) 0xf5, (byte) 0x1F });

		// In these, though, don't double-increment when copying the dest to source
			// -> ADD *R5, R1, *R5+
		_testEncode("ADD R1, *R5+", new byte[] { 0x4B, (byte) 0xC5, (byte) 0x15 });
			// -> XOR.B *R5, R1, *R5+
		_testEncode("XOR.B R1, *R5+", new byte[] { 0x5B, (byte) 0xB5, (byte) 0x15 });
		
			// can't reconcile
		assertBadInst("SUB *R0, *R1");
			// no immed in dest
		assertBadInst("SUB R0, #12");

	}
	
	public void testEncode1Ops() throws Exception {
		_testEncode("SEXT R1", new byte[] { (byte) 0x11 });
		_testEncode("EXTL R1", new byte[] { 0x44, (byte) 0x11 });
		_testEncode("EXTH R1", new byte[] { 0x48, (byte) 0x11 });
		_testEncode("SWPB R1", new byte[] { 0x4C, (byte) 0x11 });
		
		_testEncode("SEXT *R11", new byte[] { 0x42, (byte) 0x1B });
		_testEncode("SWPB *SP+", new byte[] { 0x4F, (byte) 0x1D });
		
		_testEncode("PUSH R1", new byte[] { (byte) 0x21 });
		_testEncode("PUSH #>1234", new byte[] { 0x43, (byte) 0x2E, 0x12, 0x34 });
		_testEncode("PUSH #>ff", new byte[] { 0x43, (byte) 0x2E, 0x00, (byte) 0xff });
			// auto byte selection
		_testEncode("PUSH #>12", new byte[] { 0x53, (byte) 0x2E, 0x12 });
		_testEncode("PUSH -1", new byte[] { 0x53, (byte) 0x2E, (byte) 0xff });
		_testEncode("PUSH.B R1", new byte[] { 0x50, (byte) 0x21 });
		_testEncode("PUSH.B #12", new byte[] { 0x53, (byte) 0x2E, 0x0C });
		
		_testEncode("PUSH #4, R1", new byte[] { 0x4C, (byte) 0x21 });
		_testEncode("PUSH.B #4, R1", new byte[] { 0x5C, (byte) 0x21 });
		_testEncode("POP PC", new byte[] { (byte) 0x3E });
		_testEncode("POP.B R0", new byte[] { 0x50, (byte) 0x30 });
		_testEncode("POP #2, PC", new byte[] { 0x44, (byte) 0x3E });
		_testEncode("POP #3, *R12+", new byte[] { 0x4B, (byte) 0x3C });
		_testEncode("POP.B #3, *R12+", new byte[] { 0x5B, (byte) 0x3C });
	}

	public void testEncodeJumps() throws Exception {
		_testEncode("JMP $", new byte[] { 0x77, (byte) 0xff });
		_testEncode("JMP >1000", new byte[] { 0x77, (byte) 0xff });
		_testEncode("JMP $+1", new byte[] { 0x77, (byte) 0x00 });
		_testEncode("JMP $+128+1", new byte[] { 0x40, 0x77, (byte) 0x80 });
		_testEncode("JMP $-128+1", new byte[] {0x77, (byte) 0x80 });
		_testEncode("JMP $-129+1", new byte[] { 0x4f, 0x77, (byte) 0x7f });
		_testEncode("JMP $+2047+1", new byte[] { 0x47, 0x77, (byte) 0xff });
		_testEncode("JMP $-2048+1", new byte[] { 0x48, 0x77, (byte) 0x00 });
		_testEncode("JMP $+2048+1", new byte[] { 0x58, 0x77, (byte) 0x00, 0x00 });
		_testEncode("JMP $-2049+1", new byte[] { 0x57, 0x77, (byte) 0xff, (byte) 0xff });
		_testEncode("JMP $+32767+1", new byte[] { 0x5f, 0x77, (byte) 0xff, 0x07 });
		_testEncode("JMP $-32768+1", new byte[] { 0x50, 0x77, (byte) 0x00, (byte) 0xf8 });
		
		_testEncode("JNE $+1", new byte[] { 0x70, (byte) 0x00 });
		_testEncode("JEQ $+1", new byte[] { 0x71, (byte) 0x00 });
		_testEncode("JNC $+1", new byte[] { 0x72, (byte) 0x00 });
		_testEncode("JC $+1", new byte[] { 0x73, (byte) 0x00 });
		_testEncode("JS $+1", new byte[] { 0x74, (byte) 0x00 });
		_testEncode("JGE $+1", new byte[] { 0x75, (byte) 0x00 });
		_testEncode("JL $+1", new byte[] { 0x76, (byte) 0x00 });
		_testEncode("JMP $+1", new byte[] { 0x77, (byte) 0x00 });
		
		// aliases...

	}
	
	public void testEncodeMoves() throws Exception {
		_testEncode("MOV R1, R2", new byte[] { 0x7f, 0x12 });
		_testEncode("MOV R3, PC", new byte[] { 0x7f, 0x3E });
		
		_testEncode("MOVNE R1, R2", new byte[] { 0x78, 0x12 });
		_testEncode("MOVEQ R1, R2", new byte[] { 0x79, 0x12 });
		_testEncode("MOVNC R1, R2", new byte[] { 0x7a, 0x12 });
		_testEncode("MOVC R1, R2", new byte[] { 0x7b, 0x12 });
		_testEncode("MOVS R1, R2", new byte[] { 0x7c, 0x12 });
		_testEncode("MOVGE R1, R2", new byte[] { 0x7d, 0x12 });
		_testEncode("MOVL R1, R2", new byte[] { 0x7e, 0x12 });
		
		_testEncode("MOV *R1+, *R2+", new byte[] { 0x4F, 0x7f, 0x12 });
		_testEncode("MOV.B #>ff, *R2+", new byte[] { 0x5F, 0x7f, (byte) 0xE2, (byte) 0xff });
		
		_testEncode("MOVEQ @8(R4), R2", new byte[] { 0x44, 0x79, 0x42, 0x00, 0x08 });
			// .B does not affect @xxx() size
		_testEncode("MOVEQ.B @8(R4), R2", new byte[] { 0x54, 0x79, 0x42, 0x00, 0x08 });
		
		assertBadInst("MOV R1, #11");
	}
	
	public void testEncodeShifts() throws Exception {
		_testEncode("LSH #1, R2", new byte[] { 0x68, 0x12 });
		_testEncode("LSH #0, R2", new byte[] { 0x68, 0x02 });
		_testEncode("LSH R0, R2", new byte[] { 0x68, 0x02 });
		_testEncode("RSH #8, R2", new byte[] { 0x69, (byte) 0x82 });
		_testEncode("ASH 15, R0", new byte[] { 0x6a, (byte) 0xF0 });
		_testEncode("ROL 8, R9", new byte[] { 0x6b, (byte) 0x89 });
		_testEncode("ROL 8, *R9+", new byte[] { 0x43, 0x6b, (byte) 0x89 });
		_testEncode("LSH R0, @100(R2)", new byte[] { 0x41, 0x68, 0x02, 0x00, 0x64 });
	}
	public void testEncodeMulDiv() throws Exception {
		_testEncode("MUL R1, R2", new byte[] { 0x6c, 0x12 });
		_testEncode("DIV R1, R2", new byte[] { 0x6d, 0x12 });
		_testEncode("MULD R1, R2", new byte[] { 0x6e, 0x12 });
		_testEncode("DIVD R1, R2", new byte[] { 0x6f, 0x12 });
		_testEncode("MUL R1, R2", new byte[] { 0x6c, 0x12 });
		_testEncode("MUL.B R1, R2", new byte[] { 0x50, 0x6c, 0x12 });
		_testEncode("MULD.B *R1+, *R2+", new byte[] { 0x5f, 0x6e, 0x12 });
	}
		
	private void _testEncode(String str, byte[] bytes) throws ParseException, ResolveException {
		assertInst(asmInstStage, str, bytes);
		assertInst(asmInstStage, str.toLowerCase(), bytes);

	}

	private void assertBadInst(String string) {
		assertBadInst(asmInstStage, string);
	}
	private void assertBadInst(IInstructionParserStage instStage, String string) {
		try {
			assertInst(instStage, string, new byte[0]);
			fail();
		} catch (IllegalArgumentException e) {
			
		} catch (ParseException e) {
			
		} catch (ResolveException e) {
		} catch (Error e) {
			throw e;
		}
	}
	
	private void assertInst(IInstructionParserStage instStage, String string, byte[] bytes) throws ParseException, ResolveException {
		IInstruction[] insts = instStage.parse("foo", string);
		assertNotNull("did not parse", insts);
		assertEquals(1, insts.length);
		
		assembler.setPc(0x1000);
		
		IInstruction[] irealInsts = ((AssemblerInstruction) insts[0]).resolve(assembler, null, true);
		assertEquals(1, irealInsts.length);
		assertTrue(irealInsts[0] instanceof LLInstruction);
		
		RawInstruction realInst = assembler.getInstructionFactory().createRawInstruction(
				((LLInstruction) irealInsts[0]));
		byte[] ebytes = assembler.getInstructionFactory().encodeInstruction(realInst);
		assertEquals(realInst.getSize(), ebytes.length);

		if (!Arrays.equals(bytes, ebytes)) {
			assertEquals("mismatched encoding for " + string,
					toString(bytes), toString(ebytes));
		}
		/*
		realInst.pc = 0;
		for (int i = 0; i < ebytes.length; i++)
			CPU.flatWriteByte(i, ebytes[i]);
		
		RawInstruction minst = InstTable9900.decodeInstruction(ebytes[0], 0, CPU);
		InstTable9900.coerceOperandTypes(minst);
		InstTable9900.coerceOperandTypes(realInst);
		
		assertEquals(realInst.toString(), minst.toString());
		System.out.println(insts[0]);
		*/
	}

	/**
	 * @param ebytes
	 * @return
	 */
	private String toString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (byte p : b)
			sb.append(HexUtils.toHex2(p)).append(' ');
		return sb.toString();
	}
	
	/*
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
	
	
	// Test resolving an instruction's operands 
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
	*/
}
