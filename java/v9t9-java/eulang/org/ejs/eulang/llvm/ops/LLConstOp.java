/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLIntType;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLConstOp extends BaseLLOperand {

	private static final LLType I32 = new LLIntType(null, 32);
	private final Number value;
	public LLConstOp(Number value) {
		super(I32);
		this.value = value;
	}
	public LLConstOp(LLType type, Number value) {
		super(type);
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.BaseLLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return true;
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
