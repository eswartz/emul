/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.Arrays;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstAddrOfExpr;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstFieldExpr;
import org.ejs.eulang.ast.IAstFuncCallExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstPointerType;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.types.BaseLLField;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLPointerType;
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
	public IAstFuncCallExpr copy() {
		return fixup(this, new AstFuncCallExpr(doCopy(function), doCopy(arguments)));
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
		
		ITyped realFunction = getRealFunction(function);
		
		LLCodeType codeType = null;

		LLCodeType argCodeType = getArgInferredType(typeEngine);
		
		LLType referencedType = realFunction != null ? realFunction.getType() : null;
		
		// XXX codeptr
		if (referencedType instanceof LLPointerType)
			referencedType = referencedType.getSubType();
		if (referencedType != null) {
			if (!(referencedType instanceof LLCodeType)) {
				throw new TypeException(function, "called function does not have code type");
			}
			if (!argCodeType.isCompatibleWith(referencedType)) {
				
				LLType[] argCodeTypes = argCodeType.getArgTypes();
				LLType[] refTypes = ((LLCodeType)referencedType).getArgTypes();
				
				// fix up any code ptr args
				if (argCodeTypes.length == refTypes.length) {
					for (int i = 0; i < argCodeTypes.length; i++) {
						if (argCodeTypes[i] != null && refTypes[i] != null && !argCodeTypes[i].isCompatibleWith(refTypes[i])) {
							
							// XXX codeptr
							// may be false alarm: if passing a code block to something expecting code (or ptr to code),
							// fix up here
							
							if (argCodeTypes[i] instanceof LLCodeType && refTypes[i] instanceof LLPointerType && argCodeTypes[i].isCompatibleWith(refTypes[i].getSubType())) {
								// replace with ADDROF(...)
								
								IAstTypedExpr arg = arguments.list().get(i);
								arg.setParent(null);
								IAstAddrOfExpr addrOf = new AstAddrOfExpr(arg);
								addrOf.setSourceRef(arg.getSourceRef());
								arguments.replaceChild(arg, addrOf);
								LLType argType = argCodeTypes[i];
								argType = typeEngine.getPointerType(argType);
								addrOf.setType(argType);
								return true;
							}
						}
					}
				}
					
				// don't throw unless all types are known 
				if (!(argCodeType.isComplete() && referencedType.isComplete()))
					return false;
				
				StringBuilder sb = new StringBuilder();
				sb.append("call does not match prototype");
				if (argCodeTypes.length != refTypes.length)
					sb.append(": expected " + refTypes.length + " arguments but got " + argCodeTypes.length);
				else {
					boolean first = true;
					if (argCodeType.getRetType() != null  
						&&	((LLCodeType) referencedType).getRetType() != null
						&&	!argCodeType.getRetType().equals(((LLCodeType) referencedType).getRetType())) 
					{
						sb.append(": return type is " + ((LLCodeType) referencedType).getRetType() + " but inferred " + argCodeType.getRetType());
						first = false;
					}
					for (int i = 0; i < argCodeTypes.length; i++) {
						if (argCodeTypes[i] != null && refTypes[i] != null && !argCodeTypes[i].isCompatibleWith(refTypes[i])) {
							if (first) sb.append(": "); else sb.append("; ");
							sb.append("argument " + (i+1) + " should be type " + refTypes[i] + " but got " + argCodeTypes[i]);
						}
					}
				}
					
				throw new TypeException(this, sb.toString());
			}
			if (!argCodeType.isMoreComplete(referencedType)) {
				if (!(referencedType instanceof LLCodeType)) {
					throw new TypeException("calling non-function: " + referencedType.toString());  
				}
				
				codeType = (LLCodeType) referencedType;
			}
			
		}
		if (codeType == null) {
			
			if (referencedType != null) {
				// fill in any holes
				LLType[] argTypes = Arrays.copyOf(argCodeType.getTypes(), argCodeType.getCount());
				LLType[] refTypes = referencedType.getTypes();
				if (argTypes.length == refTypes.length) {
					for (int i = 0; i < argTypes.length; i++) {
						if (argTypes[i] == null || (refTypes[i] != null && refTypes[i].isMoreComplete(argTypes[i])))
							argTypes[i] = refTypes[i];
					}
					codeType = (LLCodeType) argCodeType.updateTypes(typeEngine, argTypes);
				} else {
					codeType = argCodeType;
				}
			} else {
				codeType = argCodeType;
			}
		}

		boolean changed = false;
		
		changed |= updateType(realFunction, codeType);
		if (function != null && !(function.getType() instanceof LLPointerType))
			changed |= updateType(function, codeType);
		
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
			if (canInferTypeFrom(arg)) {
				LLType argType = arg.getType();
				infArgTypes[argIdx] = argType;
			}
			argIdx++;
		}
		
		LLType infRetType = getType();
		
		codeType = typeEngine.getCodeType(infRetType, infArgTypes);
		return codeType;
	}

	private ITyped getRealFunction(IAstTypedExpr node) {
		if (node instanceof IAstSymbolExpr) {
			IAstSymbolExpr symbolExpr = (IAstSymbolExpr) node;
			if (symbolExpr.getDefinition() != null) {
				IAstTypedExpr expr = symbolExpr.getInstance();
				return expr;
			}
			if (!(symbolExpr.getSymbol().getDefinition() instanceof ITyped))
				return null;
			
			IAstTypedNode typedNode = (IAstTypedNode) symbolExpr.getSymbol().getDefinition();
			if (typedNode.getType() instanceof LLPointerType) {
				if (typedNode instanceof IAstAllocStmt) {
					IAstAllocStmt alloc = (IAstAllocStmt) typedNode;
					if (alloc.getTypeExpr() instanceof IAstPointerType)
						return ((IAstPointerType) alloc.getTypeExpr()).getBaseType();
				}
			}
			return (IAstTypedNode)node;
		} 
		else if (node instanceof IAstFieldExpr) {
			IAstFieldExpr fieldExpr = (IAstFieldExpr) node;
			if (fieldExpr.getExpr().getType() instanceof LLDataType) {
				LLDataType data = (LLDataType) fieldExpr.getExpr().getType();
				BaseLLField field = data.getField(fieldExpr.getField().getName());
				if (field == null) {
					return null;
				}
				return field;
			}
		}
		return null;
	}

	 /* (non-Javadoc)
     * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes()
     */
    @Override
    public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
    	LLType functionType = function.getType();
    	// XXX codeptr
    	if (functionType instanceof LLPointerType)
    		functionType = functionType.getSubType();
    	if (!(functionType instanceof LLCodeType)) {
    		throw new TypeException(function, "calling non-function");
    	}
    }
}
