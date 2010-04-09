/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.ASTException;
import org.ejs.eulang.ast.IAstDefineStmt;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
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
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstFuncCallExpr copy(IAstNode copyParent) {
		return fixup(this, new AstFuncCallExpr(doCopy(function, copyParent), doCopy(arguments, copyParent)));
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("CALL");
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
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getFunction() == existing) {
			setFunction((IAstTypedExpr) another);
		} else if (arguments == existing) {
			arguments = (IAstNodeList<IAstTypedExpr>) ((IAstTypedExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpr#equalValue(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		
		LLCodeType codeType = null;

		IAstTypedNode actualFunction = getRealTypedNode(function);
		
		if (canInferTypeFrom(actualFunction)) {
			LLType type = actualFunction.getType();
			if (!(type instanceof LLCodeType)) {
				throw new TypeException("calling non-function: " + type.toString());  
			}
			codeType = (LLCodeType) type;
		} else {
			return false;
		}

		return updateType(function, codeType) | updateType(actualFunction, codeType) | updateType(this, codeType.getRetType());
	}

	private IAstTypedNode getRealTypedNode(IAstTypedExpr node) {
		if (node.getType() != null)
			return node;
		
		if (node instanceof IAstSymbolExpr) {
			IAstNode def = ((IAstSymbolExpr) node).getSymbol().getDefinition();
			if (def instanceof IAstDefineStmt) {
				// TODO: instances
				return ((IAstDefineStmt) def).getExpr();
			}
			if (!(def instanceof ITyped))
				return null;
			return ((IAstTypedNode)node);
		} 
		return null;
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
	
	 /* (non-Javadoc)
     * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes()
     */
    @Override
    public void validateChildTypes(TypeEngine typeEngine) throws ASTException {
    	if (!(function.getType() instanceof LLCodeType)) {
    		throw new ASTException(function, "calling non-function");
    	}
    }
}
