/**
 * 
 */
package v9t9.tools.asm;

import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.MachineOperand;

/**
 * @author ejs
 *
 */
public class NumberOperand implements AssemblerOperand {

	private int i;

	public NumberOperand(int i) {
		this.i = i;
	}
	
	public void negate() {
		i = -i;
	}
	@Override
	public String toString() {
		return "" + i;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NumberOperand other = (NumberOperand) obj;
		if (i != other.i) {
			return false;
		}
		return true;
	}

	public MachineOperand resolve(Assembler assembler, Instruction inst) throws ResolveException {
		return MachineOperand.createImmediate(i);
	}

	public int getValue() {
		return i;
	}
	
}
