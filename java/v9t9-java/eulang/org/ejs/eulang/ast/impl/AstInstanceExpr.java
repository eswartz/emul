/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstInstanceExpr;
import org.ejs.eulang.ast.IAstNamedType;
import org.ejs.eulang.ast.IAstSelfReferentialType;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLInstanceType;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLUpType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstInstanceExpr extends AstType implements IAstInstanceExpr {

	
	private IAstNodeList<IAstTypedExpr> exprs;
	private IAstSymbolExpr symbolExpr;

	/**
	 * @param typeEngine 
	 * @param idExpr 
	 * @param scope
	 */
	public AstInstanceExpr(TypeEngine typeEngine, IAstSymbolExpr idExpr, IAstNodeList<IAstTypedExpr> typeVariables) {
		super(null);
		setSymbolExpr(idExpr);
		setExprs(typeVariables);
		
		if (exprs != null) {
			LLType[] types = new LLType[exprs.nodeCount()];
			int idx = 0;
			for (IAstTypedExpr expr : exprs.list()) {
				LLType aType = expr.getType();
				types[idx++] = aType;
			}
			LLInstanceType type = typeEngine.getInstanceType(symbolExpr.getSymbol(), types);
			
			setType(type);
		}
	}
	

	protected AstInstanceExpr(IAstSymbolExpr idExpr, IAstNodeList<IAstTypedExpr> typeVariables, LLType type) {
		super(type);
		setSymbolExpr(idExpr);
		setExprs(typeVariables);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((exprs == null) ? 0 : exprs.hashCode());
		result = prime * result
				+ ((symbolExpr == null) ? 0 : symbolExpr.hashCode());
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
		AstInstanceExpr other = (AstInstanceExpr) obj;
		if (exprs == null) {
			if (other.exprs != null)
				return false;
		} else if (!exprs.equals(other.exprs))
			return false;
		if (symbolExpr == null) {
			if (other.symbolExpr != null)
				return false;
		} else if (!symbolExpr.equals(other.symbolExpr))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("INSTANCE");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGenericExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstInstanceExpr copy(IAstNode parent) {
		return fixup(this, new AstInstanceExpr(doCopy(symbolExpr, parent), doCopy(exprs, parent), getType()));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInstanceExpr#getSymbolExpr()
	 */
	@Override
	public IAstSymbolExpr getSymbolExpr() {
		return symbolExpr;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInstanceExpr#setSymbolExpr(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setSymbolExpr(IAstSymbolExpr symExpr) {
		this.symbolExpr = reparent(this.symbolExpr, symExpr);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGenericExpr#getTypeVariables()
	 */
	@Override
	public IAstNodeList<IAstTypedExpr> getExprs() {
		return exprs;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGenericExpr#setTypeVariables(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setExprs(IAstNodeList<IAstTypedExpr> list) {
		this.exprs = reparent(this.exprs, list);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (exprs != null)
			return new IAstNode[] { symbolExpr, exprs };
		else
			return new IAstNode[] { symbolExpr };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == symbolExpr)
			setSymbolExpr((IAstSymbolExpr) another);
		else if (existing == exprs)
			setExprs((IAstNodeList<IAstTypedExpr>) another);
		else
			throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {

		boolean changed = false;

		if (getType() == null) {
			LLType[] types;
			LLType type;
			if (exprs != null) {
				types = new LLType[exprs.nodeCount()];
				int idx = 0;
				for (IAstTypedExpr expr : exprs.list()) {
					LLType aType = expr.getType();
					types[idx++] = aType;
				}
				type = typeEngine.getInstanceType(symbolExpr.getSymbol(), types);
			} else {
				type = symbolExpr.getType();
			}
			
			//this.setType(type);
			//changed = true;
			changed |= updateType(this, type);
			
		}
		return changed;
	}
}
