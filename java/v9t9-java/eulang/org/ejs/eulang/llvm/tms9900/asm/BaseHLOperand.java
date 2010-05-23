/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.llvm.tms9900.RegisterLocal;
import org.ejs.eulang.llvm.tms9900.StackLocal;
import org.ejs.eulang.types.LLType;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
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
	
	@Override
	public ISymbolOperand setLocal(ILocal local) {
		if (local instanceof RegisterLocal)
			return new RegTempOperand(getType(), (RegisterLocal) local);
		else if (local instanceof StackLocal)
			return new StackLocalOperand(getType(), (StackLocal) local);
		else {
			assert false;
			return this;
		}
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		return this;
	}
	
}
