/**
 * 
 */
package v9t9.tools.asm.operand;

import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

/**
 * *Rx+
 * @author ejs
 *
 */
public class RegIncOperand extends RegisterOperand {

	public RegIncOperand(AssemblerOperand reg) {
		super(reg);
	}

	@Override
	public String toString() {
		return "*" + super.toString() + "+";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getReg() == null) ? 0 : getReg().hashCode());
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
		RegIncOperand other = (RegIncOperand) obj;
		if (getReg() == null) {
			if (other.getReg() != null) {
				return false;
			}
		} else if (!getReg().equals(other.getReg())) {
			return false;
		}
		return true;
	}
	
	public MachineOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		MachineOperand regRes = super.resolve(assembler, inst);
		regRes.type = MachineOperand.OP_INC;
		return regRes;
	}
	
}
