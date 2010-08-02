/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.engine.cpu.RawInstruction;

/**
 * @author Ed
 *
 */
public class InstructionFactory9900 implements IInstructionFactory {

	MachineOperandFactory9900 opFactory = new MachineOperandFactory9900();
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#createRawInstruction(v9t9.tools.asm.assembler.LLInstruction)
	 */
	@Override
	public RawInstruction createRawInstruction(LLInstruction inst)
			throws ResolveException {
		RawInstruction rawInst = new RawInstruction();
		rawInst.pc = inst.pc;
		rawInst.inst = inst.getInst();
		rawInst.setOp1(inst.getOp1() != null ? 
				inst.getOp1().createMachineOperand(opFactory) :
					MachineOperand9900.createEmptyOperand());
		rawInst.setOp2(inst.getOp2() != null ? 
				inst.getOp2().createMachineOperand(opFactory) :
					MachineOperand9900.createEmptyOperand());
		InstTable9900.calculateInstructionSize(rawInst);
		InstTable9900.coerceOperandTypes(rawInst);
		return rawInst;
	}

}
