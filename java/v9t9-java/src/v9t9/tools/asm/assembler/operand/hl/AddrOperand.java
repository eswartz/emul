/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLAddrOperand;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLJumpOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class AddrOperand extends BaseOperand {

	private final AssemblerOperand addr;

	public AddrOperand(AssemblerOperand addr) {
		this.addr = addr;
	}

	@Override
	public String toString() {
		return "@" + getAddr();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getAddr() == null) ? 0 : getAddr().hashCode());
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
		AddrOperand other = (AddrOperand) obj;
		if (getAddr() == null) {
			if (other.getAddr() != null) {
				return false;
			}
		} else if (!getAddr().equals(other.getAddr())) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isMemory() {
		return true;
	}
	@Override
	public boolean isRegister() {
		return false;
	}

	
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		LLOperand lop = getAddr().resolve(assembler, inst);
		if (lop instanceof LLForwardOperand)
			return new LLForwardOperand(this, 2);
		
		if (lop instanceof LLJumpOperand) {
			lop = new LLAddrOperand(this, inst.getPc() + ((LLJumpOperand)lop).getOffset());
		} else if (lop instanceof LLImmedOperand) {
			lop = new LLAddrOperand(this, lop.getImmediate());
		} else
			throw new ResolveException(lop, "Expected an immediate");
		return lop;
	}

	public AssemblerOperand getAddr() {
		return addr;
	}
	
}
