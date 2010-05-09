/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class StackLocal extends BaseLocal {

	private final ISymbol startLabel;
	private int offset;

	public StackLocal(ISymbol name, LLType type, int size, ISymbol startLabel, int offset) {
		super(name, type, size);
		this.startLabel = startLabel;
		this.offset = offset;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.BaseLocal#toString()
	 */
	@Override
	public String toString() {
		return "stack " + super.toString() + " @FP+>" + HexUtils.toHex4(offset);
	}

	public ISymbol getStartLabel() {
		return startLabel;
	}
	
	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
