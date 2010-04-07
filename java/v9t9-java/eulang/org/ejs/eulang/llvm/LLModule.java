/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDeclareDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.directives.LLTargetDataTypeDirective;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLCodeType;

/**
 * @author ejs
 *
 */
public class LLModule {

	List<LLBaseDirective> directives;
	private final IScope globalScope;
	
	/**
	 * 
	 */
	public LLModule(IScope globalScope) {
		this.globalScope = globalScope;
		directives = new ArrayList<LLBaseDirective>();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (LLBaseDirective d : directives) {
			sb.append(d);
			sb.append('\n');
		}
		return sb.toString();
	}

	/**
	 * @param llTargetDataTypeDirective
	 */
	public void add(LLBaseDirective directive) {
		directives.add(directive);
	}

	/**
	 * @param name
	 * @param codeType
	 * @return
	 */
	public ISymbol addExtern(String name, LLCodeType codeType,
			LLLinkage linkage, LLVisibility visibility,
			String cconv, LLAttrType retType,
			LLAttrType argTypes[], LLFuncAttrs funcAttrs, String gc) {
		ISymbol symbol = globalScope.add(new AstName(name));
		symbol.setType(codeType);
		
		add(new LLDeclareDirective(symbol, linkage, visibility, cconv, retType, argTypes, funcAttrs, gc));
		return symbol;
	};
	
}
