/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public interface LLOperand {
	boolean equals(Object obj);
	int hashCode();
	
	String toString();
	
	LLType getType();
	/**
	 * @return
	 */
	boolean isConstant();
	/**
	 * @param type
	 */
	void setType(LLType type);
}
