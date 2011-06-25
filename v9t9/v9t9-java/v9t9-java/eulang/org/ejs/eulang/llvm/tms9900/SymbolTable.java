/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.*;

import org.ejs.eulang.symbols.*;

/**
 * Lookups from ISymbol to backend structures
 * @author ejs
 *
 */
public class SymbolTable {

	private Map<ISymbol, Routine> symbolToRoutineMap;
	private Map<ISymbol, DataBlock> symbolToDataBlockMap;

	public SymbolTable() {
		symbolToRoutineMap = new HashMap<ISymbol, Routine>(); 
		symbolToDataBlockMap = new HashMap<ISymbol, DataBlock>(); 
	}
	
	public void addRoutine(Routine routine) {
		assert !symbolToRoutineMap.containsKey(routine.getName());
		symbolToRoutineMap.put(routine.getName(), routine);
	}
	
	public Routine getRoutine(ISymbol name) {
		return symbolToRoutineMap.get(name);
	}
	
	public void addDataBlock(DataBlock dataBlock) {
		assert !symbolToDataBlockMap.containsKey(dataBlock.getName());
		symbolToDataBlockMap.put(dataBlock.getName(), dataBlock);
		
	}
	
	public DataBlock getDataBlock(ISymbol name) {
		return symbolToDataBlockMap.get(name);
	}
}
