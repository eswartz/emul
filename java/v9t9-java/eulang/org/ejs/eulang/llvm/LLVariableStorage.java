/**
 * 
 */
package org.ejs.eulang.llvm;

import java.util.HashMap;
import java.util.Map;

import org.ejs.eulang.TypeEngine;
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
}