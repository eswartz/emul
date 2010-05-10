/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

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
	
	/** If this local is elected to occupy an incoming register, this is set */
	ILocal getIncoming();
}
