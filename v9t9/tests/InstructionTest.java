/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.tests;

import v9t9.MemoryDomain;
import v9t9.cpu.Instruction;
import v9t9.cpu.Operand;
import v9t9.cpu.Status;
import junit.framework.TestCase;

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
        byte[] code = { 0x04, (byte)0xc3,
                
                0x10, (byte)0xFF,
                
                0x17, (byte)0x80,
                
                (byte)0xca, 0x69,
                0x00, 0x4a,
                0x7f, (byte)0xff,
                
                (byte)0x9c, (byte)0xb2
                };
        byte[] data = new byte[0x8400];
        for (int i = 0; i < code.length; i++) {
            data[i] = code[i];
        }
        MemoryDomain domain = MemoryDomain.newFromArray(data, true);
      	Instruction inst;
      	short ea1, ea2, val1, val2;
      	
      	// simple decoding
      	short pc = 0;
      	inst = new Instruction(domain.readWord(pc), pc, domain);
       
        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Iclr);
        assertEquals(inst.name, "CLR");
        assertEquals(inst.op1.byteop, false);
        assertEquals(inst.op1.type, Operand.OP_REG);
        assertEquals(inst.op1.val, 3);
        assertEquals(inst.op1.toString(), "R3");
        assertEquals(inst.size, 2);
        assertEquals(inst.toString(), "CLR R3");

        // some pc-relative tests
        pc += inst.size;
        inst = new Instruction(domain.readWord(pc), pc, domain);
        ea1 = inst.op1.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Ijmp);
        assertEquals(inst.name, "JMP");
        assertEquals(inst.op1.byteop, false);
        assertEquals(inst.op1.type, Operand.OP_JUMP);
        assertEquals(inst.op1.val, 0);
        assertEquals(ea1, inst.pc);
        assertEquals(inst.op1.toString(), "$+>0");
        assertEquals(inst.size, 2);
        assertEquals(inst.toString(), "JMP $+>0");

        pc += inst.size;
        inst = new Instruction(domain.readWord(pc), pc, domain);
        ea1 = inst.op1.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Ijnc);
        assertEquals(inst.name, "JNC");
        assertEquals(inst.op1.byteop, false);
        assertEquals(inst.op1.type, Operand.OP_JUMP);
        assertEquals(inst.op1.val, -254);
        assertEquals(ea1, (short)(inst.pc-254));
        assertEquals(inst.op1.toString(), "$+>FF02");
        assertEquals(inst.size, 2);
        assertEquals(inst.toString(), "JNC $+>FF02");

        pc += inst.size;

        domain.writeWord(0x83E0 + 9*2, (short)0x4000);
        inst = new Instruction(domain.readWord(pc), pc, domain);
        ea1 = inst.op1.getEA(domain, pc, (short)0x83e0);
        ea2 = inst.op2.getEA(domain, pc, (short)0x83e0);

        // ensure register indirects work.
        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Imov);
        assertEquals(inst.name, "MOV");
        assertEquals(inst.op1.byteop, false);
        assertEquals(inst.op1.type, Operand.OP_ADDR);
        assertEquals(inst.op1.val, 9);
        assertEquals(inst.op1.immed, 0x4a);
        assertEquals(ea1, 0x404a);
        assertEquals(inst.op1.toString(), "@>4A(R9)");
        assertEquals(inst.op2.type, Operand.OP_ADDR);
        assertEquals(inst.op2.val, 9);
        assertEquals(inst.op2.immed, 0x7fff);
        assertEquals(ea2, (short)(0x4000+0x7fff));
        assertEquals(inst.op2.toString(), "@>7FFF(R9)");
        assertEquals(inst.size, 6);

        domain.writeWord(0x83E0 + 9*2, (short)0x4001);
        inst = new Instruction(domain.readWord(pc), pc, domain);
        ea1 = inst.op1.getEA(domain, pc, (short)0x83e0);
        ea2 = inst.op2.getEA(domain, pc, (short)0x83e0);

        // make sure odd register plus odd offset works
        assertTrue(inst != null);
        assertEquals(ea1, 0x404b);
        assertEquals(ea2, (short)(0x4001+0x7fff));

        // ensure register increment holds and works in correct order.
        pc += inst.size;
        domain.writeWord(0x83E0 + 2*2, (short)0x4000);
        inst = new Instruction(domain.readWord(pc), pc, domain);
        ea1 = inst.op1.getEA(domain, pc, (short)0x83e0);
        ea2 = inst.op2.getEA(domain, pc, (short)0x83e0);

        assertTrue(inst != null);
        assertEquals(inst.inst, Instruction.Icb);
        assertEquals(inst.name, "CB");
        assertEquals(inst.op1.byteop, true);
        assertEquals(inst.op1.type, Operand.OP_INC);
        assertEquals(inst.op1.val, 2);
        assertEquals(inst.op1.immed, 0);
        assertEquals(ea1, 0x4000);
        assertEquals(inst.op1.toString(), "*R2+");

        assertEquals(inst.op2.type, Operand.OP_INC);
        assertEquals(inst.op2.byteop, true);
        assertEquals(inst.op2.val, 2);
        assertEquals(inst.op2.immed, 0);
        assertEquals(ea2, 0x4001);
        assertEquals(inst.op2.toString(), "*R2+");
        assertEquals(inst.size, 2);

        assertEquals(domain.readWord(0x83e0+2*2), 0x4002);

    }

}
