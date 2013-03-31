/*
  AssemblerInstruction.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import v9t9.common.asm.ICpuInstruction;
import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.directive.LabelDirective;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public abstract class AssemblerInstruction extends BaseAssemblerInstruction implements ICpuInstruction {

	private static final AssemblerOperand[] NO_OPS = new AssemblerOperand[0];
	private int inst;
	private AssemblerOperand op1;
	private AssemblerOperand op2;
	private AssemblerOperand op3;
	private final IInstructionFactory factory;
	
	
	/**
	 * 
	 */
	public AssemblerInstruction(IInstructionFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * @return
	 */
	public IInstructionFactory getInstructionFactory() {
		return factory;
	}
	public IInstruction[] resolve(IAssembler assembler, IInstruction previous, boolean finalPass)
	throws ResolveException {
		int pc = assembler.getPc();
		// instructions and associated labels are bumped when following uneven data
		if (assembler.getBasicAlignment() > 1 && (pc & 1) != 0 && getInst() != InstTableCommon.Ibyte) {
			pc = (pc + 1) & 0xfffe;
			assembler.setPc(pc);
		
			if (previous instanceof LabelDirective) {
				((LabelDirective) previous).setPc(assembler.getPc());
				
			}
		}
		
		setPc(pc);
		LLOperand lop1 = getOp1() != null ? getOp1().resolve(assembler, this) : null;
		LLOperand lop2 = getOp2() != null ? getOp2().resolve(assembler, this) : null;
		LLOperand lop3 = getOp3() != null ? getOp3().resolve(assembler, this) : null;
		
		LLInstruction target = new LLInstruction(assembler.getInstructionFactory());
		target.setPc(pc);
		target.setInst(getInst());
		target.setOp1(lop1);
		target.setOp2(lop2);
		target.setOp3(lop3);
		//target.completeInstruction(pc);
	
		int size = assembler.getInstructionFactory().getInstSize(target);
		assembler.setPc((short) (target.getPc() + size));
		return new LLInstruction[] { target };
	}
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(factory.getInstName(inst));
		if (op1 != null) {
			builder.append(' ');
			builder.append(op1);
			if (op2 != null) {
				builder.append(',');
				builder.append(op2);
				if(op3 != null) {
					builder.append(',');
					builder.append(op3);
				}
			}
		}
		return builder.toString();
	}
	
	public int getInst() {
		return inst;
	}

	public void setInst(int inst) {
		this.inst = inst;
	}

	public AssemblerOperand getOp1() {
		return op1;
	}

	public void setOp1(AssemblerOperand op1) {
		this.op1 = op1;
	}

	public AssemblerOperand getOp2() {
		return op2;
	}

	public void setOp2(AssemblerOperand op2) {
		this.op2 = op2;
	}

	public AssemblerOperand getOp3() {
		return op3;
	}
	
	public void setOp3(AssemblerOperand op3) {
		this.op3 = op3;
	}
	
	/** Get an operand by traditional number (1,2,3) */
	public AssemblerOperand getOp(int i) {
		if (i == 1)
			return op1;
		else if (i == 2)
			return op2;
		else if (i == 3)
			return op3;
		throw new IllegalArgumentException();
	}
	public void setOp(int i, AssemblerOperand op) {
		if (i == 1)
			op1 = op;
		else if (i == 2)
			op2 = op;
		else if (i == 3)
			op3 = op;
		else
			throw new IllegalArgumentException();
	}
	public AssemblerOperand[] getOps() {
		if (op3 != null)
			return new AssemblerOperand[] { op1, op2, op3 };
		if (op2 != null)
			return new AssemblerOperand[] { op1, op2  };
		if (op1 != null)
			return new AssemblerOperand[] { op1 };
		return NO_OPS;
	}

}
