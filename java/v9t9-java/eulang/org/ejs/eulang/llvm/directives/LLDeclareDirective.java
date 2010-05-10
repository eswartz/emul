/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.llvm.FunctionConvention;
import org.ejs.eulang.llvm.LLArgAttrType;
import org.ejs.eulang.llvm.LLAttrType;
import org.ejs.eulang.llvm.LLFuncAttrs;
import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.LLVisibility;
import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public class LLDeclareDirective extends LLBaseDirective  {

	private final ISymbol symbol;
	private final LLLinkage linkage;
	private final LLVisibility visibility;
	private final String cconv;
	private final LLAttrType retType;
	private final LLArgAttrType[] argTypes;
	private final LLFuncAttrs funcAttrs;
	private final String gc;

	public LLDeclareDirective(
			ISymbol symbol, LLLinkage linkage, LLVisibility visibility, String cconv, LLAttrType retType,
			LLArgAttrType argTypes[], LLFuncAttrs funcAttrs, String gc) {
		this.symbol = symbol;
		this.linkage = linkage;
		this.visibility = visibility;
		this.cconv = cconv;
		this.retType = retType;
		this.argTypes = argTypes;
		this.funcAttrs = funcAttrs;
		this.gc = gc;
	}

			
	public FunctionConvention getConvention() {
		return new FunctionConvention(linkage, visibility, cconv, retType, argTypes, funcAttrs, gc);
	}
			
			
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.LLBaseDirective#toString()
	 */
	@Override
	public String toString() {
		return "declare " 
		+ (linkage != null ? linkage.getLinkageName() + " " : "")
		+ (visibility != null ? visibility.getVisibility() + " " : "")
		+ (cconv != null ? cconv + " " : "")
		+ retType.toString() + " "
		+ symbol.getLLVMName()
		+ "(" + argTypeString() + ") "
		+ (funcAttrs != null ? funcAttrs + " " : "")
		+ (gc != null ? "gc \"" + gc + "\" " : "");
		
	
	}

	/**
	 * @return
	 */
	private String argTypeString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (LLAttrType argType : argTypes) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(argType);
		}
		return sb.toString();
	}

	
}
