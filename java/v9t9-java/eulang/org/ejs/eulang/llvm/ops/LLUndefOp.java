/**
 * 
 */
package org.ejs.eulang.llvm.ops;


/**
 * @author ejs
 *
 */
public class LLUndefOp implements LLOperand {

	public LLUndefOp() {
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "undef";
	}
}
