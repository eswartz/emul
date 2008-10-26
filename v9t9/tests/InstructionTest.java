/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.tests;

import junit.framework.TestCase;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 */
public class InstructionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(InstructionTest.class);
    }

    /*
     * Class under test for String toString()
     */
    public void testToString() {
        short[] code = { 0x04c3,
                
                0x10FF,
                
                0x1780,
                
                (short) 0xca69,
                0x004a,
                0x7fff,
                
                (short) 0x9cb2
                };
        short[] data = new short[0x8400/2];
        for (int i = 0; i < code.length; i++) {
            data[i] = code[i];
        }
        MemoryDomain domain = MemoryDomain.newFromArray(data, true);
      	RawInstruction inst;
      	MachineOperand mop1;
      	MachineOperand mop2;
      	short ea1, ea2;
      	
      	// simple decoding
      	short pc = 0;
      	inst = InstructionTable.decodeInstruction(domain.readWord(pc), pc, domain);
      	mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;

        assertTrue(inst != null);
        assertEquals(InstructionTable.Iclr, inst.inst);
        assertEquals("CLR", inst.name);
        assertEquals(false, mop1.byteop);
        assertEquals(MachineOperand.OP_REG, mop1.type);
        assertEquals(3, mop1.val);
        assertEquals("R3", mop1.toString());
        assertEquals(2, inst.size);
        assertEquals("CLR R3", inst.toString());

        // some pc-relative tests
        pc += inst.size;
        inst = InstructionTable.decodeInstruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(InstructionTable.Ijmp, inst.inst);
        assertEquals("JMP", inst.name);
        assertEquals(false, mop1.byteop);
        assertEquals(MachineOperand.OP_JUMP, mop1.type);
        assertEquals(0, mop1.val);
        assertEquals(ea1, inst.pc);
        assertEquals("$+>0", mop1.toString());
        assertEquals(2, inst.size);
        assertEquals("JMP $+>0", inst.toString());

        pc += inst.size;
        inst = InstructionTable.decodeInstruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(InstructionTable.Ijnc, inst.inst);
        assertEquals("JNC", inst.name);
        assertEquals(false, mop1.byteop);
        assertEquals(MachineOperand.OP_JUMP, mop1.type);
        assertEquals(-254, mop1.val);
        assertEquals((short)(inst.pc-254), ea1);
        assertEquals("$+>FF02", mop1.toString());
        assertEquals(2, inst.size);
        assertEquals("JNC $+>FF02", inst.toString());

        pc += inst.size;

        domain.writeWord(0x83E0 + 9*2, (short)0x4000);
        inst = InstructionTable.decodeInstruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);
        ea2 = mop2.getEA(domain, pc, (short)0x83e0);

        // ensure register indirects work.
        assertTrue(inst != null);
        assertEquals(InstructionTable.Imov, inst.inst);
        assertEquals("MOV", inst.name);
        assertEquals(false, mop1.byteop);
        assertEquals(MachineOperand.OP_ADDR, mop1.type);
        assertEquals(9, mop1.val);
        assertEquals(0x4a, mop1.immed);
        assertEquals(0x404a, ea1);
        assertEquals("@>4A(R9)", mop1.toString());
        assertEquals(MachineOperand.OP_ADDR, mop2.type);
        assertEquals(9, mop2.val);
        assertEquals(0x7fff, mop2.immed);
        assertEquals((short)(0x4000+0x7fff), ea2);
        assertEquals("@>7FFF(R9)", mop2.toString());
        assertEquals(6, inst.size);

        domain.writeWord(0x83E0 + 9*2, (short)0x4001);
        inst = InstructionTable.decodeInstruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);
        ea2 = mop2.getEA(domain, pc, (short)0x83e0);

        // make sure odd register plus odd offset works
        assertTrue(inst != null);
        assertEquals(0x404b, ea1);
        assertEquals((short)(0x4001+0x7fff), ea2);

        // ensure register increment holds and works in correct order.
        pc += inst.size;
        domain.writeWord(0x83E0 + 2*2, (short)0x4000);
        inst = InstructionTable.decodeInstruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);
        ea2 = mop2.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(InstructionTable.Icb, inst.inst);
        assertEquals("CB", inst.name);
        assertEquals(true, mop1.byteop);
        assertEquals(MachineOperand.OP_INC, mop1.type);
        assertEquals(2, mop1.val);
        assertEquals(0, mop1.immed);
        assertEquals(0x4000, ea1);
        assertEquals("*R2+", mop1.toString());

        assertEquals(MachineOperand.OP_INC, mop2.type);
        assertEquals(true, mop2.byteop);
        assertEquals(2, mop2.val);
        assertEquals(0, mop2.immed);
        assertEquals(0x4001, ea2);
        assertEquals("*R2+", mop2.toString());
        assertEquals(2, inst.size);

        assertEquals(0x4002, domain.readWord(0x83e0+2*2));

    }

}
