/**
 * 
 */
package v9t9.tools.asm.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.operand.ll.LLAddrOperand;
import v9t9.tools.asm.operand.ll.LLForwardOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLJumpOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class AddrOperand implements AssemblerOperand {

	private final AssemblerOperand addr;

	public AddrOperand(AssemblerOperand addr) {
		this.addr = addr;
	}

	@Override
	public String toString() {
		return "@" + addr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
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
		if (addr == null) {
			if (other.addr != null) {
				return false;
			}
		} else if (!addr.equals(other.addr)) {
			return false;
		}
		return true;
	}
	
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		LLOperand lop = addr.resolve(assembler, inst);
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
	
}
