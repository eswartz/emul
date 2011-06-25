/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.symbols.ISymbol;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author ejs
 *
 */
public interface ISymbolOperand extends AssemblerOperand {
	/** Get the symbol referenced */
	ISymbol getSymbol();
	
	/** Get the local, if there is one */
	ILocal getLocal();
	
	/** Return the operand modified with the given local */
	ISymbolOperand setLocal(ILocal local);
}
