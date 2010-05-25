/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * Represents zero-initialized memory of the given type
 * @author ejs
 *
 */
public class ZeroInitOperand extends BaseHLOperand {

	/**
	 * @param type
	 */
	public ZeroInitOperand(LLType type) {
		super(type);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[0];
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand#getLocal()
	 */
	@Override
	public ILocal getLocal() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand#getSymbol()
	 */
	@Override
	public ISymbol getSymbol() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return true;
	}
}
