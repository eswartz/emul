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
	/** Get the size in bytes used for the local: should generally match #getType()#getBits()/8 */
	int getSize();
}
