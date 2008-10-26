/**
 * 
 */
package v9t9.tools.asm;

import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.MachineOperand;

/**
 * An oprand to be used as a jump target
 * @author ejs
 *
 */
public class JumpOperand implements AssemblerOperand {

	private final AssemblerOperand op;

	public JumpOperand(AssemblerOperand op) {
		this.op = op;
	}

	@Override
	public String toString() {
		return "$+" + op.toString();
	}
	
	public MachineOperand resolve(Assembler assembler, Instruction inst)
			throws ResolveException {
		MachineOperand opRes = op.resolve(assembler, inst);
		if (opRes.type != MachineOperand.OP_IMMED)
			throw new ResolveException(inst, op, "Expected a number");
		opRes.type = MachineOperand.OP_JUMP;
		return opRes;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
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
		JumpOperand other = (JumpOperand) obj;
		if (op == null) {
			if (other.op != null) {
				return false;
			}
		} else if (!op.equals(other.op)) {
			return false;
		}
		return true;
	}
	
}
