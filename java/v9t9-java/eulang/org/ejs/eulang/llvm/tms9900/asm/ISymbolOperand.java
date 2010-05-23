/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public interface ISymbolOperand {
	/** Get the symbol referenced */
	ISymbol getSymbol();
	
	/** Get the local, if there is one */
	ILocal getLocal();
}
