/*
  AsmInstructionFactory9900.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.inst9900;

import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.machine.ti99.asm.InstructionFactory9900;
import v9t9.machine.ti99.cpu.InstPattern9900;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;
import v9t9.tools.asm.IAsmInstructionFactory;
import v9t9.tools.asm.LLInstruction;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.hl.IRegisterOperand;
import v9t9.tools.asm.operand.hl.NumberOperand;
import v9t9.tools.asm.operand.ll.LLOperand;
import v9t9.tools.asm.operand.ll.LLPCRelativeOperand;

/**
 * @author Ed
 *
 */
public class AsmInstructionFactory9900 extends InstructionFactory9900 implements IAsmInstructionFactory {

	final static public AsmInstructionFactory9900 INSTANCE = new AsmInstructionFactory9900();
	
	MachineOperandFactory9900 opFactory = new MachineOperandFactory9900();
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.IInstructionFactory#createRawInstruction(v9t9.tools.asm.LLInstruction)
	 */
	@Override
	public RawInstruction createRawInstruction(LLInstruction inst)
			throws ResolveException {
		RawInstruction rawInst = new RawInstruction();
		rawInst.pc = inst.getPc();
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

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.IInstructionFactory#getInstSize(v9t9.tools.asm.LLInstruction)
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
