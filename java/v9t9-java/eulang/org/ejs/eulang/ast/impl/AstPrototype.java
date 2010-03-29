/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.Arrays;

import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstPrototype;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstPrototype extends AstTypedNode implements IAstPrototype {
	private IAstType retType;
	private final IAstArgDef[] argumentTypes;

	/** Create with the types; may be null */
	public AstPrototype(TypeEngine typeEngine, IAstType retType, IAstArgDef[] argumentTypes) {
		this.retType = retType;
		retType.setParent(this);
		this.argumentTypes = argumentTypes;
		for (IAstArgDef arg : argumentTypes)
			arg.setParent(this);
		setType(typeEngine.getCodeType(retType, argumentTypes));
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 55;
		result = prime * result + Arrays.hashCode(argumentTypes);
		result = prime * result + ((retType == null) ? 0 : retType.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstPrototype other = (AstPrototype) obj;
		if (!Arrays.equals(argumentTypes, other.argumentTypes))
			return false;
		if (retType == null) {
			if (other.retType != null)
				return false;
		} else if (!retType.equals(other.retType))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "(" + catenate(argumentTypes) + " => " + (retType != null ? retType.toString() : "<Object>") + ")"; 
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#argumentTypes()
	 */
	@Override
	public IAstArgDef[] argumentTypes() {
		return argumentTypes;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#getReturnType()
	 */
	@Override
	public IAstType returnType() {
		return retType;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		IAstNode[] children = new IAstNode[argumentTypes.length + 1];
		children[0] = retType;
		System.arraycopy(argumentTypes, 0, children, 1, argumentTypes.length);
		return children;
	}
	@Override
	public void replaceChildren(IAstNode[] children) {
		throw new UnsupportedOperationException();
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		LLCodeType newType = null;
		
		LLType[] argTypes = new LLType[argumentTypes.length];
		LLType tretType = retType.getType();
		for (int i = 0; i < argumentTypes.length; i++) {
			IAstArgDef argDef = argumentTypes[i];
			argTypes[i] = argDef.getType();
		}
		
		newType = typeEngine.getCodeType(tretType, argTypes);
		
		return updateType(this, newType);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstPrototype#adaptToType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean adaptToType(LLCodeType codeType) {
		boolean changed = false;
		changed = updateType(retType, codeType.getRetType());
		for (int  i = 0; i < argumentTypes.length; i++) {
			changed |= updateType(argumentTypes[i], codeType.getArgTypes()[i]);
		}
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		super.setType(type);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstPrototype#getArgCount()
	 */
	@Override
	public int getArgCount() {
		return argumentTypes.length;
	}
}
