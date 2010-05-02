/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.symbols.ISymbol;

/**
 * This is a reference to a type behind a symbol.
 * @author ejs
 *
 */
public class LLSymbolType extends BaseLLType {

	private final ISymbol symbol;

	/**
	 * @param name
	 * @param bits
	 * @param llvmType
	 * @param basicType
	 * @param subType
	 */
	public LLSymbolType(ISymbol symbol) {
		super(symbol.getUniqueName(), 1, symbol.getLLVMName(), BasicType.DATA, null);
		this.symbol = symbol;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + symbol.hashCode();
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass()) {
			if (obj instanceof LLType) {
				return symbol.getUniqueName().equals(((LLType) obj).getName());
			}
			return false;
		}
		if (!super.equals(obj))
			return false;
		return true;
	}

	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return this;
	}

	@Override
	public boolean isCompatibleWith(LLType target) {
		if (target != null) {
			return getSymbol().getUniqueName().equals(target.getName());
		}
		return super.isCompatibleWith(target);
	}

	/**
	 * @return
	 */
	public ISymbol getSymbol() {
		return symbol;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return symbol.getType() != null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isGeneric()
	 */
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#substitute(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, LLType fromType,
			LLType toType) {
		if (fromType == null || fromType.equals(this))
			return toType;
		return super.substitute(typeEngine, fromType, toType);
	}

	protected boolean subTypesCompatible(LLType subType) {
		return true;
	}
}
