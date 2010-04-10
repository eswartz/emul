/**
 * 
 */
package org.ejs.eulang.llvm.ops;

/**
 * @author ejs
 *
 */
public class LLConstOp implements LLOperand {

	private final Number value;
	public LLConstOp(Number value) {
		this.value = value;
	}
	
	/**
	 * @return the value
	 */
	public Number getValue() {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value.toString();
	}
}
