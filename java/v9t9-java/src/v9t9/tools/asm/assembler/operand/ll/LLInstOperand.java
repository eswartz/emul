/**
 * 
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.engine.cpu.*;
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
