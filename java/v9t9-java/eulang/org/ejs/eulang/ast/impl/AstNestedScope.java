/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstSymbolDefiner;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public class AstNestedScope extends AstStmtScope implements IAstSymbolDefiner {


	private ISymbol symbol;


	/**
	 * @param stmtList
	 * @param scope
	 */
	public AstNestedScope(IAstNodeList<IAstStmt> stmtList, IScope scope, ISymbol symbol) {
		super(stmtList, scope);
		this.symbol = symbol;
	}

	public AstNestedScope copy(IAstNode copyParent) {
		return (AstNestedScope) fixupStmtScope(new AstNestedScope(
				doCopy(stmtList, copyParent), getScope().newInstance(getCopyScope(copyParent)),
				symbol));
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
		AstNestedScope other = (AstNestedScope) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString(symbol != null ? symbol.getName() : "NESTED");
	}

	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstSymbolDefiner#getSymbol()
	 */
	@Override
	public ISymbol getSymbol() {
		return symbol;
	}

}
