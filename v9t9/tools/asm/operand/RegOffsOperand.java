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
 * @author ejs
 *
 */
public class RegOffsOperand extends RegisterOperand {

	private final AssemblerOperand addr;

	public RegOffsOperand(AssemblerOperand addr, AssemblerOperand reg) {
		super(reg);
		this.addr = addr;
	}

	@Override
	public String toString() {
		return "@" + addr + "(" + super.toString() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
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
		RegOffsOperand other = (RegOffsOperand) obj;
		if (addr == null) {
			if (other.addr != null) {
				return false;
			}
		} else if (!addr.equals(other.addr)) {
			return false;
		}
		if (reg == null) {
			if (other.reg != null) {
				return false;
			}
		} else if (!reg.equals(other.reg)) {
			return false;
		}
		return true;
	}
	
	@Override
	public MachineOperand resolve(Assembler assembler, Instruction inst)
			throws ResolveException {
		MachineOperand regRes = super.resolve(assembler, inst);
		MachineOperand addrRes = addr.resolve(assembler, inst);
		if (addrRes.type != MachineOperand.OP_IMMED)
			throw new ResolveException(inst, addrRes, "Expected an immediate");
		regRes.type = MachineOperand.OP_ADDR;
		regRes.immed = addrRes.immed;
		return regRes;
	}
	
}
