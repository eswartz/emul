/**
 * 
 */
package v9t9.tests.asm;

import java.util.Random;

import v9t9.engine.cpu.*;
import v9t9.engine.memory.MemoryDomain;
import junit.framework.TestCase;
import static v9t9.engine.cpu.InstMFP201.*;
import static v9t9.engine.cpu.MachineOperandMFP201.*;

/**
 * @author ejs
 *
 */
public class TestDisassemblerMFP201 extends TestCase {
	
	private int theInstPc = 0x1000;

	protected RawInstruction decode(byte[] bytes) {
		byte[] data = new byte[theInstPc + (bytes.length + 1)];
		for (int i = 0; i < bytes.length; i++) {
			data[theInstPc + i] = bytes[i];
		}
	    MemoryDomain domain = MemoryDomain.newFromArray(data);
	    RawInstruction inst = InstTableMFP201.decodeInstruction(theInstPc, domain);
	    
	    int sz = inst.getSize();
	    assertTrue(inst+":"+sz, sz > 0);
	    
	    long opcode = 0;
	    for (int i = 0; i < sz; i++)
	    	opcode = (opcode << 8) | (domain.flatReadByte(theInstPc + i) & 0xff);
	    assertEquals(inst+":"+sz, opcode, inst.opcode);
	    return inst;
	}
	
	protected void _testDecode(byte[] bytes, int op, MachineOperandMFP201... ops) {
		RawInstruction inst = decode(bytes);
		assertEquals(inst+"", op, inst.getInst());
		for (int o = 0; o < ops.length; o++)
			assertEquals(inst+"", ops[o], inst.getOp(o+1));
	}
	
