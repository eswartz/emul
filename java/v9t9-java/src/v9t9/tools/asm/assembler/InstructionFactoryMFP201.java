/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.Inst9900;
import v9t9.engine.cpu.InstEncodePattern;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.IRegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

/**
 * @author Ed
 *
 */
public class InstructionFactoryMFP201 implements IInstructionFactory {

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
		rawInst.setName(InstTable9900.getInstName(inst.getInst()));
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

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#encodeInstruction(v9t9.engine.cpu.RawInstruction)
	 */
	@Override
	public byte[] encodeInstruction(RawInstruction instruction) {
		short[] words = InstTable9900.encode(instruction);
		byte[] bytes = new byte[words.length * 2];
		for (int idx = 0; idx < words.length; idx++) {
			bytes[idx*2] = (byte) (words[idx] >> 8);
			bytes[idx*2+1] = (byte) (words[idx] & 0xff);
		}
		return bytes;
	}
	
	public boolean supportsOp(int inst, int i, AssemblerOperand op) {
		InstEncodePattern pattern = InstTable9900.lookupEncodePattern(inst);
		if (pattern == null)
			return true;

		int opType = i == 1 ? pattern.op1 : pattern.op2;
		
		switch (opType) {
		case InstEncodePattern.CNT:
			if (op.isRegister()) {
				if ((op instanceof IRegisterOperand)) {
					return ((IRegisterOperand) op).isReg(0);
				}
			}
			// fall through
		case InstEncodePattern.IMM:
		case InstEncodePattern.OFF:
			return op instanceof NumberOperand || op.isConst();
		case InstEncodePattern.REG:
			return op.isRegister();
		case InstEncodePattern.GEN:
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

	public boolean isByteOp(int inst) {
		return inst == Inst9900.Isocb || inst == Inst9900.Icb || inst == Inst9900.Iab 
		|| inst == Inst9900.Isb || inst == Inst9900.Iszcb || inst == Inst9900.Imovb;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#isJumpInst(int)
	 */
	@Override
	public boolean isJumpInst(int inst) {
		return inst >= Inst9900.Ijmp && inst <= Inst9900.Ijop;
	}
}
