/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.*;
import org.ejs.eulang.symbols.ISymbol;

/**
 * This is a placeholder type that fills in leaf nodes of types in generic invocation
 * contexts.
 * @author ejs
 *
 */
public class LLGenericType extends BaseLLType {
	
	private final ISymbol symbol;

	private static String getUniqueSymbolName(ISymbol symbol) {
		if (symbol == null)
			return null;
		return symbol.getUniqueName() + "." + symbol.getNumber();
	}
	public LLGenericType(ISymbol symbol) {
		super(getUniqueSymbolName(symbol), 0, 
				/*getUniqueSymbolName(symbol)*/ null, 
						BasicType.GENERIC, null);
		this.symbol = symbol;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLGenericType other = (LLGenericType) obj;
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
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	/*
	@Override
	public boolean isComplete() {
		return false;
	}
	*/
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isMoreComplete(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isMoreComplete(LLType type) {
		return type == null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isGeneric()
	 */
	@Override
	public boolean isGeneric() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#substitute(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, LLType fromType,
			LLType toType) {
		// generics support replacing all generics if the fromType has no name
		if (fromType instanceof LLGenericType && ((LLGenericType) fromType).getName() == null)
			return toType;
		return super.substitute(typeEngine, fromType, toType);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#updateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType[])
	 */
	@Override
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return this;
	}
}
