/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;

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

	public static FunctionConvention create(TypeEngine typeEngine, String cconv, LLCodeType codeType) {
		LLType[] argTypes = codeType.getArgTypes();
		LLArgAttrType[] argAttrs = new LLArgAttrType[argTypes.length];
		for (int i = 0; i < argAttrs.length; i++)
			argAttrs[i] = new LLArgAttrType("arg"+i, null, typeEngine.getRealType(argTypes[i]));
		return new FunctionConvention(null, LLVisibility.DEFAULT, cconv,
			new LLAttrType(null, typeEngine.getRealType(codeType.getRetType())),
			argAttrs,
			new LLFuncAttrs(), 
			null /*gc*/);
	}
			
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
	
	public LLCodeType getActualType(TypeEngine typeEngine) {
		LLType ret = retType.getType();
		LLType[] args = new LLType[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			args[i] = argTypes[i].getType();
		}
		return typeEngine.getCodeType(ret, args);
	}
	
}
