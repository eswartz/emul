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
 * An operand to be used as a jump target
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
		if (op instanceof NumberOperand)
			return "$+" + op.toString();
		else
			return op.toString();
	}
	
	public MachineOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		int pc = assembler.getPc();
		MachineOperand opRes = op.resolve(assembler, inst);
		if (opRes.type != MachineOperand.OP_IMMED)
			throw new ResolveException(op, "Expected a number");
		opRes.type = MachineOperand.OP_JUMP;
		if (opRes.symbol == null)
			opRes.val = opRes.immed - pc; 
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
