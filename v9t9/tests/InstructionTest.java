/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.tests;

import junit.framework.TestCase;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.MachineOperand;
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
      	Instruction inst;
      	MachineOperand mop1;
      	MachineOperand mop2;
      	short ea1, ea2;
      	
      	// simple decoding
      	short pc = 0;
      	inst = new Instruction(domain.readWord(pc), pc, domain);
      	mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;

        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Iclr);
        assertEquals(inst.name, "CLR");
        assertEquals(mop1.byteop, false);
        assertEquals(mop1.type, MachineOperand.OP_REG);
        assertEquals(mop1.val, 3);
        assertEquals(mop1.toString(), "R3");
        assertEquals(inst.size, 2);
        assertEquals(inst.toString(), "CLR R3");

        // some pc-relative tests
        pc += inst.size;
        inst = new Instruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Ijmp);
        assertEquals(inst.name, "JMP");
        assertEquals(mop1.byteop, false);
        assertEquals(mop1.type, MachineOperand.OP_JUMP);
        assertEquals(mop1.val, 0);
        assertEquals(ea1, inst.pc);
        assertEquals(mop1.toString(), "$+>0");
        assertEquals(inst.size, 2);
        assertEquals(inst.toString(), "JMP $+>0");

        pc += inst.size;
        inst = new Instruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Ijnc);
        assertEquals(inst.name, "JNC");
        assertEquals(mop1.byteop, false);
        assertEquals(mop1.type, MachineOperand.OP_JUMP);
        assertEquals(mop1.val, -254);
        assertEquals(ea1, (short)(inst.pc-254));
        assertEquals(mop1.toString(), "$+>FF02");
        assertEquals(inst.size, 2);
        assertEquals(inst.toString(), "JNC $+>FF02");

        pc += inst.size;

        domain.writeWord(0x83E0 + 9*2, (short)0x4000);
        inst = new Instruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);
        ea2 = mop2.getEA(domain, pc, (short)0x83e0);

        // ensure register indirects work.
        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Imov);
        assertEquals(inst.name, "MOV");
        assertEquals(mop1.byteop, false);
        assertEquals(mop1.type, MachineOperand.OP_ADDR);
        assertEquals(mop1.val, 9);
        assertEquals(mop1.immed, 0x4a);
        assertEquals(ea1, 0x404a);
        assertEquals(mop1.toString(), "@>4A(R9)");
        assertEquals(mop2.type, MachineOperand.OP_ADDR);
        assertEquals(mop2.val, 9);
        assertEquals(mop2.immed, 0x7fff);
        assertEquals(ea2, (short)(0x4000+0x7fff));
        assertEquals(mop2.toString(), "@>7FFF(R9)");
        assertEquals(inst.size, 6);

        domain.writeWord(0x83E0 + 9*2, (short)0x4001);
        inst = new Instruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);
        ea2 = mop2.getEA(domain, pc, (short)0x83e0);

        // make sure odd register plus odd offset works
        assertTrue(inst != null);
        assertEquals(ea1, 0x404b);
        assertEquals(ea2, (short)(0x4001+0x7fff));

        // ensure register increment holds and works in correct order.
        pc += inst.size;
        domain.writeWord(0x83E0 + 2*2, (short)0x4000);
        inst = new Instruction(domain.readWord(pc), pc, domain);
        mop1 = (MachineOperand) inst.op1;
        mop2 = (MachineOperand) inst.op2;
        ea1 = mop1.getEA(domain, pc, (short)0x83e0);
        ea2 = mop2.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Icb);
        assertEquals(inst.name, "CB");
        assertEquals(mop1.byteop, true);
        assertEquals(mop1.type, MachineOperand.OP_INC);
        assertEquals(mop1.val, 2);
        assertEquals(mop1.immed, 0);
        assertEquals(ea1, 0x4000);
        assertEquals(mop1.toString(), "*R2+");

        assertEquals(mop2.type, MachineOperand.OP_INC);
        assertEquals(mop2.byteop, true);
        assertEquals(mop2.val, 2);
        assertEquals(mop2.immed, 0);
        assertEquals(ea2, 0x4001);
        assertEquals(mop2.toString(), "*R2+");
        assertEquals(inst.size, 2);

        assertEquals(domain.readWord(0x83e0+2*2), 0x4002);

    }

}
