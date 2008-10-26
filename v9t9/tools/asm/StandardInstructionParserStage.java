/**
 * 
 */
package v9t9.tools.asm;

import static v9t9.engine.cpu.InstEncodePattern.CNT;
import static v9t9.engine.cpu.InstEncodePattern.GEN;
import static v9t9.engine.cpu.InstEncodePattern.IMM;
import static v9t9.engine.cpu.InstEncodePattern.NONE;
import static v9t9.engine.cpu.InstEncodePattern.OFF;
import static v9t9.engine.cpu.InstEncodePattern.REG;
import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.InstEncodePattern;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.operand.JumpOperand;
import v9t9.tools.asm.operand.NumberOperand;
import v9t9.tools.asm.operand.RegisterOperand;
import v9t9.tools.asm.operand.SymbolOperand;
import v9t9.tools.llinst.ParseException;

/**
 * Parse low-level instructions (no symbols or anything).
 * @author ejs
 *
 */
public class StandardInstructionParserStage implements IInstructionParserStage {

	private OperandParser operandParser;
    
	public StandardInstructionParserStage() {
		this.operandParser = OperandParser.STANDARD;
	}
	public StandardInstructionParserStage(OperandParser operandParser) {
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
    	
    	RawInstruction inst = new RawInstruction();
        inst.name = tokenizer.currentToken().toUpperCase();
        inst.size = 2;
        Operand op1 = null, op2 = null;
        if (inst.name.equals("RT")) {
        	inst.name = "B";
        	inst.inst = InstructionTable.Ib;
        	inst.size = 2;
            op1 = new MachineOperand(MachineOperand.OP_IND);
            ((MachineOperand)op1).val = 11;
        } else if (inst.name.equals("NOP")) {
        	inst.name = "JMP";
        	inst.inst = InstructionTable.Ijmp;
        	inst.size = 2;
            op1 = new MachineOperand(MachineOperand.OP_JUMP);
            ((MachineOperand)op1).val = 2;
        } else {
        	inst.inst = InstructionTable.lookupInst(inst.name);
            if (inst.inst < 0) {
            	return null;
            }
            
            InstEncodePattern pattern = InstructionTable.lookupEncodePattern(inst.inst);
            if (pattern == null)
            	throw new IllegalStateException("Missing instruction pattern: " + inst.name);
            
            if (pattern.op1 != InstEncodePattern.NONE) {
            	op1 = operandParser.parse(tokenizer);
            	op1 = coerceType(inst, op1, pattern.op1);
            	if (pattern.op2 != InstEncodePattern.NONE) {
            		t = tokenizer.nextToken();
            		if (t != ',') {
            			throw new ParseException("Missing second operand: " + tokenizer.currentToken());
            		}
            		op2 = operandParser.parse(tokenizer);
            		op2 = coerceType(inst, op2, pattern.op2);
            	}
            }
        }
        
        // ensure EOL
        t = tokenizer.nextToken();
        if (t != AssemblerTokenizer.EOF && t != ';') {
        	throw new ParseException("Trailing text on line: " + tokenizer.currentToken());
        }
        
        inst.op1 = op1 != null ? op1 : new MachineOperand(MachineOperand.OP_NONE);
        inst.op2 = op2 != null ? op2 : new MachineOperand(MachineOperand.OP_NONE);
        
        return new RawInstruction[] { inst };
    }

	/** 
	 * Ensure that any ambiguously parsed operands have the expected type, modifying
	 * operands as needed.
	 * @param inst
	 */
	private Operand coerceType(RawInstruction inst, Operand op, int optype) {
		if (op instanceof MachineOperand)
			return coerceMachineOperandType(inst, (MachineOperand) op, optype);
		if (op instanceof AssemblerOperand)
			return coerceAssemblerOperandType(inst, (AssemblerOperand) op, optype);
		
		return op;
	}
	
	private Operand coerceMachineOperandType(RawInstruction inst, MachineOperand mop, int op) {
		switch (op) {
		case NONE:
			break;
		case IMM:
			if (mop.type == MachineOperand.OP_REG) {
				mop.type = MachineOperand.OP_IMMED;
				mop.immed = (short) mop.val;
			}
			break;
		case CNT:
			if (mop.type == MachineOperand.OP_REG
					|| mop.type == MachineOperand.OP_IMMED)
				mop.type = MachineOperand.OP_CNT;
			break;
		case OFF:
			if (mop.type == MachineOperand.OP_REG || mop.type == MachineOperand.OP_IMMED) {
				if (inst.isJumpInst()) {
					// for a parsed inst, we don't know our pc or size,
					// so this is an absolute address
					if (mop.type == MachineOperand.OP_REG)
						mop.immed = (short) mop.val;
					mop.type = MachineOperand.OP_IMMED;
					mop.val = mop.immed;
				} else {
					mop.type = MachineOperand.OP_OFFS_R12;
				}
			}
			break;
		case REG:
			if (mop.type == MachineOperand.OP_IMMED)
				mop.type = MachineOperand.OP_REG;
			break;
		case GEN:
			if (mop.type == MachineOperand.OP_IMMED)
				mop.type = MachineOperand.OP_REG;
			break;
		}
		return mop;
	}
	
	private Operand coerceAssemblerOperandType(RawInstruction inst, AssemblerOperand op, int optype) {
		if (optype == InstEncodePattern.REG
    			|| optype == InstEncodePattern.GEN) {
    		if (op instanceof NumberOperand
    				|| op instanceof SymbolOperand)
    			return new RegisterOperand((AssemblerOperand) op);
    	}
		else if (optype == InstEncodePattern.OFF) {
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
