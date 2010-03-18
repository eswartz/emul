/**
 * 
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.assembler.ResolveException;


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



	public int getRegister() {
		return register;
	}


	public void setRegister(int number) {
		this.register = number;
	}


	@Override
	public MachineOperand createMachineOperand() throws ResolveException {
		return MachineOperand.createGeneralOperand(MachineOperand.OP_REG, (short) register);
	}

}