	protected void assertSameInst(String str, RawInstruction inst) {
		assertEquals(str, str.replaceAll(">|#|(?<=,)\\s+", "").toLowerCase(), (inst+"").replaceAll(">|#|(?<=,)\\s+", "").toLowerCase());
	}
	protected void _testDecode(byte[] bytes, String str) {
		RawInstruction inst = decode(bytes);
		assertSameInst(str, inst);
		assertEquals(bytes.length, inst.getSize());
	}
	protected void _testDecodeJump(byte[] bytes, int destPc, String str) {
		RawInstruction inst = decode(bytes);
		MachineOperandMFP201 mop = (MachineOperandMFP201) inst.getOp1();
		int thePc = mop.val + theInstPc;
		assertEquals(str, str.replaceAll("#|(?<=,)\\s+", "").toLowerCase(), (inst+"").replaceAll("#|(?<=,)\\s+", "").toLowerCase());
		assertEquals(destPc, thePc);
	}
	public void testEncodeSimple1() throws Exception {
		_testDecode(new byte[] { 0x00 }, Ibkpt);
		//_testDecode(new byte[] { 0x3E }, Iret);	// POP PC
		//_testDecode(new byte[] { 0x44, 0x3e }, ); // POP #2, PC
	}
	public void testEncodeSimple2() throws Exception {
		_testDecode(new byte[] { 0x04, 0x02, 0x33 }, 
				Ibr, createPCRelativeOperand(0x233));
		_testDecode(new byte[] { 0x05, 0x12, 0x34 }, 
				Ibra, createImmediate(0x1234));
		_testDecode(new byte[] { 0x06, (byte) 0xF2, (byte) 0x33 }, 
				Icall, createPCRelativeOperand(0x233 - 0x1000));
		_testDecode(new byte[] { 0x07, 0x02, 0x34 }, 
				Icalla, createImmediate(0x234));
		_testDecode(new byte[] { 0x04, (byte) 0xff, (byte) 0xff }, 
				Ibr, createPCRelativeOperand(0));
		

		_testDecode(new byte[] { 0x4C, 0x07, 0x01 },
				"CALL *R1+");

				
	}
	/** The immediate encodings of these should be selected */
	public void testEncodeImm1() throws Exception {
		
		// the non-.B variant tries to select the best size (word/byte)
		_testDecode(new byte[] { 0x08, (byte) 0x85, 0x20 }, "OR #>100, R5");
		_testDecode(new byte[] { 0x08, (byte) 0x8f, 0x01 }, "OR #>8, SR");
		_testDecode(new byte[] { 0x08, 0x35 }, "OR >3, R5");
		_testDecode(new byte[] { 0x08, 0x45 }, "OR >FFFC, R5");
		_testDecode(new byte[] { 0x08, (byte) 0xf5, 0x0f }, "OR >7f, R5");
		_testDecode(new byte[] { 0x08, (byte) 0xf5, (byte) 0x3f }, "OR >1ff, R5");
		_testDecode(new byte[] { 0x08, (byte) 0xf5, (byte) 0xff, 0x00 }, "OR >3ff, R5");
		_testDecode(new byte[] { 0x08, (byte) 0xf5, (byte) 0xff, 0x01 }, "OR >7ff, R5");
		_testDecode(new byte[] { 0x08, (byte) 0xc5, (byte) 0x7e }, "OR >FFF4, R5");
		
		_testDecode(new byte[] { 0x09, (byte) 0xf5, (byte) 0x1f }, "AND >ff, R5");
		
		_testDecode(new byte[] { 0x0a, (byte) 0x80, 0x20 }, "NAND >100, R0");
		_testDecode(new byte[] { 0x0b, (byte) 0x80, 0x20 }, "ADD >100, R0");
		_testDecode(new byte[] { 0x0c, (byte) 0x80, 0x20 }, "SUB >100, R0");
		_testDecode(new byte[] { 0x0d, (byte) 0x80, 0x20 }, "CMP >100, R0");
		_testDecode(new byte[] { 0x0e, (byte) 0x80, 0x20 }, "TST >100, R0");
		_testDecode(new byte[] { 0x0f, (byte) 0x80, 0x20 }, "LDC >100, R0");
		
		_testDecode(new byte[] { 0x0f, (byte) 0x91, (byte) 0x60 }, "LDC >ff01, R1");
		_testDecode(new byte[] { 0x0f, (byte) 0xfe, 0x0f }, "LDC >7f, PC");
		_testDecode(new byte[] { 0x0d, (byte) 0xf5, 0x1f }, "CMP >FF, R5"); 
		_testDecode(new byte[] { 0x08, (byte) 0xff, 0x1f }, "OR >ff, SR");
		
		_testDecode(new byte[] { 0x41, 0xa, (byte) 0x8e, 0x08, 0x12, (byte) 0x34 }, "NAND >40, @>1234(PC)");
		
		_testDecode(new byte[] { 0x5a, 0xe, (byte) 0xe9, 0x4a }, "TST.B #>FE56, *R9");

		_testDecode(new byte[] { 0x51, 0x0d, (byte) 0xfe, (byte) 0x1f, 0x12, 0x34 }, "CMP.B >ff, @>1234(PC)");

		
		// larger format immediate
		//_testDecode("LDC >e000, R5", new byte[] { 0x4c, 0x7f, (byte) 0xe5, (byte) 0xe0, 0x00 });
		_testDecode(new byte[] { 0x0f, (byte) 0x85, (byte) 0x80, 0x38 }, "LDC >e000, R5");
		_testDecode(new byte[] { 0x08, (byte) 0xc5, (byte) 0xc6, 0x04 }, "OR >1234, R5");
		_testDecode(new byte[] { 0x08, (byte) 0xb5, (byte) 0xb9, (byte) 0x3b }, "OR >edcb, R5");
	}
	

