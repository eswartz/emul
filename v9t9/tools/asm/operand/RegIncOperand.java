/**
 * 
 */
package v9t9.tools.asm.operand;

import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.Instruction;
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
		result = prime * result + ((reg == null) ? 0 : reg.hashCode());
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
		if (reg == null) {
			if (other.reg != null) {
				return false;
			}
		} else if (!reg.equals(other.reg)) {
			return false;
		}
		return true;
	}
	
	public MachineOperand resolve(Assembler assembler, Instruction inst)
			throws ResolveException {
		MachineOperand regRes = super.resolve(assembler, inst);
		regRes.type = MachineOperand.OP_INC;
		return regRes;
	}
	
}
