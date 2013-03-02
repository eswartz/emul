/*
  LLOperand.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;
import v9t9.tools.asm.assembler.operand.hl.IOperandVisitor;

/**
 * This is an operation that has been reduced from an HLOperand
 * and is ready to be assembled.
 * @author Ed
 *
 */
public abstract class LLOperand implements AssemblerOperand {

	private AssemblerOperand original;

	public LLOperand(AssemblerOperand original) {
		this.original = original;
	}
	
	/**
	 * Get the original high-level operand (usually only supplied if it references
	 * a symbol)
	 * @return AssemblerOperand
	 */
	public AssemblerOperand getOriginal() {
		return original;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((original == null) ? 0 : original.hashCode());
		return result;
	}

	public abstract boolean equals(Object obj);

	/**
	 * Get the size of the operand in terms of immediates.
	 * @return size in bytes
	 */
	public abstract int getSize();

	/**
	 * Tell if an immediate is used.
	 */
	public abstract boolean hasImmediate();
	
	/**
	 * Get the immediate if any.
	 */
	public abstract int getImmediate();
    
    /** 
     * Resolve yourself to a lower-level operand. 
     * @param opFactory TODO
     * @throws ResolveException if the operand cannot be resolved.
     */
	public abstract IMachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException;
	
	public LLOperand resolve(IAssembler assembler, IInstruction inst) throws ResolveException {
		if (original != null)
			return original.resolve(assembler, inst);
		return this;
	}

	public void setOriginal(ConstPoolRefOperand op) {
		this.original = op;
	}

	/** Tell if the operand will never change */
	public boolean isConstant() {
		return false;
	}

    
	/*
	public static LLOperand createSymbolImmediate(Symbol symbol) {
		MachineOperand op = new MachineOperand(MachineOperand.OP_IMMED);
		if (symbol.isDefined()) {
			op.immed = (short) (op.val = symbol.getAddr());
			op.symbolResolved = true;
		}
		op.symbol = symbol;
		return op;
	}*/



	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		assert false : "not implemented";
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		assert false : "not implemented";
		return new AssemblerOperand[0];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		assert false : "not implemented";
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#accept(v9t9.tools.asm.assembler.operand.hl.IOperandVisitor)
	 */
	@Override
	public void accept(IOperandVisitor visitor) {
		if (visitor.enterOperand(this)) {
			for (AssemblerOperand kid : getChildren())
				kid.accept(visitor);
			visitor.exitOperand(this);
		}
	}
}
