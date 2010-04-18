/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.IAstSymbolDefiner;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDeclareDirective;
import org.ejs.eulang.llvm.directives.LLTypeDirective;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.LocalSymbol;
import org.ejs.eulang.symbols.ModuleScope;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLModule {

	List<LLBaseDirective> directives;
	List<LLBaseDirective> externDirectives;
	private final IScope globalScope;
	
	private IScope moduleScope;
	
	/**
	 * 
	 */
	public LLModule(IScope globalScope) {
		this.globalScope = globalScope;
		directives = new ArrayList<LLBaseDirective>();
		externDirectives = new ArrayList<LLBaseDirective>();
		
		moduleScope = new ModuleScope(globalScope);
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
	 * Find or create the symbol that uniquely references this expression
	 * @param astSymbol the name of the symbol in the AST 
	 * @param expr the concrete body for the symbol
	 * @return unique symbol
	 */
	public ISymbol getModuleSymbol(ISymbol astSymbol, IAstTypedExpr expr) {
		StringBuilder sb = new StringBuilder();
		
		// put a unique scope
		getScopePrefix(sb, astSymbol.getScope());
		
		// and the name
		sb.append(astSymbol.getName());
		
		// and the exact type
		sb.append('.').append(expr.getType().getSymbolicName());
		
		String symName = sb.toString();
		ISymbol modSymbol = moduleScope.get(symName);
		if (modSymbol == null) {
			modSymbol = moduleScope.add(symName);
			modSymbol.setType(expr.getType());
		}
		return modSymbol;
	}
	
	/**
	 * @param sb 
	 * @param scope
	 * @return
	 */
	private void getScopePrefix(StringBuilder sb, IScope scope) {
		if (scope.getOwner() != null && scope.getOwner() instanceof IAstSymbolDefiner) {
			ISymbol scopeSymbol = ((IAstSymbolDefiner) scope.getOwner()).getSymbol();
			sb.append(scopeSymbol.getName()).append('.');
		}
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
	}

	/**
	 * @return
	 */
	public int getSymbolCount() {
		return moduleScope.getSymbols().length;
	};
	
}
