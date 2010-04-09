/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDeclareDirective;
import org.ejs.eulang.llvm.directives.LLTypeDirective;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalSymbol;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLType.BasicType;

/**
 * @author ejs
 *
 */
public class LLModule {

	List<LLBaseDirective> directives;
	List<LLBaseDirective> externDirectives;
	private final IScope globalScope;
	
	/**
	 * 
	 */
	public LLModule(IScope globalScope) {
		this.globalScope = globalScope;
		directives = new ArrayList<LLBaseDirective>();
		externDirectives = new ArrayList<LLBaseDirective>();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (LLBaseDirective d : externDirectives) {
			sb.append(d);
			sb.append('\n');
		}
		sb.append('\n');
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
	public void addModuleDirective(LLBaseDirective directive) {
		externDirectives.add(directive);
	}

	public ISymbol addExtern(String name, LLCodeType codeType,
			LLLinkage linkage, LLVisibility visibility,
			String cconv, LLAttrType retType,
			LLAttrType argTypes[], LLFuncAttrs funcAttrs, String gc) {
		ISymbol symbol = globalScope.get(name);
		if (symbol == null) {
			symbol = globalScope.add(new AstName(name));
			symbol.setType(codeType);
			
			externDirectives.add(new LLDeclareDirective(symbol, linkage, visibility, cconv, retType, argTypes, funcAttrs, gc));
		}
		return symbol;
	}

	/**
	 * @param rEFPTR
	 * @return
	 */
	public ISymbol addExternType(LLType type) {
		if (type.getName() == null || type.getBasicType() == BasicType.VOID) return null;
		ISymbol typeSymbol = globalScope.get(type.getName());
		if (typeSymbol == null) {
			typeSymbol = globalScope.add(new LocalSymbol(globalScope.nextId(), new AstName(type.getName()), null));
			externDirectives.add(new LLTypeDirective(typeSymbol, type));
		}
		return typeSymbol;
	};
	
}
