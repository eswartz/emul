/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

/**
 * @author ejs
 *
 */
public class RegOffsOperand extends RegisterOperand  {

	private final AssemblerOperand addr;

	public RegOffsOperand(AssemblerOperand addr, AssemblerOperand reg) {
		super(reg);
		this.addr = addr;
	}

	@Override
	public String toString() {
		return "@" + addr + "(" + super.toString() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
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
		RegOffsOperand other = (RegOffsOperand) obj;
		if (addr == null) {
			if (other.addr != null) {
				return false;
			}
		} else if (!addr.equals(other.addr)) {
			return false;
		}
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
	
	@Override
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		LLRegisterOperand regRes = (LLRegisterOperand) super.resolve(assembler, inst);
		LLOperand addrRes = addr.resolve(assembler, inst);
		if (addrRes instanceof LLForwardOperand)
			return new LLForwardOperand(this, 2);
		if (!(addrRes instanceof LLImmedOperand))
			throw new ResolveException(addrRes, "Expected an immediate");
		return new LLRegIndOperand(this, regRes.getRegister(), addrRes.getImmediate());
	}

	/**
	 * @return
	 */
	public AssemblerOperand getAddr() {
		return addr;
	}



	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand newReg = getReg().replaceOperand(src, dst);
		AssemblerOperand newAddr = addr.replaceOperand(src, dst);
		if (newReg != getReg() || newAddr != addr) {
			return new RegOffsOperand(newAddr, newReg);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[] { getReg(), addr };
	}
}
