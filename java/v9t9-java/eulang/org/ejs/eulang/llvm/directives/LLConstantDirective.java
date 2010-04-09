/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.llvm.LLConstant;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLConstantDirective extends LLBaseDirective {
	private final LLConstant constant;
	private final LLType type;
	private final boolean isConst;
	private final ISymbol symbol;
	private int addrSpace;
	
	public LLConstantDirective(ISymbol symbol, boolean isConst, LLType type, LLConstant constant) {
		this.symbol = symbol;
		this.isConst = isConst;
		this.type = type;
		this.constant = constant;
		
	}
	
	public void setAddrSpace(int addrSpace) {
		this.addrSpace = addrSpace;
		
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.LLBaseDirective#toString()
	 */
	@Override
	public String toString() {
		return symbol.getLLVMName() + " = " + (addrSpace != 0 ? "addrspace(" + addrSpace + ") " : "") +
		 (isConst ? "constant " : "") + type.getLLVMName() + " " + (constant != null ? constant : "zeroinitializer");  
	}
}
