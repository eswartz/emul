/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.ast.IAstSymbolDefiner;
import org.ejs.eulang.ast.impl.AstName;
import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDeclareDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.directives.LLTypeDirective;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.symbols.ModuleScope;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLInstanceType;
import org.ejs.eulang.types.LLSymbolType;
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
	
	private Map<LLType, ISymbol> emittedTypes = new HashMap<LLType, ISymbol>();
	
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
	 * @return the directives
	 */
	public List<LLBaseDirective> getDirectives() {
		return directives;
	}
	/**
	 * @return the externDirectives
	 */
	public List<LLBaseDirective> getExternDirectives() {
		return externDirectives;
	}
	/**
	 * @return the emittedTypes
	 */
	public Map<LLType, ISymbol> getEmittedTypes() {
		return emittedTypes;
	}
	/**
	 * @return the globalScope
	 */
	public IScope getGlobalScope() {
		return globalScope;
	}
	/**
	 * @return the moduleScope
	 */
	public IScope getModuleScope() {
		return moduleScope;
	}

	/**
	 * Find or create the symbol that uniquely references this expression
	 * @param astSymbol the name of the symbol in the AST 
	 * @param expr the concrete body for the symbol
	 * @return unique symbol
	 */
	public ISymbol getModuleSymbol(ISymbol astSymbol, LLType type) {
		String symName = constructModuleSymName(astSymbol.getScope(), astSymbol.getName(), type);
		ISymbol modSymbol = moduleScope.get(symName);
		if (modSymbol == null) {
			modSymbol = moduleScope.add(symName, false);
			modSymbol.setType(type);
		}
		return modSymbol;
	}

	private String constructModuleSymName(IScope scope, String name, LLType type) {
		StringBuilder sb = new StringBuilder();
		
		// put a unique scope
		getScopePrefix(sb, scope);
		
		// and the name
		sb.append(name);
		
		// and the exact type
		sb.append('.').append(type.getSymbolicName());
		
		String symName = sb.toString();
		return symName;
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
			LLArgAttrType[] argTypes, LLFuncAttrs funcAttrs, String gc) {
		name = constructModuleSymName(moduleScope, name, codeType);
		
		ISymbol modSymbol = globalScope.get(name);
		if (modSymbol == null) {
			modSymbol = globalScope.add(new AstName(name));
			modSymbol.setType(codeType);
			
			externDirectives.add(new LLDeclareDirective(modSymbol, linkage, visibility, cconv, 
					retType, argTypes, funcAttrs, gc));
		}
		return modSymbol;
	}

	/**
	 * @param rEFPTR
	 * @return
	 */
	public boolean addExternType(LLType type) {
		if (type == null|| type.getName() == null || type.getBasicType() == BasicType.VOID) return false;
		if (type instanceof LLInstanceType || type instanceof LLSymbolType)
			return false;
		if (emittedTypes.containsKey(type))
			return false;
		ISymbol typeSymbol = globalScope.get(type.getName());
		if (typeSymbol == null) {
			//typeSymbol = globalScope.add(new LocalSymbol(globalScope.nextId(), new AstName(type.getName()), null));
			typeSymbol = globalScope.add(ISymbol.Visibility.LOCAL, new AstName(type.getName()));
			typeSymbol.setType(type);
			externDirectives.add(new LLTypeDirective(typeSymbol, type));
		} else if (typeSymbol.getVisibility() != ISymbol.Visibility.LOCAL) {
			typeSymbol = moduleScope.get(type.getName());
			if (typeSymbol != null) {
				if (typeSymbol.getType().isMoreComplete(type))
					return false;
				moduleScope.remove(typeSymbol);
			}
			typeSymbol = moduleScope.add(ISymbol.Visibility.LOCAL, new AstName(type.getName()));
			typeSymbol.setType(type);
			externDirectives.add(new LLTypeDirective(typeSymbol, type));
		}
		emittedTypes.put(type, typeSymbol);
		return true;
	}

	/**
	 * @return
	 */
	public int getSymbolCount() {
		return moduleScope.getSymbols().length;
	}

	/**
	 * @param type
	 */
	public boolean emitTypes(LLType type) {
		if (type == null)
			return false;
		if (emittedTypes.containsKey(type))
			return false;
		if (!addExternType(type))
			return false;
		boolean changed = false;
		if (type instanceof LLAggregateType) {
			for (LLType agg : ((LLAggregateType) type).getTypes())
				changed |= emitTypes(agg);
		}
		changed |= emitTypes(type.getSubType());
		return changed;
	};
	
	public LLBaseDirective lookup(ISymbol symbol) {
		for (LLBaseDirective dir : directives) {
			if (dir instanceof LLDeclareDirective && ((LLDeclareDirective) dir).getSymbol().equals(symbol))
				return dir;
			if (dir instanceof LLDefineDirective && ((LLDefineDirective) dir).getSymbol().equals(symbol))
				return dir;
		}
		return null;
	}
	
}
