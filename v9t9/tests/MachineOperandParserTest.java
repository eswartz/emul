/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tests;

import junit.framework.TestCase;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.AssemblerTokenizer;
import v9t9.tools.asm.MachineOperandParserStage;
import v9t9.tools.llinst.ParseException;

public class MachineOperandParserTest extends TestCase {

	MachineOperandParserStage stage = new MachineOperandParserStage();
	
    public void testRX() throws Exception {
        assertTrue("r".matches(MachineOperandParserStage.OPT_R));
        assertTrue("R".matches(MachineOperandParserStage.OPT_R));
        assertTrue("".matches(MachineOperandParserStage.OPT_R));
    }
    
    public void testParseRegs() throws Exception {
        MachineOperand op;
        op = parse("");
        assertTrue(op.type == MachineOperand.OP_NONE && op.val == 0 && op.immed == 0);
        op = parse("4");
        assertTrue(op.type == MachineOperand.OP_REG && op.val == 4 && op.immed == 0);
        op = parse("R15");
        assertTrue(op.type == MachineOperand.OP_REG && op.val == 15 && op.immed == 0);
        op = parse("r15");
        assertTrue(op.type == MachineOperand.OP_REG && op.val == 15 && op.immed == 0);
        op = parse("*r11");
        assertTrue(op.type == MachineOperand.OP_IND && op.val == 11 && op.immed == 0);
        op = parse("*10");
        assertTrue(op.type == MachineOperand.OP_IND && op.val == 10 && op.immed == 0);
        
    }

    
    public void testParseAddrs() throws Exception {
        MachineOperand op;
        op = parse("@>4");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 0 && op.immed == 4);
        op = parse("@>4944");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 0 && op.immed == 0x4944);
        op = parse("@>FFFE");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 0 && op.immed == -2);
        op = parse("@>2(R4)");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 4 && op.immed == 0x2);
        op = parse("@2(R4)");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 4 && op.immed == 0x2);
        op = parse("@>-2(4)");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 4 && op.immed == -2);
        op = parse("@>FFFE(R14)");
        assertTrue(op.type == MachineOperand.OP_ADDR && op.val == 14 && op.immed == -2);
        
    }
    
    public void testParseImmed() throws Exception {
        MachineOperand op;
        op = parse(">4");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == 4 && op.immed == 4);
        op = parse(">5555");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == 0x5555 && op.immed == 0x5555);
        op = parse("5555");
        assertTrue(op.type == MachineOperand.OP_REG && op.val == 5555);
        op = parse(">-44");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == -0x44 && op.immed == -0x44);
        op = parse("-44");
        assertTrue(op.type == MachineOperand.OP_IMMED && op.val == -44 && op.immed == -44);
        
    }
    private MachineOperand parse(String str) throws ParseException {
    	return (MachineOperand) stage.parse(new AssemblerTokenizer(str));
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
            	MachineOperand op = parse(element);
            	if (op != null) {
            		op.getBits();
            		fail(element);
            	}
            } catch (IllegalArgumentException e) {
            } catch (ParseException e) {
                
            }
        }
    }

}
