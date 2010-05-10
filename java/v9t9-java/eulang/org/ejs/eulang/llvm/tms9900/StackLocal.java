/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.coffee.core.utils.HexUtils;
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
	 * Get the offset in bytes, relative to the canonical frame pointer. such that
	 * 0 = the first argument, S1 = the second argument, S2 + S1 = the third ... (if args
	 * are pushed in reverse order) and -S1 = the first local, -S1 - S2 = the second local, ...
	 * <p>
	 * The actual offset in code will be different -- e.g., the offset to non-register
	 * arguments might be larger once pushed register space (including the previous FP) is accounted for.
	 * @return the offset in bytes, relative to the canonical frame pointer
	 * 
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
