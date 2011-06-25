/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSelfReferentialType;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstSelfReferentialType extends AstType implements IAstSelfReferentialType {

	private ISymbol symbol;
	private IAstSymbolExpr symbolExpr;

	/**
	 * @param level 
	 * @param type
	 */
	public AstSelfReferentialType(IAstSymbolExpr symbolExpr, int level) {
		//super(new LLUpType(symbolExpr.getOriginalSymbol(), level, symbolExpr.getType()));
		super(new LLSymbolType(symbolExpr.getSymbol()));
		setSymbolExpr(symbolExpr);
	}
	
	protected AstSelfReferentialType(IAstSymbolExpr symbolExpr, LLType type) {
		super(type);
		setSymbolExpr(symbolExpr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstSelfReferentialType copy() {
		AstSelfReferentialType copy = new AstSelfReferentialType(doCopy(symbolExpr), type);
		copy.symbol = symbol;
		copy = fixup(this, copy);
		return copy;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((symbol == null) ? 0 : symbol.getName().hashCode());
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
		AstSelfReferentialType other = (AstSelfReferentialType) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (symbol.getNumber() != other.symbol.getNumber())
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		if (symbolExpr == null)
			return "???";
		return typedString(symbol != null ? symbol.getName() : symbolExpr.getSymbol() != null ? symbolExpr.getSymbol().getName() : "<<unnamed>>");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstType#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { symbolExpr };
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == symbolExpr)
			setSymbolExpr((org.ejs.eulang.ast.IAstSymbolExpr) another);
		else
			throw new IllegalArgumentException();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		
		if (type == null || !type.isComplete()) {
			IScope theScope = getOwnerScope();
			int count = 0;
			while (theScope != null) {
				if (theScope.getOwner() instanceof IAstDataType) { // TODO: any named type
					count++;
					ISymbol name = ((IAstDataType) theScope.getOwner()).getTypeName();
					if (name != null && symbol.equals(name))
						break;
				}
				theScope = theScope.getParent();
			}
			assert count != 0;
			changed |= updateType(this, new LLSymbolType(symbol));
			//changed |= updateType(this, new LLUpType(symbol, count, getType()));
		}
		/*else {
			// if the type got concretized, we need to update
			IAstTypedExpr body = symbolExpr.getBody();
			if (body instanceof IAstDataType) { // TODO: any named type
				LLUpType upType = (LLUpType) type;
				String name = ((IAstDataType) body).getType().getName();
				if (name != null && !upType.getName().equals(name)) {
					setType(new LLUpType(symbol, upType.getLevel(), getType()));
					changed = true;
				}
			}
		}*/
 		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#setSymbol(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setSymbolExpr(IAstSymbolExpr name) {
		Check.checkArg(name);
		this.symbolExpr = reparent(this.symbolExpr, name);
		this.symbol = name.getOriginalSymbol();
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#getSymbol()
	 */
	@Override
	public IAstSymbolExpr getSymbolExpr() {
		return symbolExpr;
	}
}
