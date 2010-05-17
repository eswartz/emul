/**
 * 
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;

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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLOperand other = (LLOperand) obj;
		if (original == null) {
			if (other.original != null)
				return false;
		} else if (!original.equals(other.original))
			return false;
		return true;
	}

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
     * @throws ResolveException if the operand cannot be resolved.
     */
	public abstract MachineOperand createMachineOperand() throws ResolveException;
	
	public LLOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException {
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
	
}