	public void testEncode3OpWith3() throws Exception {
		
		_testDecode(new byte[] { (byte) 0x85, (byte) 0xC1 }, "OR R5, R12, R1");
		
		// use register for implicit constant in 2nd position
		// or select the implicit constant form if the operation is commutative
		_testDecode(new byte[] { (byte) 0x8C, (byte) 0xD1 }, "OR R12, #1, R1");
		_testDecode(new byte[] { (byte) 0x9C, (byte) 0xE1 }, "AND R12, >8000, R1");
		_testDecode(new byte[] { 0x50, (byte) 0xAC, (byte) 0xE1 }, "NAND.B R12, >80, R1");
		_testDecode(new byte[] { (byte) 0xBC, (byte) 0xF1 }, "XOR R12, >FFFF, R1");
		_testDecode(new byte[] { 0x50, (byte) 0x8C, (byte) 0xF1 }, "OR.B R12, >FF, R1");
		
		// explicit constant in mem op position
		_testDecode(new byte[] { 0x5C, (byte) 0x8E, (byte) 0xC1, 0x1f }, "OR.B >1F, R12, R1");
		// ... autoselect byte form
		_testDecode(new byte[] { 0x4C, (byte) 0x8E, (byte) 0xC1, 0x1f, (byte)0x80 }, "OR >1F80, R12, R1");

		// ... but don't select byte form here, since it's accessing memory
		_testDecode(new byte[] { 0x4E, (byte) 0x8E, (byte) 0xC1, 0x00, 0x1f }, "OR >1F, R12, *R1");

		_testDecode(new byte[] { (byte) 0xCD, (byte) 0xED }, "ADD SP, #2, SP");
		_testDecode(new byte[] { 0x50, (byte) 0xCD, (byte) 0xED }, "ADD.B SP, #2, SP");

		// immediates with implicit constants need to swap ops to avoid using special value
		_testDecode(new byte[] { (byte) 0xC4, (byte) 0xD4 }, "ADD R4, #1, R4");
		_testDecode(new byte[] { (byte) 0xCD, (byte) 0xED }, "ADD SP, #2, SP");
		
		// TODO
		_testDecode(new byte[] { (byte) 0xAF, (byte) 0xDF }, "NAND SR, #1");
		
		_testDecode(new byte[] { (byte) 0xCD, (byte) 0x4f }, "ADD SP, R4");

		
		// make sure we can use immediates with the long SBB form
		_testDecode(new byte[] { 0x5c, (byte) 0xfe, (byte) 0x00, 123 }, "SBB.B >7B, R0, R0");
		// implicit SR
		_testDecode(new byte[] { (byte) 0xff, 0x00 }, "SBB 0, R0, R0");
		
		_testDecode(new byte[] { 0x4C, (byte) 0xCE, (byte) 0x74, (byte) 0xf0, 0x00 }, "ADD >F000, R7, R4");
		_testDecode(new byte[] { 0x5C, (byte) 0xCE, (byte) 0x74, (byte) 0x9C }, "ADD.B >9C, R7, R4");

		// immediates with implicit constants
		_testDecode(new byte[] { (byte) 0xC4, (byte) 0xD4 }, "ADD R4, #1, R4");
		_testDecode(new byte[] { (byte) 0xCD, (byte) 0xED }, "ADD SP, #2, SP");

		// but this uses a normal implicit constant register 
		_testDecode(new byte[] { (byte) 0xD1, (byte) 0xF1 }, "ADC R1, #0, R1");
		_testDecode(new byte[] { (byte) 0xE1, (byte) 0xD1 }, "SUB R1, #1, R1");
		_testDecode(new byte[] { (byte) 0xC1, (byte) 0xE1 }, "ADD R1, #2, R1");
		
		// no byte form here; accessing memory
		_testDecode(new byte[] { 0x4E, (byte) 0xCE, (byte) 0x74, (byte) 0xff, (byte) 0x9C }, "ADD >FF9C, R7, *R4");

		// cannot convert, since this places the memory operand in the register-only position
		_testDecode(new byte[] { 0x48, (byte) 0x85, (byte) 0xC1 }, "OR *R5, R12, R1");

		// SETO -> SUB 0/*SP*/, 1/*R13*/, R
		_testDecode(new byte[] { (byte) 0xEF, (byte) 0xD1 }, "SUB 0, 1, R1");
	}
	
