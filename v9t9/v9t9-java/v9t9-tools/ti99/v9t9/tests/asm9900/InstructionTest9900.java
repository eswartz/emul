/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.tests.asm9900;

import junit.framework.TestCase;
import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.MemoryDomain;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.machine.ti99.cpu.InstructionWorkBlock9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;

/**
 * @author ejs
 */
public class InstructionTest9900 extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(InstructionTest9900.class);
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
        
        CpuState9900 state = new CpuState9900(domain);
        state.setWP((short) 0x83e0);
        
        InstructionWorkBlock9900 block = new InstructionWorkBlock9900(state);
        
      	RawInstruction inst;
      	MachineOperand9900 mop1;
      	MachineOperand9900 mop2;
      	short ea1, ea2;
      	
      	// simple decoding
      	short pc = 0;
      	inst = InstTable9900.decodeInstruction(domain.readWord(pc), pc, domain);
      	block.inst = inst;
      	mop1 = (MachineOperand9900) inst.getOp1();
        mop2 = (MachineOperand9900) inst.getOp2();

        assertTrue(inst != null);
        assertEquals(Inst9900.Iclr, inst.getInst());
        assertEquals("CLR", InstTable9900.getInstName(inst.getInst()));
        assertEquals(false, mop1.byteop);
        assertEquals(MachineOperand9900.OP_REG, mop1.type);
        assertEquals(3, mop1.val);
        assertEquals("R3", mop1.toString());
        assertEquals(2, inst.getSize());
        assertEquals("CLR R3", inst.toString());

        // some pc-relative tests
        pc += inst.getSize();
        inst = InstTable9900.decodeInstruction(domain.readWord(pc), pc, domain);
        block.inst = inst;
        mop1 = (MachineOperand9900) inst.getOp1();
        mop2 = (MachineOperand9900) inst.getOp2();
        ea1 = mop1.getEA(block);

        assertTrue(inst != null);
        assertEquals(Inst9900.Ijmp, inst.getInst());
        assertEquals("JMP", InstTable9900.getInstName(inst.getInst()));
        assertEquals(false, mop1.byteop);
        assertEquals(MachineOperand9900.OP_JUMP, mop1.type);
        assertEquals(0, mop1.val);
        assertEquals(ea1, inst.pc);
        assertEquals("$+>0", mop1.toString());
        assertEquals(2, inst.getSize());
        assertEquals("JMP $+>0", inst.toString());

        pc += inst.getSize();
        inst = InstTable9900.decodeInstruction(domain.readWord(pc), pc, domain);
        block.inst = inst;
        mop1 = (MachineOperand9900) inst.getOp1();
        mop2 = (MachineOperand9900) inst.getOp2();
        ea1 = mop1.getEA(block);

        assertTrue(inst != null);
        assertEquals(Inst9900.Ijnc, inst.getInst());
        assertEquals("JNC", InstTable9900.getInstName(inst.getInst()));
        assertEquals(false, mop1.byteop);
        assertEquals(MachineOperand9900.OP_JUMP, mop1.type);
        assertEquals(-254, mop1.val);
        assertEquals((short)(inst.pc-254), ea1);
        assertEquals("$+>FF02", mop1.toString());
        assertEquals(2, inst.getSize());
        assertEquals("JNC $+>FF02", inst.toString());

        pc += inst.getSize();

        domain.writeWord(0x83E0 + 9*2, (short)0x4000);
        inst = InstTable9900.decodeInstruction(domain.readWord(pc), pc, domain);
        block.inst = inst;
        mop1 = (MachineOperand9900) inst.getOp1();
        mop2 = (MachineOperand9900) inst.getOp2();
        ea1 = mop1.getEA(block);
        ea2 = mop2.getEA(block);

        // ensure register indirects work.
        assertTrue(inst != null);
        assertEquals(Inst9900.Imov, inst.getInst());
        assertEquals("MOV", InstTable9900.getInstName(inst.getInst()));
        assertEquals(false, mop1.byteop);
        assertEquals(MachineOperand9900.OP_ADDR, mop1.type);
        assertEquals(9, mop1.val);
        assertEquals(0x4a, mop1.immed);
        assertEquals(0x404a, ea1);
        assertEquals("@>4A(R9)", mop1.toString());
        assertEquals(MachineOperand9900.OP_ADDR, mop2.type);
        assertEquals(9, mop2.val);
        assertEquals(0x7fff, mop2.immed);
        assertEquals((short)(0x4000+0x7fff), ea2);
        assertEquals("@>7FFF(R9)", mop2.toString());
        assertEquals(6, inst.getSize());

        domain.writeWord(0x83E0 + 9*2, (short)0x4001);
        inst = InstTable9900.decodeInstruction(domain.readWord(pc), pc, domain);
        block.inst = inst;
        mop1 = (MachineOperand9900) inst.getOp1();
        mop2 = (MachineOperand9900) inst.getOp2();
        ea1 = mop1.getEA(block);
        ea2 = mop2.getEA(block);

        // make sure odd register plus odd offset works
        assertTrue(inst != null);
        assertEquals(0x404b, ea1);
        assertEquals((short)(0x4001+0x7fff), ea2);

        // ensure register increment holds and works in correct order.
        pc += inst.getSize();
        domain.writeWord(0x83E0 + 2*2, (short)0x4000);
        inst = InstTable9900.decodeInstruction(domain.readWord(pc), pc, domain);
        block.inst = inst;
        mop1 = (MachineOperand9900) inst.getOp1();
        mop2 = (MachineOperand9900) inst.getOp2();
        // TODO[
        mop1.byteop = true;
        mop2.byteop = true;
        //]
        ea1 = mop1.getEA(block);
        ea2 = mop2.getEA(block);

        assertTrue(inst != null);
        assertEquals(Inst9900.Icb, inst.getInst());
        assertEquals("CB", InstTable9900.getInstName(inst.getInst()));
        //assertEquals(true, mop1.byteop); //TODO
        assertEquals(MachineOperand9900.OP_INC, mop1.type);
        assertEquals(2, mop1.val);
        assertEquals(0, mop1.immed);
        assertEquals(0x4000, ea1);
        assertEquals("*R2+", mop1.toString());

        assertEquals(MachineOperand9900.OP_INC, mop2.type);
        //assertEquals(true, mop2.byteop); //TODO
        assertEquals(2, mop2.val);
        assertEquals(0, mop2.immed);
        assertEquals(0x4001, ea2);
        assertEquals("*R2+", mop2.toString());
        assertEquals(2, inst.getSize());

        assertEquals(0x4002, domain.readWord(0x83e0+2*2));

    }

}
