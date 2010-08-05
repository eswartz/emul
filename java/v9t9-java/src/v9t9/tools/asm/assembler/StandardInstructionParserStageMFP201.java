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
import v9t9.engine.cpu.InstPatternMFP201;
import v9t9.engine.cpu.InstTableMFP201;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.JumpOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.SymbolOperand;
import v9t9.tools.asm.assembler.operand.ll.LLCountOperand;
import v9t9.tools.asm.assembler.operand.ll.LLEmptyOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOffsetOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

/**
 * Parse low-level instructions (no symbols or anything).
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
        
        InstPatternMFP201 pattern = InstTableMFP201.lookupEncodePattern(inst.getInst());
        if (pattern == null)
        	throw new IllegalStateException("Missing instruction pattern: " + inst.getInst());
        
        if (pattern.op1 != InstPatternMFP201.NONE) {
        	op1 = (AssemblerOperand) operandParser.parse(tokenizer);
        	op1 = coerceType(inst, op1, pattern.op1);
        	if (pattern.op2 != InstPatternMFP201.NONE) {
        		t = tokenizer.nextToken();
        		if (t != ',') {
        			throw new ParseException("Missing second operand: " + tokenizer.currentToken());
        		}
        		op2 = (AssemblerOperand) operandParser.parse(tokenizer);
        		op2 = coerceType(inst, op2, pattern.op2);
        		
        		if (pattern.op3 != InstPatternMFP201.NONE) {
            		t = tokenizer.nextToken();
            		if (t != ',') {
            			throw new ParseException("Missing third operand: " + tokenizer.currentToken());
            		}
            		op3 = (AssemblerOperand) operandParser.parse(tokenizer);
            		op3 = coerceType(inst, op3, pattern.op3);
            	}
        	}
        }
        
        // ensure EOL
        t = tokenizer.nextToken();
        if (t != AssemblerTokenizer.EOF && t != ';') {
        	throw new ParseException("Trailing text on line: " + tokenizer.currentToken());
        }
        
        inst.setOp1(op1 != null ? op1 : LLEmptyOperand.INSTANCE);
        inst.setOp2(op2 != null ? op2 : LLEmptyOperand.INSTANCE);
        inst.setOp3(op3 != null ? op3 : LLEmptyOperand.INSTANCE);
        
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
    		if (op instanceof NumberOperand
    				|| op instanceof SymbolOperand)
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
