/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

/**
 * @author ejs
 * 
 */
public class RegisterOperand implements AssemblerOperand {
	private final AssemblerOperand reg;

	public RegisterOperand(AssemblerOperand reg) {
		this.reg = reg;
	}
	
	@Override
	public String toString() {
		if (getReg() instanceof NumberOperand)
			return "R" + ((NumberOperand)getReg()).getValue();
		else
			return "R(" + getReg().toString() + ")";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.engine.cpu.AssemblerOperand#resolve(v9t9.tools.asm.Assembler,
	 * v9t9.engine.cpu.Instruction)
	 */
	public LLOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException {
		LLOperand op = getReg().resolve(assembler, inst);
		if (op instanceof LLImmedOperand) {
			return new LLRegisterOperand(op.getImmediate());
		}
		throw new ResolveException(op);
	}

	public AssemblerOperand getReg() {
		return reg;
	}

}
