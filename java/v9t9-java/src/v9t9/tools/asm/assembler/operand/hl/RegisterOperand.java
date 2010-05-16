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
public class RegisterOperand implements AssemblerOperand, IRegisterOperand {
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
	
	protected static LLOperand resolveRegister(Assembler assembler, IInstruction inst, AssemblerOperand reg) throws ResolveException {
		LLOperand op = reg.resolve(assembler, inst);
		if (op instanceof LLImmedOperand) {
			return new LLRegisterOperand(op.getImmediate());
		}
		throw new ResolveException(op);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return true;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.engine.cpu.AssemblerOperand#resolve(v9t9.tools.asm.Assembler,
	 * v9t9.engine.cpu.Instruction)
	 */
	public LLOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException {
		return resolveRegister(assembler, inst, reg);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.IRegisterOperand#getReg()
	 */
	public AssemblerOperand getReg() {
		return reg;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.IRegisterOperand#isReg(int)
	 */
	public boolean isReg(int reg) {
		if (getReg() instanceof NumberOperand) {
			return ((NumberOperand) getReg()).getValue() == reg;
		} else if (getReg() instanceof IRegisterOperand) {
			return ((IRegisterOperand) getReg()).isReg(reg);
		} else {
			return false;
		}
	}

}
