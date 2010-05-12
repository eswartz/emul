/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.eulang.llvm.instrs.LLInstr;
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
	
	/** Tell if local is used in more than one block.  */
	boolean isSingleBlock();
	void setSingleBlock(boolean single);
	
	/** The last instruction that uses this local */
	LLInstr getLastUse();
	void setLastUse(LLInstr instr);
}
