/**
 * 
 */
package org.ejs.eulang.types;

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
	 * @param bits
	 * @param llvmType
	 * @param basicType
	 * @param subType
	 */
	public LLUpType(String name, ISymbol symbol, int level) {
		super(name, 1, "%" + name, BasicType.DATA, null);
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
		if (!super.equals(obj))
			return false;
		LLUpType other = (LLUpType) obj;
		if (level != other.level)
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
			// HACK: type inference tends to replicate classes endlessly,
			// so there will always be a temporary instance... pretend they're all the same for now
			String targetName = target.getName();
			int idx = targetName.indexOf('.');
			if (idx > 0)
				targetName = targetName.substring(0, idx);
			return getName().equals(targetName);
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
}
