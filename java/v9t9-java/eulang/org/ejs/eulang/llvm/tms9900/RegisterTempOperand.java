/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

/**
 * @author ejs
 *
 */
public class RegisterTempOperand extends BaseHLOperand {

	private final RegisterLocal local;

	/**
	 * @param reg
	 */
	public RegisterTempOperand(RegisterLocal local) {
		this.local = local;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "R" + local.getVr() + "(" + local.getName().getName() + ")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((local == null) ? 0 : local.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegisterTempOperand other = (RegisterTempOperand) obj;
		if (local == null) {
			if (other.local != null)
				return false;
		} else if (!local.equals(other.local))
			return false;
		return true;
	}


	@Override
	public boolean isMemory() {
		return false;
	}
	
	@Override
	public boolean isRegister() {
		return true;
	}
	
	/**
	 * @return the local
	 */
	public RegisterLocal getLocal() {
		return local;
	}
}
