/**
 * 
 */
package v9t9.tools.asm.operand.ll;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.ResolveException;

/**
 * A shift count
 * @author Ed
 *
 */
public class LLCountOperand extends LLNonImmediateOperand {

	int count;
	public LLCountOperand(int value) {
		super(null);
		setCount(value);
	}

	
	@Override
	public String toString() {
		return ">" + HexUtils.toHex4(count);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count;
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
		LLCountOperand other = (LLCountOperand) obj;
		if (count != other.count)
			return false;
		return true;
	}



	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public MachineOperand createMachineOperand() throws ResolveException {
		return MachineOperand.createGeneralOperand(MachineOperand.OP_CNT, (short) count);
	}
}
