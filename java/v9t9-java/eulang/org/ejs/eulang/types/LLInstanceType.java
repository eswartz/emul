/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Arrays;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.symbols.ISymbol;

/**
 * This is a reference to an instance expression's type (generic types as children)
 * @author ejs
 *
 */
public class LLInstanceType extends BaseLLAggregateType {

	private final LLType[] types;
	private final ISymbol symbol;

	/**
	 * @param name
	 * @param bits
	 * @param llvmType
	 * @param basicType
	 * @param subType
	 */
	public LLInstanceType(ISymbol symbol, LLType[] types) {
		super(toLLVMString(symbol, types), 1, null /*toLLVMString(symbol, types)*/, BasicType.DATA, null, types == null);
		this.symbol = symbol;
		this.types = types != null ? types : NO_TYPES;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + symbol.hashCode();
		result = prime * result + Arrays.hashCode(types);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass()) {
			if (obj instanceof LLType) {
				return name.equals(((LLType) obj).getName());
			}
			return false;
		}
		if (!super.equals(obj))
			return false;
		if (!symbol.equals(((LLInstanceType) obj).getSymbol()))
			return false;
		return true;
	}


	/**
	 * @param symbol 
	 * @param types
	 * @return
	 */
	private static String toLLVMString(ISymbol symbol, LLType[] types) {
		if (types == null)
			return "<instance>";
		
		StringBuilder sb = new StringBuilder();
		sb.append(symbol.getName());
		boolean first = true;
		sb.append("<");
		for (LLType type : types) {
			if (first) first = false; else sb.append(',');
			sb.append(type != null ? (type.getLLVMName() != null ? type.getLLVMName() : type.getName()) : "<unknown>");
		}
		sb.append('>');
		return sb.toString();
	}

	/**
	 * @return
	 */
	public LLType[] getTypes() {
		return types;
	};

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getCount()
	 */
	@Override
	public int getCount() {
		return types.length;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLAggregateType#getType(int)
	 */
	@Override
	public LLType getType(int idx) {
		return types[idx];
	}
	
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return typeEngine.getInstanceType(symbol, type);
	}

	@Override
	public boolean isCompatibleWith(LLType target) {
		if (target != null) {
			boolean equal = getSymbol().getUniqueName().equals(target.getName());
			if (!equal && target instanceof LLDataType) {
				equal = ((LLDataType) target).getSymbol().getName().equals(getSymbol().getName());
			}
			return equal;
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
	/*
	@Override
	public boolean isComplete() {
		return false;
	}
	*/
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isGeneric()
	 */
	/*
	@Override
	public boolean isGeneric() {
		return false;
	}
	*/
	
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
