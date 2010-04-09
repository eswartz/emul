/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.LLVisibility;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLGlobalDirective extends LLBaseDirective {

	private final ISymbol symbol;
	private final LLVisibility visibility;
	private final LLLinkage linkage;
	private final LLType type;

	/**
	 * @param symbol
	 * @param default1
	 * @param internal
	 * @param type
	 */
	public LLGlobalDirective(ISymbol symbol, LLVisibility visibility,
			LLLinkage linkage, LLType type) {
		this.symbol = symbol;
		this.visibility = visibility;
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
		if (visibility != null)
			sb.append(visibility.getVisibility()).append(' ');	
		sb.append(type).append(' ');
		
		// TODO: actual value or code
		sb.append("zeroinitializer");
		return sb.toString();
	}

}
