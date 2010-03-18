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
	public MachineOperand createMachineOperand() throws ResolveException {
		return MachineOperand.createEmptyOperand();
	}
}
