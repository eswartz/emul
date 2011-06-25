/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;

public class LLVariableStorage {
	Map<ISymbol, ILLVariable> symbolMap = new IdentityHashMap<ISymbol, ILLVariable>();
	
	public LLVariableStorage() {
	}
	
	public ILLVariable lookupVariable(ISymbol symbol) {
		return symbolMap.get(symbol);
	}
	
	public void registerVariable(ISymbol symbol, ILLVariable variable) {
		if (symbolMap.containsKey(symbol))
			assert false;
		symbolMap.put(symbol, variable);
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