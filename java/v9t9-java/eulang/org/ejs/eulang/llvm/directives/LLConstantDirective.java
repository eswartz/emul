/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public class LLConstantDirective extends LLBaseDirective {
	private final LLOperand constant;
	private final boolean isConst;
	private final ISymbol symbol;
	private int addrSpace;
	
	public LLConstantDirective(ISymbol symbol, boolean isConst, LLOperand constant) {
		this.symbol = symbol;
		this.isConst = isConst;
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
		 (isConst ? "constant " : "") + constant.getType().getLLVMName() + " " + constant;
		 //type.getLLVMName() + " " + (constant != null ? constant : "zeroinitializer");  
	}
}
