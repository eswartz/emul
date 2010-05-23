/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public interface ILocal {

	/** Get the original symbol */
	ISymbol getName();
	/** Get the type chosen to represent the local: should generally match ISymbol#getType() */
	LLType getType();
	
	/** If this local is elected to occupy an incoming register, this is set, and should
	 * be used instead of this local */
	ILocal getIncoming();

	/** Get the initialization of the local.  Pair(null, null) means argument. */
	Pair<Block, AsmInstruction> getInit();
	/** Get the initialization of the local.  Pair(null, null) means argument. */
	void setInit(Pair<Block, AsmInstruction> init);
	/** Get instruction uses of the local. */
	Map<Block, List<AsmInstruction>> getInstUses();
	/** Get instruction numbers for uses of the local */
	BitSet getUses();
}

