/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.types.LLType;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public abstract class BaseHLOperand implements AsmOperand, ISymbolOperand {

	private LLType llType;

	public BaseHLOperand(LLType type) {
		this.llType = type;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#resolve(v9t9.tools.asm.assembler.Assembler, v9t9.engine.cpu.IInstruction)
	 */
	@Override
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		throw new ResolveException(inst, null, "Should not have this operand in assembler code!");
	}

	public LLType getType() {
		return llType;
	}
	
}
