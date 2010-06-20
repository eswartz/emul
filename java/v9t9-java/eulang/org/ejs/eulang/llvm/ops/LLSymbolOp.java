/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLSymbolOp extends BaseLLOperand {
	private ISymbol symbol;
	public LLSymbolOp(ISymbol symbol) {
		super(symbol != null ? symbol.getType() : null);
		this.symbol = symbol;
	}
	
	/**
	 * @param symbol2
	 * @param type
	 */
	public LLSymbolOp(ISymbol symbol, LLType type) {
		super(type);
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

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.BaseLLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return false;
	}
	/**
	 * @param sym
	 */
	public void setSymbol(ISymbol sym) {
		this.symbol = sym;
		setType(sym != null ? sym.getType() : null);
	}
}
