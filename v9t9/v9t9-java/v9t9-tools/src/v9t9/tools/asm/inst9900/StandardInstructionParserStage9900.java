/*
  StandardInstructionParserStage9900.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.inst9900;

import static v9t9.machine.ti99.cpu.InstPattern9900.CNT;
import static v9t9.machine.ti99.cpu.InstPattern9900.GEN;
import static v9t9.machine.ti99.cpu.InstPattern9900.IMM;
import static v9t9.machine.ti99.cpu.InstPattern9900.NONE;
import static v9t9.machine.ti99.cpu.InstPattern9900.OFF;
import static v9t9.machine.ti99.cpu.InstPattern9900.REG;
import v9t9.common.asm.IInstruction;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.InstPattern9900;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.tools.asm.AssemblerInstruction;
import v9t9.tools.asm.AssemblerTokenizer;
import v9t9.tools.asm.HLInstruction;
import v9t9.tools.asm.IInstructionParserStage;
import v9t9.tools.asm.OperandParser;
import v9t9.tools.asm.ParseException;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.hl.JumpOperand;
import v9t9.tools.asm.operand.hl.NumberOperand;
import v9t9.tools.asm.operand.hl.RegisterOperand;
import v9t9.tools.asm.operand.hl.SymbolOperand;
import v9t9.tools.asm.operand.ll.LLCountOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOffsetOperand;
import v9t9.tools.asm.operand.ll.LLOperand;
import v9t9.tools.asm.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.operand.ll.LLRegisterOperand;

/**
 * Parse instructions.
 * @author ejs
 *
 */
public class StandardInstructionParserStage9900 implements IInstructionParserStage {

	private OperandParser operandParser;
    
	public StandardInstructionParserStage9900() {
		this.operandParser = new OperandParser();
		operandParser.appendStage(new MachineOperandParserStage9900());
	}
	public StandardInstructionParserStage9900(OperandParser operandParser) {
		this.operandParser = operandParser;
	}
	 
    /**
     * Create an instruction from a string.
     */
    public IInstruction[] parse(String descr, String string) throws ParseException {
        //this(pc);
    	AssemblerTokenizer tokenizer = new AssemblerTokenizer(string);
    	
    	int t = tokenizer.nextToken();
    	if (t != AssemblerTokenizer.ID) {
    		return null;
    	}
    	
    	HLInstruction inst = new HLInstruction(AsmInstructionFactory9900.INSTANCE);
    	String name = tokenizer.currentToken().toUpperCase();
    	AssemblerOperand op1 = null, op2 = null;
        if (name.equals("RT")) {
        	inst.setInst(Inst9900.Ib);
            op1 = new LLRegIndOperand(11);
        } else if (name.equals("NOP") || name.equals("SPIN")) {
        	inst.setInst(Inst9900.Ijmp);
            op1 = new LLPCRelativeOperand(null, 2);
        } else {
        	Integer instNum = InstTable9900.lookupInst(name);
        	if (instNum == null)
        		return null;
        	inst.setInst(instNum);
            
            InstPattern9900 pattern = InstTable9900.lookupEncodePattern(inst.getInst());
            if (pattern == null)
            	throw new IllegalStateException("Missing instruction pattern: " + inst.getInst());
            
            if (pattern.op1 != InstPattern9900.NONE) {
            	op1 = (AssemblerOperand) operandParser.parse(tokenizer);
            	op1 = coerceType(inst, op1, pattern.op1);
            	if (pattern.op2 != InstPattern9900.NONE) {
            		t = tokenizer.nextToken();
            		if (t != ',') {
            			throw new ParseException("Missing second operand: " + tokenizer.currentToken());
            		}
            		op2 =  (AssemblerOperand) operandParser.parse(tokenizer);
            		op2 = coerceType(inst, op2, pattern.op2);
            	}
            }
        }
        
        // ensure EOL
        t = tokenizer.nextToken();
        if (t != AssemblerTokenizer.EOF && t != ';') {
			if (tokenizer.currentTokenType() == AssemblerTokenizer.CHAR) {
				throw new ParseException("Trailing text on line: " + tokenizer.currentToken());
			} else {
				// assume comment
				tokenizer.skipToEOF();
			}
        }
        
        inst.setOp1(op1 != null ? op1 : null);
        inst.setOp2(op2 != null ? op2 : null);
        
        return new IInstruction[] { inst };
    }

	/** 
	 * Ensure that any ambiguously parsed operands have the expected type, modifying
	 * operands as needed.
	 * @param inst
	 */
	private AssemblerOperand coerceType(AssemblerInstruction inst, AssemblerOperand op, int optype) {
		if (op instanceof LLOperand)
			return coerceLLOperandType(inst, (LLOperand) op, optype);
		if (op instanceof AssemblerOperand)
			return coerceAssemblerOperandType(inst, (AssemblerOperand) op, optype);
		
		return op;
	}
	
	private LLOperand coerceLLOperandType(AssemblerInstruction inst, LLOperand lop, int op) {
		switch (op) {
		case NONE:
			break;
		case IMM:
			if (lop instanceof LLRegisterOperand) {
				lop = new LLImmedOperand(((LLRegisterOperand) lop).getRegister());
			}
			break;
		case CNT:
			if (lop instanceof LLRegisterOperand)
				lop = new LLCountOperand(((LLRegisterOperand) lop).getRegister());
			else if (lop instanceof LLImmedOperand)
				lop = new LLCountOperand(lop.getImmediate());
			break;
		case OFF: {
			int val = 0; 
			if (lop instanceof LLRegisterOperand) {
				val = ((LLRegisterOperand) lop).getRegister();
			} else if (lop instanceof LLImmedOperand) {
				val = lop.getImmediate();
			} else {
				break;
			}
			if (InstTable9900.isJumpInst(inst.getInst())) {
				// for a parsed inst, we don't know our pc or size,
				// so this is an absolute address
				lop = new LLImmedOperand(lop.getOriginal(), val);
			} else {
				lop = new LLOffsetOperand(val);
			}
			break;
		}
		case REG:
		case GEN:
			if (lop instanceof LLImmedOperand)
				lop = new LLRegisterOperand(lop.getImmediate());
			break;
		}
		return lop;
	}
	
	private AssemblerOperand coerceAssemblerOperandType(AssemblerInstruction inst, AssemblerOperand op, int optype) {
		if (optype == InstPattern9900.REG
    			|| optype == InstPattern9900.GEN) {
    		if (op instanceof NumberOperand
    				|| op instanceof SymbolOperand)
    			return new RegisterOperand((AssemblerOperand) op);
    	}
		else if (optype == InstPattern9900.OFF) {
			if (InstTable9900.isJumpInst(inst.getInst())) {
				if (op instanceof AssemblerOperand) {
					return new JumpOperand((AssemblerOperand) op);
				}
					
			}
		}
		return op;
	}
	public OperandParser getOperandParser() {
		return operandParser;
	}
		    


}
