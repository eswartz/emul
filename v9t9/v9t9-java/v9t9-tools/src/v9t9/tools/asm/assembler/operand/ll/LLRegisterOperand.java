/**
 * 
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.engine.asm.ResolveException;
import v9t9.engine.cpu.MachineOperand;


/**
 * @author Ed
 *
 */
public class LLRegisterOperand extends LLNonImmediateOperand {

	int register;
	public LLRegisterOperand(int regnum) {
		super(null);
		setRegister(regnum);
	}

	@Override
	public String toString() {
		return "R" + register;
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
		LLRegisterOperand other = (LLRegisterOperand) obj;
		if (register != other.register)
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


	public int getRegister() {
		return register;
	}


	public void setRegister(int number) {
		this.register = number;
	}


	@Override
	public MachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createRegisterOperand(this);
	}

}
