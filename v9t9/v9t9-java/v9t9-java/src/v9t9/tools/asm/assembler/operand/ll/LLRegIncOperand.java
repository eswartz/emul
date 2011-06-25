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
public class LLRegIncOperand extends LLNonImmediateOperand implements Operand {

	int register;
	
	public LLRegIncOperand(int reg) {
		super(null);
		setRegister(reg);
	}

	@Override
	public String toString() {
		return "*R" + register + "+";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + register;
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLRegIncOperand other = (LLRegIncOperand) obj;
		if (register != other.register)
			return false;
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

	public int getRegister() {
		return register;
	}


	public void setRegister(int number) {
		this.register = number;
	}

	@Override
	public MachineOperand createMachineOperand(IMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createRegIncOperand(this);
	}
}
