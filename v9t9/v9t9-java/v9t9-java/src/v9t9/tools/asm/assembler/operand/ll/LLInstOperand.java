/**
 * 
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.engine.cpu.InstructionMFP201;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperandMFP201Inst;
import v9t9.tools.asm.assembler.InstructionFactoryMFP201;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public class LLInstOperand extends LLOperand {

	private final LLInstruction inst;

	/**
	 * @param original
	 */
	public LLInstOperand(AssemblerOperand original, LLInstruction inst) {
		super(original);
		this.inst = inst;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return inst.toString();
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inst == null) ? 0 : inst.hashCode());
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
		LLInstOperand other = (LLInstOperand) obj;
		if (inst == null) {
			if (other.inst != null)
				return false;
		} else if (!inst.equals(other.inst))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.LLOperand#createMachineOperand(v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory)
	 */
	@Override
	public MachineOperand createMachineOperand(IMachineOperandFactory opFactory)
			throws ResolveException {
		return new MachineOperandMFP201Inst(
				(InstructionMFP201) InstructionFactoryMFP201.INSTANCE.createRawInstruction(inst));
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.LLOperand#getImmediate()
	 */
	@Override
	public int getImmediate() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.LLOperand#getSize()
	 */
	@Override
	public int getSize() {
		try {
			return InstructionFactoryMFP201.INSTANCE.createRawInstruction(inst).getSize();
		} catch (ResolveException e) {
			return 1;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.LLOperand#hasImmediate()
	 */
	@Override
	public boolean hasImmediate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
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

}
