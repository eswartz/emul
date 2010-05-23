/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.llvm.tms9900.ILocal;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class SymbolOperand extends BaseHLOperand {

	private final ISymbol symbol;
	private final ILocal local;

	/**
	 * @param symbol
	 */
	public SymbolOperand(LLType type, ISymbol symbol, ILocal local) {
		super(type);
		this.symbol = symbol;
		this.local = local;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return symbol.getUniqueName();
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
		SymbolOperand other = (SymbolOperand) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}


	/**
	 * @return the symbol
	 */
	public ISymbol getSymbol() {
		return symbol;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand#getLocal()
	 */
	@Override
	public ILocal getLocal() {
		return local;
	}
	
	@Override
	public boolean isMemory() {
		return false;
	}

	@Override
	public boolean isRegister() {
		return false;
	}

}
