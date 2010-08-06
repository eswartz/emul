/**
 * 
 */
package v9t9.tools.asm.assembler;

import static v9t9.engine.cpu.InstPatternMFP201.CNT;
import static v9t9.engine.cpu.InstPatternMFP201.GEN;
import static v9t9.engine.cpu.InstPatternMFP201.IMM;
import static v9t9.engine.cpu.InstPatternMFP201.NONE;
import static v9t9.engine.cpu.InstPatternMFP201.OFF;
import static v9t9.engine.cpu.InstPatternMFP201.REG;
import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.InstMFP201;
import v9t9.engine.cpu.InstPatternMFP201;
import v9t9.engine.cpu.InstTableMFP201;
import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.JumpOperand;
import v9t9.tools.asm.assembler.operand.hl.PcRelativeOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.SymbolOperand;
import v9t9.tools.asm.assembler.operand.ll.LLCountOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOffsetOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

/**
 * Parse instructions.
 * @author ejs
 *
 */
public class StandardInstructionParserStageMFP201 implements IInstructionParserStage {

	private OperandParser operandParser;
    
	public StandardInstructionParserStageMFP201() {
		this.operandParser = new OperandParser();
		operandParser.appendStage(new MachineOperandParserStageMFP201());
	}
	public StandardInstructionParserStageMFP201(OperandParser operandParser) {
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
    	
    	String name = tokenizer.currentToken().toUpperCase();
    	
    	// handle '.B'
    	t = tokenizer.nextToken();
    	if (t == '.') {
    		name += '.';
    		t = tokenizer.nextToken();
    		if (t != AssemblerTokenizer.ID) {
    			throw new ParseException("expected id after '.' in mnemonic: " + tokenizer.currentToken());
    		}
    		name += tokenizer.currentToken();
    	} else {
    		tokenizer.pushBack();
    	}
    	
    	// handle '?'
    	t = tokenizer.nextToken();
    	if (t == '?') {
    		name += '?';
    	} else {
    		tokenizer.pushBack();
    	}
    	
    	HLInstruction inst = new HLInstruction(InstructionFactoryMFP201.INSTANCE);
    	AssemblerOperand op1 = null, op2 = null, op3 = null;
    	Integer instNum = InstTableMFP201.lookupInst(name);
    	if (instNum == null)
    		return null;
    	inst.setInst(instNum);
        
    	int count = 0;
    	t = tokenizer.nextToken();
    	if (t != AssemblerTokenizer.EOF && t != ';') {
    		tokenizer.pushBack();
    		op1 = (AssemblerOperand) operandParser.parse(tokenizer);
    		count++;
    		
    		t = tokenizer.nextToken();
    		if (t == ',' || (instNum == InstMFP201.Iloop && t == ':')) {
    			op2 = (AssemblerOperand) operandParser.parse(tokenizer);
    			count++;
    			
    			t = tokenizer.nextToken();
        		if (t == ',') {
        			op3 = (AssemblerOperand) operandParser.parse(tokenizer);
        			count++;
        			
        			t = tokenizer.nextToken();
        		}
    		} 
    	}
    	  
        // ensure EOL
        t = tokenizer.nextToken();
        if (t != AssemblerTokenizer.EOF && t != ';') {
        	throw new ParseException("Trailing text on line: " + tokenizer.currentToken());
        }
        
    	// find a pattern that matches
        InstPatternMFP201[] patterns = InstTableMFP201.lookupEncodePatterns(instNum);
        if (patterns == null)
        	throw new IllegalStateException("Missing instruction pattern: " + instNum);
        
        for (InstPatternMFP201 pattern : patterns) {
        	if (pattern.length == count) {
	        	AssemblerOperand aop1 = null, aop2 = null, aop3 = null;
	        	aop1 = coerceType(inst, op1, pattern.op1);
	        	aop2 = coerceType(inst, op2, pattern.op2);
	        	aop3 = coerceType(inst, op3, pattern.op3);
	        	if (operandMatches(aop1, pattern.op1) 
	        			&& operandMatches(aop2, pattern.op2)
	        			&& operandMatches(aop3, pattern.op3)) {
	        		op1 = aop1;
	        		op2 = aop2;
	        		op3 = aop3;
	        		break;
	        	}
        	}
        }
        
        // if no match, just accept anything
        
        inst.setOp1(op1);
        inst.setOp2(op2);
        inst.setOp3(op3);
        
        return new IInstruction[] { inst };
    }

	private boolean operandMatches(AssemblerOperand op, int type) {
		switch (type) {
		case InstPatternMFP201.NONE:
			return op == null;
		case InstPatternMFP201.REG:
			return op.isRegister();
		case InstPatternMFP201.CNT:
			return op.isConst();
		case InstPatternMFP201.IMM:
			return op.isConst();
		case InstPatternMFP201.OFF:
			return op.isConst() || op instanceof PcRelativeOperand
			|| (op instanceof RegOffsOperand && ((RegOffsOperand) op).isReg(MachineOperandMFP201.PC));
		case InstPatternMFP201.GEN:
			return true;
		}
		return false;
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
			if (inst.isJumpInst()) {
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
		if (optype == InstPatternMFP201.REG
    			|| optype == InstPatternMFP201.GEN) {
    		if (op instanceof SymbolOperand)
    			return new RegisterOperand((AssemblerOperand) op);
    	}
		else if (optype == InstPatternMFP201.OFF) {
			if (inst.isJumpInst()) {
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
