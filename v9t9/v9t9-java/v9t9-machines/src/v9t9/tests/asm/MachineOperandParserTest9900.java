/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tests.asm;

import junit.framework.TestCase;
import v9t9.engine.cpu.BaseMachineOperand;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.tools.asm.assembler.AssemblerTokenizer;
import v9t9.tools.asm.assembler.MachineOperandFactory9900;
import v9t9.tools.asm.assembler.MachineOperandParserStage9900;
import v9t9.tools.asm.assembler.ParseException;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLAddrOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

public class MachineOperandParserTest9900 extends TestCase {

	MachineOperandParserStage9900 stage = new MachineOperandParserStage9900();
	
    public void testRX() throws Exception {
        assertTrue("r".matches(MachineOperandParserStage9900.OPT_R));
        assertTrue("R".matches(MachineOperandParserStage9900.OPT_R));
        assertTrue("".matches(MachineOperandParserStage9900.OPT_R));
    }
    
    public void testParseRegs() throws Exception {
        LLOperand op;
        BaseMachineOperand mop;
        
        op = parse("");
        assertNull(op);
        //mop = createMachineOperand9900(op);
        //assertTrue(mop.type == MachineOperand.OP_NONE && mop.val == 0 && mop.immed == 0);
        
        op = parse("4");
        assertTrue(op instanceof LLRegisterOperand);
        assertEquals(4, ((LLRegisterOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_REG && mop.val == 4 && mop.immed == 0);
        
        op = parse("R15");
        assertTrue(op instanceof LLRegisterOperand);
        assertEquals(15, ((LLRegisterOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_REG && mop.val == 15 && mop.immed == 0);
        
        op = parse("r15");
        assertTrue(op instanceof LLRegisterOperand);
        assertEquals(15, ((LLRegisterOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_REG && mop.val == 15 && mop.immed == 0);
        
        op = parse("*r11");
        assertTrue(op instanceof LLRegIndOperand);
        assertEquals(11, ((LLRegIndOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_IND && mop.val == 11 && mop.immed == 0);
        
        op = parse("*10");
        assertTrue(op instanceof LLRegIndOperand);
        assertEquals(10, ((LLRegIndOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_IND && mop.val == 10 && mop.immed == 0);
        
        op = parse("*r11+");
        assertTrue(op instanceof LLRegIncOperand);
        assertEquals(11, ((LLRegIncOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_INC && mop.val == 11 && mop.immed == 0);
        
        op = parse("*10+");
        assertTrue(op instanceof LLRegIncOperand);
        assertEquals(10, ((LLRegIncOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_INC && mop.val == 10 && mop.immed == 0);
        
    }

	private MachineOperand9900 createMachineOperand9900(LLOperand op)
			throws ResolveException {
		MachineOperandFactory9900 factory = new MachineOperandFactory9900();
		return (MachineOperand9900) op.createMachineOperand(factory);
	}

    
    public void testParseAddrs() throws Exception {
        LLOperand op;
        BaseMachineOperand mop;
        
        op = parse("@>4");
        assertTrue(op instanceof LLAddrOperand);
        assertEquals(4, ((LLAddrOperand) op).getAddress());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_ADDR && mop.val == 0 && mop.immed == 4);
        
        op = parse("@>4944");
        assertTrue(op instanceof LLAddrOperand);
        assertEquals(0x4944, ((LLAddrOperand) op).getAddress());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_ADDR && mop.val == 0 && mop.immed == 0x4944);
        
        op = parse("@>FFFE");
        assertTrue(op instanceof LLAddrOperand);
        assertEquals(-2, ((LLAddrOperand) op).getAddress());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_ADDR && mop.val == 0 && mop.immed == -2);
        
        op = parse("@>2(R4)");
        assertTrue(op instanceof LLRegOffsOperand);
        assertEquals(0x2, ((LLRegOffsOperand) op).getOffset());
        assertEquals(4, ((LLRegOffsOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_ADDR && mop.val == 4 && mop.immed == 0x2);
        
        op = parse("@2(R4)");
        assertTrue(op instanceof LLRegOffsOperand);
        assertEquals(0x2, ((LLRegOffsOperand) op).getOffset());
        assertEquals(4, ((LLRegOffsOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_ADDR && mop.val == 4 && mop.immed == 0x2);
        
        op = parse("@>-2(4)");
        assertTrue(op instanceof LLRegOffsOperand);
        assertEquals(-2, ((LLRegOffsOperand) op).getOffset());
        assertEquals(4, ((LLRegOffsOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_ADDR && mop.val == 4 && mop.immed == -2);
        
        op = parse("@>FFFE(R14)");
        assertTrue(op instanceof LLRegOffsOperand);
        assertEquals(-2, ((LLRegOffsOperand) op).getOffset());
        assertEquals(14, ((LLRegOffsOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_ADDR && mop.val == 14 && mop.immed == -2);
        
        op = parse("@>0(R14)");
        assertTrue(op instanceof LLRegOffsOperand);
        assertEquals(0, ((LLRegOffsOperand) op).getOffset());
        assertEquals(14, ((LLRegOffsOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_ADDR && mop.val == 14 && mop.immed == 0);
        
        
    }
    
    public void testParseImmed() throws Exception {
        LLOperand op;
        BaseMachineOperand mop;
        
        op = parse(">4");
        assertTrue(op instanceof LLImmedOperand);
        assertEquals(4, ((LLImmedOperand) op).getValue());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_IMMED && mop.val == 4 && mop.immed == 4);
        
        op = parse(">5555");
        assertTrue(op instanceof LLImmedOperand);
        assertEquals(0x5555, ((LLImmedOperand) op).getValue());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_IMMED && mop.val == 0x5555 && mop.immed == 0x5555);
        
        op = parse("5555");
        assertTrue(op instanceof LLRegisterOperand);
        assertEquals(5555, ((LLRegisterOperand) op).getRegister());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_REG && mop.val == 5555);
        
        op = parse(">-44");
        assertTrue(op instanceof LLImmedOperand);
        assertEquals(-0x44, ((LLImmedOperand) op).getValue());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_IMMED && mop.val == -0x44 && mop.immed == -0x44);
        
        op = parse("-44");
        assertTrue(op instanceof LLImmedOperand);
        assertEquals(-44, ((LLImmedOperand) op).getValue());
        mop = createMachineOperand9900(op);
        assertTrue(mop.type == MachineOperand9900.OP_IMMED && mop.val == -44 && mop.immed == -44);
        
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
            		MachineOperand9900 mop = createMachineOperand9900(op);
            		mop.getBits();
            		fail(element);
            	}
            } catch (IllegalArgumentException e) {
            } catch (ParseException e) {
                
            }
        }
    }

}
