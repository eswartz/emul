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
public class PcRelativeOperand implements AssemblerOperand {

	public PcRelativeOperand() {
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.AssemblerOperand#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.Instruction)
	 */
	public MachineOperand resolve(Assembler assembler, Instruction inst)
			throws ResolveException {
		return MachineOperand.createImmediate(inst.pc + inst.size);
	}

}
