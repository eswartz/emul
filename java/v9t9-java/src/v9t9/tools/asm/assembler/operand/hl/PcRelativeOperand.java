/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * Operand indicating the base of a PC-relative jump ("$").
 * Its resolved value is the PC.
 * @author ejs
 *
 */
public class PcRelativeOperand extends BaseOperand {

	public PcRelativeOperand() {
	}
	
	@Override
	public String toString() {
		return "$";
	}
	@Override
	public int hashCode() {
		//final int prime = 31;
		int result = 1;
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
		//PcRelativeOperand other = (PcRelativeOperand) obj;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.AssemblerOperand#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.Instruction)
	 */
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		int pc = assembler.getPc();
		if (inst != null)
			pc = inst.getPc();
		LLImmedOperand op = new LLImmedOperand(pc);
		return op;
	}

}
