/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.Arrays;

import org.ejs.coffee.core.utils.Check;
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
		return (retType != null ? retType.toString() : "<Object>") + " (" + catenate(argumentTypes) + ")"; 
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

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public LLType inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		LLType[] argTypes = new LLType[argumentTypes.length];
		LLType tretType = null;
		if (retType != null)
			tretType = retType.getType();
		for (int i = 0; i < argumentTypes.length; i++) {
			IAstArgDef argDef = argumentTypes[i];
			argTypes[i] = argDef.getType();
		}
		
		return typeEngine.getCodeType(tretType, argTypes);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#setTypeOnChildren(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setTypeOnChildren(TypeEngine typeEngine, LLType newType) {
		// new type is a prototype
		Check.checkArg(newType instanceof LLCodeType);
		LLCodeType codeType = (LLCodeType) newType;
		retType.setType(codeType.getRetType());
		LLType[] argTypes = codeType.getArgTypes();
		for (int  i = 0; i < argumentTypes.length; i++)
			argumentTypes[i].setType(argTypes[i]);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		super.setType(type);
		if (type != null) {
			Check.checkArg(type instanceof LLCodeType);
			LLCodeType codeType = (LLCodeType) type;
			if (retType != null)
				retType.setType(codeType.getRetType());
			for (int  i = 0; i < argumentTypes.length; i++)
				argumentTypes[i].setType(codeType.getArgTypes()[i]);
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstPrototype#getArgCount()
	 */
	@Override
	public int getArgCount() {
		return argumentTypes.length;
	}
}