	public void testEncode3OpWith2() throws Exception {
		// the first source operand becomes the dest operand
			// OR R1, R5, R1
		_testDecode(new byte[] { (byte) 0x85, (byte) 0x1f }, "OR R5, R1");
		_testDecode(new byte[] { 0x50, (byte) 0xf5, (byte) 0x1f }, "SBB.B R5, R1");
		_testDecode(new byte[] { 0x48, (byte) 0x95, (byte) 0x1f }, "AND *R5, R1");
		_testDecode(new byte[] { 0x4C, (byte) 0xC5, (byte) 0x1f }, "ADD *R5+, R1");
		_testDecode(new byte[] { 0x48, (byte) 0xE5, (byte) 0x1f }, "SUB *R5, R1");

		_testDecode(new byte[] { 0x4f, (byte) 0x91, 0x3f }, "AND *R1+, *R3+");
		_testDecode(new byte[] { 0x4f, (byte) 0xc1, 0x3f }, "ADD *R1+, *R3+");


		// pseudo
			// CLR -> XOR r,r,r
		_testDecode(new byte[] { (byte) 0xB1, (byte) 0x11 }, "XOR R1, R1, R1");
			// INV -> XOR >FFFF,r,r
		_testDecode(new byte[] { (byte) 0xB1, (byte) 0xF1 }, "XOR R1, >FFFF, R1");

		// If insts have destination as memory, make the middle operand the register,
		// if possible.
		
		_testDecode(new byte[] { 0x4A, (byte) 0xC5, (byte) 0x15 }, "ADD *R5, R1, *R5");
		_testDecode(new byte[] { 0x5A, (byte) 0xB5, (byte) 0x15 }, "XOR.B *R5, R1, *R5");
		_testDecode(new byte[] { 0x4a, (byte) 0xed, (byte) 0x0d }, "SUB *SP, R0, *SP");
		_testDecode(new byte[] { 0x5C, (byte) 0xf5, (byte) 0x1F }, "SBB.B *R5+, R1");

		// In these, though, don't double-increment when copying the dest to source
			// -> ADD *R5, R1, *R5+
		_testDecode(new byte[] { 0x4B, (byte) 0xC5, (byte) 0x15 }, "ADD *R5, R1, *R5+");
			// -> XOR.B *R5, R1, *R5+
		_testDecode(new byte[] { 0x5B, (byte) 0xB5, (byte) 0x15 }, "XOR.B *R5, R1, *R5+");

	}
	
	public void testEncode1Ops() throws Exception {
		_testDecode(new byte[] { (byte) 0x11 }, "SEXT R1");
		_testDecode(new byte[] { 0x44, (byte) 0x11 }, "EXTL R1");
		_testDecode(new byte[] { 0x48, (byte) 0x11 }, "EXTH R1");
		_testDecode(new byte[] { 0x4C, (byte) 0x11 }, "SWPB R1");
		
		_testDecode(new byte[] { 0x42, (byte) 0x15 }, "SEXT *R5");
		_testDecode(new byte[] { 0x4A, (byte) 0x15 }, "EXTH *R5");
		
		_testDecode(new byte[] { 0x42, (byte) 0x1B }, "SEXT *R11");
		_testDecode(new byte[] { 0x4F, (byte) 0x1D }, "SWPB *SP+");
		
		_testDecode(new byte[] { (byte) 0x21 }, "PUSH R1");
		_testDecode(new byte[] { 0x43, (byte) 0x2E, 0x12, 0x34 }, "PUSH #>1234");
		_testDecode(new byte[] { 0x43, (byte) 0x2E, 0x00, (byte) 0xff }, "PUSH #>ff");
		_testDecode(new byte[] { 0x53, (byte) 0x2E, 0x12 }, "PUSH.B #>12");
		_testDecode(new byte[] { 0x53, (byte) 0x2E, (byte) 0xff }, "PUSH.B #>FF");
		_testDecode(new byte[] { 0x50, (byte) 0x21 }, "PUSH.B R1");
		_testDecode(new byte[] { 0x53, (byte) 0x2E, 0x0C }, "PUSH.B #>C");
		
		_testDecode(new byte[] { 0x4C, (byte) 0x21 }, "PUSHN #4, R1");
		_testDecode(new byte[] { 0x5C, (byte) 0x21 }, "PUSHN.B #4, R1");
		_testDecode(new byte[] { (byte) 0x3E }, "POP PC");
		_testDecode(new byte[] { 0x50, (byte) 0x30 }, "POP.B R0");
		_testDecode(new byte[] { 0x44, (byte) 0x3E }, "POPN #2, PC");
		_testDecode(new byte[] { 0x4B, (byte) 0x3C }, "POPN #3, *R12+");
		_testDecode(new byte[] { 0x5B, (byte) 0x3C }, "POPN.B #3, *R12+");
	}

