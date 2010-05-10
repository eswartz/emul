/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.symbols.ISymbol;

/**
 * Package of the function attributes, argument attributes, calling convention,
 * etc.
 * 
 * @author ejs
 * 
 */
public class FunctionConvention {

	private final LLLinkage linkage;
	private final LLVisibility visibility;
	private final String cconv;
	private final LLAttrType retType;
	private final LLArgAttrType[] argTypes;
	private final LLFuncAttrs funcAttrs;
	private final String gc;
	
	public FunctionConvention(LLLinkage linkage, LLVisibility visibility,
			String cconv, LLAttrType retType, LLArgAttrType argTypes[],
			LLFuncAttrs funcAttrs, String gc) {
		this.linkage = linkage;
		this.visibility = visibility;
		this.cconv = cconv;
		this.retType = retType;
		this.argTypes = argTypes;
		this.funcAttrs = funcAttrs;
		this.gc = gc;
	}

	public LLLinkage getLinkage() {
		return linkage;
	}

	public LLVisibility getVisibility() {
		return visibility;
	}

	public String getCconv() {
		return cconv;
	}

	public LLAttrType getRetType() {
		return retType;
	}

	public LLArgAttrType[] getArgTypes() {
		return argTypes;
	}

	public LLFuncAttrs getFuncAttrs() {
		return funcAttrs;
	}

	public String getGc() {
		return gc;
	}
	
}
