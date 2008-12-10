/**
 * 
 */
package v9t9.tools.asm.operand.ll;

import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.ResolveException;
import v9t9.utils.Utils;

/**
 * An offset from R12
 * @author Ed
 *
 */
public class LLOffsetOperand extends LLNonImmediateOperand {

	int offset;
	public LLOffsetOperand(int value) {
		super(null);
		setOffset(value);
	}
	
	@Override
	public String toString() {
		return ">" + Utils.toHex4(offset);
	}


	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + offset;
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
		LLOffsetOperand other = (LLOffsetOperand) obj;
		if (offset != other.offset)
			return false;
		return true;
	}




	public int getOffset() {
		return offset;
	}

	public void setOffset(int count) {
		this.offset = count;
	}

	@Override
	public MachineOperand createMachineOperand() throws ResolveException {
		return MachineOperand.createGeneralOperand(MachineOperand.OP_OFFS_R12, (short) offset);
	}
}
