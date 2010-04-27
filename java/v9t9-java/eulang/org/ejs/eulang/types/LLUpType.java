/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.ISymbol;

/**
 * This refers to an enclosing type.  It breaks circular references when defining a type.
 * @author ejs
 *
 */
public class LLUpType extends BaseLLType {

	private int level;
	private final ISymbol symbol;
	
	/**
	 * @param symbol
	 * @param actualType TODO
	 * @param bits
	 * @param llvmType
	 * @param basicType
	 * @param subType
	 */
	public LLUpType(String name, ISymbol symbol, int level, LLType actualType) {
		super(name, 1, "%" + name, BasicType.DATA, actualType instanceof LLUpType ? actualType.getSubType() : actualType);
		this.symbol = symbol;
		this.level = level;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + level;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) {
			return false;
		}
		// don't check super.equals, which checks subtypes
		LLUpType other = (LLUpType) obj;
		if (!getName().equals(other.getName()))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return level != 0;
	}

	public int getLevel() { 
		return level;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isCompatibleWith(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isCompatibleWith(LLType target) {
		if (target != null) {
			/*
			// HACK: type inference tends to replicate classes endlessly,
			// so there will always be a temporary instance... pretend they're all the same for now
			String targetName = target.getName();
			int idx = targetName.indexOf('.');
			if (idx > 0)
				targetName = targetName.substring(0, idx);
			return getName().equals(targetName);
			*/
			return getSymbol().getName().equals(target.getName());
		}
		return super.isCompatibleWith(target);
	}
	
	/**
	 * @return the symbol
	 */
	public ISymbol getSymbol() {
		return symbol;
	}
	
	public IAstType getRealType() {
		IAstNode node = getSymbol().getDefinition();
		if (node instanceof IAstDefineStmt) {
			IAstTypedExpr match = ((IAstDefineStmt) node).getMatchingBodyExpr(null);
			if (match instanceof IAstType)
				return (IAstType) match;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#matchesExactly(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean matchesExactly(LLType target) {
		IAstType realType = getRealType();
		if (realType != null && realType.getType().equals(target))
			return true;
		return super.matchesExactly(target);
	}
	
	protected boolean subTypesCompatible(LLType subType) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#substitute(org.ejs.eulang.TypeEngine, java.lang.String, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, LLType fromType, LLType toType) {
		if (fromType == null || fromType.equals(this))
			return toType;
		if (subType != null) {
			LLType newSub = subType.substitute(typeEngine, fromType, toType);
			if (newSub != subType)
				return new LLUpType(name, symbol, level, newSub);
		}
		return this;
	}
}
