/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tests.asm;

import junit.framework.TestCase;
import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.AssemblerTokenizer;
import v9t9.tools.asm.MachineOperandParserStage;
import v9t9.tools.asm.operand.ll.LLAddrOperand;
import v9t9.tools.asm.operand.ll.LLEmptyOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;
import v9t9.tools.asm.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.operand.ll.LLRegisterOperand;
import v9t9.tools.llinst.ParseException;

public class MachineOperandParserTest extends TestCase {

	MachineOperandParserStage stage = new MachineOperandParserStage();
	
    public void testRX() throws Exception {
        assertTrue("r".matches(MachineOperandParserStage.OPT_R));
        assertTrue("R".matches(MachineOperandParserStage.OPT_R));
        assertTrue("".matches(MachineOperandParserStage.OPT_R));
    }
    
    public void testParseRegs() throws Exception {
        LLOperand op;
        MachineOperand mop;
        
        op = parse("");
        assertTrue(op instanceof LLEmptyOperand);
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_NONE && mop.val == 0 && mop.immed == 0);
        
        op = parse("4");
        assertTrue(op instanceof LLRegisterOperand);
        assertEquals(4, ((LLRegisterOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_REG && mop.val == 4 && mop.immed == 0);
        
        op = parse("R15");
        assertTrue(op instanceof LLRegisterOperand);
        assertEquals(15, ((LLRegisterOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_REG && mop.val == 15 && mop.immed == 0);
        
        op = parse("r15");
        assertTrue(op instanceof LLRegisterOperand);
        assertEquals(15, ((LLRegisterOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_REG && mop.val == 15 && mop.immed == 0);
        
        op = parse("*r11");
        assertTrue(op instanceof LLRegIndOperand);
        assertEquals(11, ((LLRegIndOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_IND && mop.val == 11 && mop.immed == 0);
        
        op = parse("*10");
        assertTrue(op instanceof LLRegIndOperand);
        assertEquals(10, ((LLRegIndOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_IND && mop.val == 10 && mop.immed == 0);
        
        op = parse("*r11+");
        assertTrue(op instanceof LLRegIncOperand);
        assertEquals(11, ((LLRegIncOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_INC && mop.val == 11 && mop.immed == 0);
        
        op = parse("*10+");
        assertTrue(op instanceof LLRegIncOperand);
        assertEquals(10, ((LLRegIncOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_INC && mop.val == 10 && mop.immed == 0);
        
    }

    
    public void testParseAddrs() throws Exception {
        LLOperand op;
        MachineOperand mop;
        
        op = parse("@>4");
        assertTrue(op instanceof LLAddrOperand);
        assertEquals(4, ((LLAddrOperand) op).getAddress());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_ADDR && mop.val == 0 && mop.immed == 4);
        
        op = parse("@>4944");
        assertTrue(op instanceof LLAddrOperand);
        assertEquals(0x4944, ((LLAddrOperand) op).getAddress());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_ADDR && mop.val == 0 && mop.immed == 0x4944);
        
        op = parse("@>FFFE");
        assertTrue(op instanceof LLAddrOperand);
        assertEquals(-2, ((LLAddrOperand) op).getAddress());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_ADDR && mop.val == 0 && mop.immed == -2);
        
        op = parse("@>2(R4)");
        assertTrue(op instanceof LLRegIndOperand);
        assertEquals(0x2, ((LLRegIndOperand) op).getOffset());
        assertEquals(4, ((LLRegIndOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_ADDR && mop.val == 4 && mop.immed == 0x2);
        
        op = parse("@2(R4)");
        assertTrue(op instanceof LLRegIndOperand);
        assertEquals(0x2, ((LLRegIndOperand) op).getOffset());
        assertEquals(4, ((LLRegIndOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_ADDR && mop.val == 4 && mop.immed == 0x2);
        
        op = parse("@>-2(4)");
        assertTrue(op instanceof LLRegIndOperand);
        assertEquals(-2, ((LLRegIndOperand) op).getOffset());
        assertEquals(4, ((LLRegIndOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_ADDR && mop.val == 4 && mop.immed == -2);
        
        op = parse("@>FFFE(R14)");
        assertTrue(op instanceof LLRegIndOperand);
        assertEquals(-2, ((LLRegIndOperand) op).getOffset());
        assertEquals(14, ((LLRegIndOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_ADDR && mop.val == 14 && mop.immed == -2);
        
        
    }
    
    public void testParseImmed() throws Exception {
        LLOperand op;
        MachineOperand mop;
        
        op = parse(">4");
        assertTrue(op instanceof LLImmedOperand);
        assertEquals(4, ((LLImmedOperand) op).getValue());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_IMMED && mop.val == 4 && mop.immed == 4);
        
        op = parse(">5555");
        assertTrue(op instanceof LLImmedOperand);
        assertEquals(0x5555, ((LLImmedOperand) op).getValue());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_IMMED && mop.val == 0x5555 && mop.immed == 0x5555);
        
        op = parse("5555");
        assertTrue(op instanceof LLRegisterOperand);
        assertEquals(5555, ((LLRegisterOperand) op).getRegister());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_REG && mop.val == 5555);
        
        op = parse(">-44");
        assertTrue(op instanceof LLImmedOperand);
        assertEquals(-0x44, ((LLImmedOperand) op).getValue());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_IMMED && mop.val == -0x44 && mop.immed == -0x44);
        
        op = parse("-44");
        assertTrue(op instanceof LLImmedOperand);
        assertEquals(-44, ((LLImmedOperand) op).getValue());
        mop = op.createMachineOperand();
        assertTrue(mop.type == MachineOperand.OP_IMMED && mop.val == -44 && mop.immed == -44);
        
    }
    private LLOperand parse(String str) throws ParseException {
    	return (LLOperand) stage.parse(new AssemblerTokenizer(str));
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
            	LLOperand op = parse(element);
            	if (op != null) {
            		MachineOperand mop = op.createMachineOperand();
            		mop.getBits();
            		fail(element);
            	}
            } catch (IllegalArgumentException e) {
            } catch (ParseException e) {
                
            }
        }
    }

}
