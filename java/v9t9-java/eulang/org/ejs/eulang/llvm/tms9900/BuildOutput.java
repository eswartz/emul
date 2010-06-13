/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.eulang.llvm.tms9900.DataBlock;
import org.ejs.eulang.llvm.tms9900.Routine;
import org.ejs.eulang.symbols.ISymbol;

/**
 * This is the output from a build phase (compiler)
 * @author ejs
 *
 */
public class BuildOutput {

	private HashMap<ISymbol, Routine> routineMap;
	private HashMap<ISymbol, DataBlock> dataBlockMap;
	private List<ISymbol> staticInits;

	public BuildOutput() {
		routineMap = new HashMap<ISymbol, Routine>();
		dataBlockMap = new HashMap<ISymbol, DataBlock>();
		staticInits = new ArrayList<ISymbol>();
	}
	
	public void register(Routine routine) {
		assert !routineMap.containsKey(routine.getName());
		routineMap.put(routine.getName(), routine);
	}
	
	public void register(DataBlock data) {
		assert !dataBlockMap.containsKey(data.getName());
		dataBlockMap.put(data.getName(), data);
	}
	
	public void registerStaticInit(ISymbol sym) {
		staticInits.add(sym);
	}
	
	/**
	 * @return the staticInits
	 */
	public List<ISymbol> getStaticInits() {
		return staticInits;
	}

	protected boolean symbolMatches(ISymbol sym, String string) {
		return sym.getUniqueName().equals(string)
				 || sym.getName().equals(string)
				 || sym.getUniqueName().startsWith("%" + string)
				 || sym.getUniqueName().startsWith("@" + string)
				 || sym.getUniqueName().startsWith(string + ".")
				 || sym.getUniqueName().contains("." + string + ".");
	}
	
	public Routine getRoutine(ISymbol sym) {
		return routineMap.get(sym);
	}
	public Routine lookupRoutine(String name) {
		for (Map.Entry<ISymbol, Routine> entry : routineMap.entrySet()) {
			if (symbolMatches(entry.getKey(), name)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public DataBlock getDataBlock(ISymbol sym) {
		return dataBlockMap.get(sym);
	}
	public DataBlock lookupDataBlock(String name) {
		for (Map.Entry<ISymbol, DataBlock> entry : dataBlockMap.entrySet()) {
			if (symbolMatches(entry.getKey(), name)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public Routine[] getRoutines() {
		return routineMap.values().toArray(new Routine[routineMap.size()]);
	}
	public DataBlock[] getDataBlocks() {
		return dataBlockMap.values().toArray(new DataBlock[dataBlockMap.size()]);
	}

}
