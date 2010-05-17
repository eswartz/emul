/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLGlobalDirective extends LLBaseDirective {

	private final ISymbol symbol;
	private final LLLinkage linkage;
	private final LLType type;

	/**
	 * @param symbol
	 * @param type
	 * @param default1
	 * @param internal
	 */
	public LLGlobalDirective(ISymbol symbol, LLLinkage linkage,
			LLType type) {
		this.symbol = symbol;
		this.linkage = linkage;
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.LLBaseDirective#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		String symName = symbol.getLLVMName();
		sb.append(symName).append(" = ");
		if (linkage != null)
			sb.append(linkage.getLinkageName()).append(' ');	
		sb.append("global ");
		sb.append(type.getLLVMName()).append(' ');
		
		// TODO: actual value or code
		sb.append("zeroinitializer");
		return sb.toString();
	}

}
