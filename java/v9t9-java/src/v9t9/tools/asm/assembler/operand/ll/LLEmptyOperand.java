/**
 * 
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.ResolveException;

/**
 * @author Ed
 *
 */
public class LLEmptyOperand extends LLNonImmediateOperand implements Operand {
	public LLEmptyOperand() {
		super(null);
	}
	
	@Override
	public String toString() {
		return "<empty>";
	}
	
	@Override
	public int hashCode() {
		int result = 1;
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
		return true;
	}

	@Override
	public boolean isMemory() {
		return false;
	}
	@Override
	public boolean isRegister() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public MachineOperand createMachineOperand(IMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createEmptyOperand();
	}
}