	public void testEncodeJumps() throws Exception {
		_testDecodeJump(new byte[] { 0x60, (byte) 0xfe }, 0x1000, "JMP $+>0");
		_testDecodeJump(new byte[] { 0x60, (byte) 0x00 }, 0x1002, "JMP $+>2");
		_testDecodeJump(new byte[] { 0x40, 0x60, (byte) 0x7f }, 0x1082, "JMP $+>82");
		_testDecodeJump(new byte[] { 0x60, (byte) 0x80 }, 0xF82, "JMP $+>FF82");
		_testDecodeJump(new byte[] { 0x4f, 0x60, (byte) 0x7e }, 0xF81, "JMP $+>FF81");
		_testDecodeJump(new byte[] { 0x47, 0x60, (byte) 0xfd }, 0x1800, "JMP $+>800");
		_testDecodeJump(new byte[] { 0x48, 0x60, (byte) 0x00 }, 0x803, "JMP $+>F803");
		_testDecodeJump(new byte[] { 0x58, 0x60, (byte) 0x00, 0x00 }, 0x1804, "JMP $+>804");
		_testDecodeJump(new byte[] { 0x57, 0x60, (byte) 0xfe, (byte) 0xff }, 0x802, "JMP $+>F802");
		
		theInstPc = 0x7FFC;
		_testDecodeJump(new byte[] { 0x50, 0x60, (byte) 0x00, (byte) 0xf8 }, 0x0, "JMP $+>8004");

		/*
		_testDecode(new byte[] { 0x70, (byte) 0x00 }, "JNE $+>2");
		_testDecode(new byte[] { 0x71, (byte) 0x00 }, "JEQ $+>2");
		_testDecode(new byte[] { 0x72, (byte) 0x00 }, "JNC $+>2");
		_testDecode(new byte[] { 0x73, (byte) 0x00 }, "JC $+>2");
		_testDecode(new byte[] { 0x74, (byte) 0x00 }, "JN $+>2");
		_testDecode(new byte[] { 0x75, (byte) 0x00 }, "JGE $+>2");
		_testDecode(new byte[] { 0x76, (byte) 0x00 }, "JL $+>2");
		_testDecode(new byte[] { 0x77, (byte) 0x00 }, "JMP $+>2");
		*/
		// aliases...

	}
	
	public void testEncodeMoves() throws Exception {
		_testDecode(new byte[] { 0x6a, 0x12 }, "MOV R1, R2");
		_testDecode(new byte[] { 0x6a, 0x3E }, "MOV R3, PC");
		
		_testDecode(new byte[] { 0x4F, 0x6a, 0x12 }, "MOV *R1+, *R2+");
		_testDecode(new byte[] { 0x5F, 0x6a, (byte) 0xE2, (byte) 0xff }, "MOV.B #>ff, *R2+");
		_testDecode(new byte[] { 0x4c, 0x6a, (byte) 0xE2, (byte) 0x12, 0x34 }, "MOV #>1234, R2");
		
		_testDecode(new byte[] { 0x44, 0x6a, 0x42, 0x00, 0x08 }, "MOV @>0008(R4), R2");
			// .B does not affect @xxx() size
		_testDecode(new byte[] { 0x54, 0x6a, 0x42, 0x00, 0x08 }, "MOV.B @>0008(R4), R2");
	}
	
