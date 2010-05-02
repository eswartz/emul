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
public class AstInstanceExpr extends AstTypedExpr implements IAstInstanceExpr {

	
	private IAstNodeList<IAstTypedExpr> exprs;
	private IAstSymbolExpr symbolExpr;

	/**
	 * @param idExpr 
	 * @param scope
	 */
	public AstInstanceExpr(IAstSymbolExpr idExpr, IAstNodeList<IAstTypedExpr> typeVariables) {
		setSymbolExpr(idExpr);
		setExprs(typeVariables);
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
		return fixup(this, new AstInstanceExpr(doCopy(symbolExpr, parent), doCopy(exprs, parent)));
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
		return new IAstNode[] { symbolExpr, exprs };
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
			LLType[] types = new LLType[exprs.nodeCount()];
			int idx = 0;
			for (IAstTypedExpr expr : exprs.list()) {
				types[idx++] = expr.getType();
			}
			LLInstanceType type = typeEngine.getInstanceType(symbolExpr.getSymbol(), types);
			
			this.setType(type);
			changed = true;
			//changed |= updateType(this, type);
			
			/*
			IAstTypedExpr body = symbolExpr.getBody();
			if (body != null) {
				try {
					ISymbol instance = symbolExpr.getDefinition().getInstanceForParameters(typeEngine, body.getType(), exprs.list());
					
					AstSymbolExpr newSymExpr = new AstSymbolExpr(instance);
					newSymExpr.setSourceRef(getSourceRef());
					IAstNamedType namedType = new AstNamedType(instance.getType(), newSymExpr);
					namedType.setSourceRef(getSourceRef());
					getParent().replaceChild(this, namedType);
					return true;
					
					
					//LLSymbolType type = new LLSymbolType(instance);
					//setType(type);
					//changed = true;
				} catch (ASTException e) {
					throw new TypeException(e.getNode(), e.getMessage());
				}
			}
			*/
		}
		return changed;
	}
}
