package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

/**
 * *Rx
 * @author ejs
 *
 */
public class RegIndOperand extends RegisterOperand {

	public RegIndOperand(AssemblerOperand reg) {
		super(reg);
	}
 
	@Override
	public String toString() {
		return "*" + super.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getReg() == null) ? 0 : getReg().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RegIndOperand other = (RegIndOperand) obj;
		if (getReg() == null) {
			if (other.getReg() != null) {
				return false;
			}
		} else if (!getReg().equals(other.getReg())) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return true;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}
	
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		LLRegisterOperand regRes = (LLRegisterOperand) super.resolve(assembler, inst);
		return new LLRegIndOperand(regRes.getRegister());
	}
}
