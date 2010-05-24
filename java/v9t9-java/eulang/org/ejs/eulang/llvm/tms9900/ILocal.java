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

	/**
	 * Get the type chosen to represent the local: should generally match
	 * ISymbol#getType()
	 */
	LLType getType();

	/**
	 * If this local is elected to occupy an incoming register, this is set, and
	 * should be used instead of this local
	 */
	ILocal getIncoming();

	/**
	 * Tell if the local is used in a return from the method
	 * @param outgoing
	 */
	void setOutgoing(boolean outgoing);
	
	/** Tell if the local is used in a return from the method */
	boolean isOutgoing();
	
	/* All the following only valid after Routine#setupForOptimization */
	
	/** Get instruction numbers for uses of the local */
	BitSet getUses();

	/** Get instruction numbers for definitions of the local */
	BitSet getDefs();

	/**
	 * Get the (first) definition of the local
	 */
	int getInit();
	
	/**
	 * Set the (first) definition instruction number
	 */
	void setInit(int number);
	
	/** 
	 * Tell if the local is used as expression temporary:
	 * 1) There is only one killing def (read+write in same instruction is allowed)
	 * 2) The value is not stored in memory where it may be referenced  
	 * @return
	 */
	boolean isExprTemp();
	/** 
	 * Tell if the local is used as expression temporary:
	 * 1) There is only one killing def (read+write in same instruction is allowed)
	 * 2) The value is not stored in memory where it may be referenced
	 * @param temp 
	 */
	void setExprTemp(boolean temp);
	
	/**
	 * Tell if the local's lifetime spans only one block (uses and defs
	 * in one block).
	 */
	boolean isSingleBlock();
	
	/**
	 * Tell if the local's lifetime spans only one block (uses and defs
	 * in one block).
	 */
	void setSingleBlock(boolean single);

}
