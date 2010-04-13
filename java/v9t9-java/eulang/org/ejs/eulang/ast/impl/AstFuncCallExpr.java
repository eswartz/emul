/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
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

		LLCodeType argCodeType = getArgInferredType(typeEngine);
		
		IAstTypedNode actualFunction = getRealTypedNode(function, argCodeType);
		
		if (canInferTypeFrom(actualFunction) && !argCodeType.isMoreComplete(actualFunction.getType())) {
			LLType type = actualFunction.getType();
			
			if (!(type instanceof LLCodeType)) {
				throw new TypeException("calling non-function: " + type.toString());  
			}

			codeType = (LLCodeType) type;
		} else  {
			codeType = argCodeType;
		}

		boolean changed = updateType(function, codeType);
		if (codeType.getRetType() != null && codeType.getRetType().isComplete()
				&& !codeType.getRetType().equals(getType())) {
			setType(codeType.getRetType());
			changed = true;
		}
		// insert casts of arguments
		if (codeType.isComplete() && !codeType.isGeneric()) {
			for (int idx = 0; idx < arguments.nodeCount(); idx++) {
				IAstTypedExpr arg = arguments.list().get(idx);
				LLType argType = codeType.getArgTypes()[idx];
				arguments.replaceChild(arg, createCastOn(typeEngine, arg, argType));
			}
		}
	
		return changed;
	}
	
	private LLCodeType getArgInferredType(TypeEngine typeEngine) {
		LLCodeType codeType;
		LLType[] infArgTypes = new LLType[arguments.nodeCount()];
		int argIdx = 0;
		
		for (IAstTypedExpr arg : arguments.list()) {
			if (canInferTypeFrom(arg))
				infArgTypes[argIdx] = arg.getType();
			argIdx++;
		}
		
		LLType infRetType = getType();
		
		codeType = typeEngine.getCodeType(infRetType, infArgTypes);
		return codeType;
	}
	
	private IAstTypedNode getRealTypedNode(IAstTypedExpr node, LLType codeType) {
		if (node instanceof IAstSymbolExpr) {
			IAstSymbolExpr symbolExpr = (IAstSymbolExpr) node;
			if (symbolExpr.getDefinition() != null) {
				IAstTypedExpr expr = symbolExpr.getInstance();
				return expr;
			}
			if (!(symbolExpr.getSymbol().getDefinition() instanceof ITyped))
				return null;
			return ((IAstTypedNode)node);
		} 
		return null;
	}
	
	 /* (non-Javadoc)
     * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes()
     */
    @Override
    public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
    	if (!(function.getType() instanceof LLCodeType)) {
    		throw new TypeException(function, "calling non-function");
    	}
    }
}
