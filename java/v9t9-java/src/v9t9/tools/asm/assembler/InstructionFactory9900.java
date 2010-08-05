/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.InstPattern9900;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.InstTableCommon;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.IRegisterOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLPCRelativeOperand;

/**
 * @author Ed
 *
 */
public class InstructionFactory9900 implements IInstructionFactory {

	final static public InstructionFactory9900 INSTANCE = new InstructionFactory9900();
	
	MachineOperandFactory9900 opFactory = new MachineOperandFactory9900();
	
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
		InstTable9900.coerceOperandTypes(rawInst);
		InstTable9900.calculateInstructionSize(rawInst);
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
		InstPattern9900 pattern = InstTable9900.lookupEncodePattern(inst);
		if (pattern == null)
			return true;

		int opType = i == 1 ? pattern.op1 : pattern.op2;
		
		switch (opType) {
		case InstPattern9900.CNT:
			if (op.isRegister()) {
				if ((op instanceof IRegisterOperand)) {
					return ((IRegisterOperand) op).isReg(0);
				}
			}
			// fall through
		case InstPattern9900.IMM:
		case InstPattern9900.OFF:
			return op instanceof NumberOperand || op.isConst();
		case InstPattern9900.REG:
			return op.isRegister();
		case InstPattern9900.GEN:
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
		return InstTable9900.isByteInst(inst);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#isJumpInst(int)
	 */
	@Override
	public boolean isJumpInst(int inst) {
		return InstTable9900.isJumpInst(inst);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#getInstName(int)
	 */
	@Override
	public String getInstName(int inst) {
		return InstTable9900.getInstName(inst);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#getInstSize(v9t9.tools.asm.assembler.LLInstruction)
	 */
	@Override
	public int getInstSize(LLInstruction ins) {
		return calculateInstructionSize(ins.getInst(), ins.getOp1(), ins.getOp2());
	}
	
	private int calculateInstructionSize(int inst, LLOperand op1, LLOperand op2) {
		int size = 0;
    	if (inst == InstTableCommon.Idata) {
    		size = 2;
    		return size;
    	} else if (inst == InstTableCommon.Ibyte) {
    		size = 1;
    		return size;
    	}
    	size = 2;
    	InstPattern9900 pattern = InstTable9900.lookupEncodePattern(inst);
		if (pattern == null)
			return size;
		
		if (op1 != null)
			size += coerceSize(pattern.op1, op1);
		if (op2 != null)
			size += coerceSize(pattern.op2, op2);
		return size;
	}

	private int coerceSize(int type, LLOperand op) {
		int size = op.getSize();
		if (size > 0) {
			if (type == InstPattern9900.CNT || type == InstPattern9900.OFF)
				size = 0;
		}
		if (type == InstPattern9900.GEN && op instanceof LLPCRelativeOperand) {
			size = 2;
		}
		return size;
	}
}
