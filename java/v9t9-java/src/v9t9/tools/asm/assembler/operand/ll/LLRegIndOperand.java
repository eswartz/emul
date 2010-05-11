/**
 * 
 */
package v9t9.tools.asm.assembler.operand.ll;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;

/**
 * @author Ed
 *
 */
public class LLRegIndOperand extends LLOperand implements Operand {

	int register;
	int offset;
	
	public LLRegIndOperand(int reg) {
		super(null);
		setRegister(reg);
		setOffset(0);
	}
	public LLRegIndOperand(RegOffsOperand original, int reg, int offset) {
		super(original);
		setRegister(reg);
		setOffset(offset);
	}

	@Override
	public String toString() {
		if (offset == 0)
			return "*R" + register;
		return "@>" + HexUtils.toHex4(offset) + "(R" + register + ")";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + offset;
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
		LLRegIndOperand other = (LLRegIndOperand) obj;
		if (offset != other.offset)
			return false;
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


	public int getOffset() {
		return offset;
	}


	public void setOffset(int offset) {
		this.offset = offset;
	}


	@Override
	public boolean hasImmediate() {
		return true;
	}
	
	@Override
	public int getSize() {
		return offset != 0 ? 2 : 0;
	}
	
	@Override
	public int getImmediate() {
		return offset;
	}
	
	@Override
	public MachineOperand createMachineOperand() throws ResolveException {
		if (offset == 0)
			return MachineOperand.createGeneralOperand(MachineOperand.OP_IND, (short) register);
		else
			return MachineOperand.createGeneralOperand(MachineOperand.OP_ADDR, (short) register, (short) offset);
	}
}
