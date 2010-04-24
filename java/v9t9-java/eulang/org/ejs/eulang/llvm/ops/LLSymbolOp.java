/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public class LLSymbolOp extends BaseLLOperand {
	private ISymbol symbol;
	public LLSymbolOp(ISymbol symbol) {
		super(symbol.getType());
		this.symbol = symbol;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return symbol.getLLVMName();
	}
	
	
	public String getName() {
		return symbol.getLLVMName();
	}
	/**
	 * @return the symbol
	 */
	public ISymbol getSymbol() {
		return symbol;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLSymbolOp other = (LLSymbolOp) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
	
}
