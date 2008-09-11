/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tests;

import junit.framework.TestCase;
import v9t9.engine.cpu.MachineOperand;

public class MachineOperandParserTest extends TestCase {

    public void testRX() throws Exception {
        assertTrue("r".matches(MachineOperand.OPT_R));
        assertTrue("R".matches(MachineOperand.OPT_R));
        assertTrue("".matches(MachineOperand.OPT_R));
    }
    
    public void testParseRegs() throws Exception {
        MachineOperand op;
        op = new MachineOperand("");
        assertTrue(op.type == MachineOperand.OP_NONE && op.val == 0 && op.immed == 0);
        op = new MachineOperand("4");
        assertTrue(op.type == MachineOperand.OP_REG && op.val == 4 && op.immed == 0);
        op = new MachineOperand("R15");
        assertTrue(op.type == MachineOperand.OP_REG && op.val == 15 && op.immed == 0);
        op = new MachineOperand("r15");
        assertTrue(op.type == MachineOperand.OP_REG && op.val == 15 && op.immed == 0);
        op = new MachineOperand("*r11");
        assertTrue(op.type == MachineOperand.OP_IND && op.val == 11 && op.immed == 0);
        op = new MachineOperand("*10");
        assertTrue(op.type == MachineOperand.OP_IND && op.val == 10 && op.immed == 0);
        
    }

    
    public void testParseAddrs() throws Exception {
        MachineOperand op;
        op = new MachineOperand("@>4");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 0 && op.immed == 4);
        op = new MachineOperand("@>4944");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 0 && op.immed == 0x4944);
        op = new MachineOperand("@>FFFE");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 0 && op.immed == -2);
        op = new MachineOperand("@>2(R4)");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 4 && op.immed == 0x2);
        op = new MachineOperand("@2(R4)");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 4 && op.immed == 0x2);
        op = new MachineOperand("@>-2(4)");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 4 && op.immed == -2);
        op = new MachineOperand("@>FFFE(R14)");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 14 && op.immed == -2);
        
    }
    
    public void testParseImmed() throws Exception {
        MachineOperand op;
        op = new MachineOperand(">4");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == 4 && op.immed == 4);
        op = new MachineOperand(">5555");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == 0x5555 && op.immed == 0x5555);
        op = new MachineOperand("5555");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == 5555 && op.immed == 5555);
        op = new MachineOperand(">-44");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == -0x44 && op.immed == -0x44);
        op = new MachineOperand("-44");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == -44 && op.immed == -44);
        
    }

    public void testFail() throws Exception {
        String[] baddies = {
          "R5+",
          "R44",
          "*+",
          "@>",
          "@>()",
          "@>R(3)",
          "@>4(R)",
          "@>9(99)",
          "@>9(R-9)",
          "@>40(R0)",
          ">--",
        };
        for (String element : baddies) {
            try {
                new MachineOperand(element);
                fail(element);
            } catch (IllegalArgumentException e) {
                
            }
        }
    }

}
