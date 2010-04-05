/**
 * 
 */
package org.ejs.eulang.llvm;

/**
 * @author ejs
 *
 */
public class LLConstant {

	private final String literal;

	/**
	 * @param literal
	 */
	public LLConstant(String literal) {
		this.literal = literal;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return literal;
	}
	
}