	public void testEncodeShifts() throws Exception {
		_testDecode(new byte[] { 0x68, 0x12 }, "LSH #1, R2");
		_testDecode(new byte[] { 0x68, 0x02 }, "LSH R0, R2");
		_testDecode(new byte[] { 0x69, (byte) 0x82 }, "RSH #8, R2");
		_testDecode(new byte[] { 0x41, 0x68, 0x02, 0x00, 0x64 }, "LSH R0, @>0064(R2)");

		_testDecode(new byte[] { 0x44, 0x69, (byte) 0xF0 }, "ASH 15, R0");
		_testDecode(new byte[] { 0x47, 0x69, (byte) 0xF0 }, "ASH 15, *R0+");
		_testDecode(new byte[] { 0x48, 0x69, (byte) 0xF0 }, "RSHC 15, R0");
		_testDecode(new byte[] { 0x4C, 0x69, (byte) 0xF0 }, "RSHZ 15, R0");
		
		_testDecode(new byte[] { 0x44, 0x68, (byte) 0x89 }, "ROL 8, R9");
		_testDecode(new byte[] { 0x47, 0x68, (byte) 0x89 }, "ROL 8, *R9+");
		_testDecode(new byte[] { 0x4b, 0x68, (byte) 0x89 }, "LSHC 8, *R9+");
		_testDecode(new byte[] { 0x4f, 0x68, (byte) 0x89 }, "LSHZ 8, *R9+");
	}
	public void testEncodeMulDiv() throws Exception {
		_testDecode(new byte[] { 0x6e, 0x12 }, "MUL R1, R2");
		_testDecode(new byte[] { 0x50, 0x6e, 0x12 }, "MUL.b R1, R2");
		_testDecode(new byte[] { 0x6f, 0x12 }, "DIV R1, R2");
		_testDecode(new byte[] { 0x50, 0x6f, 0x12 }, "DIV.b R1, R2");
		_testDecode(new byte[] { 0x43, 0x6e, 0x12 }, "MUL R1, *R2+");
		_testDecode(new byte[] { 0x5c, 0x6e, 0x12 }, "MUL.b *R1+, R2");
		_testDecode(new byte[] { 0x42, 0x6f, 0x12 }, "DIV R1, *R2");
		_testDecode(new byte[] { 0x54, 0x6f, 0x12, 0x00, 0x02 }, "DIV.b @>0002(R1), R2");
		_testDecode(new byte[] { 0x6e, 0x12 }, "MUL R1, R2");
		_testDecode(new byte[] { 0x50, 0x6e, 0x12 }, "MUL.B R1, R2");
		_testDecode(new byte[] { 0x5f, 0x6e, 0x12 }, "MUL.B *R1+, *R2+");
	}
		
	public void testEncodeLea() throws Exception {
		// offset encoded as a byte
		_testDecode(new byte[] { 0x58, 0x12, (byte) 0x1F, 0x01 }, "LEA @>1(R1), R2");
		_testDecode(new byte[] { 0x50, 0x12, (byte) 0x1F, 0x10, 0x00 }, "LEA @>1000(R1), R2");
		_testDecode(new byte[] { 0x50, 0x13, (byte) 0x12, 0x01, 0x23 }, "LEA @>123(R1+R2), R3");
		_testDecode(new byte[] { 0x5A, 0x13, (byte) 0x12, 0x12 }, "LEA @>12(R1+R2*4), R3");
		_testDecode(new byte[] { 0x5F, 0x13, (byte) 0x21, 0x12 }, "LEA @>12(R2+R1*128), R3");
		_testDecode(new byte[] { 0x5F, 0x13, (byte) 0xF1, (byte) 0xf6 }, "LEA @>FFF6(R1*128), R3");
		_testDecode(new byte[] { 0x58, 0x13, (byte) 0xDD, 0x00 }, "LEA @>0(SP+SP), R3");
		// pointless
		_testDecode(new byte[] { 0x58, 0x13, (byte) 0x0F, 0x00 }, "LEA @>0(R0), R3");
	}
	
