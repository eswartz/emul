/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstForExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstForExpr extends AstBodyLoopExpr implements IAstForExpr {

	private IAstNodeList<IAstSymbolExpr> symExprs;

	/**
	 * @param scope
	 * @param expr
	 * @param body
	 */
	public AstForExpr(IScope scope, IAstNodeList<IAstSymbolExpr> symExprs, IAstTypedExpr expr, IAstTypedExpr body) {
		super(scope, expr, body);
		setSymbolExprs(symExprs);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("FOR");
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstForExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstForExpr copy(IAstNode c) {
		return (IAstForExpr) fixupLoop(new AstForExpr(getScope().newInstance(getCopyScope(c)),
				doCopy(getSymbolExprs(), c),
				 doCopy(getExpr(), c), doCopy(getBody(), c)));
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstForExpr#getSymbolExprs()
	 */
	@Override
	public IAstNodeList<IAstSymbolExpr> getSymbolExprs() {
		return symExprs;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstForExpr#setSymbolExprs(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setSymbolExprs(IAstNodeList<IAstSymbolExpr> exprs) {
		this.symExprs = reparent(this.symExprs, exprs);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstBodyLoopExpr#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == symExprs)
			setSymbolExprs((IAstNodeList<IAstSymbolExpr>) another);
		else
			super.replaceChild(existing, another);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstBodyLoopExpr#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		boolean changed = false;
		
		if (expr.getType() == null) {
			changed |= updateType(expr, typeEngine.INT);
		}
		if (canInferTypeFrom(expr)) {
			LLType exprType = expr.getType();
			
			if (!exprType.isGeneric()) {
				// only int supported now
				if (exprType.getBasicType() != BasicType.INTEGRAL) 
					throw new TypeException(expr, "can only use 'for' on integers");

				LLType iterType = exprType;
				for (IAstSymbolExpr symExpr : symExprs.list()) {
					if (!iterType.equals(symExpr.getType())
							|| !iterType.equals(symExpr.getSymbol().getType())) {
						symExpr.setType(iterType);
						changed = true;
					}
				}
			}
		}
		
		changed |= super.inferTypeFromChildren(typeEngine);
		return changed;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstBodyLoopExpr#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		LLType exprType = expr.getType();
		
		// only int supported now
		if (exprType.getBasicType() != BasicType.INTEGRAL) 
			throw new TypeException(expr, "can only use 'for' on integers");
		
		super.validateChildTypes(typeEngine);
	}

}
