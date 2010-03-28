/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstFuncCallExpr extends AstTypedExpr implements IAstFuncCallExpr {

	private IAstNodeList<IAstTypedExpr> arguments;
	private IAstTypedExpr function;

	public AstFuncCallExpr(IAstTypedExpr function, IAstNodeList<IAstTypedExpr> arguments) {
		setFunction(function);
		Check.checkArg(arguments);
		this.arguments = arguments;
		this.arguments.setParent(this);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "CALL" + ":" + getTypeString();
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((arguments == null) ? 0 : arguments.hashCode());
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
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
		AstFuncCallExpr other = (AstFuncCallExpr) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!arguments.equals(other.arguments))
			return false;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstFuncCallExpr#getFunction()
	 */
	@Override
	public IAstTypedExpr getFunction() {
		return function;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstFuncCallExpr#setFunction(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setFunction(IAstTypedExpr expr) {
		Check.checkArg(expr);
		this.function = reparent(this.function, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstFuncCallExpr#arguments()
	 */
	@Override
	public IAstNodeList<IAstTypedExpr> arguments() {
		return arguments;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { function, arguments };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstExpr#equalValue(org.ejs.eulang.ast.IAstExpr)
	 */
	@Override
	public boolean equalValue(IAstExpr expr) {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstExpr#simplify()
	 */
	@Override
	public IAstExpr simplify() {
		return this;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		
		LLCodeType codeType = null;

		if (canInferTypeFrom(function)) {
			LLType type = function.getType();
			if (!(type instanceof LLCodeType)) {
				throw new TypeException("calling non-function: " + type.toString());  
			}
			codeType = (LLCodeType) type;
		} else {
			return false;
		}

		return updateType(function, codeType) | updateType(this, codeType.getRetType());
	}

	/*
	 * if (function instanceof IAstSymbolExpr) {
			ISymbol funcSym = ((IAstSymbolExpr) function).getSymbol();
			IAstNode symdef = funcSym.getDefinition();
			IAstTypedExpr expr = null;
			if (symdef instanceof IAstDefineStmt) {
				expr = ((IAstDefineStmt) symdef).getExpr();
			} else {
				assert false;
			}
			if (!(expr instanceof IAstCodeExpr)) {
				throw new TypeException("Calling non-function");
			}
		}
	 */
}