	public void testEncodeLoopStep() throws Exception {
		_testDecode(new byte[] { 0x03, (byte) 0xc1, 
		(byte) 0xc2, (byte) 0xd2}, 
				"LOOP R1: ADD R2, #1, R2");
		
		// verify syntax
		//assertEquals("LOOP R1: ADD R2,#1,R2", getInst("LOOP R1: ADD #1, R2").toString());
		
		_testDecode(new byte[] { 0x03, (byte) 0xcf, 
		(byte) 0xc2, (byte) 0xd2}, 
				"STEP: ADD R2, #1, R2");
		
		// verify syntax
		//assertEquals("STEP: ADD R2,#1,R2", getInst("STEP: ADD #1, R2").toString());

		_testDecode(new byte[] { 0x03, (byte) 0xf1, 
				0x4F, 0x6a, 0x12}, 
				"LOOP R1: MOV *R1-,*R2-");
		
		_testDecode(new byte[] { 0x03, (byte) 0x11, 
				0x4F, 0x6a, 0x12}, 
				"LOOPNE R1: MOV *R1+,*R2-");
		
		_testDecode(new byte[] { 0x03, 0x61, 
				0x4F, 0x6a, 0x12}, 
				"LOOPEQ R1: MOV *R1-,*R2+");
		
		_testDecode(new byte[] { 0x03, (byte) 0xaf, 
				0x4F, 0x6a, 0x12}, 
				"STEPNC: MOV *R1-,*R2+");
		
	}
	

	public void testCondIf() throws Exception {
		_testDecode(new byte[] { 0x71,
				(byte) 0xc2, (byte) 0xd2},
				"IFEQ ADD R2, #1, R2" 
				);
		
		_testDecode(new byte[] { 0x73, 
				(byte) 0xc2, (byte) 0xd2}, 
				"IFC ADD R2, #1, R2" 
				);
		
	}
	
	/*
	public void testPseudos() throws Exception {
		RawInstruction ins;
		ins = getInst("CLR R1");
		assertEquals("LDC #>0,R1", ins.toString());
		assertEquals(2, ins.getSize());
		
		ins = getInst("SETO R1");
		assertEquals("LDC #>FFFF,R1", ins.toString());
		assertEquals(2, ins.getSize());
		
		ins = getInst("INV R1");
		assertEquals("XOR R1,#-1,R1", ins.toString());
		assertEquals(2, ins.getSize());
		
		ins = getInst("INV.B R1");
		assertEquals("XOR.B R1,#>ff,R1", ins.toString());
		assertEquals(3, ins.getSize());	// byte
		
		ins = getInst("INC R1");
		assertEquals("ADD R1,#1,R1", ins.toString());
		assertEquals(2, ins.getSize());
		
		ins = getInst("INCT R1");
		assertEquals("ADD R1,#2,R1", ins.toString());
		assertEquals(2, ins.getSize());
		
		ins = getInst("DEC R1");
		assertEquals("SUB R1,#1,R1", ins.toString());
		assertEquals(2, ins.getSize());
		
		ins = getInst("DECT R1");
		assertEquals("SUB R1,#2,R1", ins.toString());
		assertEquals(2, ins.getSize());
	}
*/	
	
	public void testTwoWay() throws Exception {
		Random random = new Random(0x123456);
		
		byte[] bytes = new byte[16];
		for (int c = 0; c < 100000; c++) {
			random.nextBytes(bytes);
			
			_testOneTwoWay(bytes);
		}
	}

	private void _testOneTwoWay(byte[] bytes) {
		RawInstruction inst = decode(bytes);
		assertNotNull(inst);
		String instStr = inst.toString();
		
		System.out.println(instStr + ":" + Long.toHexString(inst.opcode) + ":" + inst.getInfo().cycles);
		try {
			// may not match bytes, due to garbage
			byte[] outBytes = InstTableMFP201.encode(inst);
			
			RawInstruction outInst = decode(outBytes);
			if (!inst.equals(outInst)) {
				assertSameInst(inst.toString(), outInst);
			} else {
				assertEquals(instStr, inst, outInst);
			}
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}
}
