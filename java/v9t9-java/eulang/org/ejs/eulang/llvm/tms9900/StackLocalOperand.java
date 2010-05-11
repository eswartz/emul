/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;


/**
 * @author ejs
 *
 */
public class StackLocalOperand extends BaseHLOperand {

	private final StackLocal local;

	public StackLocalOperand(StackLocal local) {
		assert local != null;
		this.local = local;
		
	}
	
	@Override
	public String toString() {
		return "Local." + local.getName().getName();
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
		StackLocalOperand other = (StackLocalOperand) obj;
		if (local == null) {
			if (other.local != null)
				return false;
		} else if (!local.equals(other.local))
			return false;
		return true;
	}

	/**
	 * @return the local
	 */
	public StackLocal getLocal() {
		return local;
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


}
