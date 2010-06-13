/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.llvm.LLLinkage;
import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.symbols.ISymbol;

/**
 * Global data
 * @author ejs
 *
 */
public class LLGlobalDirective extends LLBaseDirective {

	private final ISymbol symbol;
	private final LLLinkage linkage;
	private final LLOperand data;
	private boolean appending;

	/**
	 * @param symbol
	 * @param dataOp
	 * @param default1
	 * @param internal
	 */
	public LLGlobalDirective(ISymbol symbol, LLLinkage linkage,
			LLOperand dataOp) {
		this.symbol = symbol;
		this.linkage = linkage;
		this.data = dataOp;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((linkage == null) ? 0 : linkage.hashCode());
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
		LLGlobalDirective other = (LLGlobalDirective) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (linkage == null) {
			if (other.linkage != null)
				return false;
		} else if (!linkage.equals(other.linkage))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.LLBaseDirective#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		String symName = symbol.getLLVMName();
		sb.append(symName).append(" = ");
		if (linkage != null)
			sb.append(linkage.getLinkageName()).append(' ');	
		if (appending)
			sb.append("appending ");
		sb.append("global ");
		
		sb.append(data.getType().getLLVMName()).append(' ');
		
		sb.append(data);
		return sb.toString();
	}

	/**
	 * 
	 */
	public LLOperand getInit() {
		return data;
	}


	/**
	 * @return
	 */
	public ISymbol getSymbol() {
		return symbol;
	}


	/**
	 * @param b
	 */
	public void setAppending(boolean b) {
		this.appending = b;
	}
	
	/**
	 * @return the appending
	 */
	public boolean isAppending() {
		return appending;
	}

}
