/*
  MachineOperandParserStage9900.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.inst9900;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.common.asm.IOperand;
import v9t9.tools.asm.AssemblerTokenizer;
import v9t9.tools.asm.IOperandParserStage;
import v9t9.tools.asm.ParseException;
import v9t9.tools.asm.operand.ll.LLAddrOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.operand.ll.LLRegisterOperand;

/**
 * Parse a 9900 operand and create an LLOperand.
 * @author ejs
 *
 */
public class MachineOperandParserStage9900 implements IOperandParserStage {

    public final static String OPT_R = "(?:R|r)?";
    //public final static String REG_NUM = "([0-9]|(?:1[0-5]))";
    public final static String REG_NUM = "(\\d+)";
    public final static String IMMED = "((?:>?)(?:(?:[+|-])?)[0-9A-Fa-f]+)";
    final static Pattern OPERAND_PATTERN = Pattern.compile(
            //       1                2        3
            "(?:(\\*?)" + OPT_R + REG_NUM + "(\\+?))|" +
            //         4          5             6 
            "(?:@" + IMMED + "(\\(" + OPT_R + REG_NUM + "\\))?)|" +
            //         7
            "(?:" + IMMED + ")"
            );
    
    final static Pattern JUMP_PATTERN = Pattern.compile(
            //       1       2
            "\\$(?:([+-])" + IMMED + ")?"
            );

    public IOperand parse(AssemblerTokenizer tokenizer) throws ParseException {
    	StringBuilder builder = new StringBuilder();
    	int t ;
    	while ((t = tokenizer.nextToken()) != AssemblerTokenizer.EOF && t != ',') {
			builder.append(tokenizer.currentToken());
		}
    	if (t == ',')
    		tokenizer.pushBack();
    	return parse(builder.toString());
    }
	private IOperand parse(String string) throws ParseException {
		//int type = 0;
		int val = 0;
		short immed = 0;
		
        if (string == null || string.length() == 0) {
            return null;
        }
        Matcher matcher = OPERAND_PATTERN.matcher(string);
        if (matcher.matches()) {
            if (matcher.group(2) != null) {
                val = Integer.parseInt(matcher.group(2));
                if (matcher.group(1).length() > 0) {
                    if (matcher.group(3) != null && matcher.group(3).length() > 0) {
                        // *R0+
                    	return new LLRegIncOperand(val);
                    } else {
                        // *R0
                    	return new LLRegIndOperand(null, val);
                    }
                } else {
                    // R9
                    if (matcher.group(3) != null && matcher.group(3).length() != 0) {
                    	throw new ParseException("Illegal register operand: " + string);
                    }
                    return new LLRegisterOperand(val);
                }
            } else if (matcher.group(4) != null) {
                immed = parseImmed(matcher.group(4));
                //type = LLOperand.OP_ADDR;
                if (matcher.group(5) != null && matcher.group(5).length() > 0) {
                    // @>4(r5)
                    val = Integer.parseInt(matcher.group(6));
                    if (val == 0) {
                    	throw new ParseException("Illegal index register (0): " + string);
                    }
                    return new LLRegOffsOperand(null, val, immed);
                } else {
                    // @>5
                    return new LLAddrOperand(null, immed);
                }
            } else {
                // immed
                //type = LLOperand.OP_IMMED;
                /*val =*/ immed = parseImmed(matcher.group(7));
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
        		//type = LLOperand.OP_JUMP;
        		val = op;
        		return new LLPCRelativeOperand(null, val);
        	} else {
        		return null;
        	}
        }
        
        /*
        LLOperand operand = new LLOperand(type);
        operand.val = val;
        operand.immed = immed;
        
        return operand;
        */
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
