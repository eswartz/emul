/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.InstPatternMFP201;
import v9t9.engine.cpu.InstTableMFP201;
import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.IRegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

/**
 * @author Ed
 *
 */
public class InstructionFactoryMFP201 implements IInstructionFactory {

	public static final IInstructionFactory INSTANCE = new InstructionFactoryMFP201();
	MachineOperandFactoryMFP201 opFactory = new MachineOperandFactoryMFP201();
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#createRawInstruction(v9t9.tools.asm.assembler.LLInstruction)
	 */
	@Override
	public RawInstruction createRawInstruction(LLInstruction inst)
			throws ResolveException {
		RawInstruction rawInst = new RawInstruction();
		rawInst.pc = inst.pc;
		rawInst.setInst(inst.getInst());
		rawInst.setName(InstTableMFP201.getInstName(inst.getInst()));
		rawInst.setOp1(inst.getOp1() != null ? 
				inst.getOp1().createMachineOperand(opFactory) :
					MachineOperandMFP201.createEmptyOperand());
		rawInst.setOp2(inst.getOp2() != null ? 
				inst.getOp2().createMachineOperand(opFactory) :
					MachineOperandMFP201.createEmptyOperand());
		rawInst.setOp3(inst.getOp3() != null ? 
				inst.getOp3().createMachineOperand(opFactory) :
					MachineOperandMFP201.createEmptyOperand());
		InstTableMFP201.coerceOperandTypes(rawInst);
		InstTableMFP201.calculateInstructionSize(rawInst);
		return rawInst;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#encodeInstruction(v9t9.engine.cpu.RawInstruction)
	 */
	@Override
	public byte[] encodeInstruction(RawInstruction instruction) {
		return InstTableMFP201.encode(instruction);
	}
	
	public boolean supportsOp(int inst, int i, AssemblerOperand op) {
		InstPatternMFP201 pattern = InstTableMFP201.lookupEncodePattern(inst);
		if (pattern == null)
			return true;

		int opType = i == 1 ? pattern.op1 : pattern.op2;
		
		switch (opType) {
		case InstPatternMFP201.CNT:
			if (op.isRegister()) {
				if ((op instanceof IRegisterOperand)) {
					return ((IRegisterOperand) op).isReg(0);
				}
			}
			// fall through
		case InstPatternMFP201.IMM:
		case InstPatternMFP201.OFF:
			return op instanceof NumberOperand || op.isConst();
		case InstPatternMFP201.REG:
			return op.isRegister();
		case InstPatternMFP201.GEN:
			if (!(op.isRegister() || op.isMemory()))
				return false;
			
			if (op instanceof IRegisterOperand) {
				AssemblerOperand reg = ((IRegisterOperand) op).getReg();
				return reg.isRegister() || reg instanceof NumberOperand;
			}
			return true;
		}
		return false;
	}

	public boolean isByteInst(int inst) {
		return InstTableMFP201.isByteInst(inst);
	}
	
	@Override
	public boolean isJumpInst(int inst) {
		return InstTableMFP201.isJumpInst(inst);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#getInstName(int)
	 */
	@Override
	public String getInstName(int inst) {
		return InstTableMFP201.getInstName(inst);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#getInstSize(v9t9.tools.asm.assembler.LLInstruction)
	 */
	@Override
	public int getInstSize(LLInstruction ins) {
		try {
			byte[] bytes = ins.getBytes(INSTANCE);
			return bytes.length;
		} catch (ResolveException e) {
			return 1;
		}
	}
}
