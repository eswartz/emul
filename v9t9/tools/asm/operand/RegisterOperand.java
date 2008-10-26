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
public class RegisterOperand implements AssemblerOperand {
	protected final AssemblerOperand reg;

	public RegisterOperand(AssemblerOperand reg) {
		this.reg = reg;
	}
	
	@Override
	public String toString() {
		return "R" + reg.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see v9t9.engine.cpu.AssemblerOperand#resolve(v9t9.tools.asm.Assembler,
	 * v9t9.engine.cpu.Instruction)
	 */
	public MachineOperand resolve(Assembler assembler, Instruction inst) throws ResolveException {
		MachineOperand op = reg.resolve(assembler, inst);
		if (op.type == MachineOperand.OP_IMMED) {
			return MachineOperand.createGeneralOperand(
					MachineOperand.OP_REG, 
					(short)((MachineOperand)op).immed);
		}
		throw new ResolveException(inst, op);
	}

}
