/**
 * 
 */
package v9t9.tools.asm.assembler;

import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory;
import v9t9.tools.asm.assembler.operand.ll.LLAddrOperand;
import v9t9.tools.asm.assembler.operand.ll.LLCountOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOffsetOperand;
import v9t9.tools.asm.assembler.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegDecOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;
import v9t9.tools.asm.assembler.operand.ll.LLScaledRegOffsOperand;

/**
 * @author Ed
 *
 */
public class MachineOperandFactoryF99 implements IMachineOperandFactory {

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createAddressOperand(v9t9.tools.asm.assembler.operand.ll.LLAddrOperand)
	 */
	@Override
	public MachineOperand createAddressOperand(LLAddrOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(
				MachineOperand9900.OP_ADDR, (short) 0, 
				(short) ((LLAddrOperand)op).getAddress());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createCountOperand(v9t9.tools.asm.assembler.operand.ll.LLCountOperand)
	 */
	@Override
	public MachineOperand createCountOperand(LLCountOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_CNT, 
				(short) op.getCount());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createEmptyOperand()
	 */
	@Override
	public MachineOperand createEmptyOperand() {
		return MachineOperand9900.createEmptyOperand();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createRegisterOperand(v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand)
	 */
	@Override
	public MachineOperand createRegisterOperand(LLRegisterOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_REG, 
				(short) op.getRegister());
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createRegIndOperand(v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand)
	 */
	@Override
	public MachineOperand createRegIndOperand(LLRegIndOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_IND, 
					(short) op.getRegister());

	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createRegIndOperand(v9t9.tools.asm.assembler.operand.ll.LLRegIndOperand)
	 */
	@Override
	public MachineOperand createRegOffsOperand(LLRegOffsOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_ADDR, 
				(short) op.getRegister(), (short) op.getOffset());

	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createRegIncOperand(v9t9.tools.asm.assembler.operand.ll.LLRegIncOperand)
	 */
	@Override
	public MachineOperand createRegIncOperand(LLRegIncOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_INC, 
				(short) op.getRegister());
	}
	
	@Override
	public MachineOperand createRegDecOperand(LLRegDecOperand op)
			throws ResolveException {
		throw new ResolveException(op, "register decrement not supported");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createOffsetOperand(v9t9.tools.asm.assembler.operand.ll.LLOffsetOperand)
	 */
	@Override
	public MachineOperand createOffsetOperand(LLOffsetOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_OFFS_R12, 
				(short) op.getOffset());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createJumpOperand(v9t9.tools.asm.assembler.operand.ll.LLJumpOperand)
	 */
	@Override
	public MachineOperand createPCRelativeOperand(LLPCRelativeOperand op)
			throws ResolveException {
		//MachineOperand9900 mop = MachineOperand9900.createGeneralOperand(
		//		MachineOperand9900.OP_ADDR, MachineOperand9900.PCREL, (short)op.getOffset());
		MachineOperand9900 mop = new MachineOperand9900(MachineOperand9900.OP_JUMP);
		mop.val = (short) op.getOffset();
		return mop;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createImmedOperand(v9t9.tools.asm.assembler.operand.ll.LLImmedOperand)
	 */
	@Override
	public MachineOperand createImmedOperand(LLImmedOperand op)
			throws ResolveException {
		return MachineOperand9900.createImmediate(op.getValue());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.ll.IMachineOperandFactory#createScaledRegOffsOperand(v9t9.tools.asm.assembler.operand.ll.LLScaledRegOffsOperand)
	 */
	@Override
	public MachineOperand createScaledRegOffsOperand(LLScaledRegOffsOperand op)
			throws ResolveException {
		throw new ResolveException(op, "scaled register offset not supported");
	}
}
