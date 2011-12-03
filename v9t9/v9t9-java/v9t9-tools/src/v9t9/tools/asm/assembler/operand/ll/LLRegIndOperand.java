/**
 * 
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.engine.asm.ResolveException;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public class LLRegIndOperand extends LLOperand implements Operand {

	int register;
	
	public LLRegIndOperand(int reg) {
		super(null);
		setRegister(reg);
	}
	public LLRegIndOperand(AssemblerOperand original, int reg) {
		super(original);
		setRegister(reg);
	}

	@Override
	public String toString() {
		return "*R" + register;
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLRegIndOperand other = (LLRegIndOperand) obj;
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
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}

	
	public int getRegister() {
		return register;
	}


	public void setRegister(int number) {
		this.register = number;
	}

	@Override
	public boolean hasImmediate() {
		return false;
	}
	
	@Override
	public int getSize() {
		return 0;
	}
	
	@Override
	public int getImmediate() {
		return 0;
	}
	
	@Override
	public MachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createRegIndOperand(this);
	}
}
