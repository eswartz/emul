/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;

public class LLVariableStorage {
	Map<ISymbol, ILLVariable> symbolMap = new HashMap<ISymbol, ILLVariable>();
	private final TypeEngine typeEngine;
	
	public LLVariableStorage(TypeEngine typeEngine) {
		this.typeEngine = typeEngine;
		
	}
	
	public ILLVariable lookupVariable(ISymbol symbol) {
		return symbolMap.get(symbol);
	}
	
	public void registerVariable(ISymbol symbol, ILLVariable variable) {
		assert !symbolMap.containsKey(symbol);
		symbolMap.put(symbol, variable);
		/*
		// the variable lives in memory, not in value
		LLType addrType = typeEngine.getPointerType(symbol.getType());
		if (isVar)
			addrType = typeEngine.getPointerType(addrType);
		temp = localScope.addTemporary(symbol.getName() + (isVar ? "$va" : "$a"), false);
		temp.setType(addrType);
		localMap.put(symbol, temp);
		
		return temp;
		*/
	}

	/**
	 * @param scope
	 * @return
	 */
	public Collection<ILLVariable> getVariablesForScope(IScope scope) {
		List<ILLVariable> vars = new ArrayList<ILLVariable>();
		for (ILLVariable var : symbolMap.values()) {
			if (scope.encloses(var.getSymbol().getScope()))
				vars.add(var);
		}
		return vars;
	}
}