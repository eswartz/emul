/**
 * 
 */
package v9t9.tools.asm.assembler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.operand.ll.LLEmptyOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegDecOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;
import v9t9.tools.asm.assembler.operand.ll.LLScaledRegOffsOperand;

/**
 * Parse a MFP201 operand and create an LLOperand.
 * @author ejs
 *
 */
public class MachineOperandParserStageMFP201 implements IOperandParserStage {

    public final static String REG_NAME = "((?i)R(?:\\d+)|SP|PC|SR)";
    public final static String OPT_R_REG_NAME = "((?i)R?(?:\\d+)|SP|PC|SR)";
    public final static String IMMED = "((?i)(?:>?)(?:(?:[+|-])?)[0-9A-F]+)";
    
    final static Pattern REGISTER_PATTERN = Pattern.compile(REG_NAME);
    final static Pattern REG_INDINCDEC_PATTERN = Pattern.compile(
            //       		1       		2      
            "(?:\\*" + OPT_R_REG_NAME + "((?:\\+|-)?))");

    final static Pattern REG_OFFS_PATTERN = Pattern.compile(
            //         1          2
            "(?:@" + IMMED + "\\(([^))]+)\\))|" +
            //        3
            "(?:&" + IMMED + ")|" +
            //        4
            "(?:" + IMMED + ")"
            );
    
    final static Pattern JUMP_PATTERN = Pattern.compile(
            //       1       2
            "\\$(?:([+-])" + IMMED + ")?"
            );

    public Operand parse(AssemblerTokenizer tokenizer) throws ParseException {
    	StringBuilder builder = new StringBuilder();
    	int t ;
    	while ((t = tokenizer.nextToken()) != AssemblerTokenizer.EOF && t != ',') {
			builder.append(tokenizer.currentToken());
		}
    	if (t == ',')
    		tokenizer.pushBack();
    	return parse(builder.toString());
    }
	private Operand parse(String string) throws ParseException {
		int val = 0;
		short immed = 0;
		
        if (string == null || string.length() == 0) {
            return LLEmptyOperand.INSTANCE;
        }
        Matcher matcher;
        
        matcher = REGISTER_PATTERN.matcher(string);
        if (matcher.matches()) {
        	return new LLRegisterOperand(getRegNum(matcher.group(1)));
        }
        
        matcher = REG_INDINCDEC_PATTERN.matcher(string);
        if (matcher.matches()) {
        	val = getRegNum(matcher.group(1));
            if ("+".equals(matcher.group(2))) {
                // *R0+
            	return new LLRegIncOperand(val);
            } else if ("-".equals(matcher.group(2))) {
                // *R0-
            	return new LLRegDecOperand(val);
            } else {
                // *R0
            	return new LLRegIndOperand(null, val);
            }
        }
        
        matcher = REG_OFFS_PATTERN.matcher(string);
        if (matcher.matches()) {
            if (matcher.group(1) != null) {
                immed = parseImmed(matcher.group(1));
                return parseRegOffs(immed, matcher.group(2));
            } else if (matcher.group(3) != null) {
            	immed = parseImmed(matcher.group(3));
                return new LLRegOffsOperand(null, MachineOperandMFP201.SR, immed);
            } else {
                // immed
            	immed = parseImmed(matcher.group(4));
                return new LLImmedOperand(immed);
            }
        } else {
        	matcher = JUMP_PATTERN.matcher(string);
        	if (matcher.matches()) {
        		int op = 0;
        		if (matcher.group(2) != null) {
	        		op = parseImmed(matcher.group(2));
	        		if (matcher.group(1) != null && matcher.group(1).equals("-"))
	        			op = -op;
        		}
        		val = op;
        		return new LLPCRelativeOperand(null, val);
        	} else {
        		return null;
        	}
        }
	}
	
	 final static String REG_MUL = OPT_R_REG_NAME + "\\*(\\d+)";
	    final static Pattern REG_ADD_SCALED = Pattern.compile(
	            //1		2						3
	            "(" + OPT_R_REG_NAME + "\\+" + OPT_R_REG_NAME + ")|" +
	            //4		5 6						7
	            "(" + REG_MUL + "\\+" + OPT_R_REG_NAME + ")|" +
	            //8		9						10  11
	            "(" + OPT_R_REG_NAME + "\\+" + REG_MUL + ")|" +
	            //12   13 14
	            "(" + REG_MUL + ")"
	            );
	    

	private Operand parseRegOffs(short immed, String str) throws ParseException {
		if (str.matches(OPT_R_REG_NAME)) {
			return new LLRegOffsOperand(null, getRegNum(str), immed);
		}
		
		Matcher matcher = REG_ADD_SCALED.matcher(str);
		if (matcher.matches()) {
			if (matcher.group(1) != null) {
				return new LLScaledRegOffsOperand(null, immed,
						getRegNum(matcher.group(2)),
						getRegNum(matcher.group(3)), 1);
			}
			if (matcher.group(4) != null) {
				return new LLScaledRegOffsOperand(null, immed,
						getRegNum(matcher.group(7)),
						getRegNum(matcher.group(5)), 
						getScale(matcher.group(6)));
			}
			if (matcher.group(8) != null) {
				return new LLScaledRegOffsOperand(null, immed,
						getRegNum(matcher.group(9)),
						getRegNum(matcher.group(10)), 
						getScale(matcher.group(11)));
			}
			if (matcher.group(12) != null) {
				return new LLScaledRegOffsOperand(null, immed,
						MachineOperandMFP201.SR,
						getRegNum(matcher.group(13)), 
						getScale(matcher.group(14)));
			}
		}
		
		return null;
	}
	private int getScale(String str) throws ParseException {
		int val = Integer.parseInt(str);
		if (val < 0 || val >= 256 || ((val & (val - 1)) != 0))
			throw new ParseException("expected a power of two between 1 and 128");
		return val;
	}
	private int getRegNum(String reg) {
		int val;
		if ("PC".equalsIgnoreCase(reg)) {
			val = MachineOperandMFP201.PC;
		} else if ("SP".equalsIgnoreCase(reg)) {
			val = MachineOperandMFP201.SP;
		} else if ("SR".equalsIgnoreCase(reg)) {
			val = MachineOperandMFP201.SR;
		} else {
			if (reg.charAt(0) == 'R' || reg.charAt(0) == 'r') {
				reg = reg.substring(1);
			}
			val = Integer.parseInt(reg);
		}
		return val;
	}
	
    private short parseImmed(String string) {
        int radix = 10;
        if (string.charAt(0) == '>') {
            radix = 16;
            string = string.substring(1);
        }
        return (short) Integer.parseInt(string, radix);
    }

}
