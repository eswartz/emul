/**
 * 
 */
package org.ejs.eulang.llvm.directives;

import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLTypeDirective extends LLBaseDirective {

	private final LLType type;
	private final ISymbol typeSymbol;

	/**
	 * 
	 */
	public LLTypeDirective(ISymbol typeSymbol, LLType type) {
		this.typeSymbol = typeSymbol;
		this.type = type;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result
				+ ((typeSymbol == null) ? 0 : typeSymbol.hashCode());
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
		LLTypeDirective other = (LLTypeDirective) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (typeSymbol == null) {
			if (other.typeSymbol != null)
				return false;
		} else if (!typeSymbol.equals(other.typeSymbol))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.directives.LLBaseDirective#toString()
	 */
	@Override
	public String toString() {
		return typeSymbol.getLLVMName() + " = type " + type.getLLVMType();
	}

}
