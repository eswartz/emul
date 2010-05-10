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
	private int byteOffset;

	public StackLocal(ISymbol name, LLType type, ISymbol startLabel, int byteOffset) {
		super(name, type);
		this.startLabel = startLabel;
		this.byteOffset = byteOffset;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.BaseLocal#toString()
	 */
	@Override
	public String toString() {
		return "stack " + super.toString() + " @FP+>" + HexUtils.toHex4(byteOffset);
	}

	public ISymbol getStartLabel() {
		return startLabel;
	}
	
	/**
	 * @return the offset in bytes, relative to the canonical frame pointer
	 */
	public int getOffset() {
		return byteOffset;
	}
	/**
	 * @param offset the offset to set in bytes, relative to the canonical frame pointer
	 */
	public void setOffset(int offset) {
		this.byteOffset = offset;
	}
}
